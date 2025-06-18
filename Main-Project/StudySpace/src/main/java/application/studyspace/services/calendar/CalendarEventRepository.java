package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalendarEventRepository {

    public void save(CalendarEvent e) throws SQLException {
        String sql = """
            INSERT INTO calendar_events (
              event_id, user_id, title, description, location,
              start_datetime, end_datetime, full_day, hidden,
              min_duration, recurrence_rule, recurrence_source,
              recurrence_id, tag_uuid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              title         = VALUES(title),
              description   = VALUES(description),
              location      = VALUES(location),
              start_datetime= VALUES(start_datetime),
              end_datetime  = VALUES(end_datetime),
              full_day      = VALUES(full_day),
              hidden        = VALUES(hidden),
              min_duration  = VALUES(min_duration),
              recurrence_rule  = VALUES(recurrence_rule),
              recurrence_source= VALUES(recurrence_source),
              recurrence_id    = VALUES(recurrence_id),
              tag_uuid         = VALUES(tag_uuid)
        """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1,  UUIDHelper.uuidToBytes(e.getId()));
            ps.setBytes(2,  UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getLocation());
            ps.setTimestamp(6, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(7, Timestamp.from(e.getEnd().toInstant()));
            ps.setBoolean(8, e.isFullDay());
            ps.setBoolean(9, e.isHidden());
            ps.setObject(10, e.getMinDuration() == null ? null : e.getMinDuration().toMinutes());
            ps.setString(11, e.getRecurrenceRule());
            ps.setBytes(12, e.getRecurrenceSource() == null
                    ? null
                    : UUIDHelper.uuidToBytes(e.getRecurrenceSource()));
            ps.setTimestamp(13, e.getRecurrenceId() == null
                    ? null
                    : Timestamp.from(e.getRecurrenceId().toInstant()));
            ps.setBytes(14, e.getTagUuid() == null
                    ? null
                    : UUIDHelper.uuidToBytes(e.getTagUuid()));

            ps.executeUpdate();
        }
    }

    public List<CalendarEvent> findByUser(UUID userId) throws SQLException {
        String sql = "SELECT * FROM calendar_events WHERE user_id = ?";
        List<CalendarEvent> list = new ArrayList<>();

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CalendarEvent(
                            UUIDHelper.BytesToUUID(rs.getBytes("event_id")),
                            userId,
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("location"),
                            rs.getTimestamp("start_datetime")
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault()),
                            rs.getTimestamp("end_datetime")
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault()),
                            rs.getBoolean("full_day"),
                            rs.getBoolean("hidden"),
                            rs.getObject("min_duration", Integer.class) == null
                                    ? null
                                    : Duration.ofMinutes(rs.getInt("min_duration")),
                            rs.getString("recurrence_rule"),
                            rs.getBytes("recurrence_source") == null
                                    ? null
                                    : UUIDHelper.BytesToUUID(rs.getBytes("recurrence_source")),
                            rs.getTimestamp("recurrence_id") == null
                                    ? null
                                    : rs.getTimestamp("recurrence_id")
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault()),
                            rs.getBytes("tag_uuid") == null
                                    ? null
                                    : UUIDHelper.BytesToUUID(rs.getBytes("tag_uuid"))
                    ));
                }
            }
        }

        return list;
    }
    public boolean exists(UUID userId, String title, ZonedDateTime start, ZonedDateTime end) {
        String sql = """
        SELECT 1 FROM calendar_events
        WHERE user_id = ? AND title = ? AND start_datetime = ? AND end_datetime = ?
        LIMIT 1
        """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBytes(1, UUIDHelper.uuidToBytes(userId));
            stmt.setString(2, title);
            stmt.setObject(3, start);
            stmt.setObject(4, end);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if any match found
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteById(UUID eventId) throws SQLException {
        String sql = "DELETE FROM calendar_events WHERE event_id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(eventId));
            ps.executeUpdate();
        }
    }

    public void deleteByUser(UUID userId) throws SQLException {
        String sql = "DELETE FROM calendar_events WHERE user_id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            ps.executeUpdate();
        }
    }
}
