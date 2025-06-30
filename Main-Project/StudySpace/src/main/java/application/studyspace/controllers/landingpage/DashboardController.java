package application.studyspace.controllers.landingpage;

// Import all dependencies
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller class for the Dashboard view.
 * Displays progress rings, countdown to next exam, study stats, and past sessions.
 */
public class DashboardController {

    // Progress rings and labels for exams
    @FXML private Label progressTitle1, progressTitle2, progressTitle3, progressTitle4;
    @FXML private Circle progressRing1, progressRing2, progressRing3, progressRing4;
    @FXML private Label progressLabel1, progressLabel2, progressLabel3, progressLabel4;

    // For study time and streak display
    @FXML private Label timeStudiedLabel;
    @FXML private Label studyStreakLabel;
    @FXML private ImageView fireImage;
    @FXML private VBox pastSessionsBox;

    // Labels for the next exam countdown
    @FXML private Label nextExamSubjectLabel;
    @FXML private Label nextExamDaysLabel;
    @FXML private Label nextExamHoursLabel;
    @FXML private Label nextExamMinutesLabel;

    // Timeline to update countdown every second
    private Timeline countdownTimeline;

    // Navigation handlers for sidebar buttons
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

    // Called automatically after FXML load
    @FXML
    private void initialize() {
        loadExamProgress();
        loadNextExam();
        loadPastSessions();
        loadStudyStats();
    }

    // Updates a circular progress ring and labels
    private void setProgress(Circle ring, Label percentLabel, Label titleLabel, String title, double percent) {
        double radius = ring.getRadius();
        double circ = 2 * Math.PI * radius;

        ring.getStrokeDashArray().setAll(circ, circ);
        ring.setStrokeDashOffset(circ * (1 - percent / 100.0));
        ring.setRotate(-90);

        percentLabel.setText(String.format("%.0f%%", percent));
        titleLabel.setText(title);
    }

    // Update study time and streak in UI
    public void setStudyInfo(String studiedTime, int streak) {
        timeStudiedLabel.setText(studiedTime);
        studyStreakLabel.setText(String.valueOf(streak));
        fireImage.setVisible(streak > 0);
    }

