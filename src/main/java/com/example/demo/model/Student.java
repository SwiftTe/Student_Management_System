package com.example.demo.model;

import java.time.LocalDate;

public class Student {
    private int studentId;
    private int userId; // Foreign key to Users table for login credentials
    private int programId; // Foreign key to Programs table
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate enrollmentDate;
    private String major; // Can be derived from program, but kept for flexibility

    // Constructor for creating a new student (ID handled by DB, user_id also typically from DB after user creation)
    public Student(int userId, int programId, String firstName, String lastName, LocalDate dateOfBirth,
                   String gender, String email, String phoneNumber, String address,
                   LocalDate enrollmentDate, String major) {
        this(0, userId, programId, firstName, lastName, dateOfBirth, gender, email, phoneNumber,
                address, enrollmentDate, major);
    }

    // Full constructor for retrieving student from the database
    public Student(int studentId, int userId, int programId, String firstName, String lastName,
                   LocalDate dateOfBirth, String gender, String email, String phoneNumber,
                   String address, LocalDate enrollmentDate, String major) {
        this.studentId = studentId;
        this.userId = userId;
        this.programId = programId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.enrollmentDate = enrollmentDate;
        this.major = major;
    }

    // --- Getters ---
    public int getStudentId() { return studentId; }
    public int getUserId() { return userId; }
    public int getProgramId() { return programId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public String getMajor() { return major; }

    // --- Setters ---
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setProgramId(int programId) { this.programId = programId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGender(String gender) { this.gender = gender; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public void setMajor(String major) { this.major = major; }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", userId=" + userId +
                ", programId=" + programId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
