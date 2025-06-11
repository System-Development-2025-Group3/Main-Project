package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class CalendarEventProvider {

    public static List<CalendarEvent> getEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        String sql = """
            SELECT title,
                   description,
                   start_date,
                   end_date,
                   event_color,
                   event_tag
              FROM calendar_events
             WHERE user_id = ?
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title       = rs.getString("title");
                    String description = rs.getString("description");
                    LocalDateTime start= rs.getTimestamp("start_date").toLocalDateTime();
                    LocalDateTime end  = rs.getTimestamp("end_date").toLocalDateTime();
                    // Map the stored name to the enum
                    EventColor color   = EventColor.fromName(rs.getString("event_color"));
                    String tag         = rs.getString("event_tag");

                    events.add(new CalendarEvent(title, description, start, end, color, tag));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }
}
