package application.studyspace;

import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.SplashScreenAnimator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    public void start(Stage ignoredPrimaryStage) {
        try {
            // Load user preferences for skipping the splash screen
            boolean skipSplash = SessionManager.getInstance().loadSkipSplashScreenPreference();

            if (skipSplash) {
                // If the preference is enabled, directly load the main application
                loadMainApplication();
            } else {
                // Otherwise, show the splash screen first
                Stage splashStage = new Stage();
                SplashScreenAnimator.showSplash(splashStage, this::loadMainApplication);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Optionally, fallback to showing the splash screen in case of errors
            Stage splashStage = new Stage();
            SplashScreenAnimator.showSplash(splashStage, this::loadMainApplication);
        }
    }

    /**
     * Loads the main application (RootLayout).
     */
    private void loadMainApplication() {
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
    }


    public static void main(String[] args) {
        launch(args);
    }
}
