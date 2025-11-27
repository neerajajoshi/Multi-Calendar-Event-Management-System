import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import org.junit.Test;

/**
 * Test class for CalendarEventSeries.
 */
public class CalendarEventSeriesTest {

  @Test
  public void testCreateSeriesWithOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 4);

    assertEquals("Weekly", series.getSubject());
    assertEquals(4, series.getEvents().size());
    assertFalse(series.isAllDay());
  }

  @Test
  public void testCreateSeriesUntilDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    LocalDate endDate = LocalDate.of(2025, 11, 30);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, endDate);

    assertEquals("Weekly", series.getSubject());
    assertTrue(series.getEvents().size() > 0);
    assertEquals(4, series.getEvents().size());
  }

  @Test
  public void testCreateAllDaySeries() {
    LocalDate startDate = LocalDate.of(2025, 11, 3); // Monday
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    days.add(DayOfWeek.WEDNESDAY);
    days.add(DayOfWeek.FRIDAY);

    CalendarEventSeries series = new CalendarEventSeries("Work", startDate, days, 6, null);

    assertTrue(series.isAllDay());
    assertEquals(6, series.getEvents().size());

    for (CalendarEvent event : series.getEvents()) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testSeriesImmutability() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    List<CalendarEvent> events = series.getEvents();

    try {
      events.add(new CalendarEvent("Test", start, end));
      fail("Should throw UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected: modification should not be allowed since the event list is immutable
    }
  }

  @Test
  public void testFindEventByDate() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);

    CalendarEvent event = series.findEventByDate(LocalDate.of(2025, 11, 10));
    assertNotNull(event);
    assertEquals("Weekly", event.getSubject());

    CalendarEvent notFound = series.findEventByDate(LocalDate.of(2025, 11, 11));
    assertNull(notFound);
  }

  @Test
  public void testGetters() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    LocalDate endDate = LocalDate.of(2025, 12, 31);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Test", start, end, days, endDate);

    assertEquals("Test", series.getSubject());
    assertEquals(start.toLocalTime(), series.getStartTime());
    assertEquals(end.toLocalTime(), series.getEndTime());
    assertEquals(days, series.getRepeatDays());
    assertEquals(start.toLocalDate(), series.getSeriesStartDate());
    assertEquals(endDate, series.getSeriesEndDate());
    assertEquals(-1, series.getMaxOccurrences());
    assertFalse(series.isAllDay());
    assertNull(series.getDescription());
    assertNull(series.getLocation());
    assertNotNull(series.getStatus());
  }

  @Test
  public void testEqualsAndHashCode() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series1 = new CalendarEventSeries("Test", start, end, days, 3);
    CalendarEventSeries series2 = new CalendarEventSeries("Test", start, end, days, 3);
    CalendarEventSeries series3 = new CalendarEventSeries("Different", start, end, days, 3);

    assertTrue(series1.equals(series1));
    assertTrue(series1.equals(series2));
    assertFalse(series1.equals(series3));
    assertFalse(series1.equals(null));
    assertFalse(series1.equals("string"));

    assertEquals(series1.hashCode(), series2.hashCode());
  }

  @Test
  public void testRepeatDaysImmutability() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Test", start, end, days, 3);
    Set<DayOfWeek> returnedDays = series.getRepeatDays();

    try {
      returnedDays.add(DayOfWeek.TUESDAY);
      fail("Should throw UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected exception — modifying the returned set should not be allowed
    }
  }

  @Test
  public void testSeriesWithMatchingDays() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Test", start, end, days, 5);

    assertEquals(5, series.getEvents().size());
  }

  @Test
  public void testAllDaySeriesWithEndDateConstraint() {
    LocalDate startDate = LocalDate.of(2025, 11, 3); // Monday
    LocalDate endDate = LocalDate.of(2025, 11, 15);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Test", startDate, days, 10, endDate);
    assertEquals(2, series.getEvents().size());
  }

  @Test
  public void testMaxOccurrencesGetter() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0); // Monday
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Test", start, end, days, 5);

    assertEquals(5, series.getMaxOccurrences());
  }
}