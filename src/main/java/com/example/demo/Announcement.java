package com.example.demo;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Announcement {
    private int announcementId;
    private String title;
    private String content;
    private LocalDate publishDate;
    private String targetRole; // e.g., 'All', 'Student', 'Faculty', 'Admin', 'Librarian'
    private Integer createdByUserId; // Nullable: Foreign key to Users table (Admin who created it)

    // Constructor for creating a new Announcement (ID and publishDate/createdByUserId handled by DB/service)
    public Announcement(String title, String content, LocalDate publishDate, String targetRole, Integer createdByUserId) {
        this(0, title, content, publishDate, targetRole, createdByUserId);
    }

    // Full constructor for retrieving Announcement from the database
    public Announcement(int announcementId, String title, String content,
                        LocalDate publishDate, String targetRole, Integer createdByUserId) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
        this.targetRole = targetRole;
        this.createdByUserId = createdByUserId;
    }

    // --- Getters ---
    public int getAnnouncementId() { return announcementId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDate getPublishDate() { return publishDate; }
    public String getTargetRole() { return targetRole; }
    public Integer getCreatedByUserId() { return createdByUserId; }

    // --- Setters ---
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    @Override
    public String toString() {
        return "Announcement{" +
                "announcementId=" + announcementId +
                ", title='" + title + '\'' +
                ", publishDate=" + publishDate +
                ", targetRole='" + targetRole + '\'' +
                '}';
    }
}
