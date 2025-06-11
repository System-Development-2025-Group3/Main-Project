package application.studyspace.controllers.calendar;

import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.EventColor;
import application.studyspace.services.calendar.InsertCalendarEvent;
import application.studyspace.controllers.landingpage.LandingpageController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CreateNewEventController {

    // ─── FXML-bound controls ────────────────────────────────────────────
    @FXML private Button closeBtn;
    @FXML private Button CreateBtn;

    @FXML private TextField EventTitleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker, endDatePicker;

    @FXML private TextField hourFieldstart, minuteFieldstart;
    @FXML private ComboBox<String> amPmDropdownstart;
    @FXML private TextField hourFieldend, minuteFieldend;
    @FXML private ComboBox<String> amPmDropdownend;

    @FXML private FlowPane tagsContainer;
    @FXML private TextField tagInputField;
    @FXML private ListView<String> suggestionsListView;

    @FXML private ToggleGroup colorGroup;
    @FXML private ToggleButton colorRed, colorBlue, colorGreen, colorYellow, colorPurple;

    // ─── Internal state ─────────────────────────────────────────────────
    private ContextMenu smartDropdown;
    private ObservableList<String> allTags;
    private ObservableList<String> selectedTags = FXCollections.observableArrayList();

    // ─── Initialization ─────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Populate AM/PM pickers
        amPmDropdownstart.getItems().addAll("AM","PM");
        amPmDropdownend  .getItems().addAll("AM","PM");
        amPmDropdownstart.getSelectionModel().select("AM");
        amPmDropdownend  .getSelectionModel().select("AM");

        // Map toggles to our EventColor enum
        colorRed  .setUserData(EventColor.RED);
        colorBlue .setUserData(EventColor.BLUE);
        colorGreen.setUserData(EventColor.GREEN);
        colorYellow.setUserData(EventColor.YELLOW);
        colorPurple.setUserData(EventColor.PURPLE);

        // Load tags and set up chips
        allTags = FXCollections.observableArrayList(CalendarEvent.getExistingTagsatUser());
        setupTagsInput();
    }

    // ─── Smart-end-time dropdown ────────────────────────────────────────
    @FXML public void showSmartSelectionDropdown() {
        if (smartDropdown == null) smartDropdown = createSmartTimeDropdown();
        TextField src = hourFieldend.isFocused() ? hourFieldend
                : minuteFieldend.isFocused()? minuteFieldend
                : null;
        if (src != null) {
            Bounds b = src.localToScene(src.getBoundsInLocal());
            smartDropdown.show(src,
                    src.getScene().getWindow().getX()+b.getMaxX()+10,
                    src.getScene().getWindow().getY()+b.getMinY()
            );
        }
    }
    private ContextMenu createSmartTimeDropdown() {
        return new ContextMenu(
                createTimeItem("+15 min",0,15),
                createTimeItem("+30 min",0,30),
                createTimeItem("+45 min",0,45),
                createTimeItem("+1 hr",1,0)
        );
    }
    private MenuItem createTimeItem(String label,int h,int m){
        MenuItem it=new MenuItem(label);
        it.setOnAction(e->adjustEndTime(h,m));
        return it;
    }
    private void adjustEndTime(int hrs,int mins){
        try {
            int sh=Integer.parseInt(hourFieldstart.getText());
            int sm=Integer.parseInt(minuteFieldstart.getText());
            boolean pm="PM".equals(amPmDropdownstart.getValue());
            int totalM=sm+mins, carry=totalM/60, finalM=totalM%60;
            int totalH=sh+hrs+carry; boolean endPm=pm;
            if (totalH>=12) { totalH%=12; if(totalH!=0) endPm=!pm; }
            totalH=(totalH==0)?12:totalH;
            hourFieldend.setText(String.format("%02d",totalH));
            minuteFieldend.setText(String.format("%02d",finalM));
            amPmDropdownend.setValue(endPm?"PM":"AM");
        } catch(Exception ex){ /* ignore parse errors */ }
    }

    // ─── Save & Close handlers ──────────────────────────────────────────
    @FXML public void handleClickClosePopupButton() {
        ((Stage)closeBtn.getScene().getWindow()).close();
    }

    /** Gather inputs, persist new event, refresh calendar, close popup **/
    @FXML public void handleSave() {
        try {
            // Validate title & date
            String title=EventTitleField.getText().trim();
            LocalDate sd=startDatePicker.getValue();
            if(title.isEmpty()||sd==null){
                new Alert(Alert.AlertType.WARNING,"Enter title & start date").showAndWait();
                return;
            }
            // Parse start time
            int sh=Integer.parseInt(hourFieldstart.getText()),
                    sm=Integer.parseInt(minuteFieldstart.getText());
            if("PM".equals(amPmDropdownstart.getValue())&&sh<12)sh+=12;
            if("AM".equals(amPmDropdownstart.getValue())&&sh==12)sh=0;
            LocalDateTime start=sd.atTime(sh,sm);
            // Parse end time & date
            LocalDate ed=(endDatePicker.getValue()!=null)?endDatePicker.getValue():sd;
            int eh=Integer.parseInt(hourFieldend.getText()),
                    em=Integer.parseInt(minuteFieldend.getText());
            if("PM".equals(amPmDropdownend.getValue())&&eh<12)eh+=12;
            if("AM".equals(amPmDropdownend.getValue())&&eh==12)eh=0;
            LocalDateTime end=ed.atTime(eh,em);
            // Other fields
            String desc=descriptionArea.getText().trim();
            String tag=selectedTags.isEmpty()?"":selectedTags.get(0);
            EventColor color=(EventColor)colorGroup.getSelectedToggle().getUserData();
            // Persist
            CalendarEvent ev=new CalendarEvent(title,desc,start,end,color,tag);
            InsertCalendarEvent.insertIntoDatabase(ev);
            // Trigger calendar view refresh
            LandingpageController.getInstance().showMonthView();
            // Close popup
            handleClickClosePopupButton();
        } catch(Exception ex) {
            new Alert(Alert.AlertType.ERROR,"Save failed:\n"+ex.getMessage()).showAndWait();
        }
    }

    public void setPreselectedDate(LocalDate date) {
        startDatePicker.setValue(date);
        endDatePicker.setValue(date);
    }

    // ─── Tag‐chip autocomplete helpers ──────────────────────────────────
    private void setupTagsInput() {
        tagInputField.textProperty().addListener((obs, oldText, newText) -> {
            // Hide if empty
            if (newText == null || newText.isBlank()) {
                suggestionsListView.setVisible(false);
                return;
            }

            // Filter out already-selected tags
            List<String> filtered = allTags.stream()
                    .filter(t ->
                            t.toLowerCase().contains(newText.toLowerCase())
                                    && !selectedTags.contains(t)
                    )
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                suggestionsListView.setVisible(false);
            } else {
                // Populate and position the dropdown
                suggestionsListView.setItems(FXCollections.observableArrayList(filtered));
                Bounds b = tagInputField.localToScene(tagInputField.getBoundsInLocal());
                suggestionsListView.setLayoutX(b.getMinX());
                suggestionsListView.setLayoutY(b.getMinY()
                        - suggestionsListView.getPrefHeight() - 5);
                suggestionsListView.setVisible(true);
            }
        });

        // Hide suggestion list when pressing Enter
        tagInputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !suggestionsListView.getItems().isEmpty()) {
                addTag(suggestionsListView.getItems().get(0));
            }
        });

        suggestionsListView.setOnMouseClicked(e -> {
            String sel = suggestionsListView.getSelectionModel().getSelectedItem();
            addTag(sel);
        });
    }

    private void addTag(String t){
        if(t!=null&&!t.isBlank()&&!selectedTags.contains(t)){
            if(!allTags.contains(t)){ CalendarEvent.addTagToDatabase(t); allTags.add(t); }
            selectedTags.add(t);
        }
        tagInputField.clear(); suggestionsListView.setVisible(false); drawChips();
    }
    private void drawChips(){
        tagsContainer.getChildren().clear();
        selectedTags.forEach(tag->{
            Button rem=new Button("x");
            rem.setOnAction(e->{ selectedTags.remove(tag); drawChips(); });
            HBox chip=new HBox(new Label(tag),rem);
            chip.getStyleClass().add("chip"); chip.setSpacing(4);
            tagsContainer.getChildren().add(chip);
        });
        tagsContainer.getChildren().add(tagInputField);
    }
}
