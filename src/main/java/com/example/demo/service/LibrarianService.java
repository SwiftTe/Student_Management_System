package com.example.demo.service;

import com.example.demo.dao.LibrarianDAO;
import com.example.demo.model.Librarian;
import com.example.demo.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class LibrarianService {
    private LibrarianDAO librarianDAO;
    private UserService userService; // To get user details for uniqueness checks

    public LibrarianService() {
        this.librarianDAO = new LibrarianDAO();
        this.userService = new UserService();
    }

    /**
     * Adds a new librarian to the system. This method performs comprehensive validation
     * and orchestrates the creation of both a User account and the Librarian record.
     *
     * @param firstName Librarian's first name.
     * @param lastName Librarian's last name.
     * @param email Librarian's email (must be unique).
     * @param phoneNumber Librarian's phone number.
     * @param username The desired username for the librarian's login.
     * @param password The password for the librarian's login (will be stored securely in real app).
     * @return The newly created Librarian object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws SQLException If a database access error occurs during user or librarian creation.
     */
    public Librarian addNewLibrarian(String firstName, String lastName, String email,
                                     String phoneNumber, String username, String password)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty for librarian login.");
        }
        if (password == null || password.trim().isEmpty() || password.length() < 6) { // Basic password length check
            throw new IllegalArgumentException("Password cannot be empty and must be at least 6 characters.");
        }

        // 2. Business Logic Checks
        // Check if email is already used by any user (student, faculty, librarian, admin)
        User existingUserByEmail = userService.getUserByUsername(email); // Often email is used as username or unique identifier
        if (existingUserByEmail != null) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered as a user.");
        }
        // Check if the username is already taken by any user
        if (userService.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        // 3. Create Librarian and User via DAO (Transactional in DAO layer)
        Librarian newLibrarian = new Librarian(
                0, // userId will be set by DAO
                firstName.trim(),
                lastName.trim(),
                email.trim(),
                phoneNumber != null ? phoneNumber.trim() : null // Phone can be null
        );

        librarianDAO.addLibrarianWithUser(newLibrarian, username.trim(), password.trim());
        return newLibrarian;
    }

    /**
     * Retrieves a librarian by their ID.
     *
     * @param librarianId The ID of the librarian.
     * @return The Librarian object, or null if not found.
     * @throws IllegalArgumentException If librarian ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Librarian getLibrarianById(int librarianId) throws IllegalArgumentException, SQLException {
        if (librarianId <= 0) {
            throw new IllegalArgumentException("Librarian ID must be positive.");
        }
        return librarianDAO.getLibrarianById(librarianId);
    }

    /**
     * Retrieves a librarian by their associated user ID.
     * Useful after authentication to get librarian-specific details.
     *
     * @param userId The ID of the user.
     * @return The Librarian object, or null if not found.
     * @throws IllegalArgumentException If user ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Librarian getLibrarianByUserId(int userId) throws IllegalArgumentException, SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return librarianDAO.getLibrarianByUserId(userId);
    }

    /**
     * Retrieves all librarian members from the system.
     *
     * @return A list of all Librarian objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Librarian> getAllLibrarians() throws SQLException {
        return librarianDAO.getAllLibrarians();
    }

    /**
     * Updates an existing librarian's information.
     *
     * @param librarian The Librarian object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateLibrarian(Librarian librarian) throws IllegalArgumentException, SQLException {
        if (librarian == null || librarian.getLibrarianId() <= 0) {
            throw new IllegalArgumentException("Librarian and a valid Librarian ID are required for update.");
        }
        if (librarian.getFirstName() == null || librarian.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (librarian.getLastName() == null || librarian.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        if (librarian.getEmail() == null || !isValidEmail(librarian.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        // Check if email is already used by another user (excluding this librarian's own user account)
        User existingUserWithSameEmail = userService.getUserByUsername(librarian.getEmail().trim());
        if (existingUserWithSameEmail != null && existingUserWithSameEmail.getUserId() != librarian.getUserId()) {
            throw new IllegalArgumentException("Email '" + librarian.getEmail() + "' is already used by another user.");
        }

        librarianDAO.updateLibrarian(librarian);
    }

    /**
     * Deletes a librarian from the system. This also triggers the deletion of their
     * associated user account through the DAO's transactional logic.
     *
     * @param librarianId The ID of the librarian to delete.
     * @throws IllegalArgumentException If librarian ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteLibrarian(int librarianId) throws IllegalArgumentException, SQLException {
        if (librarianId <= 0) {
            throw new IllegalArgumentException("Librarian ID must be positive for deletion.");
        }
        librarianDAO.deleteLibrarian(librarianId); // This method handles cascading deletion of user
    }

    /**
     * Helper method for basic email format validation.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }
}
