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

/**
 * The LandingpageController class manages the UI and interactions for a calendar-based
 * application. It controls the display of a calendar in different views (Month, Week, and Day)
 * and allows navigation between dates.
 *
 * This class implements the Initializable interface and uses JavaFX annotations
 * (e.g., @FXML) for binding UI components and event handlers.
 */
public class LandingpageController implements Initializable {

    @FXML
    private StackPane calendarContainer;

    @FXML
    private Label monthYearLabel;

    private LocalDate currentDate = LocalDate.now();

    private enum ViewMode {
        MONTH, WEEK, DAY
    }

    private ViewMode currentView = ViewMode.MONTH;

    private final List<CalendarEvent> events = CalendarEvent.getAllEvents();

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets the default calendar view to "Month View" and dynamically attaches the
     * required stylesheets to the scene.
     *
     * @param url The location used to resolve relative paths for the root object, or null if unknown.
     * @param resourceBundle The resources used to localize the root object, or null if not available.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showMonthView(); // Default view

        // Attach stylesheets safely via Java
        Platform.runLater(() -> {
            Scene scene = calendarContainer.getScene();
            if (scene != null) {
                scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/application/studyspace/styles/LandingPageStylesheet.css")).toExternalForm()
                );
                scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/application/studyspace/styles/calendar.css")).toExternalForm()
                );
            }
        });
    }

    /**
     * Updates the current calendar view to the "Month" view mode.
     * This method sets the `currentView` field to `ViewMode.MONTH` and refreshes
     * the calendar interface to display the month-based layout with relevant events.
     * The method utilizes the `updateCalendarView` helper to redraw the calendar.
     */
    @FXML
    public void showMonthView() {
        currentView = ViewMode.MONTH;
        updateCalendarView();
    }

    /**
     * Updates the current calendar view to the "Week" view mode.
     * This method sets the `currentView` field to `ViewMode.WEEK` and refreshes
     * the calendar interface to display the week-based layout with relevant events.
     * It utilizes the `updateCalendarView` helper method to redraw the calendar.
     */
    @FXML
    public void showWeekView() {
        currentView = ViewMode.WEEK;
        updateCalendarView();
    }

    /**
     * Updates the current calendar view to the "Day" view mode.
     * This method sets the `currentView` field to `ViewMode.DAY` and refreshes
     * the calendar interface to display a day-based layout with relevant events.
     * It utilizes the `updateCalendarView` helper method to redraw the calendar.
     */
    @FXML
    public void showDayView() {
        currentView = ViewMode.DAY;
        updateCalendarView();
    }

    /**
     * Advances the calendar to the next time unit based on the current view mode.
     *
     * This method increments the `currentDate` field by a month, week, or day
     * depending on the value of the `currentView` field:
     * - If the current view is set to "Month", the date is incremented by one month.
     * - If the current view is set to "Week", the date is incremented by one week.
     * - If the current view is set to "Day", the date is incremented by one day.
     *
     * After updating the `currentDate`, the method invokes the `updateCalendarView`
     * helper to refresh the calendar interface and reflect the new date.
     */
    @FXML
    public void goToNext() {
        switch (currentView) {
            case MONTH -> currentDate = currentDate.plusMonths(1);
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case DAY -> currentDate = currentDate.plusDays(1);
        }
        updateCalendarView();
    }

    /**
     * Moves the calendar to the previous time unit based on the current view mode.
     *
     * This method decrements the `currentDate` field by a month, week, or day
     * depending on the value of the `currentView` field:
     * - If the current view is set to "Month", the date is decremented by one month.
     * - If the current view is set to "Week", the date is decremented by one week.
     * - If the current view is set to "Day", the date is decremented by one day.
     *
     * After updating the `currentDate`, the method invokes the `updateCalendarView`
     * helper to refresh the calendar interface and reflect the updated date.
     */
    @FXML
    public void goToPrevious() {
        switch (currentView) {
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case DAY -> currentDate = currentDate.minusDays(1);
        }
        updateCalendarView();
    }

    /**
     * Updates the UI to display the correct calendar view based on the current view mode.
     *
     * Depending on the value of the `currentView` field, this method constructs and
     * replaces the displayed calendar view with the appropriate layout:
     * - "Month View" displays a monthly calendar layout.
     * - "Week View" displays a weekly calendar layout.
     * - "Day View" displays a single-day calendar layout.
     *
     * The method clears all child nodes of the `calendarContainer` and adds the new
     * calendar view node to the container. It then calls the `updateHeaderLabel`
     * method to refresh the header label in the UI.
     */
    private void updateCalendarView() {
        Node view;
        switch (currentView) {
            case WEEK -> view = CalendarView.buildWeekView(currentDate, events);
            case DAY -> view = CalendarView.buildDayView(currentDate, events);
            default -> view = CalendarView.buildMonthView(currentDate, events);
        }

        calendarContainer.getChildren().clear();
        calendarContainer.getChildren().add(view);
        updateHeaderLabel();
    }

    /**
     * Updates the header label text based on the current view mode and date.
     *
     * This method determines the appropriate text to display in the header label
     * (`monthYearLabel`) by formatting the `currentDate` field differently depending
     * on the value of the `currentView` field:
     *
     * - For the "Month" view mode, the label displays the full name of the month and the year.
     * - For the "Week" view mode, the label displays the date range of the week, with the
     *   start and end dates formatted as "Month Day".
     * - For the "Day" view mode, the label displays the name of the day of the week, the
     *   abbreviated month name, and the day of the month.
     *
     * The method accounts for localization by using the English locale.
     */
    private void updateHeaderLabel() {
        String labelText;
        Locale locale = Locale.ENGLISH;

        switch (currentView) {
            case MONTH -> {
                String month = currentDate.getMonth().getDisplayName(TextStyle.FULL, locale);
                labelText = month + " " + currentDate.getYear();
            }
            case WEEK -> {
                LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = currentDate.with(DayOfWeek.SUNDAY);
                String start = startOfWeek.getMonth().getDisplayName(TextStyle.SHORT, locale) + " " + startOfWeek.getDayOfMonth();
                String end = endOfWeek.getMonth().getDisplayName(TextStyle.SHORT, locale) + " " + endOfWeek.getDayOfMonth();
                labelText = start + " – " + end;
            }
            case DAY -> {
                String weekday = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
                String month = currentDate.getMonth().getDisplayName(TextStyle.SHORT, locale);
                labelText = weekday + ", " + month + " " + currentDate.getDayOfMonth();
            }
            default -> labelText = "";
        }

        monthYearLabel.setText(labelText);
    }

    /**
     * Handles the close action initiated by the user.
     *
     * This method closes the application window that triggered the event. If the window
     * is standalone, it will only close that specific stage. For fully terminating the
     * application, uncomment the `Platform.exit()` line to exit the JavaFX runtime.
     *
     * @param event the {@code ActionEvent} that triggered the close action
     */
    @FXML
    private void handleClose(ActionEvent event) {
        //standalone window:
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();

        // if you want to terminate the whole JVM:
        // Platform.exit();
    }

    @FXML
    private void handleCreateNewEvent() {
        // Get the current stage (owner stage)
        Stage ownerStage = (Stage) calendarContainer.getScene().getWindow();

        // Load CreateNewEvent.fxml as a popup
        SceneSwitcher.switchToPopupWithData(
                ownerStage,
                "/application/studyspace/calendar/CreateNewEvent.fxml",
                "Create New Event",
                controller -> {
                }
        );
    }
}
