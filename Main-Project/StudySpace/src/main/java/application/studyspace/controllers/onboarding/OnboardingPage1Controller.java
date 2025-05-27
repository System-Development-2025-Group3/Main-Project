package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.onboarding.InputStudyDays;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.util.UUID;

public class OnboardingPage1Controller {

    @FXML private ComboBox<String> preferredTimeBox;
    @FXML private Slider sessionLengthSlider;
    @FXML private Label sessionLengthLabel;
    @FXML private Slider breakLengthSlider;
    @FXML private Label breakLengthLabel;

    @FXML private CheckBox mondayCheck;
    @FXML private CheckBox tuesdayCheck;
    @FXML private CheckBox wednesdayCheck;
    @FXML private CheckBox thursdayCheck;
    @FXML private CheckBox fridayCheck;
    @FXML private CheckBox saturdayCheck;
    @FXML private CheckBox sundayCheck;

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
        Stage landingPageStage = (Stage) popupStage.getOwner();
        popupStage.close();

        Platform.runLater(() -> {
            SceneSwitcher.<OnboardingPage2Controller>switchToPopupWithData(
                    landingPageStage,
                    "/application/studyspace/onboarding/ImportCalendarPopUp.fxml",
                    "Import Calendar",
                    controller -> controller.setUserUUID(userUUID)
            );
        });
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage landingPageStage = (Stage) popupStage.getOwner();
        popupStage.close();

        Platform.runLater(() -> {
            SceneSwitcher.<OnboardingPage3Controller>switchToPopupWithData(
                    landingPageStage,
                    "/application/studyspace/onboarding/ExamOnboardingPopUp.fxml",
                    "Import Calendar",
                    controller -> controller.setUserUUID(userUUID)
            );
        });
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String blockedDays = "";
        if (mondayCheck.isSelected()) blockedDays += "Monday ";
        if (tuesdayCheck.isSelected()) blockedDays += "Tuesday ";
        if (wednesdayCheck.isSelected()) blockedDays += "Wednesday ";
        if (thursdayCheck.isSelected()) blockedDays += "Thursday ";
        if (fridayCheck.isSelected()) blockedDays += "Friday ";
        if (saturdayCheck.isSelected()) blockedDays += "Saturday ";
        if (sundayCheck.isSelected()) blockedDays += "Sunday ";

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
