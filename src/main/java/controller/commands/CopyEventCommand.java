package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to copy a single event to another calendar.
 */
public class CopyEventCommand implements Command {
  private final String eventSubject;
  private final LocalDateTime sourceDateTime;
  private final String targetCalendarName;
  private final LocalDateTime targetDateTime;

  /**
   * This is copy event command.
   *
   * @param eventSubject specifies subject of the event
   * @param sourceDateTime specifies source date time.
   * @param targetCalendarName specifies the target calendar name.
   * @param targetDateTime specifies target date time.
   */
  public CopyEventCommand(String eventSubject, LocalDateTime sourceDateTime, 
                         String targetCalendarName, LocalDateTime targetDateTime) {
    this.eventSubject = eventSubject;
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.copyEvent(eventSubject, sourceDateTime, targetCalendarName, targetDateTime);
  }
}