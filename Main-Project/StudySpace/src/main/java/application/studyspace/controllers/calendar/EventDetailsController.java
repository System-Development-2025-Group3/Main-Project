package application.studyspace.controllers.calendar;

import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.DeleteCalendarEvent;
import application.studyspace.controllers.landingpage.LandingpageController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label startLabel;
    @FXML private Label endLabel;
    @FXML private Label tagLabel;
    @FXML private Button deleteBtn;
    @FXML private Button closeBtn;

    private CalendarEvent event;

    @FXML
    public void initialize() {
        // Close popup when the Close button is clicked
        closeBtn.setOnAction(e ->
                ((Stage) closeBtn.getScene().getWindow()).close()
        );
        // Delete button will be wired once event is set
    }

    /**
     * Populate all fields from the given event, and wire the Delete button.
     * Called by CalendarView when opening this details popup.
     */
    public void setEvent(CalendarEvent ev) {
        this.event = ev;

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

        // Fill in UI labels
        titleLabel     .setText(ev.getTitle());
        descriptionArea.setText(ev.getDescription());
        startLabel     .setText(ev.getStart().toLocalDate().format(dateFmt));
        endLabel       .setText(
                ev.getStart().format(timeFmt) + " – " + ev.getEnd().format(timeFmt)
        );
        tagLabel.setText(ev.getTag());

        // Wire Delete button
        deleteBtn.setOnAction(e -> {
            // 1) Delete from DB
            DeleteCalendarEvent.delete(event);
            // 2) Refresh the calendar in LandingpageController
            LandingpageController.getInstance().showMonthView();
            // 3) Close this popup
            ((Stage) deleteBtn.getScene().getWindow()).close();
        });
    }
}
