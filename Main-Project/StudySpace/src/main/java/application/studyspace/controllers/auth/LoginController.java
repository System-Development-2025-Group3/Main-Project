package application.studyspace.controllers.auth;

import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.DataBase.SkipSplashScreenManager;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.SessionManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.auth.AutoLoginHandler.activateAutoLogin;

public class LoginController {

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private CheckBox stayLoggedInCheckbox;

    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Register.fxml");
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) throws SQLException {
        // Clear previous error state
        clearErrorStyle(InputEmailTextfield);
        clearErrorStyle(InputPassword);

        String email = InputEmailTextfield.getText();
        String pw    = InputPassword.getText();
        ValidationUtils.ValidationResult result = ValidationUtils.validateLogin(email, pw);

        switch (result) {
            case EMPTY_EMAIL -> showInlineError(InputEmailTextfield, "Please enter your e-mail address");
            case INVALID_EMAIL -> showInlineError(InputEmailTextfield, "That e-mail address isn't valid");
            case UNKNOWN_EMAIL -> showInlineError(InputEmailTextfield, "No account found for that e-mail");
            case EMPTY_PASSWORD -> showInlineError(InputPassword, "Please enter your password");
            case INVALID_CREDENTIALS -> showInlineError(InputPassword, "E-mail and password don't match");
            case OK -> {
                DatabaseHelper dbHelper = new DatabaseHelper();
                UUID userUUID = dbHelper.getUserUUIDByEmail(email);
                SessionManager.getInstance().login(userUUID);
                if (stayLoggedInCheckbox.isSelected()) {
                    activateAutoLogin(userUUID.toString());
                }
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
                ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", null);

                boolean skipSplashDatabase = new SkipSplashScreenManager()
                        .loadSkipSplashScreenFromDatabase(userUUID);
                SessionManager.getInstance().setSkipSplashScreen(skipSplashDatabase);
            }
        }
    }

    @FXML
    public void handleAboutUsClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }


    /**
     * Shows an inline error message as placeholder text with red border for 2 seconds.
     */
    private void showInlineError(TextInputControl field, String message) {
        // Clear any existing input
        field.clear();
        // Set placeholder text and red styling
        field.setPromptText(message);
        field.setStyle(
                "-fx-prompt-text-fill: red; " +
                        "-fx-border-color: red; " +
                        "-fx-border-width: 2;"
        );

        // After 2 seconds, remove error styling and placeholder
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            field.setPromptText("");
            clearErrorStyle(field);
        });
        delay.play();
    }

    /**
     * Clears any inline styles on the field.
     */
    private void clearErrorStyle(TextInputControl field) {
        field.setStyle("");
    }
}