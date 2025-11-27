package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to export calendar to iCal format.
 */
public class ExportIcalCommand implements Command {
  private final String filename;

  /**
   * this is command to export calendar.
   */
  public ExportIcalCommand(String filename) {
    this.filename = filename;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.exportCalendarToIcal(filename);
  }
}