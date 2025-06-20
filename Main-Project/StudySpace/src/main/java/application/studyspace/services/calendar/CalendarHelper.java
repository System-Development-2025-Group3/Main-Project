package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;
import com.calendarfx.model.Calendar;            // FX Calendar
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ObservableSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CalendarHelper {

    /**
     * Initializes the CalendarView for the current user:
     * loads all calendars and their events, then applies study-time limits and blocked days.
     */
    public static void setupUserCalendar(CalendarView calendarView) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();

        // clear any existing calendars
        calendarView.getCalendarSources().clear();

        // one source to hold all user calendars
        CalendarSource source = new CalendarSource("Planify");
        calendarView.getCalendarSources().add(source);

        // repositories
        CalendarRepository      calRepo = new CalendarRepository();
        CalendarEventRepository evRepo  = new CalendarEventRepository();
        ExamEventRepository     exRepo  = ExamEventRepository.getInstance();

        try {
            // fetch the list of service‐side CalendarModel objects
            List<CalendarModel> calendarModels = calRepo.findByUser(userId);

            for (CalendarModel cm : calendarModels) {
                // build a CalendarFX Calendar per model
                Calendar fxCal = new Calendar(cm.getName());
                fxCal.setStyle(Calendar.Style.valueOf(cm.getStyle()));

                // load generic calendar_events (blockers & study sessions)
                for (CalendarEvent e : evRepo.findByCalendarId(cm.getId())) {
                    fxCal.addEntry(CalendarEventMapper.toEntry(e, fxCal));
                }

                // load exam_events
                for (ExamEvent ex : exRepo.findByCalendarId(cm.getId())) {
                    fxCal.addEntry(CalendarEventMapper.toEntry(ex, fxCal));
                }

                source.getCalendars().add(fxCal);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // default to week view
        calendarView.showWeekPage();

        // apply study preferences (visible hours + blocked days)
        applyStudyPreferences(calendarView);
    }

    /**
     * Applies the current user's study-time limits (start/end) and blocked days
     * to the given CalendarView.
     */
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
                        for (String d : blockedCsv.split("\\s+")) {
                            try {
                                blockedDays.add(DayOfWeek.valueOf(d.toUpperCase()));
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

    /** Clears and re-initializes the user's calendar view, including prefs. */
    public static void updateUserCalendar(CalendarView calendarView) {
        setupUserCalendar(calendarView);
    }
}
