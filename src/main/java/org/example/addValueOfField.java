package org.example;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class addValueOfField {

    private static final String SOLR_URL = "http://localhost:8983/solr/";
    private static final Set<String> EXCLUDED_FIELDS = new HashSet<>(Set.of("_nest_path_", "_root_", "_text_", "_version_"));
    private static final List<String> FIELD_ORDER = List.of("field1", "field2", "field3"); // Thứ tự các trường mong muốn

    // Phương thức để lấy danh sách các trường từ managed-schema
    public List<String> getFieldsList(SolrClient solrClient, String coreName) throws IOException, SolrServerException {
        SchemaRequest.Fields request = new SchemaRequest.Fields();
        SchemaResponse.FieldsResponse response = request.process(solrClient, coreName);
        List<Map<String, Object>> fields = response.getFields();

        // Lấy tên của các trường
        List<String> fieldNames = fields.stream().map(field -> (String) field.get("name")).collect(Collectors.toList());

        // Sắp xếp lại theo thứ tự mong muốn
        fieldNames.sort(Comparator.comparingInt(FIELD_ORDER::indexOf));
        return fieldNames;
    }

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

    // Phương thức để thêm và truy vấn Solr
    public void addAndQuerySolr() {
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
            try (SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL).build()) {

                // Lấy danh sách các trường của core đã chọn từ managed-schema
                List<String> fieldsList = getFieldsList(solrClient, selectedCore);

                // Tạo một tài liệu Solr mới
                SolrInputDocument document = new SolrInputDocument();

                // Yêu cầu người dùng nhập giá trị cho từng trường
                for (String fieldName : fieldsList) {
                    if (!EXCLUDED_FIELDS.contains(fieldName)) { // Bỏ qua các trường không cần thiết
                        System.out.print("Enter value for " + fieldName + ": ");
                        String value = scanner.nextLine();
                        document.addField(fieldName, value);
                    }
                }

                try (SolrClient coreSolrClient = new HttpSolrClient.Builder(SOLR_URL + selectedCore).build()) {
                    // Thêm tài liệu vào Solr
                    coreSolrClient.add(document);
                    coreSolrClient.commit();
                    System.out.println("Document added successfully!");

                    // Tạo truy vấn Solr
                    SolrQuery query = new SolrQuery();
                    query.setQuery("*:*");

                    // Thực hiện truy vấn
                    QueryResponse response = coreSolrClient.query(query);
                    SolrDocumentList documents = response.getResults();

                    // Xử lý kết quả truy vấn
                    System.out.println("Found " + documents.getNumFound() + " documents");
                    documents.forEach(doc -> System.out.println("Document: " + doc));
                }
            }

        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
    }
}

//public class Main {
//    public static void main(String[] args) {
//        addValueOfField solrOps = new addValueOfField();
//        solrOps.addAndQuerySolr();
//    }
//}