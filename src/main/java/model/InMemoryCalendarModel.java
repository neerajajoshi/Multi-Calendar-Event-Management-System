package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the CalendarModel interface.
 * Design rationale:
 * - Implements the interface to support dependency inversion
 * - Uses collections for fast lookup and iteration
 * - Thread-safe operations could be added with synchronized methods or concurrent collections
 * - Validation logic is centralized in the model layer
 * - Easy to replace with database-backed implementation later
 */
public class InMemoryCalendarModel implements CalendarModel {
  private final List<CalendarEvent> singleEvents;
  private final List<CalendarEventSeries> eventSeries;
  private String name;
  private String timezone;

  private final Set<String> excludedSeriesEvents;

  /**
   * Creates a new calendar with the given name and timezone.
   * Design rationale: Using ArrayList for ordered iteration and fast random access.
   * Alternative: Use LinkedHashSet for uniqueness + ordering, or TreeSet for sorted access.
   */
  public InMemoryCalendarModel(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
    this.singleEvents = new ArrayList<>();
    this.eventSeries = new ArrayList<>();
    this.excludedSeriesEvents = new HashSet<>();
  }

  /**
   * this is in-memory calendar model.
   */
  public InMemoryCalendarModel(String name) {
    this(name, "America/New_York"); // Default to EST
  }

  /**
   * This is in-memory calendar model.
   */
  public InMemoryCalendarModel() {
    this("Default Calendar");
  }

  @Override
  public void addEvent(CalendarEvent event) {
    Objects.requireNonNull(event, "Event cannot be null");
    validateEventUniqueness(event);
    singleEvents.add(event);
  }

  @Override
  public void addEventSeries(CalendarEventSeries series) {
    Objects.requireNonNull(series, "Event series cannot be null");
    for (CalendarEvent event : series.getEvents()) {
      validateEventUniqueness(event);
    }
    eventSeries.add(series);
  }

  /**
   * Validates that an event doesn't conflict with existing events.
   * Design rationale: Centralized validation ensures business rules are enforced
   * consistently across all entry points.
   */
  private void validateEventUniqueness(CalendarEvent newEvent) {
    // Check against single events
    for (CalendarEvent existing : singleEvents) {
      if (existing.conflictsWith(newEvent)) {
        throw new IllegalArgumentException(
            "Event conflicts with existing event: " + existing.getSubject());
      }
    }

    for (CalendarEventSeries series : eventSeries) {
      for (CalendarEvent existing : series.getEvents()) {
        if (!isExcludedFromSeries(existing.getSubject(), existing.getStartDateTime())
            &&
            existing.conflictsWith(newEvent)) {
          throw new IllegalArgumentException(
              "Event conflicts with existing event in series: " + existing.getSubject());
        }
      }
    }
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");
    
    List<CalendarEvent> eventsOnDate = new ArrayList<>();

    // Add single events
    eventsOnDate.addAll(singleEvents.stream()
            .filter(event -> event.occursOn(date))
            .collect(Collectors.toList()));

    for (CalendarEventSeries series : eventSeries) {
      eventsOnDate.addAll(series.getEvents().stream()
              .filter(event -> event.occursOn(date))
              .filter(event -> !isExcludedFromSeries(event.getSubject(), event.getStartDateTime()))
              .collect(Collectors.toList()));
    }

    return Collections.unmodifiableList(eventsOnDate);
  }

