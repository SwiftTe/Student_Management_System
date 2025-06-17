package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    // This method runs after FXML is loaded and controls are injected
    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Student", "Faculty", "Librarian");
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String role = roleComboBox.getValue();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (role == null || username.isEmpty() || password.isEmpty()) {
            showAlert("Login Failed", "Please fill all fields.");
            return;
        }

        try (Connection conn = DBController.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SceneLoader loader = new SceneLoader((Stage) loginButton.getScene().getWindow());

                switch (role) {
                    case "Admin" -> loader.loadScene("admin_dashboard.fxml");
                    case "Student" -> loader.loadScene("student_dashboard.fxml");
                    case "Faculty" -> loader.loadScene("faculty_dashboard.fxml");
                    case "Librarian" -> loader.loadScene("librarian_dashboard.fxml");
                    default -> showAlert("Error", "Unknown role: " + role);
                }

            } else {
                showAlert("Login Failed", "Invalid credentials or role.");
            }

        } catch (Exception e) {
            showAlert("Database Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
