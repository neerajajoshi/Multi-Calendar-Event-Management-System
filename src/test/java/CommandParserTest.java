import static org.junit.Assert.assertTrue;

import controller.Command;
import controller.CommandParser;
import controller.commands.CreateAllDayEventCommand;
import controller.commands.CreateAllDaySeriesCommand;
import controller.commands.CreateEventCommand;
import controller.commands.CreateSeriesCommand;
import controller.commands.EditEventCommand;
import controller.commands.EditEventsCommand;
import controller.commands.EditSeriesCommand;
import controller.commands.ExitCommand;
import controller.commands.ExportCommand;
import controller.commands.PrintEventsInRangeCommand;
import controller.commands.PrintEventsOnDateCommand;
import controller.commands.ShowStatusCommand;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CommandParser.
 */
public class CommandParserTest {
  private CommandParser parser;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    parser = new CommandParser();
  }

  // ============ EXIT COMMAND TESTS ============

  @Test
  public void testParseExitCommand() {
    Command command = parser.parseCommand("exit");
    assertTrue(command instanceof ExitCommand);
  }

  @Test
  public void testParseExitCommandCaseInsensitive() {
    Command command = parser.parseCommand("EXIT");
    assertTrue(command instanceof ExitCommand);

    command = parser.parseCommand("Exit");
    assertTrue(command instanceof ExitCommand);
  }

  // ============ INVALID/EMPTY COMMAND TESTS ============

  @Test(expected = IllegalArgumentException.class)
  public void testParseEmptyCommand() {
    parser.parseCommand("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNullCommand() {
    parser.parseCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWhitespaceOnlyCommand() {
    parser.parseCommand("   ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidCommand() {
    parser.parseCommand("invalid command");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePartialCommand() {
    parser.parseCommand("create");
  }

  // ============ CREATE EVENT - SIMPLE TESTS ============

  @Test
  public void testParseCreateEventSimple() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00");

    assertTrue(command instanceof CreateEventCommand);
  }

  @Test
  public void testParseCreateEventWithQuotedSubject() {
    Command command = parser.parseCommand(
        "create event \"Team Meeting\" from 2025-11-01T10:00 to 2025-11-01T11:00");

    assertTrue(command instanceof CreateEventCommand);
  }

  @Test
  public void testParseCreateEventMultiWordQuoted() {
    Command command = parser.parseCommand(
        "create event \"Weekly Team Standup Meeting\" from 2025-11-01T10:00 "
        + "to 2025-11-01T11:00");

    assertTrue(command instanceof CreateEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventUnclosedQuote() {
    parser.parseCommand(
        "create event \"Team Meeting from 2025-11-01T10:00 to 2025-11-01T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventInvalidFormat() {
    parser.parseCommand("create event Meeting");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventInvalidDateTimeFormat() {
    parser.parseCommand(
        "create event Meeting from 2025/11/01 10:00 to 2025/11/01 11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventMissingTo() {
    parser.parseCommand(
        "create event Meeting from 2025-11-01T10:00");
  }

  // ============ CREATE ALL-DAY EVENT TESTS ============

  @Test
  public void testParseCreateAllDayEvent() {
    Command command = parser.parseCommand("create event Holiday on 2025-11-01");

    assertTrue(command instanceof CreateAllDayEventCommand);
  }

  @Test
  public void testParseCreateAllDayEventQuoted() {
    Command command = parser.parseCommand("create event \"Independence Day\" on 2025-07-04");

    assertTrue(command instanceof CreateAllDayEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateAllDayEventMissingDate() {
    parser.parseCommand("create event Holiday on");
  }

  // ============ CREATE SERIES WITH OCCURRENCES TESTS ============

  @Test
  public void testParseCreateSeriesWithOccurrences() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MWF for 10 times");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  @Test
  public void testParseCreateSeriesWithSingleDay() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-02T10:00 to 2025-11-02T11:00 "
        + "repeats M for 5 times");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  @Test
  public void testParseCreateSeriesWithAllDays() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MTWRFSU for 7 times");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  @Test
  public void testParseCreateSeriesQuotedSubject() {
    Command command = parser.parseCommand(
        "create event \"Weekly Standup\" from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MWF for 10 times");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateSeriesMissingOccurrences() {
    parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MWF for times");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateSeriesInvalidWeekday() {
    parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MXF for 10 times");
  }

  // ============ CREATE SERIES UNTIL DATE TESTS ============

  @Test
  public void testParseCreateSeriesUntilDate() {
    Command command = parser.parseCommand(
        "create event Meeting from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats MWF until 2025-12-31");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  @Test
  public void testParseCreateSeriesUntilDateQuoted() {
    Command command = parser.parseCommand(
        "create event \"Team Review\" from 2025-11-03T10:00 to 2025-11-03T11:00 "
        + "repeats TR until 2025-12-15");

    assertTrue(command instanceof CreateSeriesCommand);
  }

  // ============ CREATE ALL-DAY SERIES WITH OCCURRENCES TESTS ============

  @Test
  public void testParseCreateAllDaySeriesWithOccurrences() {
    Command command = parser.parseCommand(
        "create event Workday on 2025-11-03 repeats MTWRF for 20 times");

    assertTrue(command instanceof CreateAllDaySeriesCommand);
  }

  @Test
  public void testParseCreateAllDaySeriesQuoted() {
    Command command = parser.parseCommand(
        "create event \"Office Day\" on 2025-11-03 repeats MTWRF for 15 times");

    assertTrue(command instanceof CreateAllDaySeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateAllDaySeriesInvalidOccurrences() {
    parser.parseCommand(
        "create event Workday on 2025-11-03 repeats MTWRF for abc times");
  }

  // ============ CREATE ALL-DAY SERIES UNTIL DATE TESTS ============

  @Test
  public void testParseCreateAllDaySeriesUntilDate() {
    Command command = parser.parseCommand(
        "create event Workday on 2025-11-03 repeats MTWRF until 2025-12-31");

    assertTrue(command instanceof CreateAllDaySeriesCommand);
  }

  @Test
  public void testParseCreateAllDaySeriesUntilDateQuoted() {
    Command command = parser.parseCommand(
        "create event \"Gym Day\" on 2025-11-03 repeats MWF until 2025-12-15");

    assertTrue(command instanceof CreateAllDaySeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateAllDaySeriesMissingWeekdays() {
    parser.parseCommand(
        "create event Workday on 2025-11-03 repeats for 20 times");
  }

  // ============ PRINT EVENTS ON DATE TESTS ============

  @Test
  public void testParsePrintEventsOnDate() {
    Command command = parser.parseCommand("print events on 2025-11-01");

    assertTrue(command instanceof PrintEventsOnDateCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsOnDateMissingDate() {
    parser.parseCommand("print events on");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsInvalidCommand() {
    parser.parseCommand("print events");
  }

  // ============ PRINT EVENTS IN RANGE TESTS ============

  @Test
  public void testParsePrintEventsInRange() {
    Command command = parser.parseCommand(
        "print events from 2025-11-01T10:00 to 2025-11-30T17:00");

    assertTrue(command instanceof PrintEventsInRangeCommand);
  }

  @Test
  public void testParsePrintEventsInRangeSameDay() {
    Command command = parser.parseCommand(
        "print events from 2025-11-01T09:00 to 2025-11-01T17:00");

    assertTrue(command instanceof PrintEventsInRangeCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsInRangeMissingTo() {
    parser.parseCommand("print events from 2025-11-01T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsInRangeInvalidFormat() {
    parser.parseCommand(
        "print events from 2025-11-01T10:00 until 2025-11-30T17:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintEventsInRangeInvalidDateTime() {
    parser.parseCommand(
        "print events from 2025/11/01 10:00 to 2025/11/30 17:00");
  }

  // ============ SHOW STATUS TESTS ============

  @Test
  public void testParseShowStatus() {
    Command command = parser.parseCommand("show status on 2025-11-01T10:00");

    assertTrue(command instanceof ShowStatusCommand);
  }

  @Test
  public void testParseShowStatusDifferentTime() {
    Command command = parser.parseCommand("show status on 2025-12-25T14:30");

    assertTrue(command instanceof ShowStatusCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusMissingDateTime() {
    parser.parseCommand("show status on");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusInvalidFormat() {
    parser.parseCommand("show status at 2025-11-01T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusDateOnly() {
    parser.parseCommand("show status on 2025-11-01");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusInvalidDateTime() {
    parser.parseCommand("show status on 2025/11/01T10:00");
  }

  // ============ EDIT EVENT (SINGLE) TESTS ============

  @Test
  public void testParseEditEventSubject() {
    Command command = parser.parseCommand(
        "edit event subject Meeting from 2025-11-01T10:00 with \"Important Meeting\"");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventDescription() {
    Command command = parser.parseCommand(
        "edit event description Meeting from 2025-11-01T10:00 with \"Team standup\"");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventLocation() {
    Command command = parser.parseCommand(
        "edit event location Meeting from 2025-11-01T10:00 with \"Room 101\"");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventStatus() {
    Command command = parser.parseCommand(
        "edit event status Meeting from 2025-11-01T10:00 with private");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventStart() {
    Command command = parser.parseCommand(
        "edit event start Meeting from 2025-11-01T10:00 with 2025-11-01T09:00");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventEnd() {
    Command command = parser.parseCommand(
        "edit event end Meeting from 2025-11-01T10:00 with 2025-11-01T12:00");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventQuotedSubject() {
    Command command = parser.parseCommand(
        "edit event subject \"Team Meeting\" from 2025-11-01T10:00 with \"Weekly Standup\"");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventInvalidFormat() {
    parser.parseCommand(
        "edit event subject Meeting 2025-11-01T10:00 with New");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventMissingWith() {
    parser.parseCommand(
        "edit event subject Meeting from 2025-11-01T10:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventMissingProperty() {
    parser.parseCommand(
        "edit event Meeting from 2025-11-01T10:00 with New");
  }

  // ============ EDIT EVENTS (FROM DATE) TESTS ============

  @Test
  public void testParseEditEventsFromDate() {
    Command command = parser.parseCommand(
        "edit events subject Meeting from 2025-11-01T10:00 with \"New Meeting\"");

    assertTrue(command instanceof EditEventsCommand);
  }

  @Test
  public void testParseEditEventsLocation() {
    Command command = parser.parseCommand(
        "edit events location \"Team Standup\" from 2025-11-01T10:00 with \"Remote\"");

    assertTrue(command instanceof EditEventsCommand);
  }

  @Test
  public void testParseEditEventsDescription() {
    Command command = parser.parseCommand(
        "edit events description Meeting from 2025-11-01T10:00 with \"Updated description\"");

    assertTrue(command instanceof EditEventsCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditEventsInvalidFormat() {
    parser.parseCommand(
        "edit events subject Meeting with New");
  }

  // ============ EDIT SERIES (ALL EVENTS) TESTS ============

  @Test
  public void testParseEditSeries() {
    Command command = parser.parseCommand(
        "edit series subject Meeting from 2025-11-01T10:00 with \"Weekly Sync\"");

    assertTrue(command instanceof EditSeriesCommand);
  }

  @Test
  public void testParseEditSeriesLocation() {
    Command command = parser.parseCommand(
        "edit series location Meeting from 2025-11-01T10:00 with \"Conference Room\"");

    assertTrue(command instanceof EditSeriesCommand);
  }

  @Test
  public void testParseEditSeriesStatus() {
    Command command = parser.parseCommand(
        "edit series status Meeting from 2025-11-01T10:00 with private");

    assertTrue(command instanceof EditSeriesCommand);
  }

  @Test
  public void testParseEditSeriesQuotedSubject() {
    Command command = parser.parseCommand(
        "edit series subject \"Team Meeting\" from 2025-11-01T10:00 "
        + "with \"Leadership Meeting\"");

    assertTrue(command instanceof EditSeriesCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditSeriesMissingFrom() {
    parser.parseCommand(
        "edit series subject Meeting with New");
  }

  // ============ EXPORT COMMAND TESTS ============

  @Test
  public void testParseExportCommand() {
    Command command = parser.parseCommand("export cal calendar.csv");

    assertTrue(command instanceof ExportCommand);
  }

  @Test
  public void testParseExportCommandDifferentFilename() {
    Command command = parser.parseCommand("export cal my_events.csv");

    assertTrue(command instanceof ExportCommand);
  }

  @Test
  public void testParseExportCommandWithPath() {
    Command command = parser.parseCommand("export cal output/calendar.csv");

    assertTrue(command instanceof ExportCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseExportCommandMissingFilename() {
    parser.parseCommand("export cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseExportCommandInvalidFormat() {
    parser.parseCommand("export calendar.csv");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseExportCommandWrongKeyword() {
    parser.parseCommand("export calendar calendar.csv");
  }

  // ============ EDGE CASE TESTS ============

  @Test
  public void testParseCommandWithLeadingTrailingSpaces() {
    Command command = parser.parseCommand(
        "   create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00   ");

    assertTrue(command instanceof CreateEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCommandTypo() {
    parser.parseCommand("creat event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCommandWrongOrder() {
    parser.parseCommand("event create Meeting from 2025-11-01T10:00 to 2025-11-01T11:00");
  }

  // ============ CREATE CALENDAR TESTS ============

  @Test
  public void testParseCreateCalendar() {
    Command command = parser.parseCommand(
        "create calendar --name Work --timezone America/New_York");
    assertTrue(command instanceof controller.commands.CreateCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidFormat() {
    parser.parseCommand("create calendar Work America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarMissingTimezone() {
    parser.parseCommand("create calendar --name Work");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidTimezone() {
    parser.parseCommand("create calendar --name Work --timezone Invalid/Zone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCalendarInvalidName() {
    parser.parseCommand("create calendar --name Work@123 --timezone America/New_York");
  }

  // ============ EDIT CALENDAR TESTS ============

  @Test
  public void testParseEditCalendar() {
    Command command = parser.parseCommand(
        "edit calendar --name Work --property timezone America/Los_Angeles");
    assertTrue(command instanceof controller.commands.EditCalendarCommand);
  }

  @Test
  public void testParseEditCalendarName() {
    Command command = parser.parseCommand(
        "edit calendar --name Work --property name WorkCalendar");
    assertTrue(command instanceof controller.commands.EditCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalidFormat() {
    parser.parseCommand("edit calendar Work timezone America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalidProperty() {
    parser.parseCommand("edit calendar --name Work --property invalid value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarInvalidName() {
    parser.parseCommand("edit calendar --name Work@123 --property timezone America/New_York");
  }

  // ============ USE CALENDAR TESTS ============

  @Test
  public void testParseUseCalendar() {
    Command command = parser.parseCommand("use calendar --name Work");
    assertTrue(command instanceof controller.commands.UseCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarInvalidFormat() {
    parser.parseCommand("use calendar Work");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarMissingName() {
    parser.parseCommand("use calendar --name");
  }

  // ============ LIST CALENDARS TESTS ============

  @Test
  public void testParseListCalendars() {
    Command command = parser.parseCommand("list calendars");
    assertTrue(command instanceof controller.commands.ListCalendarsCommand);
  }

  @Test
  public void testParseShowCalendars() {
    Command command = parser.parseCommand("show calendars");
    assertTrue(command instanceof controller.commands.ListCalendarsCommand);
  }

  // ============ EXPORT ICAL TESTS ============

  @Test
  public void testParseExportIcal() {
    Command command = parser.parseCommand("export cal calendar.ical");
    assertTrue(command instanceof controller.commands.ExportIcalCommand);
  }

  @Test
  public void testParseExportIcs() {
    Command command = parser.parseCommand("export cal calendar.ics");
    assertTrue(command instanceof controller.commands.ExportIcalCommand);
  }

  @Test
  public void testParseExportIcalUppercase() {
    Command command = parser.parseCommand("export cal CALENDAR.ICAL");
    assertTrue(command instanceof controller.commands.ExportIcalCommand);
  }

  // ============ COPY EVENT TESTS ============

  @Test
  public void testParseCopyEvent() {
    Command command = parser.parseCommand(
        "copy event Meeting on 2025-11-01T10:00 --target Personal to 2025-11-02T14:00");
    assertTrue(command instanceof controller.commands.CopyEventCommand);
  }

  @Test
  public void testParseCopyEventQuoted() {
    Command command = parser.parseCommand(
        "copy event \"Team Meeting\" on 2025-11-01T10:00 --target Personal to 2025-11-02T14:00");
    assertTrue(command instanceof controller.commands.CopyEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventInvalidFormat() {
    parser.parseCommand("copy event Meeting 2025-11-01T10:00 Personal 2025-11-02T14:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventMissingTarget() {
    parser.parseCommand("copy event Meeting on 2025-11-01T10:00 to 2025-11-02T14:00");
  }

  // ============ COPY EVENTS ON DATE TESTS ============

  @Test
  public void testParseCopyEventsOnDate() {
    Command command = parser.parseCommand(
        "copy events on 2025-11-01 --target Personal to 2025-11-02");
    assertTrue(command instanceof controller.commands.CopyEventsOnDateCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateInvalidFormat() {
    parser.parseCommand("copy events on 2025-11-01 Personal 2025-11-02");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateMissingTarget() {
    parser.parseCommand("copy events on 2025-11-01 to 2025-11-02");
  }

  // ============ COPY EVENTS IN RANGE TESTS ============

  @Test
  public void testParseCopyEventsInRange() {
    Command command = parser.parseCommand(
        "copy events between 2025-11-01 and 2025-11-05 --target Personal to 2025-12-01");
    assertTrue(command instanceof controller.commands.CopyEventsInRangeCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsInRangeInvalidFormat() {
    parser.parseCommand(
        "copy events between 2025-11-01 and 2025-11-05 Personal 2025-12-01");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsInRangeMissingTarget() {
    parser.parseCommand("copy events between 2025-11-01 and 2025-11-05 to 2025-12-01");
  }

  // ============ EDIT COMMAND QUOTE HANDLING TESTS ============

  @Test
  public void testParseEditEventWithQuotedSubjectAndValue() {
    Command command = parser.parseCommand(
        "edit event subject \"Team Meeting\" from 2025-11-01T10:00 with \"Leadership Sync\"");
    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventWithQuotedValue() {
    Command command = parser.parseCommand(
        "edit event description Meeting from 2025-11-01T10:00 with \"Updated description text\"");
    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventsWithQuotedSubject() {
    Command command = parser.parseCommand(
        "edit events location \"Daily Standup\" from 2025-11-01T10:00 with \"Room 101\"");
    assertTrue(command instanceof EditEventsCommand);
  }

  @Test
  public void testParseEditSeriesWithQuotedValue() {
    Command command = parser.parseCommand(
        "edit series description Meeting from 2025-11-01T10:00 with \"New description\"");
    assertTrue(command instanceof EditSeriesCommand);
  }

  @Test
  public void testParseEditEventSubjectWithoutQuotes() {
    Command command = parser.parseCommand(
        "edit event subject Meeting from 2025-11-01T10:00 with NewMeeting");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseEditEventValueWithoutQuotes() {
    Command command = parser.parseCommand(
        "edit event location Meeting from 2025-11-01T10:00 with Room101");

    assertTrue(command instanceof EditEventCommand);
  }

  @Test
  public void testParseCopyEventSubjectWithoutQuotes() {
    Command command = parser.parseCommand(
        "copy event Meeting on 2025-11-01T10:00 --target Personal to 2025-11-02T14:00");

    assertTrue(command instanceof controller.commands.CopyEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarEmptyValue() {
    parser.parseCommand("edit calendar --name Work --property timezone ");
  }

  @Test
  public void testParseEditCalendarValidValue() {
    Command command = parser.parseCommand(
        "edit calendar --name Work --property timezone America/Chicago");

    assertTrue(command instanceof controller.commands.EditCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarWhitespaceOnlyValue() {
    parser.parseCommand("edit calendar --name Work --property name    ");
  }
}