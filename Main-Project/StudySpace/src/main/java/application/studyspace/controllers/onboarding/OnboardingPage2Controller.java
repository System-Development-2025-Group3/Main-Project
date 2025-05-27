package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.util.UUID;

public class OnboardingPage2Controller {

    private UUID userUUID;

    /** Called by SceneSwitcher to pass along the current user ID. */
    public void setUserUUID(UUID userUUID) {
        this.userUUID = userUUID;
    }

    @FXML
    private void initialize() {
        // no-op for now
    }

    @FXML
    private void handlePage1(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage landingPageStage = (Stage) popupStage.getOwner();
        popupStage.close();

        Platform.runLater(() -> {
            SceneSwitcher.<OnboardingPage1Controller>switchToPopupWithData(
                    landingPageStage,
                    "/application/studyspace/onboarding/StudyPreferencesOnboardingPopUp.fxml",
                    "Import Calendar",
                    controller -> controller.setUserUUID(userUUID)
            );
        });
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        // Already on Import Calendar → do nothing
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
}
