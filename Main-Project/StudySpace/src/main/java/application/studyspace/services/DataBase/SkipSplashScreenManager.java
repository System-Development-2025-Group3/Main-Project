package application.studyspace.services.DataBase;

import application.studyspace.services.DataBase.DatabaseConnection;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SkipSplashScreenManager {

    /**
     * Updates the "skip_splash_screen" column in the database for a given user.
     *
     * @param userUUID The UUID of the user.
     * @param skipValue The boolean value to set (1 for true, 0 for false).
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateSkipSplashScreen(UUID userUUID, boolean skipValue) {
        String sql = "UPDATE users SET SkipSplashScreen = ? WHERE user_id = ?";
        int valueToSet = skipValue ? 1 : 0;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, valueToSet);
            stmt.setBytes(2, uuidToBytes(userUUID));

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating skip_splash_screen value: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads the "skip_splash_screen" preference from the database for a given user.
     *
     * @param userUUID The UUID of the user.
     * @return The value of the "skip_splash_screen" setting (true or false).
     */
    public boolean loadSkipSplashScreenFromDatabase(UUID userUUID) {
        String sql = "SELECT skip_splash_screen FROM users WHERE user_id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBytes(1, uuidToBytes(userUUID));

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("skip_splash_screen") == 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading skip splash screen preference from database: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Default to false if the query fails
    }


    /**
     * Converts a UUID to a 16-byte array for storing in a BINARY(16) database field.
     *
     * @param uuid the UUID to convert.
     * @return the byte array representing the UUID.
     */
    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}