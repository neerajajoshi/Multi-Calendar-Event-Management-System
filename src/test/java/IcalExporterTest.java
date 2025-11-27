import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.CalendarEvent;
import model.EventStatus;
import org.junit.After;
import org.junit.Test;
import view.IcalExporter;

/**
 * Tests for IcalExporter.
 */
public class IcalExporterTest {
  private final List<String> testFiles = new ArrayList<>();

  /**
   * Clean up test files.
   */
  @After
  public void cleanup() {
    for (String filename : testFiles) {
      File file = new File(filename);
      if (file.exists()) {
        file.delete();
      }
    }
    testFiles.clear();
  }

  // ========== Constructor Tests ==========

  @Test
  public void testConstructorThrowsAssertionError() {
    try {
      java.lang.reflect.Constructor<IcalExporter> constructor = 
          IcalExporter.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      assertTrue("Expected AssertionError to be thrown", false);
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof AssertionError);
    }
  }


  // ========== Export to File Tests ==========

  @Test
  public void testExportToIcalFileSuccess() throws IOException {
    String filename = "test_export.ics";
    testFiles.add(filename);
    
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Team meeting",
        "Conference Room",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    IcalExporter.exportToIcalFile(events, filename, "Work", "America/New_York");
    
    File file = new File(filename);
    assertTrue(file.exists());
    
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("END:VCALENDAR"));
    assertTrue(content.contains("Meeting"));
  }

  @Test
  public void testExportToIcalFileEmptyEvents() throws IOException {
    String filename = "test_empty.ics";
    testFiles.add(filename);
    
    List<CalendarEvent> events = new ArrayList<>();
    
    IcalExporter.exportToIcalFile(events, filename, "Work", "America/New_York");
    
    File file = new File(filename);
    assertTrue(file.exists());
    
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("END:VCALENDAR"));
    assertFalse(content.contains("BEGIN:VEVENT"));
  }


  // ========== Generate iCal Content Tests ==========

  @Test
  public void testGenerateIcalContentBasicEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Team sync",
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("VERSION:2.0"));
    assertTrue(content.contains("PRODID:-//Calendar Application//Calendar 1.0//EN"));
    assertTrue(content.contains("X-WR-CALNAME:Work"));
    assertTrue(content.contains("X-WR-TIMEZONE:America/New_York"));
    assertTrue(content.contains("BEGIN:VTIMEZONE"));
    assertTrue(content.contains("TZID:America/New_York"));
    assertTrue(content.contains("END:VTIMEZONE"));
    assertTrue(content.contains("BEGIN:VEVENT"));
    assertTrue(content.contains("SUMMARY:Meeting"));
    assertTrue(content.contains("LOCATION:Office"));
    assertTrue(content.contains("DESCRIPTION:Team sync"));
    assertTrue(content.contains("STATUS:CONFIRMED"));
    assertTrue(content.contains("END:VEVENT"));
    assertTrue(content.contains("END:VCALENDAR"));
  }

  @Test
  public void testGenerateIcalContentAllDayEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Holiday",
        LocalDateTime.of(2024, 1, 15, 0, 0),
        LocalDateTime.of(2024, 1, 16, 0, 0),
        null,
        null,
        EventStatus.PUBLIC,
        true
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Personal", "Europe/London");
    
    assertTrue(content.contains("DTSTART;VALUE=DATE:20240115"));
    assertTrue(content.contains("DTEND;VALUE=DATE:20240116"));
    assertFalse(content.contains("DTSTART;TZID="));
  }


  @Test
  public void testGenerateIcalContentTimedEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 30),
        LocalDateTime.of(2024, 1, 15, 11, 30),
        "Discussion",
        "Room A",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("DTSTART;TZID=America/New_York:20240115T103000"));
    assertTrue(content.contains("DTEND;TZID=America/New_York:20240115T113000"));
  }

  @Test
  public void testGenerateIcalContentEventWithNullEndDateTime() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Task",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        null,
        null,
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("DTSTART;TZID=America/New_York:20240115T100000"));
    assertFalse(content.contains("DTEND"));
  }

  @Test
  public void testGenerateIcalContentEventWithNullDescription() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        null,
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertFalse(content.contains("DESCRIPTION:"));
  }


  @Test
  public void testGenerateIcalContentEventWithEmptyDescription() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "   ",
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertFalse(content.contains("DESCRIPTION:"));
  }

  @Test
  public void testGenerateIcalContentEventWithNullLocation() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Discussion",
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertFalse(content.contains("LOCATION:"));
  }

  @Test
  public void testGenerateIcalContentEventWithEmptyLocation() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Discussion",
        "   ",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertFalse(content.contains("LOCATION:"));
  }

  @Test
  public void testGenerateIcalContentEventWithNullStatusThrows() {
    assertThrows(NullPointerException.class, () -> {
      new CalendarEvent(
          "Meeting",
          LocalDateTime.of(2024, 1, 15, 10, 0),
          LocalDateTime.of(2024, 1, 15, 11, 0),
          "Discussion",
          "Office",
          null,
          false
      );
    });
  }


  @Test
  public void testGenerateIcalContentEventWithPrivateStatus() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Discussion",
        "Office",
        EventStatus.PRIVATE,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("STATUS:CONFIRMED"));
  }

  @Test
  public void testGenerateIcalContentMultipleEvents() {
    List<CalendarEvent> events = new ArrayList<>();
    
    CalendarEvent event1 = new CalendarEvent(
        "Meeting 1",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "First meeting",
        "Room A",
        EventStatus.PUBLIC,
        false
    );
    events.add(event1);
    
    CalendarEvent event2 = new CalendarEvent(
        "Meeting 2",
        LocalDateTime.of(2024, 1, 16, 14, 0),
        LocalDateTime.of(2024, 1, 16, 15, 0),
        "Second meeting",
        "Room B",
        EventStatus.PRIVATE,
        false
    );
    events.add(event2);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting 1"));
    assertTrue(content.contains("Meeting 2"));
    assertTrue(content.contains("Room A"));
    assertTrue(content.contains("Room B"));
  }

  // ========== Text Escaping Tests ==========

  @Test
  public void testEscapeTextWithComma() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting, Discussion",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Notes, comments",
        "Room A, Building B",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\, Discussion"));
    assertTrue(content.contains("Room A\\, Building B"));
    assertTrue(content.contains("Notes\\, comments"));
  }


  @Test
  public void testEscapeTextWithSemicolon() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting; Discussion",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Notes; comments",
        "Room A; Building B",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\; Discussion"));
    assertTrue(content.contains("Room A\\; Building B"));
    assertTrue(content.contains("Notes\\; comments"));
  }

  @Test
  public void testEscapeTextWithBackslash() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting\\Discussion",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Path\\to\\file",
        "C:\\Room\\A",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\\\Discussion"));
    assertTrue(content.contains("C:\\\\Room\\\\A"));
    assertTrue(content.contains("Path\\\\to\\\\file"));
  }

  @Test
  public void testEscapeTextWithNewline() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting\nDiscussion",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Line 1\nLine 2",
        "Room A\nBuilding B",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\nDiscussion"));
    assertTrue(content.contains("Room A\\nBuilding B"));
    assertTrue(content.contains("Line 1\\nLine 2"));
  }

  @Test
  public void testEscapeTextWithCarriageReturn() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting\r\nDiscussion",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Line 1\r\nLine 2",
        "Room A\r\nBuilding B",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\nDiscussion"));
    assertTrue(content.contains("Room A\\nBuilding B"));
    assertTrue(content.contains("Line 1\\nLine 2"));
    assertFalse(content.contains("\r\n\r\n"));
  }


  @Test
  public void testEscapeTextWithMultipleSpecialChars() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting, Discussion; Notes\\Path\nNew Line",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        null,
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("Meeting\\, Discussion\\; Notes\\\\Path\\nNew Line"));
  }

  // ========== UID Generation Tests ==========

  @Test
  public void testUidGenerationConsistency() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Discussion",
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content1 = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    String content2 = IcalExporter.generateIcalContent(events, "Work", "America/New_York");

    String uid1 = content1.substring(content1.indexOf("UID:") + 4, 
        content1.indexOf("@calendar-app.local") + 19);
    String uid2 = content2.substring(content2.indexOf("UID:") + 4, 
        content2.indexOf("@calendar-app.local") + 19);
    
    assertEquals(uid1, uid2);
  }

  @Test
  public void testUidGenerationDifferentEvents() {
    List<CalendarEvent> events1 = new ArrayList<>();
    CalendarEvent event1 = new CalendarEvent(
        "Meeting 1",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        "Discussion",
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events1.add(event1);
    
    List<CalendarEvent> events2 = new ArrayList<>();
    CalendarEvent event2 = new CalendarEvent(
        "Meeting 2",
        LocalDateTime.of(2024, 1, 16, 10, 0),
        LocalDateTime.of(2024, 1, 16, 11, 0),
        "Discussion",
        "Office",
        EventStatus.PUBLIC,
        false
    );
    events2.add(event2);
    
    String content1 = IcalExporter.generateIcalContent(events1, "Work", "America/New_York");
    String content2 = IcalExporter.generateIcalContent(events2, "Work", "America/New_York");
    
    assertTrue(content1.contains("UID:"));
    assertTrue(content2.contains("UID:"));
    assertTrue(content1.contains("@calendar-app.local"));
    assertTrue(content2.contains("@calendar-app.local"));
  }


  // ========== Calendar Name Escaping Tests ==========

  @Test
  public void testCalendarNameWithSpecialChars() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        null,
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, 
        "Work, Personal; Calendar\\Test\nName", "America/New_York");
    
    assertTrue(content.contains("X-WR-CALNAME:Work\\, Personal\\; Calendar\\\\Test\\nName"));
  }

  // ========== Timestamp Tests ==========

  @Test
  public void testCreatedAndLastModifiedTimestamps() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        null,
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("CREATED:"));
    assertTrue(content.contains("LAST-MODIFIED:"));
    assertTrue(content.contains("Z"));
  }

  // ========== Edge Case Tests ==========

  @Test
  public void testEmptyEventsList() {
    List<CalendarEvent> events = new ArrayList<>();
    
    String content = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    
    assertTrue(content.contains("BEGIN:VCALENDAR"));
    assertTrue(content.contains("END:VCALENDAR"));
    assertFalse(content.contains("BEGIN:VEVENT"));
  }

  @Test
  public void testAllDayEventWithNullEndDateTime() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Holiday",
        LocalDateTime.of(2024, 1, 15, 0, 0),
        null,
        null,
        null,
        EventStatus.PUBLIC,
        true
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Personal", "Europe/London");
    
    assertTrue(content.contains("DTSTART;VALUE=DATE:20240115"));
    assertFalse(content.contains("DTEND"));
  }


  @Test
  public void testDifferentTimezones() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 0),
        LocalDateTime.of(2024, 1, 15, 11, 0),
        null,
        null,
        EventStatus.PUBLIC,
        false
    );
    events.add(event);
    
    String contentNy = IcalExporter.generateIcalContent(events, "Work", "America/New_York");
    String contentLondon = IcalExporter.generateIcalContent(events, "Work", "Europe/London");
    String contentTokyo = IcalExporter.generateIcalContent(events, "Work", "Asia/Tokyo");
    
    assertTrue(contentNy.contains("TZID:America/New_York"));
    assertTrue(contentLondon.contains("TZID:Europe/London"));
    assertTrue(contentTokyo.contains("TZID:Asia/Tokyo"));
  }

  @Test
  public void testEventWithAllFieldsPopulated() {
    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Important Meeting",
        LocalDateTime.of(2024, 1, 15, 10, 30, 45),
        LocalDateTime.of(2024, 1, 15, 12, 15, 30),
        "Quarterly review meeting with stakeholders",
        "Conference Room A, 5th Floor",
        EventStatus.PRIVATE,
        false
    );
    events.add(event);
    
    String content = IcalExporter.generateIcalContent(events, "Corporate", "America/Chicago");
    
    assertTrue(content.contains("SUMMARY:Important Meeting"));
    assertTrue(content.contains("DTSTART;TZID=America/Chicago:20240115T103045"));
    assertTrue(content.contains("DTEND;TZID=America/Chicago:20240115T121530"));
    assertTrue(content.contains("LOCATION:Conference Room A\\, 5th Floor"));
    assertTrue(content.contains("DESCRIPTION:Quarterly review meeting with stakeholders"));
    assertTrue(content.contains("STATUS:CONFIRMED"));
  }
}

