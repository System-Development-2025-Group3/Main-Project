package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.*;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class AutoLoginHandler {

    public static boolean autoLoginIfPossible() {
        SessionManager session = SessionManager.getInstance();

        String savedUsername = session.getSavedUsername();
        String savedToken = session.getSavedToken();

        if (savedUsername == null || savedToken == null) {
            System.out.println("No saved credentials found. Not attempting to auto-login.");
            return false;
        }

        UUID userId = UUIDHelper.stringToUUID(savedUsername);
        String sql = "SELECT login_token FROM users WHERE user_id = ?";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String databaseToken = rs.getString("login_token");
                    if (savedToken.equals(databaseToken)) {
                        session.login(userId); // log in in-memory
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Activates auto-login for a given UUID by saving a token in preferences and database.
     */
    public static void activateAutoLogin(String uuidOfUser) {
        SessionManager session = SessionManager.getInstance();

        String token = ValidationUtils.generateToken().toString();
        session.saveLogin(uuidOfUser, token);

        String sql = "UPDATE users SET login_token = ?, timestamp_token = ? WHERE user_id = ?";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setBytes(3, uuidToBytes(UUIDHelper.stringToUUID(uuidOfUser)));

            ps.executeUpdate(); // Use executeUpdate for UPDATE/INSERT/DELETE

        } catch (SQLException e) {
            throw new RuntimeException("Failed to activate auto-login.", e);
        }
    }
}
