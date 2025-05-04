package application.studyspace.controllers.auth;

import application.studyspace.services.LoginChecker;
import application.studyspace.services.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;

    // Handle register link click
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) {
        if (InputEmailTextfield.getText().isEmpty() || InputPassword.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please fill in all fields!");
            alert.setContentText("Please fill in all fields!");
            alert.showAndWait();
            System.out.println("The user did not fill in all fields.");
        } else if (!LoginChecker.checkLogin(InputEmailTextfield.getText(), InputPassword.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Credentials do not match!");
            alert.setContentText("Please check your email and password and try again.");
            alert.showAndWait();
            System.out.println("The user did not enter correct credentials.");
        } else {
            SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Landing-Page.fxml", "Landing-Page");
        }
    }
}
