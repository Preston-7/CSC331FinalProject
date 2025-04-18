package com.library.management.librarymanagementsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class editController implements Initializable {

    @FXML
    private TextField editAuthorTextField;

    @FXML
    private TextField editTitleTextField;

    @FXML
    private TextField editISBNTextField;

    @FXML
    private TextField editCategoryTextField;

    private static final String CSV_FILE_PATH = "src/main/resources/inventory.csv";
    private String originalISBN;  // To keep track of the original ISBN for updates

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // The fields will be populated when the scene is loaded
        // We'll capture the original ISBN to identify which book to update
        originalISBN = editISBNTextField.getText();
    }

    @FXML
    public void submitEdit(ActionEvent event) {
        try {
            String author = editAuthorTextField.getText().trim();
            String title = editTitleTextField.getText().trim();
            String isbn = editISBNTextField.getText().trim();
            String genre = editCategoryTextField.getText().trim();

            // Validate inputs
            if (author.isEmpty() || title.isEmpty() || isbn.isEmpty() || genre.isEmpty()) {
                showAlert(AlertType.WARNING, "Incomplete Entry", "Please fill in all fields.");
                return;
            }

            // Create updated book
            Book updatedBook = new Book(author, title, isbn, genre);

            // Read all books from CSV
            List<String> allBooks = readAllBooksFromCsv();
            List<String> updatedBooks = new ArrayList<>();

            // Find and update the specific book
            boolean bookFound = false;
            boolean isbnExists = false;

            for (String bookLine : allBooks) {
                Book currentBook = Book.fromCsvString(bookLine);

                if (currentBook == null) {
                    // Keep invalid lines as is
                    updatedBooks.add(bookLine);
                    continue;
                }

                // Check if this is not our book but has the same ISBN
                if (!currentBook.getBook_id().equals(originalISBN) && currentBook.getBook_id().equals(isbn)) {
                    isbnExists = true;
                    break;  // ISBN already exists for another book
                }

                // If this is the book we're updating
                if (currentBook.getBook_id().equals(originalISBN)) {
                    updatedBooks.add(updatedBook.toCsvString());
                    bookFound = true;
                } else {
                    // Keep other books as is
                    updatedBooks.add(bookLine);
                }
            }

            // Handle errors
            if (isbnExists) {
                showAlert(AlertType.ERROR, "Duplicate ISBN",
                        "A different book with this ISBN already exists.");
                return;
            }

            if (!bookFound) {
                showAlert(AlertType.ERROR, "Book Not Found",
                        "The book you're trying to edit could not be found.");
                return;
            }

            // Write updated list back to CSV
            writeBooksToCsv(updatedBooks);

            // Navigate back to main view
            navigateToMainView();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    public void cancelEdit(ActionEvent event) {
        try {
            navigateToMainView();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                    "Could not navigate back to main view: " + e.getMessage());
        }
    }

    private void navigateToMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(scene);
        stage.setTitle("Library Management System");
        stage.show();
    }

    // This method should be called from HelloViewController when switching scenes
    public void setBookData(Book book) {
        if (book != null) {
            editAuthorTextField.setText(book.getAuthor());
            editTitleTextField.setText(book.getTitle());
            editISBNTextField.setText(book.getBook_id());
            editCategoryTextField.setText(book.getGenre());
            originalISBN = book.getBook_id();  // Store original ISBN for update
        }
    }

    private List<String> readAllBooksFromCsv() throws IOException {
        try {
            return Files.readAllLines(Paths.get(CSV_FILE_PATH));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void writeBooksToCsv(List<String> books) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            for (String book : books) {
                writer.write(book);
                writer.newLine();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}