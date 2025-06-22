package application.studyspace.controllers.landingpage;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.PasswordHasher;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.DataBase.DatabaseHelper;
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

    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    private final CreateToolTip toolTipService = new CreateToolTip();

    @FXML
    private void initialize() {

        String pwTip = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip1, pwTip, "tooltip-Label");

        String pwTip1 = """
            The Password should fulfill the following conditions:\s
            • At least 12 characters long
            • Includes at least one uppercase letter
            • Includes at least one number
            • Includes at least one special character (%, &, !, ?, #, _, -, $)
            """;
        toolTipService.createCustomTooltip(passwordTooltip2, pwTip1, "tooltip-Label");

        // --- Configure Time Spinners ---
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime t = LocalTime.of(6, 0);
        while (!t.isAfter(LocalTime.of(22, 0))) {
            times.add(t);
            t = t.plusMinutes(30);
        }

        // Helper to format LocalTime in "HH:mm" format for the spinners
        StringConverter<LocalTime> timeFmt = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override public String toString(LocalTime lt) {
                return lt == null ? "" : fmt.format(lt);
            }
            @Override public LocalTime fromString(String str) {
                return (str == null || str.isEmpty()) ? null : LocalTime.parse(str, fmt);
            }
        };

        // Configure the start time spinner
        SpinnerValueFactory<LocalTime> startFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        startFactory.setConverter(timeFmt);
        startFactory.setValue(LocalTime.of(9, 0)); // Default start time
        startTimeSpinner.setValueFactory(startFactory);
        startTimeSpinner.setEditable(true);

        // Configure the end time spinner
        SpinnerValueFactory<LocalTime> endFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        endFactory.setConverter(timeFmt);
        endFactory.setValue(LocalTime.of(17, 0)); // Default end time
        endTimeSpinner.setValueFactory(endFactory);
        endTimeSpinner.setEditable(true);

        // --- Configure Sliders ---
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
    }

    @FXML
    private void handleResetPassword(ActionEvent event) throws SQLException {
        String newPassword = NewPasswordField.getText();
        String confirmPassword = ConfirmPasswordField.getText();
        int secs = 5;

        ValidationUtils.ValidationResult result = ValidationUtils.validatePasswordUpdate(newPassword, confirmPassword);

        switch (result) {
            case EMPTY_PASSWORD -> StylingUtility.showError(
                    NewPasswordField, toolTipService, passwordTooltip1,
                    "Please enter a new password.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);

            case PASSWORD_INVALID -> StylingUtility.showError(
                    NewPasswordField, toolTipService, passwordTooltip1,
                    "Password is not strong enough! \n Ensure it has at least ...  \n - 12 characters, ... \n - one uppercase letter, ... \n - one number, ... \n and one special character!",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);

            case PASSWORD_MISMATCH -> StylingUtility.showError(
                    ConfirmPasswordField, toolTipService, passwordTooltip2,
                    "Passwords do not match.",
                    "tooltip-Label-Error", "password-field-error", "password-field", secs);

            case OK -> {
                UUID userUUID = SessionManager.getInstance().getLoggedInUserId();
                String email = DatabaseHelper.getEmailByUUID(userUUID);

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
            default -> {
                // To handle any other validation results if necessary
            }
        }
    }


    @FXML
    private void handleSave(ActionEvent event) {
        // Get user session
        UUID userUUID = SessionManager.getInstance().getLoggedInUserId();

        // Get blocked days
        StringBuilder blockedDaysBuilder = new StringBuilder();
        if (monBtn.isSelected()) blockedDaysBuilder.append("Monday ");
        if (tueBtn.isSelected()) blockedDaysBuilder.append("Tuesday ");
        if (wedBtn.isSelected()) blockedDaysBuilder.append("Wednesday ");
        if (thuBtn.isSelected()) blockedDaysBuilder.append("Thursday ");
        if (friBtn.isSelected()) blockedDaysBuilder.append("Friday ");
        if (satBtn.isSelected()) blockedDaysBuilder.append("Saturday ");
        if (sunBtn.isSelected()) blockedDaysBuilder.append("Sunday ");
        String blockedDays = blockedDaysBuilder.toString().trim();

        // Get allowed study time range
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
    /** Handler for clicking the “Calendar” item in the sidebar. */
    @FXML
    private void handleSidebarCalendar() {
        ViewManager.show("/application/studyspace/landingpage/Landingpage.fxml");
    }

    @FXML
    private void handleSidebarDashboard() {
        ViewManager.show("/application/studyspace/landingpage/Dashboard.fxml");
    }

    @FXML
    private void handleSidebarSettings() {
        //Already on Settings;
    }

    /** Exits the application. */
    @FXML
    private void handleExit() {
        Platform.exit();
    }
}