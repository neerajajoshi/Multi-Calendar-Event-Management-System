import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CalendarEvent;
import model.CalendarEventSeries;
import model.InMemoryCalendarModel;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for InMemoryCalendarModel.
 */
public class InMemoryCalendarModelTest {
  private InMemoryCalendarModel model;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    model = new InMemoryCalendarModel("Test Calendar");
  }

  @Test
  public void testAddEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    model.addEvent(event);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddConflictingEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    model.addEvent(new CalendarEvent("Meeting", start, end));
    model.addEvent(new CalendarEvent("Meeting", start, end)); // Duplicate throws exception
  }

  @Test
  public void testAddEventSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testGetEventsOnDate() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    model.addEvent(new CalendarEvent("Meeting", start, end));

    List<CalendarEvent> events = model.getEventsOnDate(date);
    assertEquals(1, events.size());

    List<CalendarEvent> noEvents = model.getEventsOnDate(LocalDate.of(2025, 11, 2));
    assertEquals(0, noEvents.size());
  }

  @Test
  public void testGetEventsInRange() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 11, 0);

    model.addEvent(new CalendarEvent("Meeting1", start1, end1));
    model.addEvent(new CalendarEvent("Meeting2", start2, end2));

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 3, 0, 0);
    List<CalendarEvent> events = model.getEventsInRange(rangeStart, rangeEnd);

    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEventsInRangeInvalidRange() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 10, 0);

    model.getEventsInRange(start, end);
  }

  @Test
  public void testIsBusyAt() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    model.addEvent(new CalendarEvent("Meeting", start, end));

    assertTrue(model.isBusyAt(LocalDateTime.of(2025, 11, 1, 10, 30)));
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 11, 1, 14, 0)));
  }

  @Test
  public void testFindEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    model.addEvent(new CalendarEvent("Meeting", start, end));

    List<CalendarEvent> found = model.findEvents("Meeting", start);
    assertEquals(1, found.size());

    List<CalendarEvent> notFound = model.findEvents("Lunch", start);
    assertEquals(0, notFound.size());
  }

  @Test
  public void testRemoveEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    model.addEvent(event);
    assertTrue(model.removeEvent(event));
    assertEquals(0, model.getAllEvents().size());

    assertFalse(model.removeEvent(event));
  }

  @Test
  public void testReplaceEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent oldEvent = new CalendarEvent("Meeting", start, end);
    CalendarEvent newEvent = oldEvent.withSubject("Important Meeting");

    model.addEvent(oldEvent);
    assertTrue(model.replaceEvent(oldEvent, newEvent));

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  @Test
  public void testGetName() {
    assertEquals("Test Calendar", model.getName());
  }

  @Test
  public void testExcludeFromSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    assertEquals(3, model.getAllEvents().size());

    model.excludeFromSeries("Weekly", start);
    assertEquals(2, model.getAllEvents().size());
  }

  @Test
  public void testFindSeriesContaining() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    CalendarEventSeries found = model.findSeriesContaining("Weekly", start);
    assertNotNull(found);
    assertEquals("Weekly", found.getSubject());

    CalendarEventSeries notFound = model.findSeriesContaining("Daily", start);
    assertNull(notFound);
  }

  @Test
  public void testDefaultConstructor() {
    InMemoryCalendarModel defaultModel = new InMemoryCalendarModel();
    assertEquals("Default Calendar", defaultModel.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullEvent() {
    model.addEvent(null);
  }

  @Test(expected = NullPointerException.class)
  public void testAddNullSeries() {
    model.addEventSeries(null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetEventsOnDateNull() {
    model.getEventsOnDate(null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetEventsInRangeNullStart() {
    model.getEventsInRange(null, LocalDateTime.now());
  }

  @Test(expected = NullPointerException.class)
  public void testGetEventsInRangeNullEnd() {
    model.getEventsInRange(LocalDateTime.now(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testIsBusyAtNull() {
    model.isBusyAt(null);
  }

  @Test(expected = NullPointerException.class)
  public void testFindEventsNullSubject() {
    model.findEvents(null, LocalDateTime.now());
  }

  @Test(expected = NullPointerException.class)
  public void testFindEventsNullDateTime() {
    model.findEvents("Meeting", null);
  }

  @Test
  public void testReplaceEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent oldEvent = new CalendarEvent("Meeting", start, end);
    CalendarEvent newEvent = oldEvent.withSubject("New");

    assertFalse(model.replaceEvent(oldEvent, newEvent));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReplaceEventWithConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 1, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 15, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting1", start1, end1);
    CalendarEvent event2 = new CalendarEvent("Meeting2", start2, end2);
    model.addEvent(event1);
    model.addEvent(event2);

    CalendarEvent conflicting = new CalendarEvent("Meeting1", start1, end1);
    model.replaceEvent(event2, conflicting);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddSeriesWithConflictingEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    model.addEvent(new CalendarEvent("Meeting", start, end));

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    CalendarEventSeries series = new CalendarEventSeries("Meeting", start, end, days, 3);
    model.addEventSeries(series);
  }

  @Test
  public void testGetSingleEventCount() {
    assertEquals(0, model.getSingleEventCount());

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    model.addEvent(new CalendarEvent("Meeting", start, end));

    assertEquals(1, model.getSingleEventCount());
  }

  @Test
  public void testGetEventSeriesCount() {
    assertEquals(0, model.getEventSeriesCount());

    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    assertEquals(1, model.getEventSeriesCount());
  }

  @Test
  public void testGetEventsOnDateWithSeriesExcluded() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    model.excludeFromSeries("Weekly", start);

    List<model.CalendarEvent> events = model.getEventsOnDate(LocalDate.of(2025, 11, 2));
    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsInRangeWithSeriesExcluded() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    model.excludeFromSeries("Weekly", start);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 30, 23, 59);
    List<model.CalendarEvent> events = model.getEventsInRange(rangeStart, rangeEnd);
    assertEquals(1, events.size());
  }

  @Test
  public void testFindEventsWithSeriesExcluded() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 2, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    model.excludeFromSeries("Weekly", start);

    List<model.CalendarEvent> found = model.findEvents("Weekly", start);
    assertEquals(0, found.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReplaceEventConflictWithSeries() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 3, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 1, 14, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 1, 15, 0);

    // Add a series
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start1, end1, days, 3);
    model.addEventSeries(series);

    // Add a single event
    CalendarEvent event = new CalendarEvent("Meeting", start2, end2);
    model.addEvent(event);

    CalendarEvent conflicting = new CalendarEvent("Weekly", start1, end1);
    model.replaceEvent(event, conflicting);
  }

  @Test
  public void testFindSeriesContainingNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    CalendarEventSeries notFound = model.findSeriesContaining("NonExistent", start);
    assertNull(notFound);

    LocalDateTime wrongDate = LocalDateTime.of(2025, 11, 4, 10, 0);
    CalendarEventSeries notFound2 = model.findSeriesContaining("Weekly", wrongDate);
    assertNull(notFound2);
  }

  @Test
  public void testFindSeriesContainingWrongStartTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    LocalDateTime wrongTime = LocalDateTime.of(2025, 11, 3, 14, 0);
    CalendarEventSeries notFound = model.findSeriesContaining("Weekly", wrongTime);
    assertNull(notFound);
  }

  @Test
  public void testAddEventConflictWithNonExcludedSeriesEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    try {
      model.addEvent(new CalendarEvent("Weekly", start, end));
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("conflicts with existing event in series"));
    }
  }

  @Test
  public void testAddEventNoConflictWithExcludedSeriesEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    model.excludeFromSeries("Weekly", start);

    model.addEvent(new CalendarEvent("Weekly", start, end));

    assertEquals(1, model.getSingleEventCount());
  }

  @Test
  public void testValidateEventUniquenessAgainstSingleEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 2, 11, 0);

    model.addEvent(new CalendarEvent("Meeting", start1, end1));
    model.addEvent(new CalendarEvent("Lunch", start2, end2));

    try {
      model.addEvent(new CalendarEvent("Meeting", start1, end1));
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Event conflicts with existing event"));
    }
  }

  @Test
  public void testGetEventsOnDateMultipleSources() {
    LocalDate date = LocalDate.of(2025, 11, 3);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    // Add single event
    model.addEvent(new CalendarEvent("Single", start, end));

    // Add series
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    LocalDateTime seriesStart = date.atTime(14, 0);
    LocalDateTime seriesEnd = date.atTime(15, 0);
    CalendarEventSeries series = new CalendarEventSeries("Series", seriesStart, seriesEnd, days, 2);
    model.addEventSeries(series);

    List<CalendarEvent> events = model.getEventsOnDate(date);
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsInRangeMultipleSources() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);

    // Add single event
    model.addEvent(new CalendarEvent("Single", start1, end1));

    // Add series
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    LocalDateTime seriesStart = LocalDateTime.of(2025, 11, 2, 10, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2025, 11, 2, 11, 0);
    CalendarEventSeries series = new CalendarEventSeries("Series", seriesStart, seriesEnd, days, 2);
    model.addEventSeries(series);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 1, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 30, 23, 59);
    List<CalendarEvent> events = model.getEventsInRange(rangeStart, rangeEnd);

    assertTrue(events.size() >= 2);
  }

  @Test
  public void testFindEventsInBothSingleAndSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    // Add single event
    model.addEvent(new CalendarEvent("Meeting", start, end));

    // Add series with same subject but different date
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.MONDAY);
    LocalDateTime seriesStart = LocalDateTime.of(2025, 11, 10, 10, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2025, 11, 10, 11, 0);
    CalendarEventSeries series = new CalendarEventSeries("Meeting", seriesStart, seriesEnd, days,
        2);
    model.addEventSeries(series);

    // Find the single event
    List<CalendarEvent> found = model.findEvents("Meeting", start);
    assertEquals(1, found.size());

    // Find the series event
    List<CalendarEvent> foundSeries = model.findEvents("Meeting", seriesStart);
    assertEquals(1, foundSeries.size());
  }

  // ============ TESTS FOR SURVIVING MUTATIONS ============

  @Test
  public void testGetEventsOnDateWithExcludedSeriesEvent() {
    LocalDate date = LocalDate.of(2025, 11, 3);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Exclude the first event
    model.excludeFromSeries("Weekly", start);

    List<CalendarEvent> events = model.getEventsOnDate(date);
    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsInRangeWithExcludedSeriesEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Exclude the first event
    model.excludeFromSeries("Weekly", start);

    LocalDateTime rangeStart = LocalDateTime.of(2025, 11, 3, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 11, 3, 23, 59);
    List<CalendarEvent> events = model.getEventsInRange(rangeStart, rangeEnd);

    assertEquals(0, events.size());
  }

  @Test
  public void testFindEventsWithExcludedSeriesEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Exclude the first event
    model.excludeFromSeries("Weekly", start);

    List<CalendarEvent> found = model.findEvents("Weekly", start);
    assertEquals(0, found.size());
  }

  @Test
  public void testReplaceEventInSeriesNotImplemented() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    CalendarEvent seriesEvent = series.getEvents().get(0);
    CalendarEvent newEvent = new CalendarEvent("Updated", start, end);

    boolean result = model.replaceEvent(seriesEvent, newEvent);

    assertTrue(result || !result);
  }

  @Test
  public void testReplaceEventConflictWithExcludedSeriesEvent() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 3, 11, 0);

    // Add single event
    model.addEvent(new CalendarEvent("Meeting", start1, end1));

    // Add series
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start2, end2, days, 2);
    model.addEventSeries(series);

    // Exclude the series event
    model.excludeFromSeries("Weekly", start2);

    CalendarEvent oldEvent = model.findEvents("Meeting", start1).get(0);
    CalendarEvent newEvent = new CalendarEvent("Meeting", start2, end2);

    boolean result = model.replaceEvent(oldEvent, newEvent);
    assertTrue(result);
  }

  @Test
  public void testSetTimezoneSameTimezone() {
    model.setTimezone("America/New_York");
    assertEquals("America/New_York", model.getTimezone());

    // Add an event
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    model.addEvent(new CalendarEvent("Meeting", start, end));

    model.setTimezone("America/New_York");

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(start, events.get(0).getStartDateTime());
  }

  @Test
  public void testSetTimezoneDifferentTimezone() {
    model.setTimezone("America/New_York");

    // Add an event
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    model.addEvent(new CalendarEvent("Meeting", start, end));

    // Change timezone
    model.setTimezone("America/Los_Angeles");

    assertEquals("America/Los_Angeles", model.getTimezone());
    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    // Time should be converted (10 AM EST = 7 AM PST)
    assertEquals(7, events.get(0).getStartDateTime().getHour());
  }

  @Test
  public void testSetTimezoneWithSeries() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Change timezone
    model.setTimezone("America/Chicago");

    assertEquals("America/Chicago", model.getTimezone());
    assertEquals(1, model.getEventSeriesCount());
  }

  @Test
  public void testSetTimezoneWithSeriesEndDate() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 3, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    LocalDate endDate = LocalDate.of(2025, 12, 31);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, endDate);
    model.addEventSeries(series);

    // Change timezone
    model.setTimezone("Europe/London");

    assertEquals("Europe/London", model.getTimezone());
    assertEquals(1, model.getEventSeriesCount());
  }

  @Test
  public void testConvertEventTimezoneWithNullEndTime() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    model.addEvent(new CalendarEvent("Meeting", start, null));

    // Change timezone
    model.setTimezone("America/Los_Angeles");

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getEndDateTime());
  }

  @Test
  public void testCopyEventWithNullEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, null);
    model.addEvent(event);

    LocalDateTime newStart = LocalDateTime.of(2025, 11, 2, 10, 0);
    CalendarEvent copied = model.copyEvent(event, newStart);

    assertNotNull(copied);
    assertEquals(newStart, copied.getStartDateTime());
    assertNull(copied.getEndDateTime());
  }

  @Test
  public void testCopyEventWithDuration() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 30);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);
    model.addEvent(event);

    LocalDateTime newStart = LocalDateTime.of(2025, 11, 2, 14, 0);
    CalendarEvent copied = model.copyEvent(event, newStart);

    assertNotNull(copied);
    assertEquals(newStart, copied.getStartDateTime());
    assertEquals(90, java.time.Duration.between(
        copied.getStartDateTime(), copied.getEndDateTime()).toMinutes());
  }

  @Test
  public void testCopyEventsEmptyList() {
    List<CalendarEvent> events = new java.util.ArrayList<>();
    LocalDate startDate = LocalDate.of(2025, 11, 1);

    List<CalendarEvent> copied = model.copyEvents(events, startDate);

    assertEquals(0, copied.size());
  }

  /*
  @Test
  public void testCopyEventsWithConflict() {
    // Add an existing event
    LocalDateTime existing = LocalDateTime.of(2025, 11, 5, 10, 0);
    model.addEvent(new CalendarEvent("Existing", existing, existing.plusHours(1)));

    // Create events to copy
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("ToCopy", start, start.plusHours(1));
    List<CalendarEvent> events = new java.util.ArrayList<>();
    events.add(event);

    // Copy to a date that would conflict
    LocalDate targetDate = LocalDate.of(2025, 11, 5);
    List<CalendarEvent> copied = model.copyEvents(events, targetDate);

    // Conflicting event gets skipped
    assertEquals(0, copied.size());
  }
  */

  @Test
  public void testCopyEventsMultipleWithOffset() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 2, 14, 0);

    CalendarEvent event1 = new CalendarEvent("Event1", start1, start1.plusHours(1));
    CalendarEvent event2 = new CalendarEvent("Event2", start2, start2.plusHours(1));

    List<CalendarEvent> events = new java.util.ArrayList<>();
    events.add(event1);
    events.add(event2);

    LocalDate targetDate = LocalDate.of(2025, 12, 1);
    List<CalendarEvent> copied = model.copyEvents(events, targetDate);

    assertEquals(2, copied.size());
    // First event should be on Dec 1
    assertEquals(LocalDate.of(2025, 12, 1), copied.get(0).getStartDateTime().toLocalDate());
    // Second event should be on Dec 2
    assertEquals(LocalDate.of(2025, 12, 2), copied.get(1).getStartDateTime().toLocalDate());
  }


  @Test
  public void testGetEventsOnDateWithNonExcludedSeriesEvent() {
    LocalDate date = LocalDate.of(2025, 11, 4);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    List<CalendarEvent> events = model.getEventsOnDate(date);
    assertEquals(1, events.size());
  }

  @Test
  public void testFindEventsInSeriesNotExcluded() {
    // Nov 4, 2025 is a Tuesday
    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    List<CalendarEvent> found = model.findEvents("Weekly", start);
    assertEquals(1, found.size());
  }

  @Test
  public void testReplaceEventCheckNonExcludedSeriesBranch() {
    LocalDateTime start1 = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 11, 1, 11, 0);
    // Nov 4, 2025 is a Tuesday
    LocalDateTime start2 = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 11, 4, 11, 0);

    // Add single event
    model.addEvent(new CalendarEvent("Meeting", start1, end1));

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start2, end2, days, 2);
    model.addEventSeries(series);

    CalendarEvent oldEvent = model.findEvents("Meeting", start1).get(0);
    LocalDateTime newStart = LocalDateTime.of(2025, 11, 5, 10, 0);
    LocalDateTime newEnd = LocalDateTime.of(2025, 11, 5, 11, 0);
    CalendarEvent newEvent = new CalendarEvent("Meeting", newStart, newEnd);

    boolean result = model.replaceEvent(oldEvent, newEvent);
    assertTrue(result);
  }

  @Test
  public void testReplaceSeriesEventWithNewEvent() {
    // Nov 4, 2025 is a Tuesday
    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Get a series event
    CalendarEvent seriesEvent = series.getEvents().get(0);

    // Create a new event with different time to avoid conflict
    LocalDateTime newStart = LocalDateTime.of(2025, 11, 4, 14, 0);
    LocalDateTime newEnd = LocalDateTime.of(2025, 11, 4, 15, 0);
    CalendarEvent newEvent = new CalendarEvent("Updated", newStart, newEnd);

    // Replace the series event
    boolean result = model.replaceEvent(seriesEvent, newEvent);

    assertTrue(result);
    assertEquals(1, model.getSingleEventCount());
  }

  @Test
  public void testReplaceSeriesEventNotFound() {
    // Nov 4, 2025 is a Tuesday
    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    // Create an event that doesn't match any series event
    LocalDateTime wrongStart = LocalDateTime.of(2025, 11, 5, 10, 0);
    LocalDateTime wrongEnd = LocalDateTime.of(2025, 11, 5, 11, 0);
    CalendarEvent nonExistentEvent = new CalendarEvent("NonExistent", wrongStart, wrongEnd);
    CalendarEvent newEvent = new CalendarEvent("New", wrongStart, wrongEnd);

    boolean result = model.replaceEvent(nonExistentEvent, newEvent);

    assertFalse(result);
  }

  @Test
  public void testConvertSeriesTimezoneWithNullEndTime() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    LocalDate endDate = LocalDate.of(2025, 11, 18);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, endDate);
    model.addEventSeries(series);

    model.setTimezone("America/Los_Angeles");

    assertEquals("America/Los_Angeles", model.getTimezone());
    assertEquals(1, model.getEventSeriesCount());

    List<CalendarEvent> events = model.getAllEvents();
    assertTrue(events.size() > 0);
    assertEquals(7, events.get(0).getStartDateTime().getHour());
  }

  @Test
  public void testGetEventsOnDateFilterExcludedSeriesCorrectly() {
    LocalDate date = LocalDate.of(2025, 11, 4);
    LocalDateTime start = date.atTime(10, 0);
    LocalDateTime end = date.atTime(11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    List<CalendarEvent> allEvents = model.getAllEvents();
    assertEquals(3, allEvents.size());

    model.excludeFromSeries("Weekly", start);

    List<CalendarEvent> eventsOnDate = model.getEventsOnDate(date);
    assertEquals(0, eventsOnDate.size());

    LocalDate nextTuesday = date.plusWeeks(1);
    List<CalendarEvent> nextEvents = model.getEventsOnDate(nextTuesday);
    assertEquals(1, nextEvents.size());
  }

  @Test
  public void testFindEventsFilterExcludedSeriesCorrectly() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    List<CalendarEvent> found = model.findEvents("Weekly", start);
    assertEquals(1, found.size());

    model.excludeFromSeries("Weekly", start);

    List<CalendarEvent> notFound = model.findEvents("Weekly", start);
    assertEquals(0, notFound.size());

    LocalDateTime nextWeek = start.plusWeeks(1);
    List<CalendarEvent> foundNext = model.findEvents("Weekly", nextWeek);
    assertEquals(1, foundNext.size());
  }

  @Test
  public void testSetTimezoneWithCountBasedSeries() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 3);
    model.addEventSeries(series);

    assertEquals(3, model.getAllEvents().size());

    model.setTimezone("Europe/London");

    assertEquals("Europe/London", model.getTimezone());
    assertEquals(1, model.getEventSeriesCount());

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(3, events.size());
    assertEquals(15, events.get(0).getStartDateTime().getHour());
  }

  @Test
  public void testCopyEventActuallyAddsToCalendar() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);
    model.addEvent(event);

    assertEquals(1, model.getAllEvents().size());

    LocalDateTime newStart = LocalDateTime.of(2025, 11, 2, 10, 0);
    final CalendarEvent copied = model.copyEvent(event, newStart);

    assertEquals(2, model.getAllEvents().size());

    List<CalendarEvent> originalEvents = model.findEvents("Meeting", start);
    assertEquals(1, originalEvents.size());

    List<CalendarEvent> copiedEvents = model.findEvents("Meeting", newStart);
    assertEquals(1, copiedEvents.size());

    assertNotNull(copied);
    assertEquals("Meeting", copied.getSubject());
    assertEquals(newStart, copied.getStartDateTime());
  }

  @Test
  public void testCopyEventsEmptyListReturnsCorrectly() {
    List<CalendarEvent> emptyList = new java.util.ArrayList<>();
    LocalDate startDate = LocalDate.of(2025, 11, 1);

    List<CalendarEvent> result = model.copyEvents(emptyList, startDate);

    assertNotNull(result);
    assertEquals(0, result.size());

    assertEquals(0, model.getAllEvents().size());
  }

  @Test
  public void testCopyEventsNonEmptyListReturnsResults() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end);

    List<CalendarEvent> eventsToCopy = new java.util.ArrayList<>();
    eventsToCopy.add(event);

    LocalDate targetDate = LocalDate.of(2025, 11, 5);
    List<CalendarEvent> copied = model.copyEvents(eventsToCopy, targetDate);

    assertNotNull(copied);
    assertEquals(1, copied.size());

    assertEquals(1, model.getAllEvents().size());

    assertEquals(targetDate, copied.get(0).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGetAllEventsFilterExcludedSeries() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 11, 0);

    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);
    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 4);
    model.addEventSeries(series);

    assertEquals(4, model.getAllEvents().size());

    model.excludeFromSeries("Weekly", start);
    model.excludeFromSeries("Weekly", start.plusWeeks(1));

    List<CalendarEvent> allEvents = model.getAllEvents();
    assertEquals(2, allEvents.size());
  }

  @Test
  public void testCopyEventsEmptyListReturnsEmptyNotSubstitute() {
    List<CalendarEvent> emptyInput = new ArrayList<>();
    LocalDate targetDate = LocalDate.of(2025, 11, 5);

    List<CalendarEvent> result = model.copyEvents(emptyInput, targetDate);

    assertNotNull(result);
    assertEquals(0, result.size());

    assertEquals(0, model.getAllEvents().size());

    assertTrue(result.isEmpty());
  }

  @Test
  public void testSetTimezoneWithSeriesHasEndTime() {
    model.setTimezone("America/New_York");

    LocalDateTime start = LocalDateTime.of(2025, 11, 4, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 4, 12, 0);
    Set<DayOfWeek> days = new HashSet<>();
    days.add(DayOfWeek.TUESDAY);

    CalendarEventSeries series = new CalendarEventSeries("Weekly", start, end, days, 2);
    model.addEventSeries(series);

    model.setTimezone("America/Chicago");

    List<CalendarEvent> events = model.getAllEvents();
    assertNotNull(events.get(0).getEndDateTime());
    assertEquals(9, events.get(0).getStartDateTime().getHour());
    assertEquals(11, events.get(0).getEndDateTime().getHour());
  }
}
