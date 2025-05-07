package application.studyspace.controllers;

import application.studyspace.services.SurveyPopup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class LandingPageController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // When the landing page loads, open the survey popup
        SurveyPopup.showSurvey("exampleUser"); // Use username here later from login
    }
}
