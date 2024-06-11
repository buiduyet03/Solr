package org.example;

import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
    ManageSolrDocuments manager = new ManageSolrDocuments();
    Scanner scanner = new Scanner(System.in);

    while (true) {
        System.out.println("Select an operation:");
        System.out.println("1. Delete a document");
        System.out.println("2. Update a document");
        System.out.println("3. Add a new document");
        System.out.println("4. Display documents in a core");
        System.out.println("5. Search documents in solr");        
        System.out.println("6. Admin operations");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                manager.deleteFromSolr();
                break;
            case 2:
                manager.updateDocumentInSolr();
                break;
            case 3:
                manager.addDocumentToSolr();
                break;
            case 4:
                manager.displayDocumentsInCore();
                break;
            case 5:
                manager.searchDocumentsInSolr();
                break;
            case 6:
                manager.adminOperations();
                break;
            case 7:
                System.out.println("Exiting...");
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
