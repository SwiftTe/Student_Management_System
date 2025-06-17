package com.example.demo;

import com.example.demo.Result;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {

    /**
     * Adds a new result record to the database.
     * Marks obtained, grade, and result status can be null initially.
     *
     * @param result The Result object to add. Its resultId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addResult(Result result) throws SQLException {
        String sql = "INSERT INTO Results (student_id, course_id, semester_number, academic_year, marks_obtained, grade, result_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getStudentId());
            stmt.setInt(2, result.getCourseId());
            stmt.setInt(3, result.getSemesterNumber());
            stmt.setString(4, result.getAcademicYear());
            if (result.getMarksObtained() != null) {
                stmt.setInt(5, result.getMarksObtained());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, result.getGrade()); // Can be null
            stmt.setString(7, result.getResultStatus()); // Default 'Incomplete'

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating result record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setResultId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating result record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a result record by its ID.
     *
     * @param resultId The ID of the result record to retrieve.
     * @return The Result object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Result getResultById(int resultId) throws SQLException {
        String sql = "SELECT result_id, student_id, course_id, semester_number, academic_year, marks_obtained, grade, result_status FROM Results WHERE result_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Result(
                            rs.getInt("result_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getInt("semester_number"),
                            rs.getString("academic_year"),
                            (Integer) rs.getObject("marks_obtained"), // For nullable INT
                            rs.getString("grade"),
                            rs.getString("result_status")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all results for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Result objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<Result> getResultsByStudentId(int studentId) throws SQLException {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT result_id, student_id, course_id, semester_number, academic_year, marks_obtained, grade, result_status FROM Results WHERE student_id = ? ORDER BY academic_year DESC, semester_number ASC, course_id ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Result(
                            rs.getInt("result_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getInt("semester_number"),
                            rs.getString("academic_year"),
                            (Integer) rs.getObject("marks_obtained"),
                            rs.getString("grade"),
                            rs.getString("result_status")
                    ));
                }
            }
        }
        return results;
    }

    /**
     * Retrieves results for a specific student in a specific course for a given academic year.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @param academicYear The academic year (e.g., "2023-2024").
     * @return The Result object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Result getResultByStudentCourseAndYear(int studentId, int courseId, String academicYear) throws SQLException {
        String sql = "SELECT result_id, student_id, course_id, semester_number, academic_year, marks_obtained, grade, result_status FROM Results WHERE student_id = ? AND course_id = ? AND academic_year = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.setString(3, academicYear);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Result(
                            rs.getInt("result_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getInt("semester_number"),
                            rs.getString("academic_year"),
                            (Integer) rs.getObject("marks_obtained"),
                            rs.getString("grade"),
                            rs.getString("result_status")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all result records from the database.
     *
     * @return A list of all Result objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Result> getAllResults() throws SQLException {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT result_id, student_id, course_id, semester_number, academic_year, marks_obtained, grade, result_status FROM Results ORDER BY academic_year DESC, student_id ASC, course_id ASC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new Result(
                        rs.getInt("result_id"),
                        rs.getInt("student_id"),
                        rs.getInt("course_id"),
                        rs.getInt("semester_number"),
                        rs.getString("academic_year"),
                        (Integer) rs.getObject("marks_obtained"),
                        rs.getString("grade"),
                        rs.getString("result_status")
                ));
            }
        }
        return results;
    }

    /**
     * Updates an existing result record.
     *
     * @param result The Result object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateResult(Result result) throws SQLException {
        String sql = "UPDATE Results SET student_id=?, course_id=?, semester_number=?, academic_year=?, marks_obtained=?, grade=?, result_status=? WHERE result_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, result.getStudentId());
            stmt.setInt(2, result.getCourseId());
            stmt.setInt(3, result.getSemesterNumber());
            stmt.setString(4, result.getAcademicYear());
            if (result.getMarksObtained() != null) {
                stmt.setInt(5, result.getMarksObtained());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, result.getGrade());
            stmt.setString(7, result.getResultStatus());
            stmt.setInt(8, result.getResultId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a result record from the database by its ID.
     *
     * @param resultId The ID of the result record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteResult(int resultId) throws SQLException {
        String sql = "DELETE FROM Results WHERE result_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            stmt.executeUpdate();
        }
    }
}
