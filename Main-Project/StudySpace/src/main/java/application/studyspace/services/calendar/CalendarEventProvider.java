package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.LoginChecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class CalendarEventProvider {

    public static List<CalendarEvent> getEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        UUID userId = LoginChecker.getLoggedInUserUUID();
        String sql = "SELECT * FROM calendar_events WHERE user_id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, uuidToBytes(userId));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                events.add(new CalendarEvent(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("start_date").toLocalDateTime(),
                        rs.getTimestamp("end_date").toLocalDateTime(),
                        rs.getString("event_color"),   // use the single color column
                        rs.getString("event_tag")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Retrieved Events: " + events.size());
        return events;
    }
}
