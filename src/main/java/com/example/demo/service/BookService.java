package com.example.demo.service;

import com.example.demo.dao.BookDAO;
import com.example.demo.model.Book;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookService {
    private BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Adds a new book to the library system.
     * Performs validation to ensure title, author, and total copies are valid.
     *
     * @param isbn The ISBN of the book (can be null).
     * @param title The title of the book.
     * @param author The author of the book.
     * @param publisher The publisher of the book (can be null).
     * @param publicationYear The year of publication (can be null).
     * @param genre The genre of the book (can be null).
     * @param totalCopies The total number of copies acquired.
     * @return The newly created Book object.
     * @throws IllegalArgumentException If any validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public Book addNewBook(String isbn, String title, String author, String publisher,
                           Integer publicationYear, String genre, int totalCopies)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty.");
        }
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative.");
        }
        if (publicationYear != null && (publicationYear < 1000 || publicationYear > LocalDateTime.now().getYear())) { // Basic year range
            throw new IllegalArgumentException("Publication year is invalid.");
        }

        String trimmedIsbn = isbn != null ? isbn.trim() : null;
        String trimmedTitle = title.trim();
        String trimmedAuthor = author.trim();
        String trimmedPublisher = publisher != null ? publisher.trim() : null;
        String trimmedGenre = genre != null ? genre.trim() : null;

        // 2. Business Logic Checks
        // Check for duplicate ISBN if provided
        if (trimmedIsbn != null && !trimmedIsbn.isEmpty()) {
            Book existingBook = bookDAO.getBookByIsbn(trimmedIsbn);
            if (existingBook != null) {
                throw new IllegalArgumentException("A book with ISBN '" + trimmedIsbn + "' already exists.");
            }
        }

        // 3. Create Book
        Book newBook = new Book(
                trimmedIsbn,
                trimmedTitle,
                trimmedAuthor,
                trimmedPublisher,
                publicationYear,
                trimmedGenre,
                totalCopies
        );

        bookDAO.addBook(newBook);
        return newBook;
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param bookId The ID of the book.
     * @return The Book object, or null if not found.
     * @throws IllegalArgumentException If book ID is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Book getBookById(int bookId) throws IllegalArgumentException, SQLException {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be positive.");
        }
        return bookDAO.getBookById(bookId);
    }

    /**
     * Retrieves a book by its ISBN.
     *
     * @param isbn The ISBN of the book.
     * @return The Book object, or null if not found.
     * @throws IllegalArgumentException If ISBN is invalid.
     * @throws SQLException If a database error occurs.
     */
    public Book getBookByIsbn(String isbn) throws IllegalArgumentException, SQLException {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be empty for lookup.");
        }
        return bookDAO.getBookByIsbn(isbn.trim());
    }

    /**
     * Searches for books based on a search term in title, author, or ISBN.
     *
     * @param searchTerm An optional term to search for. Can be null or empty to retrieve all books.
     * @return A list of Book objects matching the criteria.
     * @throws SQLException If a database error occurs.
     */
    public List<Book> searchBooks(String searchTerm) throws SQLException {
        return bookDAO.searchBooks(searchTerm);
    }

    /**
     * Retrieves all books from the system.
     *
     * @return A list of all Book objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.searchBooks(null); // Pass null to get all books
    }

    /**
     * Updates an existing book's information.
     * Note: This method does NOT update 'available_copies' directly. That is managed by borrowing/return.
     *
     * @param book The Book object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateBook(Book book) throws IllegalArgumentException, SQLException {
        if (book == null || book.getBookId() <= 0) {
            throw new IllegalArgumentException("Book and a valid Book ID are required for update.");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty.");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty.");
        }
        if (book.getTotalCopies() < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative.");
        }
        if (book.getPublicationYear() != null && (book.getPublicationYear() < 1000 || book.getPublicationYear() > LocalDateTime.now().getYear())) {
            throw new IllegalArgumentException("Publication year is invalid.");
        }

        book.setIsbn(book.getIsbn() != null ? book.getIsbn().trim() : null);
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setPublisher(book.getPublisher() != null ? book.getPublisher().trim() : null);
        book.setGenre(book.getGenre() != null ? book.getGenre().trim() : null);

        // Check for duplicate ISBN if updated and not the current book's own ISBN
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            Book existingBookWithSameIsbn = bookDAO.getBookByIsbn(book.getIsbn());
            if (existingBookWithSameIsbn != null && existingBookWithSameIsbn.getBookId() != book.getBookId()) {
                throw new IllegalArgumentException("ISBN '" + book.getIsbn() + "' is already assigned to another book.");
            }
        }

        // Business rule: Total copies cannot be less than borrowed copies
        Book currentBookState = bookDAO.getBookById(book.getBookId());
        if (currentBookState != null && book.getTotalCopies() < (currentBookState.getTotalCopies() - currentBookState.getAvailableCopies())) {
            throw new IllegalArgumentException("Total copies cannot be less than currently borrowed copies.");
        }
        // Update available copies if total copies changed, maintaining the difference
        if (currentBookState != null) {
            int borrowedCopies = currentBookState.getTotalCopies() - currentBookState.getAvailableCopies();
            book.setAvailableCopies(book.getTotalCopies() - borrowedCopies);
        } else {
            // This case means the book wasn't found by its ID, which implies an error
            throw new IllegalArgumentException("Book with ID " + book.getBookId() + " not found for update.");
        }


        bookDAO.updateBook(book);
    }

    /**
     * Deletes a book from the system.
     * Business rule: A book can only be deleted if there are no outstanding borrowings for it.
     *
     * @param bookId The ID of the book to delete.
     * @throws IllegalArgumentException If book ID is invalid or if there are outstanding borrowings.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteBook(int bookId) throws IllegalArgumentException, SQLException {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be positive for deletion.");
        }

        Book bookToDelete = bookDAO.getBookById(bookId);
        if (bookToDelete == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " not found.");
        }

        if (bookToDelete.getAvailableCopies() < bookToDelete.getTotalCopies()) {
            throw new IllegalArgumentException("Cannot delete book. There are outstanding borrowed copies.");
        }

        bookDAO.deleteBook(bookId);
    }
}
