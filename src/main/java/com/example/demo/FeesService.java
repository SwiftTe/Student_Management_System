package com.example.demo;

import com.example.demo.FeesDAO;
import com.example.demo.Fees;
import com.example.demo.Student;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FeesService {
    private FeesDAO feesDAO;
    private StudentService studentService; // To verify student existence

    public FeesService() {
        this.feesDAO = new FeesDAO();
        this.studentService = new StudentService();
    }

    /**
     * Adds a new fee record for a student.
     * Performs validation to ensure the student exists, and required fee details are provided.
     *
     * @param studentId The ID of the student the fee is for.
     * @param feeType The type of fee (e.g., 'Tuition', 'Exam', 'Library').
     * @param amount The amount of the fee.
     * @param dueDate The due date for the payment.
     * @return The newly created Fees object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Fees addNewFee(int studentId, String feeType, double amount, LocalDate dueDate)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (feeType == null || feeType.trim().isEmpty()) {
            throw new IllegalArgumentException("Fee type cannot be empty.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Fee amount must be positive.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due Date cannot be null.");
        }

        String trimmedFeeType = feeType.trim();

        // 2. Business Logic Checks
        // Verify student exists
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        // 3. Create Fees record
        Fees newFees = new Fees(studentId, trimmedFeeType, amount, dueDate);
        feesDAO.addFees(newFees);
        return newFees;
    }

    /**
     * Retrieves a fee record by its ID.
     *
     * @param feeId The ID of the fee record.
     * @return The Fees object, or null if not found.
     * @throws IllegalArgumentException If fee ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Fees getFeeById(int feeId) throws IllegalArgumentException, SQLException {
        if (feeId <= 0) {
            throw new IllegalArgumentException("Fee ID must be positive.");
        }
        return feesDAO.getFeesById(feeId);
    }

    /**
     * Retrieves all fee records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Fees objects for the given student.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Fees> getFeesByStudentId(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        // Optional: Verify student exists
        if (studentService.getStudentById(studentId) == null) {
            System.out.println("Warning: Attempted to get fees for non-existent student ID: " + studentId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return feesDAO.getFeesByStudentId(studentId);
    }

    /**
     * Retrieves all fee records from the system.
     *
     * @return A list of all Fees objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Fees> getAllFees() throws SQLException {
        return feesDAO.getAllFees();
    }

    /**
     * Updates an existing fee record. This can be used to mark a fee as paid, update its amount, etc.
     *
     * @param fees The Fees object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateFee(Fees fees) throws IllegalArgumentException, SQLException {
        if (fees == null || fees.getFeeId() <= 0) {
            throw new IllegalArgumentException("Fee record and a valid ID are required for update.");
        }
        if (fees.getStudentId() <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (fees.getFeeType() == null || fees.getFeeType().trim().isEmpty()) {
            throw new IllegalArgumentException("Fee type cannot be empty.");
        }
        if (fees.getAmount() <= 0) {
            throw new IllegalArgumentException("Fee amount must be positive.");
        }
        if (fees.getDueDate() == null) {
            throw new IllegalArgumentException("Due Date cannot be null.");
        }
        if (fees.getStatus() == null || fees.getStatus().trim().isEmpty() || !isValidFeeStatus(fees.getStatus())) {
            throw new IllegalArgumentException("Invalid fee status. Must be 'Paid', 'Due', 'Overdue', or 'Waived'.");
        }

        fees.setFeeType(fees.getFeeType().trim());
        fees.setStatus(fees.getStatus().trim());

        // Verify associated student still exists
        if (studentService.getStudentById(fees.getStudentId()) == null) {
            throw new IllegalArgumentException("Associated student does not exist.");
        }

        feesDAO.updateFees(fees);
    }

    /**
     * Marks a specific fee record as paid, setting the payment date to today.
     *
     * @param feeId The ID of the fee record to mark as paid.
     * @throws IllegalArgumentException If fee ID is invalid or fee is already paid.
     * @throws SQLException If a database access error occurs.
     */
    public void markFeeAsPaid(int feeId) throws IllegalArgumentException, SQLException {
        Fees fee = getFeeById(feeId);
        if (fee == null) {
            throw new IllegalArgumentException("Fee record with ID " + feeId + " not found.");
        }
        if ("Paid".equalsIgnoreCase(fee.getStatus())) {
            throw new IllegalArgumentException("Fee with ID " + feeId + " is already marked as Paid.");
        }

        fee.setPaymentDate(LocalDate.now());
        fee.setStatus("Paid");
        feesDAO.updateFees(fee);
    }

    /**
     * Deletes a fee record from the system.
     *
     * @param feeId The ID of the fee record to delete.
     * @throws IllegalArgumentException If fee ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteFee(int feeId) throws IllegalArgumentException, SQLException {
        if (feeId <= 0) {
            throw new IllegalArgumentException("Fee ID must be positive for deletion.");
        }
        feesDAO.deleteFees(feeId);
    }

    /**
     * Helper method to validate if a given string is a recognized fee status.
     */
    private boolean isValidFeeStatus(String status) {
        return "Paid".equalsIgnoreCase(status) ||
                "Due".equalsIgnoreCase(status) ||
                "Overdue".equalsIgnoreCase(status) ||
                "Waived".equalsIgnoreCase(status);
    }
}