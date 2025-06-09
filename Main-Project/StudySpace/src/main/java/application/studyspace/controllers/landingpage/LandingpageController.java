package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class LandingpageController implements Initializable {

    @FXML
    private StackPane calendarContainer;

    @FXML
    private Label monthYearLabel;

    private LocalDate currentDate = LocalDate.now();

    private enum ViewMode { MONTH, WEEK, DAY }
    private ViewMode currentView = ViewMode.MONTH;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Draw the default view
        showMonthView();

        // Attach stylesheets
        Platform.runLater(() -> {
            Scene scene = calendarContainer.getScene();
            if (scene != null) {
                scene.getStylesheets().add(
                        Objects.requireNonNull(
                                getClass().getResource("/application/studyspace/styles/LandingPageStylesheet.css")
                        ).toExternalForm()
                );
                scene.getStylesheets().add(
                        Objects.requireNonNull(
                                getClass().getResource("/application/studyspace/styles/calendar.css")
                        ).toExternalForm()
                );
            }
        });
    }

    @FXML
    public void showMonthView() {
        currentView = ViewMode.MONTH;
        updateCalendarView();
    }

    @FXML
    public void showWeekView() {
        currentView = ViewMode.WEEK;
        updateCalendarView();
    }

    @FXML
    public void showDayView() {
        currentView = ViewMode.DAY;
        updateCalendarView();
    }

    @FXML
    public void goToNext() {
        switch (currentView) {
            case MONTH -> currentDate = currentDate.plusMonths(1);
            case WEEK  -> currentDate = currentDate.plusWeeks(1);
            case DAY   -> currentDate = currentDate.plusDays(1);
        }
        updateCalendarView();
    }

    @FXML
    public void goToPrevious() {
        switch (currentView) {
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case WEEK  -> currentDate = currentDate.minusWeeks(1);
            case DAY   -> currentDate = currentDate.minusDays(1);
        }
        updateCalendarView();
    }

    /**
     * Re-fetches events from the database and redraws the correct calendar view.
     */
    private void updateCalendarView() {
        // ← fetch the latest events each time
        List<CalendarEvent> events = CalendarEvent.getAllEvents();

        Node view;
        switch (currentView) {
            case WEEK -> view = CalendarView.buildWeekView(currentDate, events);
            case DAY  -> view = CalendarView.buildDayView(currentDate, events);
            default   -> view = CalendarView.buildMonthView(currentDate, events);
        }

        // swap in the new view
        calendarContainer.getChildren().setAll(view);

        // update the "Month / Week / Day" header text
        updateHeaderLabel();
    }

    private void updateHeaderLabel() {
        String labelText;
        Locale locale = Locale.ENGLISH;
        switch (currentView) {
            case MONTH -> {
                String month = currentDate.getMonth().getDisplayName(TextStyle.FULL, locale);
                labelText = month + " " + currentDate.getYear();
            }
            case WEEK -> {
                LocalDate start = currentDate.with(DayOfWeek.MONDAY);
                LocalDate end   = currentDate.with(DayOfWeek.SUNDAY);
                String s = start.getMonth().getDisplayName(TextStyle.SHORT, locale) + " " + start.getDayOfMonth();
                String e = end  .getMonth().getDisplayName(TextStyle.SHORT, locale) + " " + end.getDayOfMonth();
                labelText = s + " – " + e;
            }
            case DAY -> {
                String weekday = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
                String monAbbr = currentDate.getMonth().getDisplayName(TextStyle.SHORT, locale);
                labelText = weekday + ", " + monAbbr + " " + currentDate.getDayOfMonth();
            }
            default -> labelText = "";
        }
        monthYearLabel.setText(labelText);
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
    }

    // (Optional, when you wire up automatic refresh after save)
    @FXML
    private void handleCreateNewEvent() {
        Stage ownerStage = (Stage) calendarContainer.getScene().getWindow();
        SceneSwitcher.switchToPopupWithData(
                ownerStage,
                "/application/studyspace/calendar/CreateNewEvent.fxml",
                "Create New Event",
                controller -> {
                    // later you can pass a callback here so that after saving,
                    // createNewEventController can invoke updateCalendarView().
                }
        );
    }
}
