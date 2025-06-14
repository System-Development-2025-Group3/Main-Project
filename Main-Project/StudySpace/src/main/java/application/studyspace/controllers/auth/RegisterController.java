package application.studyspace.controllers.auth;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.Styling.CreateToolTip;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;
import static application.studyspace.services.Styling.StylingUtility.applyErrorStyle;
import static application.studyspace.services.Styling.StylingUtility.resetFieldStyle;

/**
 * The RegisterController class manages the registration view in the application. It handles
 * user interactions during the registration process, including input validation, showing appropriate
 * feedback, and transitioning between scenes.
 *
 * This controller is linked to an FXML file, and its methods are annotated with @FXML to allow
 * integration with JavaFX elements.
 */
public class RegisterController {

    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1;
    @FXML private PasswordField RegisterPassword_2;
    @FXML private Label passwordTooltip1, emailTooltip, passwordTooltip2;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    /**
     * Handles the click event triggered by the "Back to Login" action. This method switches
     * the application's current scene to the login screen.
     *
     * @param event The ActionEvent triggered by clicking the "Back to Login" button.
     *              It provides the source of the event required for scene switching.
     */
    @FXML
    private void handleBacktoLoginClick(ActionEvent event) {
        SceneSwitcher.switchTo((Node) event.getSource(), "/application/studyspace/auth/Login.fxml", "Login");
    }

    private final CreateToolTip toolTipService = new CreateToolTip();

    /**
     * Handles the click event of the "Submit Registration" button. Validates the input fields
     * for email and passwords, provides appropriate feedback through tooltips if validation
     * fails, and either applies styling to fields with errors or saves the data and transitions
     * to the landing page when all inputs are valid.
     *
     * Validation logic includes:
     * - Checking if the email field is empty and displaying an error tooltip if true.
     * - Checking if the password fields are empty and displaying an error tooltip if true.
     * - Ensuring the passwords entered in both fields match and providing feedback if they do not.
     *
     * If all validations pass, the method saves the email and password to the database,
     * and switches to the landing page scene.
     *
     * @param event The ActionEvent triggered by clicking the "Submit Registration" button.
     */
    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        if (RegisterEmailField.getText().isEmpty()) {
            String ToolTipText = """
                    You did not enter an email address! Please enter a valid email address:
                    • The format should be like example@domain.com.""";
            toolTipService.showTooltipForDurationX(emailTooltip, ToolTipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to register, but did not fill in all fields. Missing: RegisterEmailField");

            applyErrorStyle(RegisterEmailField, "text-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(RegisterEmailField, "text-field-error", "text-field");
            });
            delay.play();

        } else if (RegisterPassword_1.getText().isEmpty() || RegisterPassword_2.getText().isEmpty()) {
            String TooltipText = """
            You did not enter a Password! Please enter a password that fulfills the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)""";
            toolTipService.showTooltipForDurationX(passwordTooltip1, TooltipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to register, but did not fill in all fields. Missing: RegisterPassword_1 or RegisterPassword_2");

            applyErrorStyle(RegisterPassword_1, "text-field-error");
            applyErrorStyle(RegisterPassword_2, "text-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(RegisterPassword_1, "text-field-error", "text-field");
                resetFieldStyle(RegisterPassword_2, "text-field-error", "text-field");
            });
            delay.play();

        } else if (!RegisterPassword_1.getText().equals(RegisterPassword_2.getText())) {
            String TooltipText = """
            The passwords you entered do not match! Please try again with matching passwords.""";
            toolTipService.showTooltipForDurationX(passwordTooltip1, TooltipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to register, but did not enter matching passwords.");

            applyErrorStyle(RegisterPassword_1, "text-field-error");
            applyErrorStyle(RegisterPassword_2, "text-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(RegisterPassword_1, "text-field-error", "text-field");
                resetFieldStyle(RegisterPassword_2, "text-field-error", "text-field");
            });
            delay.play();

        } else {
            saveToDatabase(RegisterEmailField.getText(), RegisterPassword_1.getText());

            try {
                SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/landingpage/Landing-Page.fxml", "Landing Page");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("❌ Error while loading FXML file Landing-Page.fxml" + e.getMessage());
            }
        }
    }

    /**
     * Initializes the UI components and sets up tooltips and bindings for certain elements
     * in the RegisterController. This method is automatically invoked when the controller
     * is loaded and its associated FXML file is initialized.
     *
     * The method configures:
     * - Custom tooltips for password and email input fields with specific helper text
     *   detailing the required formats and conditions for valid entries.
     * - Binding for an image's size properties to the dimensions of its container, ensuring
     *   dynamic resizing.
     */
    @FXML
    private void initialize() {
        String tooltipText = """
            Please enter a password that fulfills the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)""";

        toolTipService.createCustomTooltip(passwordTooltip1, tooltipText, "tooltip-Label");
        toolTipService.createCustomTooltip(passwordTooltip2, tooltipText, "tooltip-Label");

        tooltipText = """
                Please enter a valid email address:
                • The format should be like example@domain.com.""";

        toolTipService.createCustomTooltip(emailTooltip, tooltipText, "tooltip-Label");

        Image03.fitWidthProperty().bind(stackPane.widthProperty());
        Image03.fitHeightProperty().bind(stackPane.heightProperty());
        StackPane.setAlignment(Image03, javafx.geometry.Pos.TOP_RIGHT);

    }

}