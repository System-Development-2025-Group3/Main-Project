package application.studyspace;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static application.studyspace.BaseControllerLoginRegister.LayoutBindings.bindFontSize;

public class RegisterController extends BaseControllerLoginRegister {

    @FXML
    private TextArea TextAreaRegister;

    @FXML
    private Button SubmitRegistrationButton;

    @FXML
    private PasswordField RegisterPassword_1, RegisterPassword_2;

    @FXML
    private Pane LoginPane;

    @FXML
    private TextField WelcomeTitleTextfield, DescriptionPasswordTextfield_1, DescriptionEmailTextfield, RegisterEmailField, DescriptionPasswordTextfield_2;

    @FXML
    public void initialize() {
        initializeBase();

        // Initialize positions and bindings for elements
        double cumulativeHeight = 0.05; // Start with an initial offset (5% of LoginPane height)

        // List of elements to configure
        List<Region> elementsToConfigure = new ArrayList<>();
        elementsToConfigure.add(WelcomeTitleTextfield);
        elementsToConfigure.add(TextAreaRegister);
        elementsToConfigure.add(DescriptionEmailTextfield);
        elementsToConfigure.add(RegisterEmailField);
        elementsToConfigure.add(DescriptionPasswordTextfield_1);
        elementsToConfigure.add(RegisterPassword_1);
        elementsToConfigure.add(DescriptionPasswordTextfield_2);
        elementsToConfigure.add(RegisterPassword_2);

        // Loop through the list and apply layout bindings
        for (Region element : elementsToConfigure) {
            cumulativeHeight = LayoutBindings.configureElementBindings(element, LoginPane, cumulativeHeight, 0.8, 0.1);
        }

        //Button will be at 20% size instead of 80%
        LayoutBindings.configureElementBindings(SubmitRegistrationButton, LoginPane, cumulativeHeight, 0.2, 0.1);

        // Apply additional style bindings for specific elements if needed
        Map<Region, Double> elementsWithFontScales = new LinkedHashMap<>();
        elementsWithFontScales.put(WelcomeTitleTextfield, 0.05);
        elementsWithFontScales.put(TextAreaRegister, 0.04);
        elementsWithFontScales.put(DescriptionEmailTextfield, 0.04);
        elementsWithFontScales.put(RegisterEmailField, 0.04);
        elementsWithFontScales.put(DescriptionPasswordTextfield_1, 0.04);
        elementsWithFontScales.put(RegisterPassword_1, 0.04);
        elementsWithFontScales.put(DescriptionPasswordTextfield_2, 0.04);
        elementsWithFontScales.put(RegisterPassword_2, 0.05);

        // Loop through the map and apply the font size binding
        elementsWithFontScales.forEach((region, fontScale) -> {
            bindFontSize(region, LoginPane, fontScale); // Call utility method
        });
    }
}