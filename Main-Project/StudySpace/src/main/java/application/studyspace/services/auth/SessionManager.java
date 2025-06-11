package application.studyspace.services.auth;

import java.util.UUID;

import application.studyspace.services.auth.SessionManager;

/**
 * SessionManager is a singleton class that manages the session state of the application.
 * It holds the logged-in user's information and provides global access to it.
 */
public class SessionManager {
    private static SessionManager instance;

    // Logged-in user ID (e.g., UUID from the database)
    private UUID loggedInUserId;

    // Private constructor to prevent instantiation
    private SessionManager() {}

    /**
     * Get the single instance of the SessionManager.
     *
     * @return the single instance of SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the logged-in user's ID.
     *
     * @param userId the UUID of the currently logged-in user.
     */
    public void setLoggedInUserId(UUID userId) {
        this.loggedInUserId = userId;
    }

    /**
     * Gets the logged-in user's ID.
     *
     * @return the UUID of the currently logged-in user.
     */
    public UUID getLoggedInUserId() {
        return loggedInUserId;
    }

    /**
     * Clears the session, typically used during logout.
     */
    public void clearSession() {
        loggedInUserId = null;
    }
}