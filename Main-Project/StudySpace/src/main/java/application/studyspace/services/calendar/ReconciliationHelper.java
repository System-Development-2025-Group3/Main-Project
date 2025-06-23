package application.studyspace.services.calendar;

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReconciliationHelper {

    /**
     * Walks a ±1‐week window around the view’s current date,
     * de‐duplicates entries by ID, and upserts them into the DB.
     */
    public static void reconcileWeek(CalendarView calendarView) throws SQLException {
        var calRepo = new CalendarEventRepository();
        var exRepo  = new ExamEventRepository();

        // center on whatever the user is looking at
        LocalDate center = calendarView.getDate();
        LocalDate from   = center.minusWeeks(1);
        LocalDate to     = center.plusWeeks(1);
        ZoneId   zone    = ZoneId.systemDefault();

        // keep track of which entry IDs we've already synced
        Set<String> seen = new HashSet<>();

        for (var source : calendarView.getCalendarSources()) {
            for (var fxCal : source.getCalendars()) {
                Map<LocalDate, List<Entry<?>>> all = fxCal.findEntries(from, to, zone);

                for (var dayEntries : all.values()) {
                    for (var entry : dayEntries) {
                        String id = entry.getId();
                        if (!seen.add(id)) {
                            // already handled this entry
                            continue;
                        }

                        Object userObj = entry.getUserObject();
                        if (userObj instanceof ExamEvent exam) {
                            exam.setStart(entry.getInterval().getStartZonedDateTime());
                            exam.setEnd(  entry.getInterval().getEndZonedDateTime());
                            // save() is INSERT ... ON DUPLICATE KEY UPDATE
                            ExamEventRepository.save(exam);

                        } else if (userObj instanceof CalendarEvent evt) {
                            evt.setStart(entry.getInterval().getStartZonedDateTime());
                            evt.setEnd(  entry.getInterval().getEndZonedDateTime());
                            // same upsert style
                            CalendarEventRepository.save(evt);
                        }
                    }
                }
            }
        }
    }
}