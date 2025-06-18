package com.example.demo.dao;

import com.example.demo.model.Fees;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeesDAO {

    /**
     * Adds a new fee record to the database.
     *
     * @param fees The Fees object to add. Its feeId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addFees(Fees fees) throws SQLException {
        String sql = "INSERT INTO Fees (student_id, fee_type, amount, due_date, payment_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, fees.getStudentId());
            stmt.setString(2, fees.getFeeType());
            stmt.setDouble(3, fees.getAmount());
            stmt.setDate(4, Date.valueOf(fees.getDueDate()));
            if (fees.getPaymentDate() != null) {
                stmt.setDate(5, Date.valueOf(fees.getPaymentDate()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            stmt.setString(6, fees.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating fee record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    fees.setFeeId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating fee record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a fee record by its ID.
     *
     * @param feeId The ID of the fee record to retrieve.
     * @return The Fees object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Fees getFeesById(int feeId) throws SQLException {
        String sql = "SELECT fee_id, student_id, fee_type, amount, due_date, payment_date, status FROM Fees WHERE fee_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate paymentDate = null;
                    if (rs.getDate("payment_date") != null) {
                        paymentDate = rs.getDate("payment_date").toLocalDate();
                    }
                    return new Fees(
                            rs.getInt("fee_id"),
                            rs.getInt("student_id"),
                            rs.getString("fee_type"),
                            rs.getDouble("amount"),
                            rs.getDate("due_date").toLocalDate(),
                            paymentDate,
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all fee records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of Fees objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<Fees> getFeesByStudentId(int studentId) throws SQLException {
        List<Fees> feesList = new ArrayList<>();
        String sql = "SELECT fee_id, student_id, fee_type, amount, due_date, payment_date, status FROM Fees WHERE student_id = ? ORDER BY due_date ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate paymentDate = null;
                    if (rs.getDate("payment_date") != null) {
                        paymentDate = rs.getDate("payment_date").toLocalDate();
                    }
                    feesList.add(new Fees(
                            rs.getInt("fee_id"),
                            rs.getInt("student_id"),
                            rs.getString("fee_type"),
                            rs.getDouble("amount"),
                            rs.getDate("due_date").toLocalDate(),
                            paymentDate,
                            rs.getString("status")
                    ));
                }
            }
        }
        return feesList;
    }

    /**
     * Retrieves all fee records from the database.
     *
     * @return A list of all Fees objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Fees> getAllFees() throws SQLException {
        List<Fees> feesList = new ArrayList<>();
        String sql = "SELECT fee_id, student_id, fee_type, amount, due_date, payment_date, status FROM Fees ORDER BY due_date ASC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate paymentDate = null;
                if (rs.getDate("payment_date") != null) {
                    paymentDate = rs.getDate("payment_date").toLocalDate();
                }
                feesList.add(new Fees(
                        rs.getInt("fee_id"),
                        rs.getInt("student_id"),
                        rs.getString("fee_type"),
                        rs.getDouble("amount"),
                        rs.getDate("due_date").toLocalDate(),
                        paymentDate,
                        rs.getString("status")
                ));
            }
        }
        return feesList;
    }

    /**
     * Updates an existing fee record.
     *
     * @param fees The Fees object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateFees(Fees fees) throws SQLException {
        String sql = "UPDATE Fees SET student_id=?, fee_type=?, amount=?, due_date=?, payment_date=?, status=? WHERE fee_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fees.getStudentId());
            stmt.setString(2, fees.getFeeType());
            stmt.setDouble(3, fees.getAmount());
            stmt.setDate(4, Date.valueOf(fees.getDueDate()));
            if (fees.getPaymentDate() != null) {
                stmt.setDate(5, Date.valueOf(fees.getPaymentDate()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            stmt.setString(6, fees.getStatus());
            stmt.setInt(7, fees.getFeeId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a fee record from the database by its ID.
     *
     * @param feeId The ID of the fee record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteFees(int feeId) throws SQLException {
        String sql = "DELETE FROM Fees WHERE fee_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feeId);
            stmt.executeUpdate();
        }
    }
}
