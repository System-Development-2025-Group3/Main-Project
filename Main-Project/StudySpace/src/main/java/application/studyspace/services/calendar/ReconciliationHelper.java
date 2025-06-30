package application.studyspace.services.calendar;

import application.studyspace.services.auth.SessionManager;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ReconciliationHelper {

    /**
     * Reconciles all calendar events and exams between the UI and the database:
     * - Adds new entries from the UI to the DB
     * - Updates changed entries
     * - Removes from the DB any events/exams deleted in the UI
     * - Removes empty calendars
     * - Adds any new events created via CalendarFX UI (userObject == null)
     */
    public static void reconcile(CalendarView calendarView) throws SQLException {
        var calRepo = new CalendarEventRepository();
        var exRepo  = new ExamEventRepository();

        LocalDate center = calendarView.getDate();
        LocalDate from   = center.minusWeeks(1);
        LocalDate to     = center.plusWeeks(1);
        ZoneId   zone    = ZoneId.systemDefault();

        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        // --- Build calendar name -> id map for current user
        Map<String, UUID> calNameToId = new HashMap<>();
        for (var calModel : CalendarRepository.findByUser(userId)) {
            calNameToId.put(calModel.getName(), calModel.getId());
        }

        // --- Gather all entries currently visible in the UI ---
        Set<String> uiEventIds = new HashSet<>();
        Set<String> uiExamIds  = new HashSet<>();

        for (var source : calendarView.getCalendarSources()) {
            for (var fxCal : source.getCalendars()) {
                Map<LocalDate, List<Entry<?>>> all = fxCal.findEntries(from, to, zone);
                for (var dayEntries : all.values()) {
                    for (var entry : dayEntries) {
                        Object userObj = entry.getUserObject();
                        if (userObj instanceof ExamEvent exam) {
                            uiExamIds.add(exam.getId().toString());
                        } else if (userObj instanceof CalendarEvent evt) {
                            uiEventIds.add(evt.getId().toString());
                        }
                    }
                }
            }
        }

        // --- Gather all events/exams from DB in the same window ---
        Set<String> dbEventIds = new HashSet<>();
        Set<String> dbExamIds  = new HashSet<>();

        for (var calModel : CalendarRepository.findByUser(userId)) {
            List<CalendarEvent> events = CalendarEventRepository.findByCalendarId(calModel.getId());
            for (var evt : events) {
                LocalDate evtDate = evt.getStart().toLocalDate();
                if (!evtDate.isBefore(from) && !evtDate.isAfter(to)) {
                    dbEventIds.add(evt.getId().toString());
                }
            }
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(calModel.getId());
            for (var exam : exams) {
                LocalDate examDate = exam.getStart().toLocalDate();
                if (!examDate.isBefore(from) && !examDate.isAfter(to)) {
                    dbExamIds.add(exam.getId().toString());
                }
            }
        }

        // --- Delete from DB anything that was removed in the UI ---
        for (String id : dbEventIds) {
            if (!uiEventIds.contains(id)) {
                CalendarEventRepository.delete(UUID.fromString(id));
            }
        }
        for (String id : dbExamIds) {
            if (!uiExamIds.contains(id)) {
                ExamEventRepository.deleteExamAndSessions(UUID.fromString(id));
            }
        }

        // --- Add or update all UI entries (now handles CalendarFX-created entries too) ---
        Set<String> seen = new HashSet<>();
        for (var source : calendarView.getCalendarSources()) {
            for (var fxCal : source.getCalendars()) {
                UUID calendarId = calNameToId.get(fxCal.getName()); // match by name
                Map<LocalDate, List<Entry<?>>> all = fxCal.findEntries(from, to, zone);
                for (var dayEntries : all.values()) {
                    for (var entry : dayEntries) {
                        String id = entry.getId();
                        if (!seen.add(id)) continue; // avoid double-processing
                        Object userObj = entry.getUserObject();

                        if (userObj == null) {
                            String title = entry.getTitle();
                            String desc = "";
                            String loc   = entry.getLocation() != null ? entry.getLocation() : "";

                            // Always a CalendarEvent, assigned to the calendarId
                            CalendarEvent session = new CalendarEvent(
                                    userId,
                                    title,
                                    desc,
                                    loc,
                                    entry.getInterval().getStartZonedDateTime(),
                                    entry.getInterval().getEndZonedDateTime()
                            );
                            session.setCalendarId(calendarId);
                            CalendarEventRepository.save(session);
                            ((com.calendarfx.model.Entry<Object>) entry).setUserObject(session);
                            continue;
                        }

                        if (userObj instanceof ExamEvent exam) {
                            exam.setStart(entry.getInterval().getStartZonedDateTime());
                            exam.setEnd(entry.getInterval().getEndZonedDateTime());
                            ExamEventRepository.save(exam);
                        } else if (userObj instanceof CalendarEvent evt) {
                            evt.setStart(entry.getInterval().getStartZonedDateTime());
                            evt.setEnd(entry.getInterval().getEndZonedDateTime());
                            CalendarEventRepository.saveWithoutTouchingCompleted(evt);
                        }
                    }
                }
            }
        }

        // --- Delete any calendars that are now empty ---
        for (var calModel : CalendarRepository.findByUser(userId)) {
            List<CalendarEvent> events = CalendarEventRepository.findByCalendarId(calModel.getId());
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(calModel.getId());
            if (events.isEmpty() && exams.isEmpty()) {
                CalendarRepository.deleteCalendar(calModel.getId());
            }
        }
    }
}
