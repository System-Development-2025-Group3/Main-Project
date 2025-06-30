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
     * Generates study sessions across all exam calendars for the user,
     * distributing them evenly into available time slots.
     *
     * Process:
     * - For each exam calendar:
     *   - Remove old future sessions (so we can regenerate)
     *   - Compute how many sessions are needed based on topics × minutes/topic
     *   - Build all possible free slots between now and the exam date
     *   - Filter slots that can fit a full group of sessions
     *   - Distribute sessions evenly over the slots
     */
    public static void generateStudyPlan(UUID userId) throws SQLException {
        System.out.println("[DEBUG] generateStudyPlan for userId=" + userId);

        // Load the user's study preferences (start/end times, session length, break length)
        StudyPreferences prefs = StudyPreferences.load(userId);
        System.out.println("[DEBUG] Loaded StudyPreferences: sessionLength=" + prefs.getSessionLength());

        // Load all calendars for the user
        List<CalendarModel> calendars = CalendarRepository.findByUser(userId);

        // Identify the Blockers calendar (contains general time blockers)
        CalendarModel blockerCal = calendars.stream()
                .filter(cm -> cm.getName().toLowerCase().contains("blocker"))
                .findFirst().orElse(null);

        // All other calendars are considered exam calendars
        List<CalendarModel> examCals = calendars.stream()
                .filter(cm -> blockerCal == null || !cm.getId().equals(blockerCal.getId()))
                .toList();

        // Load all blocking events (appointments, blockers)
        List<CalendarEvent> blockerEvents = blockerCal != null
                ? CalendarEventRepository.findByCalendarId(blockerCal.getId())
                : List.of();

        // Keep a master list of all occupied timeslots across all exams and blockers
        List<CalendarEvent> occupied = new ArrayList<>(blockerEvents);

        // Map to track which exam(s) occur on which date (used for avoiding studying the day before another exam)
        Map<LocalDate, Set<UUID>> examDateToCalendarIds = new HashMap<>();
        for (CalendarModel examCal : examCals) {
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0);
            LocalDate examDate = exam.getEnd().toLocalDate();
            examDateToCalendarIds.computeIfAbsent(examDate, d -> new HashSet<>()).add(examCal.getId());
        }

        // Process each exam calendar individually
        for (CalendarModel examCal : examCals) {
            // 1. Clear any old future study sessions in this calendar
            var events = CalendarEventRepository.findByCalendarId(examCal.getId());
            ZonedDateTime now = ZonedDateTime.now();
            for (CalendarEvent ev : events) {
                boolean isStudySession = ev.getTitle().contains(" Session ") || ev.getTitle().endsWith(" Practice");
                boolean isInFuture = ev.getEnd().isAfter(now);
                if (isStudySession && !ev.isCompleted() && isInFuture) {
                    CalendarEventRepository.delete(ev.getId());
                }
            }
            occupied.removeIf(ev -> ev.getCalendarId().equals(examCal.getId()) &&
                    (ev.getTitle().contains(" Session ") || ev.getTitle().endsWith(" Practice")));

            System.out.println("[DEBUG] Scheduling for exam calendar " + examCal.getId());

            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0);

            // Compute total study time required
            int sessionLen = prefs.getSessionLength();
            int breakLen   = prefs.getBreakLength();
            Duration totalStudy = Duration.ofMinutes((long) exam.getNumberOfTopics() * exam.getMinutesPerTopic());
            System.out.println("[DEBUG] totalStudyMinutes=" + totalStudy.toMinutes());

            // The scheduling window: today to the exam date
            LocalDate from = LocalDate.now();
            LocalDate to   = exam.getEnd().toLocalDate();

            // Build list of other exams (to enforce buffer after their end)
            List<ExamEvent> otherExamEvents = new ArrayList<>();
            for (CalendarModel otherCal : examCals) {
                if (!otherCal.getId().equals(examCal.getId())) {
                    otherExamEvents.addAll(ExamEventRepository.findByCalendarId(otherCal.getId()));
                }
            }

            // Skip exam day itself
            LocalDate thisExamDate = exam.getStart().toLocalDate();

            // Build a list of candidate slots
            List<LocalDateTime> slots = new ArrayList<>();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (prefs.getBlockedDays().contains(date.getDayOfWeek())) continue;

                // Don't schedule the day before another exam (unless it's this exam)
                LocalDate tomorrow = date.plusDays(1);
                Set<UUID> tomorrowExams = examDateToCalendarIds.getOrDefault(tomorrow, Collections.emptySet());
                if (!tomorrowExams.isEmpty() && !tomorrowExams.contains(examCal.getId())) {
                    continue;
                }

                if (date.equals(thisExamDate)) continue;

                LocalTime windowStart = prefs.getStartTime();
                LocalTime windowEnd   = prefs.getEndTime();

                // For today: shift start time to now if needed
                if (date.equals(now.toLocalDate()) && now.toLocalTime().isAfter(windowStart)) {
                    windowStart = now.toLocalTime();
                }

                // Enforce 1-hour buffer after other exams end
                for (ExamEvent other : otherExamEvents) {
                    if (date.equals(other.getEnd().toLocalDate())) {
                        LocalTime afterOther = other.getEnd().toLocalDateTime()
                                .plusHours(1).toLocalTime();
                        if (windowStart.isBefore(afterOther)) {
                            windowStart = afterOther;
                        }
                    }
                }

                // Generate slots in this day
                LocalDateTime slotStart = LocalDateTime.of(date, windowStart);
                LocalDateTime dayEnd = LocalDateTime.of(date, windowEnd);
                while (!slotStart.isAfter(dayEnd.minusMinutes(sessionLen))) {
                    LocalDateTime sessionEnd = slotStart.plusMinutes(sessionLen);
                    if (sessionEnd.isAfter(dayEnd)) break;

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

            // Compute how many sessions we need
            int needed = (int) Math.ceil((double) totalStudy.toMinutes() / sessionLen);
            if (needed <= 0 || slots.isEmpty()) continue;
            needed = Math.min(needed, slots.size());
            System.out.println("[DEBUG] sessions needed after clamp: " + needed);

            // Decide group size: how many sessions in a row per block
            int sessionPlusBreak = sessionLen + breakLen;
            int groupSize;
            if (sessionPlusBreak < 30) {
                groupSize = 4;
            } else if (sessionPlusBreak < 60) {
                groupSize = 3;
            } else {
                groupSize = 2;
            }

            // --- FILTER: Only slots that can fit a full group of sessions without overlaps ---
            List<LocalDateTime> validGroupStarts = new ArrayList<>();
            for (LocalDateTime candidateStart : slots) {
                boolean fits = true;
                for (int s = 0; s < groupSize; s++) {
                    LocalDateTime sessionStart = candidateStart.plusMinutes((sessionLen + breakLen) * s);
                    LocalDateTime sessionEnd = sessionStart.plusMinutes(sessionLen);
                    LocalDateTime allowedEnd = LocalDateTime.of(sessionStart.toLocalDate(), prefs.getEndTime());
                    if (sessionEnd.isAfter(allowedEnd)) {
                        fits = false;
                        break;
                    }
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

            // Number of groups needed
            int numGroups = needed / groupSize + ((needed % groupSize == 0) ? 0 : 1);
            int sessionCreated = 0;

            List<LocalDateTime> availableGroupStarts = new ArrayList<>(slots);

            // Distribute groups evenly across available slots
            for (int g = 0; g < numGroups; g++) {
                if (availableGroupStarts.isEmpty()) break;

                // Pick index proportionally to spread out groups
                int idx = (numGroups == 1)
                        ? 0
                        : (int) Math.round(g * (availableGroupStarts.size() - 1) / (double)(numGroups - 1));
                LocalDateTime groupStart = availableGroupStarts.get(idx);
                availableGroupStarts.remove(idx);

                int sessionsThisGroup = Math.min(groupSize, needed - sessionCreated);

                for (int s = 0; s < sessionsThisGroup; s++) {
                    int sessionNum = sessionCreated + 1;
                    String title = exam.getTitle() + " Session " + sessionNum;

                    LocalDateTime sessionStart = groupStart.plusMinutes((sessionLen + breakLen) * s);

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

                    occupied.add(se);
                    sessionCreated++;
                }
            }
        }
        System.out.println("[DEBUG] generateStudyPlan completed.");
    }

    /**
     * Reschedules a single study session to the next available free slot within 30 days.
     */
    public static void rescheduleOneSession(CalendarEvent session) throws SQLException {
        UUID userId = session.getUserId();

        StudyPreferences prefs = StudyPreferences.load(userId);

        List<CalendarModel> calendars = CalendarRepository.findByUser(userId);
        List<CalendarEvent> occupied = new ArrayList<>();
        for (CalendarModel cal : calendars) {
            occupied.addAll(CalendarEventRepository.findByCalendarId(cal.getId()));
        }

        occupied.removeIf(e -> e.getId().equals(session.getId()));

        int sessionLen = prefs.getSessionLength();
        int breakLen   = prefs.getBreakLength();

        LocalDateTime now = LocalDateTime.now();

        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate date = now.toLocalDate().plusDays(dayOffset);
            if (prefs.getBlockedDays().contains(date.getDayOfWeek())) continue;

            LocalTime windowStart = prefs.getStartTime();
            LocalTime windowEnd   = prefs.getEndTime();
            if (dayOffset == 0 && now.toLocalTime().isAfter(windowStart)) {
                windowStart = now.toLocalTime();
            }

            LocalDateTime slotStart = LocalDateTime.of(date, windowStart);
            LocalDateTime dayEnd = LocalDateTime.of(date, windowEnd);

            while (!slotStart.isAfter(dayEnd.minusMinutes(sessionLen))) {
                boolean hasOverlap = false;
                for (CalendarEvent ev : occupied) {
                    if (overlaps(slotStart, sessionLen, ev)) {
                        hasOverlap = true;
                        break;
                    }
                }
                if (!hasOverlap) {
                    ZonedDateTime zStart = slotStart.atZone(ZoneId.systemDefault());
                    ZonedDateTime zEnd   = zStart.plusMinutes(sessionLen);

                    session.setStart(zStart);
                    session.setEnd(zEnd);

                    CalendarEventRepository.save(session);

                    System.out.println("[DEBUG] Rescheduled session to " + zStart);
                    return;
                }
                slotStart = slotStart.plusMinutes(sessionLen + breakLen);
            }
        }

        throw new SQLException("Could not find an available slot in the next 30 days to reschedule.");
    }
}
