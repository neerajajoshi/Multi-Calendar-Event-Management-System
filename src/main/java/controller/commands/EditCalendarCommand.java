package controller.commands;

import controller.CalendarController;
import controller.Command;

/**
 * Command to edit a calendar property.
 */
public class EditCalendarCommand implements Command {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * This is edit calendar command.
   *
   * @param calendarName specifies the name of the calendar.
   * @param property specifies the property to be edited.
   * @param newValue specifies the new value to be updated.
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.editCalendar(calendarName, property, newValue);
  }
}