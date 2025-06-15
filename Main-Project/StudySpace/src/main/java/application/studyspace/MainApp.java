package application.studyspace;

import application.studyspace.controllers.auth.LoginController;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.AutoLoginHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("🚀 Starting app...");
        stage.setFullScreen(true); // Set fullscreen before scene is shown

        if(AutoLoginHandler.AutoLoginIfPossible()) {
            SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Landing-Page.fxml", "StudySpace Landing Page - Welcome,");
            stage.show();
        } else {
            SceneSwitcher.switchTo(stage, "/application/studyspace/auth/Login.fxml", "StudySpace Login");
            stage.show();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}
