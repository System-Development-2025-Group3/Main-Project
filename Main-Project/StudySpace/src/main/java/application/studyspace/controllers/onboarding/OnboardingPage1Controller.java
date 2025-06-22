package application.studyspace.controllers.onboarding;

import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

public class OnboardingPage1Controller {

    public Button page1Btn;
    public Button page2Btn;
    public Button page3Btn;
    @FXML private Spinner<LocalTime> startTimeSpinner;
    @FXML private Spinner<LocalTime> endTimeSpinner;
    @FXML private Slider sessionLengthSlider;
    @FXML private Label sessionLengthLabel;
    @FXML private Slider breakLengthSlider;
    @FXML private Label breakLengthLabel;

    @FXML private ToggleButton monBtn;
    @FXML private ToggleButton tueBtn;
    @FXML private ToggleButton wedBtn;
    @FXML private ToggleButton thuBtn;
    @FXML private ToggleButton friBtn;
    @FXML private ToggleButton satBtn;
    @FXML private ToggleButton sunBtn;

    @FXML
    private void initialize() {
        // Populate time options
        ObservableList<LocalTime> times = FXCollections.observableArrayList();
        LocalTime t = LocalTime.of(6, 0);
        while (!t.isAfter(LocalTime.of(22, 0))) {
            times.add(t);
            t = t.plusMinutes(30);
        }

        // Time formatter
        StringConverter<LocalTime> timeFmt = new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            @Override public String toString(LocalTime lt) {
                return lt == null ? "" : fmt.format(lt);
            }
            @Override public LocalTime fromString(String str) {
                return (str == null || str.isEmpty()) ? null : LocalTime.parse(str, fmt);
            }
        };

        // Spinner factories
        SpinnerValueFactory<LocalTime> startFactory =
                new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        startFactory.setConverter(timeFmt);

        SpinnerValueFactory<LocalTime> endFactory =
                new SpinnerValueFactory.ListSpinnerValueFactory<>(times);
        endFactory.setConverter(timeFmt);

        // Load existing prefs
        try {
            UUID userId = SessionManager.getInstance().getLoggedInUserId();
            StudyPreferences prefs = StudyPreferences.load(userId);

            startFactory.setValue(prefs.getStartTime());
            endFactory.setValue(prefs.getEndTime());

            sessionLengthSlider.setValue(prefs.getSessionLength());
            sessionLengthLabel.setText(prefs.getSessionLength() + " min");

            breakLengthSlider.setValue(prefs.getBreakLength());
            breakLengthLabel.setText(prefs.getBreakLength() + " min");

            // Blocked days toggles
            Set<DayOfWeek> blocked = prefs.getBlockedDays();
            monBtn.setSelected(blocked.contains(DayOfWeek.MONDAY));
            tueBtn.setSelected(blocked.contains(DayOfWeek.TUESDAY));
            wedBtn.setSelected(blocked.contains(DayOfWeek.WEDNESDAY));
            thuBtn.setSelected(blocked.contains(DayOfWeek.THURSDAY));
            friBtn.setSelected(blocked.contains(DayOfWeek.FRIDAY));
            satBtn.setSelected(blocked.contains(DayOfWeek.SATURDAY));
            sunBtn.setSelected(blocked.contains(DayOfWeek.SUNDAY));

        } catch (SQLException ex) {
            System.out.println("No existing prefs, defaults applied.");
        }

        // Defaults if null
        if (startFactory.getValue() == null) startFactory.setValue(LocalTime.of(8, 0));
        if (endFactory.getValue() == null)   endFactory.setValue(LocalTime.of(18, 0));
        if (sessionLengthSlider.getValue() == 0) {
            sessionLengthSlider.setValue(60);
            sessionLengthLabel.setText("60 min");
        }
        if (breakLengthSlider.getValue() == 0) {
            breakLengthSlider.setValue(10);
            breakLengthLabel.setText("10 min");
        }

        startTimeSpinner.setValueFactory(startFactory);
        startTimeSpinner.setEditable(true);

        endTimeSpinner.setValueFactory(endFactory);
        endTimeSpinner.setEditable(true);

        sessionLengthSlider.valueProperty().addListener((obs, o, n) -> {
            int val = (int) (Math.round(n.doubleValue()/5)*5);
            sessionLengthSlider.setValue(val);
            sessionLengthLabel.setText(val+" min");
        });
        breakLengthSlider.valueProperty().addListener((obs, o, n) -> {
            int val = (int) (Math.round(n.doubleValue()/5)*5);
            breakLengthSlider.setValue(val);
            breakLengthLabel.setText(val+" min");
        });
    }

    @FXML
    private void handlePage1(ActionEvent event) {
        // no-op
    }

    @FXML
    private void handlePage2(ActionEvent event) {
        savePrefs();
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage2.fxml",
                ctrl -> {}
        );
    }

    @FXML
    private void handlePage3(ActionEvent event) {
        savePrefs();
        ViewManager.closeTopOverlay();
        ViewManager.showOverlay(
                "/application/studyspace/onboarding/OnboardingPage3.fxml",
                ctrl -> {}
        );
    }

    private void savePrefs() {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        StringBuilder bd = new StringBuilder();
        if(monBtn.isSelected()) bd.append("MONDAY,");
        if(tueBtn.isSelected()) bd.append("TUESDAY,");
        if(wedBtn.isSelected()) bd.append("WEDNESDAY,");
        if(thuBtn.isSelected()) bd.append("THURSDAY,");
        if(friBtn.isSelected()) bd.append("FRIDAY,");
        if(satBtn.isSelected()) bd.append("SATURDAY,");
        if(sunBtn.isSelected()) bd.append("SUNDAY,");
        String blocked = bd.length()>0 ? bd.substring(0, bd.length()-1) : "";

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        String range = startTimeSpinner.getValue().format(fmt)
                + "-" + endTimeSpinner.getValue().format(fmt);

        StudyPreferences prefs = new StudyPreferences(
                userId,
                range,
                (int)sessionLengthSlider.getValue(),
                (int)breakLengthSlider.getValue(),
                blocked
        );
        boolean ok = prefs.saveToDatabase();
        System.out.println(ok?"Prefs saved":"Prefs save failed");
    }
}
