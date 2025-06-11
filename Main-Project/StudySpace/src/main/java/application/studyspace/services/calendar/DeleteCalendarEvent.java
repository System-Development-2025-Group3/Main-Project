package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class DeleteCalendarEvent {

    /**
     * Deletes the specified calendar event for the currently logged-in user.
     * Matches on start_date, end_date, event_tag, event_color, title, and description.
     *
     * @param event the CalendarEvent to remove
     */
    public static void delete(CalendarEvent event) {
        String sql = """
            DELETE FROM calendar_events
             WHERE user_id    = ?
               AND start_date = ?
               AND end_date   = ?
               AND event_tag  = ?
               AND event_color= ?
               AND title      = ?
               AND description= ?
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            UUID userId = SessionManager.getInstance().getLoggedInUserId();
            ps.setBytes      (1, uuidToBytes(userId));
            ps.setTimestamp  (2, java.sql.Timestamp.valueOf(event.getStart()));
            ps.setTimestamp  (3, java.sql.Timestamp.valueOf(event.getEnd()));
            ps.setString     (4, event.getTag());
            ps.setString(5, event.getColor().name());
            ps.setString     (6, event.getTitle());
            ps.setString     (7, event.getDescription());

            int deleted = ps.executeUpdate();
            System.out.println("Deleted " + deleted + " calendar event(s).");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
