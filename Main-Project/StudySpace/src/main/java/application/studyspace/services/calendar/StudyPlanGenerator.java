// ── src/main/java/application/studyspace/services/calendar/StudyPlanGenerator.java ──
package application.studyspace.services.calendar;

import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.onboarding.StudyPreferences;

import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class StudyPlanGenerator {

    // Builds a list of allowed time slots based on user preferences.
    private static List<LocalDateTime> buildAllowedSlots(
            StudyPreferences prefs,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalTime windowStart = prefs.getStartTime();
        LocalTime windowEnd   = prefs.getEndTime();
        Set<DayOfWeek> blocked = prefs.getBlockedDays();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (blocked.contains(date.getDayOfWeek())) continue;
            LocalDateTime a = LocalDateTime.of(date, windowStart);
            LocalDateTime b = LocalDateTime.of(date, windowEnd);
            if (b.isAfter(a)) {
                slots.add(a);
            }
        }
        return slots;
    }

    // Computes the number of study sessions needed for each exam based on its topics.
    public static Map<ExamEvent, Integer> calculateSessionCounts(UUID calendarId)
            throws SQLException {
        List<ExamEvent> exams = ExamEventRepository
                .findByCalendarId(calendarId);

        return exams.stream()
                .sorted(Comparator.comparingInt(ExamEvent::getPriority).reversed())
                .collect(Collectors.toMap(
                        exam -> exam,
                        exam -> exam.getNumberOfTopics() + 2,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Called by your OnboardingPage3Controller.generateStudyPlan().
     * 1) builds allowed time‐slots from prefs
     * 2) computes session counts per exam
     * (future steps: subtract busy slots, allocate sessions, persist them)
     */
    public static void generateStudyPlan(
            UUID userId,
            UUID examCalendarId,
            StudyPreferences prefs
    ) throws SQLException {

        List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCalendarId);
        if (exams.isEmpty()) return;
        LocalDate today = LocalDate.now();
        // Finde das späteste Enddatum aller Exams in diesem Kalender
        LocalDate lastExamDate = exams.stream()
                .map(e -> e.getEnd().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(today.plusWeeks(4));


        List<LocalDateTime> allowedSlots = buildAllowedSlots(prefs, today, lastExamDate);

        Map<ExamEvent, Integer> sessionsByExam =
                calculateSessionCounts(examCalendarId);

        sessionsByExam.forEach((exam, count) ->
                System.out.printf("Exam '%s' needs %d sessions%n",
                        exam.getTitle(), count)
        );

        // next: take 'allowedSlots', subtract any blocker/exam events,
        // pick the first N slots for each exam in priority order,
        // name them "Session 1…Session N-2"/"Practice", and save them.
    }

    public static void generateStudyPlan(UUID userId, StudyPreferences prefs) throws SQLException {
        // Hole alle Kalender des Nutzers
        List<CalendarModel> calendars = CalendarRepository.findByUser(userId);
        // Finde den Blocker-Kalender (angenommen: Name enthält "blocker" oder Style ist speziell)
        CalendarModel blockerCal = calendars.stream()
            .filter(cm -> cm.getName().toLowerCase().contains("blocker"))
            .findFirst().orElse(null);
        // Alle anderen Kalender sind Exam-Kalender
        List<CalendarModel> examCals = calendars.stream()
            .filter(cm -> blockerCal == null || !cm.getId().equals(blockerCal.getId()))
            .collect(Collectors.toList());
        // Hole alle Blocker-Events
        List<CalendarEvent> blockerEvents = blockerCal != null ? CalendarEventRepository.findByCalendarId(blockerCal.getId()) : List.of();
        // Für jeden Exam-Kalender
        for (CalendarModel examCal : examCals) {
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0); // nur 1 pro Kalender
            int topics = exam.getNumberOfTopics();
            int minsPerTopic = exam.getMinutesPerTopic();
            Duration totalStudy = Duration.ofMinutes((long) topics * minsPerTopic);
            int sessionLength = prefs.getSessionLength();
            int breakLength = prefs.getBreakLength();
            Set<DayOfWeek> blockedDays = prefs.getBlockedDays();
            LocalTime startTime = prefs.getStartTime();
            LocalTime endTime = prefs.getEndTime();
            LocalDate from = LocalDate.now();
            LocalDate to = exam.getEnd().toLocalDate();
            // Erzeuge alle möglichen Slots
            List<LocalDateTime> slots = new ArrayList<>();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (blockedDays.contains(date.getDayOfWeek())) continue;
                LocalDateTime slotStart = LocalDateTime.of(date, startTime);
                while (slotStart.plusMinutes(sessionLength).isBefore(LocalDateTime.of(date, endTime).plusSeconds(1))) {
                    slots.add(slotStart);
                    slotStart = slotStart.plusMinutes(sessionLength + breakLength);
                }
            }
            // Filtere Slots, die mit Blockern oder anderen Exams kollidieren
            List<CalendarEvent> otherExamEvents = new ArrayList<>();
            for (CalendarModel otherCal : examCals) {
                if (!otherCal.getId().equals(examCal.getId())) {
                    List<ExamEvent> otherExams = ExamEventRepository.findByCalendarId(otherCal.getId());
                    otherExamEvents.addAll(otherExams);
                }
            }
            List<LocalDateTime> freeSlots = slots.stream().filter(slot ->
                blockerEvents.stream().noneMatch(ev -> overlaps(slot, sessionLength, ev)) &&
                otherExamEvents.stream().noneMatch(ev -> overlaps(slot, sessionLength, ev))
            ).collect(Collectors.toList());
            // Wie viele Sessions?
            int neededSessions = (int) Math.ceil((double) totalStudy.toMinutes() / sessionLength);
            // Erzeuge und speichere die Study Sessions
            for (int i = 0; i < neededSessions && i < freeSlots.size(); i++) {
                String title = (i >= neededSessions - 2) ? exam.getTitle() + " Practice" : exam.getTitle() + " Session " + (i + 1);
                LocalDateTime start = freeSlots.get(i);
                LocalDateTime end = start.plusMinutes(sessionLength);
                CalendarEvent studyEvent = new CalendarEvent(
                    userId,
                    title,
                    "Study for " + exam.getTitle(),
                    "",
                    start.atZone(ZoneId.systemDefault()),
                    end.atZone(ZoneId.systemDefault())
                );
                studyEvent.setCalendarId(examCal.getId());
                new CalendarEventRepository().save(studyEvent);
            }
        }
    }
    public static void generateStudyPlan(UUID userId) throws SQLException {
        StudyPreferences prefs = StudyPreferences.load(userId);
        // Hole alle Kalender des Nutzers
        List<CalendarModel> calendars = CalendarRepository.findByUser(userId);
        // Finde den Blocker-Kalender (angenommen: Name enthält "blocker" oder Style ist speziell)
        CalendarModel blockerCal = calendars.stream()
            .filter(cm -> cm.getName().toLowerCase().contains("blocker"))
            .findFirst().orElse(null);
        // Alle anderen Kalender sind Exam-Kalender
        List<CalendarModel> examCals = calendars.stream()
            .filter(cm -> blockerCal == null || !cm.getId().equals(blockerCal.getId()))
            .collect(Collectors.toList());
        // Hole alle Blocker-Events
        List<CalendarEvent> blockerEvents = blockerCal != null ? CalendarEventRepository.findByCalendarId(blockerCal.getId()) : List.of();
        // Für jeden Exam-Kalender
        for (CalendarModel examCal : examCals) {
            List<ExamEvent> exams = ExamEventRepository.findByCalendarId(examCal.getId());
            if (exams.isEmpty()) continue;
            ExamEvent exam = exams.get(0); // nur 1 pro Kalender
            int topics = exam.getNumberOfTopics();
            int minsPerTopic = exam.getMinutesPerTopic();
            Duration totalStudy = Duration.ofMinutes((long) topics * minsPerTopic);
            int sessionLength = prefs.getSessionLength();
            int breakLength = prefs.getBreakLength();
            Set<DayOfWeek> blockedDays = prefs.getBlockedDays();
            LocalTime startTime = prefs.getStartTime();
            LocalTime endTime = prefs.getEndTime();
            LocalDate from = LocalDate.now();
            LocalDate to = exam.getEnd().toLocalDate();
            // Erzeuge alle möglichen Slots
            List<LocalDateTime> slots = new ArrayList<>();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (blockedDays.contains(date.getDayOfWeek())) continue;
                LocalDateTime slotStart = LocalDateTime.of(date, startTime);
                while (slotStart.plusMinutes(sessionLength).isBefore(LocalDateTime.of(date, endTime).plusSeconds(1))) {
                    slots.add(slotStart);
                    slotStart = slotStart.plusMinutes(sessionLength + breakLength);
                }
            }
            // Filtere Slots, die mit Blockern oder anderen Exams kollidieren
            List<CalendarEvent> otherExamEvents = new ArrayList<>();
            for (CalendarModel otherCal : examCals) {
                if (!otherCal.getId().equals(examCal.getId())) {
                    List<ExamEvent> otherExams = ExamEventRepository.findByCalendarId(otherCal.getId());
                    otherExamEvents.addAll(otherExams);
                }
            }
            List<LocalDateTime> freeSlots = slots.stream().filter(slot ->
                blockerEvents.stream().noneMatch(ev -> overlaps(slot, sessionLength, ev)) &&
                otherExamEvents.stream().noneMatch(ev -> overlaps(slot, sessionLength, ev))
            ).collect(Collectors.toList());
            // Wie viele Sessions?
            int neededSessions = (int) Math.ceil((double) totalStudy.toMinutes() / sessionLength);
            // Erzeuge und speichere die Study Sessions
            for (int i = 0; i < neededSessions && i < freeSlots.size(); i++) {
                String title = (i >= neededSessions - 2) ? exam.getTitle() + " Practice" : exam.getTitle() + " Session " + (i + 1);
                LocalDateTime start = freeSlots.get(i);
                LocalDateTime end = start.plusMinutes(sessionLength);
                CalendarEvent session = new CalendarEvent(
                    userId,
                    title,
                    "Study for " + exam.getTitle(),
                    "",
                    start.atZone(ZoneId.systemDefault()),
                    end.atZone(ZoneId.systemDefault())
                );
                session.setCalendarId(examCal.getId());
                new CalendarEventRepository().save(session);
            }
        }
    }
    // Prüft, ob ein Slot mit einem Event kollidiert
    private static boolean overlaps(LocalDateTime slot, int length, CalendarEvent ev) {
        LocalDateTime slotEnd = slot.plusMinutes(length);
        LocalDateTime evStart = ev.getStart().toLocalDateTime();
        LocalDateTime evEnd = ev.getEnd().toLocalDateTime();
        return !slotEnd.isBefore(evStart) && !slot.isAfter(evEnd);
    }
}
