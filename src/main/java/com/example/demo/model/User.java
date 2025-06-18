package com.example.demo.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String passwordHash; // Stores the hashed password from the DB
    private String role;         // e.g., "Admin", "Student", "Faculty", "Librarian"
    private LocalDateTime createdAt;

    // Constructor for creating a new user (ID and createdAt handled by DB or later logic)
    public User(String username, String passwordHash, String role) {
        this(0, username, passwordHash, role, null); // userId 0 for new, createdAt null initially
    }

    // Full constructor for retrieving user from the database
    public User(int userId, String username, String passwordHash, String role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- Setters ---
    // userId is typically set by the database on insert, but a setter is useful for DAO to update the object
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
