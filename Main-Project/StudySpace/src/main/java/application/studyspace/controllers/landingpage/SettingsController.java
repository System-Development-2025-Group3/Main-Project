package application.studyspace.controllers.landingpage;

// Import dependencies
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.PasswordHasher;
import application.studyspace.services.auth.RememberMeHelper;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;
import application.studyspace.services.Styling.CreateToolTip;
import application.studyspace.services.Styling.StylingUtility;
import application.studyspace.services.auth.ValidationUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for the Settings screen.
 * Allows updating study preferences, password, and account options.
 */
public class SettingsController {

    // FXML bindings to password fields and tooltips
    @FXML private PasswordField NewPasswordField;
    @FXML private PasswordField ConfirmPasswordField;
    @FXML private Label passwordTooltip1, passwordTooltip2;

    // Session and break sliders
    @FXML private Slider sessionLengthSlider;
    @FXML private Label sessionLengthLabel;
    @FXML private Slider breakLengthSlider;
    @FXML private Label breakLengthLabel;

    // Time spinners
    @FXML private Spinner<LocalTime> startTimeSpinner;
    @FXML private Spinner<LocalTime> endTimeSpinner;

    // Preferences checkboxes
    @FXML private CheckBox skipSplashCheckbox;
    @FXML private CheckBox rememberMeCheckbox;

    // Toggle buttons for blocked days
    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    // Tooltip helper
    private final CreateToolTip toolTipService = new CreateToolTip();

    // Factories for spinner value management
    private SpinnerValueFactory<LocalTime> startFactory;
    private SpinnerValueFactory<LocalTime> endFactory;

    /**
     * Called after FXML load.
     * Sets up controls, tooltips, and loads saved settings.
     */
    @FXML
    private void initialize() {
        // Password tooltip text
        String pwTip = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip1, pwTip, "tooltip-Label");
        toolTipService.createCustomTooltip(passwordTooltip2, pwTip, "tooltip-Label");

        // Populate time spinners with 30-minute intervals
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime t = LocalTime.of(6, 0);
        while (!t.isAfter(LocalTime.of(22, 0))) {
            times.add(t);
            t = t.plusMinutes(30);
        }

