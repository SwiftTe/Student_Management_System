package com.example.demo;

import com.example.demo.Announcement;
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

public class AnnouncementDAO {

    /**
     * Adds a new announcement record to the database.
     *
     * @param announcement The Announcement object to add. Its announcementId will be updated upon successful creation.
     * @throws SQLException If a database access error occurs.
     */
    public void addAnnouncement(Announcement announcement) throws SQLException {
        String sql = "INSERT INTO Announcements (title, content, publish_date, target_role, created_by_user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getContent());
            stmt.setDate(3, Date.valueOf(announcement.getPublishDate()));
            stmt.setString(4, announcement.getTargetRole());
            if (announcement.getCreatedByUserId() != null) {
                stmt.setInt(5, announcement.getCreatedByUserId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating announcement record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    announcement.setAnnouncementId(generatedKeys.getInt(1)); // Set the generated ID back to the object
                } else {
                    throw new SQLException("Creating announcement record failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Retrieves an announcement record by its ID.
     *
     * @param announcementId The ID of the announcement record to retrieve.
     * @return The Announcement object if found, null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Announcement getAnnouncementById(int announcementId) throws SQLException {
        String sql = "SELECT announcement_id, title, content, publish_date, target_role, created_by_user_id FROM Announcements WHERE announcement_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, announcementId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Announcement(
                            rs.getInt("announcement_id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getDate("publish_date").toLocalDate(),
                            rs.getString("target_role"),
                            (Integer) rs.getObject("created_by_user_id")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all announcement records.
     *
     * @return A list of all Announcement objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Announcement> getAllAnnouncements() throws SQLException {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT announcement_id, title, content, publish_date, target_role, created_by_user_id FROM Announcements ORDER BY publish_date DESC";
        try (Connection conn = DBController.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                announcements.add(new Announcement(
                        rs.getInt("announcement_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDate("publish_date").toLocalDate(),
                        rs.getString("target_role"),
                        (Integer) rs.getObject("created_by_user_id")
                ));
            }
        }
        return announcements;
    }

    /**
     * Retrieves all announcement records targeted to a specific role or 'All'.
     *
     * @param role The target role (e.g., 'Student', 'Faculty', 'Librarian', 'Admin').
     * @return A list of Announcement objects relevant to the specified role.
     * @throws SQLException If a database access error occurs.
     */
    public List<Announcement> getAnnouncementsByTargetRole(String role) throws SQLException {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT announcement_id, title, content, publish_date, target_role, created_by_user_id FROM Announcements WHERE target_role = ? OR target_role = 'All' ORDER BY publish_date DESC";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(new Announcement(
                            rs.getInt("announcement_id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getDate("publish_date").toLocalDate(),
                            rs.getString("target_role"),
                            (Integer) rs.getObject("created_by_user_id")
                    ));
                }
            }
        }
        return announcements;
    }


    /**
     * Updates an existing announcement record.
     *
     * @param announcement The Announcement object with updated details.
     * @throws SQLException If a database access error occurs.
     */
    public void updateAnnouncement(Announcement announcement) throws SQLException {
        String sql = "UPDATE Announcements SET title=?, content=?, publish_date=?, target_role=?, created_by_user_id=? WHERE announcement_id=?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getContent());
            stmt.setDate(3, Date.valueOf(announcement.getPublishDate()));
            stmt.setString(4, announcement.getTargetRole());
            if (announcement.getCreatedByUserId() != null) {
                stmt.setInt(5, announcement.getCreatedByUserId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, announcement.getAnnouncementId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes an announcement record from the database by its ID.
     *
     * @param announcementId The ID of the announcement record to delete.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteAnnouncement(int announcementId) throws SQLException {
        String sql = "DELETE FROM Announcements WHERE announcement_id = ?";
        try (Connection conn = DBController.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, announcementId);
            stmt.executeUpdate();
        }
    }
}
