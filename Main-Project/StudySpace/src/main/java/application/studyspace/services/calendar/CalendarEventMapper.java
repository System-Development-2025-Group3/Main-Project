package application.studyspace.services.calendar;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.util.logging.Logger;

public class CalendarEventMapper {

    private static final Logger logger = Logger.getLogger(CalendarEventMapper.class.getName());

    /** Maps a generic CalendarEvent to a CalendarFX Entry. */
    public static Entry<CalendarEvent> toEntry(CalendarEvent e, Calendar fxCalendar) {
        Entry<CalendarEvent> entry = new Entry<>(e.getTitle());
        entry.setId(e.getId().toString());
        entry.setUserObject(e);
        entry.setCalendar(fxCalendar);

        entry.setInterval(
                e.getStart().toLocalDate(),
                e.getStart().toLocalTime(),
                e.getEnd().toLocalDate(),
                e.getEnd().toLocalTime()
        );
        entry.setFullDay(e.isFullDay());
        entry.setHidden(e.isHidden());
        entry.setLocation(e.getLocation());

        if (e.getRecurrenceRule() != null) {
            entry.setRecurrenceRule(e.getRecurrenceRule());
        }
        if (e.getMinDuration() != null) {
            entry.setMinimumDuration(e.getMinDuration());
        }

        return entry;
    }

    /** Maps an ExamEvent to a CalendarFX Entry, showing only the title. */
    public static Entry<ExamEvent> toEntry(ExamEvent e, Calendar fxCalendar) {
        Entry<ExamEvent> entry = new Entry<>(e.getTitle());
        entry.setId(e.getId().toString());
        entry.setUserObject(e);
        entry.setCalendar(fxCalendar);

        entry.setInterval(
                e.getStart().toLocalDate(),
                e.getStart().toLocalTime(),
                e.getEnd().toLocalDate(),
                e.getEnd().toLocalTime()
        );
        entry.setFullDay(false);
        entry.setLocation(e.getLocation());

        entry.setTitle(e.getTitle());

        logger.info("Mapped ExamEvent to Entry: " + e.getId());
        return entry;
    }

    /** Reconstructs a CalendarEvent from a CalendarFX Entry. */
    public static CalendarEvent fromEntry(Entry<CalendarEvent> entry) {
        CalendarEvent old = entry.getUserObject();
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
                old.getTagUuid()
        );
    }
}
