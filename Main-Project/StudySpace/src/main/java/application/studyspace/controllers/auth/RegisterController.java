package application.studyspace.controllers.auth;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class RegisterController extends BaseController {

    @FXML private Label TextAreaRegister;
    @FXML private Button SubmitRegistrationButton;
    @FXML private PasswordField RegisterPassword_1, RegisterPassword_2;
    @FXML private Pane LoginPane;
    @FXML private TextField WelcomeTitleTextfield, DescriptionPasswordTextfield_1, DescriptionEmailTextfield, RegisterEmailField, DescriptionPasswordTextfield_2;

    @FXML
    public void initialize() {
        initializeBase(); // handles arcs/image if needed
    }
}