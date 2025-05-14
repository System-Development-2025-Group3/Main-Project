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

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application/studyspace/styles/Stylesheet.css")).toExternalForm());

        targetNode.setOnMouseEntered(e -> {
            Bounds bounds = targetNode.localToScreen(targetNode.getBoundsInLocal());
            customTooltip.show(targetNode, bounds.getMaxX() + 5, bounds.getMinY());
        });

        targetNode.setOnMouseExited(e -> customTooltip.hide());
    }

    public void showTooltipForDurationX(Node targetNode, String tooltipText, String styleClass, int durationInSeconds, double offsetY) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);
        tooltipLabel.getStyleClass().add(styleClass);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        tooltipLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application/studyspace/styles/Stylesheet.css")).toExternalForm());
        
        Bounds bounds = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, bounds.getMaxX() + 5, bounds.getMinY() + offsetY);
        
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

         Button executeButton = new Button("Accept");
         executeButton.getStyleClass().add("execute-button");
         executeButton.setOnAction(event -> {
             if (onExecuteAction != null) {
                 onExecuteAction.run();
             }
             customTooltip.hide();
         });

         Button closeButton = new Button("X");
         closeButton.getStyleClass().add("close-button");
         closeButton.setOnAction(event -> customTooltip.hide());

         HBox buttonBox = new HBox(10, executeButton, closeButton);
         buttonBox.setAlignment(Pos.CENTER_RIGHT);

         VBox contentBox = new VBox(10, tooltipLabel, buttonBox);
         contentBox.setStyle("-fx-background-color: #f68f8f; " +
                             "-fx-border-color: #bf3d3d; " +
                             "-fx-border-width: 1; " +
                             "-fx-border-radius: 10; " +
                             "-fx-background-radius: 10; " +
                             "-fx-padding: 15;");
         contentBox.setAlignment(Pos.CENTER);

         customTooltip.getContent().add(contentBox);
         customTooltip.setAutoHide(false);

         tooltipLabel.getStylesheets().add(Objects.requireNonNull(
             getClass().getResource("/application/studyspace/styles/Stylesheet.css")).toExternalForm());
         executeButton.getStylesheets().add(Objects.requireNonNull(
             getClass().getResource("/application/studyspace/styles/Stylesheet.css")).toExternalForm());
         closeButton.getStylesheets().add(Objects.requireNonNull(
             getClass().getResource("/application/studyspace/styles/Stylesheet.css")).toExternalForm());

         Bounds bounds = targetNode.localToScreen(targetNode.getBoundsInLocal());
         customTooltip.show(targetNode, bounds.getMaxX() + 5, bounds.getMinY() + offsetY);
     }
}