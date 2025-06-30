package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class DashboardController {

    @FXML private Label progressTitle1, progressTitle2, progressTitle3, progressTitle4;
    @FXML private Circle progressRing1, progressRing2, progressRing3, progressRing4;
    @FXML private Label progressLabel1, progressLabel2, progressLabel3, progressLabel4;

    // --- New for streak/today ---
    @FXML private Label timeStudiedLabel;
    @FXML private Label studyStreakLabel;
    @FXML private ImageView fireImage;

    @FXML
    private void handleSidebarCalendar() {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleSidebarDashboard() {
        // Already on Dashboard, no action needed
    }

    @FXML
    private void handleSidebarSettings() {
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void initialize() {
        setProgress(progressRing1, progressLabel1, progressTitle1, "Mathe", 50);
        setProgress(progressRing2, progressLabel2, progressTitle2, "Economics", 60);
        setProgress(progressRing3, progressLabel3, progressTitle3, "English", 75);
        setProgress(progressRing4, progressLabel4, progressTitle4, "Statistics", 95);

        // Set default values for time/streak, e.g. from DB
        setStudyInfo("3h", 3);
    }

    private void setProgress(Circle ring, Label percentLabel, Label titleLabel, String title, double percent) {
        double radius = ring.getRadius();
        double circ = 2 * Math.PI * radius;

        ring.getStrokeDashArray().setAll(circ, circ);
        ring.setStrokeDashOffset(circ * (1 - percent / 100.0));
        ring.setRotate(-90);

        percentLabel.setText(String.format("%.0f%%", percent));
        titleLabel.setText(title);
    }

    // --- Update study time & streak programmatically ---
    public void setStudyInfo(String studiedTime, int streak) {
        timeStudiedLabel.setText(studiedTime);
        studyStreakLabel.setText(String.valueOf(streak));
        fireImage.setVisible(streak > 0);
    }
}
