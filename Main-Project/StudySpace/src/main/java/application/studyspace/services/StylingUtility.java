package application.studyspace.services;

import javafx.scene.control.TextField;

public class StylingUtility {
    /**
     * Resets the style of the given input field by removing the error style class
     * and ensuring the default style is applied.
     *
     * @param field The input field (TextField or PasswordField) whose style needs to be reset
     */
    public static void resetFieldStyle(TextField field, String errorStyle, String normalStyle) {
        field.getStyleClass().removeAll(errorStyle, normalStyle);

        if (!field.getStyleClass().contains(normalStyle)) {
            field.getStyleClass().add(normalStyle);
            System.out.println("Normal Style " + normalStyle + " has been applied to " + field.getId() + ".");
        }
    }

    /**
     * Applies an error style to the given input field if it is not already present.
     *
     * @param field The input field (TextField, PasswordField) to apply the error style to.
     * @param errorStyle The name of the error style class to be applied (e.g., "password-field-error").
     */
    public static void applyErrorStyle(TextField field, String errorStyle) {
        if (!field.getStyleClass().contains(errorStyle)) {
            field.getStyleClass().add(errorStyle);
            System.out.println("Error Style has been applied to " + field.getId() + ".");
        }
    }
}