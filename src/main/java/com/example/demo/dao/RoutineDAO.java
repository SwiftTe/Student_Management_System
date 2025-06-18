package com.example.demo.dao;

import com.example.demo.model.Routine;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time; // For LocalTime
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RoutineDAO {

    /**
     * Adds a new routine record to the database.
     *
     * @param routine The Routine object to add. Its routineId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addRoutine(Routine routine) throws SQLException {
        String sql = "INSERT INTO Routines (course_id, faculty_id, routine_type, day_of_week, start_time, end_time, room_location, academic_year, semester_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, routine.getCourseId());
            if (routine.getFacultyId() != null) {
                stmt.setInt(2, routine.getFacultyId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, routine.getRoutineType());
            stmt.setString(4, routine.getDayOfWeek());
            stmt.setTime(5, Time.valueOf(routine.getStartTime()));
            stmt.setTime(6, Time.valueOf(routine.getEndTime()));
            stmt.setString(7, routine.getRoomLocation());
            stmt.setString(8, routine.getAcademicYear());
            stmt.setInt(9, routine.getSemesterNumber());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating routine record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    routine.setRoutineId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating routine record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a routine record by its ID.
     *
     * @param routineId The ID of the routine record to retrieve.
     * @return The Routine object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Routine getRoutineById(int routineId) throws SQLException {
        String sql = "SELECT routine_id, course_id, faculty_id, routine_type, day_of_week, start_time, end_time, room_location, academic_year, semester_number FROM Routines WHERE routine_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routineId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Routine(
                            rs.getInt("routine_id"),
                            rs.getInt("course_id"),
                            (Integer) rs.getObject("faculty_id"), // For nullable INT
                            rs.getString("routine_type"),
                            rs.getString("day_of_week"),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("room_location"),
                            rs.getString("academic_year"),
                            rs.getInt("semester_number")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all routine records for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Routine objects for the given course.
     * @throws SQLException If a database access error occurs.
     */
    public List<Routine> getRoutinesByCourseId(int courseId) throws SQLException {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT routine_id, course_id, faculty_id, routine_type, day_of_week, start_time, end_time, room_location, academic_year, semester_number FROM Routines WHERE course_id = ? ORDER BY day_of_week ASC, start_time ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routines.add(new Routine(
                            rs.getInt("routine_id"),
                            rs.getInt("course_id"),
                            (Integer) rs.getObject("faculty_id"),
                            rs.getString("routine_type"),
                            rs.getString("day_of_week"),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("room_location"),
                            rs.getString("academic_year"),
                            rs.getInt("semester_number")
                    ));
                }
            }
        }
        return routines;
    }

    /**
     * Retrieves all class routine records for a specific academic year and semester.
     *
     * @param academicYear The academic year (e.g., "2023-2024").
     * @param semesterNumber The semester number.
     * @return A list of class Routine objects for the given year and semester.
     * @throws SQLException If a database access error occurs.
     */
    public List<Routine> getClassRoutinesByYearAndSemester(String academicYear, int semesterNumber) throws SQLException {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT routine_id, course_id, faculty_id, routine_type, day_of_week, start_time, end_time, room_location, academic_year, semester_number FROM Routines WHERE routine_type = 'Class' AND academic_year = ? AND semester_number = ? ORDER BY day_of_week ASC, start_time ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, academicYear);
            stmt.setInt(2, semesterNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routines.add(new Routine(
                            rs.getInt("routine_id"),
                            rs.getInt("course_id"),
                            (Integer) rs.getObject("faculty_id"),
                            rs.getString("routine_type"),
                            rs.getString("day_of_week"),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("room_location"),
                            rs.getString("academic_year"),
                            rs.getInt("semester_number")
                    ));
                }
            }
        }
        return routines;
    }

    /**
     * Retrieves all routine records from the database.
     *
     * @return A list of all Routine objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Routine> getAllRoutines() throws SQLException {
        List<Routine> routines = new ArrayList<>();
        String sql = "SELECT routine_id, course_id, faculty_id, routine_type, day_of_week, start_time, end_time, room_location, academic_year, semester_number FROM Routines ORDER BY academic_year DESC, semester_number ASC, day_of_week ASC, start_time ASC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                routines.add(new Routine(
                        rs.getInt("routine_id"),
                        rs.getInt("course_id"),
                        (Integer) rs.getObject("faculty_id"),
                        rs.getString("routine_type"),
                        rs.getString("day_of_week"),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("room_location"),
                        rs.getString("academic_year"),
                        rs.getInt("semester_number")
                ));
            }
        }
        return routines;
    }

    /**
     * Updates an existing routine record.
     *
     * @param routine The Routine object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateRoutine(Routine routine) throws SQLException {
        String sql = "UPDATE Routines SET course_id=?, faculty_id=?, routine_type=?, day_of_week=?, start_time=?, end_time=?, room_location=?, academic_year=?, semester_number=? WHERE routine_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routine.getCourseId());
            if (routine.getFacultyId() != null) {
                stmt.setInt(2, routine.getFacultyId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, routine.getRoutineType());
            stmt.setString(4, routine.getDayOfWeek());
            stmt.setTime(5, Time.valueOf(routine.getStartTime()));
            stmt.setTime(6, Time.valueOf(routine.getEndTime()));
            stmt.setString(7, routine.getRoomLocation());
            stmt.setString(8, routine.getAcademicYear());
            stmt.setInt(9, routine.getSemesterNumber());
            stmt.setInt(10, routine.getRoutineId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a routine record from the database by its ID.
     *
     * @param routineId The ID of the routine record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteRoutine(int routineId) throws SQLException {
        String sql = "DELETE FROM Routines WHERE routine_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, routineId);
            stmt.executeUpdate();
        }
    }
}
