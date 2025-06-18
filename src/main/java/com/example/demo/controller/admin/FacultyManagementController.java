package com.example.demo.controller.admin;

import com.example.demo.model.Faculty;
import com.example.demo.model.User; // Potentially needed if updating user directly, but FacultyService handles it
import com.example.demo.service.FacultyService;
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

public class FacultyManagementController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField departmentField;
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
    private TableView<Faculty> facultyTable;
    @FXML
    private TableColumn<Faculty, Integer> facultyIdCol;
    @FXML
    private TableColumn<Faculty, String> firstNameCol;
    @FXML
    private TableColumn<Faculty, String> lastNameCol;
    @FXML
    private TableColumn<Faculty, String> emailCol;
    @FXML
    private TableColumn<Faculty, String> phoneCol;
    @FXML
    private TableColumn<Faculty, String> departmentCol;

    private FacultyService facultyService;
    private UserService userService; // To help manage user accounts directly if needed (e.g., getting username)
    private ObservableList<Faculty> facultyList;

    public FacultyManagementController() {
        this.facultyService = new FacultyService();
        this.userService = new UserService();
        this.facultyList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configure TableView columns
        facultyIdCol.setCellValueFactory(new PropertyValueFactory<>("facultyId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        facultyTable.setItems(facultyList);
        loadFaculty();

        // Add listener to table selection
        facultyTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showFacultyDetails(newValue));

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Loads all faculty members from the database and updates the TableView.
     */
    private void loadFaculty() {
        try {
            List<Faculty> facultyMembers = facultyService.getAllFaculty();
            facultyList.setAll(facultyMembers);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load faculty members: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the details of the selected faculty member in the input fields.
     * @param faculty The selected Faculty object, or null if selection is cleared.
     */
    private void showFacultyDetails(Faculty faculty) {
        if (faculty != null) {
            firstNameField.setText(faculty.getFirstName());
            lastNameField.setText(faculty.getLastName());
            emailField.setText(faculty.getEmail());
            phoneField.setText(faculty.getPhoneNumber());
            departmentField.setText(faculty.getDepartment());

            // For username field: Fetch the associated user's username
            try {
                User associatedUser = userService.getUserById(faculty.getUserId());
                if (associatedUser != null) {
                    usernameField.setText(associatedUser.getUsername());
                } else {
                    usernameField.setText(""); // User not found
                }
            } catch (SQLException e) {
                System.err.println("Error fetching associated user for faculty ID " + faculty.getFacultyId() + ": " + e.getMessage());
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
    private void handleAddFaculty(ActionEvent event) {
        try {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String department = departmentField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            Faculty newFaculty = facultyService.addNewFaculty(
                    firstName, lastName, email, phone, department, username, password
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Faculty '" + newFaculty.getFirstName() + " " + newFaculty.getLastName() + "' added successfully with ID: " + newFaculty.getFacultyId());
            loadFaculty();
            handleClearSelection(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add faculty: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateFaculty(ActionEvent event) {
        Faculty selectedFaculty = facultyTable.getSelectionModel().getSelectedItem();
        if (selectedFaculty != null) {
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String department = departmentField.getText();
                String newUsername = usernameField.getText(); // Potentially new username
                String newPassword = passwordField.getText(); // Potentially new password

                // Update the selected faculty object
                selectedFaculty.setFirstName(firstName);
                selectedFaculty.setLastName(lastName);
                selectedFaculty.setEmail(email);
                selectedFaculty.setPhoneNumber(phone);
                selectedFaculty.setDepartment(department);

                facultyService.updateFaculty(selectedFaculty);

                // If username or password fields are not empty, update user login details
                if (!newUsername.isEmpty() || !newPassword.isEmpty()) {
                    User userToUpdate = userService.getUserById(selectedFaculty.getUserId());
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

                showAlert(Alert.AlertType.INFORMATION, "Success", "Faculty updated successfully.");
                loadFaculty();
                handleClearSelection(null);
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update faculty: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a faculty member to update.");
        }
    }

    @FXML
    private void handleDeleteFaculty(ActionEvent event) {
        Faculty selectedFaculty = facultyTable.getSelectionModel().getSelectedItem();
        if (selectedFaculty != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Faculty: " + selectedFaculty.getFirstName() + " " + selectedFaculty.getLastName() + "?");
            alert.setContentText("Are you sure you want to delete this faculty member and their associated user account? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    facultyService.deleteFaculty(selectedFaculty.getFacultyId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Faculty deleted successfully.");
                    loadFaculty();
                    handleClearSelection(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete faculty. It might be linked to other records or a system error occurred: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Error", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a faculty member to delete.");
        }
    }

    @FXML
    private void handleClearSelection(ActionEvent event) {
        facultyTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        departmentField.clear();
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
