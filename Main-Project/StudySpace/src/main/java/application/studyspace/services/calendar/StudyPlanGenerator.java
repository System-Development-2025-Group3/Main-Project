package application.studyspace.services.calendar;

import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;

import java.sql.SQLException;
import java.time.*;
import java.util.*;

public class StudyPlanGenerator {

    // Helper: Checks if a slot overlaps a calendar event
    private static boolean overlaps(LocalDateTime slot, int length, CalendarEvent ev) {
        LocalDateTime end = slot.plusMinutes(length);
        LocalDateTime es  = ev.getStart().toLocalDateTime();
        LocalDateTime ee  = ev.getEnd().toLocalDateTime();
        return !end.isBefore(es) && !slot.isAfter(ee);
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

        // For ALL exams, process one by one
        // Maintain a master occupied list as you go
        List<CalendarEvent> occupied = new ArrayList<>(blockerEvents);
        // Map each date to exams on that day
        Map<LocalDate, Set<UUID>> examDateToCalendarIds = new HashMap<>();

        for (CalendarModel examCal : examCals) {
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0);
            LocalDate examDate = exam.getEnd().toLocalDate();
            examDateToCalendarIds.computeIfAbsent(examDate, d -> new HashSet<>()).add(examCal.getId());
        }

        for (CalendarModel examCal : examCals) {
            // 1. Clear only this calendar's own old sessions
            var events = CalendarEventRepository.findByCalendarId(examCal.getId());
            for (CalendarEvent ev : events) {
                if (ev.getTitle().contains(" Session ") || ev.getTitle().endsWith(" Practice")) {
                    CalendarEventRepository.delete(ev.getId());
                }
            }
            // After deletion, remove them from occupied
            occupied.removeIf(ev -> ev.getCalendarId().equals(examCal.getId()) &&
                    (ev.getTitle().contains(" Session ") || ev.getTitle().endsWith(" Practice")));

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

            List<LocalDateTime> slots = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (prefs.getBlockedDays().contains(date.getDayOfWeek())) continue;

                // Prevent scheduling sessions for this exam the day before a different exam
                LocalDate tomorrow = date.plusDays(1);
                Set<UUID> tomorrowExams = examDateToCalendarIds.getOrDefault(tomorrow, Collections.emptySet());
                if (!tomorrowExams.isEmpty() && !tomorrowExams.contains(examCal.getId())) {
                    // There is at least one exam tomorrow, and it is NOT this exam → skip today for this exam
                    continue;
                }

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
                    // Check for overlap with any existing event in occupied
                    for (CalendarEvent ev : occupied) {
                        if (overlaps(sessionStart, sessionLen, ev)) {
                            fits = false;
                            break;
                        }
                    }
                    if (!fits) break;
                }
                if (fits) validGroupStarts.add(candidateStart);
            }
            slots = validGroupStarts;
            System.out.println("[DEBUG] slots after full-group window fit: " + slots.size());
            // -------------------------------------------------------------------------------

            int numGroups = needed / groupSize + ((needed % groupSize == 0) ? 0 : 1);
            int sessionCreated = 0;

            // Make a copy to avoid ConcurrentModification if you need to use slots elsewhere
            List<LocalDateTime> availableGroupStarts = new ArrayList<>(slots);

            for (int g = 0; g < numGroups; g++) {
                if (availableGroupStarts.isEmpty()) break; // no more starts available

                // Distribute evenly
                int idx = (numGroups == 1)
                        ? 0
                        : (int) Math.round(g * (availableGroupStarts.size() - 1) / (double)(numGroups - 1));
                LocalDateTime groupStart = availableGroupStarts.get(idx);

                // Remove the used slot so it can't be re-used!
                availableGroupStarts.remove(idx);

                // Number of sessions in this group (handle last group which may be smaller)
                int sessionsThisGroup = Math.min(groupSize, needed - sessionCreated);

                for (int s = 0; s < sessionsThisGroup; s++) {
                    int sessionNum = sessionCreated + 1;
                    String title = exam.getTitle() + " Session " + sessionNum;

                    LocalDateTime sessionStart = groupStart.plusMinutes(
                            (sessionLen + breakLen) * s
                    );

                    // Final overlap check before creating the event, just to be safe
                    boolean hasOverlap = false;
                    for (CalendarEvent ev : occupied) {
                        if (overlaps(sessionStart, sessionLen, ev)) {
                            hasOverlap = true;
                            break;
                        }
                    }
                    if (hasOverlap) continue;

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

                    // Immediately add to occupied
                    occupied.add(se);

                    sessionCreated++;
                }
            }
        }
        System.out.println("[DEBUG] generateStudyPlan completed.");
    }
}
