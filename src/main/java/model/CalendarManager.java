package model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Manages multiple calendars in the application.
 * Design rationale:
 * - Centralized management of multiple calendar instances
 * - Enforces unique calendar names
 * - Supports timezone-aware calendars
 * - Maintains current calendar context for operations
 */
public class CalendarManager {
  private final Map<String, CalendarModel> calendars;
  private String currentCalendarName;

  /**
   * This is calendar manager.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
    this.currentCalendarName = null;
  }
  
  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name unique calendar name
   * @param timezone IANA timezone (e.g., "America/New_York")
   * @throws IllegalArgumentException if name already exists or timezone is invalid
   */
  public void createCalendar(String name, String timezone) {
    Objects.requireNonNull(name, "Calendar name cannot be null");
    Objects.requireNonNull(timezone, "Timezone cannot be null");
    
    if (name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }
    
    // Validate timezone
    try {
      ZoneId.of(timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }
    
    CalendarModel calendar = new InMemoryCalendarModel(name, timezone);
    calendars.put(name, calendar);
  }
  
  /**
   * Gets a calendar by name.
   *
   * @param name calendar name
   * @return the calendar model
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  public CalendarModel getCalendar(String name) {
    CalendarModel calendar = calendars.get(name);
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    return calendar;
  }
  
  /**
   * Sets the current calendar context.
   *
   * @param name calendar name to use
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  public void useCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar '" + name + "' does not exist");
    }
    this.currentCalendarName = name;
  }
  
  /**
   * Gets the current calendar.
   *
   * @return current calendar model
   * @throws IllegalStateException if no calendar is currently selected
   */
  public CalendarModel getCurrentCalendar() {
    if (currentCalendarName == null) {
      throw new IllegalStateException(
          "No calendar is currently selected. Use 'use calendar' command first.");
    }
    return getCalendar(currentCalendarName);
  }
  
  /**
   * Gets the current calendar name.
   *
   * @return current calendar name or null if none selected
   */
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }
  
  /**
   * Edits a calendar property.
   *
   * @param calendarName name of calendar to edit
   * @param property property to edit ("name" or "timezone")
   * @param newValue new property value
   * @throws IllegalArgumentException if calendar doesn't exist or property is invalid
   */
  public void editCalendar(String calendarName, String property, String newValue) {
    CalendarModel calendar = getCalendar(calendarName);
    
    switch (property.toLowerCase()) {
      case "name":
        editCalendarName(calendarName, newValue);
        break;
      case "timezone":
        editCalendarTimezone(calendarName, newValue);
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid property: " + property + ". Valid properties: name, timezone");
    }
  }
  
  private void editCalendarName(String oldName, String newName) {
    Objects.requireNonNull(newName, "New calendar name cannot be null");
    
    if (newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    
    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + newName + "' already exists");
    }
    
    CalendarModel calendar = calendars.remove(oldName);
    if (calendar instanceof InMemoryCalendarModel) {
      ((InMemoryCalendarModel) calendar).setName(newName);
    }
    calendars.put(newName, calendar);
    
    if (oldName.equals(currentCalendarName)) {
      currentCalendarName = newName;
    }
  }
  
  private void editCalendarTimezone(String calendarName, String newTimezone) {
    Objects.requireNonNull(newTimezone, "Timezone cannot be null");
    
    // Validate timezone
    try {
      ZoneId.of(newTimezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + newTimezone);
    }
    
    CalendarModel calendar = getCalendar(calendarName);
    if (calendar instanceof InMemoryCalendarModel) {
      ((InMemoryCalendarModel) calendar).setTimezone(newTimezone);
    }
  }
  
  /**
   * Gets all calendar names.
   *
   * @return set of calendar names
   */
  public Set<String> getCalendarNames() {
    return new HashSet<>(calendars.keySet());
  }
  
  /**
   * Checks if a calendar exists.
   *
   * @param name calendar name
   * @return true if calendar exists
   */
  public boolean hasCalendar(String name) {
    return calendars.containsKey(name);
  }
  
  /**
   * Gets the number of calendars.
   *
   * @return calendar count
   */
  public int getCalendarCount() {
    return calendars.size();
  }
}