package application.studyspace.services.onboarding;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventRepository;
import application.studyspace.services.calendar.CalendarRepository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper to import .ics (iCalendar) files into the app's Blockers calendar.
 */
public class CalendarImportHelper {

    private final UUID userUUID;

    /**
     * Creates a new importer instance bound to the given user.
     * @param userUUID the UUID of the logged-in user
     */
    public CalendarImportHelper(UUID userUUID) {
        this.userUUID = userUUID;
    }

    /**
     * Imports VEVENT entries from an .ics file.
     * For each event, it:
     * - extracts title, description, location, start, and end
     * - checks for duplicates (same title + start + end)
     * - saves non-duplicates to the Blockers calendar
     *
     * @param filePath the file system path to the .ics file
     * @return true if import succeeded (even if no events were added)
     */
    public boolean importFromFile(String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {

            // Use iCal4j to parse the file
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendarIcs = builder.build(in);

            // Get repositories for saving events
            CalendarEventRepository evtRepo = new CalendarEventRepository();
            CalendarRepository calRepo = new CalendarRepository();

            // Ensure the user has a Blockers calendar
            UUID blockerCalId = calRepo.getOrCreateBlockersCalendar(userUUID);

            int added = 0;
            int total = calendarIcs.getComponents(Component.VEVENT).size();

            // Iterate over all VEVENTs
            for (Object o : calendarIcs.getComponents(Component.VEVENT)) {
                VEvent v = (VEvent) o;

                // Read summary (title)
                Optional<Property> optSummary = v.getProperties(Property.SUMMARY).stream().findFirst();
                String summary = optSummary.map(Property::getValue).orElse("");

                // Read description
                Optional<Property> optDesc = v.getProperties(Property.DESCRIPTION).stream().findFirst();
                String description = optDesc.map(Property::getValue).orElse("");

                // Read location
                Optional<Property> optLoc = v.getProperties(Property.LOCATION).stream().findFirst();
                String location = optLoc.map(Property::getValue).orElse("");

                // Read start datetime
                Optional<Property> optDtStart = v.getProperties(Property.DTSTART).stream().findFirst();
                ZonedDateTime start = optDtStart
                        .filter(p -> p instanceof DtStart)
                        .map(p -> ((DtStart) p).getDate())
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                // Read end datetime
                Optional<Property> optDtEnd = v.getProperties(Property.DTEND).stream().findFirst();
                ZonedDateTime end = optDtEnd
                        .filter(p -> p instanceof DtEnd)
                        .map(p -> ((DtEnd) p).getDate())
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                // Skip if start or end is missing
                if (start == null || end == null) {
                    continue;
                }

                // Check if a matching event already exists
                if (!evtRepo.exists(userUUID, summary, start, end)) {
                    // No duplicate—create new CalendarEvent
                    CalendarEvent ev = new CalendarEvent(
                            userUUID,
                            summary,
                            description,
                            location,
                            start,
                            end
                    );
                    ev.setCalendarId(blockerCalId);

                    // Save to DB
                    evtRepo.save(ev);
                    added++;
                }
            }

            System.out.printf("Imported %d of %d events.%n", added, total);
            return true;

        } catch (Exception e) {
            System.err.println("Calendar import failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts an iCal4j date object to ZonedDateTime.
     * Handles:
     *   - LocalDate (all-day events)
     *   - java.util.Date
     *   - other TemporalAccessors
     *
     * @param dateObj the object returned by DtStart or DtEnd
     * @return ZonedDateTime equivalent
     */
    private static ZonedDateTime toZonedDateTime(Object dateObj) {
        if (dateObj instanceof TemporalAccessor) {
            if (dateObj instanceof LocalDate localDate) {
                // All-day event: start of day
                return localDate.atStartOfDay(ZoneId.systemDefault());
            }
            // Other temporal types
            return ZonedDateTime.from((TemporalAccessor) dateObj);

        } else if (dateObj instanceof java.util.Date) {
            return ZonedDateTime.ofInstant(((java.util.Date) dateObj).toInstant(), ZoneId.systemDefault());
        } else {
            throw new IllegalArgumentException("Unsupported date type: " + dateObj.getClass());
        }
    }
}
