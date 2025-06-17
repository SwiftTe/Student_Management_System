package com.example.demo;

import java.time.LocalDate;

public class Attendance {
    private int attendanceId;
    private int studentId; // Foreign key to Students table
    private int courseId;  // Foreign key to Courses table
    private LocalDate attendanceDate;
    private String status; // ENUM in DB: 'Present', 'Absent', 'Late', 'Excused'
    private Integer takenByFacultyId; // Nullable: Foreign key to Faculty table, who marked attendance

    // Constructor for creating a new Attendance record (ID handled by DB)
    public Attendance(int studentId, int courseId, LocalDate attendanceDate, String status, Integer takenByFacultyId) {
        this(0, studentId, courseId, attendanceDate, status, takenByFacultyId);
    }

    // Full constructor for retrieving Attendance from the database
    public Attendance(int attendanceId, int studentId, int courseId,
                      LocalDate attendanceDate, String status, Integer takenByFacultyId) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.takenByFacultyId = takenByFacultyId;
    }

    // --- Getters ---
    public int getAttendanceId() { return attendanceId; }
    public int getStudentId() { return studentId; }
    public int getCourseId() { return courseId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String getStatus() { return status; }
    public Integer getTakenByFacultyId() { return takenByFacultyId; }

    // --- Setters ---
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTakenByFacultyId(Integer takenByFacultyId) { this.takenByFacultyId = takenByFacultyId; }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", attendanceDate=" + attendanceDate +
                ", status='" + status + '\'' +
                ", takenByFacultyId=" + takenByFacultyId +
                '}';
    }
}