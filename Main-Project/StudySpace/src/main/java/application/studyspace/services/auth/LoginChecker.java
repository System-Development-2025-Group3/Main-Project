package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LoginChecker {

    private static UUID loggedInUserUUID;

    /**
     * Verifies the login credentials by comparing the given email and password
     * with stored data in the database. The password input is hashed with the
     * stored salt and then compared with the stored hashed password.
     *
     * @param emailInput the email address provided by the user
     * @param passwordInput the password provided by the user
     * @return true if the email and password match an entry in the database, false otherwise
     */

    public static boolean checkLogin(String emailInput, String passwordInput) {
        try {
            Connection connection = new DatabaseConnection().getConnection();

            String sql = "SELECT user_id, password_hash, salt FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, emailInput); // fill in the username

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String savedHash = result.getString("password_hash");
                String savedSalt = result.getString("salt");

                String newHash = PasswordHasher.hashPassword(passwordInput, savedSalt);

                if (newHash.equals(savedHash)) {
                    byte[] uuidBytes = result.getBytes("user_id");
                    loggedInUserUUID = convertBytesToUUID(uuidBytes);
                    System.out.println("Login successful!");
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
     * Retrieves the universally unique identifier (UUID) of the currently logged-in user.
     * This UUID is associated with the user session and is expected to be set when a user logs in.
     * If no user is logged in, an IllegalStateException will be thrown.
     *
     * @return the UUID of the currently logged-in user
     * @throws IllegalStateException if no user is currently logged in
     */
    public static UUID getLoggedInUserUUID() {
        if (loggedInUserUUID == null) {
            throw new IllegalStateException("No user is currently logged in."); // Handle case where no user is logged in
        }
        return loggedInUserUUID;
    }

    /**
     * Converts a byte array (BINARY(16) format) into a UUID.
     *
     * @param bytes the byte array representing the UUID
     * @return the UUID object
     */
    private static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }


}
