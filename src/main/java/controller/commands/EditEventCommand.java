package controller.commands;

import controller.CalendarController;
import controller.Command;
import java.time.LocalDateTime;

/**
 * Command to edit a single event property.
 */
public class EditEventCommand implements Command {
  private final String property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;

  /**
   * Command for editing the event.
   *
   * @param property specifies the property to be changed.
   * @param subject specifies the subject for an event.
   * @param startDateTime specifies the start date and time of the event.
   * @param newValue specifies the new value to be edited.
   */
  public EditEventCommand(String property, String subject, LocalDateTime startDateTime,
                          String newValue) {
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.editEvent(subject, startDateTime, property, newValue);
  }
}