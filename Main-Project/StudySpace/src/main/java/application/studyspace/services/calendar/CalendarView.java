package application.studyspace.services.calendar;

import application.studyspace.controllers.calendar.CreateNewEventController;
import application.studyspace.controllers.calendar.EventDetailsController;
import application.studyspace.services.Scenes.SceneSwitcher;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.*;
import java.util.List;

public class CalendarView {

    private static final double HOUR_ROW_HEIGHT = 40;
    private static final double HOUR_LABEL_WIDTH  = 50;
    private static final double WEEK_TOTAL_WIDTH = 1000;

    /** Month‐view: grid of day cells with clickable events and click‐to‐create. */
    public static Node buildMonthView(LocalDate date, List<CalendarEvent> events) {
        YearMonth ym = YearMonth.from(date);
        LocalDate first = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();
        int firstDow = first.getDayOfWeek().getValue() - 1; // 0=Mon

        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        // 7 equal columns
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints(120);
            cc.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        // Day headers
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.getStyleClass().add("calendar-day-header");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            grid.add(lbl, i, 0);
        }

        // Populate days & events
        int row = 1, col = firstDow;
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate day = ym.atDay(d);
            VBox cell = createDayBox(String.valueOf(d), day);

            // Add events in this day
            for (CalendarEvent ev : events) {
                if (ev.getStart().toLocalDate().equals(day)) {
                    Label eventLabel = new Label(ev.getTitle());
                    eventLabel.getStyleClass().addAll("calendar-event", ev.getColor().getCssClass());
                    eventLabel.setMaxWidth(Double.MAX_VALUE);
                    eventLabel.prefWidthProperty().bind(cell.widthProperty().subtract(16));

                    // still use hoverProperty for the pill itself if you like:
                    eventLabel.hoverProperty().addListener((obs, wasH, isH) -> {
                        if (isH) {
                            if (!eventLabel.getStyleClass().contains("hover"))
                                eventLabel.getStyleClass().add("hover");
                        } else {
                            eventLabel.getStyleClass().remove("hover");
                        }
                    });

                    eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
                        me.consume();
                        Stage owner = (Stage) eventLabel.getScene().getWindow();
                        SceneSwitcher.switchToPopupWithData(
                                owner,
                                "/application/studyspace/calendar/EventDetails.fxml",
                                "Event Details",
                                ctrl -> ((EventDetailsController)ctrl).setEvent(ev)
                        );
                    });

