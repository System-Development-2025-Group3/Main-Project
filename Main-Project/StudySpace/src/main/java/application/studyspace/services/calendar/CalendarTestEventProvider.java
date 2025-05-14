package application.studyspace.services.calendar;

import java.time.LocalDateTime;
import java.util.List;

public class CalendarTestEventProvider {

    public static List<CalendarEvent> getTestEvents() {
        return List.of(
                new CalendarEvent("Math Study", "Algebra review",
                        LocalDateTime.of(2025, 5, 14, 10, 0),
                        LocalDateTime.of(2025, 5, 14, 11, 0),
                        "#c2e7ff"
                ),
                new CalendarEvent("Chemistry Group", "Chapter 4 discussion",
                        LocalDateTime.of(2025, 5, 15, 14, 0),
                        LocalDateTime.of(2025, 5, 15, 16, 0),
                        "#ffd6d6"
                ),
                new CalendarEvent("Exam Prep", "Mock test",
                        LocalDateTime.of(2025, 5, 16, 9, 0),
                        LocalDateTime.of(2025, 5, 16, 10, 0),
                        "#d4f4dd"
                )
        );
    }
}