  @Override
  public List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    Objects.requireNonNull(start, "Start time cannot be null");
    Objects.requireNonNull(end, "End time cannot be null");
    
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }

    List<CalendarEvent> eventsInRange = new ArrayList<>();

    // Add single events
    eventsInRange.addAll(singleEvents.stream()
            .filter(event -> event.overlapsWith(start, end))
            .collect(Collectors.toList()));

    for (CalendarEventSeries series : eventSeries) {
      eventsInRange.addAll(series.getEvents().stream()
              .filter(event -> event.overlapsWith(start, end))
              .filter(event -> !isExcludedFromSeries(event.getSubject(), event.getStartDateTime()))
              .collect(Collectors.toList()));
    }

    return Collections.unmodifiableList(eventsInRange);
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    Objects.requireNonNull(dateTime, "DateTime cannot be null");
    return !getEventsInRange(dateTime, dateTime.plusMinutes(1)).isEmpty();
  }

  @Override
  public List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime) {
    Objects.requireNonNull(subject, "Subject cannot be null");
    Objects.requireNonNull(startDateTime, "Start date/time cannot be null");
    
    List<CalendarEvent> foundEvents = new ArrayList<>();

    // Search single events
    foundEvents.addAll(singleEvents.stream()
            .filter(event -> event.getSubject().equals(subject)
                &&
                           event.getStartDateTime().equals(startDateTime))
            .collect(Collectors.toList()));

    // Search events in series
    for (CalendarEventSeries series : eventSeries) {
      foundEvents.addAll(series.getEvents().stream()
              .filter(event -> event.getSubject().equals(subject)
                  &&
                             event.getStartDateTime().equals(startDateTime))
              .filter(event -> !isExcludedFromSeries(event.getSubject(), event.getStartDateTime()))
              .collect(Collectors.toList()));
    }

    return Collections.unmodifiableList(foundEvents);
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    List<CalendarEvent> allEvents = new ArrayList<>(singleEvents);
    for (CalendarEventSeries series : eventSeries) {
      allEvents.addAll(series.getEvents().stream()
              .filter(event -> !isExcludedFromSeries(event.getSubject(), event.getStartDateTime()))
              .collect(Collectors.toList()));
    }
    return Collections.unmodifiableList(allEvents);
  }

  @Override
  public boolean removeEvent(CalendarEvent event) {
    return singleEvents.remove(event);
  }

  @Override
  public boolean replaceEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    int index = singleEvents.indexOf(oldEvent);
    if (index >= 0) {
      List<CalendarEvent> tempEvents = new ArrayList<>(singleEvents);
      tempEvents.remove(index);

      for (CalendarEvent existing : tempEvents) {
        if (existing.conflictsWith(newEvent)) {
          throw new IllegalArgumentException(
              "Updated event conflicts with existing event: " + existing.getSubject());
        }
      }

      for (CalendarEventSeries series : eventSeries) {
        for (CalendarEvent existing : series.getEvents()) {
          if (!isExcludedFromSeries(existing.getSubject(), existing.getStartDateTime())
              &&
              existing.conflictsWith(newEvent)) {
            throw new IllegalArgumentException(
                "Updated event conflicts with existing event in series: " + existing.getSubject());
          }
        }
      }
      
      singleEvents.set(index, newEvent);
      return true;
    }

    for (CalendarEventSeries series : eventSeries) {
      for (CalendarEvent seriesEvent : series.getEvents()) {
        if (seriesEvent.equals(oldEvent)) {
          validateEventUniqueness(newEvent);
          singleEvents.add(newEvent);
          return true;
        }
      }
    }
    
    return false;
  }

  @Override
  public void excludeFromSeries(String subject, LocalDateTime startDateTime) {
    String key = subject + "|" + startDateTime.toString();
    excludedSeriesEvents.add(key);
  }
  
  /**
   * Checks if a series event is excluded (overridden by a single event).
   */
  private boolean isExcludedFromSeries(String subject, LocalDateTime startDateTime) {
    String key = subject + "|" + startDateTime.toString();
    return excludedSeriesEvents.contains(key);
  }

  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public String getTimezone() {
    return timezone;
  }
  
  /**
   * Sets the calendar name (for editing).
   */
  public void setName(String name) {
    this.name = Objects.requireNonNull(name, "Name cannot be null");
  }
  
  /**
   * Sets the calendar timezone (for editing).
   * Converts all existing events to the new timezone.
   */
  public void setTimezone(String newTimezone) {
    Objects.requireNonNull(newTimezone, "Timezone cannot be null");
    
    if (this.timezone.equals(newTimezone)) {
      return;
    }
    
    String oldTimezone = this.timezone;
    
    // Convert all single events to new timezone
    List<CalendarEvent> convertedSingleEvents = new ArrayList<>();
    for (CalendarEvent event : singleEvents) {
      CalendarEvent converted = convertEventTimezone(event, oldTimezone, newTimezone);
      convertedSingleEvents.add(converted);
    }
    singleEvents.clear();
    singleEvents.addAll(convertedSingleEvents);

    List<CalendarEventSeries> convertedSeries = new ArrayList<>();
    for (CalendarEventSeries series : eventSeries) {
      CalendarEventSeries converted = convertSeriesTimezone(series, oldTimezone, newTimezone);
      convertedSeries.add(converted);
    }
    eventSeries.clear();
    eventSeries.addAll(convertedSeries);

    this.timezone = newTimezone;
  }
  
  /**
   * Helper method to convert an event to a new timezone.
   */
  private CalendarEvent convertEventTimezone(CalendarEvent event, String fromTimezone,
                                             String toTimezone) {
    LocalDateTime convertedStart = util.TimezoneConverter.convertTimezone(
        event.getStartDateTime(), fromTimezone, toTimezone);
    
    LocalDateTime convertedEnd = null;
    if (event.getEndDateTime() != null) {
      convertedEnd = util.TimezoneConverter.convertTimezone(
          event.getEndDateTime(), fromTimezone, toTimezone);
    }
    
    return new CalendarEvent(
        event.getSubject(),
        convertedStart,
        convertedEnd,
        event.getDescription(),
        event.getLocation(),
        event.getStatus(),
        event.isAllDay()
    );
  }
  
  /**
   * Helper method to convert an event series to a new timezone.
   */
  private CalendarEventSeries convertSeriesTimezone(CalendarEventSeries series,
                                                      String fromTimezone, String toTimezone) {
    CalendarEvent firstEvent = series.getEvents().get(0);
    LocalDateTime convertedStart = util.TimezoneConverter.convertTimezone(
        firstEvent.getStartDateTime(), fromTimezone, toTimezone);
    LocalDateTime convertedEnd = null;
    if (firstEvent.getEndDateTime() != null) {
      convertedEnd = util.TimezoneConverter.convertTimezone(
          firstEvent.getEndDateTime(), fromTimezone, toTimezone);
    }

    if (series.getSeriesEndDate() != null) {
      // Date-based recurrence
      return new CalendarEventSeries(
          series.getSubject(),
          convertedStart,
          convertedEnd,
          series.getRepeatDays(),
          series.getSeriesEndDate()
      );
    } else {
      // Count-based recurrence
      return new CalendarEventSeries(
          series.getSubject(),
          convertedStart,
          convertedEnd,
          series.getRepeatDays(),
          series.getMaxOccurrences()
      );
    }
  }
  
  @Override
  public CalendarEvent copyEvent(CalendarEvent event, LocalDateTime newStartDateTime) {
    Objects.requireNonNull(event, "Event cannot be null");
    Objects.requireNonNull(newStartDateTime, "New start time cannot be null");
    
    // Calculate duration of original event
    LocalDateTime originalStart = event.getStartDateTime();
    LocalDateTime originalEnd = event.getEndDateTime();
    
    LocalDateTime newEndDateTime;
    if (originalEnd != null) {
      long durationMinutes = java.time.Duration.between(originalStart, originalEnd).toMinutes();
      newEndDateTime = newStartDateTime.plusMinutes(durationMinutes);
    } else {
      newEndDateTime = null;
    }
    
    // Create new event with same properties but new times
    CalendarEvent copiedEvent = new CalendarEvent(
        event.getSubject(),
        newStartDateTime,
        newEndDateTime,
        event.getDescription(),
        event.getLocation(),
        event.getStatus(),
        event.isAllDay()
    );
    
    // Add to this calendar
    addEvent(copiedEvent);
    return copiedEvent;
  }
  
  @Override
  public List<CalendarEvent> copyEvents(List<CalendarEvent> events, LocalDate startDate) {
    Objects.requireNonNull(events, "Events list cannot be null");
    Objects.requireNonNull(startDate, "Start date cannot be null");
    
    List<CalendarEvent> copiedEvents = new ArrayList<>();
    
    if (events.isEmpty()) {
      return copiedEvents;
    }
    
    // Find the earliest event to calculate offset
    LocalDate earliestDate = events.stream()
        .map(event -> event.getStartDateTime().toLocalDate())
        .min(LocalDate::compareTo)
        .orElse(startDate);
    
    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(earliestDate, startDate);
    
    for (CalendarEvent event : events) {
      LocalDateTime originalStart = event.getStartDateTime();
      LocalDateTime newStart = originalStart.plusDays(dayOffset);
      
      try {
        CalendarEvent copied = copyEvent(event, newStart);
        copiedEvents.add(copied);
      } catch (Exception e) {
        // Skip events that can't be copied
        System.err.println("Warning: Could not copy event '" + event.getSubject() + "': "
            + e.getMessage());
      }
    }
    
    return copiedEvents;
  }

  /**
   * Gets the number of single events (for debugging/testing).
   */
  public int getSingleEventCount() {
    return singleEvents.size();
  }

  /**
   * Gets the number of event series (for debugging/testing).
   */
  public int getEventSeriesCount() {
    return eventSeries.size();
  }

  /**
   * Finds the event series containing a specific event.
   * This method supports editing operations on series.
   */
  public CalendarEventSeries findSeriesContaining(String subject, LocalDateTime startDateTime) {
    for (CalendarEventSeries series : eventSeries) {
      CalendarEvent event = series.findEventByDate(startDateTime.toLocalDate());
      if (event != null && event.getSubject().equals(subject)
          &&
          event.getStartDateTime().equals(startDateTime)) {
        return series;
      }
    }
    return null;
  }
}