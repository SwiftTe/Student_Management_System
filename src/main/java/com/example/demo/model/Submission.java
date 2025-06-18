package com.example.demo.model;

import java.time.LocalDateTime;

public class Submission {
    private int submissionId;
    private int assignmentId; // Foreign key to Assignments table
    private int studentId;    // Foreign key to Students table
    private LocalDateTime submissionDate;
    private String filePath;  // Path to the submitted file (e.g., local or cloud storage URL)
    private Integer marksObtained; // Nullable, as marks might not be assigned immediately
    private String feedback;    // Nullable, for faculty feedback

    // Constructor for creating a new Submission (ID and submissionDate handled by DB)
    public Submission(int assignmentId, int studentId, String filePath) {
        this(0, assignmentId, studentId, null, filePath, null, null);
    }

    // Full constructor for retrieving Submission from the database
    public Submission(int submissionId, int assignmentId, int studentId,
                      LocalDateTime submissionDate, String filePath, Integer marksObtained, String feedback) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionDate = submissionDate;
        this.filePath = filePath;
        this.marksObtained = marksObtained;
        this.feedback = feedback;
    }

    // --- Getters ---
    public int getSubmissionId() { return submissionId; }
    public int getAssignmentId() { return assignmentId; }
    public int getStudentId() { return studentId; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public String getFilePath() { return filePath; }
    public Integer getMarksObtained() { return marksObtained; }
    public String getFeedback() { return feedback; }

    // --- Setters ---
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    @Override
    public String toString() {
        return "Submission{" +
                "submissionId=" + submissionId +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", submissionDate=" + submissionDate +
                ", marksObtained=" + marksObtained +
                '}';
    }
}
