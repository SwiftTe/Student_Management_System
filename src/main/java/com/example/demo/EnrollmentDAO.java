package com.example.demo;

import com.example.demo.Enrollment;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    /**
     * Adds a new student enrollment to a course.
     *
     * @param enrollment The Enrollment object containing studentId, courseId, enrollmentDate, and initial grade.
     * @throws SQLException If a database access error occurs.
     */
    public void addEnrollment(Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO Enrollments (student_id, course_id, enrollment_date, grade) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setDate(3, Date.valueOf(enrollment.getEnrollmentDate()));
            stmt.setString(4, enrollment.getGrade()); // Can be null

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating enrollment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    enrollment.setEnrollmentId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating enrollment failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves an enrollment record by its ID.
     *
     * @param enrollmentId The ID of the enrollment to retrieve.
     * @return The Enrollment object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Enrollment getEnrollmentById(int enrollmentId) throws SQLException {
        String sql = "SELECT enrollment_id, student_id, course_id, enrollment_date, grade FROM Enrollments WHERE enrollment_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("enrollment_date").toLocalDate(),
                            rs.getString("grade")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all enrollments for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Enrollment objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<Enrollment> getEnrollmentsByStudentId(int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT enrollment_id, student_id, course_id, enrollment_date, grade FROM Enrollments WHERE student_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("enrollment_date").toLocalDate(),
                            rs.getString("grade")
                    ));
                }
            }
        }
        return enrollments;
    }

    /**
     * Retrieves all enrollments for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Enrollment objects for the given course.
     * @throws SQLException If a database access error occurs.
     */
    public List<Enrollment> getEnrollmentsByCourseId(int courseId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT enrollment_id, student_id, course_id, enrollment_date, grade FROM Enrollments WHERE course_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("enrollment_date").toLocalDate(),
                            rs.getString("grade")
                    ));
                }
            }
        }
        return enrollments;
    }

    /**
     * Checks if a student is already enrolled in a specific course.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return True if enrolled, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean isStudentEnrolledInCourse(int studentId, int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all enrollment records from the database.
     *
     * @return A list of all Enrollment objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Enrollment> getAllEnrollments() throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT enrollment_id, student_id, course_id, enrollment_date, grade FROM Enrollments";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                enrollments.add(new Enrollment(
                        rs.getInt("enrollment_id"),
                        rs.getInt("student_id"),
                        rs.getInt("course_id"),
                        rs.getDate("enrollment_date").toLocalDate(),
                        rs.getString("grade")
                ));
            }
        }
        return enrollments;
    }

    /**
     * Updates an existing enrollment record's grade or enrollment date.
     *
     * @param enrollment The Enrollment object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateEnrollment(Enrollment enrollment) throws SQLException {
        String sql = "UPDATE Enrollments SET student_id=?, course_id=?, enrollment_date=?, grade=? WHERE enrollment_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setDate(3, Date.valueOf(enrollment.getEnrollmentDate()));
            stmt.setString(4, enrollment.getGrade());
            stmt.setInt(5, enrollment.getEnrollmentId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an enrollment record from the database by its ID.
     *
     * @param enrollmentId The ID of the enrollment to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteEnrollment(int enrollmentId) throws SQLException {
        String sql = "DELETE FROM Enrollments WHERE enrollment_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        }
    }
}
