import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import util.ValidationUtils;

/**
 * Test class for ValidationUtils.
 */
public class ValidationUtilsTest {

  // ============ VALIDATE NOT EMPTY TESTS ============

  @Test
  public void testValidateNotEmptyValid() {
    ValidationUtils.validateNotEmpty("test", "Field");
  }

  @Test(expected = NullPointerException.class)
  public void testValidateNotEmptyNull() {
    ValidationUtils.validateNotEmpty(null, "Field");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNotEmptyEmpty() {
    ValidationUtils.validateNotEmpty("", "Field");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateNotEmptyWhitespace() {
    ValidationUtils.validateNotEmpty("   ", "Field");
  }

  // ============ VALIDATE CALENDAR NAME TESTS ============

  @Test
  public void testValidateCalendarNameValid() {
    ValidationUtils.validateCalendarName("MyCalendar");
    ValidationUtils.validateCalendarName("Work_Calendar");
    ValidationUtils.validateCalendarName("Calendar-2025");
    ValidationUtils.validateCalendarName("My Calendar 123");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarNameEmpty() {
    ValidationUtils.validateCalendarName("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarNameTooLong() {
    ValidationUtils.validateCalendarName("a".repeat(51));
  }

  @Test
  public void testValidateCalendarNameExactly50Chars() {
    ValidationUtils.validateCalendarName("a".repeat(50));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarNameInvalidCharacters() {
    ValidationUtils.validateCalendarName("Calendar@2025");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarNameSpecialChars() {
    ValidationUtils.validateCalendarName("Calendar!#$");
  }

  // ============ VALIDATE EVENT SUBJECT TESTS ============

  @Test
  public void testValidateEventSubjectValid() {
    ValidationUtils.validateEventSubject("Meeting");
    ValidationUtils.validateEventSubject("Team Standup @ 10 AM");
    // Valid subjects pass without error
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEventSubjectEmpty() {
    ValidationUtils.validateEventSubject("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEventSubjectTooLong() {
    ValidationUtils.validateEventSubject("a".repeat(101));
  }

  @Test
  public void testValidateEventSubjectExactly100Chars() {
    ValidationUtils.validateEventSubject("a".repeat(100));
  }

  @Test(expected = NullPointerException.class)
  public void testValidateEventSubjectNull() {
    ValidationUtils.validateEventSubject(null);
  }

  // ============ VALIDATE TIME ORDER TESTS ============

  @Test
  public void testValidateTimeOrderValid() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    ValidationUtils.validateTimeOrder(start, end);
    // Valid time order passes
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateTimeOrderSameTime() {
    LocalDateTime time = LocalDateTime.of(2025, 11, 1, 10, 0);

    ValidationUtils.validateTimeOrder(time, time);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateTimeOrderEndBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 10, 0);

    ValidationUtils.validateTimeOrder(start, end);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateTimeOrderNullStart() {
    LocalDateTime end = LocalDateTime.of(2025, 11, 1, 11, 0);

    ValidationUtils.validateTimeOrder(null, end);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateTimeOrderNullEnd() {
    LocalDateTime start = LocalDateTime.of(2025, 11, 1, 10, 0);

    ValidationUtils.validateTimeOrder(start, null);
  }

  // ============ VALIDATE FUTURE DATE TESTS ============

  @Test
  public void testValidateFutureDateValid() {
    LocalDate futureDate = LocalDate.now().plusDays(1);

    ValidationUtils.validateFutureDate(futureDate);
    // Future dates are valid
  }

  @Test
  public void testValidateFutureDateToday() {
    LocalDate today = LocalDate.now();

    ValidationUtils.validateFutureDate(today);
    // Today is valid too
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateFutureDatePast() {
    LocalDate pastDate = LocalDate.now().minusDays(1);

    ValidationUtils.validateFutureDate(pastDate);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateFutureDateNull() {
    ValidationUtils.validateFutureDate(null);
  }

  // ============ VALIDATE TIMEZONE TESTS ============

  @Test
  public void testValidateTimezoneValid() {
    ValidationUtils.validateTimezone("America/New_York");
    ValidationUtils.validateTimezone("Europe/London");
    ValidationUtils.validateTimezone("UTC");
    // Valid timezones pass
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateTimezoneInvalid() {
    ValidationUtils.validateTimezone("Invalid/Timezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateTimezoneEmpty() {
    ValidationUtils.validateTimezone("");
  }

  @Test(expected = NullPointerException.class)
  public void testValidateTimezoneNull() {
    ValidationUtils.validateTimezone(null);
  }

  // ============ VALIDATE AND DETECT EXPORT FORMAT TESTS ============

  @Test
  public void testValidateAndDetectExportFormatCsv() {
    String format = ValidationUtils.validateAndDetectExportFormat("export.csv");

    assertEquals("csv", format);
  }

  @Test
  public void testValidateAndDetectExportFormatCsvUppercase() {
    String format = ValidationUtils.validateAndDetectExportFormat("export.CSV");

    assertEquals("csv", format);
  }

  @Test
  public void testValidateAndDetectExportFormatIcs() {
    String format = ValidationUtils.validateAndDetectExportFormat("export.ics");

    assertEquals("ical", format);
  }

  @Test
  public void testValidateAndDetectExportFormatIcal() {
    String format = ValidationUtils.validateAndDetectExportFormat("export.ical");

    assertEquals("ical", format);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateAndDetectExportFormatUnsupported() {
    ValidationUtils.validateAndDetectExportFormat("export.txt");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateAndDetectExportFormatNoExtension() {
    ValidationUtils.validateAndDetectExportFormat("export");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateAndDetectExportFormatEmpty() {
    ValidationUtils.validateAndDetectExportFormat("");
  }

  // ============ VALIDATE CALENDAR PROPERTY TESTS ============

  @Test
  public void testValidateCalendarPropertyName() {
    ValidationUtils.validateCalendarProperty("name");
    // Valid property
  }

  @Test
  public void testValidateCalendarPropertyTimezone() {
    ValidationUtils.validateCalendarProperty("timezone");
    // Valid property
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarPropertyInvalid() {
    ValidationUtils.validateCalendarProperty("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateCalendarPropertyEmpty() {
    ValidationUtils.validateCalendarProperty("");
  }

  // ============ VALIDATE EVENT PROPERTY TESTS ============

  @Test
  public void testValidateEventPropertySubject() {
    ValidationUtils.validateEventProperty("subject");
    // Valid property
  }

  @Test
  public void testValidateEventPropertyStart() {
    ValidationUtils.validateEventProperty("start");
    // Valid property
  }

  @Test
  public void testValidateEventPropertyEnd() {
    ValidationUtils.validateEventProperty("end");
    // Valid property
  }

  @Test
  public void testValidateEventPropertyDescription() {
    ValidationUtils.validateEventProperty("description");
    // Valid property
  }

  @Test
  public void testValidateEventPropertyLocation() {
    ValidationUtils.validateEventProperty("location");
    // Valid property
  }

  @Test
  public void testValidateEventPropertyStatus() {
    ValidationUtils.validateEventProperty("status");
    // Valid property
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEventPropertyInvalid() {
    ValidationUtils.validateEventProperty("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEventPropertyEmpty() {
    ValidationUtils.validateEventProperty("");
  }

  // ============ ERROR MESSAGE TESTS ============

  @Test
  public void testCreateDateTimeErrorMessage() {
    String message = ValidationUtils.createDateTimeErrorMessage("invalid", "YYYY-MM-DD");

    assertTrue(message.contains("invalid"));
    assertTrue(message.contains("YYYY-MM-DD"));
    assertTrue(message.contains("Invalid date/time format"));
  }

  @Test
  public void testCreateCommandErrorMessage() {
    String message = ValidationUtils.createCommandErrorMessage("bad command", "expected format");

    assertTrue(message.contains("bad command"));
    assertTrue(message.contains("expected format"));
    assertTrue(message.contains("Invalid command format"));
  }

  // ============ ADDITIONAL BOUNDARY TESTS ============

  @Test(expected = NullPointerException.class)
  public void testValidateCalendarNameNull() {
    ValidationUtils.validateCalendarName(null);
  }

  @Test
  public void testValidateCalendarPropertyCaseSensitive() {
    // Property names are case-sensitive
    try {
      ValidationUtils.validateCalendarProperty("Name");
      fail("Should throw exception for incorrect case");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid property"));
    }
  }

  @Test(expected = NullPointerException.class)
  public void testValidateCalendarPropertyNull() {
    ValidationUtils.validateCalendarProperty(null);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateEventPropertyNull() {
    ValidationUtils.validateEventProperty(null);
  }

  @Test(expected = NullPointerException.class)
  public void testValidateAndDetectExportFormatNull() {
    ValidationUtils.validateAndDetectExportFormat(null);
  }

  // ============ CONSTRUCTOR TEST ============

  @Test
  public void testConstructorThrowsException() {
    try {
      java.lang.reflect.Constructor<ValidationUtils> constructor =
          ValidationUtils.class.getDeclaredConstructor();
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
  public void testValidateCalendarNameWithValidateNotEmptyCall() {
    try {
      ValidationUtils.validateCalendarName("");
      fail("Should throw exception for empty name");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be empty"));
    }
  }

  @Test
  public void testValidateCalendarNameNullThrowsNullPointer() {
    try {
      ValidationUtils.validateCalendarName(null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testValidateAndDetectExportFormatWithValidateNotEmptyCall() {
    try {
      ValidationUtils.validateAndDetectExportFormat("");
      fail("Should throw exception for empty filename");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be empty"));
    }
  }

  @Test
  public void testValidateAndDetectExportFormatNullThrowsNullPointer() {
    try {
      ValidationUtils.validateAndDetectExportFormat(null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testValidateCalendarPropertyWithValidateNotEmptyCall() {
    try {
      ValidationUtils.validateCalendarProperty("");
      fail("Should throw exception for empty property");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be empty"));
    }
  }

  @Test
  public void testValidateCalendarPropertyNullThrowsNullPointer() {
    try {
      ValidationUtils.validateCalendarProperty(null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testValidateCalendarNameWithWhitespaceOnly() {
    try {
      ValidationUtils.validateCalendarName("   ");
      fail("Should throw exception for whitespace-only name");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be empty"));
    }
  }

  @Test
  public void testValidateCalendarPropertyWithWhitespaceOnly() {
    try {
      ValidationUtils.validateCalendarProperty("   ");
      fail("Should throw exception for whitespace-only property");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be empty"));
    }
  }
}
