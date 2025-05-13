package application.studyspace.controllers.settings;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SettingsMenuController {

    public Button ButtonSettingsAccount;
    public BorderPane BorderpaneSettings;
    public Button ButtonSettingsAppearance;
    public Button ButtonSettingsNotifications;
    public Button ButtonSettingsLanguage;
    public Button ButtonSettingsTracking;
    public StackPane contentPane;
    @FXML
    private Button closePopupButton; // Matches the fx:id in SettingsPopUp.fxml

    /**
     * Handles the close button click to close the popup window.
     *
     * @param mouseEvent The mouse event triggered when the button is clicked.
     */
    @FXML
    public void handleClickClosePopupButton(MouseEvent mouseEvent) {
        // Get the stage associated with the close button and close it
        Stage stage = (Stage) closePopupButton.getScene().getWindow();
        stage.close();
    }
}