package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to edit events from a specific date forward in a series.
 */
public class EditEventsCommand implements Command {
  private final String property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;

  /**
   *  Command to edit events from a specific date forward in a series.
   */
  public EditEventsCommand(String property, String subject, LocalDateTime startDateTime,
                           String newValue) {
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.editEventsFromDate(subject, startDateTime, property, newValue);
  }
}