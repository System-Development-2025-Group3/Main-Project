package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.ValidationUtils.ExamValidationResult;
import application.studyspace.services.calendar.*;
import application.studyspace.services.onboarding.StudyPreferences;
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

public class OnboardingPage3Controller implements Initializable {

    private static final Logger logger = Logger.getLogger(OnboardingPage3Controller.class.getName());
    private final Map<UUID, Calendar> calendarMap = new HashMap<>();

    @FXML private StackPane calendarPreviewContainer;
    @FXML private VBox examForm, blockerForm;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle, blockerToggle;
    @FXML private TextField examNameField, topicsField, estimatedMinutesField;
    @FXML private DatePicker exStartDate, evtStartDate, evtEndDate;
    @FXML private Spinner<LocalTime> exStartTime, exEndTime, evtStartTime, evtEndTime;
    @FXML private CheckBox evtAllDay;
    @FXML private Button addExamBtn, SaveBtn;
    @FXML private TextField evtTitleField, evtLocationField;

    @FXML public Button page1Btn, page2Btn, page3Btn;

    private CalendarView calendarView;

    private final CalendarEventMapper     mapper   = new CalendarEventMapper();
    private final CalendarEventRepository calRepo  = new CalendarEventRepository();
    private final ExamEventRepository     exRepo   = new ExamEventRepository();
    private final CalendarRepository      calDef   = new CalendarRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPreview();
        closeOverlayButton.setDisable(true);
        closeOverlayButton.setVisible(false);

        // toggle between exam/blocker
        typeToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isExam = newT == examToggle;
            examForm.setVisible(isExam);
            examForm.setManaged(isExam);
            blockerForm.setVisible(!isExam);
            blockerForm.setManaged(!isExam);
        });
        examToggle.setSelected(true);

        setupTimeSpinners(evtStartTime, evtEndTime);
        setupTimeSpinners(exStartTime,  exEndTime);

        addExamBtn.setOnAction(this::handleAddExam);
        SaveBtn   .setOnAction(this::generateStudyPlan);

    }

    /** Called by the calendar loader to provide a fresh map of all loaded calendars. */
    public void setCalendarMap(Map<UUID, Calendar> loadedMap) {
        this.calendarMap.clear();
        if (loadedMap != null) {
            this.calendarMap.putAll(loadedMap);
        }
    }

    private void setupPreview() {
        calendarView = new CalendarView();
        calendarView.setShowToolBar(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowDeveloperConsole(false);

        CalendarHelper.setupWeekCalendarAsync(calendarView, this::setCalendarMap);

        calendarPreviewContainer.getChildren().setAll(calendarView);
    }

    private void setupTimeSpinners(Spinner<LocalTime> start, Spinner<LocalTime> end) {
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        for (int h = 1; h <= 23; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        times.add(LocalTime.MIDNIGHT);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        StringConverter<LocalTime> conv = new StringConverter<>() {
            public String toString(LocalTime t)  {
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

        // Always keep end >= start
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

    @FXML public void handlePage1() {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", c->{});
    }
    @FXML public void handlePage2() {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage2.fxml", c->{});
    }
    @FXML public void handlePage3() { /* noop */ }

    @FXML public void handleAddExam(ActionEvent e) {
        try {
            saveExam(SessionManager.getInstance().getLoggedInUserId());
            resetExamForm();
            logger.info("✅ Exam added successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Failed to add exam", ex);
        }
    }

    @FXML public void handleAddBlocker(ActionEvent e) {
        try {
            saveBlocker(SessionManager.getInstance().getLoggedInUserId());
            resetExamForm();
            logger.info("✅ Blocker added successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Failed to add blocker", ex);
        }
    }

    @FXML
    public Button closeOverlayButton;
    @FXML
    public void handleCloseOverlay(ActionEvent event) {
        ViewManager.closeTopOverlay();
    }

    @FXML
    public void generateStudyPlan(ActionEvent e) {
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

    /** Adds a blocker to the correct calendar via the calendarMap (no more defaultCalendar!) */
    private void saveBlocker(UUID userId) throws SQLException {
        UUID calId = calDef.getOrCreateBlockersCalendar(userId);
        ZonedDateTime start = ZonedDateTime.of(evtStartDate.getValue(), evtStartTime.getValue(), ZoneId.systemDefault());
        ZonedDateTime end   = evtAllDay.isSelected()
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

    /** Adds an exam to its new calendar and also uses calendarMap to show in the preview. */
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
        ZonedDateTime end   = ZonedDateTime.of(exStartDate.getValue(), exEndTime.getValue(), ZoneId.systemDefault());

        int topics     = Integer.parseInt(topicsField.getText());
        int minutes    = Integer.parseInt(estimatedMinutesField.getText());
        double weight  = 0; // Default weight, not in use
        int difficulty = 1; // Default difficulty, not in use
        String description = "";

        ExamEvent exam = new ExamEvent(
                userId,
                examNameField.getText(),
                description, // not in use
                "",
                start, end,
                weight, difficulty, //not in use
                topics, minutes
        );

        // save to its own calendar
        UUID calId = calDef.createCalendar(userId, exam.getTitle(), "STYLE2");
        exam.setCalendarId(calId);

        exRepo.save(exam);
        logger.info("✅ ExamEvent saved: " + exam.getId());

        // If calendarMap is updated, it will include this new calendar AFTER next reload.
        // But for instant preview, you can add it as a temp calendar if you want:
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

    private void resetExamForm() {
        examNameField.clear();
        exStartDate.setValue(null);
        exStartTime.getValueFactory().setValue(LocalTime.of(8,0));
        exEndTime  .getValueFactory().setValue(LocalTime.of(10,0));
        topicsField.clear();
        estimatedMinutesField.clear();
    }
}
