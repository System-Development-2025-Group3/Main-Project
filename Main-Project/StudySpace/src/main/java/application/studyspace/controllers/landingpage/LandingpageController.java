package application.studyspace.controllers.landingpage;

// Import dependencies
import application.studyspace.controllers.onboarding.OnboardingPage3Controller;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.CalendarHelper;
import application.studyspace.services.calendar.ReconciliationHelper;

import com.calendarfx.model.Calendar;
import com.calendarfx.view.CalendarView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main Landing Page view.
 * Shows the calendar, allows adding new events, and handles navigation.
 */
public class LandingpageController implements Initializable {

    private static final Logger logger = Logger.getLogger(LandingpageController.class.getName());

    // Holds the loaded calendars by UUID
    private final Map<UUID, Calendar> calendarMap = new HashMap<>();

    // FXML bindings to UI components
    @FXML private CalendarView calendarView;
    @FXML private VBox addOverlayPane;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle;
    @FXML private ToggleButton blockerToggle;
    @FXML private VBox examForm;
    @FXML private VBox blockerForm;

    /**
     * Called automatically after FXML is loaded.
     * Sets up the calendar, bindings, and stores the CalendarView.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Load all calendars/events
        refreshCalendarView();

        // Store the CalendarView so other controllers can access it
        SessionManager.getInstance().setUserCalendar(calendarView);

        // Bind visibility of the forms to toggle buttons
        examForm.visibleProperty().bind(examToggle.selectedProperty());
        examForm.managedProperty().bind(examToggle.selectedProperty());
        blockerForm.visibleProperty().bind(blockerToggle.selectedProperty());
        blockerForm.managedProperty().bind(blockerToggle.selectedProperty());
    }

    /**
     * Updates the internal map of calendars loaded in the CalendarView.
     */
    public void setCalendarMap(Map<UUID, Calendar> loadedMap) {
        this.calendarMap.clear();
        if (loadedMap != null) {
            this.calendarMap.putAll(loadedMap);
        }
    }

    /**
     * Reloads and redraws the calendar view for the current user.
     */
    public void refreshCalendarView() {
        if (calendarView == null) return;

        // Load calendars asynchronously so UI doesn't freeze
        CalendarHelper.updateUserCalendarAsync(calendarView, this::setCalendarMap);
        logger.info("Landing page calendar refreshed.");
    }

    /**
     * Opens the overlay to add a new exam or blocker.
     * By default, opens the onboarding page as the overlay content.
     */
    @FXML
    public void openAddOverlay() {
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                (OnboardingPage3Controller ctrl) -> {
                    // Hide onboarding navigation buttons
                    ctrl.page1Btn.setVisible(false);
                    ctrl.page2Btn.setVisible(false);
                    ctrl.page3Btn.setVisible(false);

                    // Show close button
                    ctrl.closeOverlayButton.setDisable(false);
                    ctrl.closeOverlayButton.setVisible(true);
                    ctrl.closeOverlayButton.setManaged(true);
                    ctrl.closeOverlayButton.toFront();
                }
        );
    }

    /**
     * Closes the add-new overlay without saving.
     */
    @FXML
    public void closeAddOverlay() {
        addOverlayPane.setVisible(false);
        addOverlayPane.setManaged(false);
    }

    /**
     * Called when user clicks Save in the overlay.
     * Currently logs that creation is not implemented.
     * Refreshes calendar afterward.
     */
    @FXML
    public void saveNewItem() {
        if (typeToggleGroup.getSelectedToggle() == examToggle) {
            logger.info("Exam creation not yet implemented in LandingpageController.");
        } else {
            logger.info("Blocker creation not yet implemented in LandingpageController.");
        }
        refreshCalendarView();
        closeAddOverlay();
    }

    /**
     * Sidebar button: Calendar.
     * Refreshes the calendar view.
     */
    @FXML
    private void handleSidebarCalendar() {
        refreshCalendarView();
    }

    /**
     * Sidebar button: Dashboard.
     * Reconciles unsaved changes, then navigates to Dashboard.
     */
    @FXML
    private void handleSidebarDashboard() {
        try {
            ReconciliationHelper.reconcile(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before leaving calendar", e);
        }
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    /**
     * Sidebar button: Settings.
     * Reconciles unsaved changes, then navigates to Settings.
     */
    @FXML
    private void handleSidebarSettings() {
        try {
            ReconciliationHelper.reconcile(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before leaving calendar", e);
        }
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }

    /**
     * Closes the app after syncing unsaved changes.
     */
    @FXML
    private void handleExit() {
        try {
            ReconciliationHelper.reconcile(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before exit", e);
        }
        Platform.exit();
    }
}
