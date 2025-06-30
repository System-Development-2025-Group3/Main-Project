package application.studyspace.controllers.auth;

// Imports for navigation, validation, styling, and UI controls
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.auth.PasswordHasher;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.util.UUID;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;

/**
 * Controller class for handling user registration.
 * Manages input validation, account creation, onboarding navigation,
 * and displays password requirement tooltips.
 */
public class RegisterController {

    // FXML bindings to registration form fields
    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1;
    @FXML private PasswordField RegisterPassword_2;

    // Tooltip icons/labels next to password fields in FXML
    @FXML private Label pwTooltipIcon1;
    @FXML private Label pwTooltipIcon2;

    // Tooltip helper instance
    private final CreateToolTip toolTipService = new CreateToolTip();

    /**
     * Called by JavaFX after FXML fields are injected.
     * Sets up password requirement tooltips.
     */
    @FXML
    private void initialize() {
        // Tooltip text explaining password strength requirements
        String pwTip = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;

        // Attach tooltips to the icon labels
        toolTipService.createCustomTooltip(pwTooltipIcon1, pwTip, "tooltip-Label");
        toolTipService.createCustomTooltip(pwTooltipIcon2, pwTip, "tooltip-Label");
    }

    /**
     * Navigates back to the login screen when clicked.
     */
    @FXML
    private void handleBackToLoginClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    /**
     * Exits the application when the close button is clicked.
     */
    @FXML
    public void handleCloseClick(MouseEvent event) {
        System.exit(0);
    }

    /**
     * Validates input and registers a new user when the "Register" button is clicked.
     */
    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        // Clear previous error styling
        clearErrorStyle(RegisterEmailField);
        clearErrorStyle(RegisterPassword_1);
        clearErrorStyle(RegisterPassword_2);

        String email = RegisterEmailField.getText();
        String pw1   = RegisterPassword_1.getText();
        String pw2   = RegisterPassword_2.getText();

        // Validate the registration fields
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
                // Persist user, capture their new UUID
                UUID userUUID = PasswordHasher.saveToDatabase(email, pw1);

                if (userUUID != null) {
                    // Immediately log them in, so downstream controllers see a logged-in user
                    SessionManager.getInstance().login(userUUID);

                    // Now safe to navigate into landing/onboarding
                    ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
                    ViewManager.showOverlay(
                            "/application/studyspace/onboarding/OnboardingPage1.fxml",
                            null
                    );
                } else {
                    // handle save failure...
                    showInlineError(RegisterEmailField,
                            "Registration failed—please try again");
                }
            }
        }
    }

    /**
     * Displays an inline error on a field by clearing its text, showing a red prompt, and styling it.
     * Clears the error styling after 2 seconds.
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
     * Clears inline error styling from a field.
     */
    private void clearErrorStyle(TextInputControl field) {
        field.setStyle("");
    }

    /**
     * Navigates to the About Us page when clicked.
     */
    @FXML
    public void handleAboutUsClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/AboutUs.fxml");
    }
}
