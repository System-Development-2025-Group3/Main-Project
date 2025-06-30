package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.calendar.ExamEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DashboardController {

    @FXML private Label progressTitle1, progressTitle2, progressTitle3, progressTitle4;
    @FXML private Circle progressRing1, progressRing2, progressRing3, progressRing4;
    @FXML private Label progressLabel1, progressLabel2, progressLabel3, progressLabel4;

    // --- For streak/today ---
    @FXML private Label timeStudiedLabel;
    @FXML private Label studyStreakLabel;
    @FXML private ImageView fireImage;

    // --- For next exam timer ---
    @FXML private Label nextExamSubjectLabel;
    @FXML private Label nextExamDaysLabel;
    @FXML private Label nextExamHoursLabel;
    @FXML private Label nextExamMinutesLabel;

    // Timeline for countdown
    private Timeline countdownTimeline;

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

        // Example default values for study info
        setStudyInfo("3h", 3);

        loadNextExam();
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

    // Update study time & streak programmatically
    public void setStudyInfo(String studiedTime, int streak) {
        timeStudiedLabel.setText(studiedTime);
        studyStreakLabel.setText(String.valueOf(streak));
        fireImage.setVisible(streak > 0);
    }


    /**
     * Loads the next upcoming exam for the current user and updates the UI countdown.
     * <p>
     * This method fetches all {@code ExamEvent}s for the logged-in user,
     * determines which one is scheduled to occur soonest in the future,
     * and then calls {@link #setNextExam(String, java.time.LocalDateTime)}
     * to display the live countdown and exam subject on the dashboard.
     * <p>
     * If no upcoming exam is found, the UI will indicate that there are no exams.
     * If an error occurs during loading, it sets fallback values and prints the stack trace.
     */
    private void loadNextExam() {
        try {
            UUID userId = application.studyspace.services.auth.SessionManager.getInstance().getLoggedInUserId();
            // Get all exams for user
            List<ExamEvent> exams = application.studyspace.services.calendar.ExamEventRepository.findByUser(userId);
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now();

            // Find the soonest exam in the future
            ExamEvent nextExam = exams.stream()
                    .filter(e -> e.getStart().isAfter(now))
                    .min(Comparator.comparing(application.studyspace.services.calendar.ExamEvent::getStart))
                    .orElse(null);

            if (nextExam != null) {
                setNextExam(nextExam.getTitle(), nextExam.getStart().toLocalDateTime());
            } else {
                setNextExam("No upcoming exams", java.time.LocalDateTime.now());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setNextExam("No data", java.time.LocalDateTime.now());
        }
    }

    /**
     * Set next exam subject and date, and start ticking down.
     * @param subject The exam subject (e.g., "Mathe")
     * @param examDateTime The LocalDateTime when the exam is scheduled
     */
    public void setNextExam(String subject, java.time.LocalDateTime examDateTime) {
        nextExamSubjectLabel.setText(subject);

        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> updateCountdown(examDateTime)),
                new KeyFrame(Duration.seconds(1))
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdown(java.time.LocalDateTime examDateTime) {
        java.time.Duration duration = java.time.Duration.between(java.time.LocalDateTime.now(), examDateTime);
        long totalSeconds = duration.getSeconds();

        if (totalSeconds <= 0) {
            nextExamDaysLabel.setText("0D");
            nextExamHoursLabel.setText("0H");
            nextExamMinutesLabel.setText("0M");
            if (countdownTimeline != null) countdownTimeline.stop();
            return;
        }

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        nextExamDaysLabel.setText(days + "D");
        nextExamHoursLabel.setText(hours + "H");
        nextExamMinutesLabel.setText(minutes + "M");
    }
}
