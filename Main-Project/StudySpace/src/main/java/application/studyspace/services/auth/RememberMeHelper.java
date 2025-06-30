package application.studyspace.services.auth;

import java.util.prefs.Preferences;
import java.util.UUID;

/**
 * Utility class for persisting a remembered user UUID across app launches.
 * Uses the Java Preferences API to store the UUID locally.
 */
public class RememberMeHelper {

    // The preferences node where we save data
    private static final String PREF_NODE = "StudySpace";
    private static final String UUID_KEY = "REMEMBERED_USER_UUID";

    /**
     * Saves the given UUID to local preferences for auto-login.
     *
     * @param uuid the user UUID to remember
     */
    public static void saveRememberedUserUUID(UUID uuid) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(UUID_KEY, uuid.toString());
    }

    /**
     * Retrieves the remembered UUID from preferences, if any.
     *
     * @return the stored UUID, or null if none exists
     */
    public static UUID getRememberedUserUUID() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String uuidString = prefs.get(UUID_KEY, null);
        return uuidString == null ? null : UUID.fromString(uuidString);
    }

    /**
     * Clears the remembered UUID (e.g., on logout).
     */
    public static void clearRememberedUserUUID() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(UUID_KEY);
    }
}
