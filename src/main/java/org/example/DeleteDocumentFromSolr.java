package org.example;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeleteDocumentFromSolr {

    private static final String SOLR_URL = "http://localhost:8983/solr/";

    // Phương thức để lấy danh sách các core
    public List<String> getCoreList() throws IOException {
        List<String> coreList = new ArrayList<>();
        URL url = new URL(SOLR_URL + "admin/cores?action=STATUS");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        while ((output = br.readLine()) != null) {
            if (output.contains("\"name\"")) {
                String coreName = output.split("\"name\":\"")[1].split("\",")[0];
                coreList.add(coreName);
            }
        }
        br.close();
        conn.disconnect();
        return coreList;
    }

    // Phương thức để xóa tài liệu khỏi Solr
    public void deleteFromSolr() {
        Scanner scanner = new Scanner(System.in);

        try {
            // Lấy danh sách các core và hiển thị cho người dùng chọn
            List<String> coreList = getCoreList();
            System.out.println("Available cores:");
            for (int i = 0; i < coreList.size(); i++) {
                System.out.println((i + 1) + ". " + coreList.get(i));
            }

            // Cho phép người dùng chọn một core
            System.out.print("Select a core by number: ");
            int coreIndex = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            if (coreIndex < 1 || coreIndex > coreList.size()) {
                System.out.println("Invalid core selection.");
                return;
            }
            String selectedCore = coreList.get(coreIndex - 1);

            // Tạo SolrClient cho core đã chọn
            try (SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL + selectedCore).build()) {

                // Hiển thị danh sách các tài liệu hiện có trong core
                SolrQuery query = new SolrQuery();
                query.setQuery("*:*");
                query.setRows(100);  // Giới hạn số lượng tài liệu hiển thị (có thể thay đổi tùy theo nhu cầu)

                QueryResponse queryResponse = solrClient.query(query);
                SolrDocumentList documents = queryResponse.getResults();

                System.out.println("Existing documents in the core:");
                for (SolrDocument document : documents) {
                    System.out.println("ID: " + document.getFieldValue("id") + ", Fields: " + document);
                }

                // Yêu cầu người dùng nhập ID của tài liệu cần xóa
                System.out.print("Enter the ID of the document to delete: ");
                String documentId = scanner.nextLine();

                // Xóa tài liệu khỏi Solr
                UpdateResponse deleteResponse = solrClient.deleteById(documentId);
                solrClient.commit();
                System.out.println("Document deleted successfully!");

                // Kiểm tra phản hồi
                if (deleteResponse.getStatus() == 0) {
                    System.out.println("Document with ID " + documentId + " has been deleted successfully.");
                } else {
                    System.out.println("Failed to delete document with ID " + documentId + ". Status: " + deleteResponse.getStatus());
                }
            }

        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
    }
}
