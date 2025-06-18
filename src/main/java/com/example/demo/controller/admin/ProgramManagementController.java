package com.example.demo.controller.admin;

import com.example.demo.model.Program;
import com.example.demo.service.ProgramService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProgramManagementController {

    @FXML
    private TextField programNameField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Program> programTable;
    @FXML
    private TableColumn<Program, Integer> programIdCol;
    @FXML
    private TableColumn<Program, String> programNameCol;

    private ProgramService programService;
    private ObservableList<Program> programList;

    public ProgramManagementController() {
        this.programService = new ProgramService();
        this.programList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Configure TableView columns to link with Program model properties
        programIdCol.setCellValueFactory(new PropertyValueFactory<>("programId"));
        programNameCol.setCellValueFactory(new PropertyValueFactory<>("programName"));

        // Set the items for the table
        programTable.setItems(programList);

        // Load existing programs from the database
        loadPrograms();

        // Add listener to table selection to populate fields for editing/deleting
        programTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProgramDetails(newValue));

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Loads all programs from the database and updates the TableView.
     */
    private void loadPrograms() {
        try {
            List<Program> programs = programService.getAllPrograms();
            programList.setAll(programs); // Replaces all elements in the observable list
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load programs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the details of the selected program in the input fields.
     * @param program The selected Program object, or null if selection is cleared.
     */
    private void showProgramDetails(Program program) {
        if (program != null) {
            programNameField.setText(program.getProgramName());
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            addButton.setDisable(true); // Disable add when an item is selected for update/delete
        } else {
            handleClearSelection(null); // Clear fields if nothing is selected
        }
    }

    @FXML
    private void handleAddProgram(ActionEvent event) {
        String programName = programNameField.getText();
        try {
            Program newProgram = programService.addNewProgram(programName);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Program '" + newProgram.getProgramName() + "' added successfully with ID: " + newProgram.getProgramId());
            loadPrograms(); // Refresh table
            handleClearSelection(null); // Clear fields and selection
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add program: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateProgram(ActionEvent event) {
        Program selectedProgram = programTable.getSelectionModel().getSelectedItem();
        if (selectedProgram != null) {
            String newProgramName = programNameField.getText();
            try {
                selectedProgram.setProgramName(newProgramName);
                programService.updateProgram(selectedProgram);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Program updated successfully.");
                loadPrograms(); // Refresh table
                handleClearSelection(null); // Clear fields and selection
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.WARNING, "Input Error", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update program: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a program to update.");
        }
    }

    @FXML
    private void handleDeleteProgram(ActionEvent event) {
        Program selectedProgram = programTable.getSelectionModel().getSelectedItem();
        if (selectedProgram != null) {
            // Confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Program: " + selectedProgram.getProgramName() + "?");
            alert.setContentText("Are you sure you want to delete this program? This action cannot be undone and may affect associated students and courses.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    programService.deleteProgram(selectedProgram.getProgramId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Program deleted successfully.");
                    loadPrograms(); // Refresh table
                    handleClearSelection(null); // Clear fields and selection
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete program. It might be linked to existing students or courses: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.WARNING, "Deletion Error", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a program to delete.");
        }
    }

    @FXML
    private void handleClearSelection(ActionEvent event) {
        programTable.getSelectionModel().clearSelection();
        programNameField.clear();
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
