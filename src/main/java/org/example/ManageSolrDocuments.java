package org.example;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManageSolrDocuments {

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
        System.out.print("Select a core by number (or type 'cancel' to cancel): ");
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            System.out.println("Operation cancelled.");
            return;
        }
        int coreIndex = Integer.parseInt(input);
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
            System.out.print("Enter the ID of the document to delete (or type 'cancel' to cancel): ");
            String documentId = scanner.nextLine();
            if ("cancel".equalsIgnoreCase(documentId)) {
                System.out.println("Operation cancelled.");
                return;
            }

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


    // Phương thức để sửa thông tin tài liệu trong Solr
    public void updateDocumentInSolr() {
    Scanner scanner = new Scanner(System.in);

    try {
        // Lấy danh sách các core và hiển thị cho người dùng chọn
        List<String> coreList = getCoreList();
        System.out.println("Available cores:");
        for (int i = 0; i < coreList.size(); i++) {
            System.out.println((i + 1) + ". " + coreList.get(i));
        }

        // Cho phép người dùng chọn một core
        System.out.print("Select a core by number (or type 'cancel' to cancel): ");
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            System.out.println("Operation cancelled.");
            return;
        }
        int coreIndex = Integer.parseInt(input);
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

            // Yêu cầu người dùng nhập ID của tài liệu cần sửa
            System.out.print("Enter the ID of the document to update (or type 'cancel' to cancel): ");
            String documentId = scanner.nextLine();
            if ("cancel".equalsIgnoreCase(documentId)) {
                System.out.println("Operation cancelled.");
                return;
            }

            // Tạo tài liệu Solr mới để cập nhật
            SolrInputDocument updatedDocument = new SolrInputDocument();
            updatedDocument.addField("id", documentId);

            // Yêu cầu người dùng nhập các trường cần cập nhật
            System.out.println("Enter the fields to update (key=value), one per line. Type 'done' to finish or 'cancel' to cancel:");
            while (true) {
                String inputField = scanner.nextLine();
                if ("done".equalsIgnoreCase(inputField)) {
                    break;
                }
                if ("cancel".equalsIgnoreCase(inputField)) {
                    System.out.println("Operation cancelled.");
                    return;
                }
                String[] parts = inputField.split("=");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String value = parts[1].trim();
                    updatedDocument.addField(field, value);
                } else {
                    System.out.println("Invalid input. Please enter in 'key=value' format.");
                }
            }

            // Cập nhật tài liệu trong Solr
            UpdateResponse updateResponse = solrClient.add(updatedDocument);
            solrClient.commit();

            // Kiểm tra phản hồi
            if (updateResponse.getStatus() == 0) {
                System.out.println("Document with ID " + documentId + " has been updated successfully.");
            } else {
                System.out.println("Failed to update document with ID " + documentId + ". Status: " + updateResponse.getStatus());
            }
        }

    } catch (IOException | SolrServerException e) {
        e.printStackTrace();
    }
}


    // Phương thức để thêm tài liệu mới vào Solr
    public void addDocumentToSolr() {
    Scanner scanner = new Scanner(System.in);

    try {
        // Lấy danh sách các core và hiển thị cho người dùng chọn
        List<String> coreList = getCoreList();
        System.out.println("Available cores:");
        for (int i = 0; i < coreList.size(); i++) {
            System.out.println((i + 1) + ". " + coreList.get(i));
        }

        // Cho phép người dùng chọn một core
        System.out.print("Select a core by number (or type 'cancel' to cancel): ");
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            System.out.println("Operation cancelled.");
            return;
        }
        int coreIndex = Integer.parseInt(input);
        if (coreIndex < 1 || coreIndex > coreList.size()) {
            System.out.println("Invalid core selection.");
            return;
        }
        String selectedCore = coreList.get(coreIndex - 1);

        // Tạo SolrClient cho core đã chọn
        try (SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL + selectedCore).build()) {

            // Tạo tài liệu Solr mới
            SolrInputDocument newDocument = new SolrInputDocument();

            // Yêu cầu người dùng nhập các trường của tài liệu mới
            System.out.println("Enter the fields for the new document (key=value), one per line. Type 'done' to finish or 'cancel' to cancel:");
            while (true) {
                String inputField = scanner.nextLine();
                if ("done".equalsIgnoreCase(inputField)) {
                    break;
                }
                if ("cancel".equalsIgnoreCase(inputField)) {
                    System.out.println("Operation cancelled.");
                    return;
                }
                String[] parts = inputField.split("=");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String value = parts[1].trim();
                    newDocument.addField(field, value);
                } else {
                    System.out.println("Invalid input. Please enter in 'key=value' format.");
                }
            }

            // Thêm tài liệu mới vào Solr
            UpdateResponse addResponse = solrClient.add(newDocument);
            solrClient.commit();

            // Kiểm tra phản hồi
            if (addResponse.getStatus() == 0) {
                System.out.println("Document has been added successfully.");
            } else {
                System.out.println("Failed to add document. Status: " + addResponse.getStatus());
            }
        }

    } catch (IOException | SolrServerException e) {
        e.printStackTrace();
    }
}
    

    // Phương thức để hiển thị danh sách các tài liệu hiện có trong core
    public void displayDocumentsInCore() {
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
            }

        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để tạo core mới
    public void createCore() {
    Scanner scanner = new Scanner(System.in);
    try {
        // Yêu cầu người dùng nhập tên core mới
        System.out.print("Enter the name of the new core (or type 'cancel' to cancel): ");
        String coreName = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(coreName)) {
            System.out.println("Operation cancelled.");
            return;
        }

        URL url = new URL(SOLR_URL + "admin/cores?action=CREATE&name=" + coreName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }
        br.close();
        conn.disconnect();
        System.out.println("Core created successfully.");
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    // Phương thức để xóa core
    public void deleteCore() {
    Scanner scanner = new Scanner(System.in);
    try {
        // Lấy danh sách các core và hiển thị cho người dùng chọn
        List<String> coreList = getCoreList();
        System.out.println("Available cores:");
        for (int i = 0; i < coreList.size(); i++) {
            System.out.println((i + 1) + ". " + coreList.get(i));
        }

        // Cho phép người dùng chọn một core để xóa
        System.out.print("Select a core by number (or type 'cancel' to cancel): ");
        String input = scanner.nextLine();
        if ("cancel".equalsIgnoreCase(input)) {
            System.out.println("Operation cancelled.");
            return;
        }
        int coreIndex = Integer.parseInt(input);
        if (coreIndex < 1 || coreIndex > coreList.size()) {
            System.out.println("Invalid core selection.");
            return;
        }
        String selectedCore = coreList.get(coreIndex - 1);

        URL url = new URL(SOLR_URL + "admin/cores?action=UNLOAD&core=" + selectedCore + "&deleteIndex=true");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }
        br.close();
        conn.disconnect();
        System.out.println("Core deleted successfully.");
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    // Phương thức chính để quản lý admin
    public void adminOperations() {
    Scanner scanner = new Scanner(System.in);

    while (true) {
        System.out.println("Select an admin operation:");
        System.out.println("1. Create a new core");
        System.out.println("2. Delete an existing core");
        System.out.println("3. Back to main menu");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                createCore();
                break;
            case 2:
                deleteCore();
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}

    // Phương thức để tìm kiếm tài liệu trong Solr
    public void searchDocumentsInSolr() {
        Scanner scanner = new Scanner(System.in);

        try {
            // Lấy danh sách các core và hiển thị cho người dùng chọn
            List<String> coreList = getCoreList();
            System.out.println("Available cores:");
            for (int i = 0; i < coreList.size(); i++) {
                System.out.println((i + 1) + ". " + coreList.get(i));
            }

            // Cho phép người dùng chọn một core
            System.out.print("Select a core by number (or type 'cancel' to cancel): ");
            String input = scanner.nextLine();
            if ("cancel".equalsIgnoreCase(input)) {
                System.out.println("Operation cancelled.");
                return;
            }
            int coreIndex = Integer.parseInt(input);
            if (coreIndex < 1 || coreIndex > coreList.size()) {
                System.out.println("Invalid core selection.");
                return;
            }
            String selectedCore = coreList.get(coreIndex - 1);

            // Tạo SolrClient cho core đã chọn
            try (SolrClient solrClient = new HttpSolrClient.Builder(SOLR_URL + selectedCore).build()) {

                // Yêu cầu người dùng nhập truy vấn tìm kiếm
                System.out.print("Enter your search query (or type 'cancel' to cancel): ");
                String searchQuery = scanner.nextLine();
                if ("cancel".equalsIgnoreCase(searchQuery)) {
                    System.out.println("Operation cancelled.");
                    return;
                }

                // Tạo truy vấn Solr và thiết lập điều kiện tìm kiếm
                SolrQuery query = new SolrQuery();
                query.setQuery(searchQuery);

                // Thực hiện truy vấn tìm kiếm
                QueryResponse queryResponse = solrClient.query(query);
                SolrDocumentList documents = queryResponse.getResults();

                // Hiển thị kết quả tìm kiếm
                System.out.println("Search results:");
                for (SolrDocument document : documents) {
                    System.out.println("ID: " + document.getFieldValue("id") + ", Fields: " + document);
                }
            }

        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
    }
}
