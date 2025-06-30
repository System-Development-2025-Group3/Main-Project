package application.studyspace.services.auth;

// Imports
import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.onboarding.StudyPreferences;
import com.calendarfx.view.CalendarView;

import java.util.Objects;
import java.util.UUID;
import java.util.prefs.Preferences;

/**
 * Singleton class to manage the current session state (logged-in user, preferences, etc.).
 */
public class SessionManager {

    // --- Login persistence keys ---
    private static final String PREF_NODE = "myapp/login";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";

    // --- In-memory session state ---
    private static final SessionManager INSTANCE = new SessionManager();

    // Currently logged in user UUID
    private UUID loggedInUserId;

    // The user's CalendarView reference (used across the app)
    private CalendarView userCalendar;

    // Whether to skip splash screen (can be loaded from DB and cached)
    private boolean skipSplashScreen = false;

    // Private constructor for singleton
    private SessionManager() {}

    /**
     * Returns the singleton instance.
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // --- Login session (in-memory) ---

    /**
     * Marks the session as logged in with the given userId.
     */
    public void login(UUID userId) {
        this.loggedInUserId = Objects.requireNonNull(userId, "userId cannot be null");
    }

    /**
     * Returns the logged-in user's UUID.
     * Throws if no user is logged in.
     */
    public UUID getLoggedInUserId() {
        if (loggedInUserId == null)
            throw new IllegalStateException("No user is currently logged in.");
        return loggedInUserId;
    }

    /**
     * Checks whether a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return loggedInUserId != null;
    }

    /**
     * Clears the session (logout).
     */
    public void logout() {
        loggedInUserId = null;
        userCalendar = null;
    }

    /**
     * Saves login credentials and the skipSplashScreen flag persistently.
     */
    public void saveLogin(String username, String token, boolean skipSplashScreen) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_TOKEN, token);
        prefs.putBoolean("SKIP_SPLASH_SCREEN", skipSplashScreen);
    }

    /**
     * Loads the skip splash screen flag from local preferences.
     * Called early on startup before login.
     */
    public boolean loadSkipSplashScreenPreferenceLocal() {
        Preferences prefs = Preferences.userRoot().node("StudySpace");
        return prefs.getBoolean("SKIP_SPLASH_SCREEN", false);
    }

    /**
     * Updates the local preference for skipping splash screen.
     */
    public void saveSkipSplashScreenPreferenceLocal(boolean skip) {
        Preferences prefs = Preferences.userRoot().node("StudySpace");
        prefs.putBoolean("SKIP_SPLASH_SCREEN", skip);
    }

    /**
     * Syncs skip splash preference from DB and caches locally.
     */
    public void syncSkipSplashScreenFromDb(UUID userId) {
        boolean skipSplash = StudyPreferences.getSkipSplashScreenPreference(userId);
        saveSkipSplashScreenPreferenceLocal(skipSplash);
    }

    /**
     * Retrieves the saved username from preferences.
     */
    public String getSavedUsername() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_USERNAME, null);
    }

    /**
     * Retrieves the saved token from preferences.
     */
    public String getSavedToken() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_TOKEN, null);
    }

    /**
     * Clears saved login credentials.
     */
    public void clearLogin() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_TOKEN);
    }

    /**
     * Whether the splash screen should be skipped (in-memory flag).
     */
    public boolean shouldSkipSplashScreen() {
        return skipSplashScreen;
    }

    /**
     * Sets whether to skip splash screen (in-memory flag).
     */
    public void setSkipSplashScreen(boolean skip) {
        this.skipSplashScreen = skip;
    }

    // --- Calendar session (in-memory reference) ---

    /**
     * Returns the user's CalendarView (for cross-controller access).
     */
    public CalendarView getUserCalendar() {
        return userCalendar;
    }

    /**
     * Sets the current user's CalendarView.
     */
    public void setUserCalendar(CalendarView calendarView) {
        this.userCalendar = calendarView;
    }
}
