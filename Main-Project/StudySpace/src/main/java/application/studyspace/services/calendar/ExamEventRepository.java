// ExamEventRepository.java
package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExamEventRepository {

    private static final Logger logger = Logger.getLogger(ExamEventRepository.class.getName());
    private static ExamEventRepository instance;

    private ExamEventRepository() {}

    public static synchronized ExamEventRepository getInstance() {
        if (instance == null) {
            instance = new ExamEventRepository();
        }
        return instance;
    }

    public void save(ExamEvent e) {
        String sql = """
            INSERT INTO exam_events (
              exam_id, user_id, title, subject, description, location,
              start_datetime, end_datetime, grade_weight, difficulty,
              number_of_topics, minutes_per_topic
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              title             = VALUES(title),
              subject           = VALUES(subject),
              description       = VALUES(description),
              location          = VALUES(location),
              start_datetime    = VALUES(start_datetime),
              end_datetime      = VALUES(end_datetime),
              grade_weight      = VALUES(grade_weight),
              difficulty        = VALUES(difficulty),
              number_of_topics  = VALUES(number_of_topics),
              minutes_per_topic = VALUES(minutes_per_topic)
            """;

        try (var conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1,  UUIDHelper.uuidToBytes(e.getId()));
            ps.setBytes(2,  UUIDHelper.uuidToBytes(e.getUserId()));
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getSubject());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getLocation());
            ps.setTimestamp(7, Timestamp.from(e.getStart().toInstant()));
            ps.setTimestamp(8, Timestamp.from(e.getEnd().toInstant()));
            ps.setDouble(9, e.getGradeWeight());
            ps.setInt(10, e.getDifficulty());
            ps.setInt(11, e.getNumberOfTopics());
            ps.setInt(12, e.getMinutesPerTopic());

            ps.executeUpdate();
            logger.info("Successfully saved ExamEvent: " + e.getId());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error saving ExamEvent: " + e.getId(), ex);
        }
    }
}
