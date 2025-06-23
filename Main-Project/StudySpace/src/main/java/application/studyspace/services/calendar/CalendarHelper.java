package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.DataBase.DataSourceManager;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helper for asynchronously loading and updating a CalendarView
 * without blocking the FX application thread.
 */
public class CalendarHelper {

    /**
     * Kick off an async refresh of whichever page (week/month) is selected.
     */
    public static void updateUserCalendarAsync(CalendarView calendarView) {
        switch (calendarView.getSelectedPage()) {
            case DAY, WEEK -> setupWeekCalendarAsync(calendarView);
            case MONTH    -> setupMonthCalendarAsync(calendarView);
            default       -> setupWeekCalendarAsync(calendarView);
        }
    }

    /**
     * Asynchronously loads and displays the week view.
     */
    public static void setupWeekCalendarAsync(CalendarView calendarView) {
        new Thread(createLoader(calendarView, ViewType.WEEK), "Calendar-Loader").start();
    }

    /**
     * Asynchronously loads and displays the month view.
     */
    public static void setupMonthCalendarAsync(CalendarView calendarView) {
        new Thread(createLoader(calendarView, ViewType.MONTH), "Calendar-Loader").start();
    }

    // Internal shared loader factory
    private static Runnable createLoader(CalendarView calendarView, ViewType viewType) {
        return () -> {
            try {
                // Phase 1: load data off FX thread
                UUID userId = SessionManager.getInstance().getLoggedInUserId();
                List<CalendarModel> models = CalendarRepository.findByUser(userId);
                var ids = models.stream().map(CalendarModel::getId).collect(Collectors.toList());
                Map<UUID, List<CalendarEvent>> eventsByCal =
                        CalendarEventRepository.findByCalendarIds(ids);
                Map<UUID, List<ExamEvent>> examsByCal =
                        ExamEventRepository.findByCalendarIds(ids);

                LoadedCalendars data = new LoadedCalendars(models, eventsByCal, examsByCal);

                // Phase 2: apply on FX thread
                javafx.application.Platform.runLater(() -> {
                    calendarView.getCalendarSources().clear();
                    CalendarSource src = new CalendarSource("Planify");
                    for (CalendarModel cm : data.models) {
                        Calendar fxCal = new Calendar(cm.getName());
                        fxCal.setStyle(Calendar.Style.valueOf(cm.getStyle()));
                        data.eventsByCal.getOrDefault(cm.getId(), Collections.emptyList())
                                .forEach(e -> fxCal.addEntry(CalendarEventMapper.toEntry(e, fxCal)));
                        data.examsByCal.getOrDefault(cm.getId(), Collections.emptyList())
                                .forEach(ex -> fxCal.addEntry(CalendarEventMapper.toEntry(ex, fxCal)));
                        src.getCalendars().add(fxCal);
                    }
                    calendarView.getCalendarSources().add(src);
                    if (viewType == ViewType.MONTH) {
                        calendarView.showMonthPage();
                    } else {
                        calendarView.showWeekPage();
                    }
                    applyStudyPreferences(calendarView);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
    }

    /**
     * Applies the user's study preferences (start/end times and blocked days).
     */
    public static void applyStudyPreferences(CalendarView calendarView) {
        String sql = "SELECT start_time, end_time, blocked_days FROM study_preferences WHERE user_id = ?";
        try (Connection conn = DataSourceManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(SessionManager.getInstance().getLoggedInUserId()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime start = rs.getTime("start_time").toLocalTime();
                    LocalTime end   = rs.getTime("end_time").toLocalTime();
                    calendarView.setStartTime(start);
                    calendarView.setEndTime(end);

                    var blockedDays = calendarView.getWeekendDays();
                    blockedDays.clear();
                    String csv = rs.getString("blocked_days");
                    if (csv != null && !csv.isBlank()) {
                        for (String d : csv.split(",")) {
                            try {
                                blockedDays.add(DayOfWeek.valueOf(d.trim()));
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Simple enum to distinguish views
    private enum ViewType { WEEK, MONTH }

    // DTO for passing loaded data
    private static class LoadedCalendars {
        final List<CalendarModel> models;
        final Map<UUID, List<CalendarEvent>> eventsByCal;
        final Map<UUID, List<ExamEvent>> examsByCal;

        LoadedCalendars(List<CalendarModel> models,
                        Map<UUID, List<CalendarEvent>> eventsByCal,
                        Map<UUID, List<ExamEvent>> examsByCal) {
            this.models = models;
            this.eventsByCal = eventsByCal;
            this.examsByCal = examsByCal;
        }
    }
}