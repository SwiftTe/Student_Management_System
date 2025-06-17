package com.example.demo;

import java.time.LocalDate;

public class Borrowing {
    private int borrowingId;
    private int bookId;    // Foreign key to Books table
    private int studentId; // Foreign key to Students table
    private LocalDate borrowDate;
    private LocalDate returnDueDate;
    private LocalDate returnDate; // Nullable: Date when the book was actually returned
    private double fineAmount;  // Fine for overdue books

    // Constructor for creating a new Borrowing record (ID, returnDate, fineAmount handled by DB initially)
    public Borrowing(int bookId, int studentId, LocalDate borrowDate, LocalDate returnDueDate) {
        this(0, bookId, studentId, borrowDate, returnDueDate, null, 0.0);
    }

    // Full constructor for retrieving Borrowing from the database
    public Borrowing(int borrowingId, int bookId, int studentId,
                     LocalDate borrowDate, LocalDate returnDueDate, LocalDate returnDate, double fineAmount) {
        this.borrowingId = borrowingId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.borrowDate = borrowDate;
        this.returnDueDate = returnDueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
    }

    // --- Getters ---
    public int getBorrowingId() { return borrowingId; }
    public int getBookId() { return bookId; }
    public int getStudentId() { return studentId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getReturnDueDate() { return returnDueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFineAmount() { return fineAmount; }

    // --- Setters ---
    public void setBorrowingId(int borrowingId) { this.borrowingId = borrowingId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public void setReturnDueDate(LocalDate returnDueDate) { this.returnDueDate = returnDueDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    @Override
    public String toString() {
        return "Borrowing{" +
                "borrowingId=" + borrowingId +
                ", bookId=" + bookId +
                ", studentId=" + studentId +
                ", borrowDate=" + borrowDate +
                ", returnDueDate=" + returnDueDate +
                '}';
    }
}
