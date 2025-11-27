package controller.commands;

import controller.CalendarController;
import controller.Command;
import model.CalendarEventSeries;

/**
 * Command to create an all-day event series.
 */
public class CreateAllDaySeriesCommand implements Command {
  private final CalendarEventSeries series;

  /**
   * this is an all day series command.
   */
  public CreateAllDaySeriesCommand(CalendarEventSeries series) {
    this.series = series;
  }

  @Override
  public void execute(CalendarController controller) {
    controller.createEventSeries(series);
  }
}