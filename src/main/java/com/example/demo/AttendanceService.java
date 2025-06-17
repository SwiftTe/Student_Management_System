package com.example.demo;

import com.example.demo.AttendanceDAO;
import com.example.demo.Attendance;
import com.example.demo.Course;
import com.example.demo.Student;
import com.example.demo.Faculty; // Needed if linking attendance to specific faculty
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AttendanceService {
    private AttendanceDAO attendanceDAO;
    private StudentService studentService;   // To verify student existence
    private CourseService courseService;     // To verify course existence
    private FacultyService facultyService;   // To verify faculty existence (for takenByFacultyId)

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
        this.facultyService = new FacultyService();
    }

    /**
     * Adds a new attendance record for a student in a specific course.
     * Performs validation to ensure student, course exist, and attendance for that
     * student/course/date combination is unique.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @param attendanceDate The date of attendance.
     * @param status The attendance status (e.g., 'Present', 'Absent', 'Late', 'Excused').
     * @param takenByFacultyId The ID of the faculty who marked the attendance (can be null).
     * @return The newly created Attendance object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Attendance addNewAttendance(int studentId, int courseId, LocalDate attendanceDate,
                                       String status, Integer takenByFacultyId)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (attendanceDate == null || attendanceDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance Date cannot be in the future.");
        }
        if (status == null || status.trim().isEmpty() || !isValidAttendanceStatus(status)) {
            throw new IllegalArgumentException("Invalid attendance status. Must be 'Present', 'Absent', 'Late', or 'Excused'.");
        }

        String trimmedStatus = status.trim();

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

        // Verify faculty exists if ID is provided
        if (takenByFacultyId != null) {
            Faculty faculty = facultyService.getFacultyById(takenByFacultyId);
            if (faculty == null) {
                throw new IllegalArgumentException("Faculty with ID " + takenByFacultyId + " (who marked attendance) does not exist.");
            }
        }

        // Check for duplicate attendance record for this student on this course and date
        if (attendanceDAO.hasAttendanceBeenMarked(studentId, courseId, attendanceDate)) {
            throw new IllegalArgumentException("Attendance for this student in this course on this date has already been marked.");
        }

        // 3. Create Attendance
        Attendance newAttendance = new Attendance(studentId, courseId, attendanceDate, trimmedStatus, takenByFacultyId);
        attendanceDAO.addAttendance(newAttendance);
        return newAttendance;
    }

    /**
     * Retrieves an attendance record by its ID.
     *
     * @param attendanceId The ID of the attendance record.
     * @return The Attendance object, or null if not found.
     * @throws IllegalArgumentException If attendance ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Attendance getAttendanceById(int attendanceId) throws IllegalArgumentException, SQLException {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive.");
        }
        return attendanceDAO.getAttendanceById(attendanceId);
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Attendance objects for the given student.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Attendance> getAttendanceByStudentId(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        // Optional: Verify student exists
        if (studentService.getStudentById(studentId) == null) {
            System.out.println("Warning: Attempted to get attendance for non-existent student ID: " + studentId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return attendanceDAO.getAttendanceByStudentId(studentId);
    }

    /**
     * Retrieves attendance records for a specific course on a given date.
     *
     * @param courseId The ID of the course.
     * @param attendanceDate The date of attendance.
     * @return A list of Attendance objects for the specified course and date.
     * @throws IllegalArgumentException If course ID or date are invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Attendance> getAttendanceByCourseAndDate(int courseId, LocalDate attendanceDate) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (attendanceDate == null) {
            throw new IllegalArgumentException("Attendance Date cannot be null.");
        }
        // Optional: Verify course exists
        if (courseService.getCourseById(courseId) == null) {
            System.out.println("Warning: Attempted to get attendance for non-existent course ID: " + courseId);
            return List.of(); // Return empty list or throw
        }
        return attendanceDAO.getAttendanceByCourseAndDate(courseId, attendanceDate);
    }

    /**
     * Retrieves all attendance records from the system.
     *
     * @return A list of all Attendance objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Attendance> getAllAttendance() throws SQLException {
        return attendanceDAO.getAllAttendance();
    }

    /**
     * Updates an existing attendance record.
     *
     * @param attendance The Attendance object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAttendance(Attendance attendance) throws IllegalArgumentException, SQLException {
        if (attendance == null || attendance.getAttendanceId() <= 0) {
            throw new IllegalArgumentException("Attendance record and a valid ID are required for update.");
        }
        if (attendance.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (attendance.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (attendance.getAttendanceDate() == null || attendance.getAttendanceDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance Date cannot be in the future.");
        }
        if (attendance.getStatus() == null || attendance.getStatus().trim().isEmpty() || !isValidAttendanceStatus(attendance.getStatus())) {
            throw new IllegalArgumentException("Invalid attendance status. Must be 'Present', 'Absent', 'Late', or 'Excused'.");
        }

        attendance.setStatus(attendance.getStatus().trim());

        // Verify associated student, course, and faculty (if applicable) still exist
        if (studentService.getStudentById(attendance.getStudentId()) == null) {
            throw new IllegalArgumentException("Associated student does not exist.");
        }
        if (courseService.getCourseById(attendance.getCourseId()) == null) {
            throw new IllegalArgumentException("Associated course does not exist.");
        }
        if (attendance.getTakenByFacultyId() != null && facultyService.getFacultyById(attendance.getTakenByFacultyId()) == null) {
            throw new IllegalArgumentException("Associated faculty (who marked attendance) does not exist.");
        }

        attendanceDAO.updateAttendance(attendance);
    }

    /**
     * Deletes an attendance record from the system.
     *
     * @param attendanceId The ID of the attendance record to delete.
     * @throws IllegalArgumentException If attendance ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAttendance(int attendanceId) throws IllegalArgumentException, SQLException {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive for deletion.");
        }
        attendanceDAO.deleteAttendance(attendanceId);
    }

    /**
     * Helper method to validate if a given string is a recognized attendance status.
     */
    private boolean isValidAttendanceStatus(String status) {
        return "Present".equalsIgnoreCase(status) ||
                "Absent".equalsIgnoreCase(status) ||
                "Late".equalsIgnoreCase(status) ||
                "Excused".equalsIgnoreCase(status);
    }
}
