package application.studyspace.services.auth;

// Imports for DB operations and UUID handling
import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.BytesToUUID;

/**
 * Provides methods to verify user credentials and manage login sessions.
 */
public class LoginChecker {

    // Stores the UUID of the user who successfully logged in
    private static UUID loggedInUserUUID;

    /**
     * Verifies the login credentials by comparing the provided email and password
     * with stored values in the database.
     *
     * @param emailInput The email address input by the user
     * @param passwordInput The plaintext password input by the user
     * @return true if login is successful, false otherwise
     */
    public static boolean checkLogin(String emailInput, String passwordInput) {
        try {
            // Obtain a database connection
            Connection connection = DataSourceManager.getConnection();

            // Prepare query to fetch hashed password and salt for the email
            String sql = "SELECT user_id, password_hash, salt FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, emailInput);

            // Execute query
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                // Retrieve stored hash and salt
                String savedHash = result.getString("password_hash");
                String savedSalt = result.getString("salt");

                // Hash the input password with stored salt
                String newHash = PasswordHasher.hashPassword(passwordInput, savedSalt);

                // Compare hashes
                if (newHash.equals(savedHash)) {
                    // Convert byte[] UUID to UUID object
                    byte[] uuidBytes = result.getBytes("user_id");
                    loggedInUserUUID = BytesToUUID(uuidBytes);

                    // Log the user in via SessionManager
                    SessionManager.getInstance().login(loggedInUserUUID);
                    System.out.println("Login successful! Session for user " + loggedInUserUUID + " created.");
                    return true;

                } else {
                    System.out.println("Wrong password.");
                    return false;
                }

            } else {
                System.out.println("User not found.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Attempts to log in automatically if a saved username and token exist.
     *
     * @return true if auto-login succeeds, false otherwise
     */
    public static boolean autoLoginIfPossible() {
        String savedUsername = SessionManager.getInstance().getSavedUsername();
        String token = SessionManager.getInstance().getSavedToken();

        if (savedUsername != null && token != null) {
            // Validate saved credentials
            return ValidationUtils.validateToken(savedUsername, token);
        }
        return false;
    }

}
