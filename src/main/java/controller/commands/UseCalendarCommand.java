package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to set the current calendar context.
 */
public class UseCalendarCommand implements Command {
  private final String calendarName;

  /**
   * This is for using the calendar.
   *
   * @param calendarName specifies the calendar name.
   */
  public UseCalendarCommand(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.useCalendar(calendarName);
  }
}