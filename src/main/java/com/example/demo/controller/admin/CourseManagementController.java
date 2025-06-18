package com.example.demo.controller.admin;

import com.example.demo.model.Course;
import com.example.demo.model.Program;
import com.example.demo.service.CourseService;
import com.example.demo.service.ProgramService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CourseManagementController {

    @FXML
    private TextField courseCodeField;
    @FXML
    private TextField courseNameField;
    @FXML
    private ComboBox<Program> programComboBox;
    @FXML
    private Spinner<Integer> semesterSpinner;
    @FXML
    private Spinner<Integer> creditsSpinner;
    @FXML
    private TextField departmentField;
    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;

    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, Integer> courseIdCol;
    @FXML
    private TableColumn<Course, String> programNameCol; // To display program name
    @FXML
    private TableColumn<Course, Integer> semesterCol;
    @FXML
    private TableColumn<Course, String> courseCodeCol;
    @FXML
    private TableColumn<Course, String> courseNameCol;
    @FXML
    private TableColumn<Course, Integer> creditsCol;
    @FXML
    private TableColumn<Course, String> descriptionCol;
    @FXML
    private TableColumn<Course, String> departmentCol;

    private CourseService courseService;
    private ProgramService programService;
    private ObservableList<Course> courseList;
    private ObservableList<Program> programOptions;

    public CourseManagementController() {
        this.courseService = new CourseService();
        this.programService = new ProgramService();
        this.courseList = FXCollections.observableArrayList();
        this.programOptions = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Initialize Spinners
        semesterSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1));
        creditsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 6, 3));

        // Load programs into ComboBox
        loadProgramsIntoComboBox();
        programComboBox.setItems(programOptions);
        // Set a string converter for Program objects in ComboBox
        programComboBox.setConverter(new javafx.util.StringConverter<Program>() {
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

        // Configure TableView columns
        courseIdCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        // Custom cell value factory for programNameCol to fetch program name from ID
        programNameCol.setCellValueFactory(cellData -> {
            int programId = cellData.getValue().getProgramId();
            try {
                Program program = programService.getProgramById(programId);
                return FXCollections.observableArrayList(program != null ? program.getProgramName() : "N/A").get(0).asString();
            } catch (SQLException e) {
                System.err.println("Error fetching program name for course ID " + cellData.getValue().getCourseId() + ": " + e.getMessage());
                return FXCollections.observableArrayList("Error").get(0).asString();
            }
        });
        semesterCol.setCellValueFactory(new PropertyValueFactory<>("semesterNumber"));
        courseCodeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        courseTable.setItems(courseList);
        loadCourses();

        // Add listener to table selection
        courseTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCourseDetails(newValue));

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

    private void loadCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            courseList.setAll(courses);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCourseDetails(Course course) {
        if (course != null) {
            courseCodeField.setText(course.getCourseCode());
            courseNameField.setText(course.getCourseName());
            semesterSpinner.getValueFactory().setValue(course.getSemesterNumber());
            creditsSpinner.getValueFactory().setValue(course.getCredits());
            departmentField.setText(course.getDepartment());
            descriptionArea.setText(course.getDescription());

            // Select the correct program in the ComboBox
            try {
                Program program = programService.getProgramById(course.getProgramId());
                programComboBox.getSelectionModel().select(program);
            } catch (SQLException e) {
                System.err.println("Error selecting program for course: " + e.getMessage());
                programComboBox.getSelectionModel().clearSelection(); // Clear selection if program not found
            }

            addButton.setDisable(true);
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            handleClearSelection(null);
        }
    }

    @FXML
    private void handleAddCourse(ActionEvent event) {
        try {
            String courseCode = courseCodeField.getText();
            String courseName = courseNameField.getText();
            int semester = semesterSpinner.getValue();
            int credits = creditsSpinner.getValue();
            String department = departmentField.getText();
            String description = descriptionArea.getText();

            Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
            if (selectedProgram == null) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                return;
            }
            int programId = selectedProgram.getProgramId();

            Course newCourse = courseService.addNewCourse(
                    programId, semester, courseCode, courseName, credits, description, department
            );
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course '" + newCourse.getCourseName() + "' (" + newCourse.getCourseCode() + ") added successfully with ID: " + newCourse.getCourseId());
            loadCourses();
            handleClearSelection(null);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add course: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateCourse(ActionEvent event) {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            try {
                String courseCode = courseCodeField.getText();
                String courseName = courseNameField.getText();
                int semester = semesterSpinner.getValue();
                int credits = creditsSpinner.getValue();
                String department = departmentField.getText();
                String description = descriptionArea.getText();

                Program selectedProgram = programComboBox.getSelectionModel().getSelectedItem();
                if (selectedProgram == null) {
                    showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Program.");
                    return;
                }
                int newProgramId = selectedProgram.getProgramId();

                // Update the selected course object
                selectedCourse.setProgramId(newProgramId);
                selectedCourse.setSemesterNumber(semester);
                selectedCourse.setCourseCode(courseCode);
                selectedCourse.setCourseName(courseName);
                selectedCourse.setCredits(credits);
                selectedCourse.setDescription(description);
                selectedCourse.setDepartment(department);

                courseService.updateCourse(selectedCourse);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully.");
                loadCourses();
                handleClearSelection(null);
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update course: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to update.");
        }
    }

    @FXML
    private void handleDeleteCourse(ActionEvent event) {
        Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Course: " + selectedCourse.getCourseName() + " (" + selectedCourse.getCourseCode() + ")?");
            alert.setContentText("Are you sure you want to delete this course? This action cannot be undone and will delete associated enrollments, assignments, and routines.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    courseService.deleteCourse(selectedCourse.getCourseId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully.");
                    loadCourses();
                    handleClearSelection(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete course: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Error", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a course to delete.");
        }
    }

    @FXML
    private void handleClearSelection(ActionEvent event) {
        courseTable.getSelectionModel().clearSelection();
        courseCodeField.clear();
        courseNameField.clear();
        programComboBox.getSelectionModel().clearSelection();
        semesterSpinner.getValueFactory().setValue(1); // Reset to default
        creditsSpinner.getValueFactory().setValue(3); // Reset to default
        departmentField.clear();
        descriptionArea.clear();

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
