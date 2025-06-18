package com.example.demo.dao;

import com.example.demo.model.User;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // For RETURN_GENERATED_KEYS
import java.time.LocalDateTime;

public class UserDAO {

    /**
     * Authenticates a user based on username, password, and role.
     * In a real application, 'password' should be hashed and compared with 'password_hash'.
     *
     * @param username The username to authenticate.
     * @param password The plain-text password (will be compared to hashed version in a real scenario).
     * @param role     The expected role of the user.
     * @return A User object if authentication is successful, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public User authenticateUser(String username, String password, String role) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, created_at FROM Users WHERE username = ? AND password_hash = ? AND role = ?";
        try (Connection conn = DBController.getConnection(); // Use your DBController
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // IMPORTANT: In a real app, hash 'password' here before setting it
            stmt.setString(3, role);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // User found and authenticated
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"), // Retrieve the stored hash
                            rs.getString("role"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null; // Authentication failed
    }

    /**
     * Adds a new user to the database.
     * The password_hash in the User object should already be hashed before calling this method.
     *
     * @param user The User object containing details (username, passwordHash, role).
     * @throws SQLException If a database access error occurs.
     */
    public void addUser(User user) throws SQLException {
        // IMPORTANT: Ensure user.getPasswordHash() already contains the securely hashed password
        String sql = "INSERT INTO Users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                    // Note: createdAt is set by DB, not explicitly retrieved here but could be.
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a user by their user ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, created_at FROM Users WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return The User object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, created_at FROM Users WHERE username = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates an existing user's information (username, password_hash, role).
     *
     * @param user The User object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE Users SET username = ?, password_hash = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getUserId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a user from the database by their user ID.
     *
     * @param userId The ID of the user to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
