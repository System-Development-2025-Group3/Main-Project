package application.studyspace.controllers.auth;

import application.studyspace.controllers.onboarding.ExamsOnboardingController;
import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.auth.LoginChecker;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.DatabaseHelper.SELECT;
import static application.studyspace.services.API.DeepSeekAPI.executeDeepSeekAPI;
import static application.studyspace.services.Styling.StylingUtility.applyErrorStyle;
import static application.studyspace.services.Styling.StylingUtility.resetFieldStyle;

public class LoginController {

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private Label passwordTooltip, emailTooltip;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;
    @FXML private ImageView gearIcon;


    /**
     * Handles the action when the "Register" text link is clicked.
     * It navigates the user to the registration page by switching the scene
     * to the Register.fxml file.
     *
     * @param event the MouseEvent triggered when the "Register" link is clicked
     */
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/auth/Register.fxml", "Register");
    }

    /**
     * Handles the action when the login button is clicked in the user interface.
     * This method performs the following:
     * 1. Validates that both the email and password input fields are not empty.
     * 2. Checks the provided login credentials against the database.
     * 3. Displays an error alert if fields are empty or credentials are invalid.
     * 4. Switches to the landing page if the login is successful.
     *
     * @param event the MouseEvent triggered when the login button is clicked
     */
    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) {

    String validationState = "CORRECT";

    if (InputEmailTextfield.getText().isEmpty()) {
        validationState= "EMPTY_EMAIL";
    }
    if (!ValidationUtils.isValidEmail(InputEmailTextfield.getText())) {
        validationState = "INVALID_EMAIL";
    }
    if (InputPassword.getText().isEmpty()) {
        validationState="EMPTY_PASSWORD";
    }
    if (!LoginChecker.checkLogin(InputEmailTextfield.getText(), InputPassword.getText())) {
       validationState = "INVALID_CREDENTIALS";
    }

    double tooltipOffsetY = 0;
    int duration = 5;

    boolean isCorrect = "CORRECT".equals(validationState); // Safely compare strings
    if (!isCorrect) {
        System.out.println("The following error occurred while trying to log in: " + validationState + ".");
        switch (validationState) {
        case "EMPTY_EMAIL" -> {
            String toolTipText = """
                    You did not enter an email address! Please enter a valid email address:
                    • The format should be like example@domain.com.""";
            System.out.println("The user tried to log in, but did not fill in all fields. Missing: InputEmailTextfield");

            applyErrorStyle(InputEmailTextfield, "text-field-error");
            toolTipService.showTooltipForDurationX(emailTooltip, toolTipText, "tooltip-Label-Error", duration, tooltipOffsetY);
            tooltipOffsetY += emailTooltip.getHeight() + 10;
            duration += 5;

            PauseTransition delay = new PauseTransition(Duration.seconds(duration));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
            });
        }

        case "INVALID_EMAIL" -> {
            System.out.println("The user tried to log in but the email format is invalid.");

            applyErrorStyle(InputEmailTextfield, "text-field-error");
            tooltipOffsetY += emailTooltip.getHeight() + 10;
            duration += 5;

            mailAutocorrect();
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputEmailTextfield, "text-field-correct", "text-field");
            });
            delay.play();
        }

        case "INVALID_CREDENTIALS" -> {
            System.out.println("The user tried to log in, but did not enter valid credentials.");

            applyErrorStyle(InputPassword, "password-field-error");
            applyErrorStyle(InputEmailTextfield, "text-field-error");

            mailAutocorrect();
            System.out.println("The Flow continues ...");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputEmailTextfield, "text-field-correct", "text-field");
                resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
                resetFieldStyle(InputPassword, "password-field-error", "password-field");
            });
            delay.play();

        }

        case "EMPTY_PASSWORD" -> {
            String toolTipText = """
                    You did not enter a password! Please enter a password that fulfills the following conditions:
                    • At least 12 characters long
                    • Includes at least one uppercase letter
                    • Includes at least one number
                    • Includes at least one special character (%, &, !, ?, #, _, -, $)""";
            System.out.println("The user tried to log in, but did not fill in all fields. Missing: InputPassword.");

            applyErrorStyle(InputPassword, "password-field-error");
            toolTipService.showTooltipForDurationX(passwordTooltip, toolTipText, "tooltip-Label-Error", 5, tooltipOffsetY);
            tooltipOffsetY += passwordTooltip.getHeight() + 10;
            duration += 5;

            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputPassword, "password-field-error", "password-field");
            });
        }
    }
} else {
    // If no validation state exists, proceed with switching the scene
        DatabaseHelper db = new DatabaseHelper();
        final UUID userUUID;
        try {
            userUUID = db.getUserUUIDByEmail(InputEmailTextfield.getText());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Get the stage before switching scenes
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Switch to the landing page
        SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Landing-Page.fxml", "Landing Page");

        // Immediately show the popup
        SceneSwitcher.switchToPopupWithData(
                stage,
                "/application/studyspace/onboarding/ExamOnboardingPopUp.fxml",
                "Exam Form",
                (ExamsOnboardingController controller) -> controller.setUserUUID(userUUID)
        );


        //SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/usermanagement/ExamOnboardingPopUp.fxml", "Landing-Page");
}

    }

    private void mailAutocorrect() {
        String dynamicInput = "You are an E-Mail Autocorrector. The following E-Mail has been provided: "
                + InputEmailTextfield.getText();
        dynamicInput += " Please look at all available E-Mails and choose the one that the user was likely to enter:";

        dynamicInput += " " + SELECT("email", "users", null, null) + "As Output please only provide the corrected E-Mail. Nothing else!";
        String email = executeDeepSeekAPI(dynamicInput);
        String TooltipText;
        TooltipText = "The E-Mail you entered is not registered.\n"
            + "However, we have found a possible valid email\n"
            + "associated with your input. Would you like to\n"
            + "accept our correction?";

        toolTipService.showAutocorrectPopup(emailTooltip, TooltipText, "tooltip-Label-Error-Autocorrect", 0, () -> setInputEmail(email));
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up a custom tooltip for the password input field, providing
     * guidance on the expected password format and requirements.
     * The custom tooltip appears when the mouse hovers over the tooltip field.
     */
    @FXML
    private void initialize() {
        System.out.println("The LoginController has been initialized.");
        String tooltipText = """
            Please enter a password that fulfills the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)""";

        toolTipService.createCustomTooltip(passwordTooltip, tooltipText, "tooltip-Label");

        tooltipText = """
                Please enter a valid email address:
                • The format should be like example@domain.com.""";

        toolTipService.createCustomTooltip(emailTooltip, tooltipText, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
        StackPane.setAlignment(Image03, javafx.geometry.Pos.TOP_RIGHT);

    }

    public void setInputEmail(String email) {
        InputEmailTextfield.setText(email);
        applyErrorStyle(InputEmailTextfield, "text-field-correct");
        System.out.println("The user accepted our correction for the E-Mail input.");

        if (!ValidationUtils.isValidEmail(email)) {
            System.out.println("The corrected email is still invalid.");
        } else {
            System.out.println("The corrected email is now valid.");
        }
    }

    @FXML
    private void handleSettingsIconClick(MouseEvent event) {
        Stage stage = (Stage) stackPane.getScene().getWindow(); // Get the current stage
        SceneSwitcher.switchToPopup(stage, "/application/studyspace/settings/SettingsPopUp.fxml", "Settings");
        System.out.println("The user clicked on the Settings icon and navigated to the Settings PopUp.");
    }

}