package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.UUID;

public class SettingsController {

    private final UUID userId = SessionManager.getInstance().getLoggedInUserId();

    @FXML
    private void handleSidebarCalendar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Landing-Page.fxml", "Calendar");
    }

    @FXML
    private void handleSidebarDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleSidebarSettings(ActionEvent event) {
        // Already on settings; no action needed
    }
}
