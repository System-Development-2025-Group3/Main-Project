package application.studyspace.services.Styling;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
}
