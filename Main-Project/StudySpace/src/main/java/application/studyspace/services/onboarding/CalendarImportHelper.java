package application.studyspace.services.onboarding;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.DtEnd;
import application.studyspace.services.calendar.CalendarEvent;
import application.studyspace.services.calendar.CalendarEventRepository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

public class CalendarImportHelper {

    private final UUID userUUID;

    public CalendarImportHelper(UUID userUUID) {
        this.userUUID = userUUID;
    }

    /**
     * Parses the .ics at filePath, maps each VEVENT into a CalendarEvent,
     * and saves it using CalendarEventRepository.
     */
    public boolean importFromFile(String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            System.out.println("📥 Starting calendar import: " + filePath);

            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(in);

            int totalEvents = calendar.getComponents(Component.VEVENT).size();
            System.out.println("🔍 Events found in file: " + totalEvents);

            CalendarEventRepository repo = new CalendarEventRepository();
            int savedCount = 0;

            for (Object obj : calendar.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) obj;

                String summary     = event.getProperty(Property.SUMMARY)
                        .map(Property::getValue)
                        .orElse("");
                String description = event.getProperty(Property.DESCRIPTION)
                        .map(Property::getValue)
                        .orElse("");
                String location    = event.getProperty(Property.LOCATION)
                        .map(Property::getValue)
                        .orElse("");

                ZonedDateTime start = event.getProperty(Property.DTSTART)
                        .map(p -> (DtStart) p)
                        .map(DtStart::getDate)
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                ZonedDateTime end = event.getProperty(Property.DTEND)
                        .map(p -> (DtEnd) p)
                        .map(DtEnd::getDate)
                        .map(CalendarImportHelper::toZonedDateTime)
                        .orElse(null);

                CalendarEvent ev = new CalendarEvent(
                        userUUID,
                        summary,
                        description,
                        location,
                        start,
                        end
                );

                repo.save(ev);
                savedCount++;
            }

            System.out.println("✅ Successfully added " + savedCount + " of " + totalEvents + " event(s) to the database");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Calendar import failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static ZonedDateTime toZonedDateTime(Object value) {
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toZonedDateTime();
        } else if (value instanceof java.util.Date) {
            return ZonedDateTime.ofInstant(((java.util.Date) value).toInstant(), ZoneId.systemDefault());
        } else if (value instanceof TemporalAccessor) {
            return ZonedDateTime.from((TemporalAccessor) value);
        } else {
            throw new IllegalArgumentException("Unsupported temporal type: " + value.getClass());
        }
    }
}
