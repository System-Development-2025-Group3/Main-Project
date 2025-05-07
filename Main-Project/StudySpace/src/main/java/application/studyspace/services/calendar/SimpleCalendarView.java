package application.studyspace.services.calendar;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;

public class SimpleCalendarView {

    public static GridPane build(LocalDate date) {
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
            VBox box = new VBox();
            box.setAlignment(Pos.TOP_LEFT);
            box.setPrefSize(80, 60);
            box.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            box.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffffff"), null, null)));

            Label dayNum = new Label(String.valueOf(day));
            dayNum.setFont(Font.font(13));
            box.getChildren().add(dayNum);

            grid.add(box, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        return grid;
    }
}
