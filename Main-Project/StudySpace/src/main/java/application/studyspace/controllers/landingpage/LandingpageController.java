package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.calendar.CalendarHelper;
import com.calendarfx.model.Calendar;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LandingpageController implements Initializable {

    @FXML private CalendarView calendarView;

    private Calendar userCalendar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Delegate all CalendarFX setup and preference application
        userCalendar = CalendarHelper.setupUserCalendar(calendarView);

        calendarView.getStylesheets().add(
                getClass()
                        .getResource("/application/studyspace/styles/calendar/calendar.css")
                        .toExternalForm()
        );
    }

    @FXML private void handleSidebarCalendar(ActionEvent event) {
        // already here
    }

    @FXML private void handleSidebarDashboard(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML private void handleSidebarSettings(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }
}
