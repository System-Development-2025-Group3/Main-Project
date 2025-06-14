package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.onboarding.InputStudyDays;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * The OnboardingPage1Controller class manages the functionality of the first page in the onboarding process for a user.
 * It allows the user to configure their study preferences, including session timing, session length, break duration,
 * and blocked days. Users can navigate between different pages of the onboarding process, and save their preferences to a database.
 */
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

    /**
     * Sets the UUID of the user for the controller.
     *
     * @param uuid the UUID to associate with the user
     */
    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    /**
     * Initializes the controller for the onboarding page and sets up default values, UI components,
     * and event listeners for user interactions.
     *
     * This method performs the following actions:
     * - Populates the preferred time ComboBox with predefined options: "Morning", "Afternoon", "Evening".
     * - Configures the session length slider to snap its value to the nearest multiple of 5 and updates
     *   the corresponding label to display the rounded value in minutes.
     * - Configures the break length slider to snap its value to the nearest multiple of 5 and updates
     *   the corresponding label to display the rounded value in minutes.
     *
     * The method is automatically invoked by the JavaFX framework upon loading the associated FXML file.
     */
    @FXML
    private void initialize() {
        preferredTimeBox.getItems().addAll("Morning", "Afternoon", "Evening");

        sessionLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            sessionLengthSlider.setValue(rounded);                 // snap thumb
            sessionLengthLabel.setText(rounded + " min");
        });

        breakLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            breakLengthSlider.setValue(rounded);
            breakLengthLabel.setText(rounded + " min");
        });    }

    @FXML private void handlePage1(ActionEvent event) {
        // already on page 1, do nothing or reset form
    }

    /**
     * Event handler for transitioning to the second onboarding page.
     * This method retrieves the current popup stage and switches its content
     * to the FXML layout for Onboarding Page 2. It also initializes the controller
     * for the new scene with the user's UUID.
     *
     * @param event the ActionEvent triggered by the user interaction,
     *              typically a button press, which initiates the transition to the second page.
     */
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

    /**
     * Event handler for transitioning to the third onboarding page.
     * This method retrieves the current popup stage and switches its content
     * to the FXML layout for Onboarding Page 3. It also initializes the controller
     * for the new scene with the user's UUID.
     *
     * @param event the ActionEvent triggered by the user interaction,
     *              typically a button press, which initiates the transition to the third page.
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


    /**
     * Handles the save action for user study preferences. This method collects user inputs
     * for study preferences such as preferred study time, session length, break length,
     * and blocked days (unavailable days). It then creates an instance of the {@code InputStudyDays}
     * class and saves this data to the database. If the operation is successful, the popup
     * window is closed. Otherwise, an error message is printed.
     *
     * @param event the {@code ActionEvent} triggered by the user's interaction with the
     *              save button, which initiates the save process.
     */
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

        InputStudyDays prefs = new InputStudyDays(
                userUUID,
                preferredTimeBox.getValue(),
                (int) sessionLengthSlider.getValue(),
                (int) breakLengthSlider.getValue(),
                blockedDays.trim()
        );
        boolean success = prefs.saveToDatabase();

        if (success) {
            System.out.println("Study preferences saved.");
            SceneSwitcher.closePopup((Node) event.getSource());
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }
}
