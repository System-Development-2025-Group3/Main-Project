package application.studyspace;

import application.studyspace.services.auth.RememberMeHelper;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.SplashScreenAnimator;
import application.studyspace.services.Scenes.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URL;
import java.util.UUID;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("🚀 Starting Planify...");
        // LOG: Checking if RememberMe UUID is present
        UUID rememberedUUID = RememberMeHelper.getRememberedUserUUID();
        System.out.println("[MainApp] Remembered UUID = " + rememberedUUID);

        // LOG: Checking if splash screen should be shown
        boolean skipSplash = SessionManager.getInstance().loadSkipSplashScreenPreferenceLocal();
        System.out.println("[MainApp] Skip splash preference (local) = " + skipSplash);

        boolean showSplash = !skipSplash;

        // If user is remembered, log them in
        if (rememberedUUID != null) {
            System.out.println("[MainApp] Logging in user from token: " + rememberedUUID);
            SessionManager.getInstance().login(rememberedUUID);
        }

        // Now decide what to show
        if (showSplash) {
            System.out.println("[MainApp] Showing splash screen");
            Stage splashStage = new Stage();
            SplashScreenAnimator.showSplash(splashStage, () ->
                    Platform.runLater(() -> {
                        System.out.println("[MainApp] Loading root layout after splash");
                        ViewManager.loadRootLayout(primaryStage);
                    })
            );
        } else {
            System.out.println("[MainApp] Skipping splash, loading root layout");
            ViewManager.loadRootLayout(primaryStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}