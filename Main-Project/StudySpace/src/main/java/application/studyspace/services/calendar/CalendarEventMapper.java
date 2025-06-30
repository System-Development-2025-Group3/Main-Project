package application.studyspace.services.calendar;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

/**
 * Utility class for mapping between your domain model events (CalendarEvent, ExamEvent)
 * and CalendarFX Entry objects for use in the JavaFX calendar UI.
 *
 * <p>
 * Provides methods to convert to and from CalendarFX entries, so your
 * domain events can be displayed, edited, and synchronized with the visual calendar.
 * </p>
 */
public class CalendarEventMapper {

    /**
     * Maps a generic CalendarEvent to a CalendarFX Entry.
     *
     * @param e          the CalendarEvent from your domain model
     * @param fxCalendar the target CalendarFX Calendar to attach the entry to
     * @return a CalendarFX Entry representing the event
     */
    public static Entry<CalendarEvent> toEntry(CalendarEvent e, Calendar fxCalendar) {
        Entry<CalendarEvent> entry = new Entry<>(e.getTitle());
        entry.setId(e.getId().toString());
        entry.setUserObject(e);
        entry.setCalendar(fxCalendar);

        // Set start and end times
        entry.setInterval(
                e.getStart().toLocalDate(),
                e.getStart().toLocalTime(),
                e.getEnd().toLocalDate(),
                e.getEnd().toLocalTime()
        );

        // Basic flags
        entry.setFullDay(e.isFullDay());
        entry.setHidden(e.isHidden());
        entry.setLocation(e.getLocation());

        // Optional recurrence rule
        if (e.getRecurrenceRule() != null) {
            entry.setRecurrenceRule(e.getRecurrenceRule());
        }

        // Optional minimum duration
        if (e.getMinDuration() != null) {
            entry.setMinimumDuration(e.getMinDuration());
        }

        return entry;
    }

    /**
     * Maps an ExamEvent to a CalendarFX Entry, showing only the title.
     *
     * @param e          the ExamEvent from your domain model
     * @param fxCalendar the target CalendarFX Calendar to attach the entry to
     * @return a CalendarFX Entry representing the exam
     */
    public static Entry<ExamEvent> toEntry(ExamEvent e, Calendar fxCalendar) {
        Entry<ExamEvent> entry = new Entry<>(e.getTitle());
        entry.setId(e.getId().toString());
        entry.setUserObject(e);
        entry.setCalendar(fxCalendar);

        // Set start and end times
        entry.setInterval(
                e.getStart().toLocalDate(),
                e.getStart().toLocalTime(),
                e.getEnd().toLocalDate(),
                e.getEnd().toLocalTime()
        );

        entry.setFullDay(false);
        entry.setLocation(e.getLocation());

        // Title can be updated here if needed
        entry.setTitle(e.getTitle());

        System.out.println("[CalendarEventMapper] Mapped ExamEvent to Entry: " + e.getId());
        return entry;
    }

    /**
     * Reconstructs a CalendarEvent from a CalendarFX Entry.
     *
     * @param entry the CalendarFX Entry to convert
     * @return a CalendarEvent domain object with equivalent data
     */
    public static CalendarEvent fromEntry(Entry<CalendarEvent> entry) {
        CalendarEvent old = entry.getUserObject();
        if (old == null) {
            System.out.println("[CalendarEventMapper] Error: Entry has no user object attached.");
            return null;
        }
        return new CalendarEvent(
                java.util.UUID.fromString(entry.getId()),
                old.getUserId(),
                entry.getTitle(),
                old.getDescription(),
                entry.getLocation(),
                entry.getInterval().getStartZonedDateTime(),
                entry.getInterval().getEndZonedDateTime(),
                entry.isFullDay(),
                entry.isHidden(),
                entry.getMinimumDuration(),
                entry.getRecurrenceRule(),
                old.getRecurrenceSource(),
                old.getRecurrenceId(),
                old.getTagUuid(),
                old.isCompleted()
        );
    }
}
