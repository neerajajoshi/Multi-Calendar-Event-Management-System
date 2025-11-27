import controller.CalendarController;
import controller.Command;
import controller.CommandParser;
import controller.GuiCalendarController;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import model.CalendarManager;
import view.CalendarView;
import view.ConsoleCalendarView;
import view.GuiCalendarView;

/**
 * Main application runner implementing proper MVC architecture.
 * Design rationale:
 * - Separates concerns: Model, View, Controller
 * - Dependency injection for testability
 * - Supports multiple I/O modes (interactive, headless)
 * - Easy to extend with new view implementations (GUI, web, etc.)
 * - Command pattern for extensible operations
 */
public class CalendarRunner {

  /**
   * Entry point for the Calendar application.
   *
   * @param args command line arguments specifying mode and optional filename
   */
  public static void main(String[] args) {
    // No arguments - launch GUI mode
    if (args.length == 0) {
      runGuiMode();
      return;
    }

    // Validate command-line arguments for text modes
    if (!args[0].equalsIgnoreCase("--mode")) {
      System.err.println("Invalid arguments. Valid usage:");
      System.err.println("  java CalendarRunner                              (GUI mode)");
      System.err.println("  java CalendarRunner --mode interactive           (text mode)");
      System.err.println("  java CalendarRunner --mode headless <filename>   (script mode)");
      System.exit(1);
    }

    if (args.length < 2) {
      System.err.println("Mode must be specified after --mode");
      System.exit(1);
    }

    // Create MVC components for text modes
    CalendarManager calendarManager = new CalendarManager();
    CalendarView view = new ConsoleCalendarView();
    CalendarController controller = new CalendarController(calendarManager, view);
    CommandParser parser = new CommandParser();

    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        runInteractiveMode(controller, parser);
        break;
      case "headless":
        if (args.length < 3) {
          System.err.println("Headless mode requires a command file");
          System.exit(1);
        }
        runHeadlessMode(controller, parser, args[2]);
        break;
      default:
        System.err.println("Invalid mode: " + mode + ". Use 'interactive' or 'headless'");
        System.exit(1);
    }
  }

  /**
   * Runs the application in interactive mode.
   * Design rationale: Separates I/O handling from business logic.
   * The controller handles all business operations.
   */
  private static void runInteractiveMode(CalendarController controller, CommandParser parser) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Calendar Application - Interactive Mode");
    System.out.println("Type 'exit' to quit");

    while (true) {
      System.out.print("> ");
      String input = scanner.nextLine();

      try {
        Command command = parser.parseCommand(input);

        // Check for exit command
        if (command instanceof controller.commands.ExitCommand) {
          command.execute(controller);
          break;
        }

        command.execute(controller);
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
      }
    }

    scanner.close();
  }

  /**
   * Runs the application in headless mode.
   * Design rationale: Same command processing as interactive mode,
   * just different input source. Demonstrates flexibility of the design.
   */
  private static void runHeadlessMode(CalendarController controller,
                                      CommandParser parser, String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      boolean exitFound = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue; // Skip empty lines and comments
        }

        try {
          Command command = parser.parseCommand(line);

          // Check for exit command
          if (command instanceof controller.commands.ExitCommand) {
            exitFound = true;
            System.out.println("Commands executed successfully");
            break;
          }

          command.execute(controller);
        } catch (Exception e) {
          System.err.println("Error executing command '" + line + "': "
              + e.getMessage());

        }
      }

      if (!exitFound) {
        System.err.println("Error: Command file must end with 'exit' command");
        System.exit(1);
      }

    } catch (IOException e) {
      System.err.println("Error reading command file: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Runs the application in GUI mode.
   * Design rationale: Launches Swing GUI on the Event Dispatch Thread.
   * Uses separate GUI controller to handle GUI-specific interactions.
   */
  private static void runGuiMode() {
    SwingUtilities.invokeLater(() -> {
      CalendarManager calendarManager = new CalendarManager();
      GuiCalendarView view = new GuiCalendarView();
      GuiCalendarController controller = new GuiCalendarController(calendarManager, view);
      controller.show();
    });
  }
}
