package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.Styling.StylingUtility;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.ValidationUtils.ValidationResult;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;

public class RegisterController {
    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1, RegisterPassword_2;
    @FXML private Label passwordTooltip1, emailTooltip;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML private void handleBackToLoginClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    @FXML private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        String email = RegisterEmailField.getText();
        String pw1   = RegisterPassword_1.getText();
        String pw2   = RegisterPassword_2.getText();
        ValidationResult result = ValidationUtils.validateRegistration(email, pw1, pw2);
        int secs = 5;
        switch (result) {
            case EMPTY_EMAIL -> StylingUtility.showError(
                    RegisterEmailField, toolTipService, emailTooltip,
                    "You did not enter an email address! Format: example@domain.com.",
                    "tooltip-Label-Error", "text-field-error", "text-field", secs);

            case INVALID_EMAIL -> StylingUtility.showError(
                    RegisterEmailField, toolTipService, emailTooltip,
                    "Invalid email! Format: example@domain.com.",
                    "tooltip-Label-Error", "text-field-error", "text-field", secs);

            case DUPLICATE_EMAIL -> toolTipService.showAutocorrectPopup(
                    RegisterEmailField,
                    "The E-Mail you entered is already associated with an account. Would you like to log in instead?",
                    "tooltip-Label-Error-Autocorrect",
                    0,
                    () -> ViewManager.show("/application/studyspace/auth/Login.fxml")
            );

            case EMPTY_PASSWORD, PASSWORD_INVALID -> {
                StylingUtility.applyErrorStyle(RegisterPassword_1, "password-field-error");
                StylingUtility.applyErrorStyle(RegisterPassword_2, "password-field-error");
                toolTipService.showTooltipForDurationX(
                        passwordTooltip1,
                        result == ValidationResult.EMPTY_PASSWORD
                                ? "You did not enter both passwords!"
                                : "Invalid Password! Must be ≥12 chars, include uppercase, number, special.",
                        "tooltip-Label-Error",
                        secs
                );
                PauseTransition delayPwd = new PauseTransition(Duration.seconds(secs));
                delayPwd.setOnFinished(e -> {
                    StylingUtility.resetFieldStyle(RegisterPassword_1, "password-field-error", "password-field");
                    StylingUtility.resetFieldStyle(RegisterPassword_2, "password-field-error", "password-field");
                });
                delayPwd.play();
            }

            case PASSWORD_MISMATCH -> {
                StylingUtility.applyErrorStyle(RegisterPassword_1, "password-field-error");
                StylingUtility.applyErrorStyle(RegisterPassword_2, "password-field-error");
                toolTipService.showTooltipForDurationX(
                        passwordTooltip1,
                        "Passwords do not match! Please try again.",
                        "tooltip-Label-Error",
                        secs
                );
                PauseTransition delayMismatch = new PauseTransition(Duration.seconds(secs));
                delayMismatch.setOnFinished(e -> {
                    StylingUtility.resetFieldStyle(RegisterPassword_1, "password-field-error", "password-field");
                    StylingUtility.resetFieldStyle(RegisterPassword_2, "password-field-error", "password-field");
                });
                delayMismatch.play();
            }

            case OK -> {
                saveToDatabase(email, pw1);
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
            }
        }
    }

    @FXML
    private void initialize() {
        String pwTip = """
            Please enter a password that fulfills the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip1, pwTip, "tooltip-Label");

        String emTip = "Please enter a valid email address:\n• The format should be like example@domain.com.";
        toolTipService.createCustomTooltip(emailTooltip, emTip, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
    }
}
