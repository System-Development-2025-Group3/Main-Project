package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class InputStudyDays {

    private final UUID userUUID;
    private final String preferredTime;
    private final String dailyLimit;
    private final String sessionLimit;
    private final String breakLength;
    private final String focusTime;
    private final String unavailableDays;
    private final String freeDays;
    private final String concentrationTime;
    private final String sessionType;

    public InputStudyDays(UUID userUUID,
                          String preferredTime,
                          String sessionLength,
                          String breakLength,
                          String blockedDays) {
        this.userUUID = userUUID;
        this.preferredTime = preferredTime;
        this.dailyLimit = null;
        this.sessionLimit = sessionLength;
        this.breakLength = breakLength;
        this.focusTime = null;
        this.unavailableDays = blockedDays;
        this.freeDays = null;
        this.concentrationTime = null;
        this.sessionType = null;
    }

    public boolean saveToDatabase() {
        String insertQuery = """
            INSERT INTO Study_Preferences (
                userUUID, preferred_time, daily_limit, session_limit, break_length,
                focus_time, unavailable_days, free_days, concentration_time, session_type
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            byte[] uuidBytes = new DatabaseHelper().uuidToBytes(userUUID);
            stmt.setBytes(1, uuidBytes);
            stmt.setString(2, preferredTime);
            stmt.setString(3, dailyLimit);
            stmt.setString(4, sessionLimit);
            stmt.setString(5, breakLength);
            stmt.setString(6, focusTime);
            stmt.setString(7, unavailableDays);
            stmt.setString(8, freeDays);
            stmt.setString(9, concentrationTime);
            stmt.setString(10, sessionType);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
