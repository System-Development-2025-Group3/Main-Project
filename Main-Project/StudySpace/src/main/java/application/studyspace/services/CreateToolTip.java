package application.studyspace.services;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.Objects;

public class CreateToolTip {
    /**
     * Creates a custom tooltip for a specified target node, displaying the provided text
     * when the mouse hovers over the node. The tooltip automatically hides when the mouse
     * exits the node's bounds.
     *
     * @param targetNode  the node to which the custom tooltip will be attached
     * @param tooltipText the text content to display within the custom tooltip
     */
    public void createCustomTooltip(Node targetNode, String tooltipText, String styleClass) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);
        tooltipLabel.getStyleClass().add(styleClass);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application/studyspace/Stylesheet.css")).toExternalForm());

        targetNode.setOnMouseEntered(e -> {
            Bounds bounds = targetNode.localToScreen(targetNode.getBoundsInLocal());
            customTooltip.show(targetNode, bounds.getMaxX() + 5, bounds.getMinY());
        });

        targetNode.setOnMouseExited(e -> customTooltip.hide());
    }

    public void showTooltipForDurationX(Node targetNode, String tooltipText, String styleClass, int durationInSeconds) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);
        tooltipLabel.getStyleClass().add(styleClass);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application/studyspace/Stylesheet.css")).toExternalForm());

        Bounds bounds = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, bounds.getMaxX() + 5, bounds.getMinY());

        PauseTransition delay = new PauseTransition(Duration.seconds(durationInSeconds));
        delay.setOnFinished(e -> customTooltip.hide());
        delay.play();
    }


}
