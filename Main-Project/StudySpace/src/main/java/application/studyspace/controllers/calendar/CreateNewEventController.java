package application.studyspace.controllers.calendar;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;

public class CreateNewEventController {

    @FXML
    private Button closeBtn;

    @FXML
    private TextField hourFieldend,minuteFieldend, hourFieldstart,minuteFieldstart;
    private ContextMenu smartDropdown;
    @FXML
    private ComboBox<String> amPmDropdownstart;

    @FXML
    private ComboBox<String> amPmDropdownend;

    /**
     * Handles the action of clicking the close button in the popup window.
     * This method is triggered when the close button is clicked, and it closes
     * the current stage (popup window) associated with the button.
     */
    @FXML
    public void handleClickClosePopupButton() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows the smart dropdown with options for time selection (15/30/45/60 minutes, 1/2/3 hours).
     * The dropdown is displayed when the user interacts with either the hours or minutes fields of the end time.
     */
    @FXML
    public void showSmartSelectionDropdown() {
        if (smartDropdown == null) {
            smartDropdown = createSmartTimeDropdown();
        }

        TextField sourceField = null;
        if (hourFieldend.isFocused()) {
            sourceField = hourFieldend;
        } else if (minuteFieldend.isFocused()) {
            sourceField = minuteFieldend;
        }

        if (sourceField != null) {
            // Show the dropdown below the focused field
            smartDropdown.show(sourceField, sourceField.getScene().getWindow().getX() + sourceField.getLayoutX(),
                sourceField.getScene().getWindow().getY() + sourceField.getLayoutY() + sourceField.getHeight());
        }
    }

    /**
     * Creates a context menu with predefined time intervals.
     *
     * @return a `ContextMenu` instance with time suggestions like 15/30/45 minutes and 1/2/3 hours.
     */
    private ContextMenu createSmartTimeDropdown() {
        ContextMenu dropdown = new ContextMenu();

        // Create menu items for 15/30/45/60 minutes
        MenuItem fifteenMinutes = createTimeMenuItem("Add 15 minutes", 0, 15);
        MenuItem thirtyMinutes = createTimeMenuItem("Add 30 minutes", 0, 30);
        MenuItem fortyFiveMinutes = createTimeMenuItem("Add 45 minutes", 0, 45);
        MenuItem sixtyMinutes = createTimeMenuItem("Add 60 minutes", 1, 0);

        // Create menu items for 1/2/3 hours
        MenuItem oneHour = createTimeMenuItem("Add 1 hour", 1, 0);
        MenuItem twoHours = createTimeMenuItem("Add 2 hours", 2, 0);
        MenuItem threeHours = createTimeMenuItem("Add 3 hours", 3, 0);

        // Add items to the dropdown
        dropdown.getItems().addAll(fifteenMinutes, thirtyMinutes, fortyFiveMinutes, sixtyMinutes, oneHour, twoHours, threeHours);
        return dropdown;
    }

    /**
     * Helper method to create a `MenuItem` for a specific time interval.
     *
     * @param label The label displayed on the menu item.
     * @param hours The number of hours to add.
     * @param minutes The number of minutes to add.
     * @return A configured `MenuItem` instance.
     */
    private MenuItem createTimeMenuItem(String label, int hours, int minutes) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> adjustEndTimeBasedOnStartTime(hours, minutes));
        return menuItem;
    }

    /**
     * Adjusts the end time fields based on the selected interval.
     * The logic ensures proper rollover of minutes/hours and handles AM/PM transitions.
     *
     * @param hours The number of hours to add.
     * @param minutes The number of minutes to add.
     */
    /**
     * Adjusts the End Time fields based on the Start Time values and selected interval.
     *
     * @param hours   Hours to add to Start Time
     * @param minutes Minutes to add to Start Time
     */
    private void adjustEndTimeBasedOnStartTime(int hours, int minutes) {
        try {
            // Parse the Start Time values
            int startHours = hourFieldstart.getText().isEmpty() ? 0 : Integer.parseInt(hourFieldstart.getText());
            int startMinutes = minuteFieldstart.getText().isEmpty() ? 0 : Integer.parseInt(minuteFieldstart.getText());
            boolean isStartPM = "PM".equals(amPmDropdownstart.getValue());

            // Calculate the total time
            int totalMinutes = startMinutes + minutes;
            int extraHours = totalMinutes / 60;
            int finalMinutes = totalMinutes % 60;

            int totalHours = startHours + hours + extraHours;

            // Handle AM/PM transitions
            boolean isEndPM = isStartPM;
            if (totalHours >= 12) {
                totalHours %= 12;

                // Toggle AM/PM if crossing 12 hours
                if (totalHours != 0) {
                    isEndPM = !isStartPM;
                }
            }

            // Adjust for the 12-hour clock format
            totalHours = (totalHours == 0) ? 12 : totalHours;

            // Update End Time fields
            hourFieldend.setText(String.format("%02d", totalHours));
            minuteFieldend.setText(String.format("%02d", finalMinutes));
            amPmDropdownend.setValue(isEndPM ? "PM" : "AM");

        } catch (NumberFormatException e) {
            System.err.println("Error parsing start time values: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        // Add items to the dropdowns
        amPmDropdownstart.getItems().addAll("AM", "PM");
        amPmDropdownend.getItems().addAll("AM", "PM");

        // Set default selections
        amPmDropdownstart.getSelectionModel().select("AM");
        amPmDropdownend.getSelectionModel().select("AM");

        // Configure CellFactory to avoid truncation
        amPmDropdownstart.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            return cell;
        });

        amPmDropdownstart.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
            }
        });

        amPmDropdownend.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            return cell;
        });

        amPmDropdownend.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
            }
        });
    }

}