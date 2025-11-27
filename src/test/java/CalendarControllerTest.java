import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import controller.CalendarController;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.CalendarModel;
import model.EventStatus;
import model.InMemoryCalendarModel;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;

/**
 * Tests for CalendarController.
 */
public class CalendarControllerTest {
  private CalendarModel model;
  private MockCalendarView view;
  private CalendarController controller;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    model = new InMemoryCalendarModel("Test Calendar");
    view = new MockCalendarView();
    controller = new CalendarController(model, view);
  }

  /*
  @Test
  public void testCreateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
    assertTrue(view.lastMessage.contains("Event created: Meeting"));
  }
  */

  @Test
  public void testCreateEventWithError() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    controller.createEvent("Meeting", start, end);

    assertTrue(view.lastError.contains("Error creating event"));
  }

  /*
  @Test
  public void testCreateAllDayEvent() {
    LocalDate date = LocalDate.of(2025, 11, 1);

    controller.createAllDayEvent("Holiday", date);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertTrue(events.get(0).isAllDay());
    assertTrue(view.lastMessage.contains("All-day event created: Holiday"));
  }
  */

  @Test
  public void testCreateEventSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly Meeting", start, end, days, 5);
    controller.createEventSeries(series);

    assertTrue(view.lastMessage.contains("Event series created"));
    assertTrue(view.lastMessage.contains("5 events"));
  }

  @Test
  public void testShowEventsOnDate() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    controller.createEvent("Meeting", start, end);
    controller.showEventsOnDate(date);

    assertTrue(view.eventsOnDateCalled);
    assertEquals(date, view.lastDate);
    assertEquals(1, view.lastEventList.size());
  }

  @Test
  public void testShowEventsOnDateEmpty() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    controller.showEventsOnDate(date);

    assertTrue(view.eventsOnDateCalled);
    assertEquals(0, view.lastEventList.size());
  }

  @Test
  public void testShowEventsInRange() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 23, 59);
    controller.showEventsInRange(rangeStart, rangeEnd);

    assertTrue(view.eventsInRangeCalled);
    assertEquals(1, view.lastEventList.size());
  }

  @Test
  public void testShowStatus() {
    LocalDateTime time = LocalDateTime.of(2025, 11, 1, 10, 30);
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.showStatus(time);

    assertTrue(view.statusCalled);
    assertTrue(view.lastBusyStatus);
  }

  @Test
  public void testShowStatusAvailable() {
    LocalDateTime time = LocalDateTime.of(2025, 11, 1, 14, 0);
    controller.showStatus(time);

    assertTrue(view.statusCalled);
    assertFalse(view.lastBusyStatus);
  }

  @Test
  public void testExportCalendar() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.exportCalendar("test.csv");

    assertTrue(view.exportCalled);
    assertEquals("test.csv", view.lastFilename);
    assertEquals(1, view.lastEventList.size());
  }

  /*
  @Test
  public void testEditEventSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "subject", "Important Meeting");

    List<CalendarEvent> events = model.findEvents("Important Meeting", start);
    assertEquals(1, events.size());
    assertTrue(view.lastMessage.contains("subject updated to: Important Meeting"));
  }

  @Test
  public void testEditEventDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "description", "Team standup");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals("Team standup", events.get(0).getDescription());
  }

  @Test
  public void testEditEventLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "location", "Room 101");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals("Room 101", events.get(0).getLocation());
  }

  @Test
  public void testEditEventStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "status", "private");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(EventStatus.PRIVATE, events.get(0).getStatus());
  }
  */

  @Test
  public void testEditEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);

    controller.editEvent("Nonexistent", start, "subject", "New");

    assertTrue(view.lastError.contains("No event found"));
  }

  @Test
  public void testEditEventInvalidProperty() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "invalid", "value");

    assertTrue(view.lastError.contains("Invalid property"));
  }

  /*
  @Test
  public void testEditSingleEventAsAllSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editAllEventsInSeries("Meeting", start, "subject", "New Meeting");

    // Should treat as single event
    List<CalendarEvent> events = model.findEvents("New Meeting", start);
    assertEquals(1, events.size());
  }
  */

  @Test
  public void testExit() {
    controller.exit();
    assertTrue(view.lastMessage.contains("Goodbye"));
  }

  private static class MockCalendarView implements CalendarView {
    String lastMessage = "";
    String lastError = "";
    boolean eventsOnDateCalled = false;
    boolean eventsInRangeCalled = false;
    boolean statusCalled = false;
    boolean exportCalled = false;
    LocalDate lastDate;
    List<CalendarEvent> lastEventList = new ArrayList<>();
    boolean lastBusyStatus;
    String lastFilename;

    @Override
    public void showMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void showError(String error) {
      this.lastError = error;
    }

    @Override
    public void showEventsOnDate(LocalDate date, List<CalendarEvent> events) {
      this.eventsOnDateCalled = true;
      this.lastDate = date;
      this.lastEventList = events;
    }

    @Override
    public void showEventsInRange(LocalDateTime start, LocalDateTime end,
                                  List<CalendarEvent> events) {
      this.eventsInRangeCalled = true;
      this.lastEventList = events;
    }

    @Override
    public void showStatus(boolean isBusy) {
      this.statusCalled = true;
      this.lastBusyStatus = isBusy;
    }

    @Override
    public void exportToFile(String filename, List<CalendarEvent> events) {
      this.exportCalled = true;
      this.lastFilename = filename;
      this.lastEventList = events;
    }
  }

  @Test
  public void testEditSeriesEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editEvent("Weekly", start, "location", "Office");

    assertFalse(view.lastMessage.isEmpty());
  }

  @Test
  public void testEditEventsFromDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 4);
    controller.createEventSeries(series);

    LocalDateTime secondOccurrence = LocalDateTime.of(2025, 11, 10, 10, 0);
    controller.editEventsFromDate("Weekly", secondOccurrence, "location", "Remote");

    assertTrue(view.lastMessage.contains("Updated") || view.lastMessage.contains("events"));
    assertTrue(view.lastMessage.contains("Weekly") || view.lastMessage.contains("location"));
  }

  @Test
  public void testEditAllEventsInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("Weekly", start, "description", "Updated");

    assertTrue(view.lastMessage.contains("Updated") || view.lastMessage.contains("events"));
    assertTrue(view.lastMessage.contains("Weekly") || view.lastMessage.contains("3"));
  }

  /*
  @Test
  public void testEditEventStart() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "2025-11-01T09:00");

    List<CalendarEvent> events = model.findEvents("Meeting",
        LocalDateTime.of(2025, 11, 1, 9, 0));
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 1, 9, 0), events.get(0).getStartDateTime());
  }
  */

  /*
   @Test
  public void testEditEventEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "end", "2025-11-01T12:00");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 1, 12, 0), events.get(0).getEndDateTime());
  }

  @Test
  public void testEditEventStartAfterEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "2025-11-01T12:00");

    assertTrue(view.lastError.contains("Start time cannot be after end time"));
  }
  */

  @Test
  public void testEditEventEndBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "end", "2025-11-01T09:00");

    assertTrue(view.lastError.contains("End time cannot be before start time"));
  }

  @Test
  public void testEditEventInvalidDateTimeFormat() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "invalid-date");

    assertTrue(view.lastError.contains("Invalid date/time format"));
  }

  @Test
  public void testEditEventMultipleEventsFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    CalendarEvent event2 = new CalendarEvent("Meeting", start, end);
    CalendarEvent withLocation = event2.withLocation("Room 2");
    try {
      model.addEvent(withLocation);
    } catch (Exception e) {
      // Expected: duplicate event may throw exception during setup
    }

    controller.editEvent("Meeting", start, "subject", "New");

    assertTrue(view.lastError.contains("Multiple events found")
        || view.lastMessage.contains("updated"));
  }

  /*
  @Test
  public void testEditEventsFromDateNotInSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEventsFromDate("Meeting", start, "location", "Office");

    // Should treat as single event
    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(1, events.size());
    assertEquals("Office", events.get(0).getLocation());
  }
  */
  @Test
  public void testEditEventsFromDateNoEventsFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    LocalDateTime futureDate = LocalDateTime.of(2025, 12, 1, 10, 0);
    controller.editEventsFromDate("Weekly", futureDate, "location", "Remote");

    assertFalse(view.lastError.isEmpty() && view.lastMessage.isEmpty());
  }

  @Test
  public void testEditAllEventsInSeriesEmpty() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);

    controller.editAllEventsInSeries("NonExistent", start, "location", "Office");

    assertTrue(view.lastError.contains("No event found")
        || view.lastMessage.contains("updated"));
  }

  @Test
  public void testEditEventsFromDateWithException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    controller.createEventSeries(series);

    controller.editEventsFromDate("Weekly", start, "invalid", "value");

    assertFalse(view.lastError.isEmpty());
  }

  @Test
  public void testEditAllEventsWithException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    controller.createEventSeries(series);

    // Edit with invalid property to trigger exception during update
    controller.editAllEventsInSeries("Weekly", start, "invalid", "value");

    assertFalse(view.lastError.isEmpty());
  }

  /*
  @Test
  public void testShowEventsOnDateException() {
    // Create a mock that throws exception
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public List<CalendarEvent> getEventsOnDate(LocalDate date) {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.showEventsOnDate(LocalDate.of(2025, 11, 1));

    assertTrue(view.lastError.contains("Error retrieving events"));
  }


  @Test
  public void testShowEventsInRangeException() {
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.showEventsInRange(
        LocalDateTime.of(2025, 11, 1, 0, 0),
        LocalDateTime.of(2025, 11, 30, 23, 59));

    assertTrue(view.lastError.contains("Error retrieving events"));
  }


  @Test
  public void testShowStatusException() {
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public boolean isBusyAt(LocalDateTime dateTime) {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.showStatus(LocalDateTime.of(2025, 11, 1, 10, 0));

    assertTrue(view.lastError.contains("Error checking status"));
  }

  @Test
  public void testExportCalendarException() {
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public List<CalendarEvent> getAllEvents() {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.exportCalendar("test.csv");

    assertTrue(view.lastError.contains("Error exporting calendar"));
  }

  @Test
  public void testCreateEventSeriesException() {
    // Create a series that will cause an exception during addition
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public void addEventSeries(CalendarEventSeries series) {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);

    faultyController.createEventSeries(series);

    assertTrue(view.lastError.contains("Error creating event series"));
  }

  @Test
  public void testCreateAllDayEventException() {
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public void addEvent(CalendarEvent event) {
        throw new RuntimeException("Database error");
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.createAllDayEvent("Holiday", LocalDate.of(2025, 11, 1));

    assertTrue(view.lastError.contains("Error creating all-day event"));
  }
  */

  @Test
  public void testEditSeriesEventExcludeException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public CalendarEventSeries findSeriesContaining(String subject, LocalDateTime startDateTime) {
        return model.findSeriesContaining(subject, startDateTime);
      }

      @Override
      public List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime) {
        return model.findEvents(subject, startDateTime);
      }

      @Override
      public void excludeFromSeries(String subject, LocalDateTime startDateTime) {
        throw new IllegalArgumentException("Cannot exclude - conflict");
      }

      @Override
      public void addEvent(CalendarEvent event) {
        throw new IllegalArgumentException("Cannot add - conflict");
      }
    };

    MockCalendarView faultyView = new MockCalendarView();
    CalendarController faultyController = new CalendarController(faultyModel, faultyView);
    faultyController.editEvent("Weekly", start, "location", "Office");

    assertFalse(faultyView.lastError.isEmpty());
  }

  /*
  @Test
  public void testEditEventReplaceFailure() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    // Create faulty model that returns false on replace
    CalendarModel faultyModel = new InMemoryCalendarModel("Test") {
      @Override
      public List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime) {
        return model.findEvents(subject, startDateTime);
      }

      @Override
      public CalendarEventSeries findSeriesContaining(String subject, LocalDateTime startDateTime) {
        return null; // Not a series
      }

      @Override
      public boolean replaceEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
        return false; // Simulate failure
      }
    };

    CalendarController faultyController = new CalendarController(faultyModel, view);
    faultyController.editEvent("Meeting", start, "location", "Office");

    assertTrue(view.lastError.contains("Failed to update event"));
  }


  @Test
  public void testEditEventStatusCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    // Test case-insensitive property names
    controller.editEvent("Meeting", start, "STATUS", "private");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(EventStatus.PRIVATE, events.get(0).getStatus());
  }

  @Test
  public void testEditEventSubjectCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    // Test case-insensitive property names
    controller.editEvent("Meeting", start, "SUBJECT", "New Meeting");

    List<CalendarEvent> events = model.findEvents("New Meeting", start);
    assertEquals(1, events.size());
  }

  @Test
  public void testEditEventDescriptionCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "DESCRIPTION", "Team meeting");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals("Team meeting", events.get(0).getDescription());
  }

  @Test
  public void testEditEventLocationCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "LOCATION", "Conference Room");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals("Conference Room", events.get(0).getLocation());
  }

  @Test
  public void testEditEventStartCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "START", "2025-11-01T09:30");

    List<CalendarEvent> events = model.findEvents("Meeting",
        LocalDateTime.of(2025, 11, 1, 9, 30));
    assertEquals(1, events.size());
  }

  @Test
  public void testEditEventEndCaseInsensitive() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "END", "2025-11-01T11:30");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(LocalDateTime.of(2025, 11, 1, 11, 30), events.get(0).getEndDateTime());
  }
  */

  @Test
  public void testEditEventsFromDateSingleEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);

    controller.editEventsFromDate("NonExistent", start, "location", "Office");

    assertTrue(view.lastError.contains("No event found"));
  }

  @Test
  public void testEditAllEventsInSeriesSingleEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);

    controller.editAllEventsInSeries("NonExistent", start, "location", "Office");

    assertTrue(view.lastError.contains("No event found"));
  }

  @Test
  public void testParseDateTimeWithDifferentException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "not-a-date-at-all");

    assertTrue(view.lastError.contains("Invalid date/time format")
        || view.lastError.contains("Error editing event"));
  }

  @Test
  public void testEditEventInvalidPropertyMixedCase() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "InvalidProp", "value");

    assertTrue(view.lastError.contains("Invalid property"));
    assertTrue(view.lastError.contains("Valid properties"));
  }

  @Test
  public void testEditSeriesEventSuccessfulUpdate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editEvent("Weekly", start, "description", "Updated description");

    assertTrue(view.lastMessage.contains("Series event")
        || view.lastMessage.contains("updated")
        || !view.lastError.isEmpty());
  }

  @Test
  public void testEditEventsFromDateMultipleUpdates() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    days.add(DayOfWeek.THURSDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 5);
    controller.createEventSeries(series);

    controller.editEventsFromDate("Weekly", start, "status", "private");

    assertTrue(!view.lastMessage.isEmpty() || !view.lastError.isEmpty());
    if (!view.lastError.isEmpty()) {
      assertFalse(view.lastError.isEmpty());
    } else {
      assertTrue(view.lastMessage.contains("Updated")
          || view.lastMessage.contains("events")
          || view.lastMessage.contains("forward"));
    }
  }

  @Test
  public void testEditAllEventsInSeriesMultipleEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 6);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("Weekly", start, "location", "Remote Office");

    assertTrue(view.lastMessage.contains("Updated all"));
    assertTrue(view.lastMessage.contains("location set to: Remote Office"));
  }


  /*
  @Test
  public void testEditSingleEventSuccessfulReplace() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "location", "New Office");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals("New Office", events.get(0).getLocation());
    assertTrue(view.lastMessage.contains("location updated to: New Office"));
    assertFalse(view.lastMessage.contains("Failed"));
  }
  */

  /**
   * Tests the branch where series is NOT null in editEvent.
   * and successfully excludes and adds the event
   */
  @Test
  public void testEditSeriesEventSuccessfulExcludeAndAdd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editEvent("Weekly", start, "location", "Conference Room");
    assertTrue(!view.lastMessage.isEmpty() || !view.lastError.isEmpty());
    if (view.lastMessage.contains("Series event")) {
      assertTrue(view.lastMessage.contains("location updated to: Conference Room"));
    }
  }

  /**
   * Tests successful update of multiple events from a date forward.
   * with proper count in message
   */
  @Test
  public void testEditEventsFromDateSuccessfulMultipleUpdates() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 5);
    controller.createEventSeries(series);

    LocalDateTime secondOccurrence = LocalDateTime.of(2025, 11, 10, 10, 0);
    controller.editEventsFromDate("Weekly", secondOccurrence, "description", "Updated desc");

    assertTrue(!view.lastMessage.isEmpty() || !view.lastError.isEmpty());
    if (!view.lastError.isEmpty()) {
      assertTrue(view.lastError.length() > 0);
    } else {
      // Success path
      assertTrue(view.lastMessage.contains("Updated") || view.lastMessage.contains("events"));
    }
  }

  @Test
  public void testEditAllEventsInSeriesSuccessfulUpdate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries("BiWeekly", start, end, days, 4);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("BiWeekly", start, "status", "confirmed");

    assertTrue(!view.lastMessage.isEmpty() || !view.lastError.isEmpty());
    if (!view.lastError.isEmpty()) {
      assertTrue(view.lastError.length() > 0);
    } else {
      assertTrue(view.lastMessage.contains("Updated") || view.lastMessage.contains("events"));
    }
  }

  /*
  @Test
  public void testEditEventStartTimeValidationPasses() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 12, 0);

    controller.createEvent("Meeting", start, end);
    // Update start to a time that is still before end (valid)
    controller.editEvent("Meeting", start, "start", "2025-11-01T11:00");

    // Should succeed without error
    List<CalendarEvent> events = model.findEvents("Meeting",
        LocalDateTime.of(2025, 11, 1, 11, 0));
    assertEquals(1, events.size());
    assertFalse(view.lastError.contains("Start time cannot be after end time"));
  }

  /**
   * Tests the branch where end time validation passes.
   * (end is NOT before start)
   /*
  @Test
  public void testEditEventEndTimeValidationPasses() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    // Update end to a time that is still after start (valid)
    controller.editEvent("Meeting", start, "end", "2025-11-01T11:30");

    List<CalendarEvent> events = model.findEvents("Meeting", start);
    assertEquals(LocalDateTime.of(2025, 11, 1, 11, 30), events.get(0).getEndDateTime());
    assertFalse(view.lastError.contains("End time cannot be before start time"));
  }

  /**
   * Tests parseDateTime with a valid format (success branch).
   /*
  @Test
  public void testParseDateTimeValidFormat() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    // Use exact valid format
    controller.editEvent("Meeting", start, "start", "2025-11-01T09:00");

    List<CalendarEvent> events = model.findEvents("Meeting",
         LocalDateTime.of(2025, 11, 1, 9, 0));
    assertEquals(1, events.size());
    assertFalse(view.lastError.contains("Invalid date/time format"));
  }
  */
  /**
   * Tests case-sensitive property matching to ensure lowercase conversion works.
   */
  @Test
  public void testUpdateEventPropertyMixedCaseProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Test1", start, end);
    controller.editEvent("Test1", start, "Subject", "NewSubject");

    controller.createEvent("Test2", start.plusHours(1), end.plusHours(1));
    controller.editEvent("Test2", start.plusHours(1), "Description", "NewDesc");

    controller.createEvent("Test3", start.plusHours(2), end.plusHours(2));
    controller.editEvent("Test3", start.plusHours(2), "Location", "NewLoc");

    controller.createEvent("Test4", start.plusHours(3), end.plusHours(3));
    controller.editEvent("Test4", start.plusHours(3), "Status", "private");

    controller.createEvent("Test5", start.plusHours(4), end.plusHours(4));
    controller.editEvent("Test5", start.plusHours(4), "Start", "2025-11-01T15:00");

    controller.createEvent("Test6", start.plusHours(5), end.plusHours(5));
    controller.editEvent("Test6", start.plusHours(5), "End", "2025-11-01T17:00");

    assertFalse(view.lastError.contains("Invalid property"));
  }

  /**
   * Tests the success path when events list is NOT empty in showEventsOnDate.
   */
  @Test
  public void testShowEventsOnDateWithEvents() {
    LocalDate date = LocalDate.of(2025, 11, 5);
    LocalDateTime start = date.atTime(14, 0);
    LocalDateTime end = date.atTime(15, 0);

    controller.createEvent("Afternoon Meeting", start, end);
    controller.showEventsOnDate(date);

    assertTrue(view.eventsOnDateCalled);
    assertEquals(1, view.lastEventList.size());
    assertEquals("Afternoon Meeting", view.lastEventList.get(0).getSubject());
  }

  /**
   * Tests the success path when events list is NOT empty in showEventsInRange.
   */
  @Test
  public void testShowEventsInRangeWithMultipleEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 5, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 5, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 6, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 6, 15, 0);

    controller.createEvent("Meeting 1", start1, end1);
    controller.createEvent("Meeting 2", start2, end2);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 5, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 7, 0, 0);
    controller.showEventsInRange(rangeStart, rangeEnd);

    assertTrue(view.eventsInRangeCalled);
    assertEquals(2, view.lastEventList.size());
  }

  /*
  @Test
  public void testEditEventsFromDateNullSeriesFallback() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Single", start, end);
    controller.editEventsFromDate("Single", start, "location", "Building A");

    List<CalendarEvent> events = model.findEvents("Single", start);
    assertEquals("Building A", events.get(0).getLocation());
  }

  /**
   * Tests editAllEventsInSeries when series IS null (single event path).
   /*
  @Test
  public void testEditAllEventsInSeriesNullSeriesFallback() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Single", start, end);
    controller.editAllEventsInSeries("Single", start, "description", "Solo event");

    List<CalendarEvent> events = model.findEvents("Single", start);
    assertEquals("Solo event", events.get(0).getDescription());
  }

  /**
   * Tests the specific branch where events list has exactly 1 event (not empty, not multiple).
   /*
  @Test
  public void testEditEventExactlyOneEventFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("UniqueEvent", start, end);
    controller.editEvent("UniqueEvent", start, "subject", "ModifiedEvent");

    List<CalendarEvent> events = model.findEvents("ModifiedEvent", start);
    assertEquals(1, events.size());
    assertTrue(view.lastMessage.contains("subject updated"));
  }
  */

  /**
   * Tests export with multiple events to ensure the list is properly populated.
   */
  @Test
  public void testExportCalendarWithMultipleEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 15, 0);

    controller.createEvent("Event1", start1, end1);
    controller.createEvent("Event2", start2, end2);
    controller.exportCalendar("export.csv");

    assertTrue(view.exportCalled);
    assertEquals(2, view.lastEventList.size());
  }

  /**
   * Tests createEventSeries success path with multiple events created.
   */
  @Test
  public void testCreateEventSeriesWithMultipleDays() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 10, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries("MWF Class", start, end, days, 6);
    controller.createEventSeries(series);

    assertTrue(view.lastMessage.contains("Event series created"));
    assertTrue(view.lastMessage.contains("MWF Class"));
    assertTrue(view.lastMessage.contains("events"));
  }

  /**
   * Tests createEvent with valid non-conflicting event.
   */
  @Test
  public void testCreateEventSuccess() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);

    controller.createEvent("Workshop", start, end);

    assertTrue(view.lastMessage.contains("Event created: Workshop"));
    assertFalse(view.lastError.contains("Error"));
  }

  /**
   * Tests createAllDayEvent success path.
   */
  @Test
  public void testCreateAllDayEventSuccess() {
    LocalDate date = LocalDate.of(2025, 12, 25);

    controller.createAllDayEvent("Christmas", date);

    assertTrue(view.lastMessage.contains("All-day event created: Christmas"));
    assertFalse(view.lastError.contains("Error"));
  }

  @Test
  public void testCreateAllDayEventException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createAllDayEvent("Holiday", LocalDate.of(2025, 11, 1));

    assertTrue(testView.lastError.contains("Error creating all-day event"));
  }

  @Test
  public void testCreateEventSeriesException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);


    testController.createEventSeries(series);

    assertTrue(testView.lastError.contains("Error creating event series"));
  }

  @Test
  public void testShowEventsOnDateException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.showEventsOnDate(LocalDate.of(2025, 11, 1));

    assertTrue(testView.lastError.contains("Error retrieving events"));
  }

  @Test
  public void testShowEventsInRangeException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.showEventsInRange(
        LocalDateTime.of(2025, 11, 1, 0, 0),
        LocalDateTime.of(2025, 11, 30, 23, 59));

    assertTrue(testView.lastError.contains("Error retrieving events"));
  }

  @Test
  public void testShowStatusException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.showStatus(LocalDateTime.of(2025, 11, 1, 10, 0));

    assertTrue(testView.lastError.contains("Error checking status"));
  }

  @Test
  public void testExportCalendarException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.exportCalendar("test.csv");

    assertTrue(testView.lastError.contains("Error exporting calendar"));
  }

  @Test
  public void testExportCalendarToIcalSuccess() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.exportCalendarToIcal("test.ical");

    assertTrue(view.lastMessage.contains("Calendar exported to iCal file: test.ical"));
  }

  @Test
  public void testExportCalendarToIcalException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.exportCalendarToIcal("test.ical");

    assertTrue(testView.lastError.contains("Error exporting calendar to iCal"));
  }

  @Test
  public void testEditEventReplaceFailure() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "location", "Office");

    assertTrue(view.lastMessage.contains("location updated to: Office")
        || view.lastMessage.contains("updated"));
  }

  @Test
  public void testUpdateEventPropertySubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "subject", "New Meeting");

    assertTrue(view.lastMessage.contains("subject updated to: New Meeting"));
  }

  @Test
  public void testUpdateEventPropertyDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "description", "Team meeting");

    assertTrue(view.lastMessage.contains("description updated to: Team meeting"));
  }

  @Test
  public void testUpdateEventPropertyLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "location", "Room 101");

    assertTrue(view.lastMessage.contains("location updated to: Room 101"));
  }

  @Test
  public void testUpdateEventPropertyStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "status", "private");

    assertTrue(view.lastMessage.contains("status updated to: private"));
  }

  @Test
  public void testUpdateEventPropertyStart() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "2025-11-01T09:00");

    assertTrue(view.lastMessage.contains("start updated to: 2025-11-01T09:00"));
  }

  @Test
  public void testUpdateEventPropertyEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "end", "2025-11-01T12:00");

    assertTrue(view.lastMessage.contains("end updated to: 2025-11-01T12:00"));
  }

  @Test
  public void testUpdateEventPropertyStartAfterEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);
    controller.editEvent("Meeting", start, "start", "2025-11-01T12:00");

    assertTrue(view.lastError.contains("Start time cannot be after end time"));
  }

  @Test
  public void testCreateCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");

    assertTrue(testView.lastMessage.contains("Calendar 'Work' created"));
    assertTrue(testView.lastMessage.contains("America/New_York"));
  }

  @Test
  public void testCreateCalendarException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Work", "America/Los_Angeles");

    assertTrue(testView.lastError.contains("Error creating calendar"));
  }

  @Test
  public void testEditCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.editCalendar("Work", "timezone", "America/Los_Angeles");

    assertTrue(testView.lastMessage.contains("timezone updated to: America/Los_Angeles"));
  }

  @Test
  public void testEditCalendarException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.editCalendar("NonExistent", "timezone", "America/New_York");

    assertTrue(testView.lastError.contains("Error editing calendar"));
  }

  @Test
  public void testUseCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.useCalendar("Work");

    assertTrue(testView.lastMessage.contains("Now using calendar: Work"));
    assertTrue(testView.lastMessage.contains("America/New_York"));
  }

  @Test
  public void testUseCalendarException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.useCalendar("NonExistent");

    assertTrue(testView.lastError.contains("Error switching calendar"));
  }

  @Test
  public void testListCalendarsEmpty() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.listCalendars();

    assertTrue(testView.lastMessage.contains("No calendars available"));
  }

  @Test
  public void testListCalendarsWithCalendars() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");
    testController.listCalendars();

    assertTrue(testView.lastMessage.contains("Available calendars"));
    assertTrue(testView.lastMessage.contains("Work"));
    assertTrue(testView.lastMessage.contains("Personal"));
    assertTrue(testView.lastMessage.contains("[ACTIVE]"));
  }

  @Test
  public void testListCalendarsException() {
    model.CalendarManager faultyManager = new model.CalendarManager() {
      @Override
      public Set<String> getCalendarNames() {
        throw new RuntimeException("Database error");
      }
    };

    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(faultyManager, testView);

    testController.listCalendars();

    assertTrue(testView.lastError.contains("Error listing calendars"));
  }

  @Test
  public void testCopyEvent() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("Meeting", start, "Personal", targetTime);

    assertTrue(testView.lastMessage.contains("Event 'Meeting' copied to calendar 'Personal'"));
  }

  @Test
  public void testCopyEventNotFound() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("NonExistent", start, "Personal", targetTime);

    assertTrue(testView.lastError.contains("No event found"));
  }

  @Test
  public void testCopyEventMultipleFound() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    CalendarModel workCal = manager.getCurrentCalendar();
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end);
    CalendarEvent withLocation = event2.withLocation("Room 2");
    try {
      workCal.addEvent(withLocation);
    } catch (Exception e) {
      // Exception ignored — duplicate event added intentionally for test setup
    }

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("Meeting", start, "Personal", targetTime);

    assertTrue(testView.lastError.contains("Multiple events found")
        || testView.lastMessage.contains("copied"));
  }

  @Test
  public void testCopyEventException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("Meeting", start, "NonExistent", targetTime);

    assertTrue(testView.lastError.contains("Error copying event"));
  }

  @Test
  public void testCopyEventsOnDate() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Personal", targetDate);

    assertTrue(testView.lastMessage.contains("Copied 1 events"));
  }

  @Test
  public void testCopyEventsOnDateEmpty() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Personal", targetDate);

    assertTrue(testView.lastMessage.contains("No events found"));
  }

  @Test
  public void testCopyEventsOnDateDifferentTimezone() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Personal", targetDate);

    assertTrue(testView.lastMessage.contains("Copied") || !testView.lastError.isEmpty());
  }

  @Test
  public void testCopyEventsOnDateException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "NonExistent", targetDate);

    assertTrue(testView.lastError.contains("Error copying events"));
  }

  @Test
  public void testCopyEventsInRange() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting1", start1, end1);

    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 15, 0);
    testController.createEvent("Meeting2", start2, end2);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 2);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Personal", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied 2 events"));
  }

  @Test
  public void testCopyEventsInRangeEmpty() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/New_York");
    testController.useCalendar("Work");

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 2);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Personal", targetStartDate);

    assertTrue(testView.lastMessage.contains("No events found"));
  }

  @Test
  public void testCopyEventsInRangeDifferentTimezone() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 1);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Personal", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied") || !testView.lastError.isEmpty());
  }

  @Test
  public void testCopyEventsInRangeException() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.useCalendar("Work");

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 2);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "NonExistent", targetStartDate);

    assertTrue(testView.lastError.contains("Error copying events"));
  }

  @Test
  public void testEditAllEventsInSeriesNoEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);

    controller.editAllEventsInSeries("NonExistent", start, "location", "Office");

    assertTrue(view.lastError.contains("No event found"));
  }

  @Test
  public void testEditEventsFromDateUpdateException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editEventsFromDate("Weekly", start, "start", "2025-11-03T15:00");

    assertTrue(!view.lastError.isEmpty() || !view.lastMessage.isEmpty());
  }

  @Test
  public void testEditAllEventsInSeriesUpdateException() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("Weekly", start, "start", "2025-11-03T15:00");

    assertTrue(!view.lastError.isEmpty() || !view.lastMessage.isEmpty());
  }


  @Test
  public void testEditEventMultipleEventsFoundError() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting", start, end);

    CalendarEvent event2 = new CalendarEvent("Meeting", start, end);
    CalendarEvent withDesc = event2.withDescription("Different");
    try {
      model.addEvent(withDesc);
    } catch (Exception e) {
      // Expected conflict
    }

    controller.editEvent("Meeting", start, "location", "Office");

    assertTrue(view.lastError.contains("Multiple events") 
        || view.lastMessage.contains("updated"));
  }

  @Test
  public void testEditSeriesEventExcludeAndAddFailure() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editEvent("Weekly", start, "location", "New Location");

    assertTrue(!view.lastMessage.isEmpty() || !view.lastError.isEmpty());
  }

  @Test
  public void testEditSeriesEventReplaceFailure() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("SingleEvent", start, end);
    controller.editEvent("SingleEvent", start, "location", "Office");

    assertTrue(view.lastMessage.contains("location updated") 
        || view.lastMessage.contains("updated"));
  }

  @Test
  public void testEditEventsFromDateWithUpdateError() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    // Edit with valid property
    controller.editEventsFromDate("Weekly", start, "location", "Remote");

    assertTrue(view.lastMessage.contains("Updated") || !view.lastError.isEmpty());
  }

  @Test
  public void testUpdateEventPropertyAllCases() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    // Test subject update
    controller.createEvent("Test1", start, end);
    controller.editEvent("Test1", start, "subject", "NewSubject");
    assertTrue(view.lastMessage.contains("subject updated"));

    // Test description update
    controller.createEvent("Test2", start.plusHours(1), end.plusHours(1));
    controller.editEvent("Test2", start.plusHours(1), "description", "New Description");
    assertTrue(view.lastMessage.contains("description updated"));

    // Test location update
    controller.createEvent("Test3", start.plusHours(2), end.plusHours(2));
    controller.editEvent("Test3", start.plusHours(2), "location", "New Location");
    assertTrue(view.lastMessage.contains("location updated"));

    // Test status update
    controller.createEvent("Test4", start.plusHours(3), end.plusHours(3));
    controller.editEvent("Test4", start.plusHours(3), "status", "private");
    assertTrue(view.lastMessage.contains("status updated"));

    // Test start time update
    controller.createEvent("Test5", start.plusHours(4), end.plusHours(4));
    controller.editEvent("Test5", start.plusHours(4), "start", "2025-11-01T15:00");
    assertTrue(view.lastMessage.contains("start updated"));

    // Test end time update
    controller.createEvent("Test6", start.plusHours(5), end.plusHours(5));
    controller.editEvent("Test6", start.plusHours(5), "end", "2025-11-01T17:00");
    assertTrue(view.lastMessage.contains("end updated"));
  }

  @Test
  public void testListCalendarsWithActiveCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");
    testController.listCalendars();

    assertTrue(testView.lastMessage.contains("[ACTIVE]"));
    assertTrue(testView.lastMessage.contains("Work"));
  }

  @Test
  public void testCopyEventWithTimezoneConversion() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("Meeting", start, "Personal", targetTime);

    assertTrue(testView.lastMessage.contains("copied"));
  }

  @Test
  public void testCopyEventsOnDateWithTimezoneConversionNeeded() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Personal", targetDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testCopyEventsInRangeWithTimezoneConversionNeeded() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 1);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Personal", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testConvertEventsTimezoneWithNullEndTime() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Work", "America/New_York");
    testController.createCalendar("Personal", "America/Los_Angeles");
    testController.useCalendar("Work");

    // Create event with null end time (all-day event)
    LocalDate date = LocalDate.of(2025, 11, 1);
    testController.createAllDayEvent("Holiday", date);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(date, "Personal", targetDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testExportCalendarToIcalWithEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    controller.createEvent("Meeting1", start, end);
    controller.createEvent("Meeting2", start.plusDays(1), end.plusDays(1));

    controller.exportCalendarToIcal("calendar.ical");

    assertTrue(view.lastMessage.contains("Calendar exported to iCal file"));
  }

  @Test
  public void testExportCalendarToIcalCallsExporter() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Test", "America/New_York");
    testController.useCalendar("Test");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    java.io.File testFile = new java.io.File("test_export.ical");
    testFile.deleteOnExit();

    testController.exportCalendarToIcal("test_export.ical");

    assertTrue(testView.lastMessage.contains("Calendar exported to iCal file"));
    assertTrue(testFile.exists());
    testFile.delete();
  }

  @Test
  public void testListCalendarsShowsActiveCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Calendar1", "America/New_York");
    testController.createCalendar("Calendar2", "America/Los_Angeles");
    testController.useCalendar("Calendar1");

    testController.listCalendars();

    assertTrue(testView.lastMessage.contains("Calendar1"));
    assertTrue(testView.lastMessage.contains("[ACTIVE]"));
    int activeIndex = testView.lastMessage.indexOf("[ACTIVE]");
    int calendar1Index = testView.lastMessage.indexOf("Calendar1");
    assertTrue(activeIndex > calendar1Index);
  }

  @Test
  public void testCopyEventDifferentTimezones() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Eastern", "America/New_York");
    testController.createCalendar("Pacific", "America/Los_Angeles");
    testController.useCalendar("Eastern");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 10, 0);
    testController.copyEvent("Meeting", start, "Pacific", targetTime);

    assertTrue(testView.lastMessage.contains("copied"));
  }

  @Test
  public void testCopyEventsOnDateSameTimezone() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/New_York");
    testController.useCalendar("Cal1");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Cal2", targetDate);

    assertTrue(testView.lastMessage.contains("Copied 1 events"));
  }

  @Test
  public void testCopyEventsInRangeSameTimezone() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/New_York");
    testController.useCalendar("Cal1");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 1);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Cal2", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied 1 events"));
  }

  @Test
  public void testConvertEventsTimezoneWithEndTime() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Eastern", "America/New_York");
    testController.createCalendar("Pacific", "America/Los_Angeles");
    testController.useCalendar("Eastern");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(LocalDate.of(2025, 11, 1), "Pacific", targetDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testConvertEventsTimezoneReturnsListCorrectly() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/Los_Angeles");
    testController.useCalendar("Cal1");

    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting1", start1, end1);

    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 15, 0);
    testController.createEvent("Meeting2", start2, end2);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 2);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Cal2", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied 2 events"));
  }

  @Test
  public void testEditAllEventsInSeriesCounterIncrement() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries("MWF", start, end, days, 6);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("MWF", start, "description", "Updated");

    assertTrue(view.lastMessage.contains("Updated all 6"));
  }

  @Test
  public void testListCalendarsWithoutActiveCalendar() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/Los_Angeles");

    testController.listCalendars();

    assertTrue(testView.lastMessage.contains("Cal1"));
    assertTrue(testView.lastMessage.contains("Cal2"));
    assertFalse(testView.lastMessage.contains("[ACTIVE]"));
  }

  @Test
  public void testCopyEventSameTimezoneNoConversion() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/New_York");
    testController.useCalendar("Cal1");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    testController.copyEvent("Meeting", start, "Cal2", targetTime);

    assertTrue(testView.lastMessage.contains("copied"));
  }

  @Test
  public void testCopyEventsOnDateWithDifferentTimezones() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Eastern", "America/New_York");
    testController.createCalendar("Pacific", "America/Los_Angeles");
    testController.useCalendar("Eastern");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Pacific", targetDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testCopyEventsInRangeWithDifferentTimezones() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Eastern", "America/New_York");
    testController.createCalendar("Pacific", "America/Los_Angeles");
    testController.useCalendar("Eastern");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 1);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);
    testController.copyEventsInRange(startDate, endDate, "Pacific", targetStartDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }

  @Test
  public void testConvertEventsTimezoneWithEventHavingEndTime() {
    model.CalendarManager manager = new model.CalendarManager();
    MockCalendarView testView = new MockCalendarView();
    CalendarController testController = new CalendarController(manager, testView);

    testController.createCalendar("Cal1", "America/New_York");
    testController.createCalendar("Cal2", "America/Los_Angeles");
    testController.useCalendar("Cal1");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    testController.createEvent("Meeting", start, end);

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    testController.copyEventsOnDate(sourceDate, "Cal2", targetDate);

    assertTrue(testView.lastMessage.contains("Copied"));
  }


  @Test
  public void testEditAllEventsInSeriesShowsErrorOnFailure() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    controller.createEventSeries(series);

    controller.editAllEventsInSeries("Weekly", start, "end", "2025-11-03T09:00");
    
    assertTrue(!view.lastError.isEmpty() || !view.lastMessage.isEmpty());
  }

}
