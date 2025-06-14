package application.studyspace;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.PasswordHasher;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.UUID;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("🚀 Starting app...");
        stage.setFullScreen(true); // Set fullscreen before scene is shown
        SceneSwitcher.switchTo(stage, "/application/studyspace/auth/Login.fxml", "StudySpace Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
