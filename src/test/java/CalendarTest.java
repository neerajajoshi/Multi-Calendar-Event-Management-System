import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A placeholder test class.
 */
public class CalendarTest {
  @Test
  public void testDummyCalendarName() {
    calendar.DummyCalendar calendar = new calendar.DummyCalendar();
    assertEquals("DummyCalendar", calendar.getName());
  }
}
