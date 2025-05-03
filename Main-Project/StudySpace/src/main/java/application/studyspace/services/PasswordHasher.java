package application.studyspace.services;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

public class PasswordHasher {

    // Creates a random string (Salt)
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 random bytes
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hashes the password together with the salt
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256"); // Use SHA-256 algorithm
            sha.update((password + salt).getBytes()); // Combine password and salt
            byte[] hash = sha.digest(); // Hash it
            return Base64.getEncoder().encodeToString(hash); // Convert to readable text
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    // Saves the username, hashed password, and salt in the database
    public static void saveToDatabase(String username, String password) {
        String salt = generateSalt(); // Generate random salt
        String hashedPassword = hashPassword(password, salt); // Hash password + salt

        try {
            Connection connection = new DatabaseConnection().getConnection();
            String sql = "INSERT INTO user(username, password, salt) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, username);        // username
            statement.setString(2, hashedPassword);  // hashed password
            statement.setString(3, salt);            // salt

            int rows = statement.executeUpdate(); // Insert into DB

            if (rows > 0) {
                System.out.println("User saved successfully.");
            } else {
                System.out.println("Failed to save user.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

}
