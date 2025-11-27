package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to print events in a date/time range.
 */
public class PrintEventsInRangeCommand implements Command {
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * this is for printing commands in range.
   *
   * @param start specifies the staring.
   * @param end specifies the end.
   */
  public PrintEventsInRangeCommand(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.showEventsInRange(start, end);
  }
}