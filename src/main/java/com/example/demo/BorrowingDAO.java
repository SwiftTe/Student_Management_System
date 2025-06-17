package com.example.demo;

import com.example.demo.Borrowing;
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

public class BorrowingDAO {

    private BookDAO bookDAO; // To update book counts when borrowing/returning

    public BorrowingDAO() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Adds a new book borrowing record. This is a transactional operation:
     * it adds the borrowing record and decreases the available copies of the book.
     *
     * @param borrowing The Borrowing object to add. Its borrowingId will be updated upon successful creation.
     * @param bookAvailableCopies The current available copies of the book to be borrowed.
     * @throws SQLException If a database access error occurs or if there are no copies available.
     */
    public void addBorrowing(Borrowing borrowing, int bookAvailableCopies) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check if book is available
            if (bookAvailableCopies <= 0) {
                throw new SQLException("Book is not available for borrowing.");
            }

            // 2. Add the borrowing record
            String sql = "INSERT INTO Borrowings (book_id, student_id, borrow_date, return_due_date, return_date, fine_amount) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, borrowing.getBookId());
                stmt.setInt(2, borrowing.getStudentId());
                stmt.setDate(3, Date.valueOf(borrowing.getBorrowDate()));
                stmt.setDate(4, Date.valueOf(borrowing.getReturnDueDate()));
                if (borrowing.getReturnDate() != null) {
                    stmt.setDate(5, Date.valueOf(borrowing.getReturnDate()));
                } else {
                    stmt.setNull(5, java.sql.Types.DATE); // Null if not returned yet
                }
                stmt.setDouble(6, borrowing.getFineAmount());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating borrowing record failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        borrowing.setBorrowingId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                    } else {
                        throw new SQLException("Creating borrowing record failed, no ID obtained.");
                    }
                }
            }

            // 3. Decrease available copies of the book
            bookDAO.updateAvailableCopies(borrowing.getBookId(), bookAvailableCopies - 1);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if any part of the transaction fails
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit mode
                    conn.close(); // Close the connection
                } catch (SQLException ex) {
                    System.err.println("Closing connection failed: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves a borrowing record by its ID.
     *
     * @param borrowingId The ID of the borrowing record to retrieve.
     * @return The Borrowing object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Borrowing getBorrowingById(int borrowingId) throws SQLException {
        String sql = "SELECT borrowing_id, book_id, student_id, borrow_date, return_due_date, return_date, fine_amount FROM Borrowings WHERE borrowing_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate returnDate = null;
                    if (rs.getDate("return_date") != null) {
                        returnDate = rs.getDate("return_date").toLocalDate();
                    }
                    return new Borrowing(
                            rs.getInt("borrowing_id"),
                            rs.getInt("book_id"),
                            rs.getInt("student_id"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("return_due_date").toLocalDate(),
                            returnDate,
                            rs.getDouble("fine_amount")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all active (not yet returned) borrowing records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of active Borrowing objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<Borrowing> getActiveBorrowingsByStudentId(int studentId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT borrowing_id, book_id, student_id, borrow_date, return_due_date, return_date, fine_amount FROM Borrowings WHERE student_id = ? AND return_date IS NULL ORDER BY return_due_date ASC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    borrowings.add(new Borrowing(
                            rs.getInt("borrowing_id"),
                            rs.getInt("book_id"),
                            rs.getInt("student_id"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("return_due_date").toLocalDate(),
                            null, // return_date is NULL for active borrowings
                            rs.getDouble("fine_amount")
                    ));
                }
            }
        }
        return borrowings;
    }

    /**
     * Retrieves all borrowing records for a specific book.
     *
     * @param bookId The ID of the book.
     * @return A list of Borrowing objects for the given book.
     * @throws SQLException If a database access error occurs.
     */
    public List<Borrowing> getBorrowingsByBookId(int bookId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT borrowing_id, book_id, student_id, borrow_date, return_due_date, return_date, fine_amount FROM Borrowings WHERE book_id = ? ORDER BY borrow_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate returnDate = null;
                    if (rs.getDate("return_date") != null) {
                        returnDate = rs.getDate("return_date").toLocalDate();
                    }
                    borrowings.add(new Borrowing(
                            rs.getInt("borrowing_id"),
                            rs.getInt("book_id"),
                            rs.getInt("student_id"),
                            rs.getDate("borrow_date").toLocalDate(),
                            rs.getDate("return_due_date").toLocalDate(),
                            returnDate,
                            rs.getDouble("fine_amount")
                    ));
                }
            }
        }
        return borrowings;
    }

    /**
     * Retrieves all borrowing records from the database.
     *
     * @return A list of all Borrowing objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Borrowing> getAllBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT borrowing_id, book_id, student_id, borrow_date, return_due_date, return_date, fine_amount FROM Borrowings ORDER BY borrow_date DESC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate returnDate = null;
                if (rs.getDate("return_date") != null) {
                    returnDate = rs.getDate("return_date").toLocalDate();
                }
                borrowings.add(new Borrowing(
                        rs.getInt("borrowing_id"),
                        rs.getInt("book_id"),
                        rs.getInt("student_id"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("return_due_date").toLocalDate(),
                        returnDate,
                        rs.getDouble("fine_amount")
                ));
            }
        }
        return borrowings;
    }

    /**
     * Marks a book as returned and updates the available copies.
     * This is a transactional operation.
     *
     * @param borrowing The Borrowing object with updated return_date and fine_amount.
     * @param bookAvailableCopies The current available copies of the book.
     * @throws SQLException If a database access error occurs.
     */
    public void returnBook(Borrowing borrowing, int bookAvailableCopies) throws SQLException {
        Connection conn = null;
        try {
            conn = DBController.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Update the borrowing record with return date and fine
            String sql = "UPDATE Borrowings SET return_date=?, fine_amount=? WHERE borrowing_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (borrowing.getReturnDate() != null) {
                    stmt.setDate(1, Date.valueOf(borrowing.getReturnDate()));
                } else {
                    stmt.setNull(1, java.sql.Types.DATE); // Should ideally be non-null when returning
                }
                stmt.setDouble(2, borrowing.getFineAmount());
                stmt.setInt(3, borrowing.getBorrowingId());
                stmt.executeUpdate();
            }

            // 2. Increase available copies of the book
            bookDAO.updateAvailableCopies(borrowing.getBookId(), bookAvailableCopies + 1);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback if any part of the transaction fails
                } catch (SQLException ex) {
                    System.err.println("Rollback failed during book return: " + ex.getMessage());
                }
            }
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit mode
                    conn.close(); // Close the connection
                } catch (SQLException ex) {
                    System.err.println("Closing connection failed: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Deletes a borrowing record from the database by its ID.
     * (Note: This method does NOT update book counts; should only be used for cleaning up invalid records.)
     *
     * @param borrowingId The ID of the borrowing record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteBorrowing(int borrowingId) throws SQLException {
        String sql = "DELETE FROM Borrowings WHERE borrowing_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowingId);
            stmt.executeUpdate();
        }
    }
}
