package application.studyspace.services.Scenes;

import application.studyspace.controllers.scenes.RootController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Central manager for handling scene and overlay loading in the StudySpace app.
 *
 * - Loads and swaps main content views.
 * - Manages overlays (popups) on top of the current content.
 * - Provides static convenience methods so any part of the app can call `ViewManager.show(...)`.
 */
public class ViewManager {

    /** Singleton instance for global access. */
    private static final ViewManager INSTANCE = new ViewManager();

    /** The main layout container (the BorderPane in RootLayout.fxml). */
    private BorderPane layoutRoot;

    /** The overlay layer where popups stack (a StackPane in RootLayout.fxml). */
    private StackPane overlayRoot;

    /** Private constructor to enforce singleton. */
    private ViewManager() {}

    /** Static accessor for singleton instance. */
    public static ViewManager getInstance() {
        return INSTANCE;
    }

    /**
     * Must be called ONCE after RootLayout.fxml is loaded, to wire up the main containers.
     */
    public void initialize(BorderPane layoutRoot, StackPane overlayRoot) {
        this.layoutRoot = layoutRoot;
        this.overlayRoot = overlayRoot;
    }

    /**
     * Convenience static method to load a page into the center of the main layout.
     */
    public static void show(String fxmlPath) {
        getInstance().loadPage(fxmlPath);
    }

    /**
     * Loads an FXML file and sets it into the center of the BorderPane.
     */
    public void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            layoutRoot.setCenter(page);
        } catch (IOException e) {
            System.err.println("❌ Failed to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Loads the RootLayout.fxml and displays the main window.
     * This is called once in your main application start method.
     */
    public static void loadRootLayout(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(
                    "/application/studyspace/scenes/RootLayout.fxml"
            ));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.setTitle("StudySpace");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a popup overlay (like a modal dialog) from FXML.
     *
     * @param fxmlPath path to the FXML resource
     * @param controllerInit a lambda to configure the controller after loading
     */
    public static <T> void showOverlay(String fxmlPath, Consumer<T> controllerInit) {
        ViewManager vm = getInstance();
        try {
            FXMLLoader loader = new FXMLLoader(vm.getClass().getResource(fxmlPath));
            Parent popupContent = loader.load();

            // Initialize the controller (e.g., inject dependencies)
            T controller = loader.getController();
            if (controllerInit != null) {
                controllerInit.accept(controller);
            }

            // Force CSS and layout pass to get correct size
            popupContent.applyCss();
            popupContent.layout();

            // Measure preferred size (not strictly necessary but sometimes useful)
            double contentW = popupContent.prefWidth(-1);
            double contentH = popupContent.prefHeight(-1);

            // Dim the background behind the popup
            Region dim = new Region();
            dim.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
            dim.prefWidthProperty().bind(vm.overlayRoot.widthProperty());
            dim.prefHeightProperty().bind(vm.overlayRoot.heightProperty());

            // The wrapper VBox holds the popup content itself
            VBox popupWrapper = new VBox(popupContent);
            popupWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            popupWrapper.setEffect(new DropShadow());
            StackPane.setAlignment(popupWrapper, Pos.CENTER);

            // StackPane containing both the dim background and the popup card
            StackPane wrapper = new StackPane(dim, popupWrapper);
            wrapper.setPickOnBounds(true);

            // Add this overlay to the overlayRoot StackPane
            vm.overlayRoot.getChildren().add(wrapper);

        } catch (IOException e) {
            System.err.println("❌ Failed to load overlay: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Closes the last overlay that was added to the overlay root.
     */
    public static void closeTopOverlay() {
        ViewManager vm = getInstance();
        int size = vm.overlayRoot.getChildren().size();
        if (size > 0) {
            vm.overlayRoot.getChildren().remove(size - 1);
        }
    }
}
