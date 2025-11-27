import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.EventStatus;
import org.junit.Test;

/**
 * Test class for CalendarEvent.
 */
public class CalendarEventTest {

  @Test
  public void testCreateSimpleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertFalse(event.isAllDay());
    assertEquals(EventStatus.PUBLIC, event.getStatus());
  }

  @Test
  public void testCreateAllDayEvent() {
    LocalDate date = LocalDate.of(2025, 11, 1);

    CalendarEvent event = new CalendarEvent("Holiday", date);

    assertEquals("Holiday", event.getSubject());
    assertTrue(event.isAllDay());
    assertEquals(date, event.getStartDateTime().toLocalDate());
  }

  @Test(expected = NullPointerException.class)
  public void testCreateEventNullSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    new CalendarEvent(null, start, end);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateEventNullStartTime() {
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    new CalendarEvent("Meeting", null, end);
  }

  @Test
  public void testWithSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    CalendarEvent updated = event.withSubject("Important Meeting");

    assertEquals("Important Meeting", updated.getSubject());
    assertEquals("Meeting", event.getSubject()); // Original unchanged
  }

  @Test
  public void testWithDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    CalendarEvent updated = event.withDescription("Team standup");

    assertEquals("Team standup", updated.getDescription());
    assertNull(event.getDescription());
  }

  @Test
  public void testWithLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    CalendarEvent updated = event.withLocation("Room 101");

    assertEquals("Room 101", updated.getLocation());
    assertNull(event.getLocation());
  }

  @Test
  public void testWithStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    CalendarEvent updated = event.withStatus(EventStatus.PRIVATE);

    assertEquals(EventStatus.PRIVATE, updated.getStatus());
    assertEquals(EventStatus.PUBLIC, event.getStatus());
  }

  @Test
  public void testConflictsWith() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end);

    assertTrue(event1.conflictsWith(event2));
  }

  @Test
  public void testNoConflictDifferentSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end);
    CalendarEvent event2 = new CalendarEvent("Lunch", start, end);

    assertFalse(event1.conflictsWith(event2));
  }

  @Test
  public void testNoConflictDifferentTime() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 1, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 15, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start1, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting", start2, end2);

    assertFalse(event1.conflictsWith(event2));
  }

  @Test
  public void testOccursOn() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    assertTrue(event.occursOn(LocalDate.of(2025, 11, 1)));
    assertFalse(event.occursOn(LocalDate.of(2025, 11, 2)));
  }

  @Test
  public void testOverlapsWith() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    // Overlapping range
    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 10, 30);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 11, 30);
    assertTrue(event.overlapsWith(rangeStart, rangeEnd));

    // Non-overlapping range
    LocalDateTime rangeStart2 = LocalDateTime.of(2025, 11, 1, 12, 0);
    LocalDateTime rangeEnd2 = LocalDateTime.of(2025, 11, 1, 13, 0);
    assertFalse(event.overlapsWith(rangeStart2, rangeEnd2));
  }

  @Test
  public void testEquals1() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testToString() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("2025-11-01"));
  }

  @Test
  public void testFindEventByDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);

    CalendarEvent found = series.findEventByDate(LocalDate.of(2025, 11, 3));
    assertNotNull(found);
    assertEquals("Weekly", found.getSubject());

    CalendarEvent notFound = series.findEventByDate(LocalDate.of(2025, 11, 4));
    assertNull(notFound);
  }

  @Test
  public void testGetters() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 5);

    assertEquals("Weekly", series.getSubject());
    assertEquals(start.toLocalTime(), series.getStartTime());
    assertEquals(end.toLocalTime(), series.getEndTime());
    assertEquals(days, series.getRepeatDays());
    assertEquals(start.toLocalDate(), series.getSeriesStartDate());
    assertNull(series.getSeriesEndDate());
    assertEquals(5, series.getMaxOccurrences());
    assertFalse(series.isAllDay());
    assertNull(series.getDescription());
    assertNull(series.getLocation());
    assertEquals(model.EventStatus.PUBLIC, series.getStatus());
  }

  @Test
  public void testSeriesWithEndDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    LocalDate endDate = LocalDate.of(2025, 12, 31);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, endDate);

    assertEquals(endDate, series.getSeriesEndDate());
    assertEquals(-1, series.getMaxOccurrences());
  }

  @Test
  public void testAllDaySeriesWithEndDate() {
    LocalDate startDate = LocalDate.of(2025, 11, 3);
    LocalDate endDate = LocalDate.of(2025, 11, 30);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Work", startDate, days, -1, endDate);

    assertTrue(series.isAllDay());
    assertEquals(endDate, series.getSeriesEndDate());
  }

  @Test
  public void testEquals() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series1 = new CalendarEventSeries("Weekly", start, end, days, 3);
    CalendarEventSeries series2 = new CalendarEventSeries("Weekly", start, end, days, 3);

    assertEquals(series1, series2);
    assertEquals(series1.hashCode(), series2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series1 = new CalendarEventSeries("Weekly", start, end, days, 3);
    CalendarEventSeries series2 = new CalendarEventSeries("Weekly", start, end, days, 5);

    assertFalse(series1.equals(series2));
  }


  @Test(expected = NullPointerException.class)
  public void testCreateEventNullStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    new CalendarEvent("Meeting", start, end, null, null, null, false);
  }

  @Test
  public void testConflictsWithNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, null);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, null);

    assertTrue(event1.conflictsWith(event2));
  }

  @Test
  public void testNoConflictDifferentEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 12, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end2);

    assertFalse(event1.conflictsWith(event2));
  }

  @Test
  public void testConflictsWithOneNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, null);

    assertFalse(event1.conflictsWith(event2));
  }

  @Test
  public void testOccursOnMultiDayEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 18, 0);
    CalendarEvent event = new CalendarEvent("Conference", start, end);

    assertTrue(event.occursOn(LocalDate.of(2025, 11, 1)));
    assertTrue(event.occursOn(LocalDate.of(2025, 11, 2)));
    assertTrue(event.occursOn(LocalDate.of(2025, 11, 3)));
    assertFalse(event.occursOn(LocalDate.of(2025, 10, 31)));
    assertFalse(event.occursOn(LocalDate.of(2025, 11, 4)));
  }

  @Test
  public void testOccursOnEventWithNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, null);

    assertTrue(event.occursOn(LocalDate.of(2025, 11, 1)));
    assertFalse(event.occursOn(LocalDate.of(2025, 11, 2)));
  }

  @Test
  public void testOverlapsWithNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, null);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 9, 30);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 10, 30);
    assertTrue(event.overlapsWith(rangeStart, rangeEnd));

    LocalDateTime rangeStart2 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime rangeEnd2 = LocalDateTime.of(2025, 11, 1, 12, 0);
    assertFalse(event.overlapsWith(rangeStart2, rangeEnd2));
  }

  @Test
  public void testOverlapsWithExactBoundaries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 12, 0);
    assertFalse(event.overlapsWith(rangeStart, rangeEnd));

    LocalDateTime rangeStart2 = LocalDateTime.of(2025, 11, 1, 9, 0);
    LocalDateTime rangeEnd2 = LocalDateTime.of(2025, 11, 1, 10, 0);
    assertFalse(event.overlapsWith(rangeStart2, rangeEnd2));
  }

  @Test
  public void testEqualsSameObject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    assertTrue(event.equals(event));
  }

  @Test
  public void testEqualsNull() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    assertFalse(event.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    assertFalse(event.equals("Not a CalendarEvent"));
  }

  @Test
  public void testEqualsDifferentSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end);
    CalendarEvent event2 = new CalendarEvent("Conference", start, end);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentStartDateTime() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 12, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start1, end);
    CalendarEvent event2 = new CalendarEvent("Meeting", start2, end);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentEndDateTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 12, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end2);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end, "Desc1", null,
        EventStatus.PUBLIC, false);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end, "Desc2", null,
        EventStatus.PUBLIC, false);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end, null, "Room 1",
        EventStatus.PUBLIC, false);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end, null, "Room 2",
        EventStatus.PUBLIC, false);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end, null, null, EventStatus.PUBLIC,
        false);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end, null, null, EventStatus.PRIVATE,
        false);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentAllDay() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end, null, null, EventStatus.PUBLIC,
        false);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end, null, null, EventStatus.PUBLIC,
        true);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testToStringWithNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("2025-11-01"));
    assertTrue(str.contains("10:00"));
  }

  @Test
  public void testFullConstructorWithAllProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        start,
        end,
        "Team standup",
        "Room 101",
        EventStatus.PRIVATE,
        true
    );

    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertEquals("Team standup", event.getDescription());
    assertEquals("Room 101", event.getLocation());
    assertEquals(EventStatus.PRIVATE, event.getStatus());
    assertTrue(event.isAllDay());
  }

  @Test
  public void testEqualsAllPropertiesSame() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent(
        "Meeting", start, end, "Description", "Location", EventStatus.PRIVATE, true
    );
    CalendarEvent event2 = new CalendarEvent(
        "Meeting", start, end, "Description", "Location", EventStatus.PRIVATE, true
    );

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testOverlapsWithEventEndsBeforeRangeStarts() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 12, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 13, 0);
    assertFalse(event.overlapsWith(rangeStart, rangeEnd));
  }

  @Test
  public void testOverlapsWithEventStartsAfterRangeEnds() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 15, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 1, 12, 0);
    assertFalse(event.overlapsWith(rangeStart, rangeEnd));
  }
}
