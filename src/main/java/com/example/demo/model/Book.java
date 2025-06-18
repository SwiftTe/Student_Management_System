package com.example.demo.model;

import java.time.LocalDateTime;

public class Book {
    private int bookId;
    private String isbn; // ISBN can be nullable
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear; // Nullable
    private String genre;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime addedDate;

    // Constructor for creating a new Book (ID and addedDate handled by DB, availableCopies equals totalCopies initially)
    public Book(String isbn, String title, String author, String publisher,
                Integer publicationYear, String genre, int totalCopies) {
        this(0, isbn, title, author, publisher, publicationYear, genre, totalCopies, totalCopies, null);
    }

    // Full constructor for retrieving Book from the database
    public Book(int bookId, String isbn, String title, String author, String publisher,
                Integer publicationYear, String genre, int totalCopies, int availableCopies, LocalDateTime addedDate) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.addedDate = addedDate;
    }

    // --- Getters ---
    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public String getGenre() { return genre; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public LocalDateTime getAddedDate() { return addedDate; }

    // --- Setters ---
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public void setAddedDate(LocalDateTime addedDate) { this.addedDate = addedDate; }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", availableCopies=" + availableCopies +
                '}';
    }
}
