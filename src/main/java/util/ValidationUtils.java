package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Utility class for input validation and error handling.
 * Design rationale:
 * - Centralized validation logic for consistency
 * - Provides clear, user-friendly error messages
 * - Supports defensive programming practices
 * - Easy to extend with new validation rules
 */
public final class ValidationUtils {
  

  private ValidationUtils() {
    throw new AssertionError("Utility class should not be instantiated");
  }
  
  /**
   * Validates that a string is not null or empty.
   *
   * @param value the string to validate
   * @param fieldName the name of the field for error messages
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateNotEmpty(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " cannot be null");
    if (value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be empty");
    }
  }
  
  /**
   * Validates a calendar name.
   *
   * @param name the calendar name to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateCalendarName(String name) {
    validateNotEmpty(name, "Calendar name");
    
    if (name.length() > 50) {
      throw new IllegalArgumentException("Calendar name cannot exceed 50 characters");
    }
    
    if (!name.matches("^[a-zA-Z0-9_\\-\\s]+$")) {
      throw new IllegalArgumentException("Calendar name can only contain letters, numbers, spaces, "
          + "hyphens, and underscores");
    }
  }
  
  /**
   * Validates an event subject.
   *
   * @param subject the event subject to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateEventSubject(String subject) {
    validateNotEmpty(subject, "Event subject");
    
    if (subject.length() > 100) {
      throw new IllegalArgumentException("Event subject cannot exceed 100 characters");
    }
  }
  
  /**
   * Validates that start time is before end time.
   *
   * @param start the start time
   * @param end the end time
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateTimeOrder(LocalDateTime start, LocalDateTime end) {
    Objects.requireNonNull(start, "Start time cannot be null");
    Objects.requireNonNull(end, "End time cannot be null");
    
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
  }
  
  /**
   * Validates that a date is not in the past (for future events).
   *
   * @param date the date to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateFutureDate(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");
    
    LocalDate today = LocalDate.now();
    if (date.isBefore(today)) {
      throw new IllegalArgumentException("Date cannot be in the past: " + date);
    }
  }
  
  /**
   * Validates a timezone string.
   *
   * @param timezone the timezone to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateTimezone(String timezone) {
    validateNotEmpty(timezone, "Timezone");
    
    if (!TimezoneConverter.isValidTimezone(timezone)) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone
          + ". Expected IANA format like 'America/New_York'");
    }
  }
  
  /**
   * Validates a file extension for export.
   *
   * @param filename the filename to validate
   * @return the detected format (csv or ical)
   * @throws IllegalArgumentException if validation fails
   */
  public static String validateAndDetectExportFormat(String filename) {
    validateNotEmpty(filename, "Filename");
    
    String lowerFilename = filename.toLowerCase();
    if (lowerFilename.endsWith(".csv")) {
      return "csv";
    } else if (lowerFilename.endsWith(".ics") || lowerFilename.endsWith(".ical")) {
      return "ical";
    } else {
      throw new IllegalArgumentException("Unsupported file format. Use .csv, .ics, or "
          + ".ical extension");
    }
  }
  
  /**
   * Validates a property name for calendar editing.
   *
   * @param property the property name to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateCalendarProperty(String property) {
    validateNotEmpty(property, "Property name");
    
    if (!property.equals("name") && !property.equals("timezone")) {
      throw new IllegalArgumentException("Invalid property: " + property
          + ". Valid properties: name, timezone");
    }
  }
  
  /**
   * Validates an event property name for editing.
   *
   * @param property the property name to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateEventProperty(String property) {
    validateNotEmpty(property, "Property name");
    
    String[] validProperties = {"subject", "start", "end", "description", "location", "status"};
    for (String validProp : validProperties) {
      if (validProp.equals(property)) {
        return;
      }
    }
    
    throw new IllegalArgumentException("Invalid property: " + property
        + ". Valid properties: subject, start, end, description, location, status");
  }
  
  /**
   * Creates a user-friendly error message for date/time parsing errors.
   *
   * @param input the invalid input
   * @param expectedFormat the expected format
   * @return formatted error message
   */
  public static String createDateTimeErrorMessage(String input, String expectedFormat) {
    return String.format("Invalid date/time format: %s. Expected format: %s", input,
        expectedFormat);
  }
  
  /**
   * Creates a user-friendly error message for command parsing errors.
   *
   * @param command the invalid command
   * @param expectedFormat the expected format
   * @return formatted error message
   */
  public static String createCommandErrorMessage(String command, String expectedFormat) {
    return String.format("Invalid command format: %s. Expected: %s", command, expectedFormat);
  }
}