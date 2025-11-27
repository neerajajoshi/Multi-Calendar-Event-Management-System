package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to exit the application.
 */
public class ExitCommand implements Command {
  
  @Override
  public void execute(CalendarController controller) {
    controller.exit();
  }
}