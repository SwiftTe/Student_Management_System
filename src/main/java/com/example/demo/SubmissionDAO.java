package com.example.demo;

import com.example.demo.Submission;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // For LocalDateTime
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    /**
     * Adds a new assignment submission to the database.
     * The submissionDate is automatically set by the database (CURRENT_TIMESTAMP).
     *
     * @param submission The Submission object to add. Its submissionId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addSubmission(Submission submission) throws SQLException {
        String sql = "INSERT INTO Submissions (assignment_id, student_id, file_path) VALUES (?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, submission.getAssignmentId());
            stmt.setInt(2, submission.getStudentId());
            stmt.setString(3, submission.getFilePath());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating submission failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    submission.setSubmissionId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                    // Retrieve generated timestamp if needed, though DB handles default
                    // submission.setSubmissionDate(generatedKeys.getTimestamp(generatedKeys.findColumn("submission_date")).toLocalDateTime());
                } else {
                    throw new SQLException("Creating submission failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a submission by its ID.
     *
     * @param submissionId The ID of the submission to retrieve.
     * @return The Submission object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Submission getSubmissionById(int submissionId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, submission_date, file_path, marks_obtained, feedback FROM Submissions WHERE submission_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submissionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp submissionTimestamp = rs.getTimestamp("submission_date");
                    LocalDateTime submissionDateTime = (submissionTimestamp != null) ? submissionTimestamp.toLocalDateTime() : null;

                    return new Submission(
                            rs.getInt("submission_id"),
                            rs.getInt("assignment_id"),
                            rs.getInt("student_id"),
                            submissionDateTime,
                            rs.getString("file_path"),
                            // Use rs.getObject for nullable INT columns
                            (Integer) rs.getObject("marks_obtained"),
                            rs.getString("feedback")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all submissions for a specific assignment.
     *
     * @param assignmentId The ID of the assignment.
     * @return A list of Submission objects for the given assignment.
     * @throws SQLException If a database access error occurs.
     */
    public List<Submission> getSubmissionsByAssignmentId(int assignmentId) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT submission_id, assignment_id, student_id, submission_date, file_path, marks_obtained, feedback FROM Submissions WHERE assignment_id = ? ORDER BY submission_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp submissionTimestamp = rs.getTimestamp("submission_date");
                    LocalDateTime submissionDateTime = (submissionTimestamp != null) ? submissionTimestamp.toLocalDateTime() : null;

                    submissions.add(new Submission(
                            rs.getInt("submission_id"),
                            rs.getInt("assignment_id"),
                            rs.getInt("student_id"),
                            submissionDateTime,
                            rs.getString("file_path"),
                            (Integer) rs.getObject("marks_obtained"),
                            rs.getString("feedback")
                    ));
                }
            }
        }
        return submissions;
    }

    /**
     * Retrieves a specific student's submission for a given assignment.
     *
     * @param assignmentId The ID of the assignment.
     * @param studentId The ID of the student.
     * @return The Submission object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Submission getSubmissionByAssignmentAndStudent(int assignmentId, int studentId) throws SQLException {
        String sql = "SELECT submission_id, assignment_id, student_id, submission_date, file_path, marks_obtained, feedback FROM Submissions WHERE assignment_id = ? AND student_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp submissionTimestamp = rs.getTimestamp("submission_date");
                    LocalDateTime submissionDateTime = (submissionTimestamp != null) ? submissionTimestamp.toLocalDateTime() : null;

                    return new Submission(
                            rs.getInt("submission_id"),
                            rs.getInt("assignment_id"),
                            rs.getInt("student_id"),
                            submissionDateTime,
                            rs.getString("file_path"),
                            (Integer) rs.getObject("marks_obtained"),
                            rs.getString("feedback")
                    );
                }
            }
        }
        return null;
    }


    /**
     * Updates an existing submission's file path, marks, and feedback.
     *
     * @param submission The Submission object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateSubmission(Submission submission) throws SQLException {
        // Note: submission_date is typically not updated manually as it's a timestamp
        String sql = "UPDATE Submissions SET file_path=?, marks_obtained=?, feedback=? WHERE submission_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, submission.getFilePath());
            // For nullable Integer, use setObject(index, value, type) or check for null
            if (submission.getMarksObtained() != null) {
                stmt.setInt(2, submission.getMarksObtained());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, submission.getFeedback());
            stmt.setInt(4, submission.getSubmissionId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a submission from the database by its ID.
     *
     * @param submissionId The ID of the submission to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteSubmission(int submissionId) throws SQLException {
        String sql = "DELETE FROM Submissions WHERE submission_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submissionId);
            stmt.executeUpdate();
        }
    }
}
