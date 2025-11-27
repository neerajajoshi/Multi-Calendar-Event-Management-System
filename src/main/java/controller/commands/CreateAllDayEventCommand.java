package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDate;

/**
 * Command to create a single all-day event.
 */
public class CreateAllDayEventCommand implements Command {
  private final String subject;
  private final LocalDate date;

  /**
   * This is an all day event command.
   */
  public CreateAllDayEventCommand(String subject, LocalDate date) {
    this.subject = subject;
    this.date = date;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.createAllDayEvent(subject, date);
  }
}