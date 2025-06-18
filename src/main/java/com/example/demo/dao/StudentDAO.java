package com.example.demo.dao;

import com.example.demo.model.Student;
import com.example.demo.model.User; // Required to handle user creation for students
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private UserDAO userDAO; // To manage user accounts associated with students

    public StudentDAO() {
        this.userDAO = new UserDAO();
    }

    /**
     * Adds a new student and simultaneously creates a corresponding user account for them.
     * This operation is transactional: if user creation fails, student creation will also be rolled back.
     *
     * @param student The Student object to add. Its userId and studentId will be updated upon successful creation.
     * @param username The desired username for the student's login.
     * @param password The plain-text password for the student's login (will be stored as hash in real app).
     * @throws SQLException If a database access error occurs during user or student creation.
     */
    public void addStudentWithUser(Student student, String username, String password) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Create a new user for the student
            // IMPORTANT: In a real app, hash the password here (e.g., String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());)
            // For now, using plain text as per project constraints for simplicity.
            User newUser = new User(username, password, "Student"); // Role is fixed as "Student"
            userDAO.addUser(newUser); // This method will set newUser.userId

            // Set the generated userId to the student object
            student.setUserId(newUser.getUserId());

            // 2. Add the student details
            String sql = "INSERT INTO Students (user_id, program_id, first_name, last_name, date_of_birth, gender, email, phone_number, address, enrollment_date, major) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, student.getUserId());
                stmt.setInt(2, student.getProgramId());
                stmt.setString(3, student.getFirstName());
                stmt.setString(4, student.getLastName());
                stmt.setDate(5, Date.valueOf(student.getDateOfBirth()));
                stmt.setString(6, student.getGender());
                stmt.setString(7, student.getEmail());
                stmt.setString(8, student.getPhoneNumber());
                stmt.setString(9, student.getAddress());
                stmt.setDate(10, Date.valueOf(student.getEnrollmentDate()));
                stmt.setString(11, student.getMajor());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating student failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1)); // Set the generated studentId back to the object
                    } else {
                        throw new SQLException("Creating student failed, no ID obtained.");
                    }
                }
            }

            conn.commit(); // Commit transaction if both user and student are added successfully
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if any part of the transaction fails
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit mode
                    conn.close(); // Close the connection
                } catch (SQLException ex) {
                    System.err.println("Closing connection failed: " + ex.getMessage());
                }
            }
        }
    }


    /**
     * Retrieves a student by their student ID.
     *
     * @param studentId The ID of the student to retrieve.
     * @return The Student object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, user_id, program_id, first_name, last_name, date_of_birth, gender, email, phone_number, address, enrollment_date, major FROM Students WHERE student_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getInt("user_id"),
                            rs.getInt("program_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getDate("date_of_birth").toLocalDate(),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getDate("enrollment_date").toLocalDate(),
                            rs.getString("major")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a student by their associated user ID.
     *
     * @param userId The ID of the user associated with the student.
     * @return The Student object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Student getStudentByUserId(int userId) throws SQLException {
        String sql = "SELECT student_id, user_id, program_id, first_name, last_name, date_of_birth, gender, email, phone_number, address, enrollment_date, major FROM Students WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getInt("user_id"),
                            rs.getInt("program_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getDate("date_of_birth").toLocalDate(),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address"),
                            rs.getDate("enrollment_date").toLocalDate(),
                            rs.getString("major")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all students from the database.
     *
     * @return A list of all Student objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, user_id, program_id, first_name, last_name, date_of_birth, gender, email, phone_number, address, enrollment_date, major FROM Students";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("student_id"),
                        rs.getInt("user_id"),
                        rs.getInt("program_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getDate("date_of_birth").toLocalDate(),
                        rs.getString("gender"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getDate("enrollment_date").toLocalDate(),
                        rs.getString("major")
                ));
            }
        }
        return students;
    }

    /**
     * Updates an existing student's information.
     *
     * @param student The Student object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateStudent(Student student) throws SQLException {
        String sql = "UPDATE Students SET program_id=?, first_name=?, last_name=?, date_of_birth=?, gender=?, email=?, phone_number=?, address=?, enrollment_date=?, major=? WHERE student_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, student.getProgramId());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setDate(4, Date.valueOf(student.getDateOfBirth()));
            stmt.setString(5, student.getGender());
            stmt.setString(6, student.getEmail());
            stmt.setString(7, student.getPhoneNumber());
            stmt.setString(8, student.getAddress());
            stmt.setDate(9, Date.valueOf(student.getEnrollmentDate()));
            stmt.setString(10, student.getMajor());
            stmt.setInt(11, student.getStudentId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a student from the database by their student ID.
     * Also deletes the associated user account. This is a transactional operation.
     *
     * @param studentId The ID of the student to delete.
     * @throws SQLException If a database access error occurs during deletion.
     */
    public void deleteStudent(int studentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get the user_id associated with the student
            int userIdToDelete = -1;
            String getUserIdSql = "SELECT user_id FROM Students WHERE student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userIdToDelete = rs.getInt("user_id");
                    } else {
                        throw new SQLException("Student with ID " + studentId + " not found.");
                    }
                }
            }

            // 2. Delete the student record
            String deleteStudentSql = "DELETE FROM Students WHERE student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteStudentSql)) {
                stmt.setInt(1, studentId);
                stmt.executeUpdate();
            }

            // 3. Delete the associated user account
            if (userIdToDelete != -1) {
                userDAO.deleteUser(userIdToDelete); // Deletes from Users table
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if any part of the transaction fails
                } catch (SQLException ex) {
                    System.err.println("Rollback failed during student deletion: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit mode
                    conn.close(); // Close the connection
                } catch (SQLException ex) {
                    System.err.println("Closing connection failed: " + ex.getMessage());
                }
            }
        }
    }
}
