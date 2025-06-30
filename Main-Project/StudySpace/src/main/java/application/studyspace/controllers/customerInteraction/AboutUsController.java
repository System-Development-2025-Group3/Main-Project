package application.studyspace.controllers.customerInteraction;

// Imports for navigation, animation, UI controls
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.customerInteraction.TeamMember;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the About Us page.
 * Displays an animated carousel of team member profiles.
 */
public class AboutUsController {

    // FXML bindings for UI components
    @FXML private StackPane cardCarousel;           // Container for the carousel
    @FXML private Button exitButton;                // Exit button

    // VBoxes representing each visible card
    @FXML private VBox leftCard, centerCard, rightCard;

    // ImageViews for profile photos
    @FXML private ImageView imageLeft, imageCenter, imageRight;

    // Labels for team member info
    @FXML private Label nameLeft, nameCenter, nameRight;
    @FXML private Label ageLeft, ageCenter, ageRight;
    @FXML private Label professionLeft, professionCenter, professionRight;
    @FXML private Label descriptionLeft, descriptionCenter, descriptionRight;

    // List of all team members
    private final List<TeamMember> teamMembers = new ArrayList<>();

    // Index of the current center card
    private int currentIndex = 0;

    /**
     * Called automatically by JavaFX after FXML loading.
     * Sets up styling, loads team data, updates UI, and starts carousel animation.
     */
    @FXML
    public void initialize() {
        // Dynamically add CSS stylesheet once scene is available
        cardCarousel.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(
                        getClass().getResource("/application/studyspace/styles/AboutUs.css").toExternalForm()
                );
            }
        });

        // Populate team member list
        initializeTeamMembers();

        // Display initial card content
        updateCards();

        // Start automatic carousel rotation
        setupCarouselAnimation();
    }

    /**
     * Populates the list of team members shown in the carousel.
     */
    private void initializeTeamMembers() {
        teamMembers.add(new TeamMember(
                "Fabian Doering",
                "Age: 21",
                "Product Owner and Business Informatics Specialist",
                "Fabian was the product owner of the team and currently works within Deutsche Bank's Digital Client Solutions Team in the Coverage Squad. He is also the main representative of the liberal party in his district.",
                "/images/customerInteraction/Team-Member-1.png"
        ));
        teamMembers.add(new TeamMember(
                "Josef Neumann",
                "Age: 20",
                "Designer",
                "Josef was the Scrum Master of the team and works as a dual student within the Chief Product Office (CPO) of Deutsche Bank.",
                "/images/customerInteraction/Team-Member-2.png"
        ));
        teamMembers.add(new TeamMember(
                "Finn Krieger",
                "Age: 21",
                "Product Manager",
                "Finn was one of the main developers and currently works within Deutsche Bank's Digital Client Solutions Team in the Product Squad. He is also a consultant for FS Student Consulting.",
                "/images/customerInteraction/Team-Member-3.png"
        ));
        teamMembers.add(new TeamMember(
                "Cedric Unger",
                "Age: 23",
                "Backend Developer",
                "Cedric was the main developer of the project and currently works as a dual student for Flatex in the IT Department.",
                "/images/customerInteraction/Team-Member-4.png"
        ));
        teamMembers.add(new TeamMember(
                "Alexander Hahn",
                "Age: 23",
                "Marketing Specialist",
                "Alex is also a developer and works within Deutsche Bank in the Private Bank.",
                "/images/customerInteraction/Team-Member-5.png"
        ));
    }

    /**
     * Configures the automatic carousel animation that rotates cards every 3 seconds.
     */
    private void setupCarouselAnimation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            rotateCards(); // Advance index
            updateCards(); // Refresh UI
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat forever
        timeline.play(); // Start animation
    }

    /**
     * Advances the current index to the next team member.
     */
    private void rotateCards() {
        currentIndex = (currentIndex + 1) % teamMembers.size();
    }

    /**
     * Updates all three visible cards with the correct team member data.
     */
    private void updateCards() {
        // Calculate indices for left, center, and right
        int leftIndex = (currentIndex - 1 + teamMembers.size()) % teamMembers.size();
        int rightIndex = (currentIndex + 1) % teamMembers.size();

        // Update left card content
        updateCardContent(leftCard, teamMembers.get(leftIndex),
                imageLeft, nameLeft, ageLeft, professionLeft, descriptionLeft);

        // Update center card content
        updateCardContent(centerCard, teamMembers.get(currentIndex),
                imageCenter, nameCenter, ageCenter, professionCenter, descriptionCenter);

        // Update right card content
        updateCardContent(rightCard, teamMembers.get(rightIndex),
                imageRight, nameRight, ageRight, professionRight, descriptionRight);
    }

    /**
     * Populates a single card's visual elements with a TeamMember's data.
     */
    private void updateCardContent(
            VBox card,
            TeamMember member,
            ImageView imageView,
            Label name,
            Label age,
            Label profession,
            Label description
    ) {
        // Load and set image
        imageView.setImage(new Image(getClass().getResource(member.getImagePath()).toExternalForm()));

        // Set text fields
        name.setText(member.getName());
        age.setText(member.getAge());
        profession.setText(member.getProfession());
        description.setText(member.getDescription());
    }

    /**
     * Called when the exit button is clicked.
     * Navigates back to the login screen.
     */
    @FXML
    private void handleExit() {
        System.out.println("[AboutUsController] handleExit() called");
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }
}
