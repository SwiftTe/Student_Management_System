package com.example.demo.service;

import com.example.demo.dao.StudentDAO;
import com.example.demo.dao.UserDAO; // Though StudentDAO uses it internally, sometimes useful for direct checks
import com.example.demo.model.Program;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class StudentService {
    private StudentDAO studentDAO;
    private ProgramService programService; // To retrieve program details
    private UserService userService; // To get user details if needed, e.g., for email uniqueness check

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.programService = new ProgramService();
        this.userService = new UserService();
    }

    /**
     * Adds a new student to the system. This method performs comprehensive validation
     * and orchestrates the creation of both a User account and the Student record.
     *
     * @param firstName Student's first name.
     * @param lastName Student's last name.
     * @param dateOfBirth Student's date of birth.
     * @param gender Student's gender.
     * @param email Student's email (must be unique).
     * @param phoneNumber Student's phone number.
     * @param address Student's address.
     * @param enrollmentDate Student's enrollment date.
     * @param major Student's major (can be derived from program).
     * @param programId The ID of the program the student is enrolling in.
     * @param username The desired username for the student's login.
     * @param password The password for the student's login (will be stored securely in real app).
     * @return The newly created Student object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws SQLException If a database access error occurs during user or student creation.
     */
    public Student addNewStudent(String firstName, String lastName, LocalDate dateOfBirth,
                                 String gender, String email, String phoneNumber, String address,
                                 LocalDate enrollmentDate, String major, int programId,
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
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of Birth cannot be in the future.");
        }
        if (enrollmentDate == null || enrollmentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Enrollment Date cannot be in the future.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty for student login.");
        }
        if (password == null || password.trim().isEmpty() || password.length() < 6) { // Basic password length check
            throw new IllegalArgumentException("Password cannot be empty and must be at least 6 characters.");
        }

        // 2. Business Logic Checks
        // Check if email is already used by any user (student, faculty, librarian, admin)
        User existingUserByEmail = userService.getUserByUsername(email); // Using email as username for simplicity often
        if (existingUserByEmail != null) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered as a user.");
        }
        // Check if the username is already taken by any user
        if (userService.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        // Verify program exists
        Program program = programService.getProgramById(programId);
        if (program == null) {
            throw new IllegalArgumentException("Selected program does not exist.");
        }

        // 3. Create Student and User via DAO (Transactional in DAO layer)
        Student newStudent = new Student(
                0, // userId will be set by DAO
                programId,
                firstName.trim(),
                lastName.trim(),
                dateOfBirth,
                gender,
                email.trim(),
                phoneNumber.trim(),
                address.trim(),
                enrollmentDate,
                major.trim()
        );

        studentDAO.addStudentWithUser(newStudent, username.trim(), password.trim());
        return newStudent;
    }

    /**
     * Retrieves a student by their ID.
     *
     * @param studentId The ID of the student.
     * @return The Student object, or null if not found.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Student getStudentById(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        return studentDAO.getStudentById(studentId);
    }

    /**
     * Retrieves a student by their associated user ID.
     * Useful after authentication to get student-specific details.
     *
     * @param userId The ID of the user.
     * @return The Student object, or null if not found.
     * @throws IllegalArgumentException If user ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Student getStudentByUserId(int userId) throws IllegalArgumentException, SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        return studentDAO.getStudentByUserId(userId);
    }

    /**
     * Retrieves all students from the system.
     *
     * @return A list of all Student objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.getAllStudents();
    }

    /**
     * Updates an existing student's information.
     *
     * @param student The Student object with updated details.
     * @param newProgramId The potentially new program ID.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateStudent(Student student, int newProgramId) throws IllegalArgumentException, SQLException {
        if (student == null || student.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student and a valid Student ID are required for update.");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        if (student.getEmail() == null || !isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (student.getDateOfBirth() == null || student.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of Birth cannot be in the future.");
        }
        if (student.getEnrollmentDate() == null || student.getEnrollmentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Enrollment Date cannot be in the future.");
        }

        // Check if email is already used by another user (excluding this student's own user account)
        User existingUserWithSameEmail = userService.getUserByUsername(student.getEmail().trim());
        if (existingUserWithSameEmail != null && existingUserWithSameEmail.getUserId() != student.getUserId()) {
            throw new IllegalArgumentException("Email '" + student.getEmail() + "' is already used by another user.");
        }

        // Verify new program exists
        Program program = programService.getProgramById(newProgramId);
        if (program == null) {
            throw new IllegalArgumentException("Selected program does not exist.");
        }
        student.setProgramId(newProgramId); // Set the updated program ID

        studentDAO.updateStudent(student);
    }

    /**
     * Deletes a student from the system. This also triggers the deletion of their
     * associated user account through the DAO's transactional logic.
     *
     * @param studentId The ID of the student to delete.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteStudent(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive for deletion.");
        }
        studentDAO.deleteStudent(studentId); // This method handles cascading deletion of user
    }

    /**
     * Helper method for basic email format validation.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    /**
     * ✅ FIXED: Expose the userService inside the class (not outside)
     */
    public UserService getUserService() {
        return userService;
    }
}
