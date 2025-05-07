package application.studyspace.controllers.landingpage;

import application.studyspace.services.calendar.SimpleCalendarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class LandingpageController implements Initializable {

    @FXML
    private StackPane calendarContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        calendarContainer.getChildren().add(SimpleCalendarView.build(LocalDate.now()));
    }
}
