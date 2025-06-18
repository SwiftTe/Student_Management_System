package com.example.demo.dao;

import com.example.demo.model.Book;
import com.example.demo.DBController; // Correctly referencing the DBController
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    /**
     * Adds a new book to the database.
     * available_copies will be automatically set to total_copies upon creation.
     * added_date will be automatically set by the database.
     *
     * @param book The Book object to add. Its bookId and addedDate will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO Books (isbn, title, author, publisher, publication_year, genre, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            if (book.getPublicationYear() != null) {
                stmt.setInt(5, book.getPublicationYear());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, book.getGenre());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getTotalCopies()); // available_copies = total_copies initially

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setBookId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                    // Retrieve generated timestamp if needed:
                    // book.setAddedDate(generatedKeys.getTimestamp(generatedKeys.findColumn("added_date")).toLocalDateTime());
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves a book by its ID.
     *
     * @param bookId The ID of the book to retrieve.
     * @return The Book object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT book_id, isbn, title, author, publisher, publication_year, genre, total_copies, available_copies, added_date FROM Books WHERE book_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp addedTimestamp = rs.getTimestamp("added_date");
                    LocalDateTime addedDateTime = (addedTimestamp != null) ? addedTimestamp.toLocalDateTime() : null;

                    return new Book(
                            rs.getInt("book_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            (Integer) rs.getObject("publication_year"), // For nullable INT
                            rs.getString("genre"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            addedDateTime
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a book by its ISBN.
     *
     * @param isbn The ISBN of the book to retrieve.
     * @return The Book object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Book getBookByIsbn(String isbn) throws SQLException {
        String sql = "SELECT book_id, isbn, title, author, publisher, publication_year, genre, total_copies, available_copies, added_date FROM Books WHERE isbn = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp addedTimestamp = rs.getTimestamp("added_date");
                    LocalDateTime addedDateTime = (addedTimestamp != null) ? addedTimestamp.toLocalDateTime() : null;

                    return new Book(
                            rs.getInt("book_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            (Integer) rs.getObject("publication_year"),
                            rs.getString("genre"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            addedDateTime
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all books from the database, optionally filtering by title, author, or ISBN.
     *
     * @param searchTerm An optional term to search for in title, author, or ISBN. Can be null or empty.
     * @return A list of Book objects matching the criteria or all books if no search term.
     * @throws SQLException If a database access error occurs.
     */
    public List<Book> searchBooks(String searchTerm) throws SQLException {
        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT book_id, isbn, title, author, publisher, publication_year, genre, total_copies, available_copies, added_date FROM Books");
        boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();

        if (hasSearchTerm) {
            sql.append(" WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?");
        }
        sql.append(" ORDER BY title ASC");

        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (hasSearchTerm) {
                String likeTerm = "%" + searchTerm.trim() + "%";
                stmt.setString(1, likeTerm);
                stmt.setString(2, likeTerm);
                stmt.setString(3, likeTerm);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp addedTimestamp = rs.getTimestamp("added_date");
                    LocalDateTime addedDateTime = (addedTimestamp != null) ? addedTimestamp.toLocalDateTime() : null;

                    books.add(new Book(
                            rs.getInt("book_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            (Integer) rs.getObject("publication_year"),
                            rs.getString("genre"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            addedDateTime
                    ));
                }
            }
        }
        return books;
    }


    /**
     * Updates an existing book's information.
     * This method does not allow updating available_copies directly; use updateAvailableCopies.
     *
     * @param book The Book object with updated details (excluding available_copies).
     * @throws SQLException If a database access error occurs.
     */
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE Books SET isbn=?, title=?, author=?, publisher=?, publication_year=?, genre=?, total_copies=? WHERE book_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            if (book.getPublicationYear() != null) {
                stmt.setInt(5, book.getPublicationYear());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setString(6, book.getGenre());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getBookId());
            stmt.executeUpdate();
        }
    }

    /**
     * Updates the available copies of a book. This method should be used when books are borrowed or returned.
     *
     * @param bookId The ID of the book to update.
     * @param newAvailableCopies The new count of available copies.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAvailableCopies(int bookId, int newAvailableCopies) throws SQLException {
        String sql = "UPDATE Books SET available_copies=? WHERE book_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newAvailableCopies);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a book from the database by its ID.
     *
     * @param bookId The ID of the book to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM Books WHERE book_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }
}
