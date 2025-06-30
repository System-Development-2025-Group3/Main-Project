package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

/**
 * Represents a user's study preferences.
 *
 * Preferences include:
 * - preferred time window (start and end time)
 * - session length in minutes
 * - break length in minutes
 * - blocked days when the user does not want to study
 * - other flags (skip splash screen, remember me)
 */
public class StudyPreferences {

    /** Formatter to store and parse time strings like "08:00" */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final UUID userId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int sessionLength;
    private final int breakLength;
    private final String blockedDays; // Comma-separated e.g. "MONDAY,FRIDAY"

    /**
     * Constructs a StudyPreferences object from raw parameters.
     *
     * @param userId            the user this preference belongs to
     * @param preferredTimeRange string like "08:00-18:00"
     * @param sessionLength     study session length (minutes)
     * @param breakLength       break length (minutes)
     * @param blockedDays       days the user wants blocked
     */
    public StudyPreferences(
            UUID userId,
            String preferredTimeRange,
            int sessionLength,
            int breakLength,
            String blockedDays
    ) {
        this.userId = userId;
        String[] parts = preferredTimeRange.split("-");
        this.startTime = LocalTime.parse(parts[0], FMT);
        this.endTime = LocalTime.parse(parts[1], FMT);
        this.sessionLength = sessionLength;
        this.breakLength = breakLength;
        this.blockedDays = blockedDays == null ? "" : blockedDays;
    }

    // --- Basic Getters ---

    public UUID getUserId() { return userId; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getSessionLength() { return sessionLength; }
    public int getBreakLength() { return breakLength; }

    /**
     * Converts the CSV blocked days string to a Set of DayOfWeek enums.
     */
    public Set<DayOfWeek> getBlockedDays() {
        if (blockedDays == null || blockedDays.isBlank()) return Collections.emptySet();
        return Arrays.stream(blockedDays.split("[,\\s]+"))
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }

    // --- Database Operations ---

    /**
     * Loads study preferences for the given user ID.
     * Throws IllegalStateException if no preferences are stored.
     */
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

                // Build time range string like "08:00-18:00"
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

    /**
     * Inserts or updates the preferences in the database.
     *
     * @return true if at least 1 row was affected (insert or update)
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

    /**
     * Loads the "skip splash screen" flag.
     * This method and getSkipSplashScreenPreference() do almost the same thing.
     */
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

    /**
     * Returns true if the user prefers to skip the splash screen.
     * Defaults to false if no record is found or if error occurs.
     */
    public static boolean getSkipSplashScreenPreference(UUID userUUID) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT skip_splash_screen FROM study_preferences WHERE user_id = ?"
             )) {
            stmt.setBytes(1, uuidToBytes(userUUID));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("skip_splash_screen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // default fallback
    }

    /**
     * Updates the skip_splash_screen flag in DB.
     */
    public static boolean updateSkipSplashScreen(UUID userUUID, boolean skip) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE study_preferences SET skip_splash_screen = ? WHERE user_id = ?"
             )) {
            stmt.setBoolean(1, skip);
            stmt.setBytes(2, uuidToBytes(userUUID));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the remember_me flag in DB.
     */
    public static boolean updateRememberMe(UUID userUUID, boolean rememberMe) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE study_preferences SET remember_me = ? WHERE user_id = ?"
             )) {
            stmt.setBoolean(1, rememberMe);
            stmt.setBytes(2, uuidToBytes(userUUID));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads the remember_me flag.
     * Defaults to false if not found or on error.
     */
    public static boolean loadRememberMe(UUID userUUID) {
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT remember_me FROM study_preferences WHERE user_id = ?"
             )) {
            stmt.setBytes(1, uuidToBytes(userUUID));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBoolean("remember_me");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
