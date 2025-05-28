package application.studyspace.services.onboarding;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.DatabaseHelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A generic “event” that the user can add during onboarding:
 * – exams or arbitrary “blockers” (e.g. hobbies).
 */
public class OnboardingEventInput {

    private final UUID userUUID;
    private final String title;
    private final String description;
    private final int estimatedHours;
    private final String type;    // “Exam” or “Blocker”
    private final String color;   // hex or CSS name

    public OnboardingEventInput(UUID userUUID,
                                String title,
                                String description,
                                int estimatedHours,
                                String type,
                                String color)
    {
        this.userUUID      = userUUID;
        this.title         = title;
        this.description   = description;
        this.estimatedHours= estimatedHours;
        this.type          = type;
        this.color         = color;
    }

    public boolean saveToDatabase() {
        String sql = """
            INSERT INTO onboarding_events
              (userUUID, title, description, estimated_hours, event_type, color, created_at)
            VALUES (?,      ?,     ?,           ?,               ?,          ?,     ?)
            """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            byte[] uuidBytes = new DatabaseHelper().uuidToBytes(userUUID);

            stmt.setBytes(    1, uuidBytes);
            stmt.setString(   2, title);
            stmt.setString(   3, description);
            stmt.setInt(      4, estimatedHours);
            stmt.setString(   5, type);
            stmt.setString(   6, color);
            stmt.setDate(     7, Date.valueOf(LocalDateTime.now().toLocalDate()));

            return stmt.executeUpdate() > 0;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
