package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.BytesToUUID;
import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class CalendarEvent {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventColor color;    // now an enum
    private String tag;

    // In-memory caches for tags (unchanged)
    private static final List<String> existingTagsAtUser = new ArrayList<>();
    private static final List<String> existingTags = new ArrayList<>();

    public CalendarEvent(String title,
                         String description,
                         LocalDateTime start,
                         LocalDateTime end,
                         EventColor color,
                         String tag) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.color = color;
        this.tag = tag;
    }

    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd()   { return end; }

    /** Returns the enum value. */
    public EventColor getColor()    { return color; }

    public String getTag()          { return tag; }

    /** Fetch all events for the user. */
    public static List<CalendarEvent> getAllEvents() {
        return CalendarEventProvider.getEvents();
    }

    /**
     * Returns tags currently associated with the logged-in user.
     * Queries calendar_tags & user_tags tables.
     */
    public static List<String> getExistingTagsatUser() {
        existingTagsAtUser.clear();
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        String sql = """
            SELECT ct.tag_value
              FROM calendar_tags ct
              JOIN user_tags ut ON ct.tag_uuid = ut.tag_uuid
             WHERE ut.user_id = ?
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingTagsAtUser.add(rs.getString("tag_value"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingTagsAtUser;
    }

    /**
     * Returns all tags defined in calendar_tags.
     */
    public static List<String> getExistingTags() {
        existingTags.clear();

        String sql = "SELECT tag_value FROM calendar_tags";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                existingTags.add(rs.getString("tag_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingTags;
    }

    /**
     * Adds a new tag to calendar_tags (if absent) and associates it to the user.
     * Inserts into calendar_tags, retrieves the generated tag_uuid, then into user_tags.
     */
    public static void addTagToDatabase(String newTag) {
        // Refresh global tag cache
        if (!existingTags.contains(newTag)) {
            existingTags.add(newTag);
        }
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        UUID tagUUID = null;

        try (Connection conn = new DatabaseConnection().getConnection()) {
            // Insert into calendar_tags if missing
            String insertTag = "INSERT INTO calendar_tags (tag_value) VALUES (?) ON CONFLICT DO NOTHING RETURNING tag_uuid";
            try (PreparedStatement ps = conn.prepareStatement(insertTag)) {
                ps.setString(1, newTag);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tagUUID = BytesToUUID(rs.getBytes("tag_uuid"));
                    }
                }
            }
            // If no UUID returned (existing tag), fetch it
            if (tagUUID == null) {
                String selectTag = "SELECT tag_uuid FROM calendar_tags WHERE tag_value = ?";
                try (PreparedStatement ps = conn.prepareStatement(selectTag)) {
                    ps.setString(1, newTag);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            tagUUID = BytesToUUID(rs.getBytes("tag_uuid"));
                        }
                    }
                }
            }
            // Associate with user
            if (tagUUID != null) {
                String insertUserTag = "INSERT INTO user_tags (user_id, tag_uuid) VALUES (?, ?) ON CONFLICT DO NOTHING";
                try (PreparedStatement ps = conn.prepareStatement(insertUserTag)) {
                    ps.setBytes(1, uuidToBytes(userId));
                    ps.setBytes(2, uuidToBytes(tagUUID));
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
