package application.studyspace.controllers.auth;

import application.studyspace.services.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LoginController {

    // Top-level container (the root <Pane>)
    @FXML private Pane AnchorPaneLogin;

    // Login card container
    @FXML private Pane LoginCard;

    // UI elements inside the login card
    @FXML private Label WelcomeTitle;
    @FXML private TextFlow textFlow;
    @FXML private Text linkText;

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;

    @FXML private CheckBox StayLoggedInCheckBox;
    @FXML private Button SubmitLoginButton;

    // Right-side laptop image
    @FXML private ImageView Image01;

    // Handle register link click
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
    }
}
