package application.studyspace.services.auth;

import java.util.prefs.Preferences;

public class LoginSession {
    private static final String PREF_NODE = "myapp/login";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";

    public static void saveLogin(String username, String token) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_TOKEN, token);
    }

    public static String getSavedUsername() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_USERNAME, null);
    }

    public static String getSavedToken() {
        return Preferences.userRoot().node(PREF_NODE).get(KEY_TOKEN, null);
    }

    public static void clearLogin() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_TOKEN);
    }
}
