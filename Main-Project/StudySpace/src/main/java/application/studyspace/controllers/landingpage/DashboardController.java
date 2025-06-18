package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.util.UUID;

public class DashboardController {

    private final UUID userId = SessionManager.getInstance().getLoggedInUserId();

    @FXML
    private void handleSidebarCalendar(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleSidebarDashboard(ActionEvent event) {
        // Already on dashboard; no action needed
    }

    @FXML
    private void handleSidebarSettings(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }
}
