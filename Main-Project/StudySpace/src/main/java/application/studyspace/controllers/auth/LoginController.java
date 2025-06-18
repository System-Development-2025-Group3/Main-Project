package application.studyspace.controllers.auth;

import application.studyspace.controllers.onboarding.OnboardingPage1Controller;
import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.LoginChecker;
import application.studyspace.services.auth.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.DatabaseHelper.SELECT;
import static application.studyspace.services.API.DeepSeekAPI.executeDeepSeekAPI;
import static application.studyspace.services.Styling.StylingUtility.applyErrorStyle;
import static application.studyspace.services.Styling.StylingUtility.resetFieldStyle;

public class LoginController {

    @FXML public VBox LoginCard;
    @FXML public Label WelcomeTitle, passwordTooltip, emailTooltip;
    @FXML public Button SubmitLoginButton;
    @FXML public TextFlow textFlow;
    @FXML public Text linkText;
    @FXML public ImageView Image01;
    @FXML public CheckBox stayLoggedInCheckbox;
    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Register.fxml");
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) {
        String validationState;
        if (InputEmailTextfield.getText().isEmpty()) {
            validationState = "EMPTY_EMAIL";
        } else if (ValidationUtils.isValidEmail(InputEmailTextfield.getText())) {
            validationState = "INVALID_EMAIL";
        } else if (!ValidationUtils.isKnownEmail(InputEmailTextfield.getText())) {
            validationState = "UNKNOWN_EMAIL";
        } else if (InputPassword.getText().isEmpty()) {
            validationState = "EMPTY_PASSWORD";
        } else if (!LoginChecker.checkLogin(InputEmailTextfield.getText(), InputPassword.getText())) {
            validationState = "INVALID_CREDENTIALS";
        } else {
            validationState = "CORRECT";
        }

        boolean isCorrect = "CORRECT".equals(validationState);
        int duration = 5;

        if (!isCorrect) {
            switch (validationState) {
                case "EMPTY_EMAIL" -> {
                    applyErrorStyle(InputEmailTextfield, "text-field-error");
                    toolTipService.showTooltipForDurationX(
                            emailTooltip,
                            "You did not enter an email address! Please enter a valid email address:\n" +
                                    "• The format should be like example@domain.com.",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field"));
                    delay.play();
                }
                case "INVALID_EMAIL" -> {
                    applyErrorStyle(InputEmailTextfield, "text-field-error");
                    toolTipService.showTooltipForDurationX(
                            emailTooltip,
                            "You did not enter a valid email address! Please do so next time.\n" +
                                    "• The format should be like example@domain.com.",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field"));
                    delay.play();
                }
                case "UNKNOWN_EMAIL" -> {
                    if (!ValidationUtils.isSimilarEmail(InputEmailTextfield.getText(), ValidationUtils.listOfKnownEmails(), 3)) {
                        applyErrorStyle(InputEmailTextfield, "text-field-error");
                        toolTipService.showTooltipForDurationX(
                                emailTooltip,
                                "The email address you entered is not registered in our system.\n" +
                                        "Please try again with a known email address.\n" +
                                        "• The format should be like example@domain.com.",
                                "tooltip-Label-Error",
                                duration
                        );
                        PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                        delay.setOnFinished(e -> resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field"));
                        delay.play();
                    } else {
                        applyErrorStyle(InputEmailTextfield, "text-field-error");
                        mailAutocorrect();
                    }
                }
                case "EMPTY_PASSWORD" -> {
                    applyErrorStyle(InputPassword, "password-field-error");
                    toolTipService.showTooltipForDurationX(
                            passwordTooltip,
                            "You did not enter a password! Please enter a password that fulfills the following:\n" +
                                    "• ≥12 chars\n• ≥1 uppercase\n• ≥1 number\n• ≥1 special (% & ! ? # _ - $)",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> resetFieldStyle(InputPassword, "password-field-error", "password-field"));
                    delay.play();
                }
                case "INVALID_CREDENTIALS" -> {
                    applyErrorStyle(InputPassword, "password-field-error");
                    toolTipService.showTooltipForDurationX(
                            passwordTooltip,
                            "The Password you entered is incorrect! Please try again with a correct Password.",
                            "tooltip-Label-Error",
                            duration
                    );
                    PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                    delay.setOnFinished(e -> resetFieldStyle(InputPassword, "password-field-error", "password-field"));
                    delay.play();
                }
            }
        } else {
            // --- on success ---
            UUID userUUID;
            try {
                userUUID = new DatabaseHelper().getUserUUIDByEmail(InputEmailTextfield.getText());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (stayLoggedInCheckbox.isSelected()) {
                AutoLoginHandler.activateAutoLogin(userUUID.toString());
            }

            ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
            ViewManager.showOverlay(
                    "/application/studyspace/onboarding/OnboardingPage1.fxml",
                    (OnboardingPage1Controller c) -> c.setUserUUID(userUUID)
            );
        }
    }

    private void mailAutocorrect() {
        String dynamicInput = "You are an E-Mail Autocorrector. The following E-Mail has been provided: "
                + InputEmailTextfield.getText()
                + " Please look at all available E-Mails and choose the one likely intended: "
                + SELECT("email","users",null,null)
                + " Output only the corrected E-Mail.";
        String suggestion = executeDeepSeekAPI(dynamicInput);

        toolTipService.showAutocorrectPopup(
                emailTooltip,
                "The E-Mail you entered is not registered.\nHowever, we might have found a possible valid email.\nAccept our suggestion?",
                "tooltip-Label-Error-Autocorrect",
                0,
                () -> setInputEmail(suggestion)
        );
    }

    public void setInputEmail(String email) {
        InputEmailTextfield.setText(email);
        applyErrorStyle(InputEmailTextfield, "text-field-correct");
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> {
            resetFieldStyle(InputEmailTextfield, "text-field-correct", "text-field");
            resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
        });
        delay.play();
    }

    @FXML
    private void initialize() {
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

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
    }

    @FXML
    public void handleAboutUsClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }

    public void handleSettingsClick() { /* no-op */ }
}
