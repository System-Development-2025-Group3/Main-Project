package application.studyspace.services.Styling;

import javafx.animation.PauseTransition;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utility methods for styling input fields and showing validation errors.
 */
public class StylingUtility {

    /**
     * Resets a text field's CSS classes: removes error and normal styles, then adds normal.
     */
    public static void resetFieldStyle(TextField field, String errorStyle, String normalStyle) {
        field.getStyleClass().removeAll(errorStyle, normalStyle);
        if (!field.getStyleClass().contains(normalStyle)) {
            field.getStyleClass().add(normalStyle);
        }
    }

    /**
     * Applies an error CSS style to a text field.
     */
    public static void applyErrorStyle(TextField field, String errorStyle) {
        if (!field.getStyleClass().contains(errorStyle)) {
            field.getStyleClass().add(errorStyle);
        }
    }

    /**
     * Shows a styled tooltip for a duration, applies error style, then resets field style.
     * @param field the TextField to style
     * @param tooltipService service to show tooltip
     * @param tooltipLabel the label acting as tooltip target
     * @param message the error message
     * @param tooltipStyle CSS class for the tooltip label
     * @param errorStyle CSS class to apply for error
     * @param normalStyle CSS class to restore after delay
     * @param seconds how long to show the tooltip
     */
    public static void showError(TextField field,
                                 CreateToolTip tooltipService,
                                 Label tooltipLabel,
                                 String message,
                                 String tooltipStyle,
                                 String errorStyle,
                                 String normalStyle,
                                 int seconds) {
        applyErrorStyle(field, errorStyle);
        // show tooltip next to label
        tooltipService.showTooltipForDurationX(
                tooltipLabel,
                message,
                tooltipStyle,
                seconds
        );
        // hide tooltip and reset style after delay
        PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
        delay.setOnFinished(e -> resetFieldStyle(field, errorStyle, normalStyle));
        delay.play();
    }

    /**
     * Shows a success message and applies success styling to a control.
     *
     * @param control       The control (e.g., TextField) to style.
     * @param message       The success message to display in a tooltip.
     * @param successClass  The CSS class to apply for success styling.
     * @param originalClass The default CSS class to revert to.
     */
    public static void showSuccess(Control control, String message, String successClass, String originalClass) {
        Tooltip successTooltip = new Tooltip(message);
        successTooltip.getStyleClass().add("tooltip-Success");

        // Position and show the tooltip
        double tooltipX = control.localToScreen(control.getBoundsInLocal()).getMinX();
        double tooltipY = control.localToScreen(control.getBoundsInLocal()).getMaxY();
        successTooltip.show(control, tooltipX, tooltipY);

        control.getStyleClass().remove(originalClass);
        control.getStyleClass().add(successClass);

        // Hide the tooltip and revert style after a delay
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            successTooltip.hide();
            control.getStyleClass().remove(successClass);
            control.getStyleClass().add(originalClass);
        });
        delay.play();
    }

}
