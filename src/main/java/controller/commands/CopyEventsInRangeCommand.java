package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDate;

/**
 * Command to copy all events in a date range to another calendar.
 */
public class CopyEventsInRangeCommand implements Command {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  /**
   * this is copy events in range command.
   *
   * @param startDate specifies the start date.
   * @param endDate specifies the end date.
   * @param targetCalendarName specifies target calendar name.
   * @param targetStartDate specifies target start date.
   */
  public CopyEventsInRangeCommand(LocalDate startDate, LocalDate endDate, 
                                 String targetCalendarName, LocalDate targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.copyEventsInRange(startDate, endDate, targetCalendarName, targetStartDate);
  }
}