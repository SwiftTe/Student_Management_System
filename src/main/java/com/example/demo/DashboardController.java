package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node; // Used for dynamic content loading
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    @FXML
    private BorderPane rootPane; // The root of base_layout.fxml
    @FXML
    private VBox sidebar; // The VBox for the left sidebar
    @FXML
    private VBox mainContent; // The VBox for the dynamic central content

    private String userRole; // To store the role of the logged-in user

    // A map to store FXML paths for each module, per role
    private final Map<String, Map<String, String>> roleModuleFxmlMap = new HashMap<>();

    public void initialize() {
        // Initialize the map with module FXML paths
        // This will be expanded significantly as we add more modules
        // For now, let's set up Admin's dashboard modules
        Map<String, String> adminModules = new HashMap<>();
        adminModules.put("Dashboard", "dashboard_overview.fxml"); // A generic overview, we'll create this
        adminModules.put("Manage Programs", "admin/manage_programs.fxml");
        adminModules.put("Manage Students", "admin/manage_students.fxml");
        adminModules.put("Manage Faculty", "admin/manage_faculty.fxml");
        adminModules.put("Manage Librarians", "admin/manage_librarians.fxml");
        adminModules.put("Manage Courses", "admin/manage_courses.fxml");
        adminModules.put("Manage Routines", "admin/manage_routines.fxml");
        adminModules.put("Manage Announcements", "admin/manage_announcements.fxml");
        adminModules.put("Reports", "admin/reports.fxml");
        roleModuleFxmlMap.put("Admin", adminModules);

        Map<String, String> studentModules = new HashMap<>();
        studentModules.put("Dashboard", "student/student_dashboard_overview.fxml");
        studentModules.put("My Profile", "student/my_profile.fxml");
        studentModules.put("My Programs & Courses", "student/my_programs_courses.fxml");
        studentModules.put("My Enrollments", "student/my_enrollments.fxml");
        studentModules.put("My Assignments", "student/my_assignments.fxml");
        studentModules.put("My Submissions", "student/my_submissions.fxml");
        studentModules.put("My Attendance", "student/my_attendance.fxml");
        studentModules.put("My Fees", "student/my_fees.fxml");
        studentModules.put("My Results", "student/my_results.fxml");
        studentModules.put("My Routines", "student/my_routines.fxml");
        studentModules.put("Announcements", "student/announcements.fxml");
        studentModules.put("Library", "student/library.fxml");
        roleModuleFxmlMap.put("Student", studentModules);

        Map<String, String> facultyModules = new HashMap<>();
        facultyModules.put("Dashboard", "faculty/faculty_dashboard_overview.fxml");
        facultyModules.put("My Profile", "faculty/my_profile.fxml");
        facultyModules.put("My Courses", "faculty/my_courses.fxml");
        facultyModules.put("Manage Assignments", "faculty/manage_assignments.fxml");
        facultyModules.put("Evaluate Submissions", "faculty/evaluate_submissions.fxml");
        facultyModules.put("Mark Attendance", "faculty/mark_attendance.fxml");
        facultyModules.put("Manage Results", "faculty/manage_results.fxml");
        facultyModules.put("My Routines", "faculty/my_routines.fxml");
        facultyModules.put("Announcements", "faculty/announcements.fxml");
        roleModuleFxmlMap.put("Faculty", facultyModules);

        Map<String, String> librarianModules = new HashMap<>();
        librarianModules.put("Dashboard", "librarian/librarian_dashboard_overview.fxml");
        librarianModules.put("My Profile", "librarian/my_profile.fxml");
        librarianModules.put("Manage Books", "librarian/manage_books.fxml");
        librarianModules.put("Manage Borrowings", "librarian/manage_borrowings.fxml");
        librarianModules.put("Announcements", "librarian/announcements.fxml");
        roleModuleFxmlMap.put("Librarian", librarianModules);
    }

    /**
     * Sets the user role and configures the dashboard accordingly (e.g., populating sidebar).
     * This method will be called from LoginController after successful login.
     * @param role The role of the logged-in user.
     */
    public void setUserRole(String role) {
        this.userRole = role;
        populateSidebar(role); // Populate sidebar buttons based on role
        // Optionally, load the default module for the role (e.g., Dashboard Overview)
        loadModule("Dashboard");
    }

    /**
     * Populates the sidebar with buttons relevant to the user's role.
     * @param role The role of the current user.
     */
    private void populateSidebar(String role) {
        sidebar.getChildren().clear(); // Clear existing buttons (from FXML initially)
        sidebar.setSpacing(10); // Set spacing for VBox children

        // Title Label
        Label titleLabel = new Label("SMS System - " + role);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        sidebar.getChildren().add(titleLabel);

        // Add padding between title and buttons
        VBox.setMargin(titleLabel, new javafx.geometry.Insets(0, 0, 10, 0));


        // Get modules for the specific role
        Map<String, String> modules = roleModuleFxmlMap.get(role);
        if (modules != null) {
            for (String moduleName : modules.keySet()) {
                Button moduleButton = new Button(moduleName);
                moduleButton.setMaxWidth(Double.MAX_VALUE); // Make button fill width
                moduleButton.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"); // Basic button style
                // Add hover effect
                moduleButton.setOnMouseEntered(e -> moduleButton.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"));
                moduleButton.setOnMouseExited(e -> moduleButton.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"));

                // Set action to load the corresponding module FXML
                moduleButton.setOnAction(event -> loadModule(moduleName));
                sidebar.getChildren().add(moduleButton);
            }
        }

        // Add Logout button at the bottom (consider a separate section or styling)
        Button logoutButton = new Button("Logout");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"); // Red for logout
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15 10 15;"));
        logoutButton.setOnAction(event -> handleLogout());
        VBox.setMargin(logoutButton, new javafx.geometry.Insets(20, 0, 0, 0)); // Top margin
        sidebar.getChildren().add(logoutButton);
    }

    /**
     * Loads the FXML content for a given module into the main content area.
     * @param moduleName The name of the module (e.g., "Add Student").
     */
    private void loadModule(String moduleName) {
        Map<String, String> modules = roleModuleFxmlMap.get(userRole);
        if (modules == null || !modules.containsKey(moduleName)) {
            System.err.println("Error: Module '" + moduleName + "' not found for role '" + userRole + "'");
            // Show an alert to the user
            showAlert(Alert.AlertType.ERROR, "Module Error", "Could not load the requested module.");
            return;
        }

        String fxmlPath = modules.get(moduleName);
        try {
            // Adjust path to be relative to the resource root, assuming FXMLs are in com/example/demo/
            // and then potentially subdirectories like 'admin/', 'student/', etc.
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node moduleContent = loader.load();
            mainContent.getChildren().clear(); // Clear previous content
            mainContent.getChildren().add(moduleContent); // Add new content
        } catch (IOException e) {
            System.err.println("Failed to load module FXML: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load '" + moduleName + "'. Please try again or contact support.");
        }
    }

    private void handleLogout() {
        // Implement logout logic here
        // For now, redirect to login screen
        Stage currentStage = (Stage) rootPane.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Student Management System - Login");
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Error redirecting to login: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Failed to return to login screen.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
