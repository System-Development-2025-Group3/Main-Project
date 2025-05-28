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

    @FXML private void goToNextPreview() {
        previewDate = previewDate.plusDays(7);
        updatePreview();
    }

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
