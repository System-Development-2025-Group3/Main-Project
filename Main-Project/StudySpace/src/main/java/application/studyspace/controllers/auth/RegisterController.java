package application.studyspace.controllers.auth;

import application.studyspace.services.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import static application.studyspace.services.PasswordHasher.saveToDatabase;

public class RegisterController {

    @FXML
    private TextField RegisterEmailField;

    @FXML
    private PasswordField RegisterPassword_1;

    @FXML
    private PasswordField RegisterPassword_2;

    @FXML
    private void handleBackToLoginClick(ActionEvent event) {
        SceneSwitcher.switchTo((Node) event.getSource(), "/application/studyspace/Login.fxml", "Login");
    }

    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        if (RegisterEmailField.getText().isEmpty() || RegisterPassword_1.getText().isEmpty() || RegisterPassword_2.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please fill in all fields!");
            alert.setContentText("Please fill in all fields!");
            alert.showAndWait();
        } else if (!RegisterPassword_1.getText().equals(RegisterPassword_2.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Passwords do not match!"); // Add header text
            alert.setContentText("Please make sure both password fields match.");
            alert.showAndWait();
} else {
            saveToDatabase(RegisterEmailField.getText(), RegisterPassword_1.getText());
            try {
    SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Landing-Page.fxml", "Landing Page");
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText("Failed to navigate to the next page.");
                alert.setContentText("Please contact support.");
                alert.showAndWait();
            }
        }




    }
}