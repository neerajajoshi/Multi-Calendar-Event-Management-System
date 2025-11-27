package controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.CalendarManager;
import model.CalendarModel;
import model.EventStatus;
import util.WeekdayParser;
import view.GuiCalendarView;

/**
 * Controller for GUI-based calendar interactions.
 * Design rationale:
 * - Extends existing controller functionality for GUI
 * - Implements GUIActionListener to handle GUI events
 * - Maintains MVC separation
 * - Reuses existing business logic from CalendarController
 */
public class GuiCalendarController implements GuiCalendarView.GuiActionListener {
  private final CalendarManager calendarManager;
  private final GuiCalendarView view;
  private final CalendarController baseController;
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  
  /**
   * Creates a GUI calendar controller.
   */
  public GuiCalendarController(CalendarManager calendarManager, GuiCalendarView view) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.baseController = new CalendarController(calendarManager, view);

    view.setActionListener(this);

    initializeDefaultCalendar();
  }
  
  /**
   * Initializes a default calendar if none exists.
   */
  private void initializeDefaultCalendar() {
    if (calendarManager.getCalendarCount() == 0) {
      String defaultTimezone = java.time.ZoneId.systemDefault().getId();
      try {
        calendarManager.createCalendar("Default", defaultTimezone);
        calendarManager.useCalendar("Default");
        view.showMessage("Created default calendar with timezone: " + defaultTimezone);
      } catch (Exception e) {
        view.showError("Failed to create default calendar: " + e.getMessage());
      }
    }
  }
  
  @Override
  public void onCreateCalendar(String name, String timezone) {
    try {
      baseController.createCalendar(name, timezone);
      baseController.useCalendar(name);
      refreshEventCache();
    } catch (Exception e) {
      view.showError("Failed to create calendar: " + e.getMessage());
    }
  }
  
  @Override
  public void onSelectCalendar(String name) {
    try {
      baseController.useCalendar(name);
      refreshEventCache();
    } catch (Exception e) {
      view.showError("Failed to select calendar: " + e.getMessage());
    }
  }
  
  @Override
  public void onRequestCalendarList() {
    try {
      Set<String> calendarNames = calendarManager.getCalendarNames();
      String currentCalendar = calendarManager.getCurrentCalendarName();
      view.showCalendarSelectionDialog(new ArrayList<>(calendarNames), currentCalendar);
    } catch (Exception e) {
      view.showError("Failed to list calendars: " + e.getMessage());
    }
  }
  
  @Override
  public void onCreateEvent(String subject, String description, String location,
      LocalDateTime start, LocalDateTime end) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      CalendarEvent event = new CalendarEvent(subject, start, end, description, location,
          EventStatus.PUBLIC, false);
      model.addEvent(event);
      view.showMessage("Event created: " + subject);
      refreshEventCache();
      onViewEventsOnDate(start.toLocalDate());
    } catch (Exception e) {
      view.showError("Failed to create event: " + e.getMessage());
    }
  }
  
  @Override
  public void onCreateAllDayEvent(String subject, String description, String location,
      LocalDate date) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      CalendarEvent event = new CalendarEvent(subject, date.atTime(8, 0), 
          date.atTime(17, 0), description, location,
          EventStatus.PUBLIC, true);
      model.addEvent(event);
      view.showMessage("All-day event created: " + subject);
      // Refresh the view and event cache
      refreshEventCache();
      onViewEventsOnDate(date);
    } catch (Exception e) {
      view.showError("Failed to create all-day event: " + e.getMessage());
    }
  }
  
  @Override
  public void onCreateRecurringEvent(String subject, String description, String location,
      LocalDate startDate, boolean isAllDay, String startTime, String endTime, 
      String weekdays, int weeks, LocalDate endDate) {
    try {
      // Parse weekdays
      Set<DayOfWeek> weekdaySet = WeekdayParser.parseWeekdays(weekdays);

      int totalOccurrences = -1;
      if (weeks > 0) {
        totalOccurrences = weeks * weekdaySet.size();
      }
      
      // Create event series
      CalendarEventSeries series;
      
      if (isAllDay) {
        if (totalOccurrences > 0) {
          series = new CalendarEventSeries(subject, startDate, weekdaySet, 
              totalOccurrences, null);
        } else {
          series = new CalendarEventSeries(subject, startDate, weekdaySet, -1, endDate);
        }
      } else {
        LocalTime start = LocalTime.parse(startTime, TIME_FORMATTER);
        LocalTime end = LocalTime.parse(endTime, TIME_FORMATTER);
        LocalDateTime startDateTime = LocalDateTime.of(startDate, start);
        LocalDateTime endDateTime = LocalDateTime.of(startDate, end);
        
        if (totalOccurrences > 0) {
          series = new CalendarEventSeries(subject, startDateTime, endDateTime, 
              weekdaySet, totalOccurrences);
        } else {
          series = new CalendarEventSeries(subject, startDateTime, endDateTime, 
              weekdaySet, endDate);
        }
      }

      if (!description.isEmpty() || !location.isEmpty()) {
        CalendarModel model = calendarManager.getCurrentCalendar();

        for (CalendarEvent event : series.getEvents()) {
          CalendarEvent eventWithDetails = new CalendarEvent(
              event.getSubject(),
              event.getStartDateTime(),
              event.getEndDateTime(),
              description.isEmpty() ? null : description,
              location.isEmpty() ? null : location,
              event.getStatus(),
              event.isAllDay()
          );
          model.addEvent(eventWithDetails);
        }
      } else {
        baseController.createEventSeries(series);
      }
      
      view.showMessage("Recurring event series created: " + subject 
          + " (" + series.getEvents().size() + " events)");
      
      // Refresh the view and event cache
      refreshEventCache();
      onViewEventsOnDate(startDate);
    } catch (Exception e) {
      view.showError("Failed to create recurring event: " + e.getMessage());
    }
  }
  
  @Override
  public void onViewEventsOnDate(LocalDate date) {
    try {
      baseController.showEventsOnDate(date);
    } catch (Exception e) {
      view.showError("Failed to view events: " + e.getMessage());
    }
  }
  
  @Override
  public void onRequestEventsForEdit(LocalDate date) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      List<CalendarEvent> events = model.getEventsOnDate(date);
      view.showEventEditDialog(events);
    } catch (Exception e) {
      view.showError("Failed to get events for editing: " + e.getMessage());
    }
  }
  
  @Override
  public void onEditEvent(CalendarEvent event, String newSubject, String newDesc,
      String newLocation, String newStartTime, String newEndTime, int scope) {
    try {
      String originalSubject = event.getSubject();
      LocalDateTime originalStartTime = event.getStartDateTime();

      String currentSubject = originalSubject;

      switch (scope) {
        case 0: // This event only
          editSingleEventProperties(event, newSubject, newDesc, newLocation, 
              newStartTime, newEndTime);
          break;
          
        case 1: // This and future events
          editFutureEventsProperties(event, newSubject, newDesc, newLocation, 
              newStartTime, newEndTime);
          break;
          
        case 2: // All events in series
          editAllSeriesProperties(event, newSubject, newDesc, newLocation, 
              newStartTime, newEndTime);
          break;
          
        default:
          view.showError("Invalid edit scope");
          break;
      }
      
      // Refresh the view and event cache
      refreshEventCache();
      onViewEventsOnDate(event.getStartDateTime().toLocalDate());
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }
  
  /**
   * Edits a single event's properties.
   */
  private void editSingleEventProperties(CalendarEvent event, String newSubject, 
      String newDesc, String newLocation, String newStartTime, String newEndTime) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      
      // Build the updated event with all changes
      String finalSubject = newSubject.isEmpty() ? event.getSubject() : newSubject;
      String finalDesc = newDesc;
      String finalLocation = newLocation;
      
      LocalDateTime finalStart = event.getStartDateTime();
      LocalDateTime finalEnd = event.getEndDateTime();
      
      // Update times if not all-day event
      if (!event.isAllDay()) {
        String currentStart = event.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
        String currentEnd = event.getEndDateTime().toLocalTime().format(TIME_FORMATTER);
        
        if (!newStartTime.equals(currentStart)) {
          finalStart = LocalDateTime.of(event.getStartDateTime().toLocalDate(), 
              LocalTime.parse(newStartTime, TIME_FORMATTER));
        }
        if (!newEndTime.equals(currentEnd)) {
          finalEnd = LocalDateTime.of(event.getEndDateTime().toLocalDate(), 
              LocalTime.parse(newEndTime, TIME_FORMATTER));
        }
      }
      
      // Create the updated event
      CalendarEvent updatedEvent = new CalendarEvent(
          finalSubject,
          finalStart,
          finalEnd,
          finalDesc.isEmpty() ? null : finalDesc,
          finalLocation.isEmpty() ? null : finalLocation,
          event.getStatus(),
          event.isAllDay()
      );

      CalendarEventSeries series = model.findSeriesContaining(event.getSubject(), 
          event.getStartDateTime());
      
      if (series != null) {
        model.excludeFromSeries(event.getSubject(), event.getStartDateTime());
        model.addEvent(updatedEvent);
        view.showMessage("Event updated (removed from series)");
      } else {
        model.replaceEvent(event, updatedEvent);
        view.showMessage("Event updated");
      }
      
    } catch (Exception e) {
      view.showError("Failed to edit event: " + e.getMessage());
    }
  }
  
  /**
   * Edits properties for this and future events in a series.
   */
  private void editFutureEventsProperties(CalendarEvent event, String newSubject, 
      String newDesc, String newLocation, String newStartTime, String newEndTime) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();

      List<CalendarEvent> allMatchingEvents = model.getAllEvents().stream()
          .filter(evt -> evt.getSubject().equals(event.getSubject()))
          .filter(evt -> !evt.getStartDateTime().isBefore(event.getStartDateTime()))
          .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
          .collect(java.util.stream.Collectors.toList());

      if (allMatchingEvents.isEmpty()) {
        view.showMessage("No events to update");
        return;
      }

      for (CalendarEvent evt : allMatchingEvents) {
        CalendarEventSeries series = model.findSeriesContaining(evt.getSubject(),
            evt.getStartDateTime());
        if (series != null) {
          model.excludeFromSeries(evt.getSubject(), evt.getStartDateTime());
        } else {
          model.removeEvent(evt);
        }
      }

      for (CalendarEvent evt : allMatchingEvents) {
        // Calculate new times
        LocalDateTime newStart = evt.getStartDateTime();
        LocalDateTime newEnd = evt.getEndDateTime();
        if (!evt.isAllDay()) {
          String currentStart = evt.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
          String currentEnd = evt.getEndDateTime().toLocalTime().format(TIME_FORMATTER);
          if (!newStartTime.equals(currentStart)) {
            newStart = LocalDateTime.of(evt.getStartDateTime().toLocalDate(),
                LocalTime.parse(newStartTime, TIME_FORMATTER));
          }
          if (!newEndTime.equals(currentEnd)) {
            newEnd = LocalDateTime.of(evt.getEndDateTime().toLocalDate(),
                LocalTime.parse(newEndTime, TIME_FORMATTER));
          }
        }

        // Create updated event with all new properties
        CalendarEvent updatedEvent = new CalendarEvent(
            newSubject.isEmpty() ? evt.getSubject() : newSubject,
            newStart,
            newEnd,
            newDesc.isEmpty() ? null : newDesc,
            newLocation.isEmpty() ? null : newLocation,
            evt.getStatus(),
            evt.isAllDay()
        );

        model.addEvent(updatedEvent);
      }

      view.showMessage("Updated " + allMatchingEvents.size() + " events from this date forward");
    } catch (Exception e) {
      view.showError("Failed to update events: " + e.getMessage());
    }
  }
  
  /**
   * Edits properties for all events in a series.
   */
  private void editAllSeriesProperties(CalendarEvent event, String newSubject, 
      String newDesc, String newLocation, String newStartTime, String newEndTime) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();

      List<CalendarEvent> allMatchingEvents = model.getAllEvents().stream()
          .filter(evt -> evt.getSubject().equals(event.getSubject()))
          .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
          .collect(java.util.stream.Collectors.toList());

      if (allMatchingEvents.isEmpty()) {
        view.showMessage("No events to update");
        return;
      }

      for (CalendarEvent evt : allMatchingEvents) {
        CalendarEventSeries series = model.findSeriesContaining(evt.getSubject(),
            evt.getStartDateTime());
        if (series != null) {
          model.excludeFromSeries(evt.getSubject(), evt.getStartDateTime());
        } else {
          model.removeEvent(evt);
        }
      }

      for (CalendarEvent evt : allMatchingEvents) {
        // Calculate new times
        LocalDateTime newStart = evt.getStartDateTime();
        LocalDateTime newEnd = evt.getEndDateTime();
        if (!evt.isAllDay()) {
          String currentStart = evt.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
          String currentEnd = evt.getEndDateTime().toLocalTime().format(TIME_FORMATTER);
          if (!newStartTime.equals(currentStart)) {
            newStart = LocalDateTime.of(evt.getStartDateTime().toLocalDate(),
                LocalTime.parse(newStartTime, TIME_FORMATTER));
          }
          if (!newEndTime.equals(currentEnd)) {
            newEnd = LocalDateTime.of(evt.getEndDateTime().toLocalDate(),
                LocalTime.parse(newEndTime, TIME_FORMATTER));
          }
        }

        // Create updated event with all new properties
        CalendarEvent updatedEvent = new CalendarEvent(
            newSubject.isEmpty() ? evt.getSubject() : newSubject,
            newStart,
            newEnd,
            newDesc.isEmpty() ? null : newDesc,
            newLocation.isEmpty() ? null : newLocation,
            evt.getStatus(),
            evt.isAllDay()
        );

        model.addEvent(updatedEvent);
      }

      view.showMessage("Updated all " + allMatchingEvents.size() + " events in series");
    } catch (Exception e) {
      view.showError("Failed to update events: " + e.getMessage());
    }
  }
  
  @Override
  public void onRequestEventsForDelete(LocalDate date) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      List<CalendarEvent> events = model.getEventsOnDate(date);
      view.showEventDeleteDialog(events);
    } catch (Exception e) {
      view.showError("Failed to get events for deletion: " + e.getMessage());
    }
  }

  @Override
  public void onDeleteEvent(CalendarEvent event, int scope) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      String originalSubject = event.getSubject();
      LocalDateTime originalStartTime = event.getStartDateTime();

      switch (scope) {
        case 0: // This event only
          deleteSingleEvent(event);
          break;

        case 1: // This and future events
          deleteFutureEvents(event);
          break;

        case 2: // All events in series
          deleteAllSeriesEvents(event);
          break;

        default:
          view.showError("Invalid delete scope");
          break;
      }

      // Refresh the view and event cache
      refreshEventCache();
      onViewEventsOnDate(event.getStartDateTime().toLocalDate());
    } catch (Exception e) {
      view.showError("Failed to delete event: " + e.getMessage());
    }
  }

  /**
   * Deletes a single event.
   */
  private void deleteSingleEvent(CalendarEvent event) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();

      CalendarEventSeries series = model.findSeriesContaining(event.getSubject(),
          event.getStartDateTime());

      if (series != null) {
        // Remove from series
        model.excludeFromSeries(event.getSubject(), event.getStartDateTime());
        view.showMessage("Event deleted (removed from series)");
      } else {
        boolean removed = model.removeEvent(event);
        if (removed) {
          view.showMessage("Event deleted");
        } else {
          view.showError("Event not found");
        }
      }
    } catch (Exception e) {
      view.showError("Failed to delete event: " + e.getMessage());
    }
  }

  /**
   * Deletes this and future events in a series.
   */
  private void deleteFutureEvents(CalendarEvent event) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();

      List<CalendarEvent> allMatchingEvents = model.getAllEvents().stream()
          .filter(evt -> evt.getSubject().equals(event.getSubject()))
          .filter(evt -> !evt.getStartDateTime().isBefore(event.getStartDateTime()))
          .collect(java.util.stream.Collectors.toList());

      if (allMatchingEvents.isEmpty()) {
        view.showError("No events found to delete");
        return;
      }

      for (CalendarEvent evt : allMatchingEvents) {
        CalendarEventSeries series = model.findSeriesContaining(evt.getSubject(),
            evt.getStartDateTime());
        if (series != null) {
          model.excludeFromSeries(evt.getSubject(), evt.getStartDateTime());
        } else {
          model.removeEvent(evt);
        }
      }

      view.showMessage("Deleted " + allMatchingEvents.size() + " events from this date forward");
    } catch (Exception e) {
      view.showError("Failed to delete future events: " + e.getMessage());
    }
  }

  /**
   * Deletes all events in a series.
   */
  private void deleteAllSeriesEvents(CalendarEvent event) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();

      List<CalendarEvent> allMatchingEvents = model.getAllEvents().stream()
          .filter(evt -> evt.getSubject().equals(event.getSubject()))
          .collect(java.util.stream.Collectors.toList());

      if (allMatchingEvents.isEmpty()) {
        view.showError("No events found to delete");
        return;
      }

      for (CalendarEvent evt : allMatchingEvents) {
        CalendarEventSeries series = model.findSeriesContaining(evt.getSubject(),
            evt.getStartDateTime());
        if (series != null) {
          model.excludeFromSeries(evt.getSubject(), evt.getStartDateTime());
        } else {
          model.removeEvent(evt);
        }
      }

      view.showMessage("Deleted all " + allMatchingEvents.size() + " events in series");
    } catch (Exception e) {
      view.showError("Failed to delete series: " + e.getMessage());
    }
  }

  @Override
  public void onRequestEventsForCopy(LocalDate date) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      List<CalendarEvent> events = model.getEventsOnDate(date);
      Set<String> calendarNames = calendarManager.getCalendarNames();
      String currentCalendar = calendarManager.getCurrentCalendarName();
      String currentTimezone = model.getTimezone();
      view.showEventCopyDialog(events, new ArrayList<>(calendarNames), currentCalendar, 
          currentTimezone, calendarManager);
    } catch (Exception e) {
      view.showError("Failed to get events for copying: " + e.getMessage());
    }
  }

  @Override
  public void onCopyEvent(CalendarEvent event, String targetCalendar,
      LocalDateTime targetDateTime) {
    try {
      baseController.copyEvent(event.getSubject(), event.getStartDateTime(),
          targetCalendar, targetDateTime);
      view.showMessage("Event copied to " + targetCalendar);
    } catch (Exception e) {
      view.showError("Failed to copy event: " + e.getMessage());
    }
  }

  @Override
  public void onMonthChanged(YearMonth month) {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      List<CalendarEvent> allEvents = model.getAllEvents();
      view.updateEventCacheForMonth(allEvents);
    } catch (Exception e) {
      // fails
    }
  }

  /**
   * Refreshes the event cache for the current month.
   */
  private void refreshEventCache() {
    try {
      CalendarModel model = calendarManager.getCurrentCalendar();
      List<CalendarEvent> allEvents = model.getAllEvents();
      view.updateEventCacheForMonth(allEvents);
    } catch (Exception e) {
      // fails
    }
  }

  /**
   * Shows the GUI.
   */
  public void show() {
    view.setVisible(true);
    // Initialize event cache
    refreshEventCache();
  }
}
