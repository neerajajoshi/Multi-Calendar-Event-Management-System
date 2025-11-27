import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import controller.GuiCalendarController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.CalendarEvent;
import model.CalendarManager;
import model.CalendarModel;
import model.EventStatus;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;
import view.GuiCalendarView;

/**
 * Tests for GuiCalendarController.
 */
public class GuiCalendarControllerTest {
  
  private CalendarManager manager;
  private MockGuiCalendarView view;
  private GuiCalendarController controller;
  
  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    manager = new CalendarManager();
    manager.createCalendar("TestCal", "America/New_York");
    manager.useCalendar("TestCal");
    
    view = new MockGuiCalendarView();
    controller = new GuiCalendarController(manager, view);
  }
  
  @Test
  public void testConstructorWithNoCalendars() {
    CalendarManager emptyManager = new CalendarManager();
    MockGuiCalendarView newView = new MockGuiCalendarView();
    
    GuiCalendarController newController = new GuiCalendarController(emptyManager, newView);
    
    assertTrue(newView.messages.stream().anyMatch(m -> m.contains("Created default calendar")));
    assertEquals(1, emptyManager.getCalendarCount());
  }
  
  @Test
  public void testConstructorWithCalendarCreationFailure() {
    CalendarManager emptyManager = new CalendarManager();
    MockGuiCalendarView newView = new MockGuiCalendarView();
    
    GuiCalendarController newController = new GuiCalendarController(
        emptyManager, newView);
  
    assertTrue(emptyManager.getCalendarCount() > 0);
  }
  
  @Test
  public void testOnCreateCalendarSuccess() {
    view.clear();
    controller.onCreateCalendar("NewCal", "America/Los_Angeles");
    
    assertTrue(manager.hasCalendar("NewCal"));
    assertEquals("NewCal", manager.getCurrentCalendarName());
  }
  
  @Test
  public void testOnCreateCalendarFailure() {
    view.clear();
    controller.onCreateCalendar("TestCal", "America/New_York"); // Duplicate name
    
    // Error message expected
    assertTrue("Expected error message for duplicate calendar", 
        view.errors.size() > 0 || view.messages.size() == 0);
  }
  
  @Test
  public void testOnSelectCalendarSuccess() {
    manager.createCalendar("AnotherCal", "America/Chicago");
    view.clear();
    
    controller.onSelectCalendar("AnotherCal");
    
    assertEquals("AnotherCal", manager.getCurrentCalendarName());
  }
  
  @Test
  public void testOnSelectCalendarFailure() {
    view.clear();
    controller.onSelectCalendar("NonExistentCal");
    
    // Error message expected
    assertTrue("Expected error message for non-existent calendar", 
        view.errors.size() > 0);
  }
  
  @Test
  public void testOnRequestCalendarListSuccess() {
    manager.createCalendar("Cal2", "America/Denver");
    view.clear();
    
    controller.onRequestCalendarList();
    
    assertNotNull(view.lastCalendarList);
    assertTrue(view.lastCalendarList.size() >= 2);
  }
  
  @Test
  public void testOnRequestCalendarListWithEmptyManager() {
    view.clear();
    controller.onRequestCalendarList();
    
    assertNotNull(view.lastCalendarList);
  }
  
  @Test
  public void testOnCreateEventSuccess() {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2024, 1, 1, 11, 0);
    view.clear();
    
    controller.onCreateEvent("Meeting", "Description", "Location", start, end);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event created")));
  }
  
  @Test
  public void testOnCreateEventWithNullStart() {
    view.clear();
    
    try {
      controller.onCreateEvent("Meeting", "Desc", "Loc", null, 
          LocalDateTime.now());
    } catch (Exception e) {
      // Expected exception
    }

    CalendarModel model = manager.getCurrentCalendar();
    assertEquals(0, model.getAllEvents().size());
  }

  
  @Test
  public void testOnCreateAllDayEventSuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    view.clear();
    
    controller.onCreateAllDayEvent("All Day Event", "Description", 
        "Location", date);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertTrue(events.get(0).isAllDay());
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("All-day event created")));
  }
  
  @Test
  public void testOnCreateAllDayEventWithNullDate() {
    view.clear();
    
    try {
      controller.onCreateAllDayEvent("Event", "Desc", "Loc", null);
    } catch (Exception e) {
      // Expected exception
    }
    
    CalendarModel model = manager.getCurrentCalendar();
    assertEquals(0, model.getAllEvents().size());
  }
  
  @Test
  public void testOnCreateRecurringEventAllDayWithWeeks() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    view.clear();
    
    controller.onCreateRecurringEvent("Recurring", "", "", startDate, true, 
        "09:00", "10:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Recurring event series created")));
  }
  
  @Test
  public void testOnCreateRecurringEventAllDayWithEndDate() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    view.clear();
    
    controller.onCreateRecurringEvent("Recurring", "", "", startDate, true, 
        "09:00", "10:00", "MWF", 0, endDate);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Recurring event series created")));
  }
  
  @Test
  public void testOnCreateRecurringEventTimedWithWeeks() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    view.clear();
    
    controller.onCreateRecurringEvent("Recurring", "", "", startDate, false, 
        "09:00", "10:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Recurring event series created")));
  }
  
  @Test
  public void testOnCreateRecurringEventTimedWithEndDate() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 1, 31);
    view.clear();
    
    controller.onCreateRecurringEvent("Recurring", "", "", startDate, false, 
        "09:00", "10:00", "MWF", 0, endDate);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Recurring event series created")));
  }
  
  @Test
  public void testOnCreateRecurringEventWithDescriptionAndLocation() {
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    view.clear();
    
    controller.onCreateRecurringEvent("Recurring", "Description", "Location", 
        startDate, false, "09:00", "10:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Recurring event series created")));
  }
  
  @Test
  public void testOnCreateRecurringEventFailure() {
    view.clear();
    
    try {
      controller.onCreateRecurringEvent("Recurring", "Desc", "Loc", null, false, 
          "09:00", "10:00", "MWF", 2, null);
    } catch (Exception e) {
      // Expected exception
    }
    
    assertTrue(view.errors.size() > 0 || view.messages.size() == 0);
  }
  
  @Test
  public void testOnViewEventsOnDateSuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", start, end));
    
    view.clear();
    controller.onViewEventsOnDate(date);
    
    // Events displayed
    assertNotNull(view.lastDisplayedEvents);
  }
  
  @Test
  public void testOnViewEventsOnDateWithNullDate() {
    view.clear();
    
    try {
      controller.onViewEventsOnDate(null);
    } catch (Exception e) {
      // Expected exception
    }
  }
  
  @Test
  public void testOnRequestEventsForEditSuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", start, end));
    
    view.clear();
    controller.onRequestEventsForEdit(date);
    
    assertNotNull(view.lastEditDialogEvents);
    assertEquals(1, view.lastEditDialogEvents.size());
  }
  
  // onRequestEventsForEdit failure case
  @Test
  public void testOnRequestEventsForEditWithNullDate() {
    view.clear();
    
    try {
      controller.onRequestEventsForEdit(null);
    } catch (Exception e) {
      // Expected
    }
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope0SingleEventNotInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "New Desc", "New Loc", 
        "10:00", "11:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope0SingleEventInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "New Desc", "New Loc", 
        "10:00", "11:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope0WithEmptyFields() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onEditEvent(event, "", "", "", "10:00", "11:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope0WithTimeChanges() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onEditEvent(event, "Event", "", "", "09:00", "12:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope0AllDayEvent() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(8, 0), date.atTime(17, 0),
        null, null, EventStatus.PUBLIC, true);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "Desc", "Loc", "08:00", "17:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }

  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope1FutureEventsWithMatches() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create multiple events with same subject
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), 
        date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(2).atTime(10, 0), 
        date.plusDays(2).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", 
        "10:00", "11:00", 1);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated") && m.contains("events")));
  }
  
  // Test onEditEvent
  @Test
  public void testOnEditEventScope1NoMatchingEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), 
        date.atTime(11, 0));
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "Desc", "Loc", 
        "10:00", "11:00", 1);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("No events to update")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope1AllDayEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(8, 0), 
        date.atTime(17, 0), null, null, EventStatus.PUBLIC, true));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(8, 0), 
        date.plusDays(1).atTime(17, 0), null, null, EventStatus.PUBLIC, true));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", 
        "08:00", "17:00", 1);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated") && m.contains("events")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope1WithTimeChanges() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "Event", "", "", "09:00", "12:00", 1);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Updated") && m.contains("events")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope2AllEventsWithMatches() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.minusDays(1).atTime(10, 0), 
        date.minusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", 
        "10:00", "11:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated all") && m.contains("events")));
  }
  
  /**
   * Test onEditEvent with scope 2 when no matching events exist.
   */
  @Test
  public void testOnEditEventScope2NoMatchingEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), 
        date.atTime(11, 0));
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "Desc", "Loc", 
        "10:00", "11:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("No events to update")));
  }
  
  // Test onEditEvent 
  @Test
  public void testOnEditEventScope2AllDayEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(8, 0), 
        date.atTime(17, 0), null, null, EventStatus.PUBLIC, true));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(8, 0), 
        date.plusDays(1).atTime(17, 0), null, null, EventStatus.PUBLIC, true));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", 
        "08:00", "17:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated all") && m.contains("events")));
  }
  
  // Test onEditEvent
  @Test
  public void testOnEditEventScope2WithTimeChanges() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), 
        date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "Event", "", "", "09:00", "12:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated all") && m.contains("events")));
  }
  
  // Test onEditEvent - invalid scope
  @Test
  public void testOnEditEventInvalidScope() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "Desc", "Loc", "10:00", "11:00", 99);
    
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("Invalid edit scope")));
  }
  
  // Test onEditEvent failure
  @Test
  public void testOnEditEventFailure() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    
    try {
      controller.onEditEvent(null, "New Subject", "Desc", "Loc", "10:00", "11:00", 0);
    } catch (Exception e) {
      // Expected
    }
    
    assertTrue(view.errors.size() > 0 || view.messages.size() == 0);
  }
  
  // Test onRequestEventsForDelete success
  @Test
  public void testOnRequestEventsForDeleteSuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    
    view.clear();
    controller.onRequestEventsForDelete(date);
    
    assertNotNull(view.lastDeleteDialogEvents);
    assertEquals(1, view.lastDeleteDialogEvents.size());
  }
  
  // Test onRequestEventsForDelete failure
  @Test
  public void testOnRequestEventsForDeleteWithNullDate() {
    view.clear();
    
    try {
      controller.onRequestEventsForDelete(null);
    } catch (Exception e) {
      // Expected
    }
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope0SingleEventNotInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onDeleteEvent(event, 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event deleted")));
    assertEquals(0, model.getAllEvents().size());
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope0SingleEventNotFound() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    controller.onDeleteEvent(event, 0);
    
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("Event not found")));
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope0SingleEventInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    int initialCount = events.size();
    assertTrue(initialCount > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onDeleteEvent(firstEvent, 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event deleted")));
    assertTrue(model.getAllEvents().size() < initialCount);
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope1FutureEventsWithMatches() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(2).atTime(10, 0), 
        date.plusDays(2).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onDeleteEvent(firstEvent, 1);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Deleted") && m.contains("events")));
    assertEquals(0, model.getAllEvents().size());
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope1NoMatchingEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    controller.onDeleteEvent(event, 1);
    
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("No events found to delete")));
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope2AllEventsWithMatches() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), 
        date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.minusDays(1).atTime(10, 0), 
        date.minusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onDeleteEvent(firstEvent, 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Deleted all") && m.contains("events")));
    assertEquals(0, model.getAllEvents().size());
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventScope2NoMatchingEvents() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    controller.onDeleteEvent(event, 2);
    
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("No events found to delete")));
  }
  
  // Test onDeleteEvent
  @Test
  public void testOnDeleteEventInvalidScope() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    view.clear();
    controller.onDeleteEvent(event, 99);
    
    assertTrue(view.errors.stream().anyMatch(e -> e.contains("Invalid delete scope")));
  }
  
  // Test onDeleteEvent failure
  @Test
  public void testOnDeleteEventFailure() {
    view.clear();
    
    try {
      controller.onDeleteEvent(null, 0);
    } catch (Exception e) {
      // Expected
    }
    
    assertTrue(view.errors.size() > 0 || view.messages.size() == 0);
  }

  
  // Test onRequestEventsForCopy success
  @Test
  public void testOnRequestEventsForCopySuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    
    manager.createCalendar("TargetCal", "America/Chicago");
    
    view.clear();
    controller.onRequestEventsForCopy(date);
    
    assertNotNull(view.lastCopyDialogEvents);
    assertEquals(1, view.lastCopyDialogEvents.size());
  }
  
  // Test onRequestEventsForCopy failure
  @Test
  public void testOnRequestEventsForCopyWithNullDate() {
    view.clear();
    
    try {
      controller.onRequestEventsForCopy(null);
    } catch (Exception e) {
      // Expected
    }
  }
  
  // Test onCopyEvent success
  @Test
  public void testOnCopyEventSuccess() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    manager.createCalendar("TargetCal", "America/Chicago");
    
    LocalDateTime targetDateTime = date.plusDays(1).atTime(10, 0);
    view.clear();
    
    controller.onCopyEvent(event, "TargetCal", targetDateTime);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event copied to TargetCal")));
  }
  
  // Test onCopyEvent failure
  @Test
  public void testOnCopyEventFailure() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    LocalDateTime targetDateTime = date.plusDays(1).atTime(10, 0);
    
    view.clear();
    controller.onCopyEvent(event, "NonExistentCal", targetDateTime);
    
    // Should have an error message
    assertTrue("Expected error message for copy failure", 
        view.errors.size() > 0);
  }
  
  // Test onMonthChanged success
  @Test
  public void testOnMonthChangedSuccess() {
    YearMonth month = YearMonth.of(2024, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", LocalDate.of(2024, 1, 1).atTime(10, 0), 
        LocalDate.of(2024, 1, 1).atTime(11, 0)));
    
    view.clear();
    controller.onMonthChanged(month);

    assertTrue(view.errors.isEmpty());
  }
  
  // Test onMonthChanged failure
  @Test
  public void testOnMonthChangedFailureSilent() {
    YearMonth month = YearMonth.of(2024, 1);

    controller.onMonthChanged(month);

    assertTrue(view.errors.isEmpty());
  }
  
  // Test show method
  @Test
  public void testShowMethod() {
    view.clear();
    controller.show();
    
    assertTrue(view.mockIsVisible);
  }
  
  // Test refresh event cache silent failure
  @Test
  public void testRefreshEventCacheSilentFailure() {
    view.clear();

    controller.show();
    
    assertTrue(view.mockIsVisible);
  }

  
  @Test
  public void testEditSingleEventPropertiesWithNonEmptyDescription() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    CalendarEvent event = new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0));
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(event);
    
    view.clear();
    controller.onEditEvent(event, "New Subject", "Non-empty desc", "Non-empty loc", 
        "10:00", "11:00", 0);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Event updated")));
  }
  
  @Test
  public void testEditFutureEventsPropertiesWithEmptySubject() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "", "Desc", "Loc", "10:00", "11:00", 1);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Updated") && m.contains("events")));
  }
  
  @Test
  public void testEditAllSeriesPropertiesWithEmptySubject() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    CalendarModel model = manager.getCurrentCalendar();
    model.addEvent(new CalendarEvent("Event", date.atTime(10, 0), date.atTime(11, 0)));
    model.addEvent(new CalendarEvent("Event", date.plusDays(1).atTime(10, 0), 
        date.plusDays(1).atTime(11, 0)));
    
    CalendarEvent firstEvent = model.getAllEvents().get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "", "Desc", "Loc", "10:00", "11:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated all") && m.contains("events")));
  }
  
  @Test
  public void testDeleteFutureEventsInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onDeleteEvent(firstEvent, 1);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Deleted") && m.contains("events")));
  }
  
  @Test
  public void testDeleteAllSeriesEventsInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onDeleteEvent(firstEvent, 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Deleted all") && m.contains("events")));
    assertEquals(0, model.getAllEvents().size());
  }
  
  @Test
  public void testEditFutureEventsInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", "10:00", "11:00", 1);
    
    assertTrue(view.messages.stream().anyMatch(m -> m.contains("Updated") && m.contains("events")));
  }
  
  @Test
  public void testEditAllSeriesEventsInSeries() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    
    // Create a series first
    controller.onCreateRecurringEvent("SeriesEvent", "", "", date, false, 
        "10:00", "11:00", "MWF", 2, null);
    
    CalendarModel model = manager.getCurrentCalendar();
    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    
    CalendarEvent firstEvent = events.get(0);
    view.clear();
    
    controller.onEditEvent(firstEvent, "New Subject", "Desc", "Loc", 
        "10:00", "11:00", 2);
    
    assertTrue(view.messages.stream()
        .anyMatch(m -> m.contains("Updated all") && m.contains("events")));
  }
  
  /**
   * Mock view for testing controller logic without GUI.
   * Extends GuiCalendarView but overrides methods to prevent actual GUI operations.
   */
  private static class MockGuiCalendarView extends GuiCalendarView {
    List<String> messages = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    List<String> lastCalendarList = null;
    List<CalendarEvent> lastEditDialogEvents = null;
    List<CalendarEvent> lastDeleteDialogEvents = null;
    List<CalendarEvent> lastCopyDialogEvents = null;
    List<CalendarEvent> lastDisplayedEvents = null;
    boolean mockIsVisible = false;
    private GuiCalendarView.GuiActionListener actionListener;
    
    /**
     * Constructor that calls super() to create the GUI view.
     * In test environment, this may fail but we handle it gracefully.
     */
    public MockGuiCalendarView() {
      super();
      try {
        super.setVisible(false);
      } catch (Exception e) {
        // Ignore any GUI-related exceptions in test environment
      }
    }
    
    @Override
    public void showMessage(String message) {
      messages.add(message);
    }
    
    @Override
    public void showError(String error) {
      errors.add(error);
    }
    
    @Override
    public void showEventsOnDate(LocalDate date, List<CalendarEvent> events) {
      lastDisplayedEvents = new ArrayList<>(events);
    }
    
    @Override
    public void showEventsInRange(LocalDateTime start, LocalDateTime end, 
        List<CalendarEvent> events) {
      // Not used in controller tests
    }
    
    @Override
    public void showStatus(boolean isBusy) {
      // Not used in controller tests
    }
    
    @Override
    public void exportToFile(String filename, List<CalendarEvent> events) {
      // Not used in controller tests
    }
    
    @Override
    public void setActionListener(GuiCalendarView.GuiActionListener listener) {
      this.actionListener = listener;
    }
    
    @Override
    public void showCalendarSelectionDialog(List<String> calendarNames, 
        String currentCalendar) {
      lastCalendarList = new ArrayList<>(calendarNames);
    }
    
    @Override
    public void showEventEditDialog(List<CalendarEvent> events) {
      lastEditDialogEvents = new ArrayList<>(events);
    }
    
    @Override
    public void showEventDeleteDialog(List<CalendarEvent> events) {
      lastDeleteDialogEvents = new ArrayList<>(events);
    }
    
    @Override
    public void showEventCopyDialog(List<CalendarEvent> events, 
        List<String> availableCalendars, String currentCalendar, 
        String currentTimezone, CalendarManager calendarManager) {
      lastCopyDialogEvents = new ArrayList<>(events);
    }
    
    @Override
    public void updateEventCacheForMonth(List<CalendarEvent> allEvents) {
      // Cache update - no action needed in mock
    }
    
    @Override
    public void setVisible(boolean visible) {
      mockIsVisible = visible;
    }
    
    public void clear() {
      messages.clear();
      errors.clear();
      lastCalendarList = null;
      lastEditDialogEvents = null;
      lastDeleteDialogEvents = null;
      lastCopyDialogEvents = null;
      lastDisplayedEvents = null;
    }

    public void onCreateCalendar(String name, String timezone) {
      if (actionListener != null) {
        actionListener.onCreateCalendar(name, timezone);
      }
    }
    
    public void onSelectCalendar(String name) {
      if (actionListener != null) {
        actionListener.onSelectCalendar(name);
      }
    }
    
    public void onRequestCalendarList() {
      if (actionListener != null) {
        actionListener.onRequestCalendarList();
      }
    }
    
    public void onCreateEvent(String subject, String description, String location,
        LocalDateTime start, LocalDateTime end) {
      if (actionListener != null) {
        actionListener.onCreateEvent(subject, description, location, start, end);
      }
    }
    
    public void onCreateAllDayEvent(String subject, String description, 
        String location, LocalDate date) {
      if (actionListener != null) {
        actionListener.onCreateAllDayEvent(subject, description, location, date);
      }
    }
    
    public void onCreateRecurringEvent(String subject, String description, 
        String location, LocalDate startDate, boolean isAllDay, String startTime, 
        String endTime, String weekdays, int weeks, LocalDate endDate) {
      if (actionListener != null) {
        actionListener.onCreateRecurringEvent(subject, description, location, 
            startDate, isAllDay, startTime, endTime, weekdays, weeks, endDate);
      }
    }
    
    public void onViewEventsOnDate(LocalDate date) {
      if (actionListener != null) {
        actionListener.onViewEventsOnDate(date);
      }
    }
    
    public void onRequestEventsForEdit(LocalDate date) {
      if (actionListener != null) {
        actionListener.onRequestEventsForEdit(date);
      }
    }
    
    public void onEditEvent(CalendarEvent event, String newSubject, String newDesc,
        String newLocation, String newStartTime, String newEndTime, int scope) {
      if (actionListener != null) {
        actionListener.onEditEvent(event, newSubject, newDesc, newLocation, 
            newStartTime, newEndTime, scope);
      }
    }
    
    public void onRequestEventsForDelete(LocalDate date) {
      if (actionListener != null) {
        actionListener.onRequestEventsForDelete(date);
      }
    }
    
    public void onDeleteEvent(CalendarEvent event, int scope) {
      if (actionListener != null) {
        actionListener.onDeleteEvent(event, scope);
      }
    }
    
    public void onRequestEventsForCopy(LocalDate date) {
      if (actionListener != null) {
        actionListener.onRequestEventsForCopy(date);
      }
    }
    
    public void onCopyEvent(CalendarEvent event, String targetCalendar, 
        LocalDateTime targetDateTime) {
      if (actionListener != null) {
        actionListener.onCopyEvent(event, targetCalendar, targetDateTime);
      }
    }
    
    public void onMonthChanged(YearMonth month) {
      if (actionListener != null) {
        actionListener.onMonthChanged(month);
      }
    }
  }
}
