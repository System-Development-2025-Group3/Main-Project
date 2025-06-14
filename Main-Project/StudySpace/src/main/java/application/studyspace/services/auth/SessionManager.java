package application.studyspace.services.auth;

import java.util.Objects;
import java.util.UUID;

/**
 * SessionManager is a singleton that holds the current user's session.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private UUID loggedInUserId;

    // Private constructor
    private SessionManager() {}

    /**
     * @return the single SessionManager instance
     */
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Log a user in by their UUID.
     * @param userId the UUID of the user logging in (must not be null)
     */
    public void login(UUID userId) {
        this.loggedInUserId = Objects.requireNonNull(userId, "userId cannot be null");
    }

    /**
     * @return true if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return loggedInUserId != null;
    }

    /**
     * Fetches the currently logged-in user's ID.
     * @return the UUID of the logged-in user
     * @throws IllegalStateException if no user is logged in
     */
    public UUID getLoggedInUserId() {
        if (loggedInUserId == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }
        return loggedInUserId;
    }

    /**
     * Clears the current session (logs out).
     */
    public void logout() {
        loggedInUserId = null;
    }
}
