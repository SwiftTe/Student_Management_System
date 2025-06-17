package com.example.demo;

import com.example.demo.EnrollmentDAO;
import com.example.demo.Course;
import com.example.demo.Enrollment;
import com.example.demo.Student;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EnrollmentService {
    private EnrollmentDAO enrollmentDAO;
    private StudentService studentService; // To verify student existence
    private CourseService courseService;   // To verify course existence

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
    }

    /**
     * Enrolls a student in a specific course.
     * Performs validation to ensure the student and course exist and that
     * the student is not already enrolled in the course.
     *
     * @param studentId The ID of the student to enroll.
     * @param courseId The ID of the course to enroll in.
     * @param enrollmentDate The date of enrollment.
     * @param grade The initial grade (can be null).
     * @return The newly created Enrollment object.
     * @throws IllegalArgumentException If validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Enrollment enrollStudentInCourse(int studentId, int courseId, LocalDate enrollmentDate, String grade)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (enrollmentDate == null || enrollmentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Enrollment Date cannot be in the future.");
        }
        // Grade validation (optional, depends on accepted formats/values)

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

        // Check if student is already enrolled in this course
        if (enrollmentDAO.isStudentEnrolledInCourse(studentId, courseId)) {
            throw new IllegalArgumentException("Student is already enrolled in this course.");
        }

        // 3. Create Enrollment
        Enrollment newEnrollment = new Enrollment(studentId, courseId, enrollmentDate, grade);
        enrollmentDAO.addEnrollment(newEnrollment);
        return newEnrollment;
    }

    /**
     * Retrieves an enrollment record by its ID.
     *
     * @param enrollmentId The ID of the enrollment.
     * @return The Enrollment object, or null if not found.
     * @throws IllegalArgumentException If enrollment ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Enrollment getEnrollmentById(int enrollmentId) throws IllegalArgumentException, SQLException {
        if (enrollmentId <= 0) {
            throw new IllegalArgumentException("Enrollment ID must be positive.");
        }
        return enrollmentDAO.getEnrollmentById(enrollmentId);
    }

    /**
     * Retrieves all enrollment records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Enrollment objects for the given student.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Enrollment> getEnrollmentsByStudentId(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        // Verify student exists (optional, but good for robust error handling)
        if (studentService.getStudentById(studentId) == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }
        return enrollmentDAO.getEnrollmentsByStudentId(studentId);
    }

    /**
     * Retrieves all enrollment records for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Enrollment objects for the given course.
     * @throws IllegalArgumentException If course ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Enrollment> getEnrollmentsByCourseId(int courseId) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        // Verify course exists (optional)
        if (courseService.getCourseById(courseId) == null) {
            throw new IllegalArgumentException("Course with ID " + courseId + " does not exist.");
        }
        return enrollmentDAO.getEnrollmentsByCourseId(courseId);
    }

    /**
     * Retrieves all enrollment records from the system.
     *
     * @return A list of all Enrollment objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Enrollment> getAllEnrollments() throws SQLException {
        return enrollmentDAO.getAllEnrollments();
    }

    /**
     * Updates an existing enrollment record's grade or enrollment date.
     *
     * @param enrollment The Enrollment object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateEnrollment(Enrollment enrollment) throws IllegalArgumentException, SQLException {
        if (enrollment == null || enrollment.getEnrollmentId() <= 0) {
            throw new IllegalArgumentException("Enrollment and a valid Enrollment ID are required for update.");
        }
        if (enrollment.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (enrollment.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (enrollment.getEnrollmentDate() == null || enrollment.getEnrollmentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Enrollment Date cannot be in the future.");
        }

        // Verify student and course still exist (important if IDs are changed or records deleted externally)
        if (studentService.getStudentById(enrollment.getStudentId()) == null) {
            throw new IllegalArgumentException("Student with ID " + enrollment.getStudentId() + " does not exist.");
        }
        if (courseService.getCourseById(enrollment.getCourseId()) == null) {
            throw new IllegalArgumentException("Course with ID " + enrollment.getCourseId() + " does not exist.");
        }

        enrollmentDAO.updateEnrollment(enrollment);
    }

    /**
     * Deletes an enrollment record from the system.
     *
     * @param enrollmentId The ID of the enrollment to delete.
     * @throws IllegalArgumentException If enrollment ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteEnrollment(int enrollmentId) throws IllegalArgumentException, SQLException {
        if (enrollmentId <= 0) {
            throw new IllegalArgumentException("Enrollment ID must be positive for deletion.");
        }
        enrollmentDAO.deleteEnrollment(enrollmentId);
    }
}
