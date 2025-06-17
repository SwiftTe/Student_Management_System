package com.example.demo;

import java.time.LocalTime;

public class Routine {
    private int routineId;
    private int courseId;   // Foreign key to Courses table
    private Integer facultyId; // Nullable: Foreign key to Faculty table (who teaches this session)
    private String routineType; // ENUM in DB: 'Class', 'Exam'
    private String dayOfWeek;  // e.g., 'Monday', 'Tuesday'
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomLocation;
    private String academicYear; // e.g., '2023-2024'
    private int semesterNumber;

    // Constructor for creating a new Routine record (ID handled by DB, facultyId nullable)
    public Routine(int courseId, Integer facultyId, String routineType, String dayOfWeek,
                   LocalTime startTime, LocalTime endTime, String roomLocation,
                   String academicYear, int semesterNumber) {
        this(0, courseId, facultyId, routineType, dayOfWeek, startTime, endTime,
                roomLocation, academicYear, semesterNumber);
    }

    // Full constructor for retrieving Routine from the database
    public Routine(int routineId, int courseId, Integer facultyId, String routineType,
                   String dayOfWeek, LocalTime startTime, LocalTime endTime,
                   String roomLocation, String academicYear, int semesterNumber) {
        this.routineId = routineId;
        this.courseId = courseId;
        this.facultyId = facultyId;
        this.routineType = routineType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomLocation = roomLocation;
        this.academicYear = academicYear;
        this.semesterNumber = semesterNumber;
    }

    // --- Getters ---
    public int getRoutineId() { return routineId; }
    public int getCourseId() { return courseId; }
    public Integer getFacultyId() { return facultyId; }
    public String getRoutineType() { return routineType; }
    public String getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoomLocation() { return roomLocation; }
    public String getAcademicYear() { return academicYear; }
    public int getSemesterNumber() { return semesterNumber; }

    // --- Setters ---
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }
    public void setRoutineType(String routineType) { this.routineType = routineType; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setRoomLocation(String roomLocation) { this.roomLocation = roomLocation; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }

    @Override
    public String toString() {
        return "Routine{" +
                "routineId=" + routineId +
                ", courseId=" + courseId +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", roomLocation='" + roomLocation + '\'' +
                '}';
    }
}
