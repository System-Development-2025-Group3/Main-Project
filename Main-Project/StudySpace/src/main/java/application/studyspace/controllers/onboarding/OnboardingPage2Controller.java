package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventMapper;
import application.studyspace.services.calendar.CalendarEventRepository;
import application.studyspace.services.onboarding.CalendarImportHelper;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.MonthView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class OnboardingPage2Controller implements Initializable {

    @FXML private StackPane calendarContainer;

    private UUID userUUID;
    private LocalDate currentDate;
    private Calendar userCalendar;
    private CalendarView calendarView;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
        if (calendarView != null) {
            refreshCalendar();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDate = LocalDate.now();

        calendarView = new CalendarView();
        calendarView.setDate(LocalDate.now());
        calendarView.showWeekPage();

// Hide clutter
        calendarView.setShowToolBar(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowToday(false);

// Set calendar source
        userCalendar = new Calendar("Imported");
        userCalendar.setStyle(Calendar.Style.STYLE1.name());

        CalendarSource source = new CalendarSource("Planify Onboard");
        source.getCalendars().add(userCalendar);
        calendarView.getCalendarSources().add(source);

        calendarContainer.getChildren().setAll(calendarView);

        // Enable drag and drop
        calendarContainer.setOnDragOver(this::onDragOver);
        calendarContainer.setOnDragDropped(this::onDragDropped);

        refreshCalendar();
    }

    private void refreshCalendar() {
        if (userUUID == null || userCalendar == null) return;

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
    }

    @FXML
    private void handleUploadCSV(ActionEvent ev) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select ICS or CSV File");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("iCalendar Files (*.ics)", "*.ics")
        );

        Window window = calendarContainer.getScene() != null
                ? calendarContainer.getScene().getWindow()
                : null;

        if (window != null) {
            File selectedFile = chooser.showOpenDialog(window);
            if (selectedFile != null && selectedFile.exists()) {
                CalendarImportHelper importer = new CalendarImportHelper(userUUID);
                boolean success = importer.importFromFile(selectedFile.getAbsolutePath());

                if (success) {
                    System.out.println("✅ File imported successfully: " + selectedFile.getName());
                    refreshCalendar();
                } else {
                    System.err.println("❌ Failed to import file: " + selectedFile.getName());
                }
            } else {
                System.out.println("⚠️ File selection was cancelled or file does not exist.");
            }
        } else {
            System.err.println("❌ No valid window available to open FileChooser.");
        }
    }

    private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        var files = e.getDragboard().getFiles();
        if (!files.isEmpty()) {
            boolean ok = new CalendarImportHelper(userUUID).importFromFile(files.get(0).getAbsolutePath());
            if (ok) {
                refreshCalendar();
            }
        }
        e.setDropCompleted(true);
        e.consume();
    }

    @FXML
    private void handlePage1(ActionEvent event) {
        Stage popup = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.<OnboardingPage1Controller>switchPopupContent(
                popup,
                "/application/studyspace/onboarding/OnboardingPage1.fxml",
                "Onboarding Page 1",
                ctrl -> ctrl.setUserUUID(userUUID)
        );
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        // No-op
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        Stage popup = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.<OnboardingPage3Controller>switchPopupContent(
                popup,
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                "Onboarding Page 3",
                ctrl -> ctrl.setUserUUID(userUUID)
        );
    }
}
