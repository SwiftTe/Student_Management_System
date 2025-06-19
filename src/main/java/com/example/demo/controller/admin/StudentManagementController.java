package com.example.demo.controller.admin;

import com.example.demo.model.Program;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.service.ProgramService;
import com.example.demo.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentManagementController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<Program> programComboBox;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker enrollmentDatePicker;
    @FXML private TextField majorField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> studentIdCol;
    @FXML private TableColumn<Student, String> firstNameCol;
    @FXML private TableColumn<Student, String> lastNameCol;
    @FXML private TableColumn<Student, String> programNameCol;
    @FXML private TableColumn<Student, LocalDate> dobCol;
    @FXML private TableColumn<Student, String> genderCol;
    @FXML private TableColumn<Student, String> emailCol;
    @FXML private TableColumn<Student, String> phoneCol;
    @FXML private TableColumn<Student, String> addressCol;
    @FXML private TableColumn<Student, LocalDate> enrollmentDateCol;
    @FXML private TableColumn<Student, String> majorCol;

    private final StudentService studentService;
    private final ProgramService programService;
    private final ObservableList<Student> studentList;
    private final ObservableList<Program> programOptions;

    public StudentManagementController() {
        this.studentService = new StudentService();
        this.programService = new ProgramService();
        this.studentList = FXCollections.observableArrayList();
        this.programOptions = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("Male", "Female", "Other");

        loadProgramsIntoComboBox();
        programComboBox.setItems(programOptions);
        programComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Program program) {
                return program != null ? program.getProgramName() : "";
            }

            @Override
            public Program fromString(String string) {
                return programOptions.stream()
                        .filter(p -> p.getProgramName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        programNameCol.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgramId();
            try {
                Program program = programService.getProgramById(programId);
                return new SimpleStringProperty(program != null ? program.getProgramName() : "N/A");
            } catch (SQLException e) {
                System.err.println("Error fetching program name: " + e.getMessage());
                return new SimpleStringProperty("Error");
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

        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showStudentDetails(newValue));

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void loadProgramsIntoComboBox() {
        try {
            List<Program> programs = programService.getAllPrograms();
            programOptions.setAll(programs);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load programs.");
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.setAll(students);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students.");
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
            usernameField.setText("");
            passwordField.setText("");

            try {
                Program program = programService.getProgramById(student.getProgramId());
                programComboBox.getSelectionModel().select(program);
            } catch (SQLException e) {
                programComboBox.getSelectionModel().clearSelection();
            }

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
            Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
            if (selectedProgram == null) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                return;
            }

            Student newStudent = studentService.addNewStudent(
                    firstNameField.getText(), lastNameField.getText(), dobPicker.getValue(),
                    genderComboBox.getValue(), emailField.getText(), phoneField.getText(),
                    addressField.getText(), enrollmentDatePicker.getValue(), majorField.getText(),
                    selectedProgram.getProgramId(), usernameField.getText(), passwordField.getText()
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Student added: " + newStudent.getFirstName());
            loadStudents();
            handleClearSelection(null);
        } catch (SQLException | IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            try {
                Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
                if (selectedProgram == null) {
                    showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                    return;
                }

                selectedStudent.setFirstName(firstNameField.getText());
                selectedStudent.setLastName(lastNameField.getText());
                selectedStudent.setDateOfBirth(dobPicker.getValue());
                selectedStudent.setGender(genderComboBox.getValue());
                selectedStudent.setEmail(emailField.getText());
                selectedStudent.setPhoneNumber(phoneField.getText());
                selectedStudent.setAddress(addressField.getText());
                selectedStudent.setEnrollmentDate(enrollmentDatePicker.getValue());
                selectedStudent.setMajor(majorField.getText());

                studentService.updateStudent(selectedStudent, selectedProgram.getProgramId());

                String newUsername = usernameField.getText();
                String newPassword = passwordField.getText();

                if (!newUsername.isEmpty() || !newPassword.isEmpty()) {
                    User userToUpdate = studentService.getUserService().getUserById(selectedStudent.getUserId());
                    if (userToUpdate != null) {
                        if (!newUsername.isEmpty()) userToUpdate.setUsername(newUsername);
                        if (!newPassword.isEmpty()) userToUpdate.setPasswordHash(newPassword);
                        studentService.getUserService().updateUser(userToUpdate, newPassword);
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Updated", "Student updated successfully.");
                loadStudents();
                handleClearSelection(null);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Update Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Delete Student?");
            confirm.setContentText("Are you sure?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    studentService.deleteStudent(selected.getStudentId());
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Student deleted.");
                    loadStudents();
                    handleClearSelection(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Delete Failed", e.getMessage());
                }
            }
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
        programComboBox.getSelectionModel().clearSelection();
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
