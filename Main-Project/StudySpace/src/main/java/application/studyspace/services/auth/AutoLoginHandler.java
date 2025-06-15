package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class AutoLoginHandler {
    public static boolean AutoLoginIfPossible() {

        if (LoginSession.getSavedUsername() == null) {
            System.out.println("No saved username found. Not attempting to auto-login.");
            return false;
        }

        UUID userId = UUIDHelper.stringToUUID(LoginSession.getSavedUsername());
        String savedToken = LoginSession.getSavedToken();
        String databaseToken = null;

        if(savedToken == null) {
            System.out.println("No saved username or token found. Not attempting to auto-login.");
            return false;
        }

        String sql = """
            SELECT login_token
              FROM users
             WHERE user_id = ?
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));

            try (ResultSet rs = ps.executeQuery()) {

                if(rs.next()) {
                    databaseToken = rs.getString("login_token");
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (savedToken.equals(databaseToken)) {

            SessionManager.getInstance().setLoggedInUserId(userId);
            return true;
        }

        return false;
    }
}
