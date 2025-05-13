package application.studyspace.controllers.general;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.form.InputStudyDays;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;


import java.util.UUID;

public class StudyDaysFormController {

    @FXML private ComboBox<String> preferredTimeBox;
    @FXML private ComboBox<String> dailyLimitBox;
    @FXML private ComboBox<String> sessionLimitBox;
    @FXML private ComboBox<String> breakLengthBox;
    @FXML private TextField focusTimeField;
    @FXML private TextField unavailableDaysField;
    @FXML private TextField freeDaysField;
    @FXML private ComboBox<String> concentrationTimeBox;
    @FXML private ComboBox<String> sessionTypeBox;

    private UUID userUUID;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @FXML
    private void initialize() {
        preferredTimeBox.getItems().addAll("Morning", "Afternoon", "Evening");
        dailyLimitBox.getItems().addAll("2", "3", "4", "5", "6");
        sessionLimitBox.getItems().addAll("25", "45", "60", "90");
        breakLengthBox.getItems().addAll("5", "10", "15", "20");
        concentrationTimeBox.getItems().addAll("Morning", "Midday", "Evening");
        sessionTypeBox.getItems().addAll("Pomodoro", "Deep Work", "Freestyle");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        InputStudyDays studyData = new InputStudyDays(
                userUUID,
                preferredTimeBox.getValue(),
                dailyLimitBox.getValue(),
                sessionLimitBox.getValue(),
                breakLengthBox.getValue(),
                focusTimeField.getText(),
                unavailableDaysField.getText(),
                freeDaysField.getText(),
                concentrationTimeBox.getValue(),
                sessionTypeBox.getValue()
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
