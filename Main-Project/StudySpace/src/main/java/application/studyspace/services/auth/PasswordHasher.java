package application.studyspace.services.auth;

// Imports for cryptography, DB access, UUID handling
import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

/**
 * Provides methods to hash passwords with salt and save or update them in the database.
 */
public class PasswordHasher {

    /**
     * Generates a random salt encoded in Base64.
     *
     * @return a new random salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password concatenated with a salt using SHA-256.
     *
     * @param password The plaintext password
     * @param salt     The salt string
     * @return Base64-encoded hash
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update((password + salt).getBytes());
            byte[] hash = sha.digest();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    /**
     * Saves a new user to the database.
     * Generates a salt, hashes the password, creates a UUID, and stores all values.
     *
     * @param email    The user's email
     * @param password The plaintext password
     * @return the generated UUID if successful, or null on failure
     */
    public static UUID saveToDatabase(String email, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        UUID newId = UUID.randomUUID();

        String sql = "INSERT INTO users(user_id, email, password_hash, salt) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(newId));
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.setString(4, salt);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("User saved: " + email + " (ID: " + newId + ")");
                return newId;
            } else {
                System.err.println("Failed to save user: " + email);
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error saving user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates the password for an existing user by email.
     * Generates a new salt and hashed password.
     *
     * @param email       The user's email
     * @param newPassword The new plaintext password
     * @return true if updated successfully, false otherwise
     */
    public static boolean updatePassword(String email, String newPassword) {
        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            System.err.println("Email or password cannot be null or empty.");
            return false;
        }

        String salt = generateSalt();
        String hashedPassword = hashPassword(newPassword, salt);

        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE email = ?";

        try (Connection connection = DataSourceManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setString(2, salt);
            ps.setString(3, email);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Successfully updated password for user: " + email);
                return true;
            } else {
                System.err.println("Failed to update password. User not found with email: " + email);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating password due to a database issue: " + e.getMessage());
            return false;
        }
    }

}
