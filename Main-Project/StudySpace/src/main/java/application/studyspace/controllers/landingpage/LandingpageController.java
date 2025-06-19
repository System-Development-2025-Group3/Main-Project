package application.studyspace.controllers.landingpage;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.CalendarHelper;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;
public class LandingpageController implements Initializable {

    @FXML private CalendarView calendarView;
    @FXML private VBox addOverlayPane;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle;
    @FXML private ToggleButton blockerToggle;
    @FXML private VBox examForm;
    @FXML private VBox blockerForm;

    public void initialize(URL location, ResourceBundle resources) {

        CalendarHelper.setupUserCalendar(calendarView);
        SessionManager.getInstance().setUserCalendar(calendarView);

        examForm.visibleProperty().bind(examToggle.selectedProperty());
        examForm.managedProperty().bind(examToggle.selectedProperty());
        blockerForm.visibleProperty().bind(blockerToggle.selectedProperty());
        blockerForm.managedProperty().bind(blockerToggle.selectedProperty());
    }

    // when opening, ensure exam is selected
    @FXML
    public void openAddOverlay() {
        examToggle.setSelected(true);
        addOverlayPane.setVisible(true);
        addOverlayPane.setManaged(true);
    }
    @FXML public void closeAddOverlay() {
        addOverlayPane.setVisible(false);
        addOverlayPane.setManaged(false);
    }
    @FXML public void saveNewItem() {
        if (typeToggleGroup.getSelectedToggle() == examToggle) {
            // TODO: create exam
        } else {
            // TODO: create blocker/event
        }
        closeAddOverlay();
    }
    @FXML private void handleSidebarCalendar() { }
    @FXML private void handleSidebarDashboard() { ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml"); }
    @FXML private void handleSidebarSettings() { ViewManager.show("/application/studyspace/landingpage/Settings.fxml"); }
    @FXML private void handleExit() { Platform.exit(); }
}
