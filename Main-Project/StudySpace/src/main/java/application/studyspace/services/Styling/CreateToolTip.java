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

/**
 * Utility for creating styled tooltips and popups anchored to a JavaFX Node.
 *
 * Features:
 * - Standard tooltip that shows on hover.
 * - Tooltip that auto-hides after a set duration.
 * - Tooltip with buttons (e.g., autocorrect suggestion accept/decline).
 */
public class CreateToolTip {

    /**
     * Shows a styled tooltip when the mouse hovers over the target node.
     * The tooltip automatically hides when the mouse leaves.
     *
     * @param targetNode the node to attach the tooltip to
     * @param tooltipText text to display in the tooltip
     * @param styleClass CSS class to style the Label
     */
    public void createCustomTooltip(Node targetNode, String tooltipText, String styleClass) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        // Apply custom stylesheet
        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/settings-override.css")).toExternalForm());

        // Show tooltip when mouse enters
        targetNode.setOnMouseEntered(e -> {
            Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
            customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY());
        });
        // Hide tooltip when mouse exits
        targetNode.setOnMouseExited(e -> customTooltip.hide());
    }

    /**
     * Shows a tooltip for a specific duration, then auto-hides.
     *
     * @param targetNode the node to anchor to
     * @param tooltipText text to show
     * @param styleClass CSS class for styling
     * @param durationInSeconds how long to display the tooltip
     */
    public void showTooltipForDurationX(Node targetNode, String tooltipText, String styleClass, int durationInSeconds) {
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        Popup customTooltip = new Popup();
        customTooltip.getContent().add(tooltipLabel);
        customTooltip.setAutoHide(true);

        // Use Landing Page stylesheet
        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());

        Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY());

        // Hide after the specified duration
        PauseTransition delay = new PauseTransition(Duration.seconds(durationInSeconds));
        delay.setOnFinished(e -> customTooltip.hide());
        delay.play();
    }

    /**
     * Shows a popup with a message and action buttons (e.g., Accept autocorrect).
     *
     * @param targetNode the node to anchor the popup to
     * @param tooltipText text to show
     * @param styleClass CSS class for styling the label
     * @param offsetY vertical offset for the popup position
     * @param onExecuteAction code to run when the "Accept" button is clicked
     */
    public void showAutocorrectPopup(Node targetNode, String tooltipText, String styleClass, double offsetY, Runnable onExecuteAction) {
        Popup customTooltip = new Popup();

        // Main label
        Label tooltipLabel = new Label(tooltipText);
        tooltipLabel.getStyleClass().add(styleClass);
        tooltipLabel.setWrapText(true);
        tooltipLabel.setMinWidth(200);

        // Accept button
        Button accept = new Button("Accept");
        accept.getStyleClass().add("execute-button");
        accept.setOnAction(e -> {
            if (onExecuteAction != null) onExecuteAction.run();
            customTooltip.hide();
        });

        // Close button
        Button close = new Button("X");
        close.getStyleClass().add("close-button");
        close.setOnAction(e -> customTooltip.hide());

        // Buttons in horizontal row
        HBox btns = new HBox(10, accept, close);
        btns.setAlignment(Pos.CENTER_RIGHT);

        // Overall box layout
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
        customTooltip.setAutoHide(false); // require user action to close

        // Add CSS to label and buttons
        tooltipLabel.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());
        accept.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());
        close.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/application/studyspace/styles/LandingPage/SettingsStylesheet.css")).toExternalForm());

        // Show the popup near the target node
        Bounds b = targetNode.localToScreen(targetNode.getBoundsInLocal());
        customTooltip.show(targetNode, b.getMaxX() + 5, b.getMinY() + offsetY);
    }
}
