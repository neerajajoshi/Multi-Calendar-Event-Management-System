package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to export calendar to a file.
 */
public class ExportCommand implements Command {
  private final String filename;

  /**
   * Command to export the file with .ical extension.
   *
   * @param filename specifies the file name.
   */
  public ExportCommand(String filename) {
    this.filename = filename;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.exportCalendar(filename);
  }
}