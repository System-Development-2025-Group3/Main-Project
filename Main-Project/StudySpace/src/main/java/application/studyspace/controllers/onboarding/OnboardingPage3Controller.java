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
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OnboardingPage3Controller implements Initializable {

    private static final Logger logger = Logger.getLogger(OnboardingPage3Controller.class.getName());

    @FXML private StackPane calendarPreviewContainer;
    @FXML private VBox examForm, blockerForm;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle, blockerToggle;
    @FXML private TextField examNameField, topicsField, difficultyField, estimatedMinutesField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker exStartDate, evtStartDate, evtEndDate;
    @FXML private Spinner<LocalTime> exStartTime, exEndTime, evtStartTime, evtEndTime;
    @FXML private CheckBox evtAllDay;
    @FXML private Slider weightSlider;
    @FXML private Label  weightLabel;
    @FXML private Button addExamBtn, SaveBtn;
    @FXML private TextField evtTitleField, evtLocationField;

    @FXML public Button page1Btn, page2Btn, page3Btn;

    private CalendarView calendarView;
    private Calendar     defaultCalendar;

    private final CalendarEventMapper     mapper   = new CalendarEventMapper();
    private final CalendarEventRepository calRepo  = new CalendarEventRepository();
    private final ExamEventRepository     exRepo   = new ExamEventRepository();
    private final CalendarRepository      calDef   = new CalendarRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPreview();

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

        weightSlider.valueProperty().addListener((o, old, nu) ->
                weightLabel.setText(String.format("%.0f%%", nu.doubleValue())));
    }

    private void setupPreview() {
        calendarView = new CalendarView();
        calendarView.setShowToolBar(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowDeveloperConsole(false);

        CalendarHelper.setupWeekCalendar(calendarView);

        calendarPreviewContainer.getChildren().setAll(calendarView);
    }

    private void setupTimeSpinners(Spinner<LocalTime> start, Spinner<LocalTime> end) {
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        // From 01:00 to 24:00 (use 00:00 next day for 24:00, or display "24:00")
        for (int h = 1; h <= 23; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        // Optionally add 24:00 as LocalTime.MIDNIGHT (next day)
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
            // If end < start, set end = start
            if (endTime != null && newStart != null && endTime.isBefore(newStart)) {
                end.getValueFactory().setValue(newStart);
            }
        });

        end.valueProperty().addListener((obs, oldEnd, newEnd) -> {
            LocalTime startTime = start.getValue();
            // If end < start, set end = start
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
            logger.info("✅ Exam added successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Failed to add exam", ex);
        }
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
        CalendarHelper.updateUserCalendar(SessionManager.getInstance().getUserCalendar());
    }

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
        defaultCalendar.addEntry(mapper.toEntry(ev, defaultCalendar));
        logger.info("✅ Blocker registered: " + ev.getTitle());
    }

    private void saveExam(UUID userId) throws SQLException {
        // validation (update the ValidationUtils method to match these params)
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
        int difficulty = Integer.parseInt(difficultyField.getText());
        int minutes    = Integer.parseInt(estimatedMinutesField.getText());
        double weight  = weightSlider.getValue();

        ExamEvent exam = new ExamEvent(
                userId,
                examNameField.getText(),
                descriptionArea.getText(),
                "",
                start, end,
                weight, difficulty,
                topics, minutes
        );

        // save to its own calendar
        UUID calId = calDef.createCalendar(userId, exam.getTitle(), "STYLE2");
        exam.setCalendarId(calId);

        exRepo.save(exam);
        logger.info("✅ ExamEvent saved: " + exam.getId());

        // render it
        Calendar fxCal = new Calendar(exam.getTitle());
        fxCal.setStyle(Style.STYLE2);
        fxCal.addEntry(mapper.toEntry(exam, fxCal));
        calendarView.getCalendarSources().get(0).getCalendars().add(fxCal);
    }

    private void resetExamForm() {
        examNameField.clear();
        descriptionArea.clear();
        exStartDate.setValue(null);
        exStartTime.getValueFactory().setValue(LocalTime.of(8,0));
        exEndTime  .getValueFactory().setValue(LocalTime.of(10,0));
        topicsField.clear();
        difficultyField.clear();
        estimatedMinutesField.clear();
        weightSlider.setValue(50);
    }
}
