package application.studyspace.services.Scenes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;        // <-- changed from StackPane
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;

/**
 * SceneSwitcher handles scene content replacement while preserving fullscreen mode.
 */
public class SceneSwitcher {

    @FunctionalInterface
    public interface ControllerInitializer<T> {
        void init(T controller);
    }

    /**
     * Switches the application's current scene to a new one specified by the given FXML file path and title.
     * This method determines the stage from the event source and delegates the scene switching to the overloaded method.
     *
     * @param eventSource The source of the event that triggered the scene switch. It should be a UI element
     *                    (e.g., a Node) that is part of the current scene.
     * @param fxmlPath The relative path of the FXML file to load and set as the root of the new scene.
     * @param title The title to set for the window after the scene switch.
     */
    public static void switchTo(Object eventSource, String fxmlPath, String title) {
        Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();
        switchTo(stage, fxmlPath, title);
    }

    /**
     * Switches the current scene of the given stage to a new FXML layout.
     * This method loads the specified FXML file, sets it as the root of the stage's scene,
     * updates the stage title, and ensures it is properly displayed.
     *
     * @param stage     the primary stage to update with the new scene
     * @param fxmlPath  the path to the FXML file to be loaded
     * @param title     the title to set for the stage
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
                Scene scene = new Scene(newRoot);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(newRoot);
            }

            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Displays a popup window with a specified FXML layout and title. The popup window is initialized
     * as modal, non-resizable, and undecorated, and it blocks interaction with the owner stage
     * until the popup is closed.
     *
     * @param ownerStage the parent stage that owns the popup window. The popup is displayed on top
     *                   of this stage and blocks input to it while open.
     * @param fxmlPath   the path to the FXML file that defines the layout and structure of the popup window.
     *                   The path must be relative to the classpath.
     * @param title      the title of the popup window, displayed in the title bar (though not visible
     *                   in undecorated style).
     */
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

    /**
     * Replaces the content of an existing popup stage with a new FXML layout and initializes its controller.
     *
     * @param popupStage the {@code Stage} representing the popup window whose content is to be replaced
     * @param fxmlPath the path to the FXML file that defines the new layout
     * @param title the title to set for the popup window
     * @param initializer the functional interface to initialize the controller of the new scene
     * @param <T> the type of the controller to be initialized
     */
    public static <T> void switchPopupContent(Stage popupStage, String fxmlPath, String title, ControllerInitializer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent newRoot = loader.load();

            T controller = loader.getController();
            initializer.init(controller);

            popupStage.getScene().setRoot(newRoot);
            popupStage.setTitle(title);

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Opens a popup window with a specified FXML file and initializes its controller
     * with provided data. The popup is displayed as a modal window with transparency and
     * a dimmed background effect for the owner stage. The popup is dismissed only
     * when the user explicitly closes it.
     *
     * @param ownerStage   the stage that owns the popup, used to apply dimming effect
     * @param fxmlPath     the path to the FXML file used for the popup layout
     * @param title        the title of the popup window
     * @param initializer  a functional interface used to initialize the controller
     *                     of the popup with specific data
     * @param <T>          the type of the controller associated with the FXML file
     */
    public static <T> void switchToPopupWithData(Stage ownerStage,
                                                 String fxmlPath,
                                                 String title,
                                                 ControllerInitializer<T> initializer) {
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

            // Cast to Pane so BorderPane (etc.) works without error
            Pane ownerRoot = (Pane) ownerStage.getScene().getRoot();

            Region dimPane = new Region();
            dimPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
            dimPane.setPrefSize(ownerRoot.getWidth(), ownerRoot.getHeight());
            dimPane.setMouseTransparent(true);

            ownerRoot.getChildren().add(dimPane);
            ownerRoot.setEffect(new GaussianBlur(10));

            popupStage.showAndWait();

            ownerRoot.getChildren().remove(dimPane);
            ownerRoot.setEffect(null);

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Closes the popup window associated with the specified source node.
     * This method retrieves the Stage object from the scene of the source node and closes it.
     * If the source node is null or not attached to a scene, an error message is printed.
     *
     * @param sourceNode the {@code Node} that triggered the popup; must be a node
     *                   contained within a valid scene for the popup to close properly
     */
    public static void closePopup(Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("❌ Cannot close popup: source node is not attached to a scene.");
        }
    }

    public static void switchToWithoutEvent(Stage stage, String fxmlPath, String title) {
        try {
            // Load the FXML file
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent newRoot = loader.load();

            // Set new root and ensure stage is fully initialized
            if (stage.getScene() == null) {
                Scene newScene = new Scene(newRoot);
                stage.setScene(newScene);
            } else {
                stage.getScene().setRoot(newRoot);
            }

            // Set the title and show the stage
            stage.setTitle(title);
            stage.show();

            // Enforce fullscreen after rendering cycle
            Platform.runLater(() -> {
                if (!stage.isFullScreen()) {
                    stage.setFullScreen(true);
                    System.out.println("✅ Fullscreen enforced in SceneSwitcher.");
                }
            });

        } catch (IOException e) {
            System.err.println("❌ Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }


}