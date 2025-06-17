package com.example.demo;

public class Course {
    private int courseId;
    private int programId; // Foreign key to Programs table
    private int semesterNumber;
    private String courseCode;
    private String courseName;
    private int credits;
    private String description;
    private String department;

    // Constructor for creating a new Course (ID handled by DB)
    public Course(int programId, int semesterNumber, String courseCode, String courseName,
                  int credits, String description, String department) {
        this(0, programId, semesterNumber, courseCode, courseName, credits, description, department);
    }

    // Full constructor for retrieving Course from the database
    public Course(int courseId, int programId, int semesterNumber, String courseCode,
                  String courseName, int credits, String description, String department) {
        this.courseId = courseId;
        this.programId = programId;
        this.semesterNumber = semesterNumber;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.description = description;
        this.department = department;
    }

    // --- Getters ---
    public int getCourseId() { return courseId; }
    public int getProgramId() { return programId; }
    public int getSemesterNumber() { return semesterNumber; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getCredits() { return credits; }
    public String getDescription() { return description; }
    public String getDepartment() { return department; }

    // --- Setters ---
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setProgramId(int programId) { this.programId = programId; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setDescription(String description) { this.description = description; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", programId=" + programId +
                ", semesterNumber=" + semesterNumber +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                '}';
    }
}