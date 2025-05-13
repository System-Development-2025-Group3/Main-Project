package application.studyspace.controllers.general;

import application.studyspace.services.form.ExamInput;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.controllers.general.StudyDaysFormController;
import javafx.application.Platform;
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

public class ExamFormController implements Initializable {

    @FXML
    private TextField examTitle;

    @FXML
    private DatePicker examDate;

    @FXML
    private ComboBox<String> examStyle;

    @FXML
    private TextField examPoint;

    @FXML
    private TextField examModule;

    private UUID userUUID;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        String[] items = {"Exam", "Speech"};
        examStyle.getItems().addAll(items);
    }

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    public void handleinputSave(MouseEvent event) {
        try {
            String title = examTitle.getText();
            String type = examStyle.getValue();
            LocalDate date = examDate.getValue();
            int creditPoints = Integer.parseInt(examPoint.getText());

            ExamInput examInput = new ExamInput(title, type, date, creditPoints, userUUID);
            if (examInput.saveToDatabase()) {
                System.out.println("✅ Exam saved. Opening StudyDays popup...");

                Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Stage landingPageStage = (Stage) popupStage.getOwner();

                // ✅ Close current popup
                popupStage.close();

                // ✅ Wait until current event cycle finishes, then open next popup
                Platform.runLater(() -> {
                    SceneSwitcher.<StudyDaysFormController>switchToPopupWithData(
                            landingPageStage,
                            "/application/studyspace/usermanagement/User-Formular-study-days.fxml",
                            "Study Preferences",
                            controller -> controller.setUserUUID(userUUID)
                    );
                });

            } else {
                System.err.println("❌ Saving exam failed.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in handleinputSave: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
