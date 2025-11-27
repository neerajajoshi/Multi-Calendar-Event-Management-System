package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.CalendarEvent;


/**
 * Interface defining the view layer for calendar operations.
 * Design rationale:
 * - Abstracts presentation logic from business logic
 * - Supports multiple implementations (console, GUI, web, etc.)
 * - Follows dependency inversion principle
 * - Makes testing easier with mock implementations
 * - Allows for different output formats without changing controller
 */
public interface CalendarView {
  
  /**
   * Displays a general message to the user.
   */
  void showMessage(String message);
  
  /**
   * Displays an error message to the user.
   */
  void showError(String error);
  
  /**
   * Displays events occurring on a specific date.
   */
  void showEventsOnDate(LocalDate date, List<CalendarEvent> events);
  
  /**
   * Displays events in a date/time range.
   */
  void showEventsInRange(LocalDateTime start, LocalDateTime end, List<CalendarEvent> events);
  
  /**
   * Displays busy/available status.
   */
  void showStatus(boolean isBusy);
  
  /**
   * Exports events to a file and shows the result.
   */
  void exportToFile(String filename, List<CalendarEvent> events);
}