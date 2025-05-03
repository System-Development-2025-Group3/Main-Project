package application.studyspace.controllers;

import application.studyspace.MainApp;
import javafx.fxml.FXML;
import application.studyspace.services.SceneSwitcher;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Arc;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static application.studyspace.controllers.BaseController.LayoutBindings.bindFontSize;

public class LoginController extends BaseController {

    @FXML private ImageView Image01;
    @FXML private AnchorPane AnchorPaneLogin;
    @FXML private Arc arc1, arc2, arc3;
    @FXML private TextFlow textFlow;
    @FXML private PasswordField InputPassword;
    @FXML private Button SubmitLoginButton;
    @FXML private Pane LoginPane;
    @FXML private CheckBox StayLoggedInCheckBox;
    @FXML private TextField DescriptionPasswordTextfield, DescriptionEmailTextfield, InputEmailTextfield;
    @FXML private Text linkText;
    @FXML private Label WelcomeTitle;

    @FXML
    public void initialize() {
        initializeBase();  // Still handles arcs + image
        linkText.setOnMouseClicked(this::handleRegisterTextClick);
    }

    //switches scene to the register page upon button press
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
    }


}