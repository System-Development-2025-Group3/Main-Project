package application.studyspace.services;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.util.HashMap;
import java.util.Map;

public class SurveyPopup {

    // This stores previous answers for each user
    private static final Map<String, Map<String, String>> savedAnswers = new HashMap<>();

    // This opens the survey window
    public static void showSurvey(String username) {
        // Create a new window (popup)
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Short Survey");

        // Questions
        Label question1 = new Label("How satisfied are you with the app?");
        TextField answer1 = new TextField();

        Label question2 = new Label("What feature would you like to see?");
        TextField answer2 = new TextField();

        // Load previous answers if available
        if (savedAnswers.containsKey(username)) {
            Map<String, String> previous = savedAnswers.get(username);
            answer1.setText(previous.getOrDefault("q1", ""));
            answer2.setText(previous.getOrDefault("q2", ""));
        }

        // Submit button
        Button submit = new Button("Submit");
        submit.setOnAction(e -> {
            // Save answers
            Map<String, String> answers = new HashMap<>();
            answers.put("q1", answer1.getText());
            answers.put("q2", answer2.getText());

            savedAnswers.put(username, answers);
            System.out.println("Answers saved for user: " + username);

            window.close(); // Close the popup
        });

        // Layout the popup
        VBox layout = new VBox(10); // 10px spacing
        layout.getChildren().addAll(question1, answer1, question2, answer2, submit);
        layout.setPadding(new javafx.geometry.Insets(20));

        Scene scene = new Scene(layout, 400, 250);
        window.setScene(scene);
        window.showAndWait(); // Wait until closed
    }
}
// seite laden---> SceneSwitcher.switchTo(myButton, "/application/studyspace/LandingPage.fxml", "Welcome");