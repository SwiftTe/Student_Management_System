package com.example.demo;

import com.example.demo.Faculty;
import com.example.demo.User; // Required to handle user creation for faculty
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {

    private UserDAO userDAO; // To manage user accounts associated with faculty

    public FacultyDAO() {
        this.userDAO = new UserDAO();
    }

    /**
     * Adds a new faculty member and simultaneously creates a corresponding user account for them.
     * This operation is transactional: if user creation fails, faculty creation will also be rolled back.
     *
     * @param faculty The Faculty object to add. Its userId and facultyId will be updated upon successful creation.
     * @param username The desired username for the faculty's login.
     * @param password The plain-text password for the faculty's login (will be stored as hash in real app).
     * @throws SQLException If a database access error occurs during user or faculty creation.
     */
    public void addFacultyWithUser(Faculty faculty, String username, String password) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Create a new user for the faculty
            // IMPORTANT: In a real app, hash the password here (e.g., String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());)
            // For now, using plain text as per project constraints for simplicity.
            User newUser = new User(username, password, "Faculty"); // Role is fixed as "Faculty"
            userDAO.addUser(newUser); // This method will set newUser.userId

            // Set the generated userId to the faculty object
            faculty.setUserId(newUser.getUserId());

            // 2. Add the faculty details
            String sql = "INSERT INTO Faculty (user_id, first_name, last_name, email, phone_number, department) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, faculty.getUserId());
                stmt.setString(2, faculty.getFirstName());
                stmt.setString(3, faculty.getLastName());
                stmt.setString(4, faculty.getEmail());
                stmt.setString(5, faculty.getPhoneNumber());
                stmt.setString(6, faculty.getDepartment());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating faculty failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        faculty.setFacultyId(generatedKeys.getInt(1)); // Set the generated facultyId back to the object
                    } else {
                        throw new SQLException("Creating faculty failed, no ID obtained.");
                    }
                }
            }

            conn.commit(); // Commit transaction if both user and faculty are added successfully
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
     * Retrieves a faculty member by their faculty ID.
     *
     * @param facultyId The ID of the faculty member to retrieve.
     * @return The Faculty object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Faculty getFacultyById(int facultyId) throws SQLException {
        String sql = "SELECT faculty_id, user_id, first_name, last_name, email, phone_number, department FROM Faculty WHERE faculty_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facultyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Faculty(
                            rs.getInt("faculty_id"),
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a faculty member by their associated user ID.
     *
     * @param userId The ID of the user associated with the faculty.
     * @return The Faculty object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Faculty getFacultyByUserId(int userId) throws SQLException {
        String sql = "SELECT faculty_id, user_id, first_name, last_name, email, phone_number, department FROM Faculty WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Faculty(
                            rs.getInt("faculty_id"),
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all faculty members from the database.
     *
     * @return A list of all Faculty objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Faculty> getAllFaculty() throws SQLException {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT faculty_id, user_id, first_name, last_name, email, phone_number, department FROM Faculty";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                facultyList.add(new Faculty(
                        rs.getInt("faculty_id"),
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("department")
                ));
            }
        }
        return facultyList;
    }

    /**
     * Updates an existing faculty member's information.
     *
     * @param faculty The Faculty object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateFaculty(Faculty faculty) throws SQLException {
        String sql = "UPDATE Faculty SET first_name=?, last_name=?, email=?, phone_number=?, department=? WHERE faculty_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, faculty.getFirstName());
            stmt.setString(2, faculty.getLastName());
            stmt.setString(3, faculty.getEmail());
            stmt.setString(4, faculty.getPhoneNumber());
            stmt.setString(5, faculty.getDepartment());
            stmt.setInt(6, faculty.getFacultyId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a faculty member from the database by their faculty ID.
     * Also deletes the associated user account. This is a transactional operation.
     *
     * @param facultyId The ID of the faculty member to delete.
     * @throws SQLException If a database access error occurs during deletion.
     */
    public void deleteFaculty(int facultyId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get the user_id associated with the faculty
            int userIdToDelete = -1;
            String getUserIdSql = "SELECT user_id FROM Faculty WHERE faculty_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdSql)) {
                stmt.setInt(1, facultyId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userIdToDelete = rs.getInt("user_id");
                    } else {
                        throw new SQLException("Faculty with ID " + facultyId + " not found.");
                    }
                }
            }

            // 2. Delete the faculty record
            String deleteFacultySql = "DELETE FROM Faculty WHERE faculty_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteFacultySql)) {
                stmt.setInt(1, facultyId);
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
                    System.err.println("Rollback failed during faculty deletion: " + ex.getMessage());
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

