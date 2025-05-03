package application.studyspace.services;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    /**
     * Switches to a new FXML scene within the existing Stage,
     * setting the window to the user's native screen resolution in windowed mode.
     *
     * @param eventSource The UI control (like Button or Label) that triggered the scene change.
     * @param fxmlPath    The path to the target FXML file, e.g., "/application/studyspace/Register.fxml".
     * @param title       The window title for the new scene.
     */
    public static void switchTo(Object eventSource, String fxmlPath, String title) {
        try {
            // Load the FXML layout for the new scene
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent view = loader.load();

            // Retrieve the current stage from the triggering UI element
            Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();

            // Apply the new scene
            Scene newScene = new Scene(view);
            stage.setScene(newScene);

            // Set window size to native screen resolution
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Disable fullscreen mode and ensure window is resizable
            stage.setFullScreen(false);
            stage.setResizable(true);

            // Optional: Center window on screen
            stage.centerOnScreen();

            // Update window title and show
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error switching scene to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
