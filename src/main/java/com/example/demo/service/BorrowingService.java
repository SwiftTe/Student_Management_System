package com.example.demo.service;

import com.example.demo.dao.BorrowingDAO;
import com.example.demo.model.Book;
import com.example.demo.model.Borrowing;
import com.example.demo.model.Student;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BorrowingService {
    private BorrowingDAO borrowingDAO;
    private BookService bookService;     // To verify book existence and manage available copies
    private StudentService studentService; // To verify student existence

    public BorrowingService() {
        this.borrowingDAO = new BorrowingDAO();
        this.bookService = new BookService();
        this.studentService = new StudentService();
    }

    /**
     * Handles the borrowing of a book by a student.
     * Performs validation and updates the book's available copies. This is a transactional operation.
     *
     * @param bookId The ID of the book to borrow.
     * @param studentId The ID of the student borrowing the book.
     * @param borrowDate The date the book is borrowed.
     * @param returnDueDate The date the book is due back.
     * @return The newly created Borrowing object.
     * @throws IllegalArgumentException If validation or business rules fail (e.g., book not found, not available).
     * @throws SQLException If a database access error occurs.
     */
    public Borrowing borrowBook(int bookId, int studentId, LocalDate borrowDate, LocalDate returnDueDate)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be positive.");
        }
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (borrowDate == null || borrowDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Borrow Date cannot be in the future.");
        }
        if (returnDueDate == null || returnDueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return Due Date cannot be before Borrow Date.");
        }

        // 2. Business Logic Checks
        // Verify book exists and is available
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("Book '" + book.getTitle() + "' is currently not available for borrowing.");
        }

        // Verify student exists
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
        }

        // Optional: Check if student has too many books borrowed already (business rule)
        // List<Borrowing> studentCurrentBorrowings = borrowingDAO.getActiveBorrowingsByStudentId(studentId);
        // if (studentCurrentBorrowings.size() >= MAX_BORROWED_BOOKS) { // Define MAX_BORROWED_BOOKS constant
        //     throw new IllegalArgumentException("Student has reached the maximum number of borrowed books.");
        // }

        // Create the borrowing object (initial fine is 0, returnDate is null)
        Borrowing newBorrowing = new Borrowing(bookId, studentId, borrowDate, returnDueDate);

        // Call DAO, which handles the transaction (add borrowing + update book copies)
        borrowingDAO.addBorrowing(newBorrowing, book.getAvailableCopies());
        return newBorrowing;
    }

    /**
     * Handles the return of a borrowed book.
     * Calculates any potential fine and updates the book's available copies. This is a transactional operation.
     *
     * @param borrowingId The ID of the borrowing record to mark as returned.
     * @param returnDate The actual date the book is returned.
     * @return The updated Borrowing object.
     * @throws IllegalArgumentException If validation or business rules fail (e.g., borrowing record not found, already returned).
     * @throws SQLException If a database access error occurs.
     */
    public Borrowing returnBook(int borrowingId, LocalDate returnDate)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (borrowingId <= 0) {
            throw new IllegalArgumentException("Borrowing ID must be positive.");
        }
        if (returnDate == null || returnDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Return Date cannot be in the future.");
        }

        // 2. Business Logic Checks
        Borrowing existingBorrowing = borrowingDAO.getBorrowingById(borrowingId);
        if (existingBorrowing == null) {
            throw new IllegalArgumentException("Borrowing record with ID " + borrowingId + " not found.");
        }
        if (existingBorrowing.getReturnDate() != null) {
            throw new IllegalArgumentException("Book for borrowing ID " + borrowingId + " has already been returned.");
        }

        // Calculate fine if overdue
        double fine = 0.0;
        if (returnDate.isAfter(existingBorrowing.getReturnDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(existingBorrowing.getReturnDueDate(), returnDate);
            // Example: 5 rupees per day fine
            fine = daysOverdue * 5.0; // Define FINE_PER_DAY_RUPEES constant
        }

        // Update the borrowing object with return details
        existingBorrowing.setReturnDate(returnDate);
        existingBorrowing.setFineAmount(fine);

        // Get current book state to update available copies
        Book book = bookService.getBookById(existingBorrowing.getBookId());
        if (book == null) {
            throw new SQLException("Associated book not found for borrowing ID " + borrowingId);
        }

        // Call DAO, which handles the transaction (update borrowing + update book copies)
        borrowingDAO.returnBook(existingBorrowing, book.getAvailableCopies());
        return existingBorrowing;
    }

    /**
     * Retrieves a borrowing record by its ID.
     *
     * @param borrowingId The ID of the borrowing record.
     * @return The Borrowing object, or null if not found.
     * @throws IllegalArgumentException If borrowing ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Borrowing getBorrowingById(int borrowingId) throws IllegalArgumentException, SQLException {
        if (borrowingId <= 0) {
            throw new IllegalArgumentException("Borrowing ID must be positive.");
        }
        return borrowingDAO.getBorrowingById(borrowingId);
    }

    /**
     * Retrieves all active (not yet returned) borrowing records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of active Borrowing objects for the given student.
     * @throws IllegalArgumentException If student ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Borrowing> getActiveBorrowingsByStudentId(int studentId) throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        // Optional: Verify student existence
        if (studentService.getStudentById(studentId) == null) {
            System.out.println("Warning: Attempted to get active borrowings for non-existent student ID: " + studentId);
            return List.of(); // Return empty list or throw if strict validation is needed
        }
        return borrowingDAO.getActiveBorrowingsByStudentId(studentId);
    }

    /**
     * Retrieves all borrowing records for a specific book.
     *
     * @param bookId The ID of the book.
     * @return A list of Borrowing objects for the given book.
     * @throws IllegalArgumentException If book ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public List<Borrowing> getBorrowingsByBookId(int bookId) throws IllegalArgumentException, SQLException {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be positive.");
        }
        // Optional: Verify book existence
        if (bookService.getBookById(bookId) == null) {
            System.out.println("Warning: Attempted to get borrowings for non-existent book ID: " + bookId);
            return List.of(); // Return empty list or throw
        }
        return borrowingDAO.getBorrowingsByBookId(bookId);
    }

    /**
     * Retrieves all borrowing records from the system (both active and returned).
     *
     * @return A list of all Borrowing objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Borrowing> getAllBorrowings() throws SQLException {
        return borrowingDAO.getAllBorrowings();
    }

    /**
     * Deletes a borrowing record from the system.
     * This method should be used cautiously, typically only for correcting erroneous entries,
     * as it does NOT reverse the book's available copy count.
     *
     * @param borrowingId The ID of the borrowing record to delete.
     * @throws IllegalArgumentException If borrowing ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteBorrowing(int borrowingId) throws IllegalArgumentException, SQLException {
        if (borrowingId <= 0) {
            throw new IllegalArgumentException("Borrowing ID must be positive for deletion.");
        }
        // IMPORTANT: This delete does NOT update book counts.
        // If you need to undo a borrowing and return the book, use the returnBook method instead.
        borrowingDAO.deleteBorrowing(borrowingId);
    }
}
