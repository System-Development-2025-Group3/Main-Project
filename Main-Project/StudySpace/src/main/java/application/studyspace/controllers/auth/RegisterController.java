package application.studyspace.controllers.auth;

import application.studyspace.services.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

import java.awt.event.ActionEvent;

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
    private void handleBackToLoginClick(javafx.event.ActionEvent event) {
        SceneSwitcher.switchTo(((javafx.scene.Node) event.getSource()), "/application/studyspace/Login.fxml", "Login");
    }

}
