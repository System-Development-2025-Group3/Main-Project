package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ObservableSet;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CalendarHelper {

    public static void setupWeekCalendar(CalendarView calendarView) {
        setupCalendar(calendarView);
        calendarView.showWeekPage();
    }

    public static void setupMonthCalendar(CalendarView calendarView) {
        setupCalendar(calendarView);
        calendarView.showMonthPage();
    }

    private static void setupCalendar(CalendarView calendarView) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        calendarView.getCalendarSources().clear();
        CalendarSource source = new CalendarSource("Planify");
        calendarView.getCalendarSources().add(source);

        // load all your calendars + events exactly as before
        CalendarRepository      calRepo = new CalendarRepository();
        CalendarEventRepository evRepo  = new CalendarEventRepository();
        ExamEventRepository     exRepo  = new ExamEventRepository();

        try {
            List<CalendarModel> models = calRepo.findByUser(userId);
            for (CalendarModel cm : models) {
                Calendar fxCal = new Calendar(cm.getName());
                fxCal.setStyle(Calendar.Style.valueOf(cm.getStyle()));
                evRepo.findByCalendarId(cm.getId())
                        .forEach(e -> fxCal.addEntry(CalendarEventMapper.toEntry(e, fxCal)));
                exRepo.findByCalendarId(cm.getId())
                        .forEach(ex -> fxCal.addEntry(CalendarEventMapper.toEntry(ex, fxCal)));
                source.getCalendars().add(fxCal);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        applyStudyPreferences(calendarView);
    }

    public static void applyStudyPreferences(CalendarView calendarView) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        String sql = "SELECT start_time, end_time, blocked_days FROM study_preferences WHERE user_id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
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
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserCalendar(CalendarView calendarView) {
        // remember current page
        var page = calendarView.getSelectedPage();
        switch (page) {
            case DAY   -> setupWeekCalendar(calendarView);  // or call a dedicated setupDayCalendar if you make one
            case MONTH -> setupMonthCalendar(calendarView);
            default    -> setupWeekCalendar(calendarView);
        }
    }
}
