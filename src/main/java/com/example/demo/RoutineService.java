package com.example.demo;

import com.example.demo.RoutineDAO;
import com.example.demo.Course;
import com.example.demo.Faculty;
import com.example.demo.Routine;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class RoutineService {
    private RoutineDAO routineDAO;
    private CourseService courseService;   // To verify course existence
    private FacultyService facultyService; // To verify faculty existence (for assigning classes)

    public RoutineService() {
        this.routineDAO = new RoutineDAO();
        this.courseService = new CourseService();
        this.facultyService = new FacultyService();
    }

    /**
     * Adds a new routine record to the system.
     * Performs validation to ensure associated course and faculty (if applicable) exist,
     * and that time/day are valid.
     *
     * @param courseId The ID of the course for the routine.
     * @param facultyId The ID of the faculty member assigned (can be null for exams or unassigned).
     * @param routineType The type of routine ('Class' or 'Exam').
     * @param dayOfWeek The day of the week (e.g., 'Monday').
     * @param startTime The start time of the routine.
     * @param endTime The end time of the routine.
     * @param roomLocation The room where the routine takes place.
     * @param academicYear The academic year (e.g., "2023-2024").
     * @param semesterNumber The semester number.
     * @return The newly created Routine object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Routine addNewRoutine(int courseId, Integer facultyId, String routineType, String dayOfWeek,
                                 LocalTime startTime, LocalTime endTime, String roomLocation,
                                 String academicYear, int semesterNumber)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (routineType == null || routineType.trim().isEmpty() || !isValidRoutineType(routineType)) {
            throw new IllegalArgumentException("Invalid routine type. Must be 'Class' or 'Exam'.");
        }
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("Day of week cannot be empty.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and End time cannot be null.");
        }
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        if (semesterNumber <= 0 || semesterNumber > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (roomLocation == null || roomLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Room location cannot be empty.");
        }

        String trimmedRoutineType = routineType.trim();
        String trimmedDayOfWeek = dayOfWeek.trim();
        String trimmedRoomLocation = roomLocation.trim();
        String trimmedAcademicYear = academicYear.trim();

        // 2. Business Logic Checks
        // Verify course exists
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course with ID " + courseId + " does not exist.");
        }

        // Verify faculty exists if provided
        if (facultyId != null) {
            Faculty faculty = facultyService.getFacultyById(facultyId);
            if (faculty == null) {
                throw new IllegalArgumentException("Faculty with ID " + facultyId + " does not exist.");
            }
        }

        // 3. Create Routine
        Routine newRoutine = new Routine(
                courseId,
                facultyId,
                trimmedRoutineType,
                trimmedDayOfWeek,
                startTime,
                endTime,
                trimmedRoomLocation,
                trimmedAcademicYear,
                semesterNumber
        );

        routineDAO.addRoutine(newRoutine);
        return newRoutine;
    }

    /**
     * Retrieves a routine record by its ID.
     *
     * @param routineId The ID of the routine record.
     * @return The Routine object if found, null otherwise.
     * @throws IllegalArgumentException If routine ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Routine getRoutineById(int routineId) throws IllegalArgumentException, SQLException {
        if (routineId <= 0) {
            throw new IllegalArgumentException("Routine ID must be positive.");
        }
        return routineDAO.getRoutineById(routineId);
    }

    /**
     * Retrieves all routine records for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Routine objects for the given course.
     * @throws IllegalArgumentException If course ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Routine> getRoutinesByCourseId(int courseId) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        // Optional: Verify course exists
        if (courseService.getCourseById(courseId) == null) {
            System.out.println("Warning: Attempted to get routines for non-existent course ID: " + courseId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return routineDAO.getRoutinesByCourseId(courseId);
    }

    /**
     * Retrieves all class routine records for a specific academic year and semester.
     *
     * @param academicYear The academic year (e.g., "2023-2024").
     * @param semesterNumber The semester number.
     * @return A list of class Routine objects for the given year and semester.
     * @throws IllegalArgumentException If academic year or semester number are invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Routine> getClassRoutinesByYearAndSemester(String academicYear, int semesterNumber) throws IllegalArgumentException, SQLException {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        if (semesterNumber <= 0 || semesterNumber > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        return routineDAO.getClassRoutinesByYearAndSemester(academicYear.trim(), semesterNumber);
    }


    /**
     * Retrieves all routine records from the system.
     *
     * @return A list of all Routine objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Routine> getAllRoutines() throws SQLException {
        return routineDAO.getAllRoutines();
    }

    /**
     * Updates an existing routine record.
     *
     * @param routine The Routine object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateRoutine(Routine routine) throws IllegalArgumentException, SQLException {
        if (routine == null || routine.getRoutineId() <= 0) {
            throw new IllegalArgumentException("Routine record and a valid ID are required for update.");
        }
        if (routine.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        if (routine.getRoutineType() == null || routine.getRoutineType().trim().isEmpty() || !isValidRoutineType(routine.getRoutineType())) {
            throw new IllegalArgumentException("Invalid routine type. Must be 'Class' or 'Exam'.");
        }
        if (routine.getDayOfWeek() == null || routine.getDayOfWeek().trim().isEmpty()) {
            throw new IllegalArgumentException("Day of week cannot be empty.");
        }
        if (routine.getStartTime() == null || routine.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and End time cannot be null.");
        }
        if (routine.getStartTime().isAfter(routine.getEndTime()) || routine.getStartTime().equals(routine.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (routine.getAcademicYear() == null || routine.getAcademicYear().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic Year cannot be empty.");
        }
        if (routine.getSemesterNumber() <= 0 || routine.getSemesterNumber() > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (routine.getRoomLocation() == null || routine.getRoomLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Room location cannot be empty.");
        }

        routine.setRoutineType(routine.getRoutineType().trim());
        routine.setDayOfWeek(routine.getDayOfWeek().trim());
        routine.setRoomLocation(routine.getRoomLocation().trim());
        routine.setAcademicYear(routine.getAcademicYear().trim());

        // Verify associated course and faculty (if applicable) still exist
        if (courseService.getCourseById(routine.getCourseId()) == null) {
            throw new IllegalArgumentException("Associated course does not exist.");
        }
        if (routine.getFacultyId() != null && facultyService.getFacultyById(routine.getFacultyId()) == null) {
            throw new IllegalArgumentException("Associated faculty does not exist.");
        }

        routineDAO.updateRoutine(routine);
    }

    /**
     * Deletes a routine record from the system.
     *
     * @param routineId The ID of the routine record to delete.
     * @throws IllegalArgumentException If routine ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteRoutine(int routineId) throws IllegalArgumentException, SQLException {
        if (routineId <= 0) {
            throw new IllegalArgumentException("Routine ID must be positive for deletion.");
        }
        routineDAO.deleteRoutine(routineId);
    }

    /**
     * Helper method to validate if a given string is a recognized routine type.
     */
    private boolean isValidRoutineType(String type) {
        return "Class".equalsIgnoreCase(type) ||
                "Exam".equalsIgnoreCase(type);
    }
}