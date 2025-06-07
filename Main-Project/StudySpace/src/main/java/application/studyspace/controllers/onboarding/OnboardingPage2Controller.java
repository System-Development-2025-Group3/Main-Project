package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * The OnboardingPage2Controller serves as the controller for the second page
 * of the onboarding process in a JavaFX application.
 * It handles the calendar view, file upload, drag-and-drop events,
 * and navigation between the onboarding pages.
 *
 * This class implements the {@link Initializable} interface
 * and overrides the {@code initialize} method for UI setup.
 *
 * Functionalities of this controller include:
 * - Rendering the calendar using event data.
 * - Navigating through months in the calendar view.
 * - Handling file uploads with a file chooser dialog.
 * - Supporting drag-and-drop for files in the calendar container.
 * - Navigating between different pages in the onboarding process.
 */
public class OnboardingPage2Controller implements Initializable {

    public Button page1Btn;
    public Button page2Btn;
    public Button page3Btn;
    public AnchorPane dropArea;
    @FXML private StackPane calendarContainer;
    @FXML private Label monthYearLabel;
    @FXML private Button handleUploadCSV;

    private LocalDate currentDate = LocalDate.now();
    private final List<CalendarEvent> events = CalendarEvent.getAllEvents();
    private UUID userUUID;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renderCalendar();

        // enable drag & drop on the calendarContainer’s parent (the VBox)
        calendarContainer.getParent().setOnDragOver(this::onDragOver);
        calendarContainer.getParent().setOnDragDropped(this::onDragDropped);
    }

    /**
     * Renders a calendar view in the application.
     *
     * This method clears any existing content in the calendar container and populates
     * it with a new monthly calendar view generated using the {@code CalendarView.buildMonthView}
     * method. The generated view displays the current date and associated events. The month
     * and year label is updated to reflect the currently selected month.
     *
     * The calendar view is dynamically built based on the fields {@code currentDate} and
     * {@code events}, where {@code currentDate} specifies the month currently being viewed
     * and {@code events} represent any associated calendar events for the month.
     */
    private void renderCalendar() {
        calendarContainer.getChildren().clear();
        calendarContainer.getChildren().add(
                CalendarView.buildMonthView(currentDate, events)
        );
        monthYearLabel.setText(
                currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + currentDate.getYear()
        );
    }

    @FXML private void handleUploadCSV(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File f = chooser.showOpenDialog(calendarContainer.getScene().getWindow());
        if (f != null) {
            System.out.println("Selected: " + f.getAbsolutePath());
            // TODO: parse CSV → events, then renderCalendar()
        }
    }

    /**
     * Handles the drag-over event during file drag-and-drop operations.
     *
     * This method checks whether the dragboard contains files and, if so,
     * accepts the transfer mode for copying the files into the component.
     * The method also consumes the drag event to prevent further processing
     * by other handlers in the event chain.
     *
     * @param e the {@link DragEvent} associated with the drag-over action,
     *          providing details about the current drag-and-drop operation.
     */
    @FXML private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    @FXML private void onDragDropped(DragEvent e) {
        List<File> files = e.getDragboard().getFiles();
        if (!files.isEmpty()) {
            System.out.println("Dropped: " + files.getFirst().getAbsolutePath());
            // TODO: parse CSV → events, then renderCalendar()
        }
        e.setDropCompleted(true);
        e.consume();
    }

    /**
     * Navigates to the previous month in the calendar view.
     *
     * This method updates the {@code currentDate} field by decrementing it by one month
     * and then triggers the re-rendering of the calendar view to reflect the updated month.
     *
     * @param e the {@link ActionEvent} triggered when the user interacts with the
     *          associated UI element to navigate to the previous month.
     */
    @FXML public void goToPrevious(ActionEvent e) {
        currentDate = currentDate.minusMonths(1);
        renderCalendar();
    }

    /**
     * Advances the current calendar view to the next month.
     *
     * This method updates the {@code currentDate} field by incrementing it by one month
     * and refreshes the calendar display by invoking the {@code renderCalendar} method.
     *
     * @param e the {@link ActionEvent} triggered by the user interaction (such as clicking
     *          a navigation button) to advance the calendar view.
     */
    @FXML public void goToNext(ActionEvent e) {
        currentDate = currentDate.plusMonths(1);
        renderCalendar();
    }

    // ——— Pager handlers ———

    /**
     * Handles the navigation event for Page 1 in the onboarding process.
     *
     * This method replaces the content of the popup window with the layout and
     * controller logic of "OnboardingPage1.fxml". It also initializes the
     * controller for onboarding page 1 by passing the current user's UUID.
     *
     * @param event the {@link ActionEvent} triggered by the user interaction
     *              with the associated UI element to navigate to onboarding page 1.
     */
    @FXML
    private void handlePage1(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneSwitcher.<OnboardingPage1Controller>switchPopupContent(
                popupStage,
                "/application/studyspace/onboarding/OnboardingPage1.fxml",
                "Onboarding Page 1",
                controller -> controller.setUserUUID(userUUID)
        );
    }

    /**
     * Handles the navigation event for Page 2.
     *
     * This method is triggered when the associated UI component is activated.
     * It performs no actions as Page 2 is already the current page, but it can
     * optionally refresh the content if necessary.
     *
     * @param event the {@link ActionEvent} triggered by the user interaction
     *              with the associated UI element for Page 2.
     */
    @FXML
    private void handlePage2(ActionEvent event) {
        // Already on page 2, do nothing or optionally refresh
    }

    /**
     * Handles navigation to the onboarding page 3 in the popup window.
     *
     * This method uses the SceneSwitcher utility to replace the content of the
     * popup window with the layout and controller logic of "OnboardingPage3.fxml".
     * It initializes the controller for onboarding page 3 by passing the
     * current user's UUID.
     *
     * @param event the {@link ActionEvent} triggered when the user interacts
     *              with the associated UI element to navigate to onboarding page 3.
     */
    @FXML
    private void handlePage3(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneSwitcher.<OnboardingPage3Controller>switchPopupContent(
                popupStage,
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                "Onboarding Page 3",
                controller -> controller.setUserUUID(userUUID)
        );
    }

}
