package application.studyspace.services.calendar;

import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;

import java.sql.SQLException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class StudyPlanGenerator {

    // Builds a list of allowed time slots based on user preferences.
    private static List<LocalDateTime> buildAllowedSlots(
            StudyPreferences prefs,
            LocalDate startDate,
            LocalDate endDate
    ) {
        System.out.println("[DEBUG] buildAllowedSlots from " + startDate + " to " + endDate);
        List<LocalDateTime> slots = new ArrayList<>();
        LocalTime windowStart = prefs.getStartTime();
        LocalTime windowEnd   = prefs.getEndTime();
        Set<DayOfWeek> blocked = prefs.getBlockedDays();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (blocked.contains(date.getDayOfWeek())) continue;
            LocalDateTime a = LocalDateTime.of(date, windowStart);
            LocalDateTime b = LocalDateTime.of(date, windowEnd);
            if (b.isAfter(a)) slots.add(a);
        }
        System.out.println("[DEBUG] total allowed slots: " + slots.size());
        return slots;
    }

    /**
     * Regenerates study sessions across all exam calendars, evenly distributing by available free slots.
     */
    public static void generateStudyPlan(UUID userId) throws SQLException {
        System.out.println("[DEBUG] generateStudyPlan for userId=" + userId);
        StudyPreferences prefs = StudyPreferences.load(userId);
        System.out.println("[DEBUG] Loaded StudyPreferences: sessionLength=" + prefs.getSessionLength());

        List<CalendarModel> calendars = CalendarRepository.findByUser(userId);
        CalendarModel blockerCal = calendars.stream()
                .filter(cm -> cm.getName().toLowerCase().contains("blocker"))
                .findFirst().orElse(null);
        List<CalendarModel> examCals = calendars.stream()
                .filter(cm -> blockerCal == null || !cm.getId().equals(blockerCal.getId()))
                .toList();

        List<CalendarEvent> blockerEvents = blockerCal != null
                ? CalendarEventRepository.findByCalendarId(blockerCal.getId())
                : List.of();

        // Clear old sessions
        for (CalendarModel cal : examCals) {
            var events = CalendarEventRepository.findByCalendarId(cal.getId());
            for (CalendarEvent ev : events) {
                if (ev.getTitle().contains(" Session ") || ev.getTitle().endsWith(" Practice")) {
                    CalendarEventRepository.delete(ev.getId());
                }
            }
        }

        // Gather occupied events
        List<CalendarEvent> occupied = new ArrayList<>(blockerEvents);
        for (CalendarModel cal : examCals) {
            occupied.addAll(CalendarEventRepository.findByCalendarId(cal.getId()));
        }

        for (CalendarModel examCal : examCals) {
            System.out.println("[DEBUG] Scheduling for exam calendar " + examCal.getId());
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0);

            int sessionLen = prefs.getSessionLength();
            int breakLen   = prefs.getBreakLength();
            Duration totalStudy = Duration.ofMinutes((long) exam.getNumberOfTopics() * exam.getMinutesPerTopic());
            System.out.println("[DEBUG] totalStudyMinutes=" + totalStudy.toMinutes());

            LocalDate from = LocalDate.now();
            LocalDate to   = exam.getEnd().toLocalDate();

            // Build and filter slots with multiple exam-time constraints
            List<LocalDateTime> slots = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // Gather other exams (for inter-exam buffers)
            List<ExamEvent> otherExamEvents = new ArrayList<>();
            for (CalendarModel otherCal : examCals) {
                if (!otherCal.getId().equals(examCal.getId())) {
                    otherExamEvents.addAll(
                            ExamEventRepository.findByCalendarId(otherCal.getId())
                    );
                }
            }

            // Own exam date/time
            LocalDateTime thisExamStart = exam.getStart().toLocalDateTime();
            LocalDateTime thisExamEnd   = exam.getEnd().toLocalDateTime();
            LocalDate thisExamDate      = thisExamStart.toLocalDate();

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (prefs.getBlockedDays().contains(date.getDayOfWeek())) continue;

                // Skip own exam day entirely
                if (date.equals(thisExamDate)) continue;

                LocalTime windowStart = prefs.getStartTime();
                LocalTime windowEnd   = prefs.getEndTime();

                // Today: don’t schedule in the past
                if (date.equals(now.toLocalDate()) && now.toLocalTime().isAfter(windowStart)) {
                    windowStart = now.toLocalTime();
                }

                // Other exams: enforce 1h buffer after their end
                for (ExamEvent other : otherExamEvents) {
                    LocalDate otherDate = other.getEnd().toLocalDate();
                    if (date.equals(otherDate)) {
                        LocalTime afterOther = other.getEnd().toLocalDateTime()
                                .plusHours(1).toLocalTime();
                        if (windowStart.isBefore(afterOther)) {
                            windowStart = afterOther;
                        }
                    }
                }

                LocalDateTime slotStart = LocalDateTime.of(date, windowStart);
                LocalDateTime dayEnd    = LocalDateTime.of(date, windowEnd);
                while (!slotStart.isAfter(dayEnd.minusMinutes(sessionLen))) {
                    LocalDateTime sessionEnd = slotStart.plusMinutes(sessionLen);
                    if (sessionEnd.isAfter(dayEnd)) {
                        break;
                    }
                    boolean hasOverlap = false;
                    for (CalendarEvent ev : occupied) {
                        if (overlaps(slotStart, sessionLen, ev)) {
                            hasOverlap = true;
                            break;
                        }
                    }
                    if (!hasOverlap) {
                        slots.add(slotStart);
                    }
                    slotStart = slotStart.plusMinutes(sessionLen + breakLen);
                }
            }
            System.out.println("[DEBUG] possible slots after time and exam constraints: " + slots.size());

            int needed = (int) Math.ceil((double) totalStudy.toMinutes() / sessionLen);
            if (needed <= 0 || slots.isEmpty()) continue;
            needed = Math.min(needed, slots.size());
            System.out.println("[DEBUG] sessions needed after clamp: " + needed);

            // Dynamically determine group size
            int sessionPlusBreak = sessionLen + breakLen;
            int groupSize;
            if (sessionPlusBreak < 30) {
                groupSize = 4;
            } else if (sessionPlusBreak < 60) {
                groupSize = 3;
            } else {
                groupSize = 2;
            }

            // ---- FILTER: Only allow group starts that keep the *entire* block in bounds ----
            List<LocalDateTime> validGroupStarts = new ArrayList<>();
            for (LocalDateTime candidateStart : slots) {
                boolean fits = true;
                for (int s = 0; s < groupSize; s++) {
                    LocalDateTime sessionStart = candidateStart.plusMinutes((sessionLen + breakLen) * s);
                    LocalDateTime sessionEnd   = sessionStart.plusMinutes(sessionLen);
                    // Find day's allowed window end for this session's date
                    LocalDate sessionDate = sessionStart.toLocalDate();
                    LocalTime allowedEnd = prefs.getEndTime();
                    LocalDateTime allowedEndDateTime = LocalDateTime.of(sessionDate, allowedEnd);

                    if (sessionEnd.isAfter(allowedEndDateTime)) {
                        fits = false;
                        break;
                    }
                }
                if (fits) validGroupStarts.add(candidateStart);
            }
            slots = validGroupStarts;
            System.out.println("[DEBUG] slots after full-group window fit: " + slots.size());
            // -------------------------------------------------------------------------------

            int numGroups = needed / groupSize + ((needed % groupSize == 0) ? 0 : 1);
            int slotCount = slots.size();
            int sessionCreated = 0;

            for (int g = 0; g < numGroups; g++) {
                // Evenly distribute groups across available slots
                int idx = (numGroups == 1)
                        ? 0
                        : (int) Math.round(g * (slotCount - 1) / (double)(numGroups - 1));
                LocalDateTime groupStart = slots.get(idx);

                // Number of sessions in this group (handle last group which may be smaller)
                int sessionsThisGroup = Math.min(groupSize, needed - sessionCreated);

                for (int s = 0; s < sessionsThisGroup; s++) {
                    int sessionNum = sessionCreated + 1;
                    String title = (sessionNum >= needed - 2)
                            ? exam.getTitle() + " Practice"
                            : exam.getTitle() + " Session " + sessionNum;

                    LocalDateTime sessionStart = groupStart.plusMinutes(
                            (sessionLen + breakLen) * s
                    );

                    System.out.println("[DEBUG] Creating session " + title + " at " + sessionStart);

                    CalendarEvent se = new CalendarEvent(
                            userId,
                            title,
                            "Study for " + exam.getTitle(),
                            "",
                            sessionStart.atZone(ZoneId.systemDefault()),
                            sessionStart.plusMinutes(sessionLen).atZone(ZoneId.systemDefault())
                    );
                    se.setCalendarId(examCal.getId());
                    CalendarEventRepository.save(se);

                    sessionCreated++;
                }
            }
        }
        System.out.println("[DEBUG] generateStudyPlan completed.");
    }



    private static boolean overlaps(LocalDateTime slot, int length, CalendarEvent ev) {
        LocalDateTime end = slot.plusMinutes(length);
        LocalDateTime es  = ev.getStart().toLocalDateTime();
        LocalDateTime ee  = ev.getEnd().toLocalDateTime();
        return !end.isBefore(es) && !slot.isAfter(ee);
    }
}
