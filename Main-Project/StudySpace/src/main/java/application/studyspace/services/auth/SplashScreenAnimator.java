package application.studyspace.services.auth;

import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreenAnimator {

    public static void showSplash(Stage splashStage, Runnable onFinished) {
        // Load the splash image from resources
        Image logoImage = new Image(SplashScreenAnimator.class.getResource("/images/auth/LoginPage02.png").toExternalForm());

        // Create the logo image view and scale it
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(400);
        logo.setPreserveRatio(true);

        // Create the root container for the splash scene
        StackPane root = new StackPane(logo);
        root.setStyle("-fx-background-color: white;");

        // Set up the scene and stage
        Scene scene = new Scene(root, 800, 600);
        splashStage.setScene(scene);
        splashStage.setResizable(false);
        splashStage.centerOnScreen();
        splashStage.show();

        // First animation: fade in the whole root (not just the logo)
        // This creates a soft fade from blank white to visible
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.0), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Second animation: scale pulse of the logo
        // This mimics your original animation where the logo "breathes"
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.8), logo);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(6); // This was in your original logic

        // Optional: short pause to give a nice transition gap
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));

        // Final animation: fade out the whole root after the scale animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Sequence of all transitions, in the correct order
        SequentialTransition sequence = new SequentialTransition(
                fadeIn,   // first fade in the splash screen
                scale,    // then play the scale pulse
                pause,    // optional pause for smooth timing
                fadeOut   // then fade out everything
        );

        // When the sequence finishes:
        // - close the splash stage (window)
        // - call onFinished (this will load RootLayout.fxml in MainApp)
        sequence.setOnFinished(event -> {
            splashStage.close();
            onFinished.run();
        });

        // Start the animation
        sequence.play();
    }
}
