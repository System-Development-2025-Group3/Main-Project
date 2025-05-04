package application.studyspace.controllers.auth;

import application.studyspace.services.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

public class RegisterController {

    @FXML
    private AnchorPane AnchorPaneLogin;

    @FXML
    private VBox LoginCard;

    @FXML
    private Label TextAreaRegister;

    @FXML
    private TextField RegisterEmailField;

    @FXML
    private PasswordField RegisterPassword_1;

    @FXML
    private PasswordField RegisterPassword_2;

    @FXML
    private Button SubmitRegistrationButton;

    @FXML
    private ImageView Image01;

    @FXML
    private Arc arc1;

    @FXML
    private Arc arc2;

    @FXML
    private Arc arc3;

    @FXML
    private Hyperlink BackToLoginLink;

    @FXML
    private void handleBackToLoginClick(ActionEvent event) {
        SceneSwitcher.switchTo((Node) event.getSource(), "/application/studyspace/Login.fxml", "Login");
    }
}
