package application.studyspace.services.auth;

// Imports for animation and JavaFX components
import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle; // Allows removing window decorations
import javafx.util.Duration;

/**
 * Responsible for displaying and animating the splash screen.
 */
public class SplashScreenAnimator {

    /**
     * Shows a splash screen window with fade and scale animations.
     *
     * @param splashStage The Stage to display the splash
     * @param onFinished  Runnable to execute after the splash closes
     */
    public static void showSplash(Stage splashStage, Runnable onFinished) {
        // Remove window decorations (title bar, borders)
        splashStage.initStyle(StageStyle.UNDECORATED);

        // Load splash image from resources
        Image logoImage = new Image(
                SplashScreenAnimator.class.getResource("/application/studyspace/landingpage/images/logos/fullLogo.png").toExternalForm()
        );
        ImageView logo = new ImageView(logoImage);

        // Set the size to a square (e.g., 500x500) with scaling
        int SQUARE_SIZE = 500;
        logo.setFitWidth(SQUARE_SIZE * 0.7); // 350px wide
        logo.setFitHeight(SQUARE_SIZE * 0.7);
        logo.setPreserveRatio(true);

        // Root container
        StackPane root = new StackPane(logo);
        root.setStyle("-fx-background-color: white; -fx-border-radius: 16; -fx-background-radius: 16;");

        // Fixed size for the window
        root.setPrefWidth(SQUARE_SIZE);
        root.setPrefHeight(SQUARE_SIZE);
        root.setMaxWidth(SQUARE_SIZE);
        root.setMaxHeight(SQUARE_SIZE);

        // Create and set the Scene
        Scene scene = new Scene(root, SQUARE_SIZE, SQUARE_SIZE);
        splashStage.setScene(scene);
        splashStage.setResizable(false);

        // Center window on screen
        splashStage.centerOnScreen();
        splashStage.show();

        // --- Animations ---

        // Fade in the splash
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.0), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Scale logo in and out repeatedly
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.8), logo);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(6);

        // Pause briefly before fading out
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));

        // Fade out the splash
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Play all animations in sequence
        SequentialTransition sequence = new SequentialTransition(
                fadeIn,
                scale,
                pause,
                fadeOut
        );

        // When animations finish, close splash and run the callback
        sequence.setOnFinished(event -> {
            splashStage.close();
            onFinished.run();
        });

        // Start the animation
        sequence.play();
    }
}
