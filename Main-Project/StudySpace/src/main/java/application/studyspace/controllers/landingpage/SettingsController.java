package application.studyspace.controllers.landingpage;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SettingsController {

    @FXML private PasswordField NewPasswordField;
    @FXML private PasswordField ConfirmPasswordField;
    @FXML private Label passwordTooltip1, passwordTooltip2;
    @FXML private Slider sessionLengthSlider;
    @FXML private Label sessionLengthLabel;
    @FXML private Slider breakLengthSlider;
    @FXML private Label breakLengthLabel;
    @FXML private Spinner<LocalTime> startTimeSpinner;
    @FXML private Spinner<LocalTime> endTimeSpinner;
    @FXML private CheckBox skipSplashCheckbox;
    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;
    @FXML private CheckBox rememberMeCheckbox;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void initialize() {
        // Tooltips for password
        String pwTip = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip1, pwTip, "tooltip-Label");
        toolTipService.createCustomTooltip(passwordTooltip2, pwTip, "tooltip-Label");

        // Configure Time Spinners
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime t = LocalTime.of(6, 0);
        while (!t.isAfter(LocalTime.of(22, 0))) {
            times.add(t);
            t = t.plusMinutes(30);
        }
        StringConverter<LocalTime> timeFmt = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override public String toString(LocalTime lt) { return lt == null ? "" : fmt.format(lt);}
            @Override public LocalTime fromString(String str) { return (str == null || str.isEmpty()) ? null : LocalTime.parse(str, fmt);}
        };
        SpinnerValueFactory<LocalTime> startFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        startFactory.setConverter(timeFmt);
        startFactory.setValue(LocalTime.of(9, 0));
        startTimeSpinner.setValueFactory(startFactory);
        startTimeSpinner.setEditable(true);

        SpinnerValueFactory<LocalTime> endFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        endFactory.setConverter(timeFmt);
        endFactory.setValue(LocalTime.of(17, 0));
        endTimeSpinner.setValueFactory(endFactory);
        endTimeSpinner.setEditable(true);

        // Sliders
        sessionLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            sessionLengthSlider.setValue(rounded);
            sessionLengthLabel.setText(rounded + " min");
        });
        breakLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) (Math.round(newVal.doubleValue() / 5) * 5);
            breakLengthSlider.setValue(rounded);
            breakLengthLabel.setText(rounded + " min");
        });

        // Load checkboxes from DB
        reloadPreferenceCheckboxes();
    }

    /** Loads checkbox values from the database and updates the UI/local tokens. */
    private void reloadPreferenceCheckboxes() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        if (userUUID == null) {
            skipSplashCheckbox.setSelected(false);
            rememberMeCheckbox.setSelected(false);
            System.out.println("[Settings] No user logged in; checkboxes reset.");
            return;
        }
        boolean skipSplash = StudyPreferences.loadSkipSplashScreen(userUUID);
        boolean rememberMe = StudyPreferences.loadRememberMe(userUUID);

        skipSplashCheckbox.setSelected(skipSplash);
        rememberMeCheckbox.setSelected(rememberMe);

        // Sync local token with DB
        if (rememberMe) {
            RememberMeHelper.saveRememberedUserUUID(userUUID);
        } else {
            RememberMeHelper.clearRememberedUserUUID();
        }
        System.out.printf("[Settings] Loaded from DB: skipSplash=%b, rememberMe=%b%n", skipSplash, rememberMe);
    }

    /** Updates skip splash in DB, then reloads from DB and updates checkbox. */
    @FXML
    private void handleSkipSplashChanged() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        boolean newValue = skipSplashCheckbox.isSelected();

        // Local always
        SessionManager.getInstance().saveSkipSplashScreenPreferenceLocal(newValue);

        boolean success = false;
        if (userUUID != null) {
            success = StudyPreferences.updateSkipSplashScreen(userUUID, newValue);
            System.out.printf("[Settings] Save skipSplash=%b to DB: %s%n", newValue, success ? "OK" : "FAILED");
        }

        // Always reload after save, to catch DB/logic desync
        boolean latestValue = (userUUID != null) ? StudyPreferences.loadSkipSplashScreen(userUUID) : newValue;
        skipSplashCheckbox.setSelected(latestValue);
        System.out.printf("[Settings] skipSplashCheckbox now set to: %b%n", latestValue);
    }

    /** Updates Remember Me in DB & local, reloads to confirm. */
    @FXML
    private void handleRememberMeChanged() {
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
        boolean newValue = rememberMeCheckbox.isSelected();

        // Local token sync
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

        // Always reload after save, to catch DB/logic desync
        boolean latestValue = (userUUID != null) ? StudyPreferences.loadRememberMe(userUUID) : newValue;
        rememberMeCheckbox.setSelected(latestValue);
        System.out.printf("[Settings] rememberMeCheckbox now set to: %b%n", latestValue);
    }

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
                    "Password is not strong enough! \n Ensure it has at least ...  \n - 12 characters, ... \n - one uppercase letter, ... \n - one number, ... \n and one special character!",
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
                        StylingUtility.showSuccess(
                                NewPasswordField,
                                "Password has been updated successfully!",
                                "text-field-success",
                                "text-field"
                        );
                        NewPasswordField.clear();
                        ConfirmPasswordField.clear();
                    } else {
                        StylingUtility.showError(
                                NewPasswordField, toolTipService, passwordTooltip1,
                                "Failed to update password. Please try again.",
                                "tooltip-Label-Error", "password-field-error", "password-field", secs);
                    }
                } else {
                    StylingUtility.showError(
                            NewPasswordField, toolTipService, passwordTooltip1,
                            "Could not find user email. Cannot update password.",
                            "tooltip-Label-Error", "password-field-error", "password-field", secs);
                }
            }
            default -> {}
        }
    }

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
        } else {
            System.err.println("Saving study preferences failed.");
        }
    }

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
        //Already on Settings;
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}