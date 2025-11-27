package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDate;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCommand implements Command {
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  /**
   * This is copy event on date command.
   */
  public CopyEventsOnDateCommand(LocalDate sourceDate, String targetCalendarName,
                                 LocalDate targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.copyEventsOnDate(sourceDate, targetCalendarName, targetDate);
  }
}