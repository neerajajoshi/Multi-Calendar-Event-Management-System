package view;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.CalendarEvent;


/**
 * Console-based implementation of the CalendarView interface.
 * Design rationale:
 * - Implements the view interface for console output
 * - Handles all formatting and presentation logic
 * - Can be easily replaced with GUI or web view
 * - Separates I/O concerns from business logic
 * - Uses standard output streams for testability
 */
public class ConsoleCalendarView implements CalendarView {
  
  private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern(
      "MM/dd/yyyy");
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

  @Override
  public void showMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void showError(String error) {
    System.err.println("Error: " + error);
  }

  @Override
  public void showEventsOnDate(LocalDate date, List<CalendarEvent> events) {
    if (events.isEmpty()) {
      showMessage("No events on " + date);
      return;
    }

    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(date).append(":\n");
    
    for (CalendarEvent event : events) {
      result.append("- ").append(event.getSubject())
            .append(" from ").append(event.getStartDateTime().toLocalTime())
            .append(" to ").append(event.getEndDateTime() != null
            ?
                event.getEndDateTime().toLocalTime() : event.getStartDateTime().toLocalTime());
      
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(" at ").append(event.getLocation());
      }
      result.append("\n");
    }
    
    showMessage(result.toString().trim());
  }

  @Override
  public void showEventsInRange(LocalDateTime start, LocalDateTime end, List<CalendarEvent>
      events) {
    if (events.isEmpty()) {
      showMessage("No events in range " + start + " to " + end);
      return;
    }

    StringBuilder result = new StringBuilder();
    result.append("Events from ").append(start).append(" to ").append(end).append(":\n");
    
    for (CalendarEvent event : events) {
      result.append("- ").append(event.toString());
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(" at ").append(event.getLocation());
      }
      result.append("\n");
    }
    
    showMessage(result.toString().trim());
  }

  @Override
  public void showStatus(boolean isBusy) {
    showMessage(isBusy ? "busy" : "available");
  }

  @Override
  public void exportToFile(String filename, List<CalendarEvent> events) {
    try {
      String absolutePath = Paths.get(filename).toAbsolutePath().toString();
      
      try (FileWriter writer = new FileWriter(filename)) {
        writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
            + "Description,Location,Status\n");
        
        for (CalendarEvent event : events) {
          writeEventToCsv(writer, event);
        }
      }
      
      showMessage("Calendar exported to: " + absolutePath);
      
    } catch (IOException e) {
      showError("Error exporting calendar: " + e.getMessage());
    }
  }

  /**
   * Writes a single event to CSV format.
   * Design rationale: Private helper method keeps the export logic encapsulated
   * and makes the main export method more readable.
   */
  private void writeEventToCsv(FileWriter writer, CalendarEvent event) throws IOException {
    StringBuilder line = new StringBuilder();
    
    // Subject
    line.append("\"").append(event.getSubject()).append("\",");
    
    // Start Date
    line.append(event.getStartDateTime().format(CSV_DATE_FORMAT)).append(",");
    
    // Start Time
    if (event.isAllDay()) {
      line.append(",");
    } else {
      line.append(event.getStartDateTime().format(CSV_TIME_FORMAT)).append(",");
    }
    
    // End Date
    if (event.getEndDateTime() != null) {
      line.append(event.getEndDateTime().format(CSV_DATE_FORMAT)).append(",");
    } else {
      line.append(event.getStartDateTime().format(CSV_DATE_FORMAT)).append(",");
    }
    
    // End Time
    if (event.isAllDay()) {
      line.append(",");
    } else if (event.getEndDateTime() != null) {
      line.append(event.getEndDateTime().format(CSV_TIME_FORMAT)).append(",");
    } else {
      line.append(event.getStartDateTime().format(CSV_TIME_FORMAT)).append(",");
    }
    
    // All Day Event
    line.append(event.isAllDay() ? "True" : "False").append(",");
    
    // Description
    String description = event.getDescription() != null ? event.getDescription() : "";
    line.append("\"").append(description).append("\",");
    
    // Location
    String location = event.getLocation() != null ? event.getLocation() : "";
    line.append("\"").append(location).append("\",");
    
    // Status
    line.append(event.getStatus().getValue());
    
    line.append("\n");
    writer.write(line.toString());
  }
}