package application.studyspace.controllers.customerInteraction;


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


public class AboutUsController {


    @FXML
    private StackPane cardCarousel;
    @FXML
    private Button exitButton;

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
        teamMembers.add(new TeamMember("Fabian Doering", "Age: 21", "Product Owener and Business Informatics Specialist",
                "Fabian was the product owner of the Team and currently works withing Deutsche Banks Digital Client Solutions Team in the Coverage Squad. Furthermore he is the main representative of the liberal party in his district.", "/images/customerInteraction/Team-Member-1.png"));
        teamMembers.add(new TeamMember("Josef Neumann", "Age: 20", "Designer",
                "Josef was the Scrum Master of the Team and works as a dual student withing the Chief Product Office (CPO) within Deutsche Bank.", "/images/customerInteraction/Team-Member-2.png"));
        teamMembers.add(new TeamMember("Finn Krieger", "Age: 21", "Product Manager",
                "Finn was one of the main Developers of the Team and currently works withing Deutsche Banks Digital Client Solutions Team in the Product Squad. Furthermore he is a consultant for FS Student Consulting.", "/images/customerInteraction/Team-Member-3.png"));
        teamMembers.add(new TeamMember("Cedric Unger", "Age: 20", "Backend Developer",
                "Cedric was the main Developer of our project and currently works as a dual Student for Flatex in the IT Department.", "/images/customerInteraction/Team-Member-4.png"));
        teamMembers.add(new TeamMember("Alexander Hahn", "Age: 23", "Marketing Specialist",
                "Alex is also a Developer of our Team and works within Deutsche Bank in the Private Bank.", "/images/customerInteraction/Team-Member-5.png"));
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

    @FXML
    private void handleExit(){
        System.out.println("[AboutUsController] handleExit() called");
        ViewManager.show("/application/studyspace/auth/Login.fxml");
    }
}
