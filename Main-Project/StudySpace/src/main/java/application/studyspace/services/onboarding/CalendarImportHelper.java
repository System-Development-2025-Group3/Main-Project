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

public class CalendarImportHelper {

    private final UUID userUUID;

    public CalendarImportHelper(UUID userUUID) {
        this.userUUID = userUUID;
    }

    /**
     * Imports VEVENTs from an .ics file into the user’s Blockers calendar,
     * skipping duplicates.
     */
    public boolean importFromFile(String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            CalendarBuilder builder    = new CalendarBuilder();
            Calendar        calendarIcs = builder.build(in);

            CalendarEventRepository evtRepo     = new CalendarEventRepository();
            CalendarRepository      calRepo     = new CalendarRepository();
            UUID blockerCalId = calRepo.getOrCreateBlockersCalendar(userUUID);

            int added = 0;
            int total = calendarIcs.getComponents(Component.VEVENT).size();
            for (Object o : calendarIcs.getComponents(Component.VEVENT)) {
                VEvent v = (VEvent) o;

                // summary
                Optional<Property> optSummary = v.getProperties(Property.SUMMARY).stream().findFirst();
                String summary = optSummary.map(Property::getValue).orElse("");

                // description
                Optional<Property> optDesc = v.getProperties(Property.DESCRIPTION).stream().findFirst();
                String description = optDesc.map(Property::getValue).orElse("");

                // location
                Optional<Property> optLoc = v.getProperties(Property.LOCATION).stream().findFirst();
                String location = optLoc.map(Property::getValue).orElse("");

                // start
                Optional<Property> optDtStart = v.getProperties(Property.DTSTART).stream().findFirst();
                ZonedDateTime start = optDtStart
                        .filter(p -> p instanceof DtStart)
                        .map(p -> ((DtStart) p).getDate())
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                // end
                Optional<Property> optDtEnd = v.getProperties(Property.DTEND).stream().findFirst();
                ZonedDateTime end = optDtEnd
                        .filter(p -> p instanceof DtEnd)
                        .map(p -> ((DtEnd) p).getDate())
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                if (start == null || end == null) {
                    continue;
                }

                if (!evtRepo.exists(userUUID, summary, start, end)) {
                    CalendarEvent ev = new CalendarEvent(
                            userUUID,
                            summary,
                            description,
                            location,
                            start,
                            end
                    );
                    ev.setCalendarId(blockerCalId);
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

    private static ZonedDateTime toZonedDateTime(Object dateObj) {
        if (dateObj instanceof TemporalAccessor) {
            return ZonedDateTime.from((TemporalAccessor) dateObj);
        } else if (dateObj instanceof java.util.Date) {
            return ZonedDateTime.ofInstant(((java.util.Date) dateObj).toInstant(), ZoneId.systemDefault());
        } else {
            throw new IllegalArgumentException("Unsupported date type: " + dateObj.getClass());
        }
    }
}
