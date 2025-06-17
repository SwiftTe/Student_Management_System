package com.example.demo;

import com.example.demo.CourseDAO;
import com.example.demo.Course;
import com.example.demo.Program;
import java.sql.SQLException;
import java.util.List;

public class CourseService {
    private CourseDAO courseDAO;
    private ProgramService programService; // To verify program existence

    public CourseService() {
        this.courseDAO = new CourseDAO();
        this.programService = new ProgramService();
    }

    /**
     * Adds a new course to the system.
     * Performs validation to ensure the course name and code are not empty,
     * credits are positive, and the combination of program, semester, and course code is unique.
     *
     * @param programId The ID of the program this course belongs to.
     * @param semesterNumber The semester in which the course is offered.
     * @param courseCode The unique code for the course.
     * @param courseName The name of the course.
     * @param credits The credit hours for the course.
     * @param description A brief description of the course.
     * @param department The department offering the course.
     * @return The newly created Course object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public Course addNewCourse(int programId, int semesterNumber, String courseCode,
                               String courseName, int credits, String description, String department)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (programId <= 0) {
            throw new IllegalArgumentException("Program ID must be positive.");
        }
        if (semesterNumber <= 0 || semesterNumber > 8) { // Assuming up to 8 semesters
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be a positive number.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }

        String trimmedCourseCode = courseCode.trim();
        String trimmedCourseName = courseName.trim();
        String trimmedDescription = description != null ? description.trim() : null;
        String trimmedDepartment = department.trim();

        // 2. Business Logic Checks
        // Verify program exists
        Program program = programService.getProgramById(programId);
        if (program == null) {
            throw new IllegalArgumentException("Selected program does not exist.");
        }

        // Check for duplicate course code within the same program and semester
        if (courseDAO.getCourseByCodeAndProgram(trimmedCourseCode, programId) != null) {
            // Further refinement: check if the existing course is in the SAME semester
            List<Course> existingCoursesInSemester = courseDAO.getCoursesByProgramAndSemester(programId, semesterNumber);
            boolean duplicateInSemester = existingCoursesInSemester.stream()
                    .anyMatch(c -> c.getCourseCode().equalsIgnoreCase(trimmedCourseCode));
            if(duplicateInSemester) {
                throw new IllegalArgumentException("Course with code '" + trimmedCourseCode + "' already exists for Program '" + program.getProgramName() + "' in Semester " + semesterNumber + ".");
            }
        }


        // 3. Create Course
        Course newCourse = new Course(
                programId,
                semesterNumber,
                trimmedCourseCode,
                trimmedCourseName,
                credits,
                trimmedDescription,
                trimmedDepartment
        );

        courseDAO.addCourse(newCourse);
        return newCourse;
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param courseId The ID of the course.
     * @return The Course object, or null if not found.
     * @throws IllegalArgumentException If course ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Course getCourseById(int courseId) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive.");
        }
        return courseDAO.getCourseById(courseId);
    }

    /**
     * Retrieves all courses for a specific program and semester.
     *
     * @param programId The ID of the program.
     * @param semesterNumber The semester number.
     * @return A list of Course objects.
     * @throws IllegalArgumentException If IDs or semester number are invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Course> getCoursesByProgramAndSemester(int programId, int semesterNumber) throws IllegalArgumentException, SQLException {
        if (programId <= 0) {
            throw new IllegalArgumentException("Program ID must be positive.");
        }
        if (semesterNumber <= 0 || semesterNumber > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        return courseDAO.getCoursesByProgramAndSemester(programId, semesterNumber);
    }

    /**
     * Retrieves all courses from the system.
     *
     * @return A list of all Course objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Course> getAllCourses() throws SQLException {
        return courseDAO.getAllCourses();
    }

    /**
     * Updates an existing course's information.
     *
     * @param course The Course object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateCourse(Course course) throws IllegalArgumentException, SQLException {
        if (course == null || course.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course and a valid Course ID are required for update.");
        }
        if (course.getProgramId() <= 0) {
            throw new IllegalArgumentException("Program ID must be positive.");
        }
        if (course.getSemesterNumber() <= 0 || course.getSemesterNumber() > 8) {
            throw new IllegalArgumentException("Semester number must be between 1 and 8.");
        }
        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }
        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException("Credits must be a positive number.");
        }
        if (course.getDepartment() == null || course.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }

        course.setCourseCode(course.getCourseCode().trim());
        course.setCourseName(course.getCourseName().trim());
        course.setDescription(course.getDescription() != null ? course.getDescription().trim() : null);
        course.setDepartment(course.getDepartment().trim());

        // Check for duplicate course code within the same program and semester, excluding the current course
        Course existingCourseWithSameCode = courseDAO.getCourseByCodeAndProgram(course.getCourseCode(), course.getProgramId());
        if (existingCourseWithSameCode != null && existingCourseWithSameCode.getCourseId() != course.getCourseId()) {
            // Further check if the existing course is in the SAME semester
            List<Course> existingCoursesInSemester = courseDAO.getCoursesByProgramAndSemester(course.getProgramId(), course.getSemesterNumber());
            boolean duplicateInSemester = existingCoursesInSemester.stream()
                    .anyMatch(c -> c.getCourseCode().equalsIgnoreCase(course.getCourseCode()) && c.getCourseId() != course.getCourseId());
            if(duplicateInSemester) {
                throw new IllegalArgumentException("Course with code '" + course.getCourseCode() + "' already exists for this program and semester.");
            }
        }

        // Verify program exists
        Program program = programService.getProgramById(course.getProgramId());
        if (program == null) {
            throw new IllegalArgumentException("Selected program does not exist.");
        }

        courseDAO.updateCourse(course);
    }

    /**
     * Deletes a course from the system.
     *
     * @param courseId The ID of the course to delete.
     * @throws IllegalArgumentException If course ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteCourse(int courseId) throws IllegalArgumentException, SQLException {
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive for deletion.");
        }
        // Business rule: Consider if courses with existing enrollments, assignments, or routines should be deleted.
        // Currently, MySQL's ON DELETE CASCADE will handle child records in Enrollments, Assignments, Routines.
        courseDAO.deleteCourse(courseId);
    }
}

