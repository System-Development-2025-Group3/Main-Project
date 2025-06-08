package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.auth.LoginChecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.DataBase.UUIDHelper.uuidToBytes;

public class InsertCalendarEvent {

    public static void InsertCalendarEventIntoDatabase(CalendarEvent event){
        try (Connection connection = new DatabaseConnection().getConnection()) {
            UUID loggedInUserUUID = LoginChecker.getLoggedInUserUUID();
            String sql = "INSERT INTO calendar_events (start_date, end_date, event_tag, event_color, color_hex, title, description, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setTimestamp(1, java.sql.Timestamp.valueOf(event.getStart()));
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(event.getEnd()));
            statement.setString(3, event.getTag());
            statement.setString(4, event.getColor());
            statement.setString(5, event.getColorHex());
            statement.setString(6, event.getTitle());
            statement.setString(7, event.getDescription());
            statement.setBytes(8, uuidToBytes(loggedInUserUUID));

            System.out.println("Executing SQL Query: " + sql + " with user_id: " + loggedInUserUUID);

            int rowsInserted = statement.executeUpdate();

            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("\n============================");
            logBuilder.append("\nSQL Execution Result");
            logBuilder.append("\n============================");

            if (rowsInserted > 0) {

                logBuilder.append("\nCalendar Event inserted successfully!");
                logBuilder.append("\nInserted Event Details:");
                logBuilder.append("\n  Title: ").append(event.getTitle());
                logBuilder.append("\n  Description: ").append(event.getDescription());
                logBuilder.append("\n  Start Date: ").append(event.getStart());
                logBuilder.append("\n  End Date: ").append(event.getEnd());
                logBuilder.append("\n  Color: ").append(event.getColor());
                logBuilder.append("\n  Tag: ").append(event.getTag());
                logBuilder.append("\n  Color Hex: ").append(event.getColorHex());
                logBuilder.append("\n  User ID: ").append(loggedInUserUUID);

            } else {
                logBuilder.append("\nFailed to insert the Calendar Event.");
            }

            logBuilder.append("\n----------------------------\n");
            System.out.println(logBuilder.toString());


            statement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
