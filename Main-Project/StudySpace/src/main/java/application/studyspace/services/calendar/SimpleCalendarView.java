package application.studyspace.services.calendar;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;

public class SimpleCalendarView {

    public static Node buildMonthView(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_CENTER);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            grid.add(dayLabel, i, 0);
        }

        int row = 1;
        int col = firstDayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            VBox box = createDayBox(String.valueOf(day));
            grid.add(box, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    public static Node buildWeekView(LocalDate startOfWeek) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setHgap(5);
        grid.setVgap(5);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int col = 0; col < days.length; col++) {
            Label dayLabel = new Label(days[col]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            dayLabel.setAlignment(Pos.CENTER);
            grid.add(dayLabel, col + 1, 0);
        }

        for (int row = 0; row < 24; row++) {
            Label hourLabel = new Label(String.format("%02d:00", row));
            hourLabel.setFont(Font.font(11));
            grid.add(hourLabel, 0, row + 1);

            for (int col = 0; col < 7; col++) {
                VBox cell = new VBox();
                cell.setPrefSize(100, 40);
                cell.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffffff"), null, null)));
                grid.add(cell, col + 1, row + 1);
            }
        }

        return grid;
    }

    public static Node buildDayView(LocalDate date) {
        VBox vbox = new VBox(5);
        for (int hour = 0; hour < 24; hour++) {
            HBox row = new HBox();
            row.setPrefHeight(40);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: lightgray;");
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setFont(Font.font(12));
            timeLabel.setPrefWidth(60);
            row.getChildren().add(timeLabel);
            vbox.getChildren().add(row);
        }
        return vbox;
    }

    private static VBox createDayBox(String dayNumber) {
        VBox box = new VBox();
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(80, 60);
        box.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        box.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffffff"), null, null)));

        Label dayNum = new Label(dayNumber);
        dayNum.setFont(Font.font(13));
        box.getChildren().add(dayNum);

        return box;
    }
}
