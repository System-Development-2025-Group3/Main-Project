package application.studyspace.controllers.auth;

import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.Styling.StylingUtility;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.auth.AutoLoginHandler.activateAutoLogin;

public class LoginController {

    @FXML private TextField      InputEmailTextfield;
    @FXML private PasswordField   InputPassword;
    @FXML private Label           passwordTooltip, emailTooltip;
    @FXML private CheckBox        stayLoggedInCheckbox;
    @FXML private StackPane       stackPane;
    @FXML private VBox            LoginCard;
    @FXML private ImageView       Image03;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void initialize() {
        // tooltips
        String pwTip = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip, pwTip, "tooltip-Label");

        String emTip = """
            Please enter your registered email address:\s
            • The format should be like example@domain.com.
            """;
        toolTipService.createCustomTooltip(emailTooltip, emTip, "tooltip-Label");

        // stretch background to fill the entire StackPane
        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
    }

    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Register.fxml");
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) {
        String email = InputEmailTextfield.getText();
        String pw    = InputPassword.getText();
        ValidationUtils.ValidationResult result = ValidationUtils.validateLogin(email, pw);
        int secs = 5;

        switch (result) {
            case EMPTY_EMAIL -> StylingUtility.showError(
                    InputEmailTextfield, toolTipService, emailTooltip,
                    "Enter your email! Format: example@domain.com.",
                    "tooltip-Label-Error", "text-field-error", "text-field", secs);

            case INVALID_EMAIL -> StylingUtility.showError(
                    InputEmailTextfield, toolTipService, emailTooltip,
                    "Invalid email! Format: example@domain.com.",
                    "tooltip-Label-Error", "text-field-error", "text-field", secs);

            case UNKNOWN_EMAIL -> StylingUtility.showError(
                    InputEmailTextfield, toolTipService, emailTooltip,
                    "Email not found! Try again.",
                    "tooltip-Label-Error", "text-field-error", "text-field", secs
            );

            case EMPTY_PASSWORD -> StylingUtility.showError(
                    InputPassword, toolTipService, passwordTooltip,
                    "Enter your password! ≥12 chars, uppercase, number, special.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);

            case INVALID_CREDENTIALS -> StylingUtility.showError(
                    InputPassword, toolTipService, passwordTooltip,
                    "Incorrect password! Please try again.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);

            case OK -> {
                DatabaseHelper dbHelper = new DatabaseHelper();
                try {
                    UUID userUUID = dbHelper.getUserUUIDByEmail(email);
                    SessionManager.getInstance().login(userUUID);

                    if (stayLoggedInCheckbox.isSelected()) {
                        activateAutoLogin(userUUID.toString());
                    }

                    ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
                    ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", null);
                } catch (SQLException e) {
                    StylingUtility.showError(
                            InputEmailTextfield, toolTipService, emailTooltip,
                            "Database error. Please try again later.",
                            "tooltip-Label-Error", "text-field-error", "text-field", secs);
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void handleAboutUsClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }

    @FXML
    public void handleSettingsClick() {
        // no-op
    }

    @FXML
    public void handleContactClick() {
        // no-op
    }
}