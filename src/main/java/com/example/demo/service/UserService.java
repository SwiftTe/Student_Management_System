package com.example.demo.service;

import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

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
        return userDAO.authenticateUser(username, password, role);
    }

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

        if (userDAO.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' already exists.");
        }

        User newUser = new User(username, password, role);
        userDAO.addUser(newUser);
        return newUser;
    }

    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return userDAO.getUserById(userId);
    }

    public User getUserByUsername(String username) throws IllegalArgumentException, SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        return userDAO.getUserByUsername(username);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

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

        User existingUserWithSameUsername = userDAO.getUserByUsername(user.getUsername());
        if (existingUserWithSameUsername != null && existingUserWithSameUsername.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken by another user.");
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPasswordHash(newPassword);
        } else {
            User existingUser = userDAO.getUserById(user.getUserId());
            if (existingUser != null) {
                user.setPasswordHash(existingUser.getPasswordHash());
            } else {
                throw new IllegalArgumentException("Could not find existing user to retain password hash.");
            }
        }
        userDAO.updateUser(user);
    }

    public void changePassword(int userId, String currentPassword, String newPassword) throws IllegalArgumentException, SQLException {
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password cannot be empty and must be at least 6 characters long.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!user.getPasswordHash().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        user.setPasswordHash(newPassword);
        userDAO.updateUser(user);
    }

    public void deleteUser(int userId) throws IllegalArgumentException, SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive for deletion.");
        }
        userDAO.deleteUser(userId);
    }

    private boolean isValidRole(String role) {
        return "Admin".equals(role) || "Student".equals(role) || "Faculty".equals(role) || "Librarian".equals(role);
    }
}
