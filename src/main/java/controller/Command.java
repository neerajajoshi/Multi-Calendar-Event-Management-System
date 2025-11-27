package controller;

/**
 * Command interface following the Command pattern.
 * Design rationale:
 * - Encapsulates requests as objects
 * - Supports undo/redo operations (future enhancement)
 * - Allows for command queuing and logging
 * - Makes adding new commands easy (Open/Closed Principle)
 * - Decouples command invoker from command executor
 */
public interface Command {
  
  /**
   * Executes the command.
   *
   * @param controller the controller to execute the command on
   */
  void execute(CalendarController controller);
}