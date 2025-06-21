package application.studyspace.controllers.landingpage;

import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.Styling.StylingUtility;
import application.studyspace.services.onboarding.InputStudyPreferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static application.studyspace.services.auth.PasswordHasher.saveToDatabase;

public class SettingsController {
    
    @FXML private PasswordField NewPasswordField ;
    @FXML private PasswordField ConfirmPasswordField ;
    @FXML private Label passwordTooltip;
    @FXML private Slider sessionLengthSlider;
    @FXML private Slider breakLengthSlider;
    @FXML private Spinner<LocalTime> startTimeSpinner;
    @FXML private Spinner<LocalTime> endTimeSpinner;

    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void handleResetPassword(ActionEvent event) throws SQLException {
        String newPassword = NewPasswordField.getText();
        String confirmPassword = ConfirmPasswordField.getText();

        int tooltipDuration = 5; // Duration in seconds
        String errorStyle = "password-field-error";

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            StylingUtility.showError(
                    NewPasswordField, toolTipService, passwordTooltip,
                    "Password fields cannot be empty.",
                    "tooltip-Label-Error", errorStyle, "password-field", tooltipDuration);
            StylingUtility.applyErrorStyle(ConfirmPasswordField, errorStyle); // Apply error style to both fields

        } else if (!newPassword.equals(confirmPassword)) {
            StylingUtility.showError(
                    ConfirmPasswordField, toolTipService, passwordTooltip,
                    "Passwords do not match! Please try again.",
                    "tooltip-Label-Error", errorStyle, "password-field", tooltipDuration);

        } else if (!ValidationUtils.isStrongPassword(newPassword)) {
            StylingUtility.showError(
                    NewPasswordField, toolTipService, passwordTooltip,
                    "Password is not strong enough! Must be ≥12 chars, include uppercase, number, and special.",
                    "tooltip-Label-Error", errorStyle, "password-field", tooltipDuration);

        } else {
            UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
            saveToDatabase(DatabaseHelper.getEmailByUUID(userUUID), NewPasswordField.getText());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {

        // get user session
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();

        // get blocked days
        String blockedDays = "";
        if (monBtn.isSelected()) blockedDays += "Monday ";
        if (tueBtn.isSelected()) blockedDays += "Tuesday ";
        if (wedBtn.isSelected()) blockedDays += "Wednesday ";
        if (thuBtn.isSelected()) blockedDays += "Thursday ";
        if (friBtn.isSelected()) blockedDays += "Friday ";
        if (satBtn.isSelected()) blockedDays += "Saturday ";
        if (sunBtn.isSelected()) blockedDays += "Sunday ";
        blockedDays = blockedDays.trim();

        //get allowed study time range
        LocalTime start = startTimeSpinner.getValue();
        LocalTime end   = endTimeSpinner.getValue();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        String preferredRange = start.format(fmt) + "-" + end.format(fmt);

        InputStudyPreferences prefs = new InputStudyPreferences(
                userUUID,
                preferredRange,
                (int) sessionLengthSlider.getValue(),
                (int) breakLengthSlider.getValue(),
                blockedDays
        );
        boolean success = prefs.saveToDatabase();

        if (success) {
            System.out.println("Study preferences saved.");
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }

    @FXML
    private void handleSidebarCalendar(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleSidebarDashboard(ActionEvent event) {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML
    private void handleSidebarSettings(ActionEvent event) {
        // Already on settings; no action needed
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}