package application.studyspace.services.auth;

import java.util.prefs.Preferences;
import java.util.UUID;

public class RememberMeHelper {
    private static final String PREF_NODE = "StudySpace";
    private static final String UUID_KEY = "REMEMBERED_USER_UUID";

    // Save the UUID to local preferences
    public static void saveRememberedUserUUID(UUID uuid) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(UUID_KEY, uuid.toString());
    }

    // Get the UUID from local preferences
    public static UUID getRememberedUserUUID() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String uuidString = prefs.get(UUID_KEY, null);
        return uuidString == null ? null : UUID.fromString(uuidString);
    }

    // Remove the UUID (on logout or “forget me”)
    public static void clearRememberedUserUUID() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(UUID_KEY);
    }
}
