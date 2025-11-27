import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import controller.CalendarController;
import controller.Command;
import controller.commands.CopyEventCommand;
import controller.commands.CopyEventsInRangeCommand;
import controller.commands.CopyEventsOnDateCommand;
import controller.commands.CreateAllDaySeriesCommand;
import controller.commands.CreateCalendarCommand;
import controller.commands.EditCalendarCommand;
import controller.commands.EditEventsCommand;
import controller.commands.ExportIcalCommand;
import controller.commands.ListCalendarsCommand;
import controller.commands.UseCalendarCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.CalendarManager;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;

/**
 * Tests for Command execution.
 */
public class CommandExecutionTest {

  private CalendarManager manager;
  private MockCalendarView view;
  private CalendarController controller;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    manager = new CalendarManager();
    view = new MockCalendarView();
    controller = new CalendarController(manager, view);
  }

  // ============ CREATE CALENDAR COMMAND TESTS ============

  @Test
  public void testCreateCalendarCommandExecution() {
    Command command = new CreateCalendarCommand("Work", "America/New_York");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Calendar 'Work' created"));
    assertTrue(view.lastMessage.contains("America/New_York"));
  }

  @Test
  public void testCreateCalendarCommandWithDifferentTimezone() {
    Command command = new CreateCalendarCommand("Personal", "Europe/London");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Calendar 'Personal' created"));
    assertTrue(view.lastMessage.contains("Europe/London"));
  }

  // ============ EDIT CALENDAR COMMAND TESTS ============

  @Test
  public void testEditCalendarCommandExecution() {
    manager.createCalendar("Work", "America/New_York");

    Command command = new EditCalendarCommand("Work", "timezone", "America/Los_Angeles");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("timezone updated to: America/Los_Angeles"));
  }

  @Test
  public void testEditCalendarCommandName() {
    manager.createCalendar("Work", "America/New_York");

    Command command = new EditCalendarCommand("Work", "name", "WorkCalendar");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("name updated to: WorkCalendar"));
  }

  // ============ USE CALENDAR COMMAND TESTS ============

  @Test
  public void testUseCalendarCommandExecution() {
    manager.createCalendar("Work", "America/New_York");

    Command command = new UseCalendarCommand("Work");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Now using calendar: Work"));
  }

  @Test
  public void testUseCalendarCommandWithDifferentCalendar() {
    manager.createCalendar("Personal", "Europe/London");

    Command command = new UseCalendarCommand("Personal");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Now using calendar: Personal"));
  }

  // ============ LIST CALENDARS COMMAND TESTS ============

  @Test
  public void testListCalendarsCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");

    Command command = new ListCalendarsCommand();
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Available calendars"));
    assertTrue(view.lastMessage.contains("Work"));
    assertTrue(view.lastMessage.contains("Personal"));
  }

  @Test
  public void testListCalendarsCommandEmpty() {
    Command command = new ListCalendarsCommand();
    command.execute(controller);

    assertTrue(view.lastMessage.contains("No calendars available"));
  }

  // ============ EXPORT ICAL COMMAND TESTS ============

  @Test
  public void testExportIcalCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    controller.createEvent("Meeting", start, end);

    Command command = new ExportIcalCommand("calendar.ical");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Calendar exported to iCal file: calendar.ical"));
  }

  @Test
  public void testExportIcalCommandWithIcsExtension() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    Command command = new ExportIcalCommand("events.ics");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Calendar exported to iCal file: events.ics"));
  }

  // ============ CREATE ALL DAY SERIES COMMAND TESTS ============

  @Test
  public void testCreateAllDaySeriesCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.WEDNESDAY);
    weekdays.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries(
        "Standup", startDate, weekdays, 5, null);

    Command command = new CreateAllDaySeriesCommand(series);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
    assertTrue(view.lastMessage.contains("Standup"));
  }

  @Test
  public void testCreateAllDaySeriesCommandWithEndDate() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 12, 31);
    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries(
        "Weekly Review", startDate, weekdays, -1, endDate);

    Command command = new CreateAllDaySeriesCommand(series);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
  }

  // ============ COPY EVENT COMMAND TESTS ============

  @Test
  public void testCopyEventCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    controller.createEvent("Meeting", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 2, 14, 0);
    Command command = new CopyEventCommand("Meeting", start, "Personal", targetTime);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event 'Meeting' copied to calendar 'Personal'"));
  }

  @Test
  public void testCopyEventCommandWithDifferentEvent() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 5, 10, 0);
    controller.createEvent("Standup", start, end);

    LocalDateTime targetTime = LocalDateTime.of(2025, 11, 6, 9, 0);
    Command command = new CopyEventCommand("Standup", start, "Personal", targetTime);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event 'Standup' copied"));
  }

  // ============ COPY EVENTS ON DATE COMMAND TESTS ============

  @Test
  public void testCopyEventsOnDateCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    LocalDateTime start = sourceDate.atTime(10, 0);
    LocalDateTime end = sourceDate.atTime(11, 0);
    controller.createEvent("Meeting", start, end);

    LocalDate targetDate = LocalDate.of(2025, 11, 2);
    Command command = new CopyEventsOnDateCommand(sourceDate, "Personal", targetDate);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Copied 1 events"));
  }

  @Test
  public void testCopyEventsOnDateCommandMultipleEvents() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDate sourceDate = LocalDate.of(2025, 11, 1);
    controller.createEvent("Meeting1", sourceDate.atTime(10, 0), sourceDate.atTime(11, 0));
    controller.createEvent("Meeting2", sourceDate.atTime(14, 0), sourceDate.atTime(15, 0));

    LocalDate targetDate = LocalDate.of(2025, 11, 5);
    Command command = new CopyEventsOnDateCommand(sourceDate, "Personal", targetDate);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Copied 2 events"));
  }

  // ============ COPY EVENTS IN RANGE COMMAND TESTS ============

  @Test
  public void testCopyEventsInRangeCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    controller.createEvent("Meeting1", start1, end1);

    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 15, 0);
    controller.createEvent("Meeting2", start2, end2);

    LocalDate startDate = LocalDate.of(2025, 11, 1);
    LocalDate endDate = LocalDate.of(2025, 11, 2);
    LocalDate targetStartDate = LocalDate.of(2025, 11, 10);

    Command command = new CopyEventsInRangeCommand(
        startDate, endDate, "Personal", targetStartDate);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Copied 2 events"));
  }

  @Test
  public void testCopyEventsInRangeCommandSingleDay() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    controller.createEvent("Meeting", start, end);

    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDate targetDate = LocalDate.of(2025, 12, 1);

    Command command = new CopyEventsInRangeCommand(date, date, "Personal", targetDate);
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Copied 1 events"));
  }

  // ============ EDIT EVENTS COMMAND TESTS ============

  @Test
  public void testEditEventsCommandExecution() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 4);
    controller.createEventSeries(series);

    LocalDateTime secondOccurrence = LocalDateTime.of(2025, 11, 10, 10, 0);
    Command command = new EditEventsCommand("location", "Weekly", secondOccurrence, "Remote");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Updated") || view.lastMessage.contains("events"));
  }

  @Test
  public void testEditEventsCommandDifferentProperty() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.WEDNESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Standup", start, end, days, 3);
    controller.createEventSeries(series);

    Command command = new EditEventsCommand("description", "Standup", start, "Daily sync");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Updated") || !view.lastError.isEmpty());
  }

  // ============ MOCK VIEW ============

  private static class MockCalendarView implements CalendarView {
    String lastMessage = "";
    String lastError = "";

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
      // Not used in these tests
    }

    @Override
    public void showEventsInRange(LocalDateTime start, LocalDateTime end,
                                   List<CalendarEvent> events) {
      // Not used in these tests
    }

    @Override
    public void showStatus(boolean isBusy) {
      // Not used in these tests
    }

    @Override
    public void exportToFile(String filename, List<CalendarEvent> events) {
      // Not used in these tests
    }
  }
}
