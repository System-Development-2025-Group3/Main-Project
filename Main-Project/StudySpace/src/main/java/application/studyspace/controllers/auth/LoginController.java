package application.studyspace.controllers.auth;

import application.studyspace.services.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LoginController extends BaseController {

    @FXML private Label WelcomeTitle;
    @FXML private TextFlow textFlow;
    @FXML private Text linkText;

    @FXML private TextField DescriptionEmailTextfield;
    @FXML private TextField InputEmailTextfield;
    @FXML private TextField DescriptionPasswordTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private CheckBox StayLoggedInCheckBox;
    @FXML private Button SubmitLoginButton;

    @FXML private AnchorPane AnchorPaneLogin;
    @FXML private ImageView Image01;

    @FXML
    public void initialize() {
        initializeBase();  // Initializes shared layout behavior (e.g., image scaling)
        linkText.setOnMouseClicked(this::handleRegisterTextClick);
    }

    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
    }
}
