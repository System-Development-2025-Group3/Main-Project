package application.studyspace.services.calendar;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.UUIDHelper;
import application.studyspace.services.auth.SessionManager;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventMapper;
import application.studyspace.services.calendar.CalendarEventRepository;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ObservableSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class CalendarHelper {

    /**
     * Initializes the CalendarView for the current user: creates the calendar,
     * loads events, applies study-time limits and blocked days.
     * Returns the created Calendar instance.
     */
    public static Calendar setupUserCalendar(CalendarView calendarView) {
        // create and style calendar
        Calendar userCalendar = new Calendar("My Events");
        userCalendar.setStyle(Calendar.Style.STYLE6);

        // add to a source
        CalendarSource source = new CalendarSource("Planify");
        source.getCalendars().add(userCalendar);
        calendarView.getCalendarSources().add(source);

        // default view
        calendarView.showWeekPage();

        // load events
        loadEvents(userCalendar);

        // load and apply preferences
        applyStudyPreferences(calendarView);

        return userCalendar;
    }

    private static void loadEvents(Calendar userCalendar) {
        CalendarEventRepository repo = new CalendarEventRepository();
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        try {
            for (CalendarEvent e : repo.findByUser(userId)) {
                userCalendar.addEntry(CalendarEventMapper.toEntry(e, userCalendar));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // TODO: surface error to user if needed
        }
    }

    private static void applyStudyPreferences(CalendarView calendarView) {
        UUID userId = SessionManager.getInstance().getLoggedInUserId();
        String sql = """
            SELECT start_time, end_time, blocked_days
              FROM study_preferences
             WHERE user_id = ?
        """;

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBytes(1, UUIDHelper.uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime start = rs.getTime("start_time").toLocalTime();
                    LocalTime end   = rs.getTime("end_time").toLocalTime();
                    String blockedCsv = rs.getString("blocked_days");
                    applyCalendarLimits(calendarView, start, end, blockedCsv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: notify user of prefs load failure
        }
    }

    private static void applyCalendarLimits(CalendarView calendarView,
                                            LocalTime start,
                                            LocalTime end,
                                            String blockedDaysCsv) {
        // set visible hours
        calendarView.setStartTime(start);
        calendarView.setEndTime(end);

        // set blocked days
        ObservableSet<DayOfWeek> weekends = calendarView.getWeekendDays();
        weekends.clear();
        if (blockedDaysCsv != null && !blockedDaysCsv.isBlank()) {
            String[] days = blockedDaysCsv.split("\\s+");
            for (String d : days) {
                try {
                    DayOfWeek dow = DayOfWeek.valueOf(d.toUpperCase());
                    weekends.add(dow);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public static void updateUserCalendar(CalendarView calendarView) {
        calendarView.getCalendarSources().clear();
        setupUserCalendar(calendarView); // re-apply data
    }
}
