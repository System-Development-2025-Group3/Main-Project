package application.studyspace.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneSwitcher {

    /**
     * Switches to the specified FXML file within the same Stage.
     *
     * @param eventSource The UI node (typically a Button or Label) that triggered the switch.
     * @param fxmlPath    The path to the FXML file (e.g., "/application/studyspace/Register.fxml").
     * @param title       The title to set on the new Stage.
     */
    public static void switchTo(Object eventSource, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent view = loader.load();

            Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();
            stage.setScene(new Scene(view));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error switching scene to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}