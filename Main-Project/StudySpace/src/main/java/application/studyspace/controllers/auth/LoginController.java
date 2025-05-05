package application.studyspace.controllers.auth;

import application.studyspace.services.CreateToolTip;
import application.studyspace.services.LoginChecker;
import application.studyspace.services.SceneSwitcher;
import application.studyspace.services.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static application.studyspace.services.StylingUtility.applyErrorStyle;
import static application.studyspace.services.StylingUtility.resetFieldStyle;
import static application.studyspace.services.AIEmailCorrector.autoCorrectEmail;

public class LoginController {

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private Label passwordTooltip, emailTooltip;
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
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
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

        if (InputEmailTextfield.getText().isEmpty()) {
            String ToolTipText = """
                    You did not enter an email address! Please enter a valid email address:
                    • The format should be like example@domain.com.""";
            toolTipService.showTooltipForDurationX(emailTooltip, ToolTipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to login, but did not fill in all fields. Missing: InputEmailTextfield");

            applyErrorStyle(InputEmailTextfield, "text-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
            });
            delay.play();

        } else if (!ValidationUtils.isValidEmail(InputEmailTextfield.getText())) {
            String ToolTipText = """
                    You did not enter an  valid email address! Please enter a valid email address:
                    • The format should be like example@domain.com.""";
            toolTipService.showTooltipForDurationX(emailTooltip, ToolTipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to login, but did not fill in all fields. Missing: InputEmailTextfield");

            applyErrorStyle(InputEmailTextfield, "text-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
            });
            delay.play();

            autoCorrectEmail(InputEmailTextfield.getText());

        } else if (InputPassword.getText().isEmpty()) {
            String TooltipText = """
            You did not enter a Password! Please enter a password that fulfills the following conditions:
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)""";

            toolTipService.showTooltipForDurationX(passwordTooltip, TooltipText, "tooltip-Label-Error", 5);
            System.out.println("The user tried to login, but did not fill in all fields. Missing: InputPassword");

            applyErrorStyle(InputPassword, "password-field-error");
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputPassword, "password-field-error", "password-field");
            });
            delay.play();

        } else if (!LoginChecker.checkLogin(InputEmailTextfield.getText(), InputPassword.getText())) {
                String TooltipText = """
            The credentials you entered are invalid! Please try again with a valid email address and password.""";

                toolTipService.showTooltipForDurationX(passwordTooltip, TooltipText, "tooltip-Label-Error", 5);
                System.out.println("The user tried to login, but did not enter valid credentials.");
                applyErrorStyle(InputPassword, "password-field-error");
                applyErrorStyle(InputEmailTextfield, "text-field-error");

                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(finishedEvent -> {
                resetFieldStyle(InputPassword, "passwort-field-error", "password-field");
                resetFieldStyle(InputEmailTextfield, "text-field-error", "text-field");
                });
                delay.play();
        } else {
            SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Landing-Page.fxml", "Landing-Page");
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





}