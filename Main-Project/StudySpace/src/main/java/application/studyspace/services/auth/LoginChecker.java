package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LoginChecker {

    /**
     * Verifies the login credentials. If valid, logs the user in via SessionManager.
     *
     * @param email    The user's email
     * @param password The user's plaintext password
     * @return true if login succeeds, false otherwise
     */
    public static boolean checkLogin(String email, String password) {
        String sql = "SELECT user_id, password_hash, salt FROM users WHERE email = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String savedHash = rs.getString("password_hash");
                    String savedSalt = rs.getString("salt");
                    String hashed     = PasswordHasher.hashPassword(password, savedSalt);

                    if (hashed.equals(savedHash)) {
                        UUID userId = UUIDHelper.BytesToUUID(rs.getBytes("user_id"));
                        SessionManager.getInstance().login(userId);
                        System.out.println("Login successful for user " + userId);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Login failed for email " + email);
        return false;
    }
}
