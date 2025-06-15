package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class InputStudyDays {

    private final UUID   userId;
    private final String preferredTime;
    private final int    sessionLength;
    private final int    breakLength;
    private final String blockedDays;

    public InputStudyDays(UUID userId,
                          String preferredTime,
                          int sessionLength,
                          int breakLength,
                          String blockedDays) {
        this.userId         = userId;
        this.preferredTime  = preferredTime;
        this.sessionLength  = sessionLength;
        this.breakLength    = breakLength;
        this.blockedDays    = blockedDays;
    }

    /**
     * Inserts or updates this user’s study preferences in the database.
     */
    public boolean saveToDatabase() {
        String sql = """
        INSERT INTO study_preferences
          (user_id, preferred_time, session_length, break_length, blocked_days)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
          preferred_time  = VALUES(preferred_time),
          session_length  = VALUES(session_length),
          break_length    = VALUES(break_length),
          blocked_days    = VALUES(blocked_days)
    """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            ps.setString(2, preferredTime != null ? preferredTime : "Morning");
            ps.setInt(3, sessionLength);
            ps.setInt(4, breakLength);
            ps.setString(5, blockedDays != null ? blockedDays : "");

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to save study preferences");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

}
