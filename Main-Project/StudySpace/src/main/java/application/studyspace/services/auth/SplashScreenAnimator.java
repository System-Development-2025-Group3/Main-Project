package application.studyspace.services.auth;

import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;             // ← import this
import javafx.util.Duration;

public class SplashScreenAnimator {

    public static void showSplash(Stage splashStage, Runnable onFinished) {
        // remove OS title‐bar / border
        splashStage.initStyle(StageStyle.UNDECORATED);
        // for full transparency (no window background at all) use:
        //   splashStage.initStyle(StageStyle.TRANSPARENT);

        // Load the splash image from resources
        Image logoImage = new Image(
                SplashScreenAnimator.class
                        .getResource("/images/auth/LoginPage02.png")
                        .toExternalForm()
        );

        // Create the logo image view and scale it
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(400);
        logo.setPreserveRatio(true);

        // Create the root container for the splash scene
        StackPane root = new StackPane(logo);
        root.setStyle("-fx-background-color: white;");
        // if you went TRANSPARENT above, give your root a background:
        // root.setStyle("-fx-background-color: white;");

        // Set up the scene and stage
        Scene scene = new Scene(root, 800, 600);
        // if you went TRANSPARENT above, also:
        // scene.setFill(Color.TRANSPARENT);

        splashStage.setScene(scene);
        splashStage.setResizable(false);
        splashStage.centerOnScreen();
        splashStage.show();

        // --- your existing animation code below ---

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.0), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.8), logo);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(6);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequence = new SequentialTransition(
                fadeIn,
                scale,
                pause,
                fadeOut
        );

        sequence.setOnFinished(event -> {
            splashStage.close();
            onFinished.run();
        });

        sequence.play();
    }
}
