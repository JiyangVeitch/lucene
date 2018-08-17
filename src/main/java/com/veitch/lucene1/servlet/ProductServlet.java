package com.veitch.lucene1.servlet;

import com.veitch.lucene1.pojo.Product;
import com.veitch.lucene1.util.ProductUtil;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        // 查询关键字
        String key = request.getParameter("key");
        // 查询结果数目
        int numberPerPage = Integer.parseInt(request.getParameter("number"));
        // 创建session , 用session来保存索引
        HttpSession session = request.getSession();
        Directory index;
        try {
            /*
            *准备中文分词器
            * 测试表明,IKAnalyzer比smartcn好用,但是二者都有点问题
            *SmartChineseAnalyzer analyzer=new SmartChineseAnalyzer();
            * */
            IKAnalyzer analyzer = new IKAnalyzer();

            // 创建索引 ,若session中已经有了索引,那么就不再创建
            if (session.getAttribute("index") == null) {
                // 无索引时,调用createIndex方法来创建索引
                index = createIndex(analyzer);
                session.setAttribute("index", index);
            } else {
                // 取得session中已经创建好的索引
                index = (Directory) session.getAttribute("index");
            }
            //  查询器
            Query query = new QueryParser("name", analyzer).parse(key);
            //  搜索
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            ScoreDoc[] hits = searcher.search(query, numberPerPage).scoreDocs;
            //  显示查询结果
            request.setAttribute("title", "找到" + hits.length + "个结果");
            //关键字高亮显示
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));

            List<Product> list = new ArrayList<>();

            for (int i = 0; i < hits.length; ++i) {
                ScoreDoc scoreDoc = hits[i];
                int docId = scoreDoc.doc;
                Document d = searcher.doc(docId);
                List<IndexableField> fields = d.getFields();
                Product product = new Product();
                product.setSid(i + 1);
                product.setScore(scoreDoc.score);
                //循环打印出单行搜索结果的不同字段中的值
                for (IndexableField f : fields) {

                    if ("id".equals(f.name())) {
                        product.setId(Integer.valueOf(d.get(f.name())));
                    }

                    if ("name".equals(f.name())) {
                        TokenStream tokenStream = analyzer.tokenStream(f.name(), new StringReader(d.get(f.name())));
                        String fieldContent = highlighter.getBestFragment(tokenStream, d.get(f.name()));
                        product.setName(fieldContent);
                        /*System.out.println(product.getName());*/
                    }

                    if ("category".equals(f.name())) {
                        product.setCategory(d.get(f.name()));
                    }

                    if ("price".equals(f.name())) {
                        product.setPrice(Float.parseFloat(d.get(f.name())));
                    }

                    if ("place".equals(f.name())) {
                        product.setPlace(d.get(f.name()));
                    }

                }

                list.add(product);
            }
            //  关闭查询
            reader.close();
            request.setAttribute("list", list);

        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("list.jsp").forward(request, response);

    }


    private Directory createIndex(IKAnalyzer analyzer) throws Exception {
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);
        //此处为140k_products.txt文件所在的全路径
        String fileName = "D:\\project\\lucene1\\src\\main\\resources\\140k_products.txt";
        List<Product> products = ProductUtil.file2list(fileName);
        int total = products.size();
        int count = 0;
        int per = 0;
        int oldPer = 0;
        for (Product p : products) {
            addDoc(writer, p);
            count++;
            per = count * 100 / total;
            if (per != oldPer) {
                oldPer = per;
                System.out.printf("索引中，总共要添加 %d 条记录，当前添加进度是： %d%% %n", total, per);
            }

        }
        writer.close();
        return index;
    }

    private static void addDoc(IndexWriter w, Product p) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("id", String.valueOf(p.getId()), Field.Store.YES));
        doc.add(new TextField("name", p.getName(), Field.Store.YES));
        doc.add(new TextField("category", p.getCategory(), Field.Store.YES));
        doc.add(new TextField("price", String.valueOf(p.getPrice()), Field.Store.YES));
        doc.add(new TextField("place", p.getPlace(), Field.Store.YES));
        doc.add(new TextField("code", p.getCode(), Field.Store.YES));
        w.addDocument(doc);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
