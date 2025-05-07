package application.studyspace.controllers.forms;

import application.studyspace.services.ExamInput;
import application.studyspace.services.SceneSwitcher;
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        String [] items = {"Exam", "Speech"};
        examStyle.getItems().addAll(items);
    }

    public void handleinputSave(MouseEvent event) {

        String title = examTitle.getText();
        String module = examModule.getText(); // Modulname
        String type = examStyle.getValue();
        LocalDate date = examDate.getValue();
        int creditPoints = Integer.parseInt(examPoint.getText());
        int userID = 234525;

        ExamInput examInput = new ExamInput(title, module, type, date, creditPoints, userID);
        examInput.saveToDatabase();
        }

    }

