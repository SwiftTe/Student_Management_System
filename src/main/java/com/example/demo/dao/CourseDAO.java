package com.example.demo.dao;

import com.example.demo.model.Course;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    /**
     * Adds a new course to the database.
     *
     * @param course The Course object to add. Its courseId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO Courses (program_id, semester_number, course_code, course_name, credits, description, department) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, course.getProgramId());
            stmt.setInt(2, course.getSemesterNumber());
            stmt.setString(3, course.getCourseCode());
            stmt.setString(4, course.getCourseName());
            stmt.setInt(5, course.getCredits());
            stmt.setString(6, course.getDescription());
            stmt.setString(7, course.getDepartment());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setCourseId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating course failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param courseId The ID of the course to retrieve.
     * @return The Course object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Course getCourseById(int courseId) throws SQLException {
        String sql = "SELECT course_id, program_id, semester_number, course_code, course_name, credits, description, department FROM Courses WHERE course_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("course_id"),
                            rs.getInt("program_id"),
                            rs.getInt("semester_number"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getString("description"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a course by its course code and program ID.
     *
     * @param courseCode The code of the course.
     * @param programId The ID of the program the course belongs to.
     * @return The Course object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Course getCourseByCodeAndProgram(String courseCode, int programId) throws SQLException {
        String sql = "SELECT course_id, program_id, semester_number, course_code, course_name, credits, description, department FROM Courses WHERE course_code = ? AND program_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setInt(2, programId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("course_id"),
                            rs.getInt("program_id"),
                            rs.getInt("semester_number"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getString("description"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all courses for a specific program and semester.
     *
     * @param programId The ID of the program.
     * @param semesterNumber The semester number.
     * @return A list of Course objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Course> getCoursesByProgramAndSemester(int programId, int semesterNumber) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, program_id, semester_number, course_code, course_name, credits, description, department FROM Courses WHERE program_id = ? AND semester_number = ? ORDER BY course_code";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, programId);
            stmt.setInt(2, semesterNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getInt("course_id"),
                            rs.getInt("program_id"),
                            rs.getInt("semester_number"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            rs.getString("description"),
                            rs.getString("department")
                    ));
                }
            }
        }
        return courses;
    }

    /**
     * Retrieves all courses from the database.
     *
     * @return A list of all Course objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, program_id, semester_number, course_code, course_name, credits, description, department FROM Courses ORDER BY program_id, semester_number, course_code";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getInt("program_id"),
                        rs.getInt("semester_number"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getString("description"),
                        rs.getString("department")
                ));
            }
        }
        return courses;
    }

    /**
     * Updates an existing course's information.
     *
     * @param course The Course object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateCourse(Course course) throws SQLException {
        String sql = "UPDATE Courses SET program_id=?, semester_number=?, course_code=?, course_name=?, credits=?, description=?, department=? WHERE course_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, course.getProgramId());
            stmt.setInt(2, course.getSemesterNumber());
            stmt.setString(3, course.getCourseCode());
            stmt.setString(4, course.getCourseName());
            stmt.setInt(5, course.getCredits());
            stmt.setString(6, course.getDescription());
            stmt.setString(7, course.getDepartment());
            stmt.setInt(8, course.getCourseId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a course from the database by its ID.
     *
     * @param courseId The ID of the course to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM Courses WHERE course_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.executeUpdate();
        }
    }
}
