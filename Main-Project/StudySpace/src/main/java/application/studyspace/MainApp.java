package application.studyspace;

import application.studyspace.controllers.auth.LoginController;
import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.auth.AutoLoginHandler;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Create the splash screen (logo + animation)
        ImageView logo = new ImageView(new Image(getClass().getResource("/images/auth/LoginPage02.png").toExternalForm()));
        logo.setFitWidth(400);
        logo.setPreserveRatio(true);

        // Set up the root layout
        StackPane root = new StackPane(logo);
        root.setStyle("-fx-background-color: white;");

        // Create the scene
        Scene splashScene = new Scene(root, 800, 600);
        stage.setScene(splashScene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        // Play splash animations
        playSplashAnimation(stage, logo);
    }

    /**
     * Plays the splash screen animation and transitions to the main scene.
     */
    private void playSplashAnimation(Stage stage, ImageView logo) {
        // Pulse animation (scale up and down)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.8), logo);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(6); // 3 seconds of pulsation (6 cycles at 0.8 seconds each)

        // Fade-Out Transition
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), logo);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // Transition to the next scene after animations are done
        fadeTransition.setOnFinished(event -> loadNextScene(stage));

        // Combine animations
        SequentialTransition sequentialTransition = new SequentialTransition(scaleTransition, fadeTransition);
        sequentialTransition.play();
    }

    /**
     * Determines whether the user should be directed to the Landing Page (auto-login)
     * or Login Page and transitions to the relevant scene.
     */
    private void loadNextScene(Stage stage) {
        if (//AutoLoginHandler.AutoLoginIfPossible()) {
            false ){// Load Landing Page if auto-login succeeds
            SceneSwitcher.switchTo(stage, "/application/studyspace/landingpage/Landing-Page.fxml", "Landing Page");
        } else {
            // Otherwise, load Login Page
            SceneSwitcher.switchTo(stage, "/application/studyspace/auth/Login.fxml", "StudySpace Login");
        }

        // Ensure fullscreen mode is applied after the scene switch
        stage.setFullScreen(true);
    }



    public static void main(String[] args) {
        launch();
    }
}
