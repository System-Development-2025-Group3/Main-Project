package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

public class PasswordHasher {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

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
     * Generates a new UUID, hashes the given password+salt, inserts
     * (user_id, email, password_hash, salt) into the users table,
     * and returns the generated UUID (or null on failure).
     *
     * @param email    The user's email
     * @param password The plaintext password
     * @return the new user's UUID if saved successfully, or null on error
     */
    public static UUID saveToDatabase(String email, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        UUID newId = UUID.randomUUID();

        String sql = "INSERT INTO users(user_id, email, password_hash, salt) VALUES (?, ?, ?, ?)";

        try (Connection connection = new DatabaseConnection().getConnection();
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
}