        // Formatter for displaying time
        StringConverter<LocalTime> timeFmt = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override public String toString(LocalTime lt) { return lt == null ? "" : fmt.format(lt);}
            @Override public LocalTime fromString(String str) { return (str == null || str.isEmpty()) ? null : LocalTime.parse(str, fmt);}
        };

        // Start time spinner setup
        startFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        startFactory.setConverter(timeFmt);
        startFactory.setValue(LocalTime.of(9, 0));
        startTimeSpinner.setValueFactory(startFactory);
        startTimeSpinner.setEditable(true);

        // End time spinner setup
        endFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        endFactory.setConverter(timeFmt);
        endFactory.setValue(LocalTime.of(17, 0));
        endTimeSpinner.setValueFactory(endFactory);
        endTimeSpinner.setEditable(true);

        // Session length slider listener
        sessionLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            sessionLengthSlider.setValue(rounded);
            sessionLengthLabel.setText(rounded + " min");
        });

        // Break length slider listener
        breakLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            breakLengthSlider.setValue(rounded);
            breakLengthLabel.setText(rounded + " min");
        });

        // Load saved settings
        reloadSavedSettings();
    }

    /**
     * Loads all settings from DB and updates the UI.
     */
    private void reloadSavedSettings() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        if (userUUID == null) {
            // No user logged in; reset to defaults
            skipSplashCheckbox.setSelected(false);
            rememberMeCheckbox.setSelected(false);
            startFactory.setValue(LocalTime.of(8, 0));
            endFactory.setValue(LocalTime.of(18, 0));
            sessionLengthSlider.setValue(60);
            sessionLengthLabel.setText("60 min");
            breakLengthSlider.setValue(10);
            breakLengthLabel.setText("10 min");
            monBtn.setSelected(false); tueBtn.setSelected(false); wedBtn.setSelected(false);
            thuBtn.setSelected(false); friBtn.setSelected(false); satBtn.setSelected(false); sunBtn.setSelected(false);
            RememberMeHelper.clearRememberedUserUUID();
            System.out.println("[Settings] No user logged in; settings reset to defaults.");
            return;
        }

        try {
            // Load skip splash and remember me flags
            boolean skipSplash = StudyPreferences.loadSkipSplashScreen(userUUID);
            boolean rememberMe = StudyPreferences.loadRememberMe(userUUID);
            skipSplashCheckbox.setSelected(skipSplash);
            rememberMeCheckbox.setSelected(rememberMe);

            // Update remembered UUID locally if needed
            if (rememberMe) {
                RememberMeHelper.saveRememberedUserUUID(userUUID);
            } else {
                RememberMeHelper.clearRememberedUserUUID();
            }

            // Load other preferences
            StudyPreferences prefs = StudyPreferences.load(userUUID);

            // Start and end times
            LocalTime start = prefs.getStartTime();
            LocalTime end = prefs.getEndTime();
            startFactory.setValue(start != null ? start : LocalTime.of(8, 0));
            endFactory.setValue(end != null ? end : LocalTime.of(18, 0));

            // Session length
            int sessionLen = prefs.getSessionLength();
            sessionLengthSlider.setValue(sessionLen > 0 ? sessionLen : 60);
            sessionLengthLabel.setText((sessionLen > 0 ? sessionLen : 60) + " min");

            // Break length
            int breakLen = prefs.getBreakLength();
            breakLengthSlider.setValue(breakLen > 0 ? breakLen : 10);
            breakLengthLabel.setText((breakLen > 0 ? breakLen : 10) + " min");

            // Blocked days
            Set<DayOfWeek> blocked = prefs.getBlockedDays();
            monBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.MONDAY));
            tueBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.TUESDAY));
            wedBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.WEDNESDAY));
            thuBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.THURSDAY));
            friBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.FRIDAY));
            satBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.SATURDAY));
            sunBtn.setSelected(blocked != null && blocked.contains(DayOfWeek.SUNDAY));

            System.out.printf("[Settings] Loaded for %s: skipSplash=%b, rememberMe=%b, start=%s, end=%s, session=%d, break=%d, blocked=%s%n",
                    userUUID, skipSplash, rememberMe, start, end, sessionLen, breakLen, blocked);

        } catch (SQLException ex) {
            // Defaults if load fails
            System.out.println("[Settings] No existing prefs, defaults applied.");
            startFactory.setValue(LocalTime.of(8, 0));
            endFactory.setValue(LocalTime.of(18, 0));
            sessionLengthSlider.setValue(60);
            sessionLengthLabel.setText("60 min");
            breakLengthSlider.setValue(10);
            breakLengthLabel.setText("10 min");
        }
    }

    /**
     * Handler for skip splash checkbox changes.
     */
    @FXML
    private void handleSkipSplashChanged() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        boolean newValue = skipSplashCheckbox.isSelected();
        SessionManager.getInstance().saveSkipSplashScreenPreferenceLocal(newValue);

        boolean success = false;
        if (userUUID != null) {
            success = StudyPreferences.updateSkipSplashScreen(userUUID, newValue);
            System.out.printf("[Settings] Save skipSplash=%b to DB: %s%n", newValue, success ? "OK" : "FAILED");
        }
        boolean latestValue = (userUUID != null) ? StudyPreferences.loadSkipSplashScreen(userUUID) : newValue;
        skipSplashCheckbox.setSelected(latestValue);
        System.out.printf("[Settings] skipSplashCheckbox now set to: %b%n", latestValue);
    }

    /**
     * Handler for remember me checkbox changes.
     */
    @FXML
    private void handleRememberMeChanged() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        boolean newValue = rememberMeCheckbox.isSelected();
        if (newValue) {
            RememberMeHelper.saveRememberedUserUUID(userUUID);
        } else {
            RememberMeHelper.clearRememberedUserUUID();
        }

        boolean dbSuccess = false;
        if (userUUID != null) {
            dbSuccess = StudyPreferences.updateRememberMe(userUUID, newValue);
            System.out.printf("[Settings] Save rememberMe=%b to DB: %s%n", newValue, dbSuccess ? "OK" : "FAILED");
        }
        boolean latestValue = (userUUID != null) ? StudyPreferences.loadRememberMe(userUUID) : newValue;
        rememberMeCheckbox.setSelected(latestValue);
        System.out.printf("[Settings] rememberMeCheckbox now set to: %b%n", latestValue);
    }

    /**
     * Handles the password reset process.
     */
    @FXML
    private void handleResetPassword(ActionEvent event) throws SQLException {
        String newPassword = NewPasswordField.getText();
        String confirmPassword = ConfirmPasswordField.getText();
        int secs = 5;
        ValidationUtils.ValidationResult result = ValidationUtils.validatePasswordUpdate(newPassword, confirmPassword);
        switch (result) {
            case EMPTY_PASSWORD -> StylingUtility.showError(NewPasswordField, toolTipService, passwordTooltip1,
                    "Please enter a new password.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);
            case PASSWORD_INVALID -> StylingUtility.showError(NewPasswordField, toolTipService, passwordTooltip1,
                    "Password is not strong enough! \n Ensure it has at least ...",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);
            case PASSWORD_MISMATCH -> StylingUtility.showError(ConfirmPasswordField, toolTipService, passwordTooltip2,
                    "Passwords do not match.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);
            case OK -> {
                UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
                String email = application.studyspace.services.DataBase.DatabaseHelper.getEmailByUUID(userUUID);
                if (email != null && !email.isEmpty()) {
                    boolean success = PasswordHasher.updatePassword(email, newPassword);
                    if (success) {
                        StylingUtility.showSuccess(NewPasswordField,
                                "Password has been updated successfully!",
                                "text-field-success", "text-field");
                        NewPasswordField.clear();
                        ConfirmPasswordField.clear();
                    } else {
                        StylingUtility.showError(NewPasswordField, toolTipService, passwordTooltip1,
                                "Failed to update password. Please try again.",
                                "tooltip-Label-Error", "password-field-error", "password-field", secs);
                    }
                } else {
                    StylingUtility.showError(NewPasswordField, toolTipService, passwordTooltip1,
                            "Could not find user email. Cannot update password.",
                            "tooltip-Label-Error", "password-field-error", "password-field", secs);
                }
            }
            default -> {}
        }
    }

    /**
     * Saves all settings to the database.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        StringBuilder blockedDaysBuilder = new StringBuilder();
        if (monBtn.isSelected()) blockedDaysBuilder.append("Monday ");
        if (tueBtn.isSelected()) blockedDaysBuilder.append("Tuesday ");
        if (wedBtn.isSelected()) blockedDaysBuilder.append("Wednesday ");
        if (thuBtn.isSelected()) blockedDaysBuilder.append("Thursday ");
        if (friBtn.isSelected()) blockedDaysBuilder.append("Friday ");
        if (satBtn.isSelected()) blockedDaysBuilder.append("Saturday ");
        if (sunBtn.isSelected()) blockedDaysBuilder.append("Sunday ");
        String blockedDays = blockedDaysBuilder.toString().trim();

        LocalTime start = startTimeSpinner.getValue();
        LocalTime end = endTimeSpinner.getValue();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        String preferredRange = start.format(fmt) + "-" + end.format(fmt);

        StudyPreferences prefs = new StudyPreferences(
                userUUID,
                preferredRange,
                (int) sessionLengthSlider.getValue(),
                (int) breakLengthSlider.getValue(),
                blockedDays
        );
        boolean success = prefs.saveToDatabase();

        if (success) {
            System.out.println("Study preferences saved.");
            reloadSavedSettings();
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }

    // Sidebar navigation handlers
    @FXML
    private void handleSidebarCalendar() {
        ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
    }

    @FXML
    private void handleSidebarDashboard() {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML
    private void handleSidebarSettings() {
        // Already on Settings
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
