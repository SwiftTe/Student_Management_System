package com.example.demo.dao;

import com.example.demo.model.Program;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // For RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;

public class ProgramDAO {

    /**
     * Adds a new academic program to the database.
     *
     * @param program The Program object containing the program name.
     * @throws SQLException If a database access error occurs.
     */
    public void addProgram(Program program) throws SQLException {
        String sql = "INSERT INTO Programs (program_name) VALUES (?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, program.getProgramName());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating program failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    program.setProgramId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating program failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a program by its ID.
     *
     * @param programId The ID of the program to retrieve.
     * @return The Program object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Program getProgramById(int programId) throws SQLException {
        String sql = "SELECT program_id, program_name FROM Programs WHERE program_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Program(
                            rs.getInt("program_id"),
                            rs.getString("program_name")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a program by its name.
     *
     * @param programName The name of the program to retrieve.
     * @return The Program object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Program getProgramByName(String programName) throws SQLException {
        String sql = "SELECT program_id, program_name FROM Programs WHERE program_name = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, programName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Program(
                            rs.getInt("program_id"),
                            rs.getString("program_name")
                    );
                }
            }
        }
        return null;
    }


    /**
     * Retrieves all academic programs from the database.
     *
     * @return A list of all Program objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Program> getAllPrograms() throws SQLException {
        List<Program> programs = new ArrayList<>();
        String sql = "SELECT program_id, program_name FROM Programs ORDER BY program_name ASC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                programs.add(new Program(
                        rs.getInt("program_id"),
                        rs.getString("program_name")
                ));
            }
        }
        return programs;
    }

    /**
     * Updates an existing academic program's information.
     *
     * @param program The Program object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateProgram(Program program) throws SQLException {
        String sql = "UPDATE Programs SET program_name = ? WHERE program_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, program.getProgramName());
            stmt.setInt(2, program.getProgramId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an academic program from the database by its ID.
     *
     * @param programId The ID of the program to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteProgram(int programId) throws SQLException {
        String sql = "DELETE FROM Programs WHERE program_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.executeUpdate();
        }
    }
}
