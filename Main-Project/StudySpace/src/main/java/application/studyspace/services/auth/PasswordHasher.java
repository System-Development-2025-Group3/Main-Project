package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseConnection;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

/**
 * The PasswordHasher class provides utility methods for generating salts,
 * hashing passwords, and saving user credentials (email, hashed password, and salt)
 * to a database. It leverages secure mechanisms to ensure password protection,
 * such as generating random salts and hashing passwords using the SHA-256 algorithm.
 */
public class PasswordHasher {

    /**
     * Generates a random salt string using a secure random number generator.
     * The generated salt is encoded as a Base64 string for safe and compact storage.
     *
     * @return a randomly generated Base64-encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 random bytes
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using the SHA-256 algorithm combined with a salt. The method
     * concatenates the password and salt, applies the SHA-256 hashing algorithm, and
     * encodes the resulting hash in Base64 for readability.
     *
     * @param password the plain text password to be hashed
     * @param salt the salt value to enhance security by introducing randomness
     * @return the hashed password as a Base64-encoded string
     * @throws RuntimeException if an error occurs while hashing the password
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
     * Saves the user's email, hashed password, and generated salt into the database.
     *
     * @param email The email address of the user to be stored.
     * @param password The plaintext password provided by the user, which will be hashed and stored.
     */
    public static void saveToDatabase(String email, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        try {
            Connection connection = new DatabaseConnection().getConnection();
            String sql = "INSERT INTO users(email, password_hash, salt) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            statement.setString(3, salt);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Saving user: " + email + ", Password Hash: " + hashPassword(password, salt));
            } else {
                System.out.println("Failed to save user.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

}