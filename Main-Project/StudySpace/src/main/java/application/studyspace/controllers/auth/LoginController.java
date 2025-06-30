package application.studyspace.controllers.auth;

// Import all necessary classes and services
import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.RememberMeHelper;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;

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

/**
 * Controller class handling login functionality for the application.
 * This manages user input validation, session setup, remember-me logic, and navigation.
 */
public class LoginController {

    // FXML bindings to UI components
    @FXML private TextField InputEmailTextfield;       // Email input field
    @FXML private PasswordField InputPassword;         // Password input field
    @FXML private CheckBox stayLoggedInCheckbox;       // "Remember me" checkbox

    /**
     * Called when the "Register" text is clicked.
     * Navigates the user to the registration screen.
     */
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Register.fxml");
    }

    /**
     * Called when the close button is clicked.
     * Exits the entire application.
     */
    @FXML
    public void handleCloseClick(javafx.scene.input.MouseEvent event) {
        System.exit(0); // Cleanly exits the app
    }

    /**
     * Called when the "Login" button is clicked.
     * Validates inputs, manages authentication, session handling, and navigation.
     */
    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) throws SQLException {
        // Clear any previous error styling
        clearErrorStyle(InputEmailTextfield);
        clearErrorStyle(InputPassword);

        // Get user inputs
        String email = InputEmailTextfield.getText();
        String pw    = InputPassword.getText();

        // Validate inputs using utility class
        ValidationUtils.ValidationResult result = ValidationUtils.validateLogin(email, pw);

        // Handle different validation outcomes
        switch (result) {
            case EMPTY_EMAIL ->
                    showInlineError(InputEmailTextfield, "Please enter your e-mail address");
            case INVALID_EMAIL ->
                    showInlineError(InputEmailTextfield, "That e-mail address isn't valid");
            case UNKNOWN_EMAIL ->
                    showInlineError(InputEmailTextfield, "No account found for that e-mail");
            case EMPTY_PASSWORD ->
                    showInlineError(InputPassword, "Please enter your password");
            case INVALID_CREDENTIALS ->
                    showInlineError(InputPassword, "E-mail and password don't match");

            // Validation passed, proceed with login
            case OK -> {
                DatabaseHelper dbHelper = new DatabaseHelper();

                // Retrieve user's UUID from database by email
                UUID userUUID = dbHelper.getUserUUIDByEmail(email);

                // Start a session for this user
                SessionManager.getInstance().login(userUUID);

                // Synchronize "skip splash screen" preference from DB
                boolean skipSplashFromDb = StudyPreferences.getSkipSplashScreenPreference(userUUID);
                SessionManager.getInstance().saveSkipSplashScreenPreferenceLocal(skipSplashFromDb);

                // Handle "remember me" checkbox
                boolean rememberMe = stayLoggedInCheckbox.isSelected();
                if (rememberMe) {
                    // Save UUID locally so user stays logged in on next app launch
                    RememberMeHelper.saveRememberedUserUUID(userUUID);
                } else {
                    // Clear any previously remembered login
                    RememberMeHelper.clearRememberedUserUUID();
                }

                // Also update the DB for consistency across devices
                StudyPreferences.updateRememberMe(userUUID, rememberMe);

                // Navigate to landing page upon successful login
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
            }
        }
    }

    /**
     * Called when the "About Us" button is clicked.
     * Navigates to the About Us screen.
     */
    @FXML
    public void handleAboutUsClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/AboutUs.fxml");
    }

    /**
     * Displays an inline error message by styling the field and showing placeholder text.
     * The error styling clears itself automatically after 2 seconds.
     */
    private void showInlineError(TextInputControl field, String message) {
        // Clear any existing input
        field.clear();
        // Set placeholder text and apply red styling
        field.setPromptText(message);
        field.setStyle(
                "-fx-prompt-text-fill: red; " +
                        "-fx-border-color: red; " +
                        "-fx-border-width: 2;"
        );

        // After 2 seconds, remove the error styling and clear the prompt
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            field.setPromptText("");
            clearErrorStyle(field);
        });
        delay.play();
    }

    /**
     * Removes any custom styling applied to indicate error.
     */
    private void clearErrorStyle(TextInputControl field) {
        field.setStyle("");
    }
}
