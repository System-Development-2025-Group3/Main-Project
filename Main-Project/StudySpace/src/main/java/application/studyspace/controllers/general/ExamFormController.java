package application.studyspace.controllers.general;

import application.studyspace.controllers.auth.LoginController;
import application.studyspace.services.form.ExamInput;
import application.studyspace.services.Scenes.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
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
        String [] items = {"Exam", "Speech"};
        examStyle.getItems().addAll(items);
    }

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    public void handleinputSave(MouseEvent event) {

        String title = examTitle.getText();
        String type = examStyle.getValue();
        LocalDate date = examDate.getValue();
        int creditPoints = Integer.parseInt(examPoint.getText());

        ExamInput examInput = new ExamInput(title, type, date, creditPoints, userUUID);
        examInput.saveToDatabase();
        }

    }

