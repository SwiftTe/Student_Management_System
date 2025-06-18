package com.example.demo.model;

import java.time.LocalDate;

public class Fees {
    private int feeId;
    private int studentId; // Foreign key to Students table
    private String feeType; // e.g., 'Tuition', 'Exam', 'Library'
    private double amount;
    private LocalDate dueDate;
    private LocalDate paymentDate; // Nullable: Date when the payment was made
    private String status; // ENUM in DB: 'Paid', 'Due', 'Overdue', 'Waived'

    // Constructor for creating a new Fee record (ID, paymentDate, status handled by DB initially)
    public Fees(int studentId, String feeType, double amount, LocalDate dueDate) {
        this(0, studentId, feeType, amount, dueDate, null, "Due"); // Default status 'Due'
    }

    // Full constructor for retrieving Fees from the database
    public Fees(int feeId, int studentId, String feeType, double amount,
                LocalDate dueDate, LocalDate paymentDate, String status) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.feeType = feeType;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    // --- Getters ---
    public int getFeeId() { return feeId; }
    public int getStudentId() { return studentId; }
    public String getFeeType() { return feeType; }
    public double getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getStatus() { return status; }

    // --- Setters ---
    public void setFeeId(int feeId) { this.feeId = feeId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Fees{" +
                "feeId=" + feeId +
                ", studentId=" + studentId +
                ", feeType='" + feeType + '\'' +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                '}';
    }
}
