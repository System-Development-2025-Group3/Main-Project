package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.LoginChecker;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CalendarEventProvider is a utility class responsible for retrieving calendar
 * events associated with a specific user from the database. It integrates
 * with the database to fetch event details such as title, description,
 * start and end times, color coding, and tags, for the currently
 * logged-in user.
 */
public class CalendarEventProvider {

    /**
     * Retrieves a list of calendar events associated with the currently logged-in user.
     * The method connects to the database, executes a query to fetch events for the
     * logged-in user's UUID, and maps the results into a list of CalendarEvent objects.
     * If no events are found or an exception occurs, an empty list is returned.
     *
     * @return a List of CalendarEvent objects representing the user's calendar events,
     *         or an empty list if no events are found or an error occurs
     */
    public static List<CalendarEvent> getEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        UUID loggedInUserUUID = LoginChecker.getLoggedInUserUUID();

        try (Connection connection = new DatabaseConnection().getConnection()) {
            String sql = "SELECT * FROM calendar_events WHERE user_id = ?";
            System.out.println("Executing SQL Query: " + sql + " with user_id: " + loggedInUserUUID);

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBytes(1, uuidToBytes(loggedInUserUUID));

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                CalendarEvent event = new CalendarEvent(
                        result.getString("title"),
                        result.getString("description"),
                        result.getTimestamp("start_date").toLocalDateTime(),
                        result.getTimestamp("end_date").toLocalDateTime(),
                        result.getString("color_hex"),
                        result.getString("event_tag"),
                        result.getString("color_hex")
                );

                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Retrieved Events: " + events.size());
        return events;
    }

    /**
     * Converts a UUID into a byte array representation.
     *
     * @param uuid the UUID to be converted, must not be null
     * @return a byte array containing the 16-byte representation of the UUID
     */
    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}