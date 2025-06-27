package application.studyspace.controllers.customerInteraction;


import application.studyspace.services.customerInteraction.TeamMember;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;


import java.util.ArrayList;
import java.util.List;


public class AboutUsController {


    @FXML
    private StackPane cardCarousel;


    @FXML
    private VBox leftCard, centerCard, rightCard;


    @FXML
    private ImageView imageLeft, imageCenter, imageRight;


    @FXML
    private Label nameLeft, nameCenter, nameRight;
    @FXML
    private Label ageLeft, ageCenter, ageRight;
    @FXML
    private Label professionLeft, professionCenter, professionRight;
    @FXML
    private Label descriptionLeft, descriptionCenter, descriptionRight;


    private final List<TeamMember> teamMembers = new ArrayList<>();
    private int currentIndex = 0;


    @FXML
    public void initialize() {
        cardCarousel.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(
                        getClass().getResource("/application/studyspace/styles/AboutUs.css").toExternalForm()
                );
            }
        });




        // Initialize team members
        initializeTeamMembers();


        // Apply initial cards content
        updateCards();


        // Set up carousel animation
        setupCarouselAnimation();
    }


    private void initializeTeamMembers() {
        teamMembers.add(new TeamMember("Fabian Doering", "Age: 30", "Software Engineer",
                "John is experienced in software development.", "/images/customerInteraction/Team-Member-1.png"));
        teamMembers.add(new TeamMember("Jane Smith", "Age: 28", "Designer",
                "Jane creates intuitive designs.", "/images/customerInteraction/Team-Member-2.png"));
        teamMembers.add(new TeamMember("Emily Brown", "Age: 35", "Product Manager",
                "Emily manages product timelines effectively.", "/images/customerInteraction/Team-Member-3.png"));
        teamMembers.add(new TeamMember("Michael Green", "Age: 32", "Backend Developer",
                "Michael specializes in backend systems.", "/images/customerInteraction/Team-Member-4.png"));
        teamMembers.add(new TeamMember("Sarah White", "Age: 27", "Marketing Specialist",
                "Sarah organizes marketing campaigns.", "/images/customerInteraction/Team-Member-5.png"));
    }


    private void setupCarouselAnimation() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            rotateCards();
            updateCards();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void rotateCards() {
        currentIndex = (currentIndex + 1) % teamMembers.size();
    }


    private void updateCards() {
        // Determine indices for left, center, and right cards
        int leftIndex = (currentIndex - 1 + teamMembers.size()) % teamMembers.size();
        int rightIndex = (currentIndex + 1) % teamMembers.size();


        // Update left card
        updateCardContent(leftCard, teamMembers.get(leftIndex), imageLeft, nameLeft, ageLeft, professionLeft, descriptionLeft);


        // Update center card
        updateCardContent(centerCard, teamMembers.get(currentIndex), imageCenter, nameCenter, ageCenter, professionCenter, descriptionCenter);


        // Update right card
        updateCardContent(rightCard, teamMembers.get(rightIndex), imageRight, nameRight, ageRight, professionRight, descriptionRight);
    }


    private void updateCardContent(VBox card, TeamMember member, ImageView imageView, Label name, Label age, Label profession, Label description) {
        imageView.setImage(new Image(getClass().getResource(member.getImagePath()).toExternalForm()));
        name.setText(member.getName());
        age.setText(member.getAge());
        profession.setText(member.getProfession());
        description.setText(member.getDescription());
    }
}
