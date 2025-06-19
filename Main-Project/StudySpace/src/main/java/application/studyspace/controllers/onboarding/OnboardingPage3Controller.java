// OnboardingPage3Controller.java
package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventMapper;
import application.studyspace.services.calendar.CalendarEventRepository;
import application.studyspace.services.calendar.ExamEvent;
import application.studyspace.services.calendar.ExamEventRepository;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.Entry;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OnboardingPage3Controller implements Initializable {

    private static final Logger logger = Logger.getLogger(OnboardingPage3Controller.class.getName());

    @FXML private StackPane calendarPreviewContainer;
    @FXML private Label     previewDateLabel;
    @FXML private Button page1Btn, page2Btn, page3Btn, SaveBtn;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle, blockerToggle;
    @FXML private VBox examForm, blockerForm;
    @FXML private TextField  examNameField;
    @FXML private TextArea   descriptionArea;
    @FXML private DatePicker exStartDate, exEndDate;
    @FXML private TextField  estimatedMinutesField;
    @FXML private ToggleButton colorRed, colorBlue, colorGreen, colorYellow, colorPurple;
    @FXML private TextField          evtTitleField, evtLocationField;
    @FXML private DatePicker         evtStartDate, evtEndDate;
    @FXML private Spinner<LocalTime> evtStartTime, evtEndTime;
    @FXML private CheckBox           evtAllDay;

    private CalendarView calendarView;
    private LocalDate    previewDate;
    private Calendar     defaultCalendar;

    private final CalendarEventMapper     mapper  = new CalendarEventMapper();
    private final CalendarEventRepository calRepo = new CalendarEventRepository();
    private final ExamEventRepository     exRepo  = ExamEventRepository.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPreview();
        setupTypeToggle();
        setupTimeSpinners();
    }

    private void setupPreview() {
        previewDate  = LocalDate.now();
        calendarView = new CalendarView();

        // only show the day page, no toolbar or extra controls
        calendarView.showDayPage();
        calendarView.setShowToolBar(false);          // hide entire top bar
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowDeveloperConsole(false);

        calendarView.setDate(previewDate);

        defaultCalendar = new Calendar("Blockers");
        defaultCalendar.setStyle(Style.STYLE1);
        calendarView.getCalendarSources().get(0).getCalendars().add(defaultCalendar);

        calendarPreviewContainer.getChildren().setAll(calendarView);
    }

    private void updateLabel() {
        previewDateLabel.setText(
                previewDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        + " " + previewDate.getDayOfMonth()
                        + ", " + previewDate.getYear()
        );
    }

    @FXML private void goToNextPreview() {
        previewDate = previewDate.plusDays(1);
        calendarView.setDate(previewDate);
        updateLabel();
    }

    @FXML private void goToPreviousPreview() {
        previewDate = previewDate.minusDays(1);
        calendarView.setDate(previewDate);
        updateLabel();
    }

    private void setupTypeToggle() {
        typeToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean isExam = newT == examToggle;
            examForm.setVisible(isExam);
            examForm.setManaged(isExam);
            blockerForm.setVisible(!isExam);
            blockerForm.setManaged(!isExam);
        });
        examToggle.setSelected(true);
    }

    private void setupTimeSpinners() {
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        for (int h = 0; h < 24; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        SpinnerValueFactory<LocalTime> sf1 = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        SpinnerValueFactory<LocalTime> sf2 = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        StringConverter<LocalTime> conv = new StringConverter<>() {
            @Override public String toString(LocalTime t) { return t == null ? "" : t.format(fmt); }
            @Override public LocalTime fromString(String s) { return LocalTime.parse(s, fmt); }
        };
        sf1.setConverter(conv);
        sf2.setConverter(conv);

        evtStartTime.setValueFactory(sf1);
        evtEndTime.setValueFactory(sf2);
        evtStartTime.setEditable(true);
        evtEndTime.setEditable(true);
    }

    @FXML private void handlePage1(ActionEvent e) {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", c -> {});
    }

    @FXML private void handlePage2(ActionEvent e) {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage2.fxml", c -> {});
    }

    @FXML private void handlePage3() {}

    @FXML
    private void handleSave(ActionEvent e) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        try {
            if (examToggle.isSelected()) {
                // Only save if both exam dates and estimated minutes are provided
                if (exStartDate.getValue() != null
                    && exEndDate.getValue()   != null
                    && !estimatedMinutesField.getText().isBlank()) {

                    saveExam(userId);
                } else {
                    System.out.println("ℹ️ No exam details entered; skipping saveExam().");
                }

            } else {
                // Only save a blocker if start date is provided and,
                // either it's all-day or an end date is provided
                if (evtStartDate.getValue() != null
                    && (evtAllDay.isSelected() || evtEndDate.getValue() != null)) {

                    saveBlocker(userId);
                } else {
                    System.out.println("ℹ️ No exam details entered; skipping saveExam().");
                }
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error in handleSave", ex);
        }

        ViewManager.closeTopOverlay();
    }

    private void saveExam(UUID userId) {
        ZonedDateTime start = exStartDate.getValue().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime end   = exEndDate  .getValue().atStartOfDay(ZoneId.systemDefault());
        int minutes = Integer.parseInt(estimatedMinutesField.getText());

        ExamEvent exam = new ExamEvent(
                userId,
                examNameField.getText(),
                "",
                descriptionArea.getText(),
                "",
                start, end,
                0.0, 0, 1, minutes
        );
        exRepo.save(exam);
        logger.info("Created ExamEvent: " + exam.getId());

        Calendar examCal = new Calendar(exam.getTitle());
        examCal.setStyle(pickStyle());
        Entry<ExamEvent> fx = mapper.toEntry(exam, examCal);
        examCal.addEntry(fx);
        calendarView.getCalendarSources().get(0).getCalendars().add(examCal);
    }

    private void saveBlocker(UUID userId) {
        try {
            LocalDate sd = evtStartDate.getValue();
            LocalTime st = evtStartTime.getValue();
            ZonedDateTime start = ZonedDateTime.of(sd, st, ZoneId.systemDefault());
            ZonedDateTime end = evtAllDay.isSelected()
                    ? start.plusDays(1)
                    : ZonedDateTime.of(evtEndDate.getValue(), evtEndTime.getValue(), ZoneId.systemDefault());

            CalendarEvent evt = new CalendarEvent(
                    userId,
                    evtTitleField.getText(),
                    "",
                    evtLocationField.getText(),
                    start, end
            );
            calRepo.save(evt);
            logger.info("Created CalendarEvent: " + evt.getId());

            Entry<CalendarEvent> fx = mapper.toEntry(evt, defaultCalendar);
            defaultCalendar.addEntry(fx);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to save blocker", ex);
        }
    }

    private Style pickStyle() {
        if (colorRed   .isSelected()) return Style.STYLE1;
        if (colorBlue  .isSelected()) return Style.STYLE2;
        if (colorGreen .isSelected()) return Style.STYLE3;
        if (colorYellow.isSelected()) return Style.STYLE4;
        if (colorPurple.isSelected()) return Style.STYLE5;
        return Style.STYLE1;
    }
}
