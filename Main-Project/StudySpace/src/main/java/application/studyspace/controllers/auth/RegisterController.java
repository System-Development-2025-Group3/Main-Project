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

public class RegisterController {

    @FXML private TextField RegisterEmailField;
    @FXML private PasswordField RegisterPassword_1;
    @FXML private PasswordField RegisterPassword_2;
    @FXML private Label passwordTooltip1, emailTooltip, passwordTooltip2;
    @FXML private StackPane stackPane;
    @FXML private ImageView Image03;

    @FXML
    private void handleBacktoLoginClick(ActionEvent event) {
        SceneSwitcher.switchTo((Node) event.getSource(), "/application/studyspace/auth/Login.fxml", "Login");
    }

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleSubmitRegistrationButtonClick(ActionEvent event) {
        if (RegisterEmailField.getText().isEmpty()) {
            String ToolTipText = """
                    You did not enter an email address! Please enter a valid email address:
                    • The format should be like example@domain.com.""";
            toolTipService.showTooltipForDurationX(emailTooltip, ToolTipText, "tooltip-Label-Error", 5, 0);
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
            toolTipService.showTooltipForDurationX(passwordTooltip1, TooltipText, "tooltip-Label-Error", 5,0);
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
            toolTipService.showTooltipForDurationX(passwordTooltip1, TooltipText, "tooltip-Label-Error", 5, 0);
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