package com.example.demo;

import com.example.demo.Librarian;
import com.example.demo.User; // Required to handle user creation for librarian
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LibrarianDAO {

    private UserDAO userDAO; // To manage user accounts associated with the librarian

    public LibrarianDAO() {
        this.userDAO = new UserDAO();
    }

    /**
     * Adds a new librarian and simultaneously creates a corresponding user account for them.
     * This operation is transactional: if user creation fails, librarian creation will also be rolled back.
     *
     * @param librarian The Librarian object to add. Its userId and librarianId will be updated upon successful creation.
     * @param username The desired username for the librarian's login.
     * @param password The plain-text password for the librarian's login (will be stored as hash in real app).
     * @throws SQLException If a database access error occurs during user or librarian creation.
     */
    public void addLibrarianWithUser(Librarian librarian, String username, String password) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Create a new user for the librarian
            // IMPORTANT: In a real app, hash the password here
            User newUser = new User(username, password, "Librarian"); // Role is fixed as "Librarian"
            userDAO.addUser(newUser); // This method will set newUser.userId

            // Set the generated userId to the librarian object
            librarian.setUserId(newUser.getUserId());

            // 2. Add the librarian details
            String sql = "INSERT INTO Librarian (user_id, first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, librarian.getUserId());
                stmt.setString(2, librarian.getFirstName());
                stmt.setString(3, librarian.getLastName());
                stmt.setString(4, librarian.getEmail());
                stmt.setString(5, librarian.getPhoneNumber());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating librarian failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        librarian.setLibrarianId(generatedKeys.getInt(1)); // Set the generated librarianId back to the object
                    } else {
                        throw new SQLException("Creating librarian failed, no ID obtained.");
                    }
                }
            }

            conn.commit(); // Commit transaction if both user and librarian are added successfully
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
     * Retrieves a librarian by their librarian ID.
     *
     * @param librarianId The ID of the librarian to retrieve.
     * @return The Librarian object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Librarian getLibrarianById(int librarianId) throws SQLException {
        String sql = "SELECT librarian_id, user_id, first_name, last_name, email, phone_number FROM Librarian WHERE librarian_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, librarianId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Librarian(
                            rs.getInt("librarian_id"),
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a librarian by their associated user ID.
     *
     * @param userId The ID of the user associated with the librarian.
     * @return The Librarian object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Librarian getLibrarianByUserId(int userId) throws SQLException {
        String sql = "SELECT librarian_id, user_id, first_name, last_name, email, phone_number FROM Librarian WHERE user_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Librarian(
                            rs.getInt("librarian_id"),
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all librarian members from the database.
     *
     * @return A list of all Librarian objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Librarian> getAllLibrarians() throws SQLException {
        List<Librarian> librarianList = new ArrayList<>();
        String sql = "SELECT librarian_id, user_id, first_name, last_name, email, phone_number FROM Librarian";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                librarianList.add(new Librarian(
                        rs.getInt("librarian_id"),
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                ));
            }
        }
        return librarianList;
    }

    /**
     * Updates an existing librarian's information.
     *
     * @param librarian The Librarian object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateLibrarian(Librarian librarian) throws SQLException {
        String sql = "UPDATE Librarian SET first_name=?, last_name=?, email=?, phone_number=? WHERE librarian_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, librarian.getFirstName());
            stmt.setString(2, librarian.getLastName());
            stmt.setString(3, librarian.getEmail());
            stmt.setString(4, librarian.getPhoneNumber());
            stmt.setInt(5, librarian.getLibrarianId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a librarian from the database by their librarian ID.
     * Also deletes the associated user account. This is a transactional operation.
     *
     * @param librarianId The ID of the librarian to delete.
     * @throws SQLException If a database access error occurs during deletion.
     */
    public void deleteLibrarian(int librarianId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get the user_id associated with the librarian
            int userIdToDelete = -1;
            String getUserIdSql = "SELECT user_id FROM Librarian WHERE librarian_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getUserIdSql)) {
                stmt.setInt(1, librarianId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userIdToDelete = rs.getInt("user_id");
                    } else {
                        throw new SQLException("Librarian with ID " + librarianId + " not found.");
                    }
                }
            }

            // 2. Delete the librarian record
            String deleteLibrarianSql = "DELETE FROM Librarian WHERE librarian_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteLibrarianSql)) {
                stmt.setInt(1, librarianId);
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
                    System.err.println("Rollback failed during librarian deletion: " + ex.getMessage());
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
