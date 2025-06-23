package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository for CRUD operations on the `calendars` table.
 */
public class CalendarRepository {

    /**
     * Fetches all calendars belonging to a user.
     */
    public static List<CalendarModel> findByUser(UUID userId) throws SQLException {
        String sql = "SELECT calendar_id, name, style FROM calendars WHERE user_id = ?";
        List<CalendarModel> out = new ArrayList<>();

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID calId   = UUIDHelper.BytesToUUID(rs.getBytes("calendar_id"));
                    String name  = rs.getString("name");
                    String style = rs.getString("style");
                    out.add(new CalendarModel(calId, userId, name, style));
                }
            }
        }
        return out;
    }

    /**
     * Creates a new calendar row and returns its UUID.
     */
    public UUID createCalendar(UUID userId, String name, String style) throws SQLException {
        UUID calId = UUID.randomUUID();
        String sql = "INSERT INTO calendars (calendar_id, user_id, name, style) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(calId));
            ps.setBytes(2, UUIDHelper.uuidToBytes(userId));
            ps.setString(3, name);
            ps.setString(4, style);
            ps.executeUpdate();
        }
        return calId;
    }

    /**
     * Fetches (or creates) the single “Blockers” calendar for a user.
     * Subsequent calls will reuse the same UUID.
     */
    public UUID getOrCreateBlockersCalendar(UUID userId) throws SQLException {
        String fetch = "SELECT calendar_id FROM calendars WHERE user_id = ? AND name = 'Blockers'";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(fetch)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUIDHelper.BytesToUUID(rs.getBytes("calendar_id"));
                }
            }
        }
        // Not found → create one
        return createCalendar(userId, "Blockers", "STYLE1");
    }
}
