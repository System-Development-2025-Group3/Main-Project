package application.studyspace.services.Scenes;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.function.Consumer;

public class ViewManager {

    private static final ViewManager INSTANCE = new ViewManager();

    private BorderPane layoutRoot;
    private StackPane overlayRoot;

    private ViewManager() {}

    public static ViewManager getInstance() {
        return INSTANCE;
    }

    public void initialize(BorderPane layoutRoot, StackPane overlayRoot) {
        this.layoutRoot = layoutRoot;
        this.overlayRoot = overlayRoot;
    }

    public static void show(String fxmlPath) {
        getInstance().loadPage(fxmlPath);
    }

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
     * Shows a popup overlay and keeps it grouped in one removable wrapper.
     */
    public static <T> void showOverlay(String fxmlPath, Consumer<T> controllerInit) {
        ViewManager vm = getInstance();

        try {
            FXMLLoader loader = new FXMLLoader(vm.getClass().getResource(fxmlPath));
            Parent popupContent = loader.load();

            // Initialize controller
            T controller = loader.getController();
            if (controllerInit != null) {
                controllerInit.accept(controller);
            }

            // Force CSS/layout pass so pref sizes are accurate
            popupContent.applyCss();
            popupContent.layout();

            // Measure content
            double contentW = popupContent.prefWidth(-1);
            double contentH = popupContent.prefHeight(-1);

            // Dim background
            Region dim = new Region();
            dim.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
            dim.prefWidthProperty().bind(vm.overlayRoot.widthProperty());
            dim.prefHeightProperty().bind(vm.overlayRoot.heightProperty());

            // Popup wrapper — make transparent so only the FXML’s white card shows
            VBox popupWrapper = new VBox(popupContent);
            popupWrapper.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            popupWrapper.setEffect(new DropShadow());
            StackPane.setAlignment(popupWrapper, Pos.CENTER);

            // Composite
            StackPane wrapper = new StackPane(dim, popupWrapper);
            wrapper.setPickOnBounds(true);

            vm.overlayRoot.getChildren().add(wrapper);

        } catch (IOException e) {
            System.err.println("❌ Failed to load overlay: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Closes the last added popup overlay (safely removes the last wrapper).
     */
    public static void closeTopOverlay() {
        ViewManager vm = getInstance();
        int size = vm.overlayRoot.getChildren().size();
        if (size > 0) {
            vm.overlayRoot.getChildren().remove(size - 1);
        }
    }
}
