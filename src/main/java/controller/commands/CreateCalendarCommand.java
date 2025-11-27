package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to create a new calendar.
 */
public class CreateCalendarCommand implements Command {
  private final String name;
  private final String timezone;

  /**
   * This is command to create calendar.
   *
   * @param name specifies the name of the calendar.
   * @param timezone specifies the timezone of the calendar.
   */
  public CreateCalendarCommand(String name, String timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.createCalendar(name, timezone);
  }
}