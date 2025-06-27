package application.studyspace.services.Styling;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.Objects;

public class CreateToolTip {
    public void createCustomTooltip(Node targetNode, String tooltipText, String styleClass) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/settings-override.css")).toExternalForm());

        targetNode.setOnMouseEntered(e -> {
            Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
            customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY());
        });
        targetNode.setOnMouseExited(e -> customTooltip.hide());
    }

    public void showTooltipForDurationX(Node targetNode, String tooltipText, String styleClass, int durationInSeconds) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());

        Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY());

        PauseTransition delay = new PauseTransition(Duration.seconds(durationInSeconds));
        delay.setOnFinished(e -> customTooltip.hide());
        delay.play();
    }

    public void showAutocorrectPopup(Node targetNode, String tooltipText, String styleClass, double offsetY, Runnable onExecuteAction) {
        Popup customTooltip = new Popup();

        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        Button accept = new Button("Accept");
        accept.getStyleClass().add("execute-button");
        accept.setOnAction(e -> {
            if (onExecuteAction != null) onExecuteAction.run();
            customTooltip.hide();
        });

        Button close = new Button("X");
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> customTooltip.hide());

        HBox btns = new HBox(10, accept, close);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, tooltipLabel, btns);
        box.setStyle("""
            -fx-background-color: #f68f8f;
            -fx-border-color: #bf3d3d;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-padding: 15;
        """);
        box.setAlignment(Pos.CENTER);

        customTooltip.getContent().add(box);
        customTooltip.setAutoHide(false);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());
        accept.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());
        close.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());

        Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY() + offsetY);
    }
}
