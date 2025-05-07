package application.studyspace.services.Scenes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * SceneSwitcher handles scene content replacement while preserving fullscreen mode.
 * Instead of replacing the entire scene, it replaces the root node to avoid fullscreen flicker.
 */
public class SceneSwitcher {

    /**
     * Switches scenes from a UI element like a button.
     *
     * @param eventSource  the triggering node (e.g. Button)
     * @param fxmlPath     the path to the FXML file (e.g. "/application/studyspace/Login.fxml")
     * @param title        the new window title
     */
    public static void switchTo(Object eventSource, String fxmlPath, String title) {
        Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();
        switchTo(stage, fxmlPath, title);
    }

    /**
     * Switches scenes using a given Stage (e.g. on app startup).
     *
     * @param stage     the JavaFX stage
     * @param fxmlPath  the FXML file path (e.g. "/application/studyspace/Login.fxml")
     * @param title     the window title
     */
    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent newRoot = loader.load();

            if (stage.getScene() == null) {
                // First-time setup
                Scene scene = new Scene(newRoot);
                stage.setScene(scene);
            } else {
                // Switch just the root to preserve fullscreen
                stage.getScene().setRoot(newRoot);
            }

            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen(); // optional
            stage.show(); // safe to call again

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
