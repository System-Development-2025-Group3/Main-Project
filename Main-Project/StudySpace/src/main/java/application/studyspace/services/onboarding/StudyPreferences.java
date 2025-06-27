// ── src/main/java/application/studyspace/services/onboarding/StudyPreferences.java ──
package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

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
        try (Connection c = DataSourceManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
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

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
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

    public static boolean loadSkipSplashScreen(UUID userUUID) {
        String sql = "SELECT skip_splash_screen FROM study_preferences WHERE user_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBytes(1, uuidToBytes(userUUID));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("skip_splash_screen");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading skip splash screen: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Returns TRUE if user wants to SKIP splash, FALSE if should show splash
    public static boolean getSkipSplashScreenPreference(UUID userUUID) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT skip_splash_screen FROM study_preferences WHERE user_id = ?")) {
            stmt.setBytes(1, uuidToBytes(userUUID));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("skip_splash_screen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // default to show splash if not found or error
    }

    public static boolean updateSkipSplashScreen(UUID userUUID, boolean skip) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE study_preferences SET skip_splash_screen = ? WHERE user_id = ?")) {
            stmt.setBoolean(1, skip);
            stmt.setBytes(2, uuidToBytes(userUUID));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRememberMe(UUID userUUID, boolean rememberMe) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE study_preferences SET remember_me = ? WHERE user_id = ?")) {
            stmt.setBoolean(1, rememberMe);
            stmt.setBytes(2, uuidToBytes(userUUID));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadRememberMe(UUID userUUID) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT remember_me FROM study_preferences WHERE user_id = ?")) {
            stmt.setBytes(1, uuidToBytes(userUUID));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("remember_me");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
