package com.example.demo;

import com.example.demo.SubmissionDAO;
import com.example.demo.Assignment;
import com.example.demo.Submission;
import com.example.demo.Student;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SubmissionService {
    private SubmissionDAO submissionDAO;
    private AssignmentService assignmentService; // To verify assignment existence
    private StudentService studentService;     // To verify student existence

    public SubmissionService() {
        this.submissionDAO = new SubmissionDAO();
        this.assignmentService = new AssignmentService();
        this.studentService = new StudentService();
    }

    /**
     * Adds a new student submission for an assignment.
     * Performs validation to ensure the assignment and student exist, and that
     * a student hasn't already submitted for this assignment (unique submission per student per assignment).
     *
     * @param assignmentId The ID of the assignment the submission is for.
     * @param studentId The ID of the student submitting.
     * @param filePath The path or identifier for the submitted file/content.
     * @return The newly created Submission object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Submission addNewSubmission(int assignmentId, int studentId, String filePath)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (assignmentId <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path for submission cannot be empty.");
        }

        String trimmedFilePath = filePath.trim();

        // 2. Business Logic Checks
        // Verify assignment exists
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with ID " + assignmentId + " does not exist.");
        }

        // Verify student exists
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        // Check if student has already submitted for this assignment
        if (submissionDAO.getSubmissionByAssignmentAndStudent(assignmentId, studentId) != null) {
            throw new IllegalArgumentException("Student has already submitted for this assignment.");
        }

        // 3. Create Submission
        Submission newSubmission = new Submission(assignmentId, studentId, trimmedFilePath);
        submissionDAO.addSubmission(newSubmission);
        return newSubmission;
    }

    /**
     * Retrieves a submission by its ID.
     *
     * @param submissionId The ID of the submission.
     * @return The Submission object, or null if not found.
     * @throws IllegalArgumentException If submission ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Submission getSubmissionById(int submissionId) throws IllegalArgumentException, SQLException {
        if (submissionId <= 0) {
            throw new IllegalArgumentException("Submission ID must be positive.");
        }
        return submissionDAO.getSubmissionById(submissionId);
    }

    /**
     * Retrieves all submissions for a specific assignment.
     *
     * @param assignmentId The ID of the assignment.
     * @return A list of Submission objects for the given assignment.
     * @throws IllegalArgumentException If assignment ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Submission> getSubmissionsByAssignmentId(int assignmentId) throws IllegalArgumentException, SQLException {
        if (assignmentId <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        // Optional: Verify assignment exists
        if (assignmentService.getAssignmentById(assignmentId) == null) {
            System.out.println("Warning: Attempted to get submissions for non-existent assignment ID: " + assignmentId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return submissionDAO.getSubmissionsByAssignmentId(assignmentId);
    }

    /**
     * Retrieves a specific student's submission for a given assignment.
     *
     * @param assignmentId The ID of the assignment.
     * @param studentId The ID of the student.
     * @return The Submission object if found, null otherwise.
     * @throws IllegalArgumentException If IDs are invalid.
     * @throws SQLException If a database error occurs.
     */
    public Submission getSubmissionByAssignmentAndStudent(int assignmentId, int studentId) throws IllegalArgumentException, SQLException {
        if (assignmentId <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        return submissionDAO.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
    }

    /**
     * Updates an existing submission, typically used for grading or adding feedback.
     *
     * @param submission The Submission object with updated details (e.g., marksObtained, feedback, new filePath).
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateSubmission(Submission submission) throws IllegalArgumentException, SQLException {
        if (submission == null || submission.getSubmissionId() <= 0) {
            throw new IllegalArgumentException("Submission and a valid Submission ID are required for update.");
        }
        if (submission.getAssignmentId() <= 0) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        if (submission.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (submission.getFilePath() == null || submission.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("File path for submission cannot be empty.");
        }
        if (submission.getMarksObtained() != null && (submission.getMarksObtained() < 0 || submission.getMarksObtained() > 100)) { // Example range
            // Optionally, compare against max_marks of the assignment
            Assignment assignment = assignmentService.getAssignmentById(submission.getAssignmentId());
            if (assignment != null && submission.getMarksObtained() > assignment.getMaxMarks()) {
                throw new IllegalArgumentException("Marks obtained (" + submission.getMarksObtained() + ") cannot exceed maximum marks (" + assignment.getMaxMarks() + ").");
            }
            if (assignment == null) {
                System.out.println("Warning: Cannot validate marks against max_marks for assignment ID " + submission.getAssignmentId() + " as assignment was not found.");
            }
        }

        submission.setFilePath(submission.getFilePath().trim());
        submission.setFeedback(submission.getFeedback() != null ? submission.getFeedback().trim() : null);

        // Verify associated assignment and student still exist
        if (assignmentService.getAssignmentById(submission.getAssignmentId()) == null) {
            throw new IllegalArgumentException("Associated assignment does not exist.");
        }
        if (studentService.getStudentById(submission.getStudentId()) == null) {
            throw new IllegalArgumentException("Associated student does not exist.");
        }

        submissionDAO.updateSubmission(submission);
    }

    /**
     * Deletes a submission from the system.
     *
     * @param submissionId The ID of the submission to delete.
     * @throws IllegalArgumentException If submission ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteSubmission(int submissionId) throws IllegalArgumentException, SQLException {
        if (submissionId <= 0) {
            throw new IllegalArgumentException("Submission ID must be positive for deletion.");
        }
        submissionDAO.deleteSubmission(submissionId);
    }
}
