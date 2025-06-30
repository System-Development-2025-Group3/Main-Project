package application.studyspace.controllers.scenes;

// Imports for scene management and session handling
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * The RootController is responsible for initializing the main application layout.
 * It decides whether to show the Login screen or the Landing page based on the login status.
 */
public class RootController {

    // The overlay container used for pop-up overlays
    @FXML private StackPane overlayRoot;

    // The main content area
    @FXML private BorderPane rootLayout;

    // Reference to the ViewManager singleton
    private ViewManager viewManager;

    /**
     * Called automatically after FXML is loaded.
     * Initializes the ViewManager and loads the appropriate first scene.
     */
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

    /**
     * Provides access to the ViewManager.
     */
    public ViewManager getViewManager() {
        return viewManager;
    }

    /**
     * Provides access to the overlay root pane.
     */
    public StackPane getOverlayRoot() {
        return overlayRoot;
    }
}
