package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OnboardingPage1Controller {

    public Button page1Btn;
    public Button page2Btn;
    public Button page3Btn;
    @FXML private ComboBox<String> preferredTimeBox;
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

    @FXML
    private void initialize() {
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime t = LocalTime.of(6, 0);
        while (!t.isAfter(LocalTime.of(22, 0))) {
            times.add(t);
            t = t.plusMinutes(30);
        }

        // helper to show "HH:mm" in the spinner
        StringConverter<LocalTime> timeFmt = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override public String toString(LocalTime lt) {
                return lt == null ? "" : fmt.format(lt);
            }
            @Override public LocalTime fromString(String str) {
                return (str == null || str.isEmpty()) ? null : LocalTime.parse(str, fmt);
            }
        };

        // configure start spinner
        SpinnerValueFactory<LocalTime> startFactory =
                new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        startFactory.setConverter(timeFmt);
        startFactory.setValue(LocalTime.of(0, 0)); // default
        startTimeSpinner.setValueFactory(startFactory);
        startTimeSpinner.setEditable(true);

        // configure end spinner
        SpinnerValueFactory<LocalTime> endFactory =
                new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        endFactory.setConverter(timeFmt);
        endFactory.setValue(LocalTime.of(23, 59)); // default
        endTimeSpinner.setValueFactory(endFactory);
        endTimeSpinner.setEditable(true);

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
    private void handlePage1(ActionEvent event) {
        // Already on page 1
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        handleSave(event); // Save the current form
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage2.fxml",
                controller -> {} // No setup needed; controller uses SessionManager
        );
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        handleSave(event); // Save the current form
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                controller -> {}
        );
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
}
