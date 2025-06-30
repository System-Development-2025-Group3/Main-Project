package application.studyspace.controllers.landingpage;

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

public class DashboardController {

    @FXML private Label progressTitle1, progressTitle2, progressTitle3, progressTitle4;
    @FXML private Circle progressRing1, progressRing2, progressRing3, progressRing4;
    @FXML private Label progressLabel1, progressLabel2, progressLabel3, progressLabel4;

    // --- For streak/today ---
    @FXML private Label timeStudiedLabel;
    @FXML private Label studyStreakLabel;
    @FXML private ImageView fireImage;
    @FXML private VBox pastSessionsBox;

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
        loadExamProgress();
        loadNextExam();
        loadPastSessions();
        loadStudyStats();
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

        // Add handlers for the buttons here
        doneButton.setOnAction(e -> markSessionCompleted(event));
        skipButton.setOnAction(e -> rescheduleSession(event));

        row.getChildren().addAll(label, spacer, doneButton, skipButton);
        return row;
    }

    private void loadPastSessions() {
        try {
            UUID userId = application.studyspace.services.auth.SessionManager.getInstance().getLoggedInUserId();

            // Get the blocker calendar (if any)
            var calendars = application.studyspace.services.calendar.CalendarRepository.findByUser(userId);
            UUID blockerCalendarId = calendars.stream()
                    .filter(c -> c.getName().toLowerCase().contains("blocker"))
                    .map(c -> c.getId())
                    .findFirst()
                    .orElse(null);

            // Load uncompleted past sessions
            List<CalendarEvent> pastEvents =
                    application.studyspace.services.calendar.CalendarEventRepository.findUncompletedPastStudySessionsByUser(userId);
            pastSessionsBox.getChildren().removeIf(node -> node instanceof HBox);

            pastEvents.stream()
                    // Exclude any that belong to the blocker calendar
                    .filter(evt -> blockerCalendarId == null || !evt.getCalendarId().equals(blockerCalendarId))
                    // Sort most recent first
                    .sorted(Comparator.comparing(CalendarEvent::getStart).reversed())
                    // Limit to 6
                    .limit(6)
                    .forEach(evt -> {
                        HBox row = createSessionRow(evt);
                        pastSessionsBox.getChildren().add(row);
                    });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

    private void rescheduleSession(CalendarEvent event) {
        try {
            StudyPlanGenerator.rescheduleOneSession(event);
            loadPastSessions();
            loadStudyStats();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadStudyStats() {
        try {
            UUID userId = SessionManager.getInstance().getLoggedInUserId();

            // Load only completed events from DB
            List<CalendarEvent> completedEvents = CalendarEventRepository.findCompletedEventsByUser(userId);

            LocalDate today = LocalDate.now();

            // Calculate total minutes studied today
            long totalMinutesToday = completedEvents.stream()
                    .filter(e -> e.getStart().toLocalDate().equals(today))
                    .mapToLong(e -> java.time.Duration.between(e.getStart(), e.getEnd()).toMinutes())
                    .sum();

            double hours = totalMinutesToday / 60.0;
            String formattedHours = String.format("%.1f", hours);

            // Calculate streak based on completed events
            int streak = calculateStreak(completedEvents);

            // Update UI
            setStudyInfo(formattedHours + "h", streak);

            System.out.println("[DEBUG] Total hours studied today: " + formattedHours + ", streak: " + streak);

        } catch (Exception ex) {
            ex.printStackTrace();
            setStudyInfo("0h", 0);
        }
    }


    private int calculateStreak(List<CalendarEvent> completedEvents) {
        // Map of LocalDate -> count of completed sessions
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

    private void loadExamProgress() {
        try {
            UUID userId = SessionManager.getInstance().getLoggedInUserId();

            // Fetch all exams for user, limit to 4
            List<ExamEvent> exams = ExamEventRepository.findByUser(userId).stream()
                    .limit(4)
                    .collect(Collectors.toList());

            // Debug: print loaded exams
            System.out.println("[DEBUG] Loaded exams:");
            for (ExamEvent exam : exams) {
                System.out.println("  Exam: " + exam.getTitle() + " (" + exam.getId() + ")");
            }

            // Clear and hide all first
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

            for (int i = 0; i < exams.size(); i++) {
                ExamEvent exam = exams.get(i);
                UUID examId = exam.getId();

                // Fetch all sessions for this exam
                List<CalendarEvent> allSessions = CalendarEventRepository.findByUserAndExam(userId, examId);
                // Fetch only completed sessions for this exam and user from DB, same as study streak logic
                List<CalendarEvent> completedSessions = CalendarEventRepository.findCompletedEventsByUserAndExam(userId, examId);


                // Subtract 2 from total sessions count to exclude the exam event itself
                int adjustedTotalSessions = Math.max(allSessions.size() - 2, 1);  // avoid division by zero
                int percent = (int) ((completedSessions.size() * 100) / adjustedTotalSessions);

                //DEBUG
                System.out.println("[DEBUG] Exam '" + exam.getTitle() + "' has " + allSessions.size() + " sessions.");
                System.out.println("[DEBUG] Completed sessions from DB: " + completedSessions.size());
                System.out.println("[DEBUG] Adjusted total sessions (minus 2): " + adjustedTotalSessions);
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

    private void updateProgress(Circle ring, Label percentLabel, Label titleLabel, String title, int percent) {
        ring.setVisible(true);
        percentLabel.setVisible(true);
        titleLabel.setVisible(true);
        setProgress(ring, percentLabel, titleLabel, title, percent);
    }


}
