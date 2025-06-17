package com.example.demo;

public class Result {
    private int resultId;
    private int studentId; // Foreign key to Students table
    private int courseId;  // Foreign key to Courses table
    private int semesterNumber;
    private String academicYear; // e.g., '2023-2024'
    private Integer marksObtained; // Nullable, as marks might not be assigned immediately
    private String grade; // Nullable, e.g., 'A+', 'B', 'P', 'F'
    private String resultStatus; // ENUM in DB: 'Pass', 'Fail', 'Incomplete'

    // Constructor for creating a new Result record (ID handled by DB, marks/grade/status nullable)
    public Result(int studentId, int courseId, int semesterNumber, String academicYear) {
        this(0, studentId, courseId, semesterNumber, academicYear, null, null, "Incomplete"); // Default status 'Incomplete'
    }

    // Full constructor for retrieving Result from the database
    public Result(int resultId, int studentId, int courseId, int semesterNumber,
                  String academicYear, Integer marksObtained, String grade, String resultStatus) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.semesterNumber = semesterNumber;
        this.academicYear = academicYear;
        this.marksObtained = marksObtained;
        this.grade = grade;
        this.resultStatus = resultStatus;
    }

    // --- Getters ---
    public int getResultId() { return resultId; }
    public int getStudentId() { return studentId; }
    public int getCourseId() { return courseId; }
    public int getSemesterNumber() { return semesterNumber; }
    public String getAcademicYear() { return academicYear; }
    public Integer getMarksObtained() { return marksObtained; }
    public String getGrade() { return grade; }
    public String getResultStatus() { return resultStatus; }

    // --- Setters ---
    public void setResultId(int resultId) { this.resultId = resultId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setSemesterNumber(int semesterNumber) { this.semesterNumber = semesterNumber; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
    public void setGrade(String grade) { this.grade = grade; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    @Override
    public String toString() {
        return "Result{" +
                "resultId=" + resultId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", academicYear='" + academicYear + '\'' +
                ", grade='" + grade + '\'' +
                ", resultStatus='" + resultStatus + '\'' +
                '}';
    }
}
