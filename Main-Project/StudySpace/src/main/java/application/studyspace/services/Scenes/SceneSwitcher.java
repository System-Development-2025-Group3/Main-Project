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

    public static void switchTo(Object eventSource, String fxmlPath, String title) {
        Stage stage = (Stage) ((Node) eventSource).getScene().getWindow();
        switchTo(stage, fxmlPath, title);
    }

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

    public static void closePopup(Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("❌ Cannot close popup: source node is not attached to a scene.");
        }
    }
}
