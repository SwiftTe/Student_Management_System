package com.example.demo.controller.admin;

import com.example.demo.model.Librarian;
import com.example.demo.model.User; // Potentially needed if updating user directly, but LibrarianService handles it
import com.example.demo.service.LibrarianService;
import com.example.demo.service.UserService; // To retrieve associated user details if needed
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LibrarianManagementController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField usernameField; // For login username
    @FXML
    private PasswordField passwordField; // For login password

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;

    @FXML
    private TableView<Librarian> librarianTable;
    @FXML
    private TableColumn<Librarian, Integer> librarianIdCol;
    @FXML
    private TableColumn<Librarian, String> firstNameCol;
    @FXML
    private TableColumn<Librarian, String> lastNameCol;
    @FXML
    private TableColumn<Librarian, String> emailCol;
    @FXML
    private TableColumn<Librarian, String> phoneCol;

    private LibrarianService librarianService;
    private UserService userService; // To help manage user accounts directly if needed (e.g., getting username)
    private ObservableList<Librarian> librarianList;

    public LibrarianManagementController() {
        this.librarianService = new LibrarianService();
        this.userService = new UserService();
        this.librarianList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configure TableView columns
        librarianIdCol.setCellValueFactory(new PropertyValueFactory<>("librarianId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        librarianTable.setItems(librarianList);
        loadLibrarians();

        // Add listener to table selection
        librarianTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showLibrarianDetails(newValue));

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Loads all librarians from the database and updates the TableView.
     */
    private void loadLibrarians() {
        try {
            List<Librarian> librarians = librarianService.getAllLibrarians();
            librarianList.setAll(librarians);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load librarians: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the details of the selected librarian in the input fields.
     * @param librarian The selected Librarian object, or null if selection is cleared.
     */
    private void showLibrarianDetails(Librarian librarian) {
        if (librarian != null) {
            firstNameField.setText(librarian.getFirstName());
            lastNameField.setText(librarian.getLastName());
            emailField.setText(librarian.getEmail());
            phoneField.setText(librarian.getPhoneNumber());

            // For username field: Fetch the associated user's username
            try {
                User associatedUser = userService.getUserById(librarian.getUserId());
                if (associatedUser != null) {
                    usernameField.setText(associatedUser.getUsername());
                } else {
                    usernameField.setText(""); // User not found
                }
            } catch (SQLException e) {
                System.err.println("Error fetching associated user for librarian ID " + librarian.getLibrarianId() + ": " + e.getMessage());
                usernameField.setText("Error");
            }
            passwordField.setText(""); // Never pre-fill password field for security

            addButton.setDisable(true);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            handleClearSelection(null);
        }
    }

    @FXML
    private void handleAddLibrarian(ActionEvent event) {
        try {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            Librarian newLibrarian = librarianService.addNewLibrarian(
                    firstName, lastName, email, phone, username, password
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Librarian '" + newLibrarian.getFirstName() + " " + newLibrarian.getLastName() + "' added successfully with ID: " + newLibrarian.getLibrarianId());
            loadLibrarians();
            handleClearSelection(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add librarian: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateLibrarian(ActionEvent event) {
        Librarian selectedLibrarian = librarianTable.getSelectionModel().getSelectedItem();
        if (selectedLibrarian != null) {
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String newUsername = usernameField.getText(); // Potentially new username
                String newPassword = passwordField.getText(); // Potentially new password

                // Update the selected librarian object
                selectedLibrarian.setFirstName(firstName);
                selectedLibrarian.setLastName(lastName);
                selectedLibrarian.setEmail(email);
                selectedLibrarian.setPhoneNumber(phone);

                librarianService.updateLibrarian(selectedLibrarian);

                // If username or password fields are not empty, update user login details
                if (!newUsername.isEmpty() || !newPassword.isEmpty()) {
                    User userToUpdate = userService.getUserById(selectedLibrarian.getUserId());
                    if (userToUpdate != null) {
                        if (!newUsername.isEmpty()) {
                            userToUpdate.setUsername(newUsername);
                        }
                        if (!newPassword.isEmpty()) {
                            userToUpdate.setPasswordHash(newPassword); // In real app, hash newPassword
                        }
                        userService.updateUser(userToUpdate, newPassword); // Pass newPassword for service to handle hashing
                    } else {
                        showAlert(Alert.AlertType.WARNING, "User Not Found", "Associated user account not found for update.");
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Librarian updated successfully.");
                loadLibrarians();
                handleClearSelection(null);
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update librarian: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a librarian to update.");
        }
    }

    @FXML
    private void handleDeleteLibrarian(ActionEvent event) {
        Librarian selectedLibrarian = librarianTable.getSelectionModel().getSelectedItem();
        if (selectedLibrarian != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Librarian: " + selectedLibrarian.getFirstName() + " " + selectedLibrarian.getLastName() + "?");
            alert.setContentText("Are you sure you want to delete this librarian and their associated user account? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    librarianService.deleteLibrarian(selectedLibrarian.getLibrarianId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Librarian deleted successfully.");
                    loadLibrarians();
                    handleClearSelection(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete librarian. It might be linked to other records or a system error occurred: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Error", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a librarian to delete.");
        }
    }

    @FXML
    private void handleClearSelection(ActionEvent event) {
        librarianTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        usernameField.clear();
        passwordField.clear();

        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Helper method to display an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header for simplicity
        alert.setContentText(message);
        alert.showAndWait();
    }
}
