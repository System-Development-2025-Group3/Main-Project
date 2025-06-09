package application.studyspace.services.calendar;

import java.time.LocalDateTime;
import java.util.List;

public class CalendarEvent {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;  // only one color field now
    private String tag;

    public CalendarEvent(String title,
                         String description,
                         LocalDateTime start,
                         LocalDateTime end,
                         String color,
                         String tag) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.color = color;
        this.tag = tag;
    }

    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStart(){ return start; }
    public LocalDateTime getEnd()  { return end; }
    public String getColor()       { return color; }
    public String getTag()         { return tag; }

    public static List<CalendarEvent> getAllEvents() {
        return CalendarEventProvider.getEvents();
    }
}
