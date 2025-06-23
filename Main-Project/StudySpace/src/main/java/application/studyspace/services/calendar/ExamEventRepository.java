package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository for saving and loading ExamEvent objects, now with calendarId support.
 */
public class ExamEventRepository {

    private static final Logger logger = Logger.getLogger(ExamEventRepository.class.getName());

    /**
     * Saves (or updates) an ExamEvent, including its calendarId.
     */
    public static void save(ExamEvent e) {
        String sql = """
            INSERT INTO exam_events (
              exam_id, calendar_id, user_id, title, description, location,
              start_datetime, grade_weight, difficulty, priority, hidden,
              number_of_topics, minutes_per_topic, total_study_minutes, end_datetime
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              calendar_id        = VALUES(calendar_id),
              title              = VALUES(title),
              description        = VALUES(description),
              location           = VALUES(location),
              start_datetime     = VALUES(start_datetime),
              grade_weight       = VALUES(grade_weight),
              difficulty         = VALUES(difficulty),
              priority           = VALUES(priority),
              hidden             = VALUES(hidden),
              number_of_topics   = VALUES(number_of_topics),
              minutes_per_topic  = VALUES(minutes_per_topic),
              total_study_minutes= VALUES(total_study_minutes),
              end_datetime       = VALUES(end_datetime)
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1,  UUIDHelper.uuidToBytes(e.getId()));
            ps.setBytes(2,  UUIDHelper.uuidToBytes(e.getCalendarId()));
            ps.setBytes(3,  UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setString(4, e.getTitle());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getLocation());
            ps.setTimestamp(7,  java.sql.Timestamp.from(e.getStart().toInstant()));
            ps.setDouble(8,  e.getGradeWeight());
            ps.setInt(9,    e.getDifficulty());
            ps.setInt(10,   0); // priority (not verwendet)
            ps.setBoolean(11,false); // hidden (not verwendet)
            ps.setInt(12,   e.getNumberOfTopics());
            ps.setInt(13,   e.getMinutesPerTopic());
            ps.setInt(14,   0); // total_study_minutes (not verwendet)
            ps.setTimestamp(15, java.sql.Timestamp.from(e.getEnd().toInstant()));

            ps.executeUpdate();
            logger.info("Successfully saved ExamEvent: " + e.getId());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error saving ExamEvent: " + e.getId(), ex);
        }
    }

    /**
     * Loads all ExamEvents belonging to the given calendar.
     */
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
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(calendarId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id       = UUIDHelper.BytesToUUID(rs.getBytes("exam_id"));
                    UUID userId   = UUIDHelper.BytesToUUID(rs.getBytes("user_id"));
                    String title  = rs.getString("title");
                    String desc   = rs.getString("description");
                    String loc    = rs.getString("location");
                    var start     = rs.getTimestamp("start_datetime")
                            .toInstant().atZone(ZoneId.systemDefault());
                    var end       = rs.getTimestamp("end_datetime")
                            .toInstant().atZone(ZoneId.systemDefault());
                    double weight = rs.getDouble("grade_weight");
                    int diff      = rs.getInt("difficulty");
                    int topics    = rs.getInt("number_of_topics");
                    int minsPt    = rs.getInt("minutes_per_topic");

                    ExamEvent exam = new ExamEvent(
                            id,
                            userId,
                            calendarId,
                            title,
                            desc,
                            loc,
                            start,
                            end,
                            weight,
                            diff,
                            topics,
                            minsPt
                    );
                    out.add(exam);
                }
            }
        }
        return out;
    }

    public void update(ExamEvent exam) throws SQLException {
        String sql = """
            UPDATE exam_events
               SET start_datetime = ?,
                   end_datetime   = ?,
                   location       = ?
             WHERE exam_id        = ?
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(exam.getStart().toInstant()));
            ps.setTimestamp(2, Timestamp.from(exam.getEnd().toInstant()));
            ps.setString(3, exam.getLocation());
            ps.setBytes(4, UUIDHelper.uuidToBytes(exam.getId()));

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No exam_event found with id " + exam.getId());
            }
        }
    }
}
