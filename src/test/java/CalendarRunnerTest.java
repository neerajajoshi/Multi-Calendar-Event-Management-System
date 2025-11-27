import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarRunner.
 */
public class CalendarRunnerTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  /**
   * Exception to catch System.exit() calls.
   */
  private static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("System.exit() called");
      this.status = status;
    }
  }

  /**
   * Security manager that prevents System.exit().
   */
  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(java.security.Permission perm) {
      // Allow everything
    }

    @Override
    public void checkPermission(java.security.Permission perm, Object context) {
      // Allow everything
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }

  /**
   * Set up output stream redirection.
   */
  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    System.setSecurityManager(new NoExitSecurityManager());
  }

  /**
   * Restore original streams.
   */
  @After
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
    System.setSecurityManager(null);
  }

  /*
  @Test
  public void testInteractiveModeExit() {
    String[] args = {"--mode", "interactive"};
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar Application - Interactive Mode"));
    assertTrue(output.contains("Goodbye"));
  }
  */

  /*
  @Test
  public void testInteractiveModeWithCommands() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar Application - Interactive Mode"));
    assertTrue(output.contains("Event created"));
  }
  */

  /*
  @Test
  public void testInteractiveModeInvalidCommand() {
    String[] args = {"--mode", "interactive"};
    String input = "invalid command\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String error = errContent.toString();
    assertTrue(error.contains("Error"));
  }
  */

  /*
  @Test
  public void testHeadlessModeValidFile() throws IOException {
    // Create temporary command file
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeWithComments() throws IOException {
    // Create temporary command file with comments
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("# This is a comment\n");
      writer.write("\n"); // Empty line
      writer.write("create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("# Another comment\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeWithInvalidCommand() throws IOException {
    // Create temporary command file with invalid command
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("invalid command\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String error = errContent.toString();
    assertTrue(error.contains("Error executing command"));

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeMultipleCommands() throws IOException {

    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Meeting1 from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("create event Meeting2 from 2025-11-02T14:00 to 2025-11-02T15:00\n");
      writer.write("create event Holiday on 2025-11-11\n");
      writer.write("print events on 2025-11-01\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testInteractiveModeCaseInsensitive() {
    String[] args = {"--mode", "INTERACTIVE"};
    String input = "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar Application - Interactive Mode"));
  }
  */

  /*
  @Test
  public void testHeadlessModeCaseInsensitive() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("exit\n");
    }

    String[] args = {"--mode", "HEADLESS", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeExitInMiddle() throws IOException {

    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("exit\n");
      writer.write("create event ShouldNotExecute from 2025-11-02T10:00 to 2025-11-02T11:00\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testInteractiveModeMultipleEvents() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Meeting1 from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "create event Meeting2 from 2025-11-02T14:00 to 2025-11-02T15:00\n"
        + "print events on 2025-11-01\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Event created"));
    assertTrue(output.contains("Meeting1"));
  }
  */

  /*
  @Test
  public void testHeadlessModeCreateSeries() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Weekly from 2025-11-03T10:00 to 2025-11-03T11:00 ");
      writer.write("repeats MW for 3 times\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testInteractiveModeWithEmptyLines() {
    String[] args = {"--mode", "interactive"};
    String input = "\n\ncreate event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar Application - Interactive Mode"));
  }
  */

  /*
  @Test
  public void testInteractiveModeMultipleInvalidCommands() {
    String[] args = {"--mode", "interactive"};
    String input = "invalid1\ninvalid2\ninvalid3\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String error = errContent.toString();
    // Should have multiple error messages
    int errorCount = error.split("Error:").length - 1;
    assertTrue(errorCount >= 3);
  }
  */

  /*
  @Test
  public void testInteractiveModeCreateAndPrint() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "print events on 2025-11-01\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Event created"));
    assertTrue(output.contains("Test"));
  }

  @Test
  public void testInteractiveModeCreateAllDay() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Holiday on 2025-11-01\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("All-day event created"));
  }

  @Test
  public void testInteractiveModeShowStatus() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "show status on 2025-11-01T10:30\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("busy"));
  }

  @Test
  public void testInteractiveModeExport() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "export cal test_interactive.csv\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Calendar exported"));

    new File("test_interactive.csv").delete();
  }
  */

  /*
  @Test
  public void testHeadlessModeAllDayEvent() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Holiday on 2025-12-25\n");
      writer.write("print events on 2025-12-25\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeShowStatus() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("show status on 2025-11-01T10:30\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeExport() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("export cal test_headless.csv\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));

    new File("test_headless.csv").delete();
  }
  */

  /*
  @Test
  public void testHeadlessModeOnlyComments() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("# Comment 1\n");
      writer.write("# Comment 2\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeOnlyEmptyLines() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("\n\n\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeMultipleInvalidCommands() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("invalid command 1\n");
      writer.write("invalid command 2\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String error = errContent.toString();
    assertTrue(error.contains("Error executing command"));

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeEditEvent() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("edit event subject Meeting from 2025-11-01T10:00 with \"Important\"\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModePrintRange() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("print events from 2025-11-01T00:00 to 2025-11-30T23:59\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testInteractiveModeEditEvent() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Meeting from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "edit event location Meeting from 2025-11-01T10:00 with \"Room 101\"\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Event created"));
  }

  @Test
  public void testInteractiveModePrintRange() {
    String[] args = {"--mode", "interactive"};
    String input = "create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Event created"));
    assertTrue(output.contains("Test"));
  }
  */


  /*
  @Test
  public void testHeadlessModeWhitespaceLines() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("   \n");  // Whitespace only
      writer.write("\t\n");   // Tab only
      writer.write("create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testHeadlessModeCommentInMiddle() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Test1 from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("# This is a comment in the middle\n");
      writer.write("create event Test2 from 2025-11-02T10:00 to 2025-11-02T11:00\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */

  /*
  @Test
  public void testInteractiveModeQuotedEventName() {
    String[] args = {"--mode", "interactive"};
    String input = "create event \"Team Meeting\" from 2025-11-01T10:00 to 2025-11-01T11:00\n"
        + "exit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Event created"));
  }
  */

  /*
  @Test
  public void testHeadlessModeQuotedEventName() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event \"Team Meeting\" from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("exit\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarRunner.main(args);

    String output = outContent.toString();
    assertTrue(output.contains("Commands executed successfully"));
  }
  */


  /*
  @Test
  public void testNoArguments() {
    String[] args = {};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Usage: java CalendarRunner --mode"));
    assertTrue(error.contains("<interactive|headless> [filename]"));
  }
  */

  /*
  @Test
  public void testFirstArgumentNotMode() {
    String[] args = {"--invalid", "interactive"};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("First argument must be --mode"));
  }
  */

  /*
  @Test
  public void testModeNotSpecified() {
    String[] args = {"--mode"};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Mode must be specified"));
  }
  */

  /*
  @Test
  public void testInvalidMode() {
    String[] args = {"--mode", "invalid"};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Invalid mode: invalid"));
    assertTrue(error.contains("Use 'interactive' or 'headless'"));
  }
  */

  /*
  @Test
  public void testHeadlessModeNoFilename() {
    String[] args = {"--mode", "headless"};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Headless mode requires a command file"));
  }
  */

  /*
  @Test
  public void testHeadlessModeFileNotFound() {
    String[] args = {"--mode", "headless", "nonexistent_file.txt"};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Error reading command file"));
  }
  */

  /*
  @Test
  public void testHeadlessModeNoExitCommand() throws IOException {
    File tempFile = File.createTempFile("calendar_test", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("create event Test from 2025-11-01T10:00 to 2025-11-01T11:00\n");
      writer.write("print events on 2025-11-01\n");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    
    try {
      CalendarRunner.main(args);
    } catch (ExitException e) {
      // Expected - System.exit() was called
      assertTrue(e.status == 1);
    }

    String error = errContent.toString();
    assertTrue(error.contains("Error: Command file must end with 'exit' command"));
  }
  */
}