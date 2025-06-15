package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.LoginChecker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        attemptLogin();
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent mouseEvent) {
        attemptLogin();
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (LoginChecker.checkLogin(email, password)) {
            try {
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Unable to load Landing Page").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Invalid email or password").showAndWait();
        }
    }

    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        try {
            ViewManager.show("/application/studyspace/auth/Register.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to load Register screen").showAndWait();
        }
    }
}
