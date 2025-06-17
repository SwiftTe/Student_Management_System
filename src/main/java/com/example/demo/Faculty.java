package com.example.demo;

public class Faculty {
    private int facultyId;
    private int userId; // Foreign key to Users table for login credentials
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;

    // Constructor for creating a new faculty (ID handled by DB, user_id also typically from DB after user creation)
    public Faculty(int userId, String firstName, String lastName, String email, String phoneNumber, String department) {
        this(0, userId, firstName, lastName, email, phoneNumber, department);
    }

    // Full constructor for retrieving faculty from the database
    public Faculty(int facultyId, int userId, String firstName, String lastName, String email, String phoneNumber, String department) {
        this.facultyId = facultyId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }

    // --- Getters ---
    public int getFacultyId() { return facultyId; }
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDepartment() { return department; }

    // --- Setters ---
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "Faculty{" +
                "facultyId=" + facultyId +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
