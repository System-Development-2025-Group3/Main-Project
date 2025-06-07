package application.studyspace.services.calendar;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * The CalendarView class provides methods for constructing calendar views including
 * month, week, and day layouts. These views are rendered using JavaFX components and
 * are structured to display dates, times, and events in an organized grid-based format.
 *
 * It supports embedding date-based events with customization options, such as colors and
 * titles, to represent various calendar events visually. The views make use of specific
 * JavaFX style classes to allow further customization via stylesheets.
 */
public class CalendarView {

    /**
     * Builds a monthly calendar view based on the provided date and associated events.
     * The calendar includes headers for days of the week and individual cells for each day
     * of the month. Each day cell can display events that occur on that specific date.
     *
     * @param date   the date from which the desired month is derived
     * @param events a list of calendar events, each containing details such as title,
     *               start time, end time, and associated color
     * @return a Node representing the structured calendar grid for the specified month
     */
    public static Node buildMonthView(LocalDate date, List<CalendarEvent> events) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday

        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        // Give each of the 7 columns a 120px pref width & center their content
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(120);
            cc.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        // Day‐of‐week headers
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int col = 0; col < days.length; col++) {
            Label lbl = new Label(days[col]);
            lbl.getStyleClass().add("calendar-day-header");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, col, 0);
        }

        // Fill in day‐cells
        int row = 1, col = firstDayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = yearMonth.atDay(day);
            VBox box = createDayBox(String.valueOf(day));

            // Add any events
            for (CalendarEvent ev : events) {
                if (ev.getStart().toLocalDate().equals(currentDay)) {
                    Label eventLabel = new Label(ev.getTitle());
                    eventLabel.getStyleClass().add("calendar-event");
                    eventLabel.setStyle("-fx-background-color: " + ev.getColor() + ";");
                    box.getChildren().add(eventLabel);
                }
            }

            grid.add(box, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    /**
     * Builds a weekly calendar view based on the given reference date and list of events.
     * The calendar displays a grid with days of the week as columns and hourly time slots
     * as rows. Events that match a specific day and time are displayed within the corresponding
     * cell of the grid.
     *
     * @param referenceDate the date from which the week starts; the grid aligns days beginning
     *                      from the Monday of the week containing this date
     * @param events        a list of calendar events, each containing details such as title,
     *                      start time, end time, and associated color; events are matched to
     *                      time slots based on their start times
     * @return a Node representing the weekly calendar grid with structured day and hour cells,
     *         displaying events in the appropriate slots
     */
    public static Node buildWeekView(LocalDate referenceDate, List<CalendarEvent> events) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setHgap(8);
        grid.setVgap(1);
        grid.setPrefWidth(1000);

        // Day headers
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        LocalDate monday = referenceDate.with(DayOfWeek.MONDAY);
        for (int c = 0; c < days.length; c++) {
            Label lbl = new Label(days[c]);
            lbl.getStyleClass().add("calendar-day-header");
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(lbl, Priority.ALWAYS);
            grid.add(lbl, c + 1, 0);
        }

        // Hour rows
        for (int hour = 0; hour < 24; hour++) {
            Label hourLbl = new Label(String.format("%02d:00", hour));
            hourLbl.getStyleClass().add("calendar-hour-label");
            hourLbl.setMinWidth(50);
            hourLbl.setAlignment(Pos.CENTER_RIGHT);
            grid.add(hourLbl, 0, hour + 1);

            for (int c = 0; c < 7; c++) {
                VBox cell = new VBox();
                cell.getStyleClass().add("calendar-hour-cell");
                cell.setPrefHeight(40);
                cell.setAlignment(Pos.TOP_LEFT);

                LocalDateTime slotStart = monday.plusDays(c).atTime(hour, 0);
                for (CalendarEvent ev : events) {
                    if (ev.getStart().toLocalDate().equals(slotStart.toLocalDate())
                            && ev.getStart().getHour() == hour) {
                        Label eventLabel = new Label(ev.getTitle());
                        eventLabel.getStyleClass().add("calendar-event");
                        eventLabel.setStyle("-fx-background-color: " + ev.getColor() + ";");
                        cell.getChildren().add(eventLabel);
                    }
                }

                GridPane.setHgrow(cell, Priority.ALWAYS);
                grid.add(cell, c + 1, hour + 1);
            }
        }

        return grid;
    }

    /**
     * Builds a view representing a single day in the calendar, showing hourly time slots
     * and any events that occur on the specified date.
     *
     * Each hour of the day is displayed as a row, with events represented as labeled elements
     * appearing in the corresponding hourly row.
     *
     * @param date   the date for which the day view is to be constructed
     * @param events a list of calendar events, each containing details such as title,
     *               start time, and associated color
     * @return a Node containing the day's view, structured with an hourly breakdown
     *         and populated with events for the provided date
     */
    public static Node buildDayView(LocalDate date, List<CalendarEvent> events) {
        VBox vbox = new VBox(5);

        for (int hour = 0; hour < 24; hour++) {
            HBox row = new HBox();
            row.getStyleClass().add("calendar-hour-cell");
            row.setPrefHeight(40);
            row.setAlignment(Pos.CENTER_LEFT);

            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.getStyleClass().add("calendar-hour-label");
            timeLabel.setPrefWidth(60);
            row.getChildren().add(timeLabel);

            for (CalendarEvent ev : events) {
                if (ev.getStart().toLocalDate().equals(date)
                        && ev.getStart().getHour() == hour) {
                    Label eventLabel = new Label(ev.getTitle());
                    eventLabel.getStyleClass().add("calendar-event");
                    eventLabel.setStyle("-fx-background-color: " + ev.getColor() + ";");
                    row.getChildren().add(eventLabel);
                }
            }

            vbox.getChildren().add(row);
        }

        return vbox;
    }

    /**
     * Creates a VBox representing a single day in the calendar. The VBox includes
     * a label to display the day's number and is styled and sized for a calendar view.
     *
     * @param dayNumber the number of the day to be displayed in the VBox
     * @return a VBox styled as a day box, containing the day's number
     */
    private static VBox createDayBox(String dayNumber) {
        VBox box = new VBox();
        box.getStyleClass().add("calendar-day-box");
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(120, 100);

        Label dayNum = new Label(dayNumber);
        dayNum.getStyleClass().add("calendar-day-number");
        box.getChildren().add(dayNum);

        return box;
    }
}
