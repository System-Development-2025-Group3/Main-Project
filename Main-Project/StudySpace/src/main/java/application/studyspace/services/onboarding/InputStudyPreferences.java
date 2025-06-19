package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class InputStudyPreferences {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final UUID       userId;
    private final LocalTime  startTime;
    private final LocalTime  endTime;
    private final int        sessionLength;
    private final int        breakLength;
    private final String     blockedDays;

    /**
     * @param preferredTimeRange in the form "HH:mm-HH:mm", e.g. "08:00-20:00"
     */
    public InputStudyPreferences(UUID userId,
                                 String preferredTimeRange,
                                 int sessionLength,
                                 int breakLength,
                                 String blockedDays) {
        this.userId        = userId;
        // split and parse
        String[] parts     = preferredTimeRange.split("-");
        this.startTime     = LocalTime.parse(parts[0], FMT);
        this.endTime       = LocalTime.parse(parts[1], FMT);
        this.sessionLength = sessionLength;
        this.breakLength   = breakLength;
        this.blockedDays   = blockedDays;
    }

    /**
     * Inserts or updates this user’s study preferences in the database.
     */
    public boolean saveToDatabase() {
        String sql = """
            INSERT INTO study_preferences
              (user_id, start_time, end_time, session_length, break_length, blocked_days)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              start_time     = VALUES(start_time),
              end_time       = VALUES(end_time),
              session_length = VALUES(session_length),
              break_length   = VALUES(break_length),
              blocked_days   = VALUES(blocked_days)
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            ps.setTime(  2, Time.valueOf(startTime));
            ps.setTime(  3, Time.valueOf(endTime));
            ps.setInt(   4, sessionLength);
            ps.setInt(   5, breakLength);
            ps.setString(6, blockedDays != null ? blockedDays : "");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to save study preferences");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
}
