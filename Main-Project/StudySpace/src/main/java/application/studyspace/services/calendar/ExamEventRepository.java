package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ExamEventRepository {

    /** Maps a ResultSet row into an ExamEvent. */
    private static ExamEvent mapRow(ResultSet rs) throws SQLException {
        UUID id         = UUIDHelper.BytesToUUID(rs.getBytes("exam_id"));
        UUID userId     = UUIDHelper.BytesToUUID(rs.getBytes("user_id"));
        UUID calId      = UUIDHelper.BytesToUUID(rs.getBytes("calendar_id"));
        String title    = rs.getString("title");
        String desc     = rs.getString("description");
        String loc      = rs.getString("location");

        ZonedDateTime start = rs.getTimestamp("start_datetime")
                .toInstant().atZone(ZoneId.systemDefault());
        ZonedDateTime end   = rs.getTimestamp("end_datetime")
                .toInstant().atZone(ZoneId.systemDefault());

        double weight = rs.getDouble("grade_weight");
        int diff      = rs.getInt("difficulty");
        int topics    = rs.getInt("number_of_topics");
        int minsPt    = rs.getInt("minutes_per_topic");

        ExamEvent exam = new ExamEvent(
                id, userId, calId, title, desc, loc,
                start, end, weight, diff, topics, minsPt
        );
        exam.setCalendarId(calId);
        return exam;
    }

    /** Single-calendar loader (unchanged). */
    public static List<ExamEvent> findByCalendarId(UUID calendarId) throws SQLException {
        String sql = """
            SELECT 
              exam_id, user_id, title, description, location,
              start_datetime, end_datetime, grade_weight, difficulty,
              number_of_topics, minutes_per_topic, calendar_id
            FROM exam_events
            WHERE calendar_id = ?
        """;

        List<ExamEvent> out = new ArrayList<>();
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(calendarId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        }
        return out;
    }

    /**
     * Batched loader: loads all ExamEvents for any of the given calendar IDs
     * in one query and groups them by calendar.
     */
    public static Map<UUID, List<ExamEvent>> findByCalendarIds(List<UUID> calIds) throws SQLException {
        if (calIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = calIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM exam_events WHERE calendar_id IN (" + placeholders + ")";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < calIds.size(); i++) {
                ps.setBytes(i + 1, UUIDHelper.uuidToBytes(calIds.get(i)));
            }

            ResultSet rs = ps.executeQuery();
            Map<UUID, List<ExamEvent>> map = new HashMap<>();
            while (rs.next()) {
                ExamEvent ex = mapRow(rs);
                map.computeIfAbsent(ex.getCalendarId(), k -> new ArrayList<>())
                        .add(ex);
            }
            return map;
        }
    }

    /**
     * Deletes an ExamEvent from the database by its UUID.
     */
    public static void delete(UUID examId) throws SQLException {
        String sql = "DELETE FROM exam_events WHERE exam_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(examId));
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No exam event found with id " + examId);
            }
        }
    }

    public static void deleteExamAndSessions(UUID examId) throws SQLException {
        // 1. Find the calendar ID for this exam
        UUID calendarId = null;
        String findCalendarSql = "SELECT calendar_id FROM exam_events WHERE exam_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(findCalendarSql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(examId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    calendarId = UUIDHelper.BytesToUUID(rs.getBytes("calendar_id"));
                }
            }
        }

        if (calendarId == null) {
            throw new SQLException("No calendar found for examId " + examId);
        }

        // 2. Delete the ExamEvent itself
        String deleteExamSql = "DELETE FROM exam_events WHERE exam_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteExamSql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(examId));
            ps.executeUpdate();
        }

        // 3. Delete all CalendarEvents in this calendar
        String deleteEventsSql = "DELETE FROM calendar_events WHERE calendar_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteEventsSql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(calendarId));
            ps.executeUpdate();
        }

        // 4. Delete the calendar itself
        String deleteCalendarSql = "DELETE FROM calendars WHERE calendar_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteCalendarSql)) {
            ps.setBytes(1, UUIDHelper.uuidToBytes(calendarId));
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a new ExamEvent or updates it if it already exists (by exam_id).
     */
    public static void save(ExamEvent e) throws SQLException {
        String sql = """
            INSERT INTO exam_events (
                exam_id, user_id, calendar_id, title, description, location,
                start_datetime, end_datetime, grade_weight,
                number_of_topics, minutes_per_topic
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                calendar_id = VALUES(calendar_id),
                title = VALUES(title),
                description = VALUES(description),
                location = VALUES(location),
                start_datetime = VALUES(start_datetime),
                end_datetime = VALUES(end_datetime),
                grade_weight = VALUES(grade_weight),
                number_of_topics = VALUES(number_of_topics),
                minutes_per_topic = VALUES(minutes_per_topic)
        """;

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(e.getId()));
            ps.setBytes(2, UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setBytes(3, UUIDHelper.uuidToBytes(e.getCalendarId()));
            ps.setString(4, e.getTitle());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getLocation());
            ps.setTimestamp(7, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(8, Timestamp.from(e.getEnd().toInstant()));
            ps.setDouble(9, e.getGradeWeight());
            ps.setInt(10, e.getNumberOfTopics());
            ps.setInt(11, e.getMinutesPerTopic());
            ps.executeUpdate();
        }
    }

    /**
     * Updates an existing ExamEvent in the database.
     */
    public void update(ExamEvent e) throws SQLException {
        String sql = """
            UPDATE exam_events SET
                user_id = ?,
                calendar_id = ?,
                title = ?,
                description = ?,
                location = ?,
                start_datetime = ?,
                end_datetime = ?,
                grade_weight = ?,
                number_of_topics = ?,
                minutes_per_topic = ?
            WHERE exam_id = ?
        """;

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setBytes(2, UUIDHelper.uuidToBytes(e.getCalendarId()));
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getLocation());
            ps.setTimestamp(6, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(7, Timestamp.from(e.getEnd().toInstant()));
            ps.setDouble(8, e.getGradeWeight());
            ps.setInt(9, e.getNumberOfTopics());
            ps.setInt(10, e.getMinutesPerTopic());
            ps.setBytes(11, UUIDHelper.uuidToBytes(e.getId()));
            ps.executeUpdate();
        }
    }
}