    /**
     * Loads the next upcoming exam and starts the countdown.
     */
    private void loadNextExam() {
        try {
            UUID userId = application.studyspace.services.auth.SessionManager.getInstance().getLoggedInUserId();
            List<ExamEvent> exams = application.studyspace.services.calendar.ExamEventRepository.findByUser(userId);
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now();

            // Find soonest upcoming exam
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
     * Sets the exam info and starts the countdown timer.
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

    // Updates countdown labels based on time remaining
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

    // Creates a row displaying a past uncompleted session
    private HBox createSessionRow(CalendarEvent event) {
        HBox row = new HBox(14);
        row.getStyleClass().add("session-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(event.getTitle());
        label.getStyleClass().add("session-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button doneButton = new Button("✔");
        doneButton.getStyleClass().addAll("dashboard-icon-btn", "dashboard-icon-btn-check");

        Button skipButton = new Button("−");
        skipButton.getStyleClass().addAll("dashboard-icon-btn", "dashboard-icon-btn-skip");

        // Action handlers
        doneButton.setOnAction(e -> markSessionCompleted(event));
        skipButton.setOnAction(e -> rescheduleSession(event));

        row.getChildren().addAll(label, spacer, doneButton, skipButton);
        return row;
    }

    /**
     * Loads and displays uncompleted past study sessions.
     */
    private void loadPastSessions() {
        try {
            UUID userId = application.studyspace.services.auth.SessionManager.getInstance().getLoggedInUserId();

            // Find blocker calendar if exists
            var calendars = application.studyspace.services.calendar.CalendarRepository.findByUser(userId);
            UUID blockerCalendarId = calendars.stream()
                    .filter(c -> c.getName().toLowerCase().contains("blocker"))
                    .map(c -> c.getId())
                    .findFirst()
                    .orElse(null);

            // Load past uncompleted events
            List<CalendarEvent> pastEvents =
                    application.studyspace.services.calendar.CalendarEventRepository.findUncompletedPastStudySessionsByUser(userId);

            // Clear previous rows
            pastSessionsBox.getChildren().removeIf(node -> node instanceof HBox);

            pastEvents.stream()
                    .filter(evt -> blockerCalendarId == null || !evt.getCalendarId().equals(blockerCalendarId))
                    .sorted(Comparator.comparing(CalendarEvent::getStart).reversed())
                    .limit(6)
                    .forEach(evt -> pastSessionsBox.getChildren().add(createSessionRow(evt)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Marks a session as completed and refreshes UI
    private void markSessionCompleted(CalendarEvent event) {
        try {
            event.setCompleted(true);
            CalendarEventRepository.save(event);
            loadPastSessions();
            loadStudyStats();
            loadExamProgress();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Reschedules a session and refreshes UI
    private void rescheduleSession(CalendarEvent event) {
        try {
            StudyPlanGenerator.rescheduleOneSession(event);
            loadPastSessions();
            loadStudyStats();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads study statistics (time studied and streak).
     */
    private void loadStudyStats() {
        try {
            UUID userId = SessionManager.getInstance().getLoggedInUserId();

            List<CalendarEvent> completedEvents = CalendarEventRepository.findCompletedEventsByUser(userId);
            LocalDate today = LocalDate.now();

            long totalMinutesToday = completedEvents.stream()
                    .filter(e -> e.getStart().toLocalDate().equals(today))
                    .mapToLong(e -> java.time.Duration.between(e.getStart(), e.getEnd()).toMinutes())
                    .sum();

            double hours = totalMinutesToday / 60.0;
            String formattedHours = String.format("%.1f", hours);

            int streak = calculateStreak(completedEvents);

            setStudyInfo(formattedHours + "h", streak);

            System.out.println("[DEBUG] Total hours studied today: " + formattedHours + ", streak: " + streak);

        } catch (Exception ex) {
            ex.printStackTrace();
            setStudyInfo("0h", 0);
        }
    }

    // Calculates the number of consecutive study days
    private int calculateStreak(List<CalendarEvent> completedEvents) {
        Map<LocalDate, Long> completedByDay = completedEvents.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getStart().toLocalDate(),
                        Collectors.counting()
                ));

        int streak = 0;
        LocalDate today = LocalDate.now();

        while (true) {
            if (completedByDay.getOrDefault(today, 0L) > 0) {
                streak++;
                today = today.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * Loads progress rings for up to 4 exams.
     */
    private void loadExamProgress() {
        try {
            UUID userId = SessionManager.getInstance().getLoggedInUserId();

            List<ExamEvent> exams = ExamEventRepository.findByUser(userId).stream()
                    .limit(4)
                    .collect(Collectors.toList());

            // Debug print
            System.out.println("[DEBUG] Loaded exams:");
            for (ExamEvent exam : exams) {
                System.out.println("  Exam: " + exam.getTitle() + " (" + exam.getId() + ")");
            }

            // Clear and hide all progress UI
            progressTitle1.setText("");
            progressTitle2.setText("");
            progressTitle3.setText("");
            progressTitle4.setText("");

            progressRing1.setVisible(false);
            progressLabel1.setVisible(false);
            progressTitle1.setVisible(false);

            progressRing2.setVisible(false);
            progressLabel2.setVisible(false);
            progressTitle2.setVisible(false);

            progressRing3.setVisible(false);
            progressLabel3.setVisible(false);
            progressTitle3.setVisible(false);

            progressRing4.setVisible(false);
            progressLabel4.setVisible(false);
            progressTitle4.setVisible(false);

            // Loop through exams
            for (int i = 0; i < exams.size(); i++) {
                ExamEvent exam = exams.get(i);
                UUID examId = exam.getId();

                List<CalendarEvent> allSessions = CalendarEventRepository.findByUserAndExam(userId, examId);
                List<CalendarEvent> completedSessions = CalendarEventRepository.findCompletedEventsByUserAndExam(userId, examId);

                int adjustedTotalSessions = Math.max(allSessions.size() - 2, 1);
                int percent = (int) ((completedSessions.size() * 100) / adjustedTotalSessions);

                // Debug logs
                System.out.println("[DEBUG] Exam '" + exam.getTitle() + "' has " + allSessions.size() + " sessions.");
                System.out.println("[DEBUG] Completed sessions: " + completedSessions.size() + " / " + adjustedTotalSessions + " (" + percent + "%)");

                switch (i) {
                    case 0 -> updateProgress(progressRing1, progressLabel1, progressTitle1, exam.getTitle(), percent);
                    case 1 -> updateProgress(progressRing2, progressLabel2, progressTitle2, exam.getTitle(), percent);
                    case 2 -> updateProgress(progressRing3, progressLabel3, progressTitle3, exam.getTitle(), percent);
                    case 3 -> updateProgress(progressRing4, progressLabel4, progressTitle4, exam.getTitle(), percent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Makes a progress ring visible and sets its progress
    private void updateProgress(Circle ring, Label percentLabel, Label titleLabel, String title, int percent) {
        ring.setVisible(true);
        percentLabel.setVisible(true);
        titleLabel.setVisible(true);
        setProgress(ring, percentLabel, titleLabel, title, percent);
    }
}
