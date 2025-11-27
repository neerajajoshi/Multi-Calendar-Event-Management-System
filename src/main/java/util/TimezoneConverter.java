package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Objects;

/**
 * Utility class for timezone conversions and time handling.
 * Design rationale:
 * - Centralized timezone conversion logic
 * - Handles conversion between different calendar timezones
 * - Supports both LocalDateTime and ZonedDateTime operations
 * - Thread-safe utility methods
 */
public final class TimezoneConverter {
  

  private TimezoneConverter() {
    throw new AssertionError("Utility class should not be instantiated");
  }
  
  /**
   * Converts a LocalDateTime from one timezone to another.
   *
   * @param dateTime the datetime to convert
   * @param fromTimezone source timezone
   * @param toTimezone target timezone
   * @return converted datetime in target timezone
   */
  public static LocalDateTime convertTimezone(LocalDateTime dateTime, String fromTimezone,
                                                String toTimezone) {
    Objects.requireNonNull(dateTime, "DateTime cannot be null");
    Objects.requireNonNull(fromTimezone, "From timezone cannot be null");
    Objects.requireNonNull(toTimezone, "To timezone cannot be null");
    
    if (fromTimezone.equals(toTimezone)) {
      return dateTime;
    }
    
    try {
      ZoneId fromZone = ZoneId.of(fromTimezone);
      ZoneId toZone = ZoneId.of(toTimezone);
      

      ZonedDateTime zonedDateTime = dateTime.atZone(fromZone);
      
      // Convert to target timezone
      ZonedDateTime convertedDateTime = zonedDateTime.withZoneSameInstant(toZone);
      
      // Return as LocalDateTime
      return convertedDateTime.toLocalDateTime();
    } catch (Exception e) {
      throw new IllegalArgumentException("Error converting timezone from " + fromTimezone + " to "
          + toTimezone + ": " + e.getMessage());
    }
  }
  
  /**
   * Converts a LocalDate to the same date in a different timezone.
   * For date-only operations, this typically returns the same date unless crossing date boundaries.
   *
   * @param date the date to convert
   * @param fromTimezone source timezone
   * @param toTimezone target timezone
   * @return converted date
   */
  public static LocalDate convertDateTimezone(LocalDate date, String fromTimezone,
                                                String toTimezone) {

    LocalDateTime dateTime = date.atStartOfDay();
    LocalDateTime converted = convertTimezone(dateTime, fromTimezone, toTimezone);
    return converted.toLocalDate();
  }
  
  /**
   * Validates that a timezone string is valid.
   *
   * @param timezone timezone string to validate
   * @return true if valid
   */
  public static boolean isValidTimezone(String timezone) {
    try {
      ZoneId.of(timezone);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Gets the current time in a specific timezone.
   *
   * @param timezone target timezone
   * @return current time in the specified timezone
   */
  public static LocalDateTime getCurrentTimeInTimezone(String timezone) {
    ZoneId zoneId = ZoneId.of(timezone);
    return LocalDateTime.now(zoneId);
  }
  
  /**
   * Formats a datetime with timezone information for display.
   *
   * @param dateTime the datetime to format
   * @param timezone the timezone
   * @return formatted string with timezone info
   */
  public static String formatWithTimezone(LocalDateTime dateTime, String timezone) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    ZoneId zoneId = ZoneId.of(timezone);
    String shortName = zoneId.getDisplayName(TextStyle.SHORT, java.util.Locale.getDefault());
    return dateTime.format(formatter) + " (" + shortName + ")"; 
  }
}