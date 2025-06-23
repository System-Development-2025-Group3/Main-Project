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

    /** Save/update (unchanged). */
    public static void save(ExamEvent e) throws SQLException { /* … */ }

    /** Update single exam (unchanged). */
    public void update(ExamEvent exam) throws SQLException { /* … */ }
}
