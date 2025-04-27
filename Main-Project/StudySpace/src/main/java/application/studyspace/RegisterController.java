package application.studyspace;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private DatabaseConnection databaseConnection = new DatabaseConnection();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        // Insert into DB
        try {
            Connection connectDB = databaseConnection.getConnection();
            if (connectDB == null) {
                messageLabel.setText("Database connection failed.");
                return;
            }
            String insertQuery = "INSERT INTO user(username, password) VALUES(?, ?)";

            PreparedStatement preparedStatement = connectDB.prepareStatement(insertQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password); // Passwort könnte man noch hashen, optional.

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                messageLabel.setText("Registration successful!");
                messageLabel.setStyle("-fx-text-fill: green;");
            } else {
                messageLabel.setText("Registration failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Error: " + e.getMessage());
        }
    }
}
