package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventMapper;
import application.studyspace.services.calendar.CalendarEventRepository;
import application.studyspace.services.auth.SessionManager;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class LandingpageController implements Initializable {

    @FXML private CalendarView calendarView;
    @FXML private ToggleButton showDayButton, showWeekButton, showMonthButton;
    @FXML private BorderPane rootPane;

    private Calendar userCalendar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Create and style your CalendarFX calendar
        userCalendar = new Calendar("My Events");
        userCalendar.setStyle(Calendar.Style.STYLE6);

        CalendarSource source = new CalendarSource("Planify");
        source.getCalendars().add(userCalendar);
        calendarView.getCalendarSources().add(source);

        // 4) Show the week view by default
        calendarView.showWeekPage();

        // 5) Populate existing events from the DB
        loadEvents();

        // 6) Kick off the onboarding overlay after the scene is ready
        Platform.runLater(() -> {
            ViewManager.showOverlay(
                    "/application/studyspace/onboarding/OnboardingPage1.fxml",
                    controller -> {}
            );
        });
    }

    private void loadEvents() {
        CalendarEventRepository repo = new CalendarEventRepository();
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        try {
            List<CalendarEvent> events = repo.findByUser(userId);
            for (CalendarEvent e : events) {
                Entry<CalendarEvent> entry = CalendarEventMapper.toEntry(e, userCalendar);
                userCalendar.addEntry(entry);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // TODO: show an error alert to the user
        }
    }

    @FXML private void showDayView(ActionEvent ev) {
        calendarView.showDayPage();
    }

    @FXML private void showWeekView(ActionEvent ev) {
        calendarView.showWeekPage();
    }

    @FXML private void showMonthView(ActionEvent ev) {
        calendarView.showMonthPage();
    }

    @FXML private void handleCreateNewEvent(ActionEvent ev) {
        // TODO: open your "New Event" dialog
    }

    @FXML private void handleSidebarCalendar(ActionEvent event) {
        // Already on calendar
    }

    @FXML private void handleSidebarDashboard(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML private void handleSidebarSettings(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }
}
