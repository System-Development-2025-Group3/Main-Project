package application.studyspace.controllers.scenes;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
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
        boolean isLoggedIn = SessionManager.getInstance().isLoggedIn();
        System.out.println("[RootController] isLoggedIn = " + isLoggedIn);

        if (isLoggedIn) {
            System.out.println("[RootController] User is logged in, loading Landing Page");
            viewManager.loadPage("/application/studyspace/landingpage/Landing-Page.fxml");
        } else {
            System.out.println("[RootController] User is NOT logged in, loading Login page");
            viewManager.loadPage("/application/studyspace/auth/Login.fxml");
        }
    }

    public ViewManager getViewManager() { return viewManager; }
    public StackPane getOverlayRoot() { return overlayRoot; }
}