package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.DataBase.DataSourceManager;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helper for setting up CalendarView with user calendars, events, exams, and preferences.
 */
public class CalendarHelper {

    /**
     * Asynchronously loads and displays the week view for the given CalendarView.
     */
    public static void setupWeekCalendarAsync(CalendarView calendarView) {
        Task<Void> loader = new Task<>() {
            @Override
            protected Void call() throws Exception {
                setupCalendar(calendarView);
                return null;
            }
        };
        loader.setOnSucceeded(e -> calendarView.showWeekPage());
        loader.setOnFailed(e -> loader.getException().printStackTrace());
        new Thread(loader, "Calendar-Loader").start();
    }

    /**
     * Asynchronously loads and displays the month view for the given CalendarView.
     */
    public static void setupMonthCalendarAsync(CalendarView calendarView) {
        Task<Void> loader = new Task<>() {
            @Override
            protected Void call() throws Exception {
                setupCalendar(calendarView);
                return null;
            }
        };
        loader.setOnSucceeded(e -> calendarView.showMonthPage());
        loader.setOnFailed(e -> loader.getException().printStackTrace());
        new Thread(loader, "Calendar-Loader").start();
    }

    /**
     * Internal method: loads all calendars, events, exams, and preferences synchronously.
     */
    private static void setupCalendar(CalendarView calendarView) throws SQLException {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        // Clear existing sources
        calendarView.getCalendarSources().clear();

        // 1) Load calendar definitions
        List<CalendarModel> models = CalendarRepository.findByUser(userId);
        List<UUID> calIds = models.stream()
                .map(CalendarModel::getId)
                .collect(Collectors.toList());

        // 2) Batch-load events and exams
        Map<UUID,List<CalendarEvent>> eventsByCal =
                CalendarEventRepository.findByCalendarIds(calIds);
        Map<UUID,List<ExamEvent>> examsByCal =
                ExamEventRepository.findByCalendarIds(calIds);

        // 3) Build CalendarSource
        CalendarSource source = new CalendarSource("Planify");
        for (CalendarModel cm : models) {
            Calendar fxCal = new Calendar(cm.getName());
            fxCal.setStyle(Calendar.Style.valueOf(cm.getStyle()));

            // Add events
            eventsByCal.getOrDefault(cm.getId(), Collections.emptyList())
                    .forEach(e -> fxCal.addEntry(CalendarEventMapper.toEntry(e, fxCal)));

            // Add exams
            examsByCal.getOrDefault(cm.getId(), Collections.emptyList())
                    .forEach(ex -> fxCal.addEntry(CalendarEventMapper.toEntry(ex, fxCal)));

            source.getCalendars().add(fxCal);
        }
        calendarView.getCalendarSources().add(source);

        // 4) Apply user study preferences
        applyStudyPreferences(calendarView);
    }

    /**
     * Applies the user's study preferences (start/end times and blocked days) to the CalendarView.
     */
    public static void applyStudyPreferences(CalendarView calendarView) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        String sql = "SELECT start_time, end_time, blocked_days FROM study_preferences WHERE user_id = ?";

        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime start   = rs.getTime("start_time").toLocalTime();
                    LocalTime end     = rs.getTime("end_time").toLocalTime();
                    String blockedCsv = rs.getString("blocked_days");

                    calendarView.setStartTime(start);
                    calendarView.setEndTime(end);

                    ObservableSet<DayOfWeek> blockedDays = calendarView.getWeekendDays();
                    blockedDays.clear();
                    if (blockedCsv != null && !blockedCsv.isBlank()) {
                        for (String d : blockedCsv.split("\\s*,\\s*")) {
                            try {
                                blockedDays.add(DayOfWeek.valueOf(d));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the CalendarView based on the currently selected page (day/week/month).
     */
    public static void updateUserCalendarAsync(CalendarView calendarView) {
        switch (calendarView.getSelectedPage()) {
            case DAY:
            case WEEK:
                setupWeekCalendarAsync(calendarView);
                break;
            case MONTH:
                setupMonthCalendarAsync(calendarView);
                break;
            default:
                setupWeekCalendarAsync(calendarView);
                break;
        }
    }
}