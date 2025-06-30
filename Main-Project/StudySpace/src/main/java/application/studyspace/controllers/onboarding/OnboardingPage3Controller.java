package application.studyspace.controllers.onboarding;

// Imports
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.ValidationUtils.ExamValidationResult;
import application.studyspace.services.calendar.*;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.view.CalendarView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Onboarding Page 3.
 * Lets the user create Exams and Blockers and preview them in a calendar.
 */
public class OnboardingPage3Controller implements Initializable {

    private static final Logger logger = Logger.getLogger(OnboardingPage3Controller.class.getName());

    // Keeps loaded calendars mapped by UUID
    private final Map<UUID, Calendar> calendarMap = new HashMap<>();

    // UI components
    @FXML private StackPane calendarPreviewContainer;
    @FXML private VBox examForm, blockerForm;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle, blockerToggle;

    // Exam input fields
    @FXML private TextField examNameField, topicsField, estimatedMinutesField;
    @FXML private DatePicker exStartDate;
    @FXML private Spinner<LocalTime> exStartTime, exEndTime;

    // Blocker input fields
    @FXML private DatePicker evtStartDate, evtEndDate;
    @FXML private Spinner<LocalTime> evtStartTime, evtEndTime;
    @FXML private CheckBox evtAllDay;
    @FXML private TextField evtTitleField, evtLocationField;

    @FXML private Button addExamBtn, SaveBtn;

    // Onboarding page navigation buttons
    @FXML public Button page1Btn, page2Btn, page3Btn;

    // The calendar preview component
    private CalendarView calendarView;

    // Services for DB operations
    private final CalendarEventMapper     mapper   = new CalendarEventMapper();
    private final CalendarEventRepository calRepo  = new CalendarEventRepository();
    private final ExamEventRepository     exRepo   = new ExamEventRepository();
    private final CalendarRepository      calDef   = new CalendarRepository();

    /**
     * Called automatically after FXML loads.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup calendar preview
        setupPreview();

        // Initially hide close overlay button
        closeOverlayButton.setDisable(true);
        closeOverlayButton.setVisible(false);

        // Toggle between exam/blocker forms
        typeToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isExam = newT == examToggle;
            examForm.setVisible(isExam);
            examForm.setManaged(isExam);
            blockerForm.setVisible(!isExam);
            blockerForm.setManaged(!isExam);
        });
        examToggle.setSelected(true);

        // Time spinners for both exam and blocker
        setupTimeSpinners(evtStartTime, evtEndTime);
        setupTimeSpinners(exStartTime,  exEndTime);

        // Button handlers
        addExamBtn.setOnAction(this::handleAddExam);
        SaveBtn.setOnAction(this::generateStudyPlan);
    }

    /**
     * Stores calendars from async loader.
     */
    public void setCalendarMap(Map<UUID, Calendar> loadedMap) {
        this.calendarMap.clear();
        if (loadedMap != null) {
            this.calendarMap.putAll(loadedMap);
        }
    }

