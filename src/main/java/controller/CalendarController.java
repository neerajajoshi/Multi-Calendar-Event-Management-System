package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.CalendarManager;
import model.CalendarModel;
import model.EventStatus;
import util.TimezoneConverter;
import view.CalendarView;
import view.IcalExporter;


/**
 * Controller that coordinates between the calendar model and view.
 * Design rationale:
 * - Implements MVC pattern by separating concerns
 * - Controller handles user actions and updates model/view accordingly
 * - No direct coupling between model and view
 * - Supports multiple view implementations (console, GUI, web, etc.)
 * - Business logic stays in model, presentation logic in view
 */
public class CalendarController {
  private final CalendarManager calendarManager;
  private final CalendarView view;

  /**
   * this is calendar controller.
   */
  public CalendarController(CalendarManager calendarManager, CalendarView view) {
    this.calendarManager = calendarManager;
    this.view = view;
  }

  /**
   * This is calendar controller.
   */
  public CalendarController(CalendarModel model, CalendarView view) {
    this.calendarManager = new CalendarManager();
    this.view = view;
    this.calendarManager.createCalendar(model.getName(), model.getTimezone());
    this.calendarManager.useCalendar(model.getName());
  }

  /**
   * Creates a single event.
   */
  public void createEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    try {
      CalendarModel model = getCurrentCalendar();
      CalendarEvent event = new CalendarEvent(subject, startDateTime, endDateTime);
      model.addEvent(event);
      view.showMessage("Event created: " + subject);
    } catch (Exception e) {
      view.showError("Error creating event: " + e.getMessage());
    }
  }

  /**
   * Creates an all-day event.
   */
  public void createAllDayEvent(String subject, LocalDate date) {
    try {
      CalendarModel model = getCurrentCalendar();
      CalendarEvent event = new CalendarEvent(subject, date);
      model.addEvent(event);
      view.showMessage("All-day event created: " + subject);
    } catch (Exception e) {
      view.showError("Error creating all-day event: " + e.getMessage());
    }
  }

  /**
   * Creates an event series.
   */
  public void createEventSeries(CalendarEventSeries series) {
    try {
      CalendarModel model = getCurrentCalendar();
      model.addEventSeries(series);
      view.showMessage("Event series created: " + series.getSubject()
          + " (" + series.getEvents().size() + " events)");
    } catch (Exception e) {
      view.showError("Error creating event series: " + e.getMessage());
    }
  }

  /**
   * Displays events on a specific date.
   */
  public void showEventsOnDate(LocalDate date) {
    try {
      CalendarModel model = getCurrentCalendar();
      List<CalendarEvent> events = model.getEventsOnDate(date);
      view.showEventsOnDate(date, events);
    } catch (Exception e) {
      view.showError("Error retrieving events: " + e.getMessage());
    }
  }

  /**
   * Displays events in a date range.
   */
  public void showEventsInRange(LocalDateTime start, LocalDateTime end) {
    try {
      CalendarModel model = getCurrentCalendar();
      List<CalendarEvent> events = model.getEventsInRange(start, end);
      view.showEventsInRange(start, end, events);
    } catch (Exception e) {
      view.showError("Error retrieving events: " + e.getMessage());
    }
  }

  /**
   * Shows busy/available status at a specific time.
   */
  public void showStatus(LocalDateTime dateTime) {
    try {
      CalendarModel model = getCurrentCalendar();
      boolean isBusy = model.isBusyAt(dateTime);
      view.showStatus(isBusy);
    } catch (Exception e) {
      view.showError("Error checking status: " + e.getMessage());
    }
  }

  /**
   * Exports calendar to a file.
   */
  public void exportCalendar(String filename) {
    try {
      CalendarModel model = getCurrentCalendar();
      List<CalendarEvent> events = model.getAllEvents();
      view.exportToFile(filename, events);
    } catch (Exception e) {
      view.showError("Error exporting calendar: " + e.getMessage());
    }
  }

  /**
   * Exports calendar to iCal format.
   */
  public void exportCalendarToIcal(String filename) {
    try {
      CalendarModel model = getCurrentCalendar();
      List<CalendarEvent> events = model.getAllEvents();

      IcalExporter.exportToIcalFile(events, filename, model.getName(), model.getTimezone());
      view.showMessage("Calendar exported to iCal file: " + filename);
    } catch (Exception e) {
      view.showError("Error exporting calendar to iCal: " + e.getMessage());
    }
  }



  /**
   * Edits a single event property.
   */
  public void editEvent(String subject, LocalDateTime startDateTime,
                       String property, String newValue) {
    try {
      CalendarModel model = getCurrentCalendar();
      List<CalendarEvent> events = model.findEvents(subject, startDateTime);

      if (events.isEmpty()) {
        view.showError("No event found with subject '" + subject + "' at " + startDateTime);
        return;
      }

      if (events.size() > 1) {
        view.showError("Multiple events found. Cannot edit.");
        return;
      }

      CalendarEvent originalEvent = events.get(0);
      CalendarEvent updatedEvent = updateEventProperty(originalEvent, property, newValue);

      CalendarEventSeries series = model.findSeriesContaining(subject, startDateTime);

      if (series != null) {
        try {
          model.excludeFromSeries(originalEvent.getSubject(), originalEvent.getStartDateTime());
          model.addEvent(updatedEvent);
          view.showMessage("Series event '" + originalEvent.getSubject() + "' "
              + property + " updated to: " + newValue);
        } catch (Exception e) {
          view.showError("Error: " + e.getMessage());
        }
      } else {
        boolean success = model.replaceEvent(originalEvent, updatedEvent);
        if (success) {
          view.showMessage("Event '" + originalEvent.getSubject() + "' "
              + property + " updated to: " + newValue);
        } else {
          view.showError("Failed to update event");
        }
      }

    } catch (Exception e) {
      view.showError("Error editing event: " + e.getMessage());
    }
  }

  /**
   * Edits events in a series from a specific date forward.
   */
  public void editEventsFromDate(String subject, LocalDateTime startDateTime,
                                String property, String newValue) {
    try {
      CalendarModel model = getCurrentCalendar();
      CalendarEventSeries series = model.findSeriesContaining(subject, startDateTime);

      if (series == null) {
        editEvent(subject, startDateTime, property, newValue);
        return;
      }

      List<CalendarEvent> eventsToEdit = series.getEvents().stream()
              .filter(event -> !event.getStartDateTime().isBefore(startDateTime))
              .collect(java.util.stream.Collectors.toList());

      if (eventsToEdit.isEmpty()) {
        view.showError("No events found from " + startDateTime + " forward in series: " + subject);
        return;
      }

      for (CalendarEvent event : eventsToEdit) {
        model.excludeFromSeries(event.getSubject(), event.getStartDateTime());
      }

      int updatedCount = 0;
      for (CalendarEvent event : eventsToEdit) {
        try {
          CalendarEvent updatedEvent = updateEventProperty(event, property, newValue);
          model.addEvent(updatedEvent);
          updatedCount++;
        } catch (Exception e) {
          view.showError("Error updating event on " + event.getStartDateTime().toLocalDate() + ": "
              + e.getMessage());
        }
      }

      view.showMessage("Updated " + updatedCount + " events from " + startDateTime.toLocalDate()
          + " forward in series '" + subject + "' - " + property + " set to: " + newValue);

    } catch (Exception e) {
      view.showError("Error editing events from date: " + e.getMessage());
    }
  }

  /**
   * Edits all events in a series.
   */
  public void editAllEventsInSeries(String subject, LocalDateTime startDateTime,
                                   String property, String newValue) {
    try {
      // Find the series containing this event
      CalendarModel model = getCurrentCalendar();
      CalendarEventSeries series = model.findSeriesContaining(subject, startDateTime);

      if (series == null) {
        editEvent(subject, startDateTime, property, newValue);
        return;
      }

      List<CalendarEvent> allEventsInSeries = series.getEvents();

      if (allEventsInSeries.isEmpty()) {
        view.showError("No events found in series: " + subject);
        return;
      }

      for (CalendarEvent event : allEventsInSeries) {
        model.excludeFromSeries(event.getSubject(), event.getStartDateTime());
      }

      int updatedCount = 0;
      for (CalendarEvent event : allEventsInSeries) {
        try {
          CalendarEvent updatedEvent = updateEventProperty(event, property, newValue);
          model.addEvent(updatedEvent);
          updatedCount++;
        } catch (Exception e) {
          view.showError("Error updating event on " + event.getStartDateTime().toLocalDate()
              + ": " + e.getMessage());
        }
      }

      view.showMessage("Updated all " + updatedCount + " events in series '" + subject
          + "' - " + property + " set to: " + newValue);

    } catch (Exception e) {
      view.showError("Error editing series: " + e.getMessage());
    }
  }

  /**
   * Helper method to update event properties.
   */
  private CalendarEvent updateEventProperty(CalendarEvent event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        return event.withSubject(newValue);
      case "description":
        return event.withDescription(newValue);
      case "location":
        return event.withLocation(newValue);
      case "status":
        return event.withStatus(EventStatus.fromString(newValue));
      case "start":
        LocalDateTime newStartTime = parseDateTime(newValue);
        if (event.getEndDateTime() != null && newStartTime.isAfter(event.getEndDateTime())) {
          throw new IllegalArgumentException("Start time cannot be after end time");
        }
        return new CalendarEvent(event.getSubject(), newStartTime, event.getEndDateTime(),
            event.getDescription(), event.getLocation(), event.getStatus(), event.isAllDay());
      case "end":
        LocalDateTime newEndTime = parseDateTime(newValue);
        if (newEndTime.isBefore(event.getStartDateTime())) {
          throw new IllegalArgumentException("End time cannot be before start time");
        }
        return new CalendarEvent(event.getSubject(), event.getStartDateTime(), newEndTime,
            event.getDescription(), event.getLocation(), event.getStatus(), event.isAllDay());
      default:
        throw new IllegalArgumentException("Invalid property: " + property
            + ". Valid properties: subject, description, location, status, start, end");
    }
  }

  /**
   * Helper method to parse date/time strings.
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter
          .ofPattern("yyyy-MM-dd'T'HH:mm"));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date/time format: " + dateTimeStr
          + ". Expected format: YYYY-MM-DDTHH:mm");
    }
  }

  /**
   * Creates a new calendar.
   */
  public void createCalendar(String name, String timezone) {
    try {
      calendarManager.createCalendar(name, timezone);
      view.showMessage("Calendar '" + name + "' created with timezone " + timezone);
    } catch (Exception e) {
      view.showError("Error creating calendar: " + e.getMessage());
    }
  }

  /**
   * Edits a calendar property.
   */
  public void editCalendar(String calendarName, String property, String newValue) {
    try {
      calendarManager.editCalendar(calendarName, property, newValue);
      view.showMessage("Calendar '" + calendarName + "' " + property + " updated to: " + newValue);
    } catch (Exception e) {
      view.showError("Error editing calendar: " + e.getMessage());
    }
  }

  /**
   * Sets the current calendar context.
   */
  public void useCalendar(String calendarName) {
    try {
      calendarManager.useCalendar(calendarName);
      CalendarModel calendar = calendarManager.getCurrentCalendar();
      view.showMessage("Now using calendar: " + calendarName + " (timezone: "
          + calendar.getTimezone() + ")");
    } catch (Exception e) {
      view.showError("Error switching calendar: " + e.getMessage());
    }
  }

  /**
   * Lists all available calendars.
   */
  public void listCalendars() {
    try {
      java.util.Set<String> calendarNames = calendarManager.getCalendarNames();

      if (calendarNames.isEmpty()) {
        view.showMessage("No calendars available. Create one with: create calendar "
            + "--name <name> --timezone <timezone>");
        return;
      }

      StringBuilder result = new StringBuilder();
      result.append("Available calendars (").append(calendarNames.size()).append("):\n");

      String currentCalendar = calendarManager.getCurrentCalendarName();

      for (String name : calendarNames) {
        CalendarModel calendar = calendarManager.getCalendar(name);
        result.append("- ").append(name);
        result.append(" (").append(calendar.getTimezone()).append(")");

        if (name.equals(currentCalendar)) {
          result.append(" [ACTIVE]");
        }

        result.append("\n");
      }

      view.showMessage(result.toString().trim());
    } catch (Exception e) {
      view.showError("Error listing calendars: " + e.getMessage());
    }
  }

  /**
   * Gets the current calendar model.
   */
  private CalendarModel getCurrentCalendar() {
    return calendarManager.getCurrentCalendar();
  }

  /**
   * Copies a single event to another calendar.
   */
  public void copyEvent(String eventSubject, LocalDateTime sourceDateTime,
                       String targetCalendarName, LocalDateTime targetDateTime) {
    try {
      CalendarModel sourceCalendar = getCurrentCalendar();
      CalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);

      List<CalendarEvent> events = sourceCalendar.findEvents(eventSubject, sourceDateTime);
      if (events.isEmpty()) {
        view.showError("No event found with subject '" + eventSubject + "' at " + sourceDateTime);
        return;
      }

      if (events.size() > 1) {
        view.showError("Multiple events found. Cannot copy.");
        return;
      }

      CalendarEvent sourceEvent = events.get(0);

      LocalDateTime adjustedTargetTime = targetDateTime;
      if (!sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        adjustedTargetTime = TimezoneConverter.convertTimezone(
            targetDateTime, targetCalendar.getTimezone(), targetCalendar.getTimezone());
      }

      CalendarEvent copiedEvent = targetCalendar.copyEvent(sourceEvent, adjustedTargetTime);
      view.showMessage("Event '" + eventSubject + "' copied to calendar '"
          + targetCalendarName + "' at " + targetDateTime);

    } catch (Exception e) {
      view.showError("Error copying event: " + e.getMessage());
    }
  }

  /**
   * Copies all events on a specific date to another calendar.
   */
  public void copyEventsOnDate(LocalDate sourceDate, String targetCalendarName, LocalDate
      targetDate) {
    try {
      CalendarModel sourceCalendar = getCurrentCalendar();
      CalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);

      List<CalendarEvent> eventsOnDate = sourceCalendar.getEventsOnDate(sourceDate);
      if (eventsOnDate.isEmpty()) {
        view.showMessage("No events found on " + sourceDate + " to copy");
        return;
      }

      List<CalendarEvent> eventsToConvert = eventsOnDate;
      if (!sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        eventsToConvert = convertEventsTimezone(eventsOnDate, sourceCalendar.getTimezone(),
            targetCalendar.getTimezone());
      }

      List<CalendarEvent> copiedEvents = targetCalendar.copyEvents(eventsToConvert, targetDate);
      view.showMessage("Copied " + copiedEvents.size() + " events from " + sourceDate
          + " to calendar '" + targetCalendarName + "' starting " + targetDate);

    } catch (Exception e) {
      view.showError("Error copying events: " + e.getMessage());
    }
  }

  /**
   * Copies all events in a date range to another calendar.
   */
  public void copyEventsInRange(LocalDate startDate, LocalDate endDate,
                               String targetCalendarName, LocalDate targetStartDate) {
    try {
      CalendarModel sourceCalendar = getCurrentCalendar();
      CalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);

      LocalDateTime rangeStart = startDate.atStartOfDay();
      LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);

      List<CalendarEvent> eventsInRange = sourceCalendar.getEventsInRange(rangeStart, rangeEnd);
      if (eventsInRange.isEmpty()) {
        view.showMessage("No events found in range " + startDate + " to " + endDate + " to copy");
        return;
      }

      List<CalendarEvent> eventsToConvert = eventsInRange;
      if (!sourceCalendar.getTimezone().equals(targetCalendar.getTimezone())) {
        eventsToConvert = convertEventsTimezone(eventsInRange, sourceCalendar.getTimezone(),
            targetCalendar.getTimezone());
      }

      List<CalendarEvent> copiedEvents = targetCalendar.copyEvents(eventsToConvert,
          targetStartDate);
      view.showMessage("Copied " + copiedEvents.size() + " events from range " + startDate + " to "
          + endDate + " to calendar '" + targetCalendarName + "' starting " + targetStartDate);

    } catch (Exception e) {
      view.showError("Error copying events in range: " + e.getMessage());
    }
  }

  /**
   * Helper method to convert event times between timezones.
   */
  private List<CalendarEvent> convertEventsTimezone(List<CalendarEvent> events, String fromTimezone,
                                                      String toTimezone) {
    List<CalendarEvent> convertedEvents = new ArrayList<>();

    for (CalendarEvent event : events) {
      LocalDateTime convertedStart = TimezoneConverter.convertTimezone(
          event.getStartDateTime(), fromTimezone, toTimezone);

      LocalDateTime convertedEnd = null;
      if (event.getEndDateTime() != null) {
        convertedEnd = TimezoneConverter.convertTimezone(
            event.getEndDateTime(), fromTimezone, toTimezone);
      }

      CalendarEvent convertedEvent = new CalendarEvent(
          event.getSubject(),
          convertedStart,
          convertedEnd,
          event.getDescription(),
          event.getLocation(),
          event.getStatus(),
          event.isAllDay()
      );

      convertedEvents.add(convertedEvent);
    }

    return convertedEvents;
  }

  /**
   * Handles application exit.
   */
  public void exit() {
    view.showMessage("Goodbye!");
  }
}