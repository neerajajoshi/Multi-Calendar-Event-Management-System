package controller;

import controller.commands.CopyEventCommand;
import controller.commands.CopyEventsInRangeCommand;
import controller.commands.CopyEventsOnDateCommand;
import controller.commands.CreateAllDayEventCommand;
import controller.commands.CreateAllDaySeriesCommand;
import controller.commands.CreateCalendarCommand;
import controller.commands.CreateEventCommand;
import controller.commands.CreateSeriesCommand;
import controller.commands.EditCalendarCommand;
import controller.commands.EditEventCommand;
import controller.commands.EditEventsCommand;
import controller.commands.EditSeriesCommand;
import controller.commands.ExitCommand;
import controller.commands.ExportCommand;
import controller.commands.ExportIcalCommand;
import controller.commands.ListCalendarsCommand;
import controller.commands.PrintEventsInRangeCommand;
import controller.commands.PrintEventsOnDateCommand;
import controller.commands.ShowStatusCommand;
import controller.commands.UseCalendarCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.CalendarEventSeries;
import util.WeekdayParser;


/**
 * Parses user input into Command objects.
 * Design rationale:
 * - Separates parsing logic from command execution
 * - Returns Command objects that can be executed later
 * - Supports command validation and preprocessing
 * - Easy to extend with new command types
 * - Centralized parsing logic for consistency
 */
public class CommandParser {
  
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Parses a command string and returns a Command object.
   *
   * @param input the command string
   * @return the parsed command
   * @throws IllegalArgumentException if the command is invalid
   */
  public Command parseCommand(String input) {
    if (input == null || input.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be empty");
    }

    String command = input.trim();
    
    // Exit command
    if (command.equalsIgnoreCase("exit")) {
      return new ExitCommand();
    }

    // Create calendar command
    if (command.startsWith("create calendar")) {
      return parseCreateCalendarCommand(command);
    }

    // Create event commands
    if (command.startsWith("create event")) {
      return parseCreateCommand(command);
    }

    // Print commands
    if (command.startsWith("print events")) {
      return parsePrintCommand(command);
    }

    // Status command
    if (command.startsWith("show status")) {
      return parseStatusCommand(command);
    }

    // Edit calendar command
    if (command.startsWith("edit calendar")) {
      return parseEditCalendarCommand(command);
    }

    // Edit event commands
    if (command.startsWith("edit event") || command.startsWith("edit events")
        || command.startsWith("edit series")) {
      return parseEditCommand(command);
    }

    // Use calendar command
    if (command.startsWith("use calendar")) {
      return parseUseCalendarCommand(command);
    }
    
    // List calendars command
    if (command.equalsIgnoreCase("list calendars") || command.equalsIgnoreCase("show calendars")) {
      return new ListCalendarsCommand();
    }

    // Export commands
    if (command.startsWith("export cal")) {
      return parseExportCommand(command);
    }

    // Copy commands
    if (command.startsWith("copy events between")) {
      return parseCopyEventsInRangeCommand(command);
    }
    
    if (command.startsWith("copy events on")) {
      return parseCopyEventsOnDateCommand(command);
    }
    
    if (command.startsWith("copy event")) {
      return parseCopyEventCommand(command);
    }

    throw new IllegalArgumentException("Invalid command: " + command);
  }

  private Command parseCreateCommand(String command) {
    String remainder = command.substring(12).trim();
    

    String subject;
    String rest;
    if (remainder.startsWith("\"")) {
      int endQuote = remainder.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in subject");
      }
      subject = remainder.substring(1, endQuote);
      rest = remainder.substring(endQuote + 1).trim();
    } else {
      String[] parts = remainder.split("\\s+", 2);
      subject = parts[0];
      rest = parts.length > 1 ? parts[1] : "";
    }

    if (rest.startsWith("from")) {
      return parseCreateFromTo(subject, rest);
    } else if (rest.startsWith("on")) {
      return parseCreateOn(subject, rest);
    }

