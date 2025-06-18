package application.studyspace;

import application.studyspace.services.auth.SplashScreenAnimator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage ignoredPrimaryStage) {
        Stage splashStage = new Stage();
        SplashScreenAnimator.showSplash(splashStage, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/studyspace/scenes/RootLayout.fxml"));
                Parent root = loader.load();

                Stage mainStage = new Stage();
                Scene scene = new Scene(root);
                mainStage.setScene(scene);
                mainStage.setFullScreen(true);
                mainStage.setTitle("StudySpace");
                mainStage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
