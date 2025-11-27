package view;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.CalendarEvent;
import model.EventStatus;


/**
 * Utility class for exporting calendar events to iCal format.
 * Design rationale:
 * - Follows iCal specification
 * - Generates Google Calendar compatible iCal files
 * - Handles timezone information properly
 * - Separates iCal logic from view implementation
 */
public final class IcalExporter {
  
  private static final DateTimeFormatter ICAL_DATETIME_FORMAT = DateTimeFormatter.ofPattern(
      "yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter ICAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  
  // Private constructor to prevent instantiation
  private IcalExporter() {
    throw new AssertionError("Utility class should not be instantiated");
  }
  
  /**
   * Exports events to iCal format file.
   *
   * @param events list of events to export
   * @param filename output filename
   * @param calendarName name of the calendar
   * @param timezone timezone of the calendar
   * @throws IOException if file writing fails
   */
  public static void exportToIcalFile(List<CalendarEvent> events, String filename,
                                     String calendarName, String timezone) throws IOException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write(generateIcalContent(events, calendarName, timezone));
    }
  }
  
  /**
   * Generates iCal content as a string.
   *
   * @param events list of events to export
   * @param calendarName name of the calendar
   * @param timezone timezone of the calendar
   * @return iCal formatted string
   */
  public static String generateIcalContent(List<CalendarEvent> events, String calendarName,
                                           String timezone) {
    StringBuilder ical = new StringBuilder();
    
    // iCal header
    ical.append("BEGIN:VCALENDAR\r\n");
    ical.append("VERSION:2.0\r\n");
    ical.append("PRODID:-//Calendar Application//Calendar 1.0//EN\r\n");
    ical.append("CALSCALE:GREGORIAN\r\n");
    ical.append("METHOD:PUBLISH\r\n");
    ical.append("X-WR-CALNAME:").append(escapeText(calendarName)).append("\r\n");
    ical.append("X-WR-TIMEZONE:").append(timezone).append("\r\n");
    
    // Add timezone definition
    ical.append("BEGIN:VTIMEZONE\r\n");
    ical.append("TZID:").append(timezone).append("\r\n");
    ical.append("END:VTIMEZONE\r\n");
    
    // Add events
    for (CalendarEvent event : events) {
      ical.append(generateEventComponent(event, timezone));
    }
    
    // iCal footer
    ical.append("END:VCALENDAR\r\n");
    
    return ical.toString();
  }
  
  /**
   * Generates a single VEVENT component.
   */
  private static String generateEventComponent(CalendarEvent event, String timezone) {
    StringBuilder vevent = new StringBuilder();
    
    vevent.append("BEGIN:VEVENT\r\n");

    String uid = generateUid(event);
    vevent.append("UID:").append(uid).append("\r\n");
    
    // DTSTART
    if (event.isAllDay()) {
      vevent.append("DTSTART;VALUE=DATE:").append(event.getStartDateTime()
          .format(ICAL_DATE_FORMAT)).append("\r\n");
    } else {
      vevent.append("DTSTART;TZID=").append(timezone).append(":").append(event.getStartDateTime()
          .format(ICAL_DATETIME_FORMAT)).append("\r\n");
    }
    
    // DTEND
    if (event.getEndDateTime() != null) {
      if (event.isAllDay()) {
        vevent.append("DTEND;VALUE=DATE:").append(event.getEndDateTime()
            .format(ICAL_DATE_FORMAT)).append("\r\n");
      } else {
        vevent.append("DTEND;TZID=").append(timezone).append(":").append(event.getEndDateTime()
            .format(ICAL_DATETIME_FORMAT)).append("\r\n");
      }
    }
    
    // SUMMARY (subject)
    vevent.append("SUMMARY:").append(escapeText(event.getSubject())).append("\r\n");
    
    // DESCRIPTION
    if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
      vevent.append("DESCRIPTION:").append(escapeText(event.getDescription())).append("\r\n");
    }
    
    // LOCATION
    if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
      vevent.append("LOCATION:").append(escapeText(event.getLocation())).append("\r\n");
    }
    
    // STATUS
    if (event.getStatus() != null) {
      String icalStatus = convertStatusToIcal(event.getStatus());
      vevent.append("STATUS:").append(icalStatus).append("\r\n");
    }
    
    // CREATED and LAST-MODIFIED (use current time)
    String now = LocalDateTime.now().format(ICAL_DATETIME_FORMAT) + "Z";
    vevent.append("CREATED:").append(now).append("\r\n");
    vevent.append("LAST-MODIFIED:").append(now).append("\r\n");
    
    vevent.append("END:VEVENT\r\n");
    
    return vevent.toString();
  }
  
  /**
   * Generates a unique identifier for an event.
   */
  private static String generateUid(CalendarEvent event) {
    // Create a simple UID based on event properties
    String base = event.getSubject() + event.getStartDateTime().toString();
    return base.hashCode() + "@calendar-app.local";
  }
  
  /**
   * Converts EventStatus to iCal status.
   * Since our EventStatus only has PUBLIC/PRIVATE, we map to iCal status appropriately.
   */
  private static String convertStatusToIcal(EventStatus status) {
    // For now, all events are CONFIRMED since we only have PUBLIC/PRIVATE status
    return "CONFIRMED";
  }
  
  /**
   * Escapes text for iCal format.
   * Escapes commas, semicolons, backslashes, and newlines.
   */
  private static String escapeText(String text) {
    if (text == null) {
      return "";
    }
    
    return text.replace("\\", "\\\\")
               .replace(",", "\\,")
               .replace(";", "\\;")
               .replace("\n", "\\n")
               .replace("\r", "");
  }
}