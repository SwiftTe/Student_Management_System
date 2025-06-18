package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Assignment {
    private int assignmentId;
    private int courseId;   // Foreign key to Courses table
    private int facultyId;  // Foreign key to Faculty table (who created the assignment)
    private String title;
    private String description;
    private LocalDate dueDate;
    private int maxMarks;
    private LocalDateTime createdAt;

    // Constructor for creating a new Assignment (ID and createdAt handled by DB)
    public Assignment(int courseId, int facultyId, String title, String description,
                      LocalDate dueDate, int maxMarks) {
        this(0, courseId, facultyId, title, description, dueDate, maxMarks, null);
    }

    // Full constructor for retrieving Assignment from the database
    public Assignment(int assignmentId, int courseId, int facultyId, String title,
                      String description, LocalDate dueDate, int maxMarks, LocalDateTime createdAt) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.facultyId = facultyId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public int getAssignmentId() { return assignmentId; }
    public int getCourseId() { return courseId; }
    public int getFacultyId() { return facultyId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public int getMaxMarks() { return maxMarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignmentId +
                ", courseId=" + courseId +
                ", facultyId=" + facultyId +
                ", title='" + title + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}
