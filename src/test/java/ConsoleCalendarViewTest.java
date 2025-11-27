import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.CalendarEvent;
import model.EventStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import view.ConsoleCalendarView;

/**
 * Tests for ConsoleCalendarView.
 */
public class ConsoleCalendarViewTest {
  private ConsoleCalendarView view;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    view = new ConsoleCalendarView();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Restore original streams.
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);

    new File("test.csv").delete();
    new File("test_allday.csv").delete();
  }

  @Test
  public void testShowMessage() {
    view.showMessage("Test message");
    assertTrue(outContent.toString().contains("Test message"));
  }

  @Test
  public void testShowError() {
    view.showError("Test error");
    String error = errContent.toString();
    assertTrue("Error output should contain 'Error: Test error', but was: " + error,
        error.contains("Error: Test error"));
  }


  @Test
  public void testShowEventsOnDateEmpty() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    List<CalendarEvent> events = new ArrayList<>();

    view.showEventsOnDate(date, events);

    assertTrue(outContent.toString().contains("No events on 2025-11-01"));
  }

  @Test
  public void testShowEventsOnDateSingleEvent() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsOnDate(date, events);

    String output = outContent.toString();
    assertTrue(output.contains("Events on 2025-11-01"));
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("10:00"));
    assertTrue(output.contains("11:00"));
  }

  @Test
  public void testShowEventsOnDateWithLocation() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null, "Room 101",
        EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsOnDate(date, events);

    assertTrue(outContent.toString().contains("Room 101"));
  }

  @Test
  public void testShowEventsInRangeEmpty() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 17, 0);
    List<CalendarEvent> events = new ArrayList<>();

    view.showEventsInRange(start, end, events);

    assertTrue(outContent.toString().contains("No events in range"));
  }

  @Test
  public void testShowEventsInRangeSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 23, 59);
    LocalDateTime eventStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", eventStart, eventEnd);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsInRange(start, end, events);

    String output = outContent.toString();
    assertTrue(output.contains("Events from"));
    assertTrue(output.contains("Meeting"));
  }

  @Test
  public void testShowStatusBusy() {
    view.showStatus(true);

    assertTrue(outContent.toString().contains("busy"));
  }

  @Test
  public void testShowStatusAvailable() {
    view.showStatus(false);

    assertTrue(outContent.toString().contains("available"));
  }

  @Test
  public void testExportToFileSingleEvent() throws Exception {
    String filename = "test.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting"));
    assertTrue(content.contains("11/01/2025"));
    assertTrue(content.contains("False"));
  }

  @Test
  public void testExportToFileAllDayEvent() throws Exception {
    String filename = "test_allday.csv";
    LocalDate date = LocalDate.of(2025, 11, 1);
    CalendarEvent event = new CalendarEvent("Holiday", date);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Holiday"));
    assertTrue(content.contains("True")); // All Day Event
  }

  @Test
  public void testExportToFileIoError() {
    String filename = "/invalid/path/test.csv";
    List<CalendarEvent> events = new ArrayList<>();

    view.exportToFile(filename, events);

    String error = errContent.toString();
    assertTrue(error.contains("Error exporting calendar"));
  }


  @Test
  public void testShowEventsOnDateMultipleEvents() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start1 = date.atTime(10, 0);
    LocalDateTime end1 = date.atTime(11, 0);
    LocalDateTime start2 = date.atTime(14, 0);
    LocalDateTime end2 = date.atTime(15, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting 1", start1, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting 2", start2, end2);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    view.showEventsOnDate(date, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting 1"));
    assertTrue(output.contains("Meeting 2"));
  }

  @Test
  public void testShowEventsOnDateWithoutLocation() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsOnDate(date, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertFalse(output.contains(" at ")); // No location shown
  }

  @Test
  public void testShowEventsOnDateWithEmptyLocation() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null, "",
        EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsOnDate(date, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertFalse(output.contains(" at ")); 
  }

  @Test
  public void testShowEventsOnDateWithNullEndTime() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    // Create event with null end time
    CalendarEvent event = new CalendarEvent("All Day", start, null, null, null,
        EventStatus.PUBLIC, true);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsOnDate(date, events);

    String output = outContent.toString();
    assertTrue(output.contains("All Day"));
    assertTrue(output.contains("10:00")); // Shows start time
  }

  @Test
  public void testShowEventsInRangeWithLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 23, 59);
    LocalDateTime eventStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", eventStart, eventEnd, null, "Room 101",
        EventStatus.PUBLIC, false);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsInRange(start, end, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Room 101"));
  }

  @Test
  public void testShowEventsInRangeWithoutLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 23, 59);
    LocalDateTime eventStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", eventStart, eventEnd);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsInRange(start, end, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Events from"));
  }

  @Test
  public void testShowEventsInRangeWithEmptyLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 23, 59);
    LocalDateTime eventStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", eventStart, eventEnd, null, "",
        EventStatus.PUBLIC, false);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.showEventsInRange(start, end, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Events from"));
  }

  @Test
  public void testExportToFileWithDescription() throws Exception {
    String filename = "test_description.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, "Team standup",
        "Room 101", EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting"));
    assertTrue(content.contains("Team standup"));
    assertTrue(content.contains("Room 101"));

    file.delete();
  }

  @Test
  public void testExportToFileWithNullDescription() throws Exception {
    String filename = "test_null_desc.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null, "Room 101",
        EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting"));
    assertTrue(content.contains("\"\",\"Room 101\"")); // Empty description field

    file.delete();
  }

  @Test
  public void testExportToFileWithNullLocation() throws Exception {
    String filename = "test_null_loc.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting"));
    assertTrue(content.contains("\"\""));

    file.delete();
  }

  @Test
  public void testExportToFileAllDayWithNullEndTime() throws Exception {
    String filename = "test_allday_null_end.csv";
    LocalDate date = LocalDate.of(2025, 11, 1);
    CalendarEvent event = new CalendarEvent("Holiday", date);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Holiday"));
    assertTrue(content.contains("True")); // All day
    assertTrue(content.contains("11/01/2025")); // Date appears twice (start and end)

    file.delete();
  }

  @Test
  public void testShowEventsInRangeMultipleEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 23, 59);
    LocalDateTime event1Start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime event1End = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime event2Start = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime event2End = LocalDateTime.of(2025, 11, 2, 15, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting 1", event1Start, event1End);
    CalendarEvent event2 = new CalendarEvent("Meeting 2", event2Start, event2End);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    view.showEventsInRange(start, end, events);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting 1"));
    assertTrue(output.contains("Meeting 2"));
  }

  @Test
  public void testExportToFilePrivateStatus() throws Exception {
    String filename = "test_private.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end, null, null,
        EventStatus.PRIVATE, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("private")); // Status value

    file.delete();
  }

  @Test
  public void testExportToFileMultipleEvents() throws Exception {
    String filename = "test_multiple.csv";
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 15, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting 1", start1, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting 2", start2, end2);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event1);
    events.add(event2);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting 1"));
    assertTrue(content.contains("Meeting 2"));

    file.delete();
  }

  @Test
  public void testExportToFileEmptyList() throws Exception {
    String filename = "test_empty.csv";
    List<CalendarEvent> events = new ArrayList<>();

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));

    assertTrue(content.contains("Subject,Start Date"));

    file.delete();
  }

  @Test
  public void testExportToFileSuccessMessage() {
    String filename = "test_success.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar exported to:"));
    assertTrue(output.contains(filename));

    new File(filename).delete();
  }

  @Test
  public void testExportToFileAllDayEventTimeFieldsEmpty() throws Exception {
    String filename = "test_allday_times.csv";
    LocalDate date = LocalDate.of(2025, 11, 1);
    CalendarEvent event = new CalendarEvent("Holiday", date);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    assertTrue(file.exists());

    String content = new String(Files.readAllBytes(file.toPath()));

    String[] lines = content.split("\n");
    assertTrue(lines.length >= 2);
    String dataLine = lines[1];

    assertTrue(dataLine.matches(".*11/01/2025,,.*"));
    assertTrue(dataLine.contains(",,True,"));

    file.delete();
  }

  /**
   * All-day events should have empty start time in CSV export.
   */
  @Test
  public void testExportAllDayEventHasEmptyStartTime() throws Exception {
    String filename = "test_allday_start_time.csv";
    LocalDate date = LocalDate.of(2025, 11, 1);
    CalendarEvent event = new CalendarEvent("Holiday", date);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    String content = new String(Files.readAllBytes(file.toPath()));
    String[] lines = content.split("\n");
    String dataLine = lines[1];
    assertTrue("All-day event should have empty start time field",
        dataLine.contains("11/01/2025,,"));

    file.delete();
  }

  /**
   * Events with null end date use start date as end date in CSV.
   */
  @Test
  public void testExportEventWithNullEndDateUsesStartDate() throws Exception {
    String filename = "test_null_end_date.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    // Create event with null end time but not all-day
    CalendarEvent event = new CalendarEvent("Meeting", start, null, null, null,
        EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    String content = new String(Files.readAllBytes(file.toPath()));

    // Both start date and end date should be 11/01/2025
    String[] lines = content.split("\n");
    String dataLine = lines[1];

    // Count occurrences of the date
    int count = 0;
    int index = 0;
    while ((index = dataLine.indexOf("11/01/2025", index)) != -1) {
      count++;
      index++;
    }
    assertTrue("Date should appear twice (start and end)", count >= 2);

    file.delete();
  }

  /*@Test
  public void testExportNonAllDayEventWithNullEndTimeUsesStartTime() throws Exception {
    String filename = "test_null_end_time.csv";
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 14, 30);
    CalendarEvent event = new CalendarEvent("Meeting", start, null, null, null,
        EventStatus.PUBLIC, false);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.exportToFile(filename, events);

    File file = new File(filename);
    String content = new String(Files.readAllBytes(file.toPath()));

    String[] lines = content.split("\n");
    String dataLine = lines[1];

    assertTrue("Data line should contain time in both positions: " + dataLine,
        dataLine.contains("02:30 PM,11/01/2025,02:30 PM"));
    assertFalse("End time should not be empty for non-all-day event with null end",
        dataLine.contains("11/01/2025,,False"));

    file.delete();
  }*/

}