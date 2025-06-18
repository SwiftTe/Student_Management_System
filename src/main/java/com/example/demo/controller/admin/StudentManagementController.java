package com.example.demo.controller.admin;

import com.example.demo.model.Program;
import com.example.demo.model.Student;
import com.example.demo.service.ProgramService;
import com.example.demo.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentManagementController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private ComboBox<Program> programComboBox; // Stores Program objects
    @FXML
    private DatePicker dobPicker;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private DatePicker enrollmentDatePicker;
    @FXML
    private TextField majorField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;

    @FXML
    private TableView<Student> studentTable;
    @FXML
    private TableColumn<Student, Integer> studentIdCol;
    @FXML
    private TableColumn<Student, String> firstNameCol;
    @FXML
    private TableColumn<Student, String> lastNameCol;
    @FXML
    private TableColumn<Student, String> programNameCol; // This will display program name from Program object
    @FXML
    private TableColumn<Student, LocalDate> dobCol;
    @FXML
    private TableColumn<Student, String> genderCol;
    @FXML
    private TableColumn<Student, String> emailCol;
    @FXML
    private TableColumn<Student, String> phoneCol;
    @FXML
    private TableColumn<Student, String> addressCol;
    @FXML
    private TableColumn<Student, LocalDate> enrollmentDateCol;
    @FXML
    private TableColumn<Student, String> majorCol;

    private StudentService studentService;
    private ProgramService programService;
    private ObservableList<Student> studentList;
    private ObservableList<Program> programOptions;

    public StudentManagementController() {
        this.studentService = new StudentService();
        this.programService = new ProgramService();
        this.studentList = FXCollections.observableArrayList();
        this.programOptions = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Initialize ComboBoxes
        genderComboBox.getItems().addAll("Male", "Female", "Other");

        // Load programs into ComboBox
        loadProgramsIntoComboBox();
        programComboBox.setItems(programOptions);
        // Set a string converter if Program objects are complex and you want to display just the name
        programComboBox.setConverter(new javafx.util.StringConverter<Program>() {
            @Override
            public String toString(Program program) {
                return program != null ? program.getProgramName() : "";
            }

            @Override
            public Program fromString(String string) {
                // Not used for selection in this context, but required for the interface
                return programOptions.stream()
                        .filter(p -> p.getProgramName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        // Configure TableView columns
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        // Custom cell value factory for programNameCol to fetch program name from ID
        programNameCol.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgramId();
            try {
                Program program = programService.getProgramById(programId);
                return FXCollections.observableArrayList(program != null ? program.getProgramName() : "N/A").get(0).asString();
            } catch (SQLException e) {
                System.err.println("Error fetching program name for student ID " + cellData.getValue().getStudentId() + ": " + e.getMessage());
                return FXCollections.observableArrayList("Error").get(0).asString();
            }
        });
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        enrollmentDateCol.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        majorCol.setCellValueFactory(new PropertyValueFactory<>("major"));

        studentTable.setItems(studentList);
        loadStudents();

        // Listener for table selection
        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showStudentDetails(newValue));

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void loadProgramsIntoComboBox() {
        try {
            List<Program> programs = programService.getAllPrograms();
            programOptions.setAll(programs);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load programs for dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.setAll(students);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showStudentDetails(Student student) {
        if (student != null) {
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            dobPicker.setValue(student.getDateOfBirth());
            genderComboBox.setValue(student.getGender());
            emailField.setText(student.getEmail());
            phoneField.setText(student.getPhoneNumber());
            addressField.setText(student.getAddress());
            enrollmentDatePicker.setValue(student.getEnrollmentDate());
            majorField.setText(student.getMajor());

            // Select the correct program in the ComboBox
            try {
                Program program = programService.getProgramById(student.getProgramId());
                programComboBox.getSelectionModel().select(program);
            } catch (SQLException e) {
                System.err.println("Error selecting program for student: " + e.getMessage());
                programComboBox.getSelectionModel().clearSelection(); // Clear selection if program not found
            }

            // Username and password fields should not be pre-filled for security.
            // When updating, password should be re-entered or left blank to keep old.
            usernameField.setText(""); // Clear username field for security/re-entry
            passwordField.setText(""); // Clear password field for security/re-entry

            addButton.setDisable(true);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            handleClearSelection(null);
        }
    }

    @FXML
    private void handleAddStudent(ActionEvent event) {
        try {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            LocalDate dob = dobPicker.getValue();
            String gender = genderComboBox.getValue();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            LocalDate enrollmentDate = enrollmentDatePicker.getValue();
            String major = majorField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
            if (selectedProgram == null) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                return;
            }
            int programId = selectedProgram.getProgramId();

            Student newStudent = studentService.addNewStudent(
                    firstName, lastName, dob, gender, email, phone, address,
                    enrollmentDate, major, programId, username, password
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student '" + newStudent.getFirstName() + " " + newStudent.getLastName() + "' added successfully with ID: " + newStudent.getStudentId());
            loadStudents();
            handleClearSelection(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add student: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                LocalDate dob = dobPicker.getValue();
                String gender = genderComboBox.getValue();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String address = addressField.getText();
                LocalDate enrollmentDate = enrollmentDatePicker.getValue();
                String major = majorField.getText();
                String newUsername = usernameField.getText(); // Potentially new username
                String newPassword = passwordField.getText(); // Potentially new password

                Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
                if (selectedProgram == null) {
                    showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                    return;
                }
                int newProgramId = selectedProgram.getProgramId();

                // Update the selected student object
                selectedStudent.setFirstName(firstName);
                selectedStudent.setLastName(lastName);
                selectedStudent.setDateOfBirth(dob);
                selectedStudent.setGender(gender);
                selectedStudent.setEmail(email);
                selectedStudent.setPhoneNumber(phone);
                selectedStudent.setAddress(address);
                selectedStudent.setEnrollmentDate(enrollmentDate);
                selectedStudent.setMajor(major);

                // Update student details including potentially new program ID
                studentService.updateStudent(selectedStudent, newProgramId);

                // If username or password fields are not empty, update user login details
                if (!newUsername.isEmpty() || !newPassword.isEmpty()) {
                    // Fetch the user associated with this student
                    User userToUpdate = studentService.userService.getUserById(selectedStudent.getUserId());
                    if (userToUpdate != null) {
                        if (!newUsername.isEmpty()) {
                            userToUpdate.setUsername(newUsername);
                        }
                        if (!newPassword.isEmpty()) {
                            // In a real app, hash newPassword here
                            userToUpdate.setPasswordHash(newPassword); // For simplicity, direct set
                        }
                        studentService.userService.updateUser(userToUpdate, newPassword); // Pass newPassword for hashing if not handled in service
                    } else {
                        showAlert(Alert.AlertType.WARNING, "User Not Found", "Associated user account not found for update.");
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully.");
                loadStudents();
                handleClearSelection(null);
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update student: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to update.");
        }
    }

    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Student: " + selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "?");
            alert.setContentText("Are you sure you want to delete this student and their associated user account? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    studentService.deleteStudent(selectedStudent.getStudentId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully.");
                    loadStudents();
                    handleClearSelection(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete student. It might be linked to other records or a system error occurred: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Error", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete.");
        }
    }

    @FXML
    private void handleClearSelection(ActionEvent event) {
        studentTable.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        genderComboBox.getSelectionModel().clearSelection();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        enrollmentDatePicker.setValue(null);
        majorField.clear();
        usernameField.clear();
        passwordField.clear();
        programComboBox.getSelectionModel().clearSelection(); // Clear program combo box selection

        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
