package application.studyspace.services.calendar;

import java.time.LocalDateTime;
import java.util.List;

public class CalendarEvent {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;

    public CalendarEvent(String title, String description, LocalDateTime start, LocalDateTime end, String color) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getColor() { return color; }

    // For demo/testing — pulls from CalendarTestEventProvider
    public static List<CalendarEvent> getAllEvents() {
        return CalendarTestEventProvider.getTestEvents();
    }
}
