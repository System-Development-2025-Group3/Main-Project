package application.studyspace.controllers.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

public class BaseController {

    @FXML protected VBox LoginCard;
    @FXML protected AnchorPane AnchorPaneLogin;
    @FXML protected ImageView Image01;
    @FXML private Arc arc1, arc2, arc3;

    @FXML
    public void initializeBase() {
        if (AnchorPaneLogin == null || LoginCard == null || Image01 == null) return;

        LoginCard.prefWidthProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.36));
        LoginCard.prefHeightProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.6));

        Image01.fitWidthProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.32));
        Image01.fitHeightProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.55));

        AnchorPaneLogin.widthProperty().addListener((obs, oldVal, newVal) ->
                AnchorPane.setLeftAnchor(LoginCard, newVal.doubleValue() * 0.10));
        AnchorPaneLogin.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> {
                    double cardHeight = LoginCard.getBoundsInParent().getHeight();
                    AnchorPane.setTopAnchor(LoginCard, (newVal.doubleValue() - cardHeight) / 2);
                }));

        AnchorPaneLogin.widthProperty().addListener((obs, oldVal, newVal) ->
                AnchorPane.setRightAnchor(Image01, newVal.doubleValue() * 0.06));
        AnchorPaneLogin.heightProperty().addListener((obs, oldVal, newVal) ->
                AnchorPane.setTopAnchor(Image01, (newVal.doubleValue() - Image01.getFitHeight()) / 2 - 10));

        arc1.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.40));
        arc1.radiusYProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.5));
        arc1.centerXProperty().bind(AnchorPaneLogin.widthProperty());
        arc1.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc2.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.30));
        arc2.radiusYProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.5));
        arc2.centerXProperty().bind(AnchorPaneLogin.widthProperty());
        arc2.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));

        arc3.radiusXProperty().bind(AnchorPaneLogin.widthProperty().multiply(0.22));
        arc3.radiusYProperty().bind(AnchorPaneLogin.heightProperty().multiply(0.5));
        arc3.centerXProperty().bind(AnchorPaneLogin.widthProperty());
        arc3.centerYProperty().bind(AnchorPaneLogin.heightProperty().divide(2));
    }
}
