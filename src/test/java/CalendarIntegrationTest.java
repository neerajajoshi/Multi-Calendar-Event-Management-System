import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import controller.CalendarController;
import controller.Command;
import controller.CommandParser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.CalendarModel;
import model.InMemoryCalendarModel;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;

/**
 * Integration tests for the calendar system.
 */
public class CalendarIntegrationTest {
  private CalendarModel model;
  private MockView view;
  private CalendarController controller;
  private CommandParser parser;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    model = new InMemoryCalendarModel("Integration Test Calendar");
    view = new MockView();
    controller = new CalendarController(model, view);
    parser = new CommandParser();
  }

  // ============ SINGLE EVENT INTEGRATION TESTS ============
  /*
  @Test
  public void testCreateAndRetrieveSingleEvent() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event created: Meeting"));
    assertEquals(1, model.getAllEvents().size());
  }
  */

  @Test
  public void testCreateAndPrintEventsOnDate() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand("print events on 2025-11-01").execute(controller);

    assertTrue(view.eventsOnDateCalled);
    assertEquals(1, view.lastEvents.size());
  }

  @Test
  public void testCreateAndCheckStatus() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand("show status on 2025-11-01T10:30").execute(controller);

    assertTrue(view.statusCalled);
    assertTrue(view.isBusy);
  }

  /*
  @Test
  public void testCreateAndEditSingleEvent() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand(
        "edit event subject Meeting from 2025-11-01T10:00 with \"Important Meeting\"")
        .execute(controller);

    assertEquals(1, model.getAllEvents().size());
    assertEquals("Important Meeting", model.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testCreateConflictingEvent() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);

    assertTrue(view.lastError.contains("Error creating event"));
    assertEquals(1, model.getAllEvents().size());
  }

  // ============ ALL-DAY EVENT INTEGRATION TESTS ============

  @Test
  public void testCreateAllDayEvent() {
    Command command = parser.parseCommand("create event Holiday on 2025-11-01");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("All-day event created: Holiday"));
    assertEquals(1, model.getAllEvents().size());
    assertTrue(model.getAllEvents().get(0).isAllDay());
  }
  */

  @Test
  public void testCreateAndPrintAllDayEvent() {
    parser.parseCommand("create event Holiday on 2025-11-01").execute(controller);
    parser.parseCommand("print events on 2025-11-01").execute(controller);

    assertEquals(1, view.lastEvents.size());
    assertTrue(view.lastEvents.get(0).isAllDay());
  }

  // ============ EVENT SERIES INTEGRATION TESTS ============
  /*
  @Test
  public void testCreateEventSeriesWithOccurrences() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
          + "repeats MW for 4 times");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
    assertTrue(view.lastMessage.contains("4 events"));
    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateEventSeriesUntilDate() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-02T10:00 to 2025-11-02T11:00 "
        + "repeats M until 2025-11-30");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
    // November 2025: Mondays are 3, 10, 17, 24 (4 occurrences)
    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDaySeriesWithOccurrences() {
    Command command = parser.parseCommand(
        "create event Workday on 2025-11-03 repeats MTWRF for 10 times");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
    assertEquals(10, model.getAllEvents().size());

    for (var event : model.getAllEvents()) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testCreateAllDaySeriesUntilDate() {
    Command command = parser.parseCommand(
        "create event Workday on 2025-11-03 repeats MWF until 2025-11-14");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Event series created"));
    // Nov 3-14: M(3), W(5), F(7), M(10), W(12), F(14) = 6 events
    assertEquals(6, model.getAllEvents().size());
  }
  */

  // ============ EDIT SERIES INTEGRATION TESTS ============

  @Test
  public void testEditAllEventsInSeries() {
    parser.parseCommand(
      "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats M for 3 times").execute(controller);

    parser.parseCommand(
        "edit series description Meeting from 2025-11-03T10:00 with \"Team sync\"")
        .execute(controller);

    assertTrue(view.lastMessage.contains("Updated all"));
    assertTrue(view.lastMessage.contains("3 events"));
  }

  // ============ PRINT EVENTS IN RANGE INTEGRATION TESTS ============

  @Test
  public void testPrintEventsInRange() {
    parser.parseCommand(
        "create event Meeting1 from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand(
        "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:00")
        .execute(controller);

    parser.parseCommand(
        "print events from 2025-11-01T00:00 to 2025-11-30T23:59")
        .execute(controller);

    assertTrue(view.eventsInRangeCalled);
    assertEquals(2, view.lastEvents.size());
  }

  @Test
  public void testPrintEventsInRangeWithSeries() {
    parser.parseCommand(
      "create event Meeting from 2025-11-02T10:00 to 2025-11-02T11:00 "
        + "repeats M for 4 times").execute(controller);

    parser.parseCommand(
        "print events from 2025-11-01T00:00 to 2025-11-30T23:59")
        .execute(controller);

    assertEquals(4, view.lastEvents.size());
  }

  // ============ EXPORT INTEGRATION TESTS ============

  @Test
  public void testExportCalendar() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand("export cal test_export.csv").execute(controller);

    assertTrue(view.exportCalled);
    assertEquals("test_export.csv", view.lastFilename);
    assertEquals(1, view.lastEvents.size());
  }

  @Test
  public void testExportEmptyCalendar() {
    parser.parseCommand("export cal empty.csv").execute(controller);

    assertTrue(view.exportCalled);
    assertEquals(0, view.lastEvents.size());
  }

  @Test
  public void testExportCalendarWithSeries() {
    parser.parseCommand(
      "create event Meeting from 2025-11-02T10:00 to 2025-11-02T11:00 "
        + "repeats M for 3 times").execute(controller);
    parser.parseCommand("export cal series.csv").execute(controller);

    assertEquals(3, view.lastEvents.size());
  }

  // ============ COMPLEX SCENARIO TESTS ============

  /*
  @Test
  public void testComplexScenarioWithEdits() {
    // Create a series
    parser.parseCommand(
      "create event Meeting from 2025-11-02T10:00 to 2025-11-02T11:00 "
        + "repeats M for 4 times").execute(controller);

    assertEquals(4, model.getAllEvents().size());

    // Edit one occurrence
    parser.parseCommand(
        "edit event location Meeting from 2025-11-02T10:00 with Office")
        .execute(controller);

    // Edit from date
    parser.parseCommand(
        "edit events description Meeting from 2025-11-16T10:00 with Updated")
        .execute(controller);

    // All events should still exist (some modified)
    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testComplexScenarioQuotedSubjects() {
    parser.parseCommand(
      "create event \"Weekly Team Standup\" from 2025-11-03T09:00 to 2025-11-03T09:15 "
        + "repeats MW for 6 times").execute(controller);

    assertEquals(6, model.getAllEvents().size());

    parser.parseCommand(
      "edit event subject \"Weekly Team Standup\" from 2025-11-03T09:00 "
        + "with \"Daily Standup\"").execute(controller);

    assertTrue(view.lastMessage.contains("updated"));
  }
  */

  @Test
  public void testStatusAcrossMultipleEvents() {
    parser.parseCommand(
        "create event Meeting1 from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);
    parser.parseCommand(
        "create event Meeting2 from 2025-11-01T14:00 to 2025-11-01T15:00")
        .execute(controller);

    // Busy during first meeting
    parser.parseCommand("show status on 2025-11-01T10:30").execute(controller);
    assertTrue(view.isBusy);

    // Available between meetings
    parser.parseCommand("show status on 2025-11-01T12:00").execute(controller);
    assertFalse(view.isBusy);

    // Busy during second meeting
    parser.parseCommand("show status on 2025-11-01T14:30").execute(controller);
    assertTrue(view.isBusy);
  }

  @Test
  public void testPrintRangeWithMixedEventTypes() {
    // Single event
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00")
        .execute(controller);

    // All-day event
    parser.parseCommand("create event Holiday on 2025-11-11").execute(controller);

    // Series
    parser.parseCommand(
      "create event Standup from 2025-11-02T09:00 to 2025-11-02T09:15 "
        + "repeats M for 3 times").execute(controller);

    parser.parseCommand(
        "print events from 2025-11-01T00:00 to 2025-11-30T23:59")
        .execute(controller);

    assertEquals(5, view.lastEvents.size());
  }

  // ============ ERROR HANDLING INTEGRATION TESTS ============

  @Test
  public void testInvalidCommandHandling() {
    try {
      parser.parseCommand("invalid command here");
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid command"));
    }
  }

  @Test
  public void testEditNonExistentEvent() {
    parser.parseCommand(
        "edit event subject NonExistent from 2025-11-01T10:00 with New")
        .execute(controller);

    assertTrue(view.lastError.contains("No event found"));
  }

  @Test
  public void testInvalidDateFormat() {
    try {
      parser.parseCommand("create event Meeting from 11/01/2025T10:00 to 11/01/2025T11:00");
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid date/time format")
          || e.getMessage().contains("Invalid from-to format"));
    }
  }

  @Test
  public void testPrintEventsOnEmptyDate() {
    parser.parseCommand("print events on 2025-11-01").execute(controller);

    assertTrue(view.eventsOnDateCalled);
    assertEquals(0, view.lastEvents.size());
  }

  // ============ EXIT COMMAND INTEGRATION TEST ============

  @Test
  public void testExitCommand() {
    Command command = parser.parseCommand("exit");
    command.execute(controller);

    assertTrue(view.lastMessage.contains("Goodbye"));
  }

  private static class MockView implements CalendarView {
    String lastMessage = "";
    String lastError = "";
    boolean eventsOnDateCalled = false;
    boolean eventsInRangeCalled = false;
    boolean statusCalled = false;
    boolean exportCalled = false;
    List<model.CalendarEvent> lastEvents = new ArrayList<>();
    boolean isBusy = false;
    String lastFilename = "";

    @Override
    public void showMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void showError(String error) {
      this.lastError = error;
    }

    @Override
    public void showEventsOnDate(LocalDate date, List<model.CalendarEvent> events) {
      this.eventsOnDateCalled = true;
      this.lastEvents = new ArrayList<>(events);
    }

    @Override
    public void showEventsInRange(LocalDateTime start, LocalDateTime end,
                                  List<model.CalendarEvent> events) {
      this.eventsInRangeCalled = true;
      this.lastEvents = new ArrayList<>(events);
    }

    @Override
    public void showStatus(boolean isBusy) {
      this.statusCalled = true;
      this.isBusy = isBusy;
    }

    @Override
    public void exportToFile(String filename, List<model.CalendarEvent> events) {
      this.exportCalled = true;
      this.lastFilename = filename;
      this.lastEvents = new ArrayList<>(events);
    }
  }
}