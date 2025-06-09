package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.LoginChecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class InsertCalendarEvent {

    /**
     * Inserts the specified event into the database.
     *
     * @param event the {@link CalendarEvent} to save; must have valid start/end times
     */
    public static void insertIntoDatabase(CalendarEvent event) {
        String sql = """
            INSERT INTO calendar_events
              (start_date, end_date, event_tag, event_color,
               title, description, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind parameters in the order of the INSERT statement
            UUID userId = LoginChecker.getLoggedInUserUUID();
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(event.getStart()));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(event.getEnd()));
            ps.setString(3, event.getTag());
            ps.setString(4, event.getColor());
            ps.setString(5, event.getTitle());
            ps.setString(6, event.getDescription());
            ps.setBytes(7, uuidToBytes(userId));

            int rows = ps.executeUpdate();
            System.out.println("Inserted rows: " + rows);

        } catch (SQLException e) {
            // Log any SQL errors for debugging
            e.printStackTrace();
        }
    }
}
