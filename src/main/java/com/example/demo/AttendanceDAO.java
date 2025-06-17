package com.example.demo;

import com.example.demo.Attendance;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    /**
     * Adds a new attendance record to the database.
     *
     * @param attendance The Attendance object to add. Its attendanceId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO Attendance (student_id, course_id, attendance_date, status, taken_by_faculty_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attendance.getStudentId());
            stmt.setInt(2, attendance.getCourseId());
            stmt.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(4, attendance.getStatus());
            if (attendance.getTakenByFacultyId() != null) {
                stmt.setInt(5, attendance.getTakenByFacultyId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating attendance record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    attendance.setAttendanceId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating attendance record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves an attendance record by its ID.
     *
     * @param attendanceId The ID of the attendance record to retrieve.
     * @return The Attendance object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Attendance getAttendanceById(int attendanceId) throws SQLException {
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, taken_by_faculty_id FROM Attendance WHERE attendance_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("attendance_date").toLocalDate(),
                            rs.getString("status"),
                            (Integer) rs.getObject("taken_by_faculty_id") // Use getObject for nullable int
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all attendance records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Attendance objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<Attendance> getAttendanceByStudentId(int studentId) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, taken_by_faculty_id FROM Attendance WHERE student_id = ? ORDER BY attendance_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("attendance_date").toLocalDate(),
                            rs.getString("status"),
                            (Integer) rs.getObject("taken_by_faculty_id")
                    ));
                }
            }
        }
        return attendanceList;
    }

    /**
     * Retrieves attendance records for a specific course on a given date.
     *
     * @param courseId The ID of the course.
     * @param attendanceDate The date of attendance.
     * @return A list of Attendance objects for the specified course and date.
     * @throws SQLException If a database access error occurs.
     */
    public List<Attendance> getAttendanceByCourseAndDate(int courseId, LocalDate attendanceDate) throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, taken_by_faculty_id FROM Attendance WHERE course_id = ? AND attendance_date = ? ORDER BY student_id ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setDate(2, Date.valueOf(attendanceDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getInt("student_id"),
                            rs.getInt("course_id"),
                            rs.getDate("attendance_date").toLocalDate(),
                            rs.getString("status"),
                            (Integer) rs.getObject("taken_by_faculty_id")
                    ));
                }
            }
        }
        return attendanceList;
    }

    /**
     * Checks if attendance has already been marked for a student on a specific course and date.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @param attendanceDate The date of attendance.
     * @return True if attendance exists, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean hasAttendanceBeenMarked(int studentId, int courseId, LocalDate attendanceDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE student_id = ? AND course_id = ? AND attendance_date = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.setDate(3, Date.valueOf(attendanceDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves all attendance records from the database.
     *
     * @return A list of all Attendance objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, student_id, course_id, attendance_date, status, taken_by_faculty_id FROM Attendance ORDER BY attendance_date DESC, student_id ASC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                attendanceList.add(new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("student_id"),
                        rs.getInt("course_id"),
                        rs.getDate("attendance_date").toLocalDate(),
                        rs.getString("status"),
                        (Integer) rs.getObject("taken_by_faculty_id")
                ));
            }
        }
        return attendanceList;
    }

    /**
     * Updates an existing attendance record.
     *
     * @param attendance The Attendance object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAttendance(Attendance attendance) throws SQLException {
        String sql = "UPDATE Attendance SET student_id=?, course_id=?, attendance_date=?, status=?, taken_by_faculty_id=? WHERE attendance_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendance.getStudentId());
            stmt.setInt(2, attendance.getCourseId());
            stmt.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(4, attendance.getStatus());
            if (attendance.getTakenByFacultyId() != null) {
                stmt.setInt(5, attendance.getTakenByFacultyId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, attendance.getAttendanceId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an attendance record from the database by its ID.
     *
     * @param attendanceId The ID of the attendance record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAttendance(int attendanceId) throws SQLException {
        String sql = "DELETE FROM Attendance WHERE attendance_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendanceId);
            stmt.executeUpdate();
        }
    }
}
