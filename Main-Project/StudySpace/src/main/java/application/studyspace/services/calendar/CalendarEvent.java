package application.studyspace.services.calendar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

public class CalendarEvent {

    private final UUID id;
    private final UUID userId;
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

    // constructor for new events (calendarId to be set via setter)
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
    }

    // full constructor (used when loading from DB—set calendarId via setter afterward)
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
                         UUID tagUuid) {
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
    }

    // ---- New calendarId accessor methods ----

    public UUID getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(UUID calendarId) {
        this.calendarId = calendarId;
    }

    // ---- existing getters/setters ----

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
}
