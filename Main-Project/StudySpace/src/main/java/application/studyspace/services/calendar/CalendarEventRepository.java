package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository for saving, loading, and de-duplicating CalendarEvent objects.
 */
public class CalendarEventRepository {

    /**
     * Save or update a CalendarEvent, including its calendarId.
     */
    public static void save(CalendarEvent e) throws SQLException {
        String sql = """
            INSERT INTO calendar_events (
              event_id, calendar_id, user_id, title, description, location,
              start_datetime, end_datetime, full_day, hidden,
              min_duration, recurrence_rule, recurrence_source,
              recurrence_id, tag_uuid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              calendar_id      = VALUES(calendar_id),
              title            = VALUES(title),
              description      = VALUES(description),
              location         = VALUES(location),
              start_datetime   = VALUES(start_datetime),
              end_datetime     = VALUES(end_datetime),
              full_day         = VALUES(full_day),
              hidden           = VALUES(hidden),
              min_duration     = VALUES(min_duration),
              recurrence_rule  = VALUES(recurrence_rule),
              recurrence_source= VALUES(recurrence_source),
              recurrence_id    = VALUES(recurrence_id),
              tag_uuid         = VALUES(tag_uuid)
            """;

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1,  UUIDHelper.uuidToBytes(e.getId()));
            ps.setBytes(2,  UUIDHelper.uuidToBytes(e.getCalendarId()));
            ps.setBytes(3,  UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setString(4, e.getTitle());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getLocation());
            ps.setTimestamp(7, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(8, Timestamp.from(e.getEnd().toInstant()));
            ps.setBoolean(9, e.isFullDay());
            ps.setBoolean(10, e.isHidden());
            ps.setObject(11, e.getMinDuration() == null ? null : e.getMinDuration().toMinutes());
            ps.setString(12, e.getRecurrenceRule());
            ps.setBytes(13, e.getRecurrenceSource() == null ? null : UUIDHelper.uuidToBytes(e.getRecurrenceSource()));
            ps.setTimestamp(14, e.getRecurrenceId() == null ? null : Timestamp.from(e.getRecurrenceId().toInstant()));
            ps.setBytes(15, e.getTagUuid() == null ? null : UUIDHelper.uuidToBytes(e.getTagUuid()));
            ps.executeUpdate();
        }
    }

    /**
     * Delete a CalendarEvent by its UUID.
     */
    public static void delete(UUID eventId) throws SQLException {
        String sql = "DELETE FROM calendar_events WHERE event_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(eventId));
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No calendar event found with id " + eventId);
            }
        }
    }

    /**
     * Check if an event already exists to avoid duplicates.
     */
    public boolean exists(UUID userId, String title, ZonedDateTime start, ZonedDateTime end) throws SQLException {
        String sql = """
            SELECT 1 FROM calendar_events
             WHERE user_id = ? AND title = ? AND start_datetime = ? AND end_datetime = ?
             LIMIT 1
        """;
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            ps.setString(2, title);
            ps.setTimestamp(3, Timestamp.from(start.toInstant()));
            ps.setTimestamp(4, Timestamp.from(end.toInstant()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Load all events for a given calendar.
     */
    public static List<CalendarEvent> findByCalendarId(UUID calId) throws SQLException {
        return loadByClause("calendar_id = ?", UUIDHelper.uuidToBytes(calId));
    }

    /**
     * Load all events for a given user (across all their calendars).
     */
    public List<CalendarEvent> findByUser(UUID userId) throws SQLException {
        return loadByClause("user_id = ?", UUIDHelper.uuidToBytes(userId));
    }

    // Shared loader to avoid duplication
    private static List<CalendarEvent> loadByClause(String where, byte[] param) throws SQLException {
        String sql = "SELECT * FROM calendar_events WHERE " + where;
        List<CalendarEvent> list = new ArrayList<>();
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Batch-load events for multiple calendars in one query.
     */
    public static Map<UUID,List<CalendarEvent>> findByCalendarIds(List<UUID> calIds) throws SQLException {
        if (calIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = calIds.stream()
                .map(u -> "?")
                .collect(Collectors.joining(","));
        String sql = "SELECT * FROM calendar_events WHERE calendar_id IN (" + placeholders + ")";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < calIds.size(); i++) {
                ps.setBytes(i + 1, UUIDHelper.uuidToBytes(calIds.get(i)));
            }

            ResultSet rs = ps.executeQuery();
            Map<UUID,List<CalendarEvent>> map = new HashMap<>();
            while (rs.next()) {
                CalendarEvent e = mapRow(rs);
                map.computeIfAbsent(e.getCalendarId(), k -> new ArrayList<>()).add(e);
            }
            return map;
        }
    }

    /**
     * Update an existing CalendarEvent in the DB.
     */
    public void update(CalendarEvent e) throws SQLException {
        String sql = """
        UPDATE calendar_events
           SET calendar_id      = ?,
               user_id          = ?,
               title            = ?,
               description      = ?,
               location         = ?,
               start_datetime   = ?,
               end_datetime     = ?,
               full_day         = ?,
               hidden           = ?,
               min_duration     = ?,
               recurrence_rule  = ?,
               recurrence_source= ?,
               recurrence_id    = ?,
               tag_uuid         = ?
         WHERE event_id        = ?
        """;

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1,  UUIDHelper.uuidToBytes(e.getCalendarId()));
            ps.setBytes(2,  UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getLocation());
            ps.setTimestamp(6, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(7, Timestamp.from(e.getEnd().toInstant()));
            ps.setBoolean(8, e.isFullDay());
            ps.setBoolean(9, e.isHidden());
            ps.setObject(10, e.getMinDuration() == null ? null : (int) e.getMinDuration().toMinutes());
            ps.setString(11, e.getRecurrenceRule());
            ps.setBytes(12, e.getRecurrenceSource() == null ? null : UUIDHelper.uuidToBytes(e.getRecurrenceSource()));
            ps.setTimestamp(13, e.getRecurrenceId() == null
                    ? null
                    : Timestamp.from(e.getRecurrenceId().toInstant()));
            ps.setBytes(14, e.getTagUuid() == null ? null : UUIDHelper.uuidToBytes(e.getTagUuid()));
            ps.setBytes(15, UUIDHelper.uuidToBytes(e.getId()));

            ps.executeUpdate();
        }
    }

    /**
     * Helper to map a JDBC row to a CalendarEvent.
     */
    private static CalendarEvent mapRow(ResultSet rs) throws SQLException {
        UUID id      = UUIDHelper.BytesToUUID(rs.getBytes("event_id"));
        UUID userId  = UUIDHelper.BytesToUUID(rs.getBytes("user_id"));
        UUID calId   = UUIDHelper.BytesToUUID(rs.getBytes("calendar_id"));
        String title = rs.getString("title");
        String desc  = rs.getString("description");
        String loc   = rs.getString("location");

        ZonedDateTime start = rs.getTimestamp("start_datetime")
                .toInstant().atZone(ZoneId.systemDefault());
        ZonedDateTime end   = rs.getTimestamp("end_datetime")
                .toInstant().atZone(ZoneId.systemDefault());
        boolean fullDay = rs.getBoolean("full_day");
        boolean hidden  = rs.getBoolean("hidden");

        Duration minDur = rs.getObject("min_duration", Integer.class) == null
                ? null
                : Duration.ofMinutes(rs.getInt("min_duration"));

        String recurRule = rs.getString("recurrence_rule");
        UUID   recurSrc  = rs.getBytes("recurrence_source") == null
                ? null
                : UUIDHelper.BytesToUUID(rs.getBytes("recurrence_source"));
        ZonedDateTime recurId = rs.getTimestamp("recurrence_id") == null
                ? null
                : rs.getTimestamp("recurrence_id")
                .toInstant().atZone(ZoneId.systemDefault());
        UUID tagUuid = rs.getBytes("tag_uuid") == null
                ? null
                : UUIDHelper.BytesToUUID(rs.getBytes("tag_uuid"));

        CalendarEvent e = new CalendarEvent(
                id, userId, title, desc, loc,
                start, end, fullDay, hidden,
                minDur, recurRule, recurSrc, recurId, tagUuid
        );
        e.setCalendarId(calId);
        return e;
    }
}
