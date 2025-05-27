package application.studyspace.services.onboarding;

import java.util.UUID;

public class CalendarImportHelper {

    private final UUID userUUID;

    public CalendarImportHelper(UUID userUUID) {
        this.userUUID = userUUID;
    }

    /**
     * Stub method — later you can pass in a file path or InputStream,
     * parse the calendar, and save events to the database.
     */
    public boolean importFromFile(String filePath) {
        // TODO: implement real import logic
        System.out.println("Importing calendar for user " + userUUID + " from " + filePath);
        return false;
    }
}
