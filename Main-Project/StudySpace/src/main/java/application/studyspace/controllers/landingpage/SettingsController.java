package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.util.UUID;

public class SettingsController {

    private final UUID userId = SessionManager.getInstance().getLoggedInUserId();

    @FXML
    private void handleSidebarCalendar(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleSidebarDashboard(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML
    private void handleSidebarSettings(ActionEvent event) {
        // Already on settings; no action needed
    }
}
