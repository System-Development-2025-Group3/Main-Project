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

    /**
     * Activates the auto-login mechanism for the specified user. This method generates
     * a unique token if the "stay logged in" option is selected and stores the token
     * in both the session and the database for future login validation. The token is
     * accompanied by a timestamp to track its creation time. If an error occurs during
     * the database operation, a runtime exception is thrown.
     *
     * @param uuidOfUser The unique identifier of the user for whom the auto-login process
     *                   is being activated.
     */
    public static void activateAutoLogin(String uuidOfUser) {

        String token = ValidationUtils.generateToken().toString();
        LoginSession.saveLogin(uuidOfUser, token);

        String sql = """
        UPDATE users
           SET login_token = ?, timestamp_token = ?
           WHERE user_id = ?;""";

        System.out.println("!!!!!!! " + token);
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setBytes(3, uuidToBytes(UUIDHelper.stringToUUID(uuidOfUser)));

            ps.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
