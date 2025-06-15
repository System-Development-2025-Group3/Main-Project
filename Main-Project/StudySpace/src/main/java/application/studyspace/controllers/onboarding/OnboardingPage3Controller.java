package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import com.calendarfx.view.CalendarView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

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
                previewDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) +
                        " " + previewDate.getDayOfMonth() +
                        ", " + previewDate.getYear()
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
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", controller -> {});
    }

    @FXML private void handlePage2(ActionEvent event) {
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage2.fxml", controller -> {});
    }

    @FXML private void handlePage3() {
        // Already on this page
    }

    @FXML private void handleSave(ActionEvent event) {
        // TODO: Save logic goes here

        // For now, just close the overlay
        ViewManager.closeTopOverlay();
    }
}
