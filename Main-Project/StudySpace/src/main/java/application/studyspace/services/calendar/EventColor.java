package application.studyspace.services.calendar;

/**
 * Defines the set of allowed event colors, each with a corresponding
 * CSS class name.  No inline styling is needed—just add the class to your node.
 */
public enum EventColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE;

    /**
     * Returns the CSS class you should add to a calendar‐event node
     * to get the correct pastel fill and border outline.
     * e.g. "event-red", "event-blue", etc.
     */
    public String getCssClass() {
        return "event-" + name().toLowerCase();
    }

    /**
     * Parse from the name stored in the DB, falling back to RED.
     */
    public static EventColor fromName(String name) {
        try {
            return EventColor.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return RED;
        }
    }
}
