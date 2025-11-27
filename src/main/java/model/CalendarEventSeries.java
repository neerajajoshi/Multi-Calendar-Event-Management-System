package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a series of recurring calendar events.
 * Design rationale:
 * - Encapsulates the logic for generating recurring events
 * - Immutable after creation to prevent inconsistent state
 * - Generates events lazily or eagerly based on requirements
 * - Supports both count-based and date-based recurrence
 */
public final class CalendarEventSeries {
  private final String subject;
  private final LocalTime startTime;
  private final LocalTime endTime;
  private final Set<DayOfWeek> repeatDays;
  private final LocalDate seriesStartDate;
  private final LocalDate seriesEndDate;
  private final int maxOccurrences;
  private final boolean isAllDay;
  private final String description;
  private final String location;
  private final EventStatus status;

  private final List<CalendarEvent> events;

  /**
   * Creates an event series with a specific number of occurrences.
   */
  public CalendarEventSeries(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                            Set<DayOfWeek> repeatDays, int occurrences) {
    this.subject = Objects.requireNonNull(subject);
    this.startTime = startDateTime.toLocalTime();
    this.endTime = endDateTime != null ? endDateTime.toLocalTime() : startTime;
    this.repeatDays = Collections.unmodifiableSet(new HashSet<>(repeatDays));
    this.seriesStartDate = startDateTime.toLocalDate();
    this.maxOccurrences = occurrences;
    this.seriesEndDate = null;
    this.isAllDay = false;
    this.status = EventStatus.PUBLIC;
    this.description = null;
    this.location = null;
    this.events = generateEvents();
  }

  /**
   * Creates an event series until a specific end date.
   */
  public CalendarEventSeries(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                            Set<DayOfWeek> repeatDays, LocalDate endDate) {
    this.subject = Objects.requireNonNull(subject);
    this.startTime = startDateTime.toLocalTime();
    this.endTime = endDateTime != null ? endDateTime.toLocalTime() : startTime;
    this.repeatDays = Collections.unmodifiableSet(new HashSet<>(repeatDays));
    this.seriesStartDate = startDateTime.toLocalDate();
    this.seriesEndDate = endDate;
    this.maxOccurrences = -1;
    this.isAllDay = false;
    this.status = EventStatus.PUBLIC;
    this.description = null;
    this.location = null;
    this.events = generateEvents();
  }

  /**
   * Creates an all-day event series.
   */
  public CalendarEventSeries(String subject, LocalDate startDate, Set<DayOfWeek> repeatDays, 
                            int occurrences, LocalDate endDate) {
    this.subject = Objects.requireNonNull(subject);
    this.startTime = LocalTime.of(8, 0);
    this.endTime = LocalTime.of(17, 0);
    this.repeatDays = Collections.unmodifiableSet(new HashSet<>(repeatDays));
    this.seriesStartDate = startDate;
    this.seriesEndDate = endDate;
    this.maxOccurrences = occurrences;
    this.isAllDay = true;
    this.status = EventStatus.PUBLIC;
    this.description = null;
    this.location = null;
    this.events = generateEvents();
  }

  /**
   * Generates all events in this series based on the recurrence rules.
   * Design rationale: Pre-compute events for better performance and immutability.
   * Alternative: Generate events on-demand for memory efficiency with large series.
   */
  private List<CalendarEvent> generateEvents() {
    List<CalendarEvent> generatedEvents = new ArrayList<>();
    LocalDate currentDate = seriesStartDate;
    int count = 0;
    
    while ((maxOccurrences == -1 || count < maxOccurrences)
        &&
           (seriesEndDate == null || !currentDate.isAfter(seriesEndDate))) {
      
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        CalendarEvent event = isAllDay
            ?
            new CalendarEvent(subject, currentDate) :
            new CalendarEvent(subject, currentDate.atTime(startTime), currentDate.atTime(endTime));
        
        generatedEvents.add(event);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }
    
    return Collections.unmodifiableList(generatedEvents);
  }

  /**
   * Gets all events in this series.
   *
   * @return immutable list of events
   */
  public List<CalendarEvent> getEvents() {
    return events;
  }

  /**
   * Finds an event in this series by start date.
   *
   * @param date the date to search for
   * @return the event on that date, or null if none exists
   */
  public CalendarEvent findEventByDate(LocalDate date) {
    return events.stream()
            .filter(event -> event.getStartDateTime().toLocalDate().equals(date))
            .findFirst()
            .orElse(null);
  }

  // Getters
  public String getSubject() {
    return subject;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public Set<DayOfWeek> getRepeatDays() {
    return repeatDays;
  }

  public LocalDate getSeriesStartDate() {
    return seriesStartDate;
  }

  public LocalDate getSeriesEndDate() {
    return seriesEndDate;
  }

  public int getMaxOccurrences() {
    return maxOccurrences;
  }

  public boolean isAllDay() {
    return isAllDay;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CalendarEventSeries that = (CalendarEventSeries) obj;
    return maxOccurrences == that.maxOccurrences
        &&
           isAllDay == that.isAllDay
        &&
           Objects.equals(subject, that.subject)
        &&
           Objects.equals(startTime, that.startTime)
        &&
           Objects.equals(endTime, that.endTime)
        &&
           Objects.equals(repeatDays, that.repeatDays)
        &&
           Objects.equals(seriesStartDate, that.seriesStartDate)
        &&
           Objects.equals(seriesEndDate, that.seriesEndDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startTime, endTime, repeatDays, seriesStartDate, 
                       seriesEndDate, maxOccurrences, isAllDay);
  }
}