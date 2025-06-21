// ── src/main/java/application/studyspace/services/onboarding/StudyPreferences.java ──
package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

public class StudyPreferences {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final UUID      userId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int       sessionLength;
    private final int       breakLength;
    private final String    blockedDays;    // e.g. "MONDAY,WEDNESDAY,FRIDAY"

    public StudyPreferences(
            UUID userId,
            String preferredTimeRange,
            int sessionLength,
            int breakLength,
            String blockedDays
    ) {
        this.userId        = userId;
        String[] parts     = preferredTimeRange.split("-");
        this.startTime     = LocalTime.parse(parts[0], FMT);
        this.endTime       = LocalTime.parse(parts[1], FMT);
        this.sessionLength = sessionLength;
        this.breakLength   = breakLength;
        this.blockedDays   = blockedDays == null ? "" : blockedDays;
    }

    // ─── Getters for algorithm ───────────────────────────────────

    public UUID       getUserId()        { return userId; }
    public LocalTime  getStartTime()     { return startTime; }
    public LocalTime  getEndTime()       { return endTime; }
    public int        getSessionLength() { return sessionLength; }
    public int        getBreakLength()   { return breakLength; }

    /** Days when the user does NOT want to study. */
    /** Days when the user does NOT want to study. */
    public Set<DayOfWeek> getBlockedDays() {
        if (blockedDays == null || blockedDays.isBlank()) return Collections.emptySet();
        return Arrays.stream(blockedDays.split("[,\\s]+"))
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }


    // ─── Load & Save ──────────────────────────────────────────────

    /** Load prefs from DB or throw if none found. */
    public static StudyPreferences load(UUID userId) throws SQLException {
        String sql = """
            SELECT start_time, end_time, session_length, break_length, blocked_days
              FROM study_preferences
             WHERE user_id = ?
        """;
        try (Connection c = new DatabaseConnection().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new IllegalStateException("No prefs for user " + userId);

                String range = rs.getTime("start_time").toLocalTime().format(FMT)
                        + "-"
                        + rs.getTime("end_time").toLocalTime().format(FMT);

                return new StudyPreferences(
                        userId,
                        range,
                        rs.getInt("session_length"),
                        rs.getInt("break_length"),
                        rs.getString("blocked_days")
                );
            }
        }
    }

    /** Inserts or updates this user’s study preferences in the database. */
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
            ps.setTime(2, Time.valueOf(startTime));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setInt(4, sessionLength);
            ps.setInt(5, breakLength);
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
