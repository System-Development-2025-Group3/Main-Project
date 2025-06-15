package application.studyspace;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/application/studyspace/scenes/RootLayout.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setTitle("Planify");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Failed to load RootLayout.fxml");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
