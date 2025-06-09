package application.studyspace.services.calendar;

import application.studyspace.controllers.calendar.CreateNewEventController;
import application.studyspace.controllers.calendar.EventDetailsController;
import application.studyspace.controllers.landingpage.LandingpageController;
import application.studyspace.services.Scenes.SceneSwitcher;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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

        // Seven columns for Mon–Sun
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(120);
            cc.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        // Day headers
        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int col = 0; col < days.length; col++) {
            Label lbl = new Label(days[col]);
            lbl.getStyleClass().add("calendar-day-header");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, col, 0);
        }

        // Day cells
        int row = 1, col = firstDayOfWeek - 1;
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate currentDay = yearMonth.atDay(d);
            VBox box = createDayBox(String.valueOf(d), currentDay);

            // populate events with click & hover behavior
            for (CalendarEvent ev : events) {
                if (ev.getStart().toLocalDate().equals(currentDay)) {
                    Label eventLabel = new Label(ev.getTitle());
                    eventLabel.getStyleClass().add("calendar-event");
                    eventLabel.setStyle("-fx-background-color: " + ev.getColor() + ";");

                    // hover effect
                    eventLabel.setOnMouseEntered(e ->
                            eventLabel.getStyleClass().add("calendar-event-hover")
                    );
                    eventLabel.setOnMouseExited(e ->
                            eventLabel.getStyleClass().remove("calendar-event-hover")
                    );

                    // click opens details popup
                    eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                        e.consume();
                        Stage owner = (Stage) eventLabel.getScene().getWindow();
                        SceneSwitcher.switchToPopupWithData(
                                owner,
                                "/application/studyspace/calendar/EventDetails.fxml",
                                "Event Details",
                                controller -> {
                                    EventDetailsController ctrl = (EventDetailsController) controller;
                                    ctrl.setEvent(ev);
                                }
                        );
                    });

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

        // Day headers Mon–Sun
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

        // Hour rows and events
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

                        // click to details
                        eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                            e.consume();
                            Stage owner = (Stage) eventLabel.getScene().getWindow();
                            SceneSwitcher.switchToPopupWithData(
                                    owner,
                                    "/application/studyspace/calendar/EventDetails.fxml",
                                    "Event Details",
                                    controller -> {
                                        EventDetailsController ctrl = (EventDetailsController) controller;
                                        ctrl.setEvent(ev);
                                    }
                            );
                        });

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

                    // click to details
                    eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                        e.consume();
                        Stage owner = (Stage) eventLabel.getScene().getWindow();
                        SceneSwitcher.switchToPopupWithData(
                                owner,
                                "/application/studyspace/calendar/EventDetails.fxml",
                                "Event Details",
                                controller -> {
                                    EventDetailsController ctrl = (EventDetailsController) controller;
                                    ctrl.setEvent(ev);
                                }
                        );
                    });

                    row.getChildren().add(eventLabel);
                }
            }

            vbox.getChildren().add(row);
        }

        return vbox;
    }

    // day-cell creation remains unchanged
    private static VBox createDayBox(String dayNumber, LocalDate date) {
        VBox box = new VBox();
        box.getStyleClass().add("calendar-day-box");
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(120, 100);

        Label dayNum = new Label(dayNumber);
        dayNum.getStyleClass().add("calendar-day-number");
        box.getChildren().add(dayNum);

        box.setOnMouseClicked(e -> {
            Stage owner = (Stage) box.getScene().getWindow();
            SceneSwitcher.switchToPopupWithData(
                    owner,
                    "/application/studyspace/calendar/CreateNewEvent.fxml",
                    "Create New Event",
                    controller -> {
                        CreateNewEventController popup = (CreateNewEventController) controller;
                        popup.setPreselectedDate(date);
                    }
            );
        });

        return box;
    }
}
