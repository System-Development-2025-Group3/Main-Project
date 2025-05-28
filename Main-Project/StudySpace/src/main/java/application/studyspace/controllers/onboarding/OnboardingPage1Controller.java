package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.onboarding.InputStudyDays;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

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

    private UUID userUUID;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @FXML
    private void initialize() {
        preferredTimeBox.getItems().addAll("Morning", "Afternoon", "Evening");

        sessionLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sessionLengthLabel.setText((int) newVal.doubleValue() + " min");
        });

        breakLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            breakLengthLabel.setText((int) newVal.doubleValue() + " min");
        });
    }

    @FXML private void handlePage1(ActionEvent event) {
        // already on page 1, do nothing or reset form
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


    @FXML
    private void handleSave(ActionEvent event) {
        String blockedDays = "";
        if (monBtn.isSelected()) blockedDays += "Monday ";
        if (tueBtn.isSelected()) blockedDays += "Tuesday ";
        if (wedBtn.isSelected()) blockedDays += "Wednesday ";
        if (thuBtn.isSelected()) blockedDays += "Thursday ";
        if (friBtn.isSelected()) blockedDays += "Friday ";
        if (satBtn.isSelected()) blockedDays += "Saturday ";
        if (sunBtn.isSelected()) blockedDays += "Sunday ";
        blockedDays = blockedDays.trim();

        InputStudyDays studyData = new InputStudyDays(
                userUUID,
                preferredTimeBox.getValue(),
                String.valueOf((int) sessionLengthSlider.getValue()),
                String.valueOf((int) breakLengthSlider.getValue()),
                blockedDays.trim()
        );

        boolean success = studyData.saveToDatabase();
        if (success) {
            System.out.println("Study preferences saved.");
            SceneSwitcher.closePopup((Node) event.getSource());
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }
}