    throw new IllegalArgumentException("Invalid create command format");
  }

  private Command parseCreateFromTo(String subject, String rest) {
    // Pattern: from <datetime> to <datetime> [repeats <weekdays> for <N> times | until <date>]
    Pattern pattern = Pattern.compile("from (\\S+) to (\\S+)(?:\\s+repeats (\\S+) "
        + "(?:for (\\d+) times|until (\\S+)))?");
    Matcher matcher = pattern.matcher(rest);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid from-to format");
    }

    LocalDateTime startDateTime = parseDateTime(matcher.group(1));
    LocalDateTime endDateTime = parseDateTime(matcher.group(2));
    
    if (matcher.group(3) == null) {
      // Single event
      return new CreateEventCommand(subject, startDateTime, endDateTime);
    } else {
      // Recurring event - create series command
      Set<DayOfWeek> weekdays = WeekdayParser.parseWeekdays(matcher.group(3));
      if (matcher.group(4) != null) {
        // For N times
        int occurrences = Integer.parseInt(matcher.group(4));
        CalendarEventSeries series = new CalendarEventSeries(subject, startDateTime, endDateTime,
            weekdays, occurrences);
        return new CreateSeriesCommand(series);
      } else {
        // Until date
        LocalDate endDate = LocalDate.parse(matcher.group(5), DATE_FORMAT);
        CalendarEventSeries series = new CalendarEventSeries(subject, startDateTime, endDateTime,
            weekdays, endDate);
        return new CreateSeriesCommand(series);
      }
    }
  }

  private Command parseCreateOn(String subject, String rest) {
    // Pattern: on <date> [repeats <weekdays> for <N> times | until <date>]
    Pattern pattern = Pattern.compile("on (\\S+)(?:\\s+repeats (\\S+) "
        + "(?:for (\\d+) times|until (\\S+)))?");
    Matcher matcher = pattern.matcher(rest);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid on format");
    }

    LocalDate date = LocalDate.parse(matcher.group(1), DATE_FORMAT);
    
    if (matcher.group(2) == null) {
      // Single all-day event
      return new CreateAllDayEventCommand(subject, date);
    } else {
      // Recurring all-day event
      Set<DayOfWeek> weekdays = WeekdayParser.parseWeekdays(matcher.group(2));
      if (matcher.group(3) != null) {
        // For N times
        int occurrences = Integer.parseInt(matcher.group(3));
        CalendarEventSeries series = new CalendarEventSeries(subject, date, weekdays, occurrences,
            null);
        return new CreateAllDaySeriesCommand(series);
      } else {
        // Until date
        LocalDate endDate = LocalDate.parse(matcher.group(4), DATE_FORMAT);
        CalendarEventSeries series = new CalendarEventSeries(subject, date, weekdays, -1, endDate);
        return new CreateAllDaySeriesCommand(series);
      }
    }
  }

  private Command parsePrintCommand(String command) {
    if (command.startsWith("print events on ")) {
      String dateStr = command.substring(16);
      LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
      return new PrintEventsOnDateCommand(date);
    } else if (command.startsWith("print events from ")) {
      String remainder = command.substring(18);
      String[] parts = remainder.split(" to ");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid print events range format");
      }
      LocalDateTime start = parseDateTime(parts[0]);
      LocalDateTime end = parseDateTime(parts[1]);
      return new PrintEventsInRangeCommand(start, end);
    }
    
    throw new IllegalArgumentException("Invalid print command format");
  }

  private Command parseStatusCommand(String command) {
    // Pattern: show status on <datetime>
    if (!command.startsWith("show status on ")) {
      throw new IllegalArgumentException("Invalid status command format");
    }
    
    String datetimeStr = command.substring(15);
    LocalDateTime dateTime = parseDateTime(datetimeStr);
    return new ShowStatusCommand(dateTime);
  }

  private Command parseEditCommand(String command) {
    String editType;
    String remainder;
    if (command.startsWith("edit event ")) {
      editType = "single";
      remainder = command.substring(11);
    } else if (command.startsWith("edit events ")) {
      editType = "from_date";
      remainder = command.substring(12);
    } else {
      editType = "all_series";
      remainder = command.substring(12);
    }

    // Pattern: <property> <subject> from <datetime> with <newValue>
    Pattern pattern = Pattern.compile("(\\w+) (.+?) from (\\S+) with (.+)");
    Matcher matcher = pattern.matcher(remainder);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid edit command format");
    }

    String property = matcher.group(1);
    String subject = matcher.group(2);

    if (subject.startsWith("\"") && subject.endsWith("\"")) {
      subject = subject.substring(1, subject.length() - 1);
    }
    
    LocalDateTime startDateTime = parseDateTime(matcher.group(3));
    String newValue = matcher.group(4);

    if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
      newValue = newValue.substring(1, newValue.length() - 1);
    }

    switch (editType) {
      case "single":
        return new EditEventCommand(property, subject, startDateTime, newValue);
      case "from_date":
        return new EditEventsCommand(property, subject, startDateTime, newValue);
      case "all_series":
        return new EditSeriesCommand(property, subject, startDateTime, newValue);
      default:
        throw new IllegalArgumentException("Unknown edit type");
    }
  }

  private Command parseExportCommand(String command) {
    if (!command.startsWith("export cal ")) {
      throw new IllegalArgumentException("Invalid export command format");
    }
    
    String filename = command.substring(11);

    if (filename.toLowerCase().endsWith(".ical") || filename.toLowerCase().endsWith(".ics")) {
      return new ExportIcalCommand(filename);
    } else {
      return new ExportCommand(filename);
    }
  }
  
  private Command parseCopyEventCommand(String command) {

    Pattern pattern = Pattern.compile("copy event (.+?) on (\\S+)"
        + " --target (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy event format."
          + "Expected: copy event <eventName>"
          + "on <dateStringTtimeString> --target <calendarName> to <dateStringTtimeString>");
    }
    
    String subject = matcher.group(1);

    if (subject.startsWith("\"") && subject.endsWith("\"")) {
      subject = subject.substring(1, subject.length() - 1);
    }
    
    LocalDateTime sourceDateTime = parseDateTime(matcher.group(2));
    String targetCalendar = matcher.group(3);
    LocalDateTime targetDateTime = parseDateTime(matcher.group(4));
    
    return new CopyEventCommand(subject, sourceDateTime, targetCalendar, targetDateTime);
  }
  
  private Command parseCopyEventsOnDateCommand(String command) {
    // Pattern: copy events on <dateString> --target <calendarName> to <dateString>
    Pattern pattern = Pattern.compile("copy events on (\\S+) --target (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy events on date format. "
          + "Expected: copy events on <dateString> --target <calendarName> to <dateString>");
    }
    
    LocalDate sourceDate = LocalDate.parse(matcher.group(1), DATE_FORMAT);
    String targetCalendar = matcher.group(2);
    LocalDate targetDate = LocalDate.parse(matcher.group(3), DATE_FORMAT);
    
    return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
  }
  
  private Command parseCopyEventsInRangeCommand(String command) {

    Pattern pattern = Pattern.compile("copy events between (\\S+) and (\\S+) "
        + "--target (\\S+) to (\\S+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid copy events range format.Expected: copy events"
          + " between <dateString> and <dateString> --target <calendarName> to <dateString>");
    }
    
    LocalDate startDate = LocalDate.parse(matcher.group(1), DATE_FORMAT);
    LocalDate endDate = LocalDate.parse(matcher.group(2), DATE_FORMAT);
    String targetCalendar = matcher.group(3);
    LocalDate targetStartDate = LocalDate.parse(matcher.group(4), DATE_FORMAT);
    
    return new CopyEventsInRangeCommand(startDate, endDate, targetCalendar, targetStartDate);
  }

  private Command parseCreateCalendarCommand(String command) {
    // Pattern: create calendar --name <name> --timezone <timezone>
    Pattern pattern = Pattern.compile("create calendar --name (\\S+) --timezone (\\S+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException(util.ValidationUtils.createCommandErrorMessage(
          command, "create calendar --name <name> --timezone <timezone>"));
    }
    
    String name = matcher.group(1);
    String timezone = matcher.group(2);
    
    // Validate inputs
    util.ValidationUtils.validateCalendarName(name);
    util.ValidationUtils.validateTimezone(timezone);
    
    return new CreateCalendarCommand(name, timezone);
  }
  
  private Command parseEditCalendarCommand(String command) {
    // Pattern: edit calendar --name <name> --property <property> <value>
    Pattern pattern = Pattern.compile("edit calendar --name (\\S+) --property (\\S+) (.+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException(util.ValidationUtils.createCommandErrorMessage(
          command, "edit calendar --name <name> --property <property> <value>"));
    }
    
    String name = matcher.group(1);
    String property = matcher.group(2);
    String value = matcher.group(3);
    
    // Validate inputs
    util.ValidationUtils.validateCalendarName(name);
    util.ValidationUtils.validateCalendarProperty(property);
    util.ValidationUtils.validateNotEmpty(value, "Property value");
    
    return new EditCalendarCommand(name, property, value);
  }
  
  private Command parseUseCalendarCommand(String command) {
    // Pattern: use calendar --name <name>
    Pattern pattern = Pattern.compile("use calendar --name (\\S+)");
    Matcher matcher = pattern.matcher(command);
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid use calendar format. "
          + "Expected: use calendar --name <name>");
    }
    
    String name = matcher.group(1);
    return new UseCalendarCommand(name);
  }

  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(util.ValidationUtils.createDateTimeErrorMessage(
          dateTimeStr, "YYYY-MM-DDTHH:mm"));
    }
  }
}