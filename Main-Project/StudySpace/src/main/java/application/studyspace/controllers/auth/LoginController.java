package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.LoginChecker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML private TextField    emailField;
    @FXML private PasswordField passwordField;

    /**
     * Called when the user clicks the login button’s *action* (if you ever hook up onAction).
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        attemptLogin(event.getSource(), event);
    }

    /**
     * Called when the user clicks the login button’s *mouse* event (your FXML uses onMouseClicked).
     */
    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent mouseEvent) {
        // delegate to the same logic, wrapping MouseEvent as an ActionEvent
        Object src = mouseEvent.getSource();
        attemptLogin(src, new ActionEvent(src, null));
    }

    /**
     * Shared login logic.
     */
    private void attemptLogin(Object source, ActionEvent fxEvent) {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (LoginChecker.checkLogin(email, password)) {
            try {
                SceneSwitcher.switchTo(
                        (Node) source,
                        "/application/studyspace/landingpage/Landing-Page.fxml",
                        "Landing Page"
                );
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Unable to load Landing Page").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Invalid email or password").showAndWait();
        }
    }

    /**
     * Handler for the "Register" link/text in your login FXML.
     */
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        try {
            SceneSwitcher.switchTo(
                    (Node) event.getSource(),
                    "/application/studyspace/auth/Register.fxml",
                    "Register"
            );
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Unable to load Register screen").showAndWait();
        }
    }
}
