package com.example.demo.service;

import com.example.demo.dao.ProgramDAO;
import com.example.demo.model.Program;
import java.sql.SQLException;
import java.util.List;

public class ProgramService {
    private ProgramDAO programDAO;

    public ProgramService() {
        this.programDAO = new ProgramDAO();
    }

    /**
     * Adds a new academic program.
     * Performs validation to ensure the program name is not empty and doesn't already exist.
     *
     * @param programName The name of the new program.
     * @return The created Program object with its new ID.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public Program addNewProgram(String programName) throws IllegalArgumentException, SQLException {
        if (programName == null || programName.trim().isEmpty()) {
            throw new IllegalArgumentException("Program name cannot be empty.");
        }
        String trimmedProgramName = programName.trim();

        // Check for duplicate program name
        if (programDAO.getProgramByName(trimmedProgramName) != null) {
            throw new IllegalArgumentException("Program '" + trimmedProgramName + "' already exists.");
        }

        Program newProgram = new Program(trimmedProgramName);
        programDAO.addProgram(newProgram);
        return newProgram;
    }

    /**
     * Retrieves a program by its ID.
     *
     * @param programId The ID of the program.
     * @return The Program object, or null if not found.
     * @throws IllegalArgumentException If program ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Program getProgramById(int programId) throws IllegalArgumentException, SQLException {
        if (programId <= 0) {
            throw new IllegalArgumentException("Program ID must be positive.");
        }
        return programDAO.getProgramById(programId);
    }

    /**
     * Retrieves a program by its name.
     *
     * @param programName The name of the program.
     * @return The Program object, or null if not found.
     * @throws IllegalArgumentException If program name is empty.
     * @throws SQLException If a database error occurs.
     */
    public Program getProgramByName(String programName) throws IllegalArgumentException, SQLException {
        if (programName == null || programName.trim().isEmpty()) {
            throw new IllegalArgumentException("Program name cannot be empty.");
        }
        return programDAO.getProgramByName(programName.trim());
    }

    /**
     * Retrieves all academic programs.
     *
     * @return A list of all Program objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Program> getAllPrograms() throws SQLException {
        return programDAO.getAllPrograms();
    }

    /**
     * Updates an existing academic program's name.
     * Performs validation to ensure the new program name is not empty and unique (excluding itself).
     *
     * @param program The Program object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateProgram(Program program) throws IllegalArgumentException, SQLException {
        if (program == null || program.getProgramId() <= 0) {
            throw new IllegalArgumentException("Program and a valid Program ID are required for update.");
        }
        if (program.getProgramName() == null || program.getProgramName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name cannot be empty.");
        }
        String trimmedProgramName = program.getProgramName().trim();

        // Check for duplicate program name, excluding the current program being updated
        Program existingProgram = programDAO.getProgramByName(trimmedProgramName);
        if (existingProgram != null && existingProgram.getProgramId() != program.getProgramId()) {
            throw new IllegalArgumentException("Program '" + trimmedProgramName + "' is already taken by another program.");
        }

        program.setProgramName(trimmedProgramName); // Ensure the name is trimmed before saving
        programDAO.updateProgram(program);
    }

    /**
     * Deletes an academic program by its ID.
     *
     * @param programId The ID of the program to delete.
     * @throws IllegalArgumentException If program ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteProgram(int programId) throws IllegalArgumentException, SQLException {
        if (programId <= 0) {
            throw new IllegalArgumentException("Program ID must be positive for deletion.");
        }
        // Potential business logic: check if any students or courses are linked to this program
        // before allowing deletion. MySQL FK constraints (ON DELETE RESTRICT/CASCADE) handle this on DB level,
        // but explicit checks here provide better user feedback.
        // For simplicity, we rely on DB RESTRICT for students and CASCADE for courses for now.

        programDAO.deleteProgram(programId);
    }
}
