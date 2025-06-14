package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class OnboardingPage3Controller implements Initializable {

    @FXML private StackPane calendarPreviewContainer;
    @FXML private Label previewDateLabel;

    @FXML private Button SaveBtn;
    @FXML private Button page1Btn, page2Btn, page3Btn;

    @FXML private TextField examNameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField estimatedHoursField;
    @FXML private ComboBox<String> typeCombo;

    @FXML private ToggleButton colorRed;
    @FXML private ToggleButton colorBlue;
    @FXML private ToggleButton colorGreen;
    @FXML private ToggleButton colorYellow;
    @FXML private ToggleButton colorPurple;

    private CalendarView calendarView;
    private LocalDate previewDate;
    private UUID userUUID;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        previewDate = LocalDate.now();

        calendarView = new CalendarView();
        calendarView.showDayPage();
        calendarView.setDate(previewDate);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarPreviewContainer.getChildren().setAll(calendarView);

        updateLabel();
    }

    private void updateLabel() {
        previewDateLabel.setText(
                previewDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        + " " + previewDate.getDayOfMonth()
                        + ", " + previewDate.getYear()
        );
    }

    @FXML private void goToNextPreview() {
        previewDate = previewDate.plusDays(1);
        calendarView.setDate(previewDate);
        updateLabel();
    }

    @FXML private void goToPreviousPreview() {
        previewDate = previewDate.minusDays(1);
        calendarView.setDate(previewDate);
        updateLabel();
    }

    @FXML private void handlePage1(ActionEvent event) {
        Stage popup = (Stage)((Node)event.getSource()).getScene().getWindow();
        SceneSwitcher.<OnboardingPage1Controller>switchPopupContent(
                popup,
                "/application/studyspace/onboarding/OnboardingPage1.fxml",
                "Onboarding Page 1",
                ctrl -> ctrl.setUserUUID(userUUID)
        );
    }

    @FXML private void handlePage2(ActionEvent event) {
        Stage popup = (Stage)((Node)event.getSource()).getScene().getWindow();
        SceneSwitcher.<OnboardingPage2Controller>switchPopupContent(
                popup,
                "/application/studyspace/onboarding/OnboardingPage2.fxml",
                "Onboarding Page 2",
                ctrl -> ctrl.setUserUUID(userUUID)
        );
    }

    @FXML private void handlePage3() {
        // Already here
    }

    @FXML private void handleSave(ActionEvent event) {
        // TODO: Save logic
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
}
