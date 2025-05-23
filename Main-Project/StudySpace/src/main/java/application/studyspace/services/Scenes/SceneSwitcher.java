package application.studyspace.services.Scenes;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color; // at the top of the file

import java.io.IOException;
import java.net.URL;

/**
 * SceneSwitcher handles scene content replacement while preserving fullscreen mode.
 * Instead of replacing the entire scene, it replaces the root node to avoid fullscreen flicker.
 */
public class SceneSwitcher {

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void init(T controller);
    }

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

    public static void switchToPopup(Stage ownerStage, String fxmlPath, String title) {
        try {
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent popupRoot = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle(title);

            Scene popupScene = new Scene(popupRoot);
            popupStage.setScene(popupScene);

            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setResizable(false);

            popupStage.showAndWait();

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static <T> void switchToWithData(Object eventSource, String fxmlPath, String title, ControllerInitializer<T> initializer) {
        Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();

            T controller = loader.getController();
            initializer.init(controller); // Hier z. B. setUserUUID()

            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void switchToPopupWithData(Stage ownerStage, String fxmlPath, String title, ControllerInitializer<T> initializer) {
        try {
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent popupRoot = loader.load();

            T controller = loader.getController();
            initializer.init(controller);

            // ✅ Create transparent scene
            Scene scene = new Scene(popupRoot);
            scene.setFill(Color.TRANSPARENT);

            Stage popupStage = new Stage();
            popupStage.setTitle(title);
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setResizable(false);
            popupStage.setAlwaysOnTop(true);
            popupStage.setScene(scene);

            popupStage.showAndWait();

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void closePopup(Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("❌ Cannot close popup: source node is not attached to a scene.");
        }
    }
}

