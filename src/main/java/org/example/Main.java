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
            System.out.println("3. Exit");
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
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}