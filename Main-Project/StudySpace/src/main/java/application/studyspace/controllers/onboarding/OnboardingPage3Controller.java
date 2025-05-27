package application.studyspace.controllers.onboarding;

import application.studyspace.services.onboarding.ExamInput;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;

public class OnboardingPage3Controller implements Initializable {

    @FXML
    private TextField examTitle;

    @FXML
    private DatePicker examDate;

    @FXML
    private ComboBox<String> examStyle;

    @FXML
    private TextField examPoint;

    private UUID userUUID;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        String[] items = {"Exam", "Speech"};
        examStyle.getItems().addAll(items);
    }

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @FXML
    public void handleInputSave(MouseEvent event) {
        try {
            String title = examTitle.getText();
            String type = examStyle.getValue();
            LocalDate date = examDate.getValue();
            int creditPoints = Integer.parseInt(examPoint.getText());

            ExamInput examInput = new ExamInput(title, type, date, creditPoints, userUUID);
            if (examInput.saveToDatabase()) {
                System.out.println("✅ Exam saved. Closing onboarding popup.");

                Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // ✅ Just close the popup, no reopening of Page 1
                popupStage.close();

            } else {
                System.err.println("❌ Saving exam failed.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in handleInputSave: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
