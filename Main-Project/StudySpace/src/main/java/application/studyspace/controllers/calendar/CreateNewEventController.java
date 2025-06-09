package application.studyspace.controllers.calendar;

import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.InsertCalendarEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateNewEventController {

    @FXML private Button closeBtn;
    @FXML private Button CreateBtn;

    @FXML private TextField EventTitleField;
    @FXML private TextArea descriptionArea;

    @FXML private DatePicker datePicker;

    @FXML private TextField hourFieldstart, minuteFieldstart;
    @FXML private ComboBox<String> amPmDropdownstart;

    @FXML private TextField hourFieldend, minuteFieldend;
    @FXML private ComboBox<String> amPmDropdownend;

    @FXML private ComboBox<String> typeCombo;

    @FXML private ToggleGroup colorGroup;
    @FXML private ToggleButton colorRed, colorBlue, colorGreen, colorYellow, colorPurple;

    private ContextMenu smartDropdown;

    @FXML
    public void initialize() {
        // Populate AM/PM dropdowns
        amPmDropdownstart.getItems().addAll("AM","PM");
        amPmDropdownend  .getItems().addAll("AM","PM");
        amPmDropdownstart.getSelectionModel().select("AM");
        amPmDropdownend  .getSelectionModel().select("AM");

        // Prevent truncation in combo-boxes
        ListCell<String> cellFactory = new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
            }
        };
        amPmDropdownstart.setCellFactory(lv -> {
            ListCell<String> c = new ListCell<>(); c.textProperty().bind(c.itemProperty()); return c;
        });
        amPmDropdownstart.setButtonCell(cellFactory);
        amPmDropdownend.setCellFactory(lv -> {
            ListCell<String> c = new ListCell<>(); c.textProperty().bind(c.itemProperty()); return c;
        });
        amPmDropdownend.setButtonCell(cellFactory);

        // Assign userData (CSS color) to each toggle
        colorRed   .setUserData("red");
        colorBlue  .setUserData("blue");
        colorGreen .setUserData("green");
        colorYellow.setUserData("yellow");
        colorPurple.setUserData("purple");
    }

    /** Shown when user clicks end-time fields **/
    @FXML
    public void showSmartSelectionDropdown() {
        if (smartDropdown == null) {
            smartDropdown = new ContextMenu(
                    createTimeMenuItem("Add 15 min", 0, 15),
                    createTimeMenuItem("Add 30 min", 0, 30),
                    createTimeMenuItem("Add 45 min", 0, 45),
                    createTimeMenuItem("Add 1 hr",    1, 0),
                    createTimeMenuItem("Add 2 hr",    2, 0),
                    createTimeMenuItem("Add 3 hr",    3, 0)
            );
        }

        TextField src = hourFieldend.isFocused()   ? hourFieldend
                : minuteFieldend.isFocused()? minuteFieldend
                : null;
        if (src != null) {
            smartDropdown.show(
                    src,
                    src.getScene().getWindow().getX() + src.getLayoutX(),
                    src.getScene().getWindow().getY() + src.getLayoutY() + src.getHeight()
            );
        }
    }

    private MenuItem createTimeMenuItem(String label, int hrs, int mins) {
        MenuItem m = new MenuItem(label);
        m.setOnAction(e -> {
            try {
                int h = hourFieldstart.getText().isBlank() ? 0 : Integer.parseInt(hourFieldstart.getText());
                int min = minuteFieldstart.getText().isBlank()? 0: Integer.parseInt(minuteFieldstart.getText());
                boolean pm = "PM".equals(amPmDropdownstart.getValue());

                int totalMin = min + mins;
                int carry    = totalMin/60;
                int finalMin = totalMin%60;
                int totalHr  = h + hrs + carry;
                boolean endPM= pm;
                if (totalHr>=12) {
                    totalHr%=12;
                    if (totalHr!=0) endPM = !pm;
                }
                totalHr = (totalHr==0)?12:totalHr;

                hourFieldend  .setText(String.format("%02d", totalHr));
                minuteFieldend.setText(String.format("%02d", finalMin));
                amPmDropdownend.setValue(endPM?"PM":"AM");
            } catch (NumberFormatException ex) {
                System.err.println("Time parse error: " + ex.getMessage());
            }
        });
        return m;
    }

    /** Called by FXML via onAction="#handleSave" **/
    @FXML
    public void handleSave() {
        try {
            // 1) Gather & validate
            String title = EventTitleField.getText().trim();
            LocalDate date = datePicker.getValue();
            if (title.isEmpty() || date == null) {
                new Alert(Alert.AlertType.WARNING, "Please enter a Title and pick a Date").showAndWait();
                return;
            }

            // parse start
            int sh = Integer.parseInt(hourFieldstart.getText());
            int sm = Integer.parseInt(minuteFieldstart.getText());
            if ("PM".equals(amPmDropdownstart.getValue()) && sh<12) sh+=12;
            if ("AM".equals(amPmDropdownstart.getValue()) && sh==12) sh=0;
            LocalDateTime start = date.atTime(sh, sm);

            // parse end
            int eh = Integer.parseInt(hourFieldend.getText());
            int em = Integer.parseInt(minuteFieldend.getText());
            if ("PM".equals(amPmDropdownend.getValue()) && eh<12) eh+=12;
            if ("AM".equals(amPmDropdownend.getValue()) && eh==12) eh=0;
            LocalDateTime end = date.atTime(eh, em);

            String desc = descriptionArea.getText().trim();
            String tag  = typeCombo.getValue()==null?"":typeCombo.getValue();
            Toggle sel  = colorGroup.getSelectedToggle();
            String color= sel==null?"gray":sel.getUserData().toString();

            // 2) Persist
            CalendarEvent ev = new CalendarEvent(title, desc, start, end, color, tag);
            InsertCalendarEvent.insertIntoDatabase(ev);

            // 3) Close
            ((Stage)closeBtn.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Save failed:\n"+ex.getMessage()).showAndWait();
        }
    }

    /** Pre-fill the DatePicker */
    public void setPreselectedDate(LocalDate date) {
        datePicker.setValue(date);
    }

    /** Just closes */
    @FXML public void handleClickClosePopupButton() {
        ((Stage)closeBtn.getScene().getWindow()).close();
    }
}
