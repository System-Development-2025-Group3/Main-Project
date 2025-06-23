package application.studyspace.services.calendar;

import java.util.UUID;

/**
 * Mirrors the `calendars` table: holds the UUID, user owner, display name, and style.
 */
public class CalendarModel {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String style;

    public CalendarModel(UUID id, UUID userId, String name, String style) {
        this.id     = id;
        this.userId = userId;
        this.name   = name;
        this.style  = style;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }


}
