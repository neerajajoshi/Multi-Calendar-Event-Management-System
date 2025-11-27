import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.DayOfWeek;
import java.util.Set;
import org.junit.Test;
import util.WeekdayParser;

/**
 * Test class for WeekdayParser utility.
 */
public class WeekdayParserTest {

  // ============ PARSE WEEKDAYS TESTS ============

  @Test
  public void testParseSingleWeekday() {
    Set<DayOfWeek> days = WeekdayParser.parseWeekdays("M");

    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testParseMultipleWeekdays() {
    Set<DayOfWeek> days = WeekdayParser.parseWeekdays("MWF");

    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseAllWeekdays() {
    Set<DayOfWeek> days = WeekdayParser.parseWeekdays("MTWRFSU");

    assertEquals(7, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.THURSDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
    assertTrue(days.contains(DayOfWeek.SATURDAY));
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseWeekdaysLowercase() {
    Set<DayOfWeek> days = WeekdayParser.parseWeekdays("mwf");

    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseDuplicateWeekdays() {
    Set<DayOfWeek> days = WeekdayParser.parseWeekdays("MMM");

    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysNull() {
    WeekdayParser.parseWeekdays(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysEmpty() {
    WeekdayParser.parseWeekdays("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysInvalidCharacter() {
    WeekdayParser.parseWeekdays("MXF");
  }

  // ============ FORMAT WEEKDAYS TESTS ============

  @Test
  public void testFormatSingleWeekday() {
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY);
    String formatted = WeekdayParser.formatWeekdays(days);

    assertEquals("M", formatted);
  }

  @Test
  public void testFormatMultipleWeekdays() {
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    String formatted = WeekdayParser.formatWeekdays(days);

    assertEquals("MWF", formatted);
  }

  @Test
  public void testFormatAllWeekdays() {
    Set<DayOfWeek> days = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    String formatted = WeekdayParser.formatWeekdays(days);

    assertEquals("MTWRFSU", formatted);
  }

  @Test
  public void testFormatEmptySet() {
    Set<DayOfWeek> days = Set.of();
    String formatted = WeekdayParser.formatWeekdays(days);

    assertEquals("", formatted);
  }

  @Test
  public void testFormatNull() {
    String formatted = WeekdayParser.formatWeekdays(null);

    assertEquals("", formatted);
  }

  // ============ CONSTRUCTOR TEST ============

  @Test
  public void testConstructorThrowsException() {
    try {
      java.lang.reflect.Constructor<WeekdayParser> constructor =
          WeekdayParser.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
      fail("Should throw AssertionError");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof AssertionError);
      assertEquals("Utility class should not be instantiated", e.getCause().getMessage());
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }
}