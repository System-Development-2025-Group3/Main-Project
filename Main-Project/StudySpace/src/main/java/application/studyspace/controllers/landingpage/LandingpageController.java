package application.studyspace.controllers.landingpage;

import application.studyspace.controllers.onboarding.OnboardingPage3Controller;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.*;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;


import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LandingpageController implements Initializable {

    private static final Logger logger = Logger.getLogger(LandingpageController.class.getName());

    @FXML private CalendarView calendarView;
    @FXML private VBox addOverlayPane;
    @FXML private ToggleGroup typeToggleGroup;
    @FXML private ToggleButton examToggle;
    @FXML private ToggleButton blockerToggle;
    @FXML private VBox examForm;
    @FXML private VBox blockerForm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Always load the latest events/calendars for the current user
        refreshCalendarView();

        // Store this CalendarView in the SessionManager for other controllers to update/refresh
        SessionManager.getInstance().setUserCalendar(calendarView);

        // Bind overlay form visibility
        examForm.visibleProperty().bind(examToggle.selectedProperty());
        examForm.managedProperty().bind(examToggle.selectedProperty());
        blockerForm.visibleProperty().bind(blockerToggle.selectedProperty());
        blockerForm.managedProperty().bind(blockerToggle.selectedProperty());
    }

    /**
     * Reloads and re-renders all calendars and events for the current user in the CalendarView.
     * You can call this method anytime (e.g. after new events are added by other controllers).
     */
    public void refreshCalendarView() {
        if (calendarView == null) return;
        try {
            CalendarHelper.setupWeekCalendarAsync(calendarView);
            logger.info("Landing page calendar refreshed.");
        } catch (Exception e) {
            logger.severe("❌ Failed to refresh calendar view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Opens the overlay for adding a new exam or blocker; defaults to exam. */
    @FXML
    public void openAddOverlay() {
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                (OnboardingPage3Controller ctrl) -> {
                    // hide the built-in pagination controls
                    ctrl.page1Btn.setVisible(false);
                    ctrl.page2Btn.setVisible(false);
                    ctrl.page3Btn.setVisible(false);

                    ctrl.closeOverlayButton.setDisable(false);
                    ctrl.closeOverlayButton.setVisible(true);
                    ctrl.closeOverlayButton.setManaged(true);
                    ctrl.closeOverlayButton.toFront();
                }
        );
    }

    /** Closes the add‐new overlay without saving. */
    @FXML
    public void closeAddOverlay() {
        addOverlayPane.setVisible(false);
        addOverlayPane.setManaged(false);
    }

    /**
     * Invoked when the user clicks “Save” in the add‐new overlay.
     * After saving, also refreshes the calendar view to show the new event.
     */
    @FXML
    public void saveNewItem() {
        if (typeToggleGroup.getSelectedToggle() == examToggle) {
            // TODO: wire up saving a new exam (e.g. delegate to OnboardingPage3Controller.saveExam)
            logger.info("Exam creation not yet implemented in LandingpageController.");
        } else {
            // TODO: wire up saving a new blocker/event (e.g. delegate to OnboardingPage3Controller.saveBlocker)
            logger.info("Blocker creation not yet implemented in LandingpageController.");
        }
        refreshCalendarView();
        closeAddOverlay();
    }

    /** Handler for clicking the “Calendar” item in the sidebar. */
    @FXML
    private void handleSidebarCalendar() {
        // No-op, or you could refresh the calendar if you want.
        refreshCalendarView();
    }

    @FXML
    private void handleSidebarDashboard() {
        try {
            ReconciliationHelper.reconcileWeek(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before leaving calendar", e);
            // optionally show the user an alert here
        }
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML
    private void handleSidebarSettings() {
        try {
            ReconciliationHelper.reconcileWeek(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before leaving calendar", e);
        }
        ViewManager.show("/application/studyspace/landingpage/Settings.fxml");
    }

    @FXML
    private void handleExit() {
        try {
            ReconciliationHelper.reconcileWeek(calendarView);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to sync before exit", e);
        }
        Platform.exit();
    }
}
