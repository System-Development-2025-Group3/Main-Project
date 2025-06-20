package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventMapper;
import application.studyspace.services.calendar.CalendarEventRepository;
import application.studyspace.services.calendar.CalendarHelper;
import application.studyspace.services.onboarding.CalendarImportHelper;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class OnboardingPage2Controller implements Initializable {

    @FXML private StackPane calendarContainer;

    private LocalDate     currentDate;
    private Calendar      userCalendar;
    private CalendarView  calendarView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDate = LocalDate.now();
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();

        // --- set up CalendarView preview ---
        calendarView = new CalendarView();
        calendarView.setDate(currentDate);
        calendarView.showWeekPage();

        calendarView.setShowToolBar(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowToday(false);

        userCalendar = new Calendar("Imported");
        userCalendar.setStyle(Calendar.Style.STYLE1);

        CalendarSource source = new CalendarSource("Planify Onboard");
        source.getCalendars().add(userCalendar);
        calendarView.getCalendarSources().add(source);

        calendarContainer.getChildren().setAll(calendarView);

        // **NEW** apply visible‐hours & blocked‐days prefs
        CalendarHelper.applyStudyPreferences(calendarView);

        // drag‐and‐drop handlers
        calendarContainer.setOnDragOver(this::onDragOver);
        calendarContainer.setOnDragDropped(this::onDragDropped);

        // initial load
        refreshCalendar(userUUID);
    }

    private void refreshCalendar(UUID userUUID) {
        if (userUUID == null || userCalendar == null) return;

        // clear out old entries
        userCalendar.clear();

        try {
            List<CalendarEvent> events = new CalendarEventRepository().findByUser(userUUID);
            for (CalendarEvent e : events) {
                Entry<CalendarEvent> entry = CalendarEventMapper.toEntry(e, userCalendar);
                userCalendar.addEntry(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // **RE‐APPLY** prefs in case user changed them during import
        CalendarHelper.applyStudyPreferences(calendarView);
    }

    @FXML
    private void handleUploadCSV(ActionEvent ev) {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select ICS or CSV File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("iCalendar Files (*.ics)", "*.ics")
        );

        Window window = calendarContainer.getScene() != null
                ? calendarContainer.getScene().getWindow()
                : null;

        if (window != null) {
            File selectedFile = chooser.showOpenDialog(window);
            if (selectedFile != null && selectedFile.exists()) {
                boolean success = new CalendarImportHelper(userUUID)
                        .importFromFile(selectedFile.getAbsolutePath());

                if (success) {
                    System.out.println("✅ File imported: " + selectedFile.getName());
                    refreshCalendar(userUUID);
                } else {
                    System.err.println("❌ Import failed: " + selectedFile.getName());
                }
            } else {
                System.out.println("⚠️ No file selected or file not found.");
            }
        } else {
            System.err.println("❌ Cannot open FileChooser—no window available.");
        }
    }

    private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        var files = e.getDragboard().getFiles();
        if (!files.isEmpty()) {
            boolean ok = new CalendarImportHelper(userUUID)
                    .importFromFile(files.get(0).getAbsolutePath());
            if (ok) {
                refreshCalendar(userUUID);
            }
        }
        e.setDropCompleted(true);
        e.consume();
    }

    @FXML private void handlePage1(ActionEvent event) {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage1.fxml",
                controller -> {});
    }

    @FXML private void handlePage2(ActionEvent event) {
        // no‐op, already here
    }

    @FXML private void handlePage3(ActionEvent event) {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                controller -> {});
    }
}
