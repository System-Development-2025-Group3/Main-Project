package application.studyspace.services.calendar;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.time.ZoneId;
import java.util.UUID;

public class CalendarEventMapper {

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

    public static CalendarEvent fromEntry(Entry<CalendarEvent> entry) {
        CalendarEvent old = entry.getUserObject();

        return new CalendarEvent(
                // id and owner
                UUID.fromString(entry.getId()),
                old.getUserId(),

                // visible properties
                entry.getTitle(),
                old.getDescription(),            // pull from your domain object
                entry.getLocation(),

                // interval
                entry.getInterval().getStartZonedDateTime(),
                entry.getInterval().getEndZonedDateTime(),

                // flags & constraints
                entry.isFullDay(),
                entry.isHidden(),
                entry.getMinimumDuration(),

                // recurrence rule only
                entry.getRecurrenceRule(),

                // preserve original recurrence metadata & tag
                old.getRecurrenceSource(),
                old.getRecurrenceId(),
                old.getTagUuid()
        );
    }
}
