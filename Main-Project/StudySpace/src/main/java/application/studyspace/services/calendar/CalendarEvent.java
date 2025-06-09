package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.LoginChecker;
import application.studyspace.services.auth.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.BytesToUUID;
import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class CalendarEvent {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;
    private String tag;
    String colorHex;

    public CalendarEvent(String title, String description, LocalDateTime start, LocalDateTime end, String color, String tag, String colorHex) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.color = color;
        this.tag = tag;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getColor() { return color; }
    public String getTag() { return tag; }
    public String getColorHex() { return colorHex; }

    public static List<CalendarEvent> getAllEvents() {
        return CalendarEventProvider.getEvents();
    }

    private static final List<String> existingTagsatUser = new ArrayList<>();
    private static final List<String> existingTags = new ArrayList<>();

    /**
     * Retrieves a list of tags associated with the currently logged-in user from the database.
     * The tags are fetched by querying the "calendar_tags" and "user_tags" tables.
     *
     * @return a List of Strings representing the tag values associated with the logged-in user
     */
    public static List<String> getExistingTagsatUser() {
        existingTagsatUser.clear();
        UUID loggedInUserUUID = SessionManager.getInstance().getLoggedInUserId();

        try (Connection connection = new DatabaseConnection().getConnection()) {
            String sql = "SELECT ct.tag_value FROM calendar_tags ct, user_tags ut WHERE ct.tag_uuid = ut.tag_uuid AND ut.user_id = ?";
            System.out.println("Executing SQL Query: " + sql + " with user_id: " + loggedInUserUUID);

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBytes(1, uuidToBytes(loggedInUserUUID));

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                existingTagsatUser.add(result.getString("tag_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingTagsatUser;
    }

    /**
     * Retrieves a list of existing tags from the database.
     * The tags are fetched from the "calendar_tags" table.
     *
     * @return a List of Strings representing the existing tag values retrieved from the database
     */
    public static List<String> getExistingTags() {
        existingTags.clear();

        try (Connection connection = new DatabaseConnection().getConnection()) {
            String sql = "SELECT ct.tag_value FROM calendar_tags ct;";
            System.out.println("Executing SQL Query: " + sql);

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                existingTags.add(result.getString("tag_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingTags;
    }

    /**
     * Adds a new tag to the database if it does not already exist.
     * This method inserts the new tag into the "calendar_tags" table and associates it
     * with the currently logged-in user in the "user_tags" table.
     *
     * @param newTag the new tag to be added to the database
     */
    public static void addTagToDatabase(String newTag) {
        UUID loggedInUserUUID = SessionManager.getInstance().getLoggedInUserId();

        if (!existingTags.contains(newTag)) {
            existingTags.add(newTag);

            try (Connection connection = new DatabaseConnection().getConnection()) {
                // Insert into calendar_tags table
                String sql = "INSERT INTO calendar_tags (tag_value) VALUES (?) RETURNING tag_uuid;";
                System.out.println("Executing SQL Query: " + sql);

                // Create PreparedStatement with RETURN_GENERATED_KEYS
                PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, newTag); // Add the `tag_value` parameter
                ResultSet result = statement.executeQuery();

                UUID tagUUID = null;

                if (result.next()) {
                    byte[] uuidBytes = result.getBytes("tag_uuid");
                    tagUUID = BytesToUUID(uuidBytes);
                }

                System.out.println("New tag UUID: " + tagUUID);

                // Insert into user_tags table
                sql = "INSERT INTO user_tags (user_id, tag_uuid) VALUES (?, ?)";
                System.out.println("Executing SQL Query: " + sql);

                statement = connection.prepareStatement(sql);
                statement.setBytes(1, uuidToBytes(loggedInUserUUID));

                assert tagUUID != null;
                statement.setBytes(2, uuidToBytes(tagUUID));
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("New tag added to database: " + newTag);
        }
    }



}