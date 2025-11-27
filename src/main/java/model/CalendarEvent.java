package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable representation of a calendar event.
 * Design rationale: 
 * - Immutable to prevent accidental modification and ensure thread safety
 * - Uses LocalDateTime for precise time representation (EST timezone assumed)
 * - Builder pattern could be added later for complex event creation
 * - Value object pattern - equality based on content, not identity
 */
public final class CalendarEvent {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final EventStatus status;
  private final boolean isAllDay;

  /**
   * Creates a new event with start and end date/time.
   *
   * @param subject event subject
   * @param startDateTime start time
   * @param endDateTime end time
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this(subject, startDateTime, endDateTime, null, null, EventStatus.PUBLIC, false);
  }

  /**
   * Creates a new all-day event.
   *
   * @param subject event subject
   * @param date event date
   */
  public CalendarEvent(String subject, LocalDate date) {
    this(subject, date.atTime(8, 0), date.atTime(17, 0), null, null, EventStatus.PUBLIC, true);
  }

  /**
   * Full constructor for creating events with all properties.
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                      String description, String location, EventStatus status, boolean isAllDay) {
    this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
    this.startDateTime = Objects.requireNonNull(startDateTime, "Start date/time cannot be null");
    
    // Validate that end time is after start time
    if (endDateTime != null && endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException(
          "End time (" + endDateTime + ") cannot be before start time (" + startDateTime + ")");
    }
    
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.status = Objects.requireNonNull(status, "Status cannot be null");
    this.isAllDay = isAllDay;
  }

  // Getters
  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public EventStatus getStatus() {
    return status;
  }

  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Creates a new event with modified properties.
   */
  public CalendarEvent withSubject(String newSubject) {
    return new CalendarEvent(newSubject, startDateTime, endDateTime, description, location, status,
        isAllDay);
  }

  /**
   * Creates a new event with modified properties.
   */
  public CalendarEvent withDescription(String newDescription) {
    return new CalendarEvent(subject, startDateTime, endDateTime, newDescription, location, status,
        isAllDay);
  }

  /**
   * Creates a new event with modified properties.
   */
  public CalendarEvent withLocation(String newLocation) {
    return new CalendarEvent(subject, startDateTime, endDateTime, description, newLocation, status,
        isAllDay);
  }

  /**
   * Creates a new event with modified properties.
   */
  public CalendarEvent withStatus(EventStatus newStatus) {
    return new CalendarEvent(subject, startDateTime, endDateTime, description, location, newStatus,
        isAllDay);
  }

  /**
   * Checks if this event conflicts with another event (same subject, start, and end times).
   */
  public boolean conflictsWith(CalendarEvent other) {
    return this.subject.equals(other.subject)
        &&
           this.startDateTime.equals(other.startDateTime)
        &&
           Objects.equals(this.endDateTime, other.endDateTime);
  }

  /**
   * Checks if this event occurs on the specified date.
   */
  public boolean occursOn(LocalDate date) {
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime != null ? endDateTime.toLocalDate() : startDate;
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Checks if this event overlaps with the specified time range.
   */
  public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
    LocalDateTime eventEnd = endDateTime != null ? endDateTime : startDateTime.plusMinutes(1);
    return startDateTime.isBefore(end) && eventEnd.isAfter(start);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CalendarEvent event = (CalendarEvent) obj;
    return Objects.equals(subject, event.subject)
        &&
           Objects.equals(startDateTime, event.startDateTime)
        &&
           Objects.equals(endDateTime, event.endDateTime)
        &&
           Objects.equals(description, event.description)
        &&
           Objects.equals(location, event.location)
        &&
           Objects.equals(status, event.status)
        &&
           isAllDay == event.isAllDay;
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime, description, location, status,
        isAllDay);
  }

  @Override
  public String toString() {
    return String.format("%s starting on %s at %s, ending on %s at %s",
            subject,
            startDateTime.toLocalDate(),
            startDateTime.toLocalTime(),
            endDateTime != null ? endDateTime.toLocalDate() : startDateTime.toLocalDate(),
            endDateTime != null ? endDateTime.toLocalTime() : startDateTime.toLocalTime());
  }
}