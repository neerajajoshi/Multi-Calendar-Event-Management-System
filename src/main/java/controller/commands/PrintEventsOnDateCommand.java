package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDate;

/**
 * Command to print events on a specific date.
 */
public class PrintEventsOnDateCommand implements Command {
  private final LocalDate date;

  /**
   * this is for printing the events on particular date.
   *
   * @param date specifies the date.
   */
  public PrintEventsOnDateCommand(LocalDate date) {
    this.date = date;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.showEventsOnDate(date);
  }
}