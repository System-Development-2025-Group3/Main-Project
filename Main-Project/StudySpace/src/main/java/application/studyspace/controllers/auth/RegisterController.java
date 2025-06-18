package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.auth.PasswordHasher;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.ValidationUtils.RegistrationError;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.UUID;

import static application.studyspace.services.Styling.StylingUtility.applyErrorStyle;
import static application.studyspace.services.Styling.StylingUtility.resetFieldStyle;

public class RegisterController {

    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1;
    @FXML private PasswordField RegisterPassword_2;
    @FXML private Label emailTooltip;
    @FXML private Label passwordTooltip1;
    @FXML private Label passwordTooltip2;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        String email = RegisterEmailField.getText().trim();
        String pw1 = RegisterPassword_1.getText();
        String pw2 = RegisterPassword_2.getText();

        RegistrationError err = ValidationUtils.validateRegistration(email, pw1, pw2);
        if (err != RegistrationError.NONE) {
            showRegistrationError(err);
            return;
        }

        UUID newUserId = PasswordHasher.saveToDatabase(email, pw1);
        SessionManager.getInstance().login(newUserId);
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleBacktoLoginClick(ActionEvent event) {
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }

    private void showRegistrationError(RegistrationError err) {
        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        switch (err) {
            case EMAIL_EMPTY -> {
                toolTipService.showTooltipForDurationX(emailTooltip, "Please enter an email address.", "tooltip-Label-Error", 5, 0);
                applyErrorStyle(RegisterEmailField, "text-field-error");
                delay.setOnFinished(e -> resetFieldStyle(RegisterEmailField, "text-field-error", "text-field"));
            }
            case EMAIL_INVALID -> {
                toolTipService.showTooltipForDurationX(emailTooltip, "That doesn’t look like a valid email address!", "tooltip-Label-Error", 5, 0);
                applyErrorStyle(RegisterEmailField, "text-field-error");
                delay.setOnFinished(e -> resetFieldStyle(RegisterEmailField, "text-field-error", "text-field"));
            }
            case PASSWORD_EMPTY -> {
                toolTipService.showTooltipForDurationX(passwordTooltip1, "Please enter a password and confirm it.", "tooltip-Label-Error", 5, 0);
                applyErrorStyle(RegisterPassword_1, "text-field-error");
                applyErrorStyle(RegisterPassword_2, "text-field-error");
                delay.setOnFinished(e -> {
                    resetFieldStyle(RegisterPassword_1, "text-field-error", "text-field");
                    resetFieldStyle(RegisterPassword_2, "text-field-error", "text-field");
                });
            }
            case PASSWORD_MISMATCH -> {
                toolTipService.showTooltipForDurationX(passwordTooltip1, "Passwords do not match. Please try again.", "tooltip-Label-Error", 5, 0);
                applyErrorStyle(RegisterPassword_1, "text-field-error");
                applyErrorStyle(RegisterPassword_2, "text-field-error");
                delay.setOnFinished(e -> {
                    resetFieldStyle(RegisterPassword_1, "text-field-error", "text-field");
                    resetFieldStyle(RegisterPassword_2, "text-field-error", "text-field");
                });
            }
            case PASSWORD_WEAK -> {
                toolTipService.showTooltipForDurationX(passwordTooltip1, """
                    Password must be at least 12 characters, 
                    include an uppercase letter, a digit, 
                    and a special char (! % & ? # _ - $).
                """, "tooltip-Label-Error", 5, 0);
                applyErrorStyle(RegisterPassword_1, "text-field-error");
                applyErrorStyle(RegisterPassword_2, "text-field-error");
                delay.setOnFinished(e -> {
                    resetFieldStyle(RegisterPassword_1, "text-field-error", "text-field");
                    resetFieldStyle(RegisterPassword_2, "text-field-error", "text-field");
                });
            }
        }

        delay.play();
    }

    @FXML
    private void initialize() {
        String pwTooltip = """
            Please enter a password that fulfills the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip1, pwTooltip, "tooltip-Label");
        toolTipService.createCustomTooltip(passwordTooltip2, pwTooltip, "tooltip-Label");

        String emailTooltipText = """
            Please enter a valid email address:
            • The format should be like example@domain.com.
            """;
        toolTipService.createCustomTooltip(emailTooltip, emailTooltipText, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
    }

    // Menu button placeholders
    @FXML
    private void handleSettingsClick(ActionEvent event) {
        System.out.println("⚙ Settings clicked (not implemented yet)");
    }

    @FXML
    private void handleAboutUsClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }

    @FXML
    private void handleContactClick(ActionEvent event) {
        System.out.println("📨 Contact clicked (not implemented yet)");
    }
}
