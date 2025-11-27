import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import util.TimezoneConverter;

/**
 * Test class for TimezoneConverter utility.
 */
public class TimezoneConverterTest {

  @Test
  public void testConvertTimezoneSameTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime converted = TimezoneConverter.convertTimezone(
        dateTime, "America/New_York", "America/New_York");

    assertEquals(dateTime, converted);
  }

  @Test
  public void testConvertTimezoneDifferentTimezones() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime converted = TimezoneConverter.convertTimezone(
        dateTime, "America/New_York", "America/Los_Angeles");

    assertNotNull(converted);
    // 10 AM EST is 7 AM PST (3 hour difference)
    assertEquals(7, converted.getHour());
  }

  @Test(expected = NullPointerException.class)
  public void testConvertTimezoneNullDateTime() {
    TimezoneConverter.convertTimezone(null, "America/New_York", "America/Los_Angeles");
  }

  @Test(expected = NullPointerException.class)
  public void testConvertTimezoneNullFromTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    TimezoneConverter.convertTimezone(dateTime, null, "America/Los_Angeles");
  }

  @Test(expected = NullPointerException.class)
  public void testConvertTimezoneNullToTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    TimezoneConverter.convertTimezone(dateTime, "America/New_York", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTimezoneInvalidFromTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    TimezoneConverter.convertTimezone(dateTime, "Invalid/Timezone", "America/Los_Angeles");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTimezoneInvalidToTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    TimezoneConverter.convertTimezone(dateTime, "America/New_York", "Invalid/Timezone");
  }

  @Test
  public void testConvertDateTimezone() {
    LocalDate date = LocalDate.of(2025, 11, 1);
    LocalDate converted = TimezoneConverter.convertDateTimezone(
        date, "America/New_York", "America/Los_Angeles");

    assertNotNull(converted);
    assertTrue(converted.equals(date) || converted.equals(date.minusDays(1)));
  }

  @Test
  public void testIsValidTimezoneValid() {
    assertTrue(TimezoneConverter.isValidTimezone("America/New_York"));
    assertTrue(TimezoneConverter.isValidTimezone("Europe/London"));
    assertTrue(TimezoneConverter.isValidTimezone("Asia/Tokyo"));
    assertTrue(TimezoneConverter.isValidTimezone("UTC"));
  }

  @Test
  public void testIsValidTimezoneInvalid() {
    assertFalse(TimezoneConverter.isValidTimezone("Invalid/Timezone"));
    assertFalse(TimezoneConverter.isValidTimezone("NotATimezone"));
    assertFalse(TimezoneConverter.isValidTimezone(""));
  }

  @Test
  public void testGetCurrentTimeInTimezone() {
    LocalDateTime currentTime = TimezoneConverter.getCurrentTimeInTimezone("America/New_York");

    assertNotNull(currentTime);
    assertTrue(currentTime.getYear() >= 2025);
  }

  @Test(expected = Exception.class)
  public void testGetCurrentTimeInTimezoneInvalid() {
    TimezoneConverter.getCurrentTimeInTimezone("Invalid/Timezone");
  }

  @Test
  public void testFormatWithTimezone() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 30);
    String formatted = TimezoneConverter.formatWithTimezone(dateTime, "America/New_York");

    assertNotNull(formatted);
    assertTrue(formatted.contains("2025-11-01 10:30"));
    assertTrue(formatted.contains("("));
    assertTrue(formatted.contains(")"));
  }

  @Test(expected = Exception.class)
  public void testFormatWithTimezoneInvalid() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 30);
    TimezoneConverter.formatWithTimezone(dateTime, "Invalid/Timezone");
  }

  @Test
  public void testConstructorThrowsException() {
    try {
      java.lang.reflect.Constructor<TimezoneConverter> constructor =
          TimezoneConverter.class.getDeclaredConstructor();
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

  @Test
  public void testConvertTimezoneAcrossDaylightSavingTime() {
    // Conversion during daylight saving time
    LocalDateTime dateTime = LocalDateTime.of(2025, 7, 1, 12, 0);
    LocalDateTime converted = TimezoneConverter.convertTimezone(
        dateTime, "America/New_York", "America/Chicago");

    assertNotNull(converted);
    // EDT to CDT is 1 hour difference
    assertEquals(11, converted.getHour());
  }

  @Test
  public void testConvertTimezoneToUtc() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime converted = TimezoneConverter.convertTimezone(
        dateTime, "America/New_York", "UTC");

    assertNotNull(converted);
    // 10 AM EST is 3 PM UTC (5 hour difference in November)
    assertTrue(converted.getHour() >= 14 && converted.getHour() <= 16);
  }
}
