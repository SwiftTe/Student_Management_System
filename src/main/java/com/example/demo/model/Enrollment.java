package com.example.demo.model;

import java.time.LocalDate;

public class Enrollment {
    private int enrollmentId;
    private int studentId; // Foreign key to Students table
    private int courseId;  // Foreign key to Courses table
    private LocalDate enrollmentDate;
    private String grade; // Can be null initially, e.g., "A+", "B", "P", "F"

    // Constructor for creating a new Enrollment (ID handled by DB)
    public Enrollment(int studentId, int courseId, LocalDate enrollmentDate, String grade) {
        this(0, studentId, courseId, enrollmentDate, grade);
    }

    // Full constructor for retrieving Enrollment from the database
    public Enrollment(int enrollmentId, int studentId, int courseId, LocalDate enrollmentDate, String grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
        this.grade = grade;
    }

    // --- Getters ---
    public int getEnrollmentId() { return enrollmentId; }
    public int getStudentId() { return studentId; }
    public int getCourseId() { return courseId; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public String getGrade() { return grade; }

    // --- Setters ---
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public void setGrade(String grade) { this.grade = grade; }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", enrollmentDate=" + enrollmentDate +
                ", grade='" + grade + '\'' +
                '}';
    }
}
