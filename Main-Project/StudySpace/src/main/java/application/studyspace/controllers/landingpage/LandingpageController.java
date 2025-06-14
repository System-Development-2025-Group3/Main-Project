package application.studyspace.controllers.landingpage;

import application.studyspace.controllers.onboarding.OnboardingPage1Controller;
import application.studyspace.services.Scenes.SceneSwitcher;
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
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class LandingpageController implements Initializable {

    @FXML
    private CalendarView calendarView;

    @FXML
    private ToggleButton showDayButton, showWeekButton, showMonthButton;

    @FXML private BorderPane rootPane;

    // This is the CalendarFX Calendar that will hold your user's events
    private Calendar userCalendar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Create your CalendarFX Calendar and style it
        userCalendar = new Calendar("My Events");
        userCalendar.setStyle(Calendar.Style.STYLE1.name());

        // 2) Put it into a CalendarSource and add to the view
        CalendarSource source = new CalendarSource("Planify");
        source.getCalendars().add(userCalendar);
        calendarView.getCalendarSources().add(source);

        // 3) Show the Month page by default
        calendarView.showMonthPage();

        // 4) Load events from DB
        loadEvents();

        //temporary loading of the onboarding process not just the first time but always
        final UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        Platform.runLater(() -> {
            Stage popup = new Stage();
            popup.initOwner(rootPane.getScene().getWindow());
            popup.initModality(Modality.APPLICATION_MODAL);
            SceneSwitcher.<OnboardingPage1Controller>switchToPopupWithData(
                    popup,
                    "/application/studyspace/onboarding/OnboardingPage1.fxml",
                    "Onboarding — Step 1",
                    controller -> controller.setUserUUID(userUUID)
            );
            popup.show();
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

    // FXML action handlers for your toggle buttons:

    @FXML
    private void showDayView(ActionEvent ev) {
        calendarView.showDayPage();
    }

    @FXML
    private void showWeekView(ActionEvent ev) {
        calendarView.showWeekPage();
    }

    @FXML
    private void showMonthView(ActionEvent ev) {
        calendarView.showMonthPage();
    }

    @FXML
    private void handleCreateNewEvent(ActionEvent ev) {
        // TODO: open your "New Event" dialog
    }
    @FXML
    private void handleSidebarCalendar(ActionEvent event) {
        // Already on calendar; no action needed
    }

    @FXML
    private void handleSidebarDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleSidebarSettings(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Settings.fxml", "Settings");
    }
}