    /**
     * Creates and configures the preview CalendarView.
     */
    private void setupPreview() {
        calendarView = new CalendarView();
        calendarView.setShowToolBar(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowDeveloperConsole(false);

        // Load week view asynchronously
        CalendarHelper.setupWeekCalendarAsync(calendarView, this::setCalendarMap);

        calendarPreviewContainer.getChildren().setAll(calendarView);
    }

    /**
     * Prepares spinners for selecting times.
     */
    private void setupTimeSpinners(Spinner<LocalTime> start, Spinner<LocalTime> end) {
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        for (int h = 1; h <= 23; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        times.add(LocalTime.MIDNIGHT);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        StringConverter<LocalTime> conv = new StringConverter<>() {
            public String toString(LocalTime t) {
                if (t == null) return "";
                if (t.equals(LocalTime.MIDNIGHT)) return "24:00";
                return t.format(fmt);
            }
            public LocalTime fromString(String s) {
                if (s.equals("24:00")) return LocalTime.MIDNIGHT;
                return LocalTime.parse(s, fmt);
            }
        };

        SpinnerValueFactory<LocalTime> sf1 = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        SpinnerValueFactory<LocalTime> sf2 = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        sf1.setConverter(conv);
        sf2.setConverter(conv);

        start.setValueFactory(sf1);
        end.setValueFactory(sf2);
        start.setEditable(true);
        end.setEditable(true);

        // Keep end >= start
        start.valueProperty().addListener((obs, oldStart, newStart) -> {
            LocalTime endTime = end.getValue();
            if (endTime != null && newStart != null && endTime.isBefore(newStart)) {
                end.getValueFactory().setValue(newStart);
            }
        });
        end.valueProperty().addListener((obs, oldEnd, newEnd) -> {
            LocalTime startTime = start.getValue();
            if (startTime != null && newEnd != null && newEnd.isBefore(startTime)) {
                end.getValueFactory().setValue(startTime);
            }
        });
    }

    // Navigation handlers for onboarding
    @FXML public void handlePage1() {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", c -> {});
    }
    @FXML public void handlePage2() {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage2.fxml", c -> {});
    }
    @FXML public void handlePage3() {
        // Already on Page 3, no-op
    }

    /**
     * Called when the user clicks "Add Exam".
     */
    @FXML public void handleAddExam(ActionEvent e) {
        try {
            saveExam(SessionManager.getInstance().getLoggedInUserId());
            resetExamForm();
            logger.info("✅ Exam added successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Failed to add exam", ex);
        }
    }

    /**
     * Called when the user clicks "Add Blocker".
     */
    @FXML public void handleAddBlocker(ActionEvent e) {
        try {
            saveBlocker(SessionManager.getInstance().getLoggedInUserId());
            resetExamForm();
            logger.info("✅ Blocker added successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Failed to add blocker", ex);
        }
    }

    @FXML public Button closeOverlayButton;

    /**
     * Closes the onboarding overlay.
     */
    @FXML public void handleCloseOverlay(ActionEvent event) {
        ViewManager.closeTopOverlay();
    }

    /**
     * Generates study plan for the current user.
     */
    @FXML public void generateStudyPlan(ActionEvent e) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        try {
            StudyPlanGenerator.generateStudyPlan(userId);
            System.out.println("Successfully generated study plan.");
        } catch (SQLException ex) {
            System.out.println("Failed to generate study plan: " + ex.getMessage());
            ex.printStackTrace();
        }
        ViewManager.closeTopOverlay();
        CalendarHelper.updateUserCalendarAsync(SessionManager.getInstance().getUserCalendar(), this::setCalendarMap);
    }

    /**
     * Saves a blocker event to DB and preview.
     */
    private void saveBlocker(UUID userId) throws SQLException {
        UUID calId = calDef.getOrCreateBlockersCalendar(userId);
        ZonedDateTime start = ZonedDateTime.of(evtStartDate.getValue(), evtStartTime.getValue(), ZoneId.systemDefault());
        ZonedDateTime end = evtAllDay.isSelected()
                ? start.plusDays(1)
                : ZonedDateTime.of(evtEndDate.getValue(), evtEndTime.getValue(), ZoneId.systemDefault());

        CalendarEvent ev = new CalendarEvent(
                userId,
                evtTitleField.getText(),
                "",
                evtLocationField.getText(),
                start, end
        );
        ev.setCalendarId(calId);
        calRepo.save(ev);

        Calendar fxCal = calendarMap.get(calId);
        if (fxCal != null) {
            fxCal.addEntry(mapper.toEntry(ev, fxCal));
        } else {
            System.out.println("[OnboardingPage3Controller] Could not find Calendar for ID: " + calId);
        }
        logger.info("✅ Blocker registered: " + ev.getTitle());
    }

    /**
     * Saves an exam event to DB and preview.
     */
    private void saveExam(UUID userId) throws SQLException {
        ExamValidationResult vr = ValidationUtils.validateExamFields(
                examNameField.getText(),
                exStartDate.getValue(), exStartTime.getValue(),
                exEndTime.getValue(),
                estimatedMinutesField.getText()
        );
        if (vr != ExamValidationResult.OK) {
            logger.warning("⚠️ Exam validation failed: " + vr);
            return;
        }

        ZonedDateTime start = ZonedDateTime.of(exStartDate.getValue(), exStartTime.getValue(), ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.of(exStartDate.getValue(), exEndTime.getValue(), ZoneId.systemDefault());

        int topics = Integer.parseInt(topicsField.getText());
        int minutes = Integer.parseInt(estimatedMinutesField.getText());
        double weight = 0; // Not used
        int difficulty = 1; // Not used

        ExamEvent exam = new ExamEvent(
                userId,
                examNameField.getText(),
                "",
                "",
                start, end,
                weight, difficulty,
                topics, minutes
        );

        UUID calId = calDef.createCalendar(userId, exam.getTitle(), "STYLE2");
        exam.setCalendarId(calId);
        exRepo.save(exam);
        logger.info("✅ ExamEvent saved: " + exam.getId());

        // Preview in the CalendarView
        Calendar fxCal = new Calendar(exam.getTitle());
        fxCal.setStyle(Style.STYLE2);
        fxCal.addEntry(mapper.toEntry(exam, fxCal));
        if (calendarView.getCalendarSources().isEmpty()) {
            com.calendarfx.model.CalendarSource src = new com.calendarfx.model.CalendarSource("Planify");
            src.getCalendars().add(fxCal);
            calendarView.getCalendarSources().add(src);
        } else {
            calendarView.getCalendarSources().get(0).getCalendars().add(fxCal);
        }
    }

    /**
     * Resets the exam form inputs to default.
     */
    private void resetExamForm() {
        examNameField.clear();
        exStartDate.setValue(null);
        exStartTime.getValueFactory().setValue(LocalTime.of(8, 0));
        exEndTime.getValueFactory().setValue(LocalTime.of(10, 0));
        topicsField.clear();
        estimatedMinutesField.clear();
    }
}
