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
        splashStage.initStyle(StageStyle.UNDECORATED);

        // Load splash image
        Image logoImage = new Image(
                SplashScreenAnimator.class.getResource("/images/auth/LoginPage02.png").toExternalForm()
        );
        ImageView logo = new ImageView(logoImage);

        // Set the size for a perfect square
        int SQUARE_SIZE = 500;
        logo.setFitWidth(SQUARE_SIZE * 0.7); // or any value that looks good, e.g., 350px for 500x500 window
        logo.setFitHeight(SQUARE_SIZE * 0.7);
        logo.setPreserveRatio(true);

        StackPane root = new StackPane(logo);
        root.setStyle("-fx-background-color: white; -fx-border-radius: 16; -fx-background-radius: 16;"); // Rounded corners optional

        // Make sure the StackPane is fixed size
        root.setPrefWidth(SQUARE_SIZE);
        root.setPrefHeight(SQUARE_SIZE);
        root.setMaxWidth(SQUARE_SIZE);
        root.setMaxHeight(SQUARE_SIZE);

        Scene scene = new Scene(root, SQUARE_SIZE, SQUARE_SIZE);

        splashStage.setScene(scene);
        splashStage.setResizable(false);

        // Center the window
        splashStage.centerOnScreen();

        splashStage.show();

        // --- Animations (unchanged) ---
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