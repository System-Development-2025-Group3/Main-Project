package application.studyspace.controllers.auth;

import application.studyspace.controllers.onboarding.OnboardingPage1Controller;
import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.auth.*;
import application.studyspace.services.Scenes.SceneSwitcher;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static application.studyspace.services.DataBase.DatabaseHelper.SELECT;
import static application.studyspace.services.API.DeepSeekAPI.executeDeepSeekAPI;
import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;
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
     */
    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) {

        String validationState;

        if (InputEmailTextfield.getText().isEmpty()) {
            validationState = "EMPTY_EMAIL";

        } else if (ValidationUtils.isValidEmail(InputEmailTextfield.getText())) {
            validationState = "INVALID_EMAIL";
            System.out.println("The user tried to log in, but the E-Mail address is invalid. Provided E-Mail: " + InputEmailTextfield.getText() + ".");

        } else if(!ValidationUtils.isKnownEmail(InputEmailTextfield.getText())) {
            validationState = "UNKNOWN_EMAIL";
            System.out.println("The user tried to log in, but the E-Mail address is not known in the Database. Provided E-Mail: " + InputEmailTextfield.getText() + ".");

        } else if (InputPassword.getText().isEmpty()) {
            validationState = "EMPTY_PASSWORD";

        } else if (!LoginChecker.checkLogin(InputEmailTextfield.getText(), InputPassword.getText())) {
            validationState = "INVALID_CREDENTIALS";

        } else {
            validationState = "CORRECT";
        }

        int duration = 5;

        boolean isCorrect = "CORRECT".equals(validationState);

        if (!isCorrect) {

            System.out.println("Current Validation Status: " + validationState);

            switch (validationState) {
            case "EMPTY_EMAIL" -> {

                String toolTipText = """
                        You did not enter an email address! Please enter a valid email address:
                        • The format should be like example@domain.com.""";

                applyErrorStyle(InputEmailTextfield, "text-field-error");

                toolTipService.showTooltipForDurationX(emailTooltip,
                        toolTipText,
                        "tooltip-Label-Error",
                        duration);

                PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                delay.setOnFinished(finishedEvent -> {
                    resetFieldStyle(InputEmailTextfield,
                            "text-field-error",
                            "text-field");
                });
                delay.play();
            }

            case "INVALID_EMAIL" -> {

                String toolTipText = """
                        You did not enter a valid email address! Please do so next time.
                        • The format should be like example@domain.com.""";

                applyErrorStyle(InputEmailTextfield, "text-field-error");
                toolTipService.showTooltipForDurationX(emailTooltip, toolTipText, "tooltip-Label-Error", duration);

                PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                delay.setOnFinished(finishedEvent -> {
                    resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
                });
                delay.play();
            }

                case "UNKNOWN_EMAIL" -> {

                    if (!ValidationUtils.isSimilarEmail(InputEmailTextfield.getText(), ValidationUtils.listOfKnownEmails(), 3)){

                        String toolTipText = """
                            The email address you entered is not registered in our system.\s
                            Please try again with a known email address. Remember ...\s
                            • The format should be like example@domain.com.""";


                        applyErrorStyle(InputEmailTextfield, "text-field-error");
                        toolTipService.showTooltipForDurationX(emailTooltip, toolTipText, "tooltip-Label-Error", duration);

                        PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                        delay.setOnFinished(finishedEvent -> {
                            resetFieldStyle(InputEmailTextfield, "text-field-correct", "text-field");
                            resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
                        });
                        delay.play();

                    } else {
                        applyErrorStyle(InputEmailTextfield, "text-field-error");
                        mailAutocorrect();

                        PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                        delay.setOnFinished(finishedEvent -> {
                            resetFieldStyle(InputEmailTextfield, "text-field-correct", "text-field");
                            resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
                        });
                        delay.play();
                    }

                }

            case "INVALID_CREDENTIALS" -> {

                String toolTipText = """
                        The Password you entered is incorrect! Please try again with a correct Password.""";

                applyErrorStyle(InputPassword, "password-field-error");
                toolTipService.showTooltipForDurationX(passwordTooltip, toolTipText, "tooltip-Label-Error", 5);

                PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                delay.setOnFinished(finishedEvent -> {
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

                applyErrorStyle(InputPassword, "password-field-error");
                toolTipService.showTooltipForDurationX(passwordTooltip, toolTipText, "tooltip-Label-Error", 5);

                PauseTransition delay = new PauseTransition(Duration.seconds(10));
                delay.setOnFinished(finishedEvent -> {
                    resetFieldStyle(InputPassword, "password-field-error", "password-field");
                });

            }
        }

        } else { //Validation was successful. Changing Scene to Landing Page.
            DatabaseHelper db = new DatabaseHelper();
            final UUID userUUID;
            try {
                userUUID = db.getUserUUIDByEmail(InputEmailTextfield.getText());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (stayLoggedInCheckbox.isSelected()) {
                AutoLoginHandler.activateAutoLogin(userUUID.toString());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Landing-Page.fxml", "Landing Page");

            // Immediately show the popup
            SceneSwitcher.switchToPopupWithData(
                    stage,
                "/application/studyspace/onboarding/OnboardingPage1.fxml",
                "Planify Onboarding",
                (OnboardingPage1Controller controller) -> controller.setUserUUID(userUUID)
            );
        }
    }

    /**
     * Performs email autocorrection by analyzing the user-provided email input
     * and suggesting the most likely intended email address from the database.

     * The method uses the `executeDeepSeekAPI` to call an external AI-based service
     * to analyze and correct the email input. It includes the following steps:
     * 1. Constructs a dynamic input request string containing the user-provided email,
     *    and a list of potential email options fetched from the database using `SELECT`.
     * 2. Executes the AI-based autocorrection via the `executeDeepSeekAPI`.
     * 3. Displays a tooltip popup with the suggested correction using the `toolTipService`.
     *    The user can choose to accept the correction, which updates the input email field
     *    by invoking the `setInputEmail` method.
     */

    private void mailAutocorrect() {
        String dynamicInput = "You are an E-Mail Autocorrector. The following E-Mail has been provided: "
                + InputEmailTextfield.getText();
        dynamicInput += " Please look at all available E-Mails and choose the one that the user was likely to enter:";

        dynamicInput += " " + SELECT("email", "users", null, null) + "As Output please only provide the corrected E-Mail. Nothing else!";
        String email = executeDeepSeekAPI(dynamicInput);
        String TooltipText;
        TooltipText = """
                The E-Mail you entered is not registered.
                However, we might have found a possible valid email
                associated with your input. Would you like to
                accept our correction?""";

        toolTipService.showAutocorrectPopup(emailTooltip, TooltipText, "tooltip-Label-Error-Autocorrect", 0, () -> setInputEmail(email));
    }

    /**
     * Sets the input email in the email text field, applies a correction style,
     * and logs validation status after the correction.
     *
     * @param email The email address to set and validate.
     */
    public void setInputEmail(String email) {
        InputEmailTextfield.setText(email);
        applyErrorStyle(InputEmailTextfield, "text-field-correct");
        System.out.println("The user accepted our correction for the E-Mail input.");

        PauseTransition delay = new PauseTransition(Duration.seconds(10));
        delay.setOnFinished(finishedEvent -> {
            resetFieldStyle(InputPassword, "password-field-correct", "password-field");
        });

        if (ValidationUtils.isValidEmail(email)) {
            System.out.println("The corrected email is still invalid.");
        } else {
            System.out.println("The corrected email is now valid.");
        }
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
            The Password should fulfill the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            
            Please store the Password in a secure place.""";

        toolTipService.createCustomTooltip(passwordTooltip, tooltipText, "tooltip-Label");

        tooltipText = """
                Please your registered email address:
                
                Remember ...
                • The format should be like example@domain.com.""";

        toolTipService.createCustomTooltip(emailTooltip, tooltipText, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
        StackPane.setAlignment(Image03, javafx.geometry.Pos.TOP_RIGHT);

    }
}