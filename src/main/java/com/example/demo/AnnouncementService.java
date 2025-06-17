package com.example.demo;

import com.example.demo.AnnouncementDAO;
import com.example.demo.Announcement;
import com.example.demo.User;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AnnouncementService {
    private AnnouncementDAO announcementDAO;
    private UserService userService; // To verify user who created the announcement

    public AnnouncementService() {
        this.announcementDAO = new AnnouncementDAO();
        this.userService = new UserService();
    }

    /**
     * Adds a new announcement to the system.
     * Performs validation to ensure title, content, publish date are valid,
     * and that the creating user exists.
     *
     * @param title The title of the announcement.
     * @param content The content/body of the announcement.
     * @param publishDate The date the announcement is published.
     * @param targetRole The target audience for the announcement (e.g., 'All', 'Student', 'Faculty', 'Admin', 'Librarian').
     * @param createdByUserId The ID of the user (Admin) who created this announcement (can be null if system-generated).
     * @return The newly created Announcement object.
     * @throws IllegalArgumentException If any validation or business rules fail.
     * @throws SQLException If a database access error occurs.
     */
    public Announcement addNewAnnouncement(String title, String content, LocalDate publishDate,
                                           String targetRole, Integer createdByUserId)
            throws IllegalArgumentException, SQLException {

        // 1. Input Validation
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement title cannot be empty.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement content cannot be empty.");
        }
        if (publishDate == null || publishDate.isBefore(LocalDate.now())) { // Publish date cannot be in the past
            throw new IllegalArgumentException("Publish Date cannot be in the past.");
        }
        if (targetRole == null || targetRole.trim().isEmpty() || !isValidTargetRole(targetRole)) {
            throw new IllegalArgumentException("Invalid target role. Must be 'All', 'Student', 'Faculty', 'Admin', or 'Librarian'.");
        }

        String trimmedTitle = title.trim();
        String trimmedContent = content.trim();
        String trimmedTargetRole = targetRole.trim();

        // 2. Business Logic Checks
        // Verify creating user exists if provided
        if (createdByUserId != null) {
            User creatingUser = userService.getUserById(createdByUserId);
            if (creatingUser == null) {
                throw new IllegalArgumentException("User with ID " + createdByUserId + " (who created the announcement) does not exist.");
            }
            // Optional: Ensure only 'Admin' role can create announcements
            // if (!"Admin".equals(creatingUser.getRole())) {
            //     throw new IllegalArgumentException("Only Admin users can create announcements.");
            // }
        }

        // 3. Create Announcement
        Announcement newAnnouncement = new Announcement(
                trimmedTitle,
                trimmedContent,
                publishDate,
                trimmedTargetRole,
                createdByUserId
        );

        announcementDAO.addAnnouncement(newAnnouncement);
        return newAnnouncement;
    }

    /**
     * Retrieves an announcement record by its ID.
     *
     * @param announcementId The ID of the announcement record.
     * @return The Announcement object if found, null otherwise.
     * @throws IllegalArgumentException If announcement ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public Announcement getAnnouncementById(int announcementId) throws IllegalArgumentException, SQLException {
        if (announcementId <= 0) {
            throw new IllegalArgumentException("Announcement ID must be positive.");
        }
        return announcementDAO.getAnnouncementById(announcementId);
    }

    /**
     * Retrieves all announcement records from the system.
     *
     * @return A list of all Announcement objects.
     * @throws SQLException If a database error occurs.
     */
    public List<Announcement> getAllAnnouncements() throws SQLException {
        return announcementDAO.getAllAnnouncements();
    }

    /**
     * Retrieves all announcement records targeted to a specific role (including 'All').
     *
     * @param role The target role (e.g., 'Student', 'Faculty', 'Librarian', 'Admin').
     * @return A list of Announcement objects relevant to the specified role.
     * @throws IllegalArgumentException If role is invalid or empty.
     * @throws SQLException If a database access error occurs.
     */
    public List<Announcement> getAnnouncementsByTargetRole(String role) throws IllegalArgumentException, SQLException {
        if (role == null || role.trim().isEmpty() || !isValidTargetRole(role)) {
            throw new IllegalArgumentException("Invalid target role for retrieving announcements.");
        }
        return announcementDAO.getAnnouncementsByTargetRole(role.trim());
    }

    /**
     * Updates an existing announcement record.
     *
     * @param announcement The Announcement object with updated details.
     * @throws IllegalArgumentException If validation fails.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAnnouncement(Announcement announcement) throws IllegalArgumentException, SQLException {
        if (announcement == null || announcement.getAnnouncementId() <= 0) {
            throw new IllegalArgumentException("Announcement record and a valid ID are required for update.");
        }
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement title cannot be empty.");
        }
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement content cannot be empty.");
        }
        if (announcement.getPublishDate() == null || announcement.getPublishDate().isBefore(LocalDate.now())) {
            // Allow updating past announcements, but new publish date must not be in the past
            // If the original publish date was in the past, and new one is also in past, that's fine.
            // If new publish date is set, ensure it's not before current date for future announcements.
            // Simplified for now: no specific check for existing vs new past date.
        }
        if (announcement.getTargetRole() == null || announcement.getTargetRole().trim().isEmpty() || !isValidTargetRole(announcement.getTargetRole())) {
            throw new IllegalArgumentException("Invalid target role. Must be 'All', 'Student', 'Faculty', 'Admin', or 'Librarian'.");
        }

        announcement.setTitle(announcement.getTitle().trim());
        announcement.setContent(announcement.getContent().trim());
        announcement.setTargetRole(announcement.getTargetRole().trim());

        // Verify creating user still exists if provided
        if (announcement.getCreatedByUserId() != null && userService.getUserById(announcement.getCreatedByUserId()) == null) {
            throw new IllegalArgumentException("Associated creating user does not exist.");
        }

        announcementDAO.updateAnnouncement(announcement);
    }

    /**
     * Deletes an announcement record from the system.
     *
     * @param announcementId The ID of the announcement record to delete.
     * @throws IllegalArgumentException If announcement ID is invalid.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAnnouncement(int announcementId) throws IllegalArgumentException, SQLException {
        if (announcementId <= 0) {
            throw new IllegalArgumentException("Announcement ID must be positive for deletion.");
        }
        announcementDAO.deleteAnnouncement(announcementId);
    }

    /**
     * Helper method to validate if a given string is a recognized target role.
     */
    private boolean isValidTargetRole(String role) {
        return "All".equalsIgnoreCase(role) ||
                "Admin".equalsIgnoreCase(role) ||
                "Student".equalsIgnoreCase(role) ||
                "Faculty".equalsIgnoreCase(role) ||
                "Librarian".equalsIgnoreCase(role);
    }
}