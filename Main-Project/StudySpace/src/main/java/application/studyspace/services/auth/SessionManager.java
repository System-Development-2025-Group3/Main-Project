package application.studyspace.services.auth;

import com.calendarfx.view.CalendarView;

import java.util.Objects;
import java.util.UUID;
import java.util.prefs.Preferences;

public class SessionManager {

    // --- Login persistence keys ---
    private static final String PREF_NODE = "myapp/login";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";

    // --- In-memory session state ---
    private static final SessionManager INSTANCE = new SessionManager();

    private UUID loggedInUserId;
    private CalendarView userCalendar;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // --- Login session (in-memory) ---
    public void login(UUID userId) {
        this.loggedInUserId = Objects.requireNonNull(userId, "userId cannot be null");
    }

    public UUID getLoggedInUserId() {
        if (loggedInUserId == null)
            throw new IllegalStateException("No user is currently logged in.");
        return loggedInUserId;
    }

    public boolean isLoggedIn() {
        return loggedInUserId != null;
    }

    public void logout() {
        loggedInUserId = null;
        userCalendar = null;
    }

    // --- Persistent login (Preferences) ---
    public void saveLogin(String username, String token) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_TOKEN, token);
    }

    public String getSavedUsername() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_USERNAME, null);
    }

    public String getSavedToken() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_TOKEN, null);
    }

    public void clearLogin() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_TOKEN);
    }

    // --- Calendar session (in-memory reference) ---
    public CalendarView getUserCalendar() {
        return userCalendar;
    }

    public void setUserCalendar(CalendarView calendarView) {
        this.userCalendar = calendarView;
    }
}
