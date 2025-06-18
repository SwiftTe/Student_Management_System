package com.example.demo.service;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user.
     * This method also performs basic validation on inputs.
     * IMPORTANT: In a real-world application, password hashing and comparison (e.g., using BCrypt)
     * would happen here instead of passing plain text to DAO. For this project, plain text is used
     * in DAO due to "no unnecessary tech" constraint, but this is a critical security vulnerability
     * for production.
     *
     * @param username The username provided by the user.
     * @param password The password provided by the user.
     * @param role The role selected by the user.
     * @return The authenticated User object if successful, null otherwise.
     * @throws IllegalArgumentException If input validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public User authenticateUser(String username, String password, String role) throws IllegalArgumentException, SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role must be selected.");
        }

        // For a real app: Hash the password provided by the user here and pass the hash to DAO
        // String hashedPassword = hashPassword(password); // Custom hash method or library call
        return userDAO.authenticateUser(username, password, role); // Currently passing plain text password to DAO
    }

    /**
     * Creates a new user in the system.
     * Performs validation and uses the UserDAO to persist the user.
     * IMPORTANT: The password passed here should be plain text, and it will be hashed
     * within this service method before being passed to the DAO in a real application.
     * For this project, we're passing it through to the DAO directly for simplicity.
     *
     * @param username The desired username.
     * @param password The plain-text password for the new user.
     * @param role The role for the new user ('Admin', 'Student', 'Faculty', 'Librarian').
     * @return The created User object with its new ID.
     * @throws IllegalArgumentException If input validation fails or username already exists.
     * @throws SQLException If a database access error occurs.
     */
    public User createUser(String username, String password, String role) throws IllegalArgumentException, SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty.");
        }
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }

        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' already exists.");
        }

        // IMPORTANT: In a real app, hash the password here before creating the User object
        // For example: String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        // For this project:
        User newUser = new User(username, password, role); // password is plain text here (for simplicity only)
        userDAO.addUser(newUser);
        return newUser;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user.
     * @return The User object, or null if not found.
     * @throws SQLException If a database error occurs.
     */
    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return userDAO.getUserById(userId);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return The User object, or null if not found.
     * @throws IllegalArgumentException If username is invalid.
     * @throws SQLException If a database error occurs.
     */
    public User getUserByUsername(String username) throws IllegalArgumentException, SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        return userDAO.getUserByUsername(username);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return A list of all User objects.
     * @throws SQLException If a database error occurs.
     */
    public List<User> getAllUsers() throws SQLException { // Corrected method name: getAllUsers()
        return userDAO.getAllUsers();
    }

    /**
     * Updates an existing user's details, including username, password, and role.
     * * @param user The User object with updated details.
     * @param newPassword Optional new password. If provided, it will be updated (and hashed in real app).
     * @throws IllegalArgumentException If input validation fails or new username already exists.
     * @throws SQLException If a database error occurs.
     */
    public void updateUser(User user, String newPassword) throws IllegalArgumentException, SQLException {
        if (user == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("User and a valid User ID are required for update.");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty() || !isValidRole(user.getRole())) {
            throw new IllegalArgumentException("Invalid user role.");
        }

        // Check for duplicate username if changed
        User existingUserWithSameUsername = userDAO.getUserByUsername(user.getUsername());
        if (existingUserWithSameUsername != null && existingUserWithSameUsername.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken by another user.");
        }

        // Handle password update
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // IMPORTANT: In a real app, hash newPassword here
            user.setPasswordHash(newPassword); // For this project, directly setting plain text
        } else {
            // If no new password is provided, retain the existing hashed password
            // Fetch the existing user to get their current password hash
            User existingUser = userDAO.getUserById(user.getUserId());
            if (existingUser != null) {
                user.setPasswordHash(existingUser.getPasswordHash());
            } else {
                // This case should ideally not happen if user.getUserId() is valid
                throw new IllegalArgumentException("Could not find existing user to retain password hash.");
            }
        }
        userDAO.updateUser(user);
    }

    /**
     * Changes a user's password.
     *
     * @param userId The ID of the user whose password is to be changed.
     * @param currentPassword The user's current plain-text password.
     * @param newPassword The new plain-text password.
     * @throws IllegalArgumentException If passwords do not meet criteria or current password is incorrect.
     * @throws SQLException If a database error occurs.
     */
    public void changePassword(int userId, String currentPassword, String newPassword) throws IllegalArgumentException, SQLException {
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) { // Example length check
            throw new IllegalArgumentException("New password cannot be empty and must be at least 6 characters long.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        // IMPORTANT: In a real app, compare hashed currentPassword with user.getPasswordHash()
        if (!user.getPasswordHash().equals(currentPassword)) { // For this project, direct comparison
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        // IMPORTANT: In a real app, hash newPassword before setting it
        user.setPasswordHash(newPassword); // For this project, directly setting plain text
        userDAO.updateUser(user); // Reuse updateUser to persist password change
    }

    /**
     * Deletes a user from the system.
     * This method should be used with caution as it will delete the user's login and
     * potentially cascade deletions or set null on related records (Student, Faculty, Librarian).
     *
     * @param userId The ID of the user to delete.
     * @throws IllegalArgumentException If user ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteUser(int userId) throws IllegalArgumentException, SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive for deletion.");
        }
        userDAO.deleteUser(userId);
    }

    /**
     * Helper method to validate if a given string is a recognized user role.
     *
     * @param role The role string to validate.
     * @return True if the role is valid, false otherwise.
     */
    private boolean isValidRole(String role) {
        return "Admin".equals(role) || "Student".equals(role) || "Faculty".equals(role) || "Librarian".equals(role);
    }
}
