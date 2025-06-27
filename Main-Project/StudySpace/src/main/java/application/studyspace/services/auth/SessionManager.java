package application.studyspace.services.auth;

import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.onboarding.StudyPreferences;
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
    private boolean skipSplashScreen = false;


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

    public void saveLogin(String username, String token, boolean skipSplashScreen) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_TOKEN, token);
        prefs.putBoolean("SKIP_SPLASH_SCREEN", skipSplashScreen); // Save preference persistently
    }

    // Always called at app start, before login
    public boolean loadSkipSplashScreenPreferenceLocal() {
        Preferences prefs = Preferences.userRoot().node("StudySpace");
        return prefs.getBoolean("SKIP_SPLASH_SCREEN", false); // default: show splash
    }

    // Update local value
    public void saveSkipSplashScreenPreferenceLocal(boolean skip) {
        Preferences prefs = Preferences.userRoot().node("StudySpace");
        prefs.putBoolean("SKIP_SPLASH_SCREEN", skip);
    }

    // Call after successful login
    public void syncSkipSplashScreenFromDb(UUID userId) {
        boolean skipSplash = StudyPreferences.getSkipSplashScreenPreference(userId);
        saveSkipSplashScreenPreferenceLocal(skipSplash);
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

    public boolean shouldSkipSplashScreen() {
        return skipSplashScreen;
    }

    public void setSkipSplashScreen(boolean skip) {
        this.skipSplashScreen = skip;
    }


    // --- Calendar session (in-memory reference) ---
    public CalendarView getUserCalendar() {
        return userCalendar;
    }

    public void setUserCalendar(CalendarView calendarView) {
        this.userCalendar = calendarView;
    }
}
