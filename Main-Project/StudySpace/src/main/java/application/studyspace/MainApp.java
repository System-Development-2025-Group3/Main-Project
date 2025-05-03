package application.studyspace;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("LoginPage");
        stage.setScene(scene);

        // Set the stage to full screen
        stage.setFullScreen(true);
        stage.setResizable(true);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}