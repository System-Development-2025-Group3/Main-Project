package application.studyspace.controllers.calendar;

import application.studyspace.services.calendar.CalendarEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CreateNewEventController {

    @FXML
    private Button closeBtn;

    @FXML
    private TextField hourFieldend,minuteFieldend, hourFieldstart,minuteFieldstart,tagInputField,
            EventTitleField, descriptionArea;

    @FXML
    private ContextMenu smartDropdown;

    @FXML
    private FlowPane tagsContainer;

    @FXML
    private ListView<String> suggestionsListView;

    @FXML
    private ComboBox<String> amPmDropdownstart, amPmDropdownend;

    private final ObservableList<String> selectedTags = FXCollections.observableArrayList();
    private ObservableList<String> allTags;


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

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    /**
     * Displays a smart selection dropdown near the currently focused input field for end time
     * (either hours or minutes). The dropdown is populated with predefined time intervals.
     *
     * The method dynamically calculates the position of the dropdown based on the location
     * of the focused text field and adjusts it to appear near the field. It creates the
     * dropdown menu if it does not already exist. The dropdown provides options for quick
     * time selection, easing user interaction.
     *
     * The dropdown is shown if one of the end time input fields (`hourFieldend` or
     * `minuteFieldend`) is focused. Its position is offset to appear to the right of the field.
     */
    @FXML
    public void showSmartSelectionDropdown() {
        if (smartDropdown == null) {
            smartDropdown = createSmartTimeDropdown();
        }

        // Determine the source field (either hours or minutes of the End Time)
        TextField sourceField = null;
        if (hourFieldend.isFocused()) {
            sourceField = hourFieldend;
        } else if (minuteFieldend.isFocused()) {
            sourceField = minuteFieldend;
        }

        if (sourceField != null) {
            // Calculate the dropdown position (right or left of the field)
            double xOffset = 10; // Space between field and dropdown
            double fieldWidth = sourceField.getWidth();
            double dropdownX = sourceField.getScene().getWindow().getX() +
                    sourceField.localToScene(0, 0).getX() + fieldWidth + xOffset;

            double dropdownY = sourceField.getScene().getWindow().getY() +
                    sourceField.localToScene(0, 0).getY();

            // Show the dropdown near the source field
            smartDropdown.show(sourceField, dropdownX, dropdownY);
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
    private ComboBox<String> tagsCombo;

    @FXML
    public void initialize() {
        // Load all possible tags from the database
        allTags = FXCollections.observableArrayList(CalendarEvent.getExistingTagsatUser());

        // Initialize tags UI
        setupTagsInput();
    }

    public void handleClickCreateEventButton(){
        System.out.println("Create Event Button Clicked");
        System.out.println("Start Date: " + startDatePicker.getValue());
        System.out.println("End Date: " + endDatePicker.getValue());
        System.out.println("Start Time: " + hourFieldstart.getText() + ":" + minuteFieldstart.getText() + " " + amPmDropdownstart.getValue());
        System.out.println("End Time: " + hourFieldend.getText() + ":" + minuteFieldend.getText() + " " + amPmDropdownend.getValue());
        System.out.println("Event Title: " + EventTitleField.getText());
        System.out.println("Event Description: " + descriptionArea.getText());
        System.out.println("Event Tag: " + "Event Category");
    }

    private void setupTagsInput() {
        // Autocomplete suggestions when typing
        tagInputField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                suggestionsListView.setVisible(false);
            } else {
                List<String> filtered = allTags.stream()
                        .filter(tag -> tag.toLowerCase().contains(newText.toLowerCase())
                                && !selectedTags.contains(tag))
                        .collect(Collectors.toList());

                suggestionsListView.setItems(FXCollections.observableArrayList(filtered));
                if (!filtered.isEmpty()) {
                    positionSuggestionsDropdown(); // Position dropdown below the input field
                } else {
                    suggestionsListView.setVisible(false);
                }
            }
        });

        // Add tag on Enter key
        tagInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTagFromInput();
            } else if (event.getCode() == KeyCode.DOWN) {
                if (suggestionsListView.isVisible() && !suggestionsListView.getItems().isEmpty()) {
                    suggestionsListView.requestFocus();
                    suggestionsListView.getSelectionModel().select(0);
                }
            }
        });

        // Add tag on single click in suggestions (instead of double click)
        suggestionsListView.setOnMouseClicked(event -> {
            String selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                addTag(selected);
            }
        });

        // Add tag when Enter key is pressed inside the ListView
        suggestionsListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selected = suggestionsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    addTag(selected);
                }
            }
        });

        // Initial drawing of chips
        drawChips();
    }

    private void addTagFromInput() {
        String tag = tagInputField.getText().trim();
        if (!tag.isEmpty() && !selectedTags.contains(tag)) {
            // Optionally: Add new tag to DB & update allTags if it doesn't exist
            if (!allTags.contains(tag)) {
                CalendarEvent.addTagToDatabase(tag);
                allTags.add(tag);
            }
            selectedTags.add(tag);
            drawChips();
        }
        tagInputField.clear();
        suggestionsListView.setVisible(false);
    }

    private void addTag(String tag) {
        if (tag != null && !tag.isEmpty() && !selectedTags.contains(tag)) {
            selectedTags.add(tag);
            drawChips();
        }
        tagInputField.clear();
        suggestionsListView.setVisible(false);
    }

    private void drawChips() {
        tagsContainer.getChildren().clear();
        for (String tag : selectedTags) {
            tagsContainer.getChildren().add(createChip(tag));
        }
        tagsContainer.getChildren().add(tagInputField); // Always keep input at end
    }

    private HBox createChip(String tag) {
        Label label = new Label(tag);
        label.getStyleClass().add("chip-label");
        Button remove = new Button("x");
        remove.getStyleClass().add("chip-remove");
        remove.setOnAction(e -> {
            selectedTags.remove(tag);
            drawChips();
        });
        HBox chip = new HBox(label, remove);
        chip.getStyleClass().add("chip");
        chip.setAlignment(Pos.CENTER);
        chip.setSpacing(4);
        return chip;
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }

    private void positionSuggestionsDropdown() {
        Bounds bounds = tagInputField.localToScene(tagInputField.getBoundsInLocal());

        // Position the dropdown directly above the tag input field
        suggestionsListView.setLayoutX(bounds.getMinX());
        suggestionsListView.setLayoutY(bounds.getMinY() - suggestionsListView.getPrefHeight() - 5); // Offset by 5px
        suggestionsListView.setVisible(true);
    }


}