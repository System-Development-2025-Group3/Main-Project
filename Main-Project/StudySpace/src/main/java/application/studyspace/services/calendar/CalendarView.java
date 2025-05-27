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

public class CalendarView {

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
