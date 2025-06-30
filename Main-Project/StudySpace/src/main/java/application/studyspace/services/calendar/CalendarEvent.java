package application.studyspace.services.calendar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a calendar event (study session, blocker, etc.)
 * Can be persisted to and loaded from the database.
 */
public class CalendarEvent {

    // Unique identifier for this event
    private final UUID id;

    // Owner of the event
    private final UUID userId;

    // ID of the calendar this event belongs to
    private UUID calendarId;

    private String title;
    private String description;
    private String location;
    private ZonedDateTime start;
    private ZonedDateTime end;

    private boolean fullDay;
    private boolean hidden;

    private Duration minDuration;

    private String recurrenceRule;
    private UUID recurrenceSource;
    private ZonedDateTime recurrenceId;

    private UUID tagUuid;

    private boolean completed;

    /**
     * Constructor for creating a new event.
     * (calendarId can be set later via setter.)
     */
    public CalendarEvent(UUID userId,
                         String title,
                         String description,
                         String location,
                         ZonedDateTime start,
                         ZonedDateTime end) {
        this.id          = UUID.randomUUID();
        this.userId      = userId;
        this.title       = title;
        this.description = description;
        this.location    = location;
        this.start       = start;
        this.end         = end;
        this.completed   = false;
    }

    /**
     * Full constructor for loading an event from the database.
     * (calendarId should be set via setter afterward.)
     */
    public CalendarEvent(UUID id,
                         UUID userId,
                         String title,
                         String description,
                         String location,
                         ZonedDateTime start,
                         ZonedDateTime end,
                         boolean fullDay,
                         boolean hidden,
                         Duration minDuration,
                         String recurrenceRule,
                         UUID recurrenceSource,
                         ZonedDateTime recurrenceId,
                         UUID tagUuid,
                         boolean completed) {
        this.id               = id;
        this.userId           = userId;
        this.title            = title;
        this.description      = description;
        this.location         = location;
        this.start            = start;
        this.end              = end;
        this.fullDay          = fullDay;
        this.hidden           = hidden;
        this.minDuration      = minDuration;
        this.recurrenceRule   = recurrenceRule;
        this.recurrenceSource = recurrenceSource;
        this.recurrenceId     = recurrenceId;
        this.tagUuid          = tagUuid;
        this.completed        = false;
    }

    // ---- calendarId getter/setter ----

    public UUID getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(UUID calendarId) {
        this.calendarId = calendarId;
    }

    // ---- Basic getters/setters ----

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public boolean isFullDay() {
        return fullDay;
    }

    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Duration getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Duration minDuration) {
        this.minDuration = minDuration;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public UUID getRecurrenceSource() {
        return recurrenceSource;
    }

    public void setRecurrenceSource(UUID recurrenceSource) {
        this.recurrenceSource = recurrenceSource;
    }

    public ZonedDateTime getRecurrenceId() {
        return recurrenceId;
    }

    public void setRecurrenceId(ZonedDateTime recurrenceId) {
        this.recurrenceId = recurrenceId;
    }

    public UUID getTagUuid() {
        return tagUuid;
    }

    public void setTagUuid(UUID tagUuid) {
        this.tagUuid = tagUuid;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
