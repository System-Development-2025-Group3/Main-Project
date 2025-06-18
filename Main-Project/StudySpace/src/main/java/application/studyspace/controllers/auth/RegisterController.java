package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.auth.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;
import static application.studyspace.services.Styling.StylingUtility.applyErrorStyle;
import static application.studyspace.services.Styling.StylingUtility.resetFieldStyle;

public class RegisterController {

    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1, RegisterPassword_2;
    @FXML private Label passwordTooltip1, emailTooltip, passwordTooltip2;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleBackToLoginClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        String validationState;
        if (RegisterEmailField.getText().isEmpty()) {
            validationState = "EMPTY_EMAIL";
        } else if (ValidationUtils.isValidEmail(RegisterEmailField.getText())) {
            validationState = "INVALID_EMAIL";
        } else if (RegisterPassword_1.getText().isEmpty() || RegisterPassword_2.getText().isEmpty()) {
            validationState = "EMPTY_PASSWORD";
        } else if (!RegisterPassword_1.getText().equals(RegisterPassword_2.getText())) {
            validationState = "NOT_MATCHING_PASSWORDS";
        } else if (!ValidationUtils.isValidPassword(RegisterPassword_1.getText()).equals("VALID")) {
            validationState = "REGISTER_PASSWORD_INVALID";
        } else if (ValidationUtils.isKnownEmail(RegisterEmailField.getText())) {
            validationState = "DUPLICATE_EMAIL";
        } else {
            validationState = "CORRECT";
        }

        boolean isCorrect = "CORRECT".equals(validationState);
        int duration = 5;

        if (!isCorrect) {
            switch (validationState) {
                case "EMPTY_EMAIL" -> {
                    applyErrorStyle(RegisterEmailField, "text-field-error");
                    toolTipService.showTooltipForDurationX(
                            emailTooltip,
                            "You did not enter an email address! Please enter a valid email address:\n" +
                                    "• The format should be like example@domain.com.",
                            "tooltip-Label-Error",
                            duration
                    );
                    // <-- fix here
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e ->
                            resetFieldStyle(RegisterEmailField, "text-field-error", "text-field")
                    );
                    delay.play();
                }

                case "INVALID_EMAIL" -> {
                    applyErrorStyle(RegisterEmailField, "text-field-error");
                    toolTipService.showTooltipForDurationX(
                            emailTooltip,
                            "You did not enter a valid email address! Please do so next time.\n" +
                                    "• The format should be like example@domain.com.",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e ->
                            resetFieldStyle(RegisterEmailField, "text-field-error", "text-field")
                    );
                    delay.play();
                }

                case "DUPLICATE_EMAIL" -> {
                    toolTipService.showAutocorrectPopup(
                            emailTooltip,
                            "The E-Mail you entered is already associated with an account.\n" +
                                    "Would you instead like to log in?",
                            "tooltip-Label-Error-Autocorrect",
                            0,
                            () -> ViewManager.show("/application/studyspace/auth/Login.fxml")
                    );
                }

                // both EMPTY_PASSWORD and REGISTER_PASSWORD_INVALID share the same styling logic
                case "EMPTY_PASSWORD", "REGISTER_PASSWORD_INVALID" -> {
                    applyErrorStyle(RegisterPassword_1, "password-field-error");
                    applyErrorStyle(RegisterPassword_2, "password-field-error");
                    toolTipService.showTooltipForDurationX(
                            passwordTooltip1,
                            validationState.equals("EMPTY_PASSWORD")
                                    ? "You did not enter both passwords!"
                                    : "Invalid Password! Please make sure that your password fulfills the following conditions:\n" +
                                    "• At least 12 characters long\n" +
                                    "• Includes at least one uppercase letter\n" +
                                    "• Includes at least one number\n" +
                                    "• Includes at least one special character (%, &, !, ?, #, _, -, $)",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> {
                        resetFieldStyle(RegisterPassword_1, "password-field-error", "password-field");
                        resetFieldStyle(RegisterPassword_2, "password-field-error", "password-field");
                    });
                    delay.play();
                }

                case "NOT_MATCHING_PASSWORDS" -> {
                    applyErrorStyle(RegisterPassword_1, "password-field-error");
                    applyErrorStyle(RegisterPassword_2, "password-field-error");
                    toolTipService.showTooltipForDurationX(
                            passwordTooltip1,
                            "The passwords you entered do not match! Please try again with matching passwords.",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> {
                        resetFieldStyle(RegisterPassword_1, "password-field-error", "password-field");
                        resetFieldStyle(RegisterPassword_2, "password-field-error", "password-field");
                    });
                    delay.play();
                }
            }
        } else {
            // all valid → save and go
            saveToDatabase(RegisterEmailField.getText(), RegisterPassword_1.getText());
            ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
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
        toolTipService.createCustomTooltip(passwordTooltip2, pwTip, "tooltip-Label");

        String emTip = "Please enter a valid email address:\n• The format should be like example@domain.com.";
        toolTipService.createCustomTooltip(emailTooltip, emTip, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
    }
}
