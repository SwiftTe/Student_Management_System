package com.example.demo;

import com.example.demo.Assignment;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    /**
     * Adds a new assignment to the database.
     *
     * @param assignment The Assignment object to add. Its assignmentId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addAssignment(Assignment assignment) throws SQLException {
        String sql = "INSERT INTO Assignments (course_id, faculty_id, title, description, due_date, max_marks) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, assignment.getCourseId());
            stmt.setInt(2, assignment.getFacultyId());
            stmt.setString(3, assignment.getTitle());
            stmt.setString(4, assignment.getDescription());
            stmt.setDate(5, Date.valueOf(assignment.getDueDate()));
            stmt.setInt(6, assignment.getMaxMarks());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating assignment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    assignment.setAssignmentId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating assignment failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves an assignment by its ID.
     *
     * @param assignmentId The ID of the assignment to retrieve.
     * @return The Assignment object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Assignment getAssignmentById(int assignmentId) throws SQLException {
        String sql = "SELECT assignment_id, course_id, faculty_id, title, description, due_date, max_marks, created_at FROM Assignments WHERE assignment_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Assignment(
                            rs.getInt("assignment_id"),
                            rs.getInt("course_id"),
                            rs.getInt("faculty_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getDate("due_date").toLocalDate(),
                            rs.getInt("max_marks"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all assignments for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Assignment objects for the given course.
     * @throws SQLException If a database access error occurs.
     */
    public List<Assignment> getAssignmentsByCourseId(int courseId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT assignment_id, course_id, faculty_id, title, description, due_date, max_marks, created_at FROM Assignments WHERE course_id = ? ORDER BY due_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(new Assignment(
                            rs.getInt("assignment_id"),
                            rs.getInt("course_id"),
                            rs.getInt("faculty_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getDate("due_date").toLocalDate(),
                            rs.getInt("max_marks"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return assignments;
    }

    /**
     * Retrieves all assignments created by a specific faculty member.
     *
     * @param facultyId The ID of the faculty member.
     * @return A list of Assignment objects created by the given faculty.
     * @throws SQLException If a database access error occurs.
     */
    public List<Assignment> getAssignmentsByFacultyId(int facultyId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT assignment_id, course_id, faculty_id, title, description, due_date, max_marks, created_at FROM Assignments WHERE faculty_id = ? ORDER BY due_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facultyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(new Assignment(
                            rs.getInt("assignment_id"),
                            rs.getInt("course_id"),
                            rs.getInt("faculty_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getDate("due_date").toLocalDate(),
                            rs.getInt("max_marks"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return assignments;
    }


    /**
     * Retrieves all assignments from the database.
     *
     * @return A list of all Assignment objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Assignment> getAllAssignments() throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT assignment_id, course_id, faculty_id, title, description, due_date, max_marks, created_at FROM Assignments ORDER BY due_date DESC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                assignments.add(new Assignment(
                        rs.getInt("assignment_id"),
                        rs.getInt("course_id"),
                        rs.getInt("faculty_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getInt("max_marks"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return assignments;
    }

    /**
     * Updates an existing assignment's information.
     *
     * @param assignment The Assignment object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAssignment(Assignment assignment) throws SQLException {
        String sql = "UPDATE Assignments SET course_id=?, faculty_id=?, title=?, description=?, due_date=?, max_marks=? WHERE assignment_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignment.getCourseId());
            stmt.setInt(2, assignment.getFacultyId());
            stmt.setString(3, assignment.getTitle());
            stmt.setString(4, assignment.getDescription());
            stmt.setDate(5, Date.valueOf(assignment.getDueDate()));
            stmt.setInt(6, assignment.getMaxMarks());
            stmt.setInt(7, assignment.getAssignmentId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an assignment from the database by its ID.
     *
     * @param assignmentId The ID of the assignment to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAssignment(int assignmentId) throws SQLException {
        String sql = "DELETE FROM Assignments WHERE assignment_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.executeUpdate();
        }
    }
}