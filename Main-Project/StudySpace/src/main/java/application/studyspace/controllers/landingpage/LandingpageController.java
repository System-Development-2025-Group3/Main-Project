package application.studyspace.controllers.landingpage;

import application.studyspace.controllers.calendar.CreateNewEventController;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.calendar.CalendarEvent;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class LandingpageController implements Initializable {

    // ─── Singleton boilerplate ───────────────────────────────────────────────
    private static LandingpageController instance;
    public LandingpageController() {
        instance = this;
    }
    public static LandingpageController getInstance() {
        return instance;
    }
    // ────────────────────────────────────────────────────────────────────────

    @FXML private CalendarView calendarView;

    private Calendar fxCalendar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // ─── 0) Hide all of CalendarFX’s built-in chrome ────────────────────────
        calendarView.setShowToolBar(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowPageToolBarControls(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowDeveloperConsole(false);
        calendarView.setShowSourceTray(false);

        // ─── 1) Create & style your Calendar, attach it ─────────────────────────
        fxCalendar = new Calendar("Planify Events");
        fxCalendar.setStyle(Calendar.Style.STYLE1);
        CalendarSource source = new CalendarSource("StudySpace");
        source.getCalendars().add(fxCalendar);
        calendarView.getCalendarSources().add(source);

        // ─── 2) Load your app’s events into it ─────────────────────────────────
        loadAppEvents();

        // ─── 3) Clicking on an empty slot → your own “New Event” popup ─────────
        calendarView.setEntryFactory(param -> {
            Stage owner = (Stage) calendarView.getScene().getWindow();
            SceneSwitcher.switchToPopupWithData(
                    owner,
                    "/application/studyspace/calendar/CreateNewEvent.fxml",
                    "Create New Event",
                    ctrl -> ((CreateNewEventController)ctrl)
                            .setPreselectedDate(param.getZonedDateTime().toLocalDate())
            );
            return null;
        });

        // ─── 4) Clicking on an event → minimal pop-over ─────────────────────────
        calendarView.setEntryDetailsPopOverContentCallback(param ->
                new Label(param.getEntry().getTitle())
        );

        // ─── 5) Show the Month view by default ─────────────────────────────────
        calendarView.showMonthPage();
    }

    private void loadAppEvents() {
        for (CalendarEvent ev : CalendarEvent.getAllEvents()) {
            Entry<CalendarEvent> entry = new Entry<>(ev.getTitle());
            entry.setUserObject(ev);
            entry.changeStartDate(ev.getStart().toLocalDate());
            entry.changeStartTime(ev.getStart().toLocalTime());
            entry.changeEndDate(ev.getEnd().toLocalDate());
            entry.changeEndTime(ev.getEnd().toLocalTime());
            fxCalendar.addEntry(entry);
        }
    }

    // ─── Bound to your Day/Week/Month toggle buttons ─────────────────────────

    @FXML public void showDayView()   { calendarView.showDayPage(); }
    @FXML public void showWeekView()  { calendarView.showWeekPage(); }
    @FXML public void showMonthView() { calendarView.showMonthPage(); }

    // ─── Bound to the “+ New Event” button ──────────────────────────────────

    @FXML public void handleCreateNewEvent(ActionEvent evt) {
        Stage owner = (Stage) calendarView.getScene().getWindow();
        SceneSwitcher.switchToPopupWithData(
                owner,
                "/application/studyspace/calendar/CreateNewEvent.fxml",
                "Create New Event",
                ctrl -> ((CreateNewEventController)ctrl)
                        .setPreselectedDate(LocalDate.now())
        );
    }
}
