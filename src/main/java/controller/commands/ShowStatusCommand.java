package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to show busy/available status at a specific time.
 */
public class ShowStatusCommand implements Command {
  private final LocalDateTime dateTime;

  /**
   * This is for showing the status of an event.
   *
   * @param dateTime specifies the date time.
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.showStatus(dateTime);
  }
}