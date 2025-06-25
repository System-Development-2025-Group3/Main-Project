package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;

public class RegisterController {
    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1;
    @FXML private PasswordField RegisterPassword_2;

    @FXML private void handleBackToLoginClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    @FXML private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        // clear any previous error styling
        clearErrorStyle(RegisterEmailField);
        clearErrorStyle(RegisterPassword_1);
        clearErrorStyle(RegisterPassword_2);

        String email = RegisterEmailField.getText();
        String pw1   = RegisterPassword_1.getText();
        String pw2   = RegisterPassword_2.getText();
        ValidationUtils.ValidationResult result = ValidationUtils.validateRegistration(email, pw1, pw2);

        switch (result) {
            case EMPTY_EMAIL ->
                    showInlineError(RegisterEmailField, "Please enter your e-mail address");

            case INVALID_EMAIL ->
                    showInlineError(RegisterEmailField, "That e-mail address isn't valid");

            case DUPLICATE_EMAIL ->
                    showInlineError(RegisterEmailField, "This e-mail is already registered");

            case EMPTY_PASSWORD -> {
                showInlineError(RegisterPassword_1, "Please enter a password");
                showInlineError(RegisterPassword_2, "Please enter a password");
            }

            case PASSWORD_INVALID -> {
                showInlineError(RegisterPassword_1,
                        "Password must be ≥12 chars, include uppercase, number, special");
                showInlineError(RegisterPassword_2,
                        "Password must be ≥12 chars, include uppercase, number, special");
            }

            case PASSWORD_MISMATCH -> {
                showInlineError(RegisterPassword_1, "Passwords do not match");
                showInlineError(RegisterPassword_2, "Passwords do not match");
            }

            case OK -> {
                // persist new user and go to landing
                saveToDatabase(email, pw1);
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
            }
        }
    }

    /**
     * Inline error: clears field, shows message as prompt text in red, red border, then clears after 2s
     */
    private void showInlineError(TextInputControl field, String message) {
        field.clear();
        field.setPromptText(message);
        field.setStyle(
                "-fx-prompt-text-fill: red; " +
                        "-fx-border-color: red; " +
                        "-fx-border-width: 2;"
        );

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            field.setPromptText("");
            clearErrorStyle(field);
        });
        delay.play();
    }

    /**
     * Resets inline styles on a field.
     */
    private void clearErrorStyle(TextInputControl field) {
        field.setStyle("");
    }

    @FXML
    public void handleAboutUsClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }
}
