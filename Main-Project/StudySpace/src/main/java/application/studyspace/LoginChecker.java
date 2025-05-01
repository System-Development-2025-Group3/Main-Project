package application.studyspace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginChecker {

    // This method checks if the username and password are correct
    public static boolean checkLogin(String usernameInput, String passwordInput) {
        try {
            // Connect to the database
            Connection connection = new DatabaseConnection().getConnection();

            // Ask the database for the saved password and salt of the user
            String sql = "SELECT password, salt FROM user WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, usernameInput); // fill in the username

            // Run the database request
            ResultSet result = statement.executeQuery();

            // If the user was found
            if (result.next()) {
                // Get the saved hashed password and salt from the database
                String savedHash = result.getString("password");
                String savedSalt = result.getString("salt");

                // Hash the entered password again using the same salt
                String newHash = PasswordHasher.hashPassword(passwordInput, savedSalt);

                // Compare the two hashes
                if (newHash.equals(savedHash)) {
                    System.out.println("Login successful!");
                    return true;
                } else {
                    System.out.println("Wrong password.");
                    return false;
                }

            } else {
                // User was not found in the database
                System.out.println("User not found.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Show error if something goes wrong
            return false;
        }
    }
}
