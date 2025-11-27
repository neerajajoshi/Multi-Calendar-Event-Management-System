package controller.commands;

import controller.CalendarController;
import controller.Command;
import model.CalendarEventSeries;

/**
 * Command to create an event series.
 */
public class CreateSeriesCommand implements Command {
  private final CalendarEventSeries series;

  /**
   * This is command for creating a series.
   */
  public CreateSeriesCommand(CalendarEventSeries series) {
    this.series = series;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.createEventSeries(series);
  }
}