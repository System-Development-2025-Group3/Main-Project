package application.studyspace.controllers.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Settingscontroller {

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