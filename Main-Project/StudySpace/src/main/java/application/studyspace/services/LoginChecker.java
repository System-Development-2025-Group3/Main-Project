package application.studyspace.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginChecker {

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

            String sql = "SELECT password_hash, salt FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, emailInput); // fill in the username

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String savedHash = result.getString("password_hash");
                String savedSalt = result.getString("salt");

                String newHash = PasswordHasher.hashPassword(passwordInput, savedSalt);

                if (newHash.equals(savedHash)) {
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
}
