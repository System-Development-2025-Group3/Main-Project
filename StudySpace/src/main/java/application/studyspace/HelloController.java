package application.studyspace;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;

public class HelloController {

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
    private TextField WelcomeTitleTextfield, DescriptionPasswordTextfield, DescriptionEmailTextfield, InputEmailTextfield;

    @FXML
    public void initialize() {

        //Dynamically Adjusting the Elements on the Login Page
        //Arch 1
        arc1.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.4));
        arc1.radiusYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc1.centerXProperty().bind(
                AnchorPaneLogin.widthProperty()
        );
        arc1.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc2.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.25));
        arc2.radiusYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        //Arch 2
        arc2.centerXProperty().bind(
                AnchorPaneLogin.widthProperty()
        );
        arc2.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc3.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.1));
        arc3.radiusYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc3.centerXProperty().bind(
                AnchorPaneLogin.widthProperty()
        );
        arc3.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        // Picture of Laptop
        if (Image01.layoutXProperty().isBound()) {
            Image01.layoutXProperty().unbind();
        }
        if (Image01.layoutYProperty().isBound()) {
            Image01.layoutYProperty().unbind();
        }

        // Bind fitWidth and fitHeight to AnchorPane proportions (visual scaling)
        Image01.fitWidthProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.50));
        Image01.fitHeightProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.50));

        // Define offset: 2 times the width of the Image01 (you can adjust this to a specific value if needed)
        double offsetMultiplier = 0.75;

        // Dynamically set initial topAnchor and leftAnchor with offset
        AnchorPane.setLeftAnchor(Image01, (AnchorPaneLogin.getWidth() - Image01.getFitWidth()) / 2 + (Image01.getFitWidth() * offsetMultiplier));
        AnchorPane.setTopAnchor(Image01, (AnchorPaneLogin.getHeight() - Image01.getFitHeight()) / 2);

        // Add listeners to dynamically reapply anchors with the offset when resizing
        AnchorPaneLogin.widthProperty().addListener((observable, oldValue, newValue) -> {
            double centerX = (newValue.doubleValue() - Image01.getFitWidth()) / 2 + (Image01.getFitWidth() * offsetMultiplier);
            AnchorPane.setLeftAnchor(Image01, centerX);
        });

        AnchorPaneLogin.heightProperty().addListener((observable, oldValue, newValue) -> {
            double centerY = (newValue.doubleValue() - Image01.getFitHeight()) / 2;
            AnchorPane.setTopAnchor(Image01, centerY);
        });

        //Adjustment of the Login Section (Pane)
        double offsetMultiplierPane = 0.5;

        LoginPane.prefWidthProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.4));
        LoginPane.prefHeightProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.5));

        // Unbind layoutX of LoginPane (if bound)
        if (LoginPane.layoutXProperty().isBound()) {
            LoginPane.layoutXProperty().unbind();
        }

        AnchorPane.setLeftAnchor(LoginPane, (AnchorPaneLogin.getWidth() - LoginPane.getPrefWidth()) / 2 - (LoginPane.getPrefWidth() * offsetMultiplierPane));
        AnchorPane.setTopAnchor(LoginPane, (AnchorPaneLogin.getHeight() - LoginPane.getPrefHeight()) / 2);

        AnchorPaneLogin.widthProperty().addListener((observable, oldValue, newValue) -> {
            double centerX = (newValue.doubleValue() - LoginPane.getPrefWidth()) / 2 - (LoginPane.getPrefWidth() * offsetMultiplierPane);
            AnchorPane.setLeftAnchor(LoginPane, centerX);
        });

        AnchorPaneLogin.heightProperty().addListener((observable, oldValue, newValue) -> {
            double centerY = (newValue.doubleValue() - LoginPane.getPrefHeight()) / 2;
            AnchorPane.setTopAnchor(LoginPane, centerY);
        });

        //Contents of the Login Pane
        double cumulativeHeight = 0.05; // Initial top margin (5% of LoginPane height)

        // Welcome Title
        WelcomeTitleTextfield.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        WelcomeTitleTextfield.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        WelcomeTitleTextfield.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        WelcomeTitleTextfield.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1; // Account for the element's height (10% of LoginPane) + spacing

        // Text Flow
        textFlow.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        textFlow.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        textFlow.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        textFlow.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1; // Update cumulative height

        // Description Email
        DescriptionEmailTextfield.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        DescriptionEmailTextfield.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        DescriptionEmailTextfield.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        DescriptionEmailTextfield.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1;

        // Input Email
        InputEmailTextfield.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        InputEmailTextfield.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        InputEmailTextfield.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        InputEmailTextfield.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1;

        // Description Password
        DescriptionPasswordTextfield.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        DescriptionPasswordTextfield.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        DescriptionPasswordTextfield.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        DescriptionPasswordTextfield.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1;

        // Input Password
        InputPassword.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        InputPassword.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        InputPassword.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        InputPassword.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1;

        // Stay Logged In Checkbox
        StayLoggedInCheckBox.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.8));
        StayLoggedInCheckBox.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        StayLoggedInCheckBox.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.1));
        StayLoggedInCheckBox.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));
        cumulativeHeight += 0.1;

        // Submit Button
        SubmitLoginButton.prefWidthProperty().bind(LoginPane.widthProperty().multiply(0.2));
        SubmitLoginButton.prefHeightProperty().bind(LoginPane.heightProperty().multiply(0.1));
        SubmitLoginButton.layoutXProperty().bind(LoginPane.widthProperty().multiply(0.4)); // Centered horizontally
        SubmitLoginButton.layoutYProperty().bind(LoginPane.heightProperty().multiply(cumulativeHeight));

        WelcomeTitleTextfield.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.05).asString(
                WelcomeTitleTextfield.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        // Similarly for all other elements
        textFlow.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                textFlow.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        DescriptionEmailTextfield.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                DescriptionEmailTextfield.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        InputEmailTextfield.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                InputEmailTextfield.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        DescriptionPasswordTextfield.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                DescriptionPasswordTextfield.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        InputPassword.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                InputPassword.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        StayLoggedInCheckBox.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.04).asString(
                StayLoggedInCheckBox.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

        SubmitLoginButton.styleProperty().bind(
            LoginPane.heightProperty().multiply(0.05).asString(
                SubmitLoginButton.getStyle() + "; -fx-font-size: %.2fpx;"
            )
        );

    }


}