package application.studyspace.controllers.scenes;

import application.studyspace.services.Scenes.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class RootController {

    @FXML private StackPane overlayRoot;
    @FXML private BorderPane rootLayout;

    private ViewManager viewManager;

    @FXML
    public void initialize() {
        viewManager = ViewManager.getInstance();
        viewManager.initialize(rootLayout, overlayRoot);
        viewManager.loadPage("/application/studyspace/auth/Login.fxml");
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public StackPane getOverlayRoot() {
        return overlayRoot;
    }
}
