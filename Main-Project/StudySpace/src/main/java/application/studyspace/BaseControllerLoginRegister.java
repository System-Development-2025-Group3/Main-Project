package application.studyspace;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

public class BaseControllerLoginRegister {

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
        private Text linkText;

        @FXML
        public void initializeBase() {

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
    }

    protected static class LayoutBindings {

        /**
         * Reusable method to dynamically bind layout properties for UI elements.
         *
         * @param element          The UI element (Region) being configured.
         * @param parentPane       The parent Pane in which the element resides.
         * @param cumulativeHeight The current Y offset for layout positioning.
         * @param widthRatio       The proportional width of the element relative to the parent Pane.
         * @param heightRatio      The proportional height of the element relative to the parent Pane.
         * @return The updated cumulativeHeight for the next UI element.
         */
        public static double configureElementBindings(Region element, Pane parentPane, double cumulativeHeight, double widthRatio, double heightRatio) {
            // Bind the element's width and height properties to the parent pane
            element.prefWidthProperty().bind(parentPane.widthProperty().multiply(widthRatio));
            element.prefHeightProperty().bind(parentPane.heightProperty().multiply(heightRatio));

            // Bind the element's layout X and Y properties for positioning
            element.layoutXProperty().bind(parentPane.widthProperty().multiply(0.1)); // Fixed horizontal offset
            element.layoutYProperty().bind(parentPane.heightProperty().multiply(cumulativeHeight)); // Vertical offset

            // Update and return the cumulativeHeight for the next element
            return cumulativeHeight + heightRatio;
        }

        /**
         * Binds the font size of a UI element dynamically based on the height of the parent Pane.
         *
         * @param region       The UI element (e.g., TextField, Button) whose font size needs to be adjusted.
         * @param parentPane   The parent Pane whose height will determine the font size.
         * @param fontScale    The scale factor for the font size relative to the parent Pane's height.
         */
        public static void bindFontSize(Region region, Pane parentPane, double fontScale) {
            region.styleProperty().bind(
                    parentPane.heightProperty().multiply(fontScale).asString(
                            region.getStyle() + "; -fx-font-size: %.2fpx;"
                    )
            );
        }


    }
}
