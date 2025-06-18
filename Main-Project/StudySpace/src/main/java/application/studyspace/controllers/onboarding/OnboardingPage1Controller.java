package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.InputStudyPreferences;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.util.UUID;

public class OnboardingPage1Controller {

    public Button page1Btn;
    public Button page2Btn;
    public Button page3Btn;
    @FXML private ComboBox<String> preferredTimeBox;
    @FXML private Slider sessionLengthSlider;
    @FXML private Label sessionLengthLabel;
    @FXML private Slider breakLengthSlider;
    @FXML private Label breakLengthLabel;

    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    @FXML
    private void initialize() {
        preferredTimeBox.getItems().addAll("Morning", "Afternoon", "Evening");

        sessionLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            sessionLengthSlider.setValue(rounded);
            sessionLengthLabel.setText(rounded + " min");
        });

        breakLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            breakLengthSlider.setValue(rounded);
            breakLengthLabel.setText(rounded + " min");
        });
    }

    @FXML
    private void handlePage1(ActionEvent event) {
        // Already on page 1
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        handleSave(event); // Save the current form
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage2.fxml",
                controller -> {} // No setup needed; controller uses SessionManager
        );
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        handleSave(event); // Save the current form
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                controller -> {}
        );
    }

    @FXML
    private void handleSave(ActionEvent event) {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();

        String blockedDays = "";
        if (monBtn.isSelected()) blockedDays += "Monday ";
        if (tueBtn.isSelected()) blockedDays += "Tuesday ";
        if (wedBtn.isSelected()) blockedDays += "Wednesday ";
        if (thuBtn.isSelected()) blockedDays += "Thursday ";
        if (friBtn.isSelected()) blockedDays += "Friday ";
        if (satBtn.isSelected()) blockedDays += "Saturday ";
        if (sunBtn.isSelected()) blockedDays += "Sunday ";
        blockedDays = blockedDays.trim();

        InputStudyPreferences prefs = new InputStudyPreferences(
                userUUID,
                preferredTimeBox.getValue(),
                (int) sessionLengthSlider.getValue(),
                (int) breakLengthSlider.getValue(),
                blockedDays
        );
        boolean success = prefs.saveToDatabase();

        if (success) {
            System.out.println("Study preferences saved.");
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }
}
