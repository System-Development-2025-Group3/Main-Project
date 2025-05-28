package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.SceneSwitcher;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class OnboardingPage2Controller implements Initializable {

    @FXML private StackPane calendarContainer;
    @FXML private Label monthYearLabel;
    @FXML private Button handleUploadCSV;

    private LocalDate currentDate = LocalDate.now();
    private final List<CalendarEvent> events = CalendarEvent.getAllEvents();
    private UUID userUUID;

    public void setUserUUID(UUID uuid) {
        this.userUUID = uuid;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renderCalendar();

        // enable drag & drop on the calendarContainer’s parent (the VBox)
        calendarContainer.getParent().setOnDragOver(this::onDragOver);
        calendarContainer.getParent().setOnDragDropped(this::onDragDropped);
    }

    private void renderCalendar() {
        calendarContainer.getChildren().clear();
        calendarContainer.getChildren().add(
                CalendarView.buildMonthView(currentDate, events)
        );
        monthYearLabel.setText(
                currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + currentDate.getYear()
        );
    }

    @FXML private void handleUploadCSV(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select CSV");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File f = chooser.showOpenDialog(calendarContainer.getScene().getWindow());
        if (f != null) {
            System.out.println("Selected: " + f.getAbsolutePath());
            // TODO: parse CSV → events, then renderCalendar()
        }
    }

    @FXML private void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    @FXML private void onDragDropped(DragEvent e) {
        List<File> files = e.getDragboard().getFiles();
        if (!files.isEmpty()) {
            System.out.println("Dropped: " + files.get(0).getAbsolutePath());
            // TODO: parse CSV → events, then renderCalendar()
        }
        e.setDropCompleted(true);
        e.consume();
    }

    @FXML public void goToPrevious(ActionEvent e) {
        currentDate = currentDate.minusMonths(1);
        renderCalendar();
    }

    @FXML public void goToNext(ActionEvent e) {
        currentDate = currentDate.plusMonths(1);
        renderCalendar();
    }

    // ——— Pager handlers ———

    @FXML
    private void handlePage1(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage landingPageStage = (Stage) popupStage.getOwner();
        popupStage.close();

        Platform.runLater(() -> {
            SceneSwitcher.<OnboardingPage1Controller>switchToPopupWithData(
                    landingPageStage,
                    "/application/studyspace/onboarding/StudyPreferencesOnboardingPopUp.fxml",
                    "Onboarding Page 1",
                    controller -> controller.setUserUUID(userUUID)
            );
        });
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        //already on page 2
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        Stage popupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Stage landingPageStage = (Stage) popupStage.getOwner();
        popupStage.close();

        Platform.runLater(() -> {
            SceneSwitcher.<OnboardingPage3Controller>switchToPopupWithData(
                    landingPageStage,
                    "/application/studyspace/onboarding/ExamOnboardingPopUp.fxml",
                    "Exam Setup",
                    controller -> controller.setUserUUID(userUUID)
            );
        });
    }
}
