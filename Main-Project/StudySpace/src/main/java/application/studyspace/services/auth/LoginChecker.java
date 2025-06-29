package application.studyspace.services.auth;


import application.studyspace.services.DataBase.DataSourceManager;
import application.studyspace.services.DataBase.DatabaseConnection;


import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


import static application.studyspace.services.DataBase.UUIDHelper.BytesToUUID;


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
            Connection connection = DataSourceManager.getConnection();


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
                    loggedInUserUUID = BytesToUUID(uuidBytes);


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


    public static boolean autoLoginIfPossible() {
        String savedUsername = SessionManager.getInstance().getSavedUsername();
        String token = SessionManager.getInstance().getSavedToken();


        if (savedUsername != null && token != null) {


            return ValidationUtils.validateToken(savedUsername, token);
        }
        return false;
    }


}



