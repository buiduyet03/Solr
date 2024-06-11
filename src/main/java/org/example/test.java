//package org.example;
//
//import org.apache.solr.client.solrj.SolrClient;
//import org.apache.solr.client.solrj.SolrRequest;
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.HttpSolrClient;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.SolrInputDocument;
//import org.apache.solr.common.params.SolrParams;
//import org.apache.solr.common.params.SolrQuery;
//
//import java.io.IOException;
//
//public class test {
//        String solrUrl = "http://localhost:8983/solr/mycore"; // Thay đổi thành URL của Solr và core của bạn
//        SolrClient solrClient = new HttpSolrClient.Builder(solrUrl).build();
//
//        try {
//            // Thêm tài liệu vào Solr
//            SolrInputDocument document = new SolrInputDocument();
//            document.addField("id", "1");
//            document.addField("name", "John Doe");
//            document.addField("age", 30);
//            solrClient.add(document);
//            solrClient.commit();
//
//            // Tìm kiếm tài liệu từ Solr
//            SolrQuery query = new SolrQuery();
//            query.setQuery("name:John Doe");
//            QueryResponse response = solrClient.query(query, SolrRequest.METHOD.GET);
//            response.getResults().forEach(result -> System.out.println("Found: " + result));
//
//            // Xóa tài liệu từ Solr
//            solrClient.deleteById("1");
//            solrClient.commit();
//
//        } catch (SolrServerException | IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                solrClient.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//}
