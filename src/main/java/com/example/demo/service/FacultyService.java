package com.example.demo.service;

import com.example.demo.dao.FacultyDAO;
import com.example.demo.dao.UserDAO; // Though FacultyDAO uses it internally, sometimes useful for direct checks
import com.example.demo.model.Faculty;
import com.example.demo.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class FacultyService {
    private FacultyDAO facultyDAO;
    private UserService userService; // To get user details for uniqueness checks

    public FacultyService() {
        this.facultyDAO = new FacultyDAO();
        this.userService = new UserService();
    }

    /**
     * Adds a new faculty member to the system. This method performs comprehensive validation
     * and orchestrates the creation of both a User account and the Faculty record.
     *
     * @param firstName Faculty's first name.
     * @param lastName Faculty's last name.
     * @param email Faculty's email (must be unique).
     * @param phoneNumber Faculty's phone number.
     * @param department Faculty's department.
     * @param username The desired username for the faculty's login.
     * @param password The password for the faculty's login (will be stored securely in real app).
     * @return The newly created Faculty object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws SQLException If a database access error occurs during user or faculty creation.
     */
    public Faculty addNewFaculty(String firstName, String lastName, String email,
                                 String phoneNumber, String department,
                                 String username, String password)
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
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty for faculty login.");
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

        // 3. Create Faculty and User via DAO (Transactional in DAO layer)
        Faculty newFaculty = new Faculty(
                0, // userId will be set by DAO
                firstName.trim(),
                lastName.trim(),
                email.trim(),
                phoneNumber != null ? phoneNumber.trim() : null, // Phone can be null
                department.trim()
        );

        facultyDAO.addFacultyWithUser(newFaculty, username.trim(), password.trim());
        return newFaculty;
    }

    /**
     * Retrieves a faculty member by their ID.
     *
     * @param facultyId The ID of the faculty member.
     * @return The Faculty object, or null if not found.
     * @throws IllegalArgumentException If faculty ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Faculty getFacultyById(int facultyId) throws IllegalArgumentException, SQLException {
        if (facultyId <= 0) {
            throw new IllegalArgumentException("Faculty ID must be positive.");
        }
        return facultyDAO.getFacultyById(facultyId);
    }

    /**
     * Retrieves a faculty member by their associated user ID.
     * Useful after authentication to get faculty-specific details.
     *
     * @param userId The ID of the user.
     * @return The Faculty object, or null if not found.
     * @throws IllegalArgumentException If user ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Faculty getFacultyByUserId(int userId) throws IllegalArgumentException, SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return facultyDAO.getFacultyByUserId(userId);
    }

    /**
     * Retrieves all faculty members from the system.
     *
     * @return A list of all Faculty objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Faculty> getAllFaculty() throws SQLException {
        return facultyDAO.getAllFaculty();
    }

    /**
     * Updates an existing faculty member's information.
     *
     * @param faculty The Faculty object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateFaculty(Faculty faculty) throws IllegalArgumentException, SQLException {
        if (faculty == null || faculty.getFacultyId() <= 0) {
            throw new IllegalArgumentException("Faculty and a valid Faculty ID are required for update.");
        }
        if (faculty.getFirstName() == null || faculty.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (faculty.getLastName() == null || faculty.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        if (faculty.getEmail() == null || !isValidEmail(faculty.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (faculty.getDepartment() == null || faculty.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }

        // Check if email is already used by another user (excluding this faculty's own user account)
        User existingUserWithSameEmail = userService.getUserByUsername(faculty.getEmail().trim());
        if (existingUserWithSameEmail != null && existingUserWithSameEmail.getUserId() != faculty.getUserId()) {
            throw new IllegalArgumentException("Email '" + faculty.getEmail() + "' is already used by another user.");
        }

        facultyDAO.updateFaculty(faculty);
    }

    /**
     * Deletes a faculty member from the system. This also triggers the deletion of their
     * associated user account through the DAO's transactional logic.
     *
     * @param facultyId The ID of the faculty member to delete.
     * @throws IllegalArgumentException If faculty ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteFaculty(int facultyId) throws IllegalArgumentException, SQLException {
        if (facultyId <= 0) {
            throw new IllegalArgumentException("Faculty ID must be positive for deletion.");
        }
        facultyDAO.deleteFaculty(facultyId); // This method handles cascading deletion of user
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
