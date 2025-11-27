package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to create a single event.
 * Design rationale:
 * - Encapsulates event creation parameters
 * - Immutable command object
 * - Easy to test in isolation
 * - Can be extended for validation or preprocessing
 */
public class CreateEventCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * This is command for creating an event.
   */
  public CreateEventCommand(String subject, LocalDateTime startDateTime,
                            LocalDateTime endDateTime) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.createEvent(subject, startDateTime, endDateTime);
  }
}