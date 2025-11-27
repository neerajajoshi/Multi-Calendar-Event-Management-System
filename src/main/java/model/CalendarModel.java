package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface defining the core calendar model operations.
 * This represents the business logic layer, decoupled from I/O and presentation.
 * Design rationale: Using an interface allows for different implementations
 * (in-memory, database-backed, etc.) and supports dependency inversion principle.
 */
public interface CalendarModel {
  
  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event conflicts with existing events
   */
  void addEvent(CalendarEvent event);
  
  /**
   * Adds an event series to the calendar.
   *
   * @param series the event series to add
   * @throws IllegalArgumentException if any event in series conflicts
   */
  void addEventSeries(CalendarEventSeries series);
  
  /**
   * Retrieves all events occurring on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<CalendarEvent> getEventsOnDate(LocalDate date);
  
  /**
   * Retrieves all events overlapping with a time range.
   *
   * @param start start of time range
   * @param end end of time range
   * @return list of events in range
   */
  List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end);
  
  /**
   * Checks if the calendar has any events at a specific time.
   *
   * @param dateTime the time to check
   * @return true if busy, false if available
   */
  boolean isBusyAt(LocalDateTime dateTime);
  
  /**
   * Finds events matching specific criteria.
   *
   * @param subject event subject
   * @param startDateTime event start time
   * @return list of matching events
   */
  List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime);
  
  /**
   * Gets all events in the calendar.
   *
   * @return all events
   */
  List<CalendarEvent> getAllEvents();
  
  /**
   * Removes an event from the calendar.
   *
   * @param event the event to remove
   * @return true if the event was removed, false if not found
   */
  boolean removeEvent(CalendarEvent event);
  
  /**
   * Replaces an existing event with an updated version.
   *
   * @param oldEvent the event to replace
   * @param newEvent the new event
   * @return true if the event was replaced, false if old event not found
   */
  boolean replaceEvent(CalendarEvent oldEvent, CalendarEvent newEvent);
  
  /**
   * Finds the event series containing a specific event.
   *
   * @param subject event subject
   * @param startDateTime event start time
   * @return the series containing the event, or null if not found
   */
  CalendarEventSeries findSeriesContaining(String subject, LocalDateTime startDateTime);
  
  /**
   * Excludes a specific date from a series (marks it as overridden).
   *
   * @param subject event subject
   * @param startDateTime event start time
   */
  void excludeFromSeries(String subject, LocalDateTime startDateTime);

  /**
   * Gets the calendar name.
   *
   * @return calendar name
   */
  String getName();
  
  /**
   * Gets the calendar timezone.
   *
   * @return timezone string (IANA format)
   */
  String getTimezone();
  
  /**
   * Copies a single event to this calendar at the specified time.
   *
   * @param event the event to copy
   * @param newStartDateTime the new start time in this calendar's timezone
   * @return the copied event
   */
  CalendarEvent copyEvent(CalendarEvent event, LocalDateTime newStartDateTime);
  
  /**
   * Copies multiple events to this calendar starting from the specified date.
   *
   * @param events the events to copy
   * @param startDate the start date in this calendar's timezone
   * @return list of copied events
   */
  List<CalendarEvent> copyEvents(List<CalendarEvent> events, LocalDate startDate);
}