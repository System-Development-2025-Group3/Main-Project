package application.studyspace.services.Styling;

import javafx.scene.control.TextField;

public class StylingUtility {
    public static void resetFieldStyle(TextField field, String errorStyle, String normalStyle) {
        field.getStyleClass().removeAll(errorStyle, normalStyle);
        if (!field.getStyleClass().contains(normalStyle)) {
            field.getStyleClass().add(normalStyle);
        }
    }

    public static void applyErrorStyle(TextField field, String errorStyle) {
        if (!field.getStyleClass().contains(errorStyle)) {
            field.getStyleClass().add(errorStyle);
        }
    }
}
