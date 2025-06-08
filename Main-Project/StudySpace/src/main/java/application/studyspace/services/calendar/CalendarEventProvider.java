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

public class CalendarEventProvider {

    public static List<CalendarEvent> getEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        UUID loggedInUserUUID = LoginChecker.getLoggedInUserUUID();

        try (Connection connection = new DatabaseConnection().getConnection()) {
            String sql = "SELECT * FROM calendar_events WHERE user_id = ?";
            System.out.println("Executing SQL Query: " + sql + " with user_id: " + loggedInUserUUID);

            PreparedStatement statement = connection.prepareStatement(sql);
            // Pass UUID as a byte array for the BINARY(16) format
            statement.setBytes(1, uuidToBytes(loggedInUserUUID));

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                CalendarEvent event = new CalendarEvent(
                        result.getString("title"),
                        result.getString("description"),
                        result.getTimestamp("start_date").toLocalDateTime(),
                        result.getTimestamp("end_date").toLocalDateTime(),
                        result.getString("color_hex"),
                        result.getString("event_tag")
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