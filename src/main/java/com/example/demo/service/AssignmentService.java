package com.example.demo.service;

import com.example.demo.dao.AssignmentDAO;
import com.example.demo.model.Assignment;
import com.example.demo.model.Course;
import com.example.demo.model.Faculty;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AssignmentService {
    private AssignmentDAO assignmentDAO;
    private CourseService courseService;   // To verify course existence
    private FacultyService facultyService; // To verify faculty existence

    public AssignmentService() {
        this.assignmentDAO = new AssignmentDAO();
        this.courseService = new CourseService();
        this.facultyService = new FacultyService();
    }

    /**
     * Adds a new assignment to the system.
     * Performs validation to ensure title, max marks, due date are valid,
     * and that the associated course and faculty exist.
     *
     * @param courseId The ID of the course the assignment belongs to.
     * @param facultyId The ID of the faculty who created the assignment.
     * @param title The title of the assignment.
     * @param description A brief description of the assignment.
     * @param dueDate The due date for the assignment.
     * @param maxMarks The maximum marks for the assignment.
     * @return The newly created Assignment object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Assignment addNewAssignment(int courseId, int facultyId, String title,
                                       String description, LocalDate dueDate, int maxMarks)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (facultyId <= 0) {
            throw new IllegalArgumentException("Faculty ID must be positive.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment title cannot be empty.");
        }
        if (dueDate == null || dueDate.isBefore(LocalDate.now())) { // Assignment due date cannot be in the past
            throw new IllegalArgumentException("Due Date cannot be in the past.");
        }
        if (maxMarks <= 0) {
            throw new IllegalArgumentException("Max marks must be a positive number.");
        }

        String trimmedTitle = title.trim();
        String trimmedDescription = description != null ? description.trim() : null;

        // 2. Business Logic Checks
        // Verify course exists
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course with ID " + courseId + " does not exist.");
        }

        // Verify faculty exists
        Faculty faculty = facultyService.getFacultyById(facultyId);
        if (faculty == null) {
            throw new IllegalArgumentException("Faculty with ID " + facultyId + " does not exist.");
        }

        // 3. Create Assignment
        Assignment newAssignment = new Assignment(
                courseId,
                facultyId,
                trimmedTitle,
                trimmedDescription,
                dueDate,
                maxMarks
        );

        assignmentDAO.addAssignment(newAssignment);
        return newAssignment;
    }

    /**
     * Retrieves an assignment by its ID.
     *
     * @param assignmentId The ID of the assignment.
     * @return The Assignment object, or null if not found.
     * @throws IllegalArgumentException If assignment ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Assignment getAssignmentById(int assignmentId) throws IllegalArgumentException, SQLException {
        if (assignmentId <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        return assignmentDAO.getAssignmentById(assignmentId);
    }

    /**
     * Retrieves all assignments for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Assignment objects for the given course.
     * @throws IllegalArgumentException If course ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Assignment> getAssignmentsByCourseId(int courseId) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        // Optional: Verify course exists
        if (courseService.getCourseById(courseId) == null) {
            System.out.println("Warning: Attempted to get assignments for non-existent course ID: " + courseId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return assignmentDAO.getAssignmentsByCourseId(courseId);
    }

    /**
     * Retrieves all assignments created by a specific faculty member.
     *
     * @param facultyId The ID of the faculty member.
     * @return A list of Assignment objects created by the given faculty.
     * @throws IllegalArgumentException If faculty ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Assignment> getAssignmentsByFacultyId(int facultyId) throws IllegalArgumentException, SQLException {
        if (facultyId <= 0) {
            throw new IllegalArgumentException("Faculty ID must be positive.");
        }
        // Optional: Verify faculty exists
        if (facultyService.getFacultyById(facultyId) == null) {
            System.out.println("Warning: Attempted to get assignments for non-existent faculty ID: " + facultyId);
            return List.of(); // Return empty list or throw
        }
        return assignmentDAO.getAssignmentsByFacultyId(facultyId);
    }

    /**
     * Retrieves all assignments from the system.
     *
     * @return A list of all Assignment objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Assignment> getAllAssignments() throws SQLException {
        return assignmentDAO.getAllAssignments();
    }

    /**
     * Updates an existing assignment's information.
     *
     * @param assignment The Assignment object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAssignment(Assignment assignment) throws IllegalArgumentException, SQLException {
        if (assignment == null || assignment.getAssignmentId() <= 0) {
            throw new IllegalArgumentException("Assignment and a valid Assignment ID are required for update.");
        }
        if (assignment.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (assignment.getFacultyId() <= 0) {
            throw new IllegalArgumentException("Faculty ID must be positive.");
        }
        if (assignment.getTitle() == null || assignment.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment title cannot be empty.");
        }
        if (assignment.getDueDate() == null || assignment.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due Date cannot be in the past.");
        }
        if (assignment.getMaxMarks() <= 0) {
            throw new IllegalArgumentException("Max marks must be a positive number.");
        }

        assignment.setTitle(assignment.getTitle().trim());
        assignment.setDescription(assignment.getDescription() != null ? assignment.getDescription().trim() : null);

        // Verify associated course and faculty still exist
        if (courseService.getCourseById(assignment.getCourseId()) == null) {
            throw new IllegalArgumentException("Associated course does not exist.");
        }
        if (facultyService.getFacultyById(assignment.getFacultyId()) == null) {
            throw new IllegalArgumentException("Associated faculty does not exist.");
        }

        assignmentDAO.updateAssignment(assignment);
    }

    /**
     * Deletes an assignment from the system.
     *
     * @param assignmentId The ID of the assignment to delete.
     * @throws IllegalArgumentException If assignment ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAssignment(int assignmentId) throws IllegalArgumentException, SQLException {
        if (assignmentId <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive for deletion.");
        }
        // Business rule: Consider effects on existing submissions before deleting assignment.
        // MySQL FK (ON DELETE CASCADE) will delete associated submissions automatically.
        assignmentDAO.deleteAssignment(assignmentId);
    }
}