                    cell.getChildren().add(eventLabel);
                }
            }

            grid.add(cell, col, row);
            if (++col == 7) { col = 0; row++; }
        }

        return grid;
    }

    /** Single-day cell that opens Create New Event on click. */
    private static VBox createDayBox(String dayNumber, LocalDate date) {
        VBox box = new VBox();
        box.getStyleClass().add("calendar-day-box");
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(120, 100);

        Label lbl = new Label(dayNumber);
        lbl.getStyleClass().add("calendar-day-number");
        box.getChildren().add(lbl);

        // bind the "hover" style to the node’s aggregated hover state
        box.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered) {
                if (!box.getStyleClass().contains("hover"))
                    box.getStyleClass().add("hover");
            } else {
                box.getStyleClass().remove("hover");
            }
        });

        // Click → open Create New Event
        box.setOnMouseClicked(e -> {
            Stage owner = (Stage)box.getScene().getWindow();
            SceneSwitcher.switchToPopupWithData(
                    owner,
                    "/application/studyspace/calendar/CreateNewEvent.fxml",
                    "Create New Event",
                    ctrl -> ((CreateNewEventController)ctrl).setPreselectedDate(date)
            );
        });

        return box;
    }


    /** Week‐view: grid + overlay of event pills. */
    public static Node buildWeekView(LocalDate refDate, List<CalendarEvent> events) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        grid.setHgap(1);
        grid.setVgap(1);

        // Hour-label column
        ColumnConstraints c0 = new ColumnConstraints(HOUR_LABEL_WIDTH);
        c0.setHalignment(HPos.CENTER);
        grid.getColumnConstraints().add(c0);

        // Seven day columns
        double dayW = (WEEK_TOTAL_WIDTH - HOUR_LABEL_WIDTH) / 7;
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints(dayW);
            cc.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        // 24 hourly rows + labels
        for (int h = 0; h < 24; h++) {
            RowConstraints rc = new RowConstraints(HOUR_ROW_HEIGHT);
            grid.getRowConstraints().add(rc);

            Label hl = new Label(String.format("%02d:00", h));
            hl.getStyleClass().add("calendar-hour-label");
            hl.setMinWidth(HOUR_LABEL_WIDTH);
            hl.setAlignment(Pos.CENTER_RIGHT);
            grid.add(hl, 0, h);
        }

        // Overlay pane for event pills
        Pane overlay = new Pane();
        overlay.setPickOnBounds(false);

        LocalDate monday = refDate.with(DayOfWeek.MONDAY);
        for (CalendarEvent ev : events) {
            LocalDateTime s = ev.getStart();
            LocalDateTime e = ev.getEnd();
            long idx = Duration.between(
                    monday.atStartOfDay(),
                    s.toLocalDate().atStartOfDay()
            ).toDays();
            if (idx < 0 || idx > 6) continue;

            double y      = (s.getHour() + s.getMinute()/60.0) * HOUR_ROW_HEIGHT;
            double height = Duration.between(s, e).toMinutes()/60.0 * HOUR_ROW_HEIGHT;
            double x      = HOUR_LABEL_WIDTH + idx * dayW + 4;
            double w      = dayW - 8;

            Label eventLabel = new Label(ev.getTitle());
            eventLabel.getStyleClass().add("calendar-event");  // only base class here

            // Manual pill hover
            eventLabel.setOnMouseEntered(me -> eventLabel.getStyleClass().add("hover"));
            eventLabel.setOnMouseExited (me -> eventLabel.getStyleClass().remove("hover"));

            // Click → show details
            eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
                me.consume();
                Stage owner = (Stage) eventLabel.getScene().getWindow();
                SceneSwitcher.switchToPopupWithData(
                        owner,
                        "/application/studyspace/calendar/EventDetails.fxml",
                        "Event Details",
                        ctrl -> ((EventDetailsController)ctrl).setEvent(ev)
                );
            });

            eventLabel.setLayoutX(x);
            eventLabel.setLayoutY(y);
            eventLabel.setPrefWidth(w);
            eventLabel.setPrefHeight(Math.max(height, HOUR_ROW_HEIGHT * 0.5));
            overlay.getChildren().add(eventLabel);
        }

        return new StackPane(grid, overlay);
    }

    /** Day‐view: hourly rows + overlay of event pills. */
    public static Node buildDayView(LocalDate date, List<CalendarEvent> events) {
        VBox background = new VBox();
        background.getStyleClass().add("calendar-grid");
        background.setSpacing(1);

        // Hour rows & labels
        for (int h = 0; h < 24; h++) {
            HBox row = new HBox();
            row.getStyleClass().add("calendar-hour-cell");
            row.setPrefHeight(HOUR_ROW_HEIGHT);
            row.setAlignment(Pos.TOP_LEFT);

            Label timeLbl = new Label(String.format("%02d:00", h));
            timeLbl.getStyleClass().add("calendar-hour-label");
            timeLbl.setPrefWidth(HOUR_LABEL_WIDTH);

            Region filler = new Region();
            HBox.setHgrow(filler, Priority.ALWAYS);

            row.getChildren().addAll(timeLbl, filler);
            background.getChildren().add(row);
        }

        // Overlay pane for event pills
        Pane overlay = new Pane();
        overlay.setPickOnBounds(false);

        for (CalendarEvent ev : events) {
            if (!ev.getStart().toLocalDate().equals(date)) continue;
            LocalDateTime s = ev.getStart();
            LocalDateTime e = ev.getEnd();

            double y      = (s.getHour() + s.getMinute()/60.0) * HOUR_ROW_HEIGHT;
            double height = Duration.between(s, e).toMinutes()/60.0 * HOUR_ROW_HEIGHT;
            double x      = HOUR_LABEL_WIDTH + 4;
            double w      = background.getWidth() - HOUR_LABEL_WIDTH - 8;

            Label eventLabel = new Label(ev.getTitle());
            eventLabel.getStyleClass().add("calendar-event");

            // Manual pill hover
            eventLabel.setOnMouseEntered(me -> eventLabel.getStyleClass().add("hover"));
            eventLabel.setOnMouseExited (me -> eventLabel.getStyleClass().remove("hover"));

            eventLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
                me.consume();
                Stage owner = (Stage) eventLabel.getScene().getWindow();
                SceneSwitcher.switchToPopupWithData(
                        owner,
                        "/application/studyspace/calendar/EventDetails.fxml",
                        "Event Details",
                        ctrl -> ((EventDetailsController)ctrl).setEvent(ev)
                );
            });

            eventLabel.setLayoutX(x);
            eventLabel.setLayoutY(y);
            eventLabel.setPrefWidth(w);
            eventLabel.setPrefHeight(Math.max(height, HOUR_ROW_HEIGHT * 0.5));
            overlay.getChildren().add(eventLabel);
        }

        return new StackPane(background, overlay);
    }
}
