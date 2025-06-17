package com.example.demo;

import com.example.demo.ResultDAO;
import com.example.demo.Course;
import com.example.demo.Result;
import com.example.demo.Student;
import java.sql.SQLException;
import java.util.List;

public class ResultService {
    private ResultDAO resultDAO;
    private StudentService studentService; // To verify student existence
    private CourseService courseService;   // To verify course existence

    public ResultService() {
        this.resultDAO = new ResultDAO();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
    }

    /**
     * Adds a new result record for a student in a specific course and academic year/semester.
     * Performs validation to ensure student and course exist, and that a result for
     * that student/course/academic year combination is unique.
     *
     * @param studentId The ID of the student the result is for.
     * @param courseId The ID of the course the result is for.
     * @param semesterNumber The semester the course was taken.
     * @param academicYear The academic year (e.g., "2023-2024").
     * @param marksObtained The marks obtained by the student (can be null initially).
     * @param grade The grade obtained by the student (can be null initially).
     * @param resultStatus The status of the result ('Pass', 'Fail', 'Incomplete').
     * @return The newly created Result object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Result addNewResult(int studentId, int courseId, int semesterNumber, String academicYear,
                               Integer marksObtained, String grade, String resultStatus)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (semesterNumber <= 0 || semesterNumber > 8) { // Assuming up to 8 semesters
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        if (marksObtained != null && (marksObtained < 0 || marksObtained > 100)) { // Example range
            throw new IllegalArgumentException("Marks obtained must be between 0 and 100 (inclusive) or null.");
        }
        if (resultStatus == null || resultStatus.trim().isEmpty() || !isValidResultStatus(resultStatus)) {
            throw new IllegalArgumentException("Invalid result status. Must be 'Pass', 'Fail', or 'Incomplete'.");
        }

        String trimmedAcademicYear = academicYear.trim();
        String trimmedGrade = grade != null ? grade.trim() : null;
        String trimmedResultStatus = resultStatus.trim();

        // 2. Business Logic Checks
        // Verify student exists
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        // Verify course exists
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course with ID " + courseId + " does not exist.");
        }

        // Check for duplicate result record for this student in this course and academic year
        if (resultDAO.getResultByStudentCourseAndYear(studentId, courseId, trimmedAcademicYear) != null) {
            throw new IllegalArgumentException("Result for this student in this course for academic year '" + trimmedAcademicYear + "' already exists.");
        }

        // 3. Create Result
        Result newResult = new Result(studentId, courseId, semesterNumber, trimmedAcademicYear, marksObtained, trimmedGrade, trimmedResultStatus);
        resultDAO.addResult(newResult);
        return newResult;
    }

    /**
     * Retrieves a result record by its ID.
     *
     * @param resultId The ID of the result record.
     * @return The Result object if found, null otherwise.
     * @throws IllegalArgumentException If result ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Result getResultById(int resultId) throws IllegalArgumentException, SQLException {
        if (resultId <= 0) {
            throw new IllegalArgumentException("Result ID must be positive.");
        }
        return resultDAO.getResultById(resultId);
    }

    /**
     * Retrieves all result records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Result objects for the given student.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Result> getResultsByStudentId(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        // Optional: Verify student exists
        if (studentService.getStudentById(studentId) == null) {
            System.out.println("Warning: Attempted to get results for non-existent student ID: " + studentId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return resultDAO.getResultsByStudentId(studentId);
    }

    /**
     * Retrieves a specific student's result for a given course in an academic year.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @param academicYear The academic year.
     * @return The Result object if found, null otherwise.
     * @throws IllegalArgumentException If IDs or academic year are invalid.
     * @throws SQLException If a database error occurs.
     */
    public Result getResultByStudentCourseAndYear(int studentId, int courseId, String academicYear) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        return resultDAO.getResultByStudentCourseAndYear(studentId, courseId, academicYear.trim());
    }


    /**
     * Retrieves all result records from the system.
     *
     * @return A list of all Result objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Result> getAllResults() throws SQLException {
        return resultDAO.getAllResults();
    }

    /**
     * Updates an existing result record.
     *
     * @param result The Result object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateResult(Result result) throws IllegalArgumentException, SQLException {
        if (result == null || result.getResultId() <= 0) {
            throw new IllegalArgumentException("Result record and a valid ID are required for update.");
        }
        if (result.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (result.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (result.getSemesterNumber() <= 0 || result.getSemesterNumber() > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (result.getAcademicYear() == null || result.getAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        if (result.getMarksObtained() != null && (result.getMarksObtained() < 0 || result.getMarksObtained() > 100)) {
            throw new IllegalArgumentException("Marks obtained must be between 0 and 100 (inclusive) or null.");
        }
        if (result.getResultStatus() == null || result.getResultStatus().trim().isEmpty() || !isValidResultStatus(result.getResultStatus())) {
            throw new IllegalArgumentException("Invalid result status. Must be 'Pass', 'Fail', or 'Incomplete'.");
        }

        result.setAcademicYear(result.getAcademicYear().trim());
        result.setGrade(result.getGrade() != null ? result.getGrade().trim() : null);
        result.setResultStatus(result.getResultStatus().trim());

        // Verify associated student and course still exist
        if (studentService.getStudentById(result.getStudentId()) == null) {
            throw new IllegalArgumentException("Associated student does not exist.");
        }
        if (courseService.getCourseById(result.getCourseId()) == null) {
            throw new IllegalArgumentException("Associated course does not exist.");
        }

        resultDAO.updateResult(result);
    }

    /**
     * Deletes a result record from the system.
     *
     * @param resultId The ID of the result record to delete.
     * @throws IllegalArgumentException If result ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteResult(int resultId) throws IllegalArgumentException, SQLException {
        if (resultId <= 0) {
            throw new IllegalArgumentException("Result ID must be positive for deletion.");
        }
        resultDAO.deleteResult(resultId);
    }

    /**
     * Helper method to validate if a given string is a recognized result status.
     */
    private boolean isValidResultStatus(String status) {
        return "Pass".equalsIgnoreCase(status) ||
                "Fail".equalsIgnoreCase(status) ||
                "Incomplete".equalsIgnoreCase(status);
    }
}
