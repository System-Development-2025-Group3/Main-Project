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

    @FXML
    private ImageView Image01;

    @FXML
    private AnchorPane AnchorPaneLogin;

    @FXML
    private Arc arc1, arc2, arc3;

    @FXML
    private TextFlow textFlow;

    @FXML
    private PasswordField InputPassword;

    @FXML
    private Button SubmitLoginButton;

    @FXML
    private Pane LoginPane;

    @FXML
    private CheckBox StayLoggedInCheckBox;

    @FXML
    private TextField DescriptionPasswordTextfield, DescriptionEmailTextfield, InputEmailTextfield;

    @FXML
    private Text linkText;

    @FXML
    private Label WelcomeTitle;

    @FXML
    public void initialize() {
        initializeBase();

        //Contents of the Login Pane
        double cumulativeHeight = 0.05; // Initial top margin (5% of LoginPane height)

        // List of elements to configure
        List<Region> elementsToConfigure = new ArrayList<>();
        elementsToConfigure.add(WelcomeTitle);
        elementsToConfigure.add(textFlow);
        elementsToConfigure.add(DescriptionEmailTextfield);
        elementsToConfigure.add(InputEmailTextfield);
        elementsToConfigure.add(DescriptionPasswordTextfield);
        elementsToConfigure.add(InputPassword);
        elementsToConfigure.add(StayLoggedInCheckBox);

        // Loop through the list and apply layout bindings
        for (Region element : elementsToConfigure) {
            cumulativeHeight = LayoutBindings.configureElementBindings(element, LoginPane, cumulativeHeight, 0.8, 0.1);
        }

        cumulativeHeight += 0.05;

        //Button will be at 20% size instead of 80%
        LayoutBindings.configureElementBindings(SubmitLoginButton, LoginPane, cumulativeHeight, 0.2, 0.1);

        // Apply additional style bindings for specific elements if needed
        Map<Region, Double> elementsWithFontScales = new LinkedHashMap<>();
        elementsWithFontScales.put(WelcomeTitle, 0.05);
        elementsWithFontScales.put(textFlow, 0.04);
        elementsWithFontScales.put(DescriptionEmailTextfield, 0.04);
        elementsWithFontScales.put(InputEmailTextfield, 0.04);
        elementsWithFontScales.put(DescriptionPasswordTextfield, 0.04);
        elementsWithFontScales.put(InputPassword, 0.04);
        elementsWithFontScales.put(StayLoggedInCheckBox, 0.04);
        elementsWithFontScales.put(SubmitLoginButton, 0.05);

        // Loop through the map and apply the font size binding
        elementsWithFontScales.forEach((region, fontScale) -> {
            bindFontSize(region, LoginPane, fontScale); // Call utility method
        });

        linkText.setOnMouseClicked(this::handleRegisterTextClick);
    }

    //switches scene to the register page upon button press
    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        SceneSwitcher.switchTo(event.getSource(), "/application/studyspace/Register.fxml", "Register");
    }


}