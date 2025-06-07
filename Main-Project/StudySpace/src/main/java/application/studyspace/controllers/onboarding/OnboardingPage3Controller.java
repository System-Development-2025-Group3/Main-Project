package application.studyspace.controllers.onboarding;

import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarView;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.onboarding.OnboardingEventInput;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller class responsible for managing functionality and interactions
 * on the third page of the onboarding sequence. This includes UI component
 * initialization, navigation to other onboarding pages, updating the calendar
 * preview, and handling user inputs.
 *
 * Implements the {@code Initializable} interface, allowing custom setup and
 * initialization logic for this controller.
 */
public class OnboardingPage3Controller implements Initializable {

    public Button page1Btn;
    public Button page2Btn;
    public Button page3Btn;
    public Button SaveBtn;
    @FXML private TextField examNameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField estimatedHoursField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ToggleGroup colorGroup;

    @FXML private Label previewMonthLabel;
    @FXML private StackPane calendarPreviewContainer;

    private LocalDate previewDate = LocalDate.now();
    private final List<CalendarEvent> events = CalendarEvent.getAllEvents();
    private UUID userUUID;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1) populate type selector
        typeCombo.getItems().addAll("Exam", "Blocker");
        updatePreview();
    }

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    /**
     * Updates the calendar preview component to display the week view for the current
     * preview date and sets the corresponding label displaying the range of dates.
     *
     * The method does the following:
     * 1. Calculates the start and end dates of the week based on the {@code previewDate}.
     * 2. Builds a week view using the {@code CalendarView.buildWeekView} method, populating it with events.
     * 3. Updates the {@code calendarPreviewContainer} with the newly built week view.
     * 4. Generates a textual representation of the week’s date range in the format
     *    "StartMonth StartDay – EndMonth EndDay, Year" and sets it on the {@code previewMonthLabel}.
     *
     * Dependencies:
     * - The method uses the {@code previewDate} field as the reference date.
     * - The list of events is accessed through the {@code events} field.
     * - The visual calendar view is updated in the {@code calendarPreviewContainer}.
     * - The label displaying the date range is updated through the {@code previewMonthLabel}.
     *
     * This method assumes that {@code previewDate} has already been set and represents
     * the starting date for the week view to be generated.
     */
    private void updatePreview() {
        // build a week view and update label to "MonDay – SunDay, Year"
        LocalDate start = previewDate;
        LocalDate end = previewDate.plusDays(6);

        calendarPreviewContainer.getChildren().setAll(
                CalendarView.buildWeekView(start, events)
        );

        String startMonth = start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String endMonth   = end.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        previewMonthLabel.setText(
                startMonth + " " + start.getDayOfMonth()
                        + " – "
                        + endMonth   + " " + end.getDayOfMonth()
                        + ", " + end.getYear()
        );
    }

    /**
     * Advances the preview date by one week and updates the calendar preview.
     *
     * This method increments the current `previewDate` by 7 days, effectively
     * shifting the displayed weekly calendar view forward by one week.
     * After updating the preview date, the method invokes `updatePreview()`
     * to refresh the UI components, including updating the label to reflect
     * the new date range and rebuilding the weekly calendar view.
     *
     * Note: This method is triggered by a UI event handler, typically when
     * the user interacts with a specific control (e.g., a "Next" button).
     */
    @FXML private void goToNextPreview() {
        previewDate = previewDate.plusDays(7);
        updatePreview();
    }

    /**
     * Navigates to the previous week in the calendar preview.
     *
     * This method modifies the `previewDate` field by subtracting 7 days, effectively moving
     * the displayed preview week backward by one week. After updating the date, it calls
     * the `updatePreview` method to refresh the calendar preview and update the associated label
     * to reflect the new date range.
     *
     * The operation is triggered by a user interaction in the interface, typically linked
     * to a button or other UI element.
     */
    @FXML private void goToPreviousPreview() {
        previewDate = previewDate.minusDays(7);
        updatePreview();
    }

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

    @FXML
    private void handlePage2(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneSwitcher.<OnboardingPage2Controller>switchPopupContent(
                popupStage,
                "/application/studyspace/onboarding/OnboardingPage2.fxml",
                "Onboarding Page 2",
                controller -> controller.setUserUUID(userUUID)
        );
    }

    @FXML private void handlePage3() {
        // no-op: already on Page 3
    }

    @FXML
    private void handleSave(ActionEvent event) {
        //temporary solution
        Stage popup = (Stage) ((Node) event.getSource()).getScene().getWindow();
        popup.close();
        /** String title = examNameField.getText();
         String desc  = descriptionArea.getText();
         int hours    = Integer.parseInt(estimatedHoursField.getText());
         String type  = typeCombo.getValue();
         String color = ((ToggleButton) colorGroup.getSelectedToggle())
         .getId()
         .replace("color", "")
         .toLowerCase();

         OnboardingEventInput input = new OnboardingEventInput(
         userUUID, title, desc, hours, type, color
         );

         if (input.saveToDatabase()) {
         // Stage popup = (Stage) ((Node)event.getSource()).getScene().getWindow();
         //popup.close();
         // proceed…
         } else {
         // show error to user…
         }
         **/
    }
}
