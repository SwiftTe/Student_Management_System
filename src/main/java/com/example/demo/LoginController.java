package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException; // Import SQLException

public class LoginController {

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private CheckBox rememberMeCheckBox;

    private UserService userService; // Instance of the UserService

    public LoginController() {
        this.userService = new UserService(); // Initialize the UserService
    }

    // This method runs after FXML is loaded and controls are injected
    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Student", "Faculty", "Librarian");
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String role = roleComboBox.getValue();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim(); // Note: In a real app, hash this password before use

        if (role == null || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Please fill all fields.");
            return;
        }

        try {
            // Authenticate user using the UserService
            User authenticatedUser = userService.authenticateUser(username, password, role);

            if (authenticatedUser != null) {
                // Login successful, load the base_layout and pass the role
                FXMLLoader loader = new FXMLLoader(getClass().getResource("base_layout.fxml"));
                Parent root = loader.load();
                DashboardController dashboardController = loader.getController();
                dashboardController.setUserRole(authenticatedUser.getRole()); // Pass the actual role from DB

                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.setMaximized(true); // Fullscreen maximized window
                currentStage.setTitle("SMS - " + authenticatedUser.getRole() + " Dashboard");
                currentStage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials or role.");
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Application Error", "Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "An Error Occurred", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
