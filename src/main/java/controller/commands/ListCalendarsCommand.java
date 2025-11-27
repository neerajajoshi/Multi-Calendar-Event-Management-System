package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to list all available calendars.
 */
public class ListCalendarsCommand implements Command {

  /**
   * this is calendar command list.
   */
  public ListCalendarsCommand() {
    // No parameters needed
  }

  @Override
  public void execute(CalendarController controller) {
    controller.listCalendars();
  }
}
