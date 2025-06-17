package com.example.demo;

public class Librarian {
    private int librarianId;
    private int userId; // Foreign key to Users table for login credentials
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Constructor for creating a new librarian (ID handled by DB, user_id also typically from DB after user creation)
    public Librarian(int userId, String firstName, String lastName, String email, String phoneNumber) {
        this(0, userId, firstName, lastName, email, phoneNumber);
    }

    // Full constructor for retrieving librarian from the database
    public Librarian(int librarianId, int userId, String firstName, String lastName, String email, String phoneNumber) {
        this.librarianId = librarianId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // --- Getters ---
    public int getLibrarianId() { return librarianId; }
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }

    // --- Setters ---
    public void setLibrarianId(int librarianId) { this.librarianId = librarianId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "Librarian{" +
                "librarianId=" + librarianId +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
