package application.studyspace.controllers.auth;

// Imports for navigation, validation, and UI controls
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

/**
 * Controller class for handling user registration.
 * Manages input validation, account creation, and navigation to onboarding.
 */
public class RegisterController {

    // FXML bindings to registration form fields
    @FXML private TextField RegisterEmailField;      // Email input field
    @FXML private PasswordField RegisterPassword_1;  // Password input field
    @FXML private PasswordField RegisterPassword_2;  // Confirm password input field

    /**
     * Called when "Back to Login" is clicked.
     * Navigates back to the login screen.
     */
    @FXML
    private void handleBackToLoginClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    /**
     * Called when the close button is clicked.
     * Exits the application.
     */
    @FXML
    public void handleCloseClick(javafx.scene.input.MouseEvent event) {
        System.exit(0); // Cleanly exits the app
    }

    /**
     * Called when the "Register" button is clicked.
     * Validates user input and, if valid, persists the new account and starts onboarding.
     */
    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        // Clear any existing error styling
        clearErrorStyle(RegisterEmailField);
        clearErrorStyle(RegisterPassword_1);
        clearErrorStyle(RegisterPassword_2);

        // Collect user input
        String email = RegisterEmailField.getText();
        String pw1   = RegisterPassword_1.getText();
        String pw2   = RegisterPassword_2.getText();

        // Validate the inputs
        ValidationUtils.ValidationResult result = ValidationUtils.validateRegistration(email, pw1, pw2);

        // Handle validation outcomes
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
                // All validation passed, save user to the database
                saveToDatabase(email, pw1);

                // Navigate to landing page
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");

                // Show onboarding overlay on top of the landing page
                ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", null);
            }
        }
    }

    /**
     * Shows an inline error message by clearing the field, displaying a red prompt, and styling the border.
     * Automatically resets styling after 2 seconds.
     */
    private void showInlineError(TextInputControl field, String message) {
        // Clear input
        field.clear();
        // Show prompt text and apply red styling
        field.setPromptText(message);
        field.setStyle(
                "-fx-prompt-text-fill: red; " +
                        "-fx-border-color: red; " +
                        "-fx-border-width: 2;"
        );

        // Remove error styling after delay
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            field.setPromptText("");
            clearErrorStyle(field);
        });
        delay.play();
    }

    /**
     * Clears any inline error styles from a field.
     */
    private void clearErrorStyle(TextInputControl field) {
        field.setStyle("");
    }

    /**
     * Called when the "About Us" button is clicked.
     * Navigates to the About Us screen.
     */
    @FXML
    public void handleAboutUsClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/AboutUs.fxml");
    }
}
