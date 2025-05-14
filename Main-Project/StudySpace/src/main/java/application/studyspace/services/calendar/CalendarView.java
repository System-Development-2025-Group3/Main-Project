package application.studyspace.services.calendar;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;

public class CalendarView {

    public static Node buildMonthView(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.getStyleClass().add("calendar-grid");
        grid.setMaxWidth(1000); // prevent grid from shrinking too much
        grid.setPrefWidth(Region.USE_COMPUTED_SIZE);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            grid.add(dayLabel, i, 0);
        }

        int row = 1;
        int col = firstDayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            VBox box = createDayBox(String.valueOf(day));
            GridPane.setHgrow(box, Priority.ALWAYS);
            GridPane.setVgrow(box, Priority.ALWAYS);
            grid.add(box, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        return grid;
    }



    public static Node buildWeekView(LocalDate referenceDate) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setGridLinesVisible(false); // use CSS for borders
        grid.setHgap(8);
        grid.setVgap(1);
        grid.setPrefWidth(1000);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int col = 0; col < days.length; col++) {
            Label dayLabel = new Label(days[col]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            grid.add(dayLabel, col + 1, 0); // shift by 1 to leave space for hours
        }

        // Add hour labels and cells
        for (int hour = 0; hour < 24; hour++) {
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.getStyleClass().add("calendar-hour-label");
            hourLabel.setMinWidth(50);
            hourLabel.setAlignment(Pos.CENTER_RIGHT);
            grid.add(hourLabel, 0, hour + 1); // column 0, shift row by 1 for header

            for (int col = 0; col < 7; col++) {
                VBox cell = new VBox();
                cell.getStyleClass().add("calendar-hour-cell");
                cell.setPrefHeight(40);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                grid.add(cell, col + 1, hour + 1); // skip hour column
            }
        }

        return grid;
    }

    public static Node buildDayView(LocalDate date) {
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
            vbox.getChildren().add(row);
        }
        return vbox;
    }

    private static VBox createDayBox(String dayNumber) {
        VBox box = new VBox();
        box.getStyleClass().add("calendar-day-box");
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(120, 100); // 🔁 Updated size
        box.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label dayNum = new Label(dayNumber);
        dayNum.getStyleClass().add("calendar-day-number");
        box.getChildren().add(dayNum);

        return box;
    }
}
