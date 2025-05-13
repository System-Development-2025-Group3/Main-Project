package application.studyspace.controllers.landingpage;

import application.studyspace.services.calendar.SimpleCalendarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showMonthView(); // Default view
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
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case DAY -> currentDate = currentDate.plusDays(1);
        }
        updateCalendarView();
    }

    @FXML
    public void goToPrevious() {
        switch (currentView) {
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case DAY -> currentDate = currentDate.minusDays(1);
        }
        updateCalendarView();
    }

    private void updateCalendarView() {
        Node view;
        switch (currentView) {
            case WEEK -> view = SimpleCalendarView.buildWeekView(currentDate);
            case DAY -> view = SimpleCalendarView.buildDayView(currentDate);
            default -> view = SimpleCalendarView.buildMonthView(currentDate);
        }

        calendarContainer.getChildren().clear();
        calendarContainer.getChildren().add(view);

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


}
