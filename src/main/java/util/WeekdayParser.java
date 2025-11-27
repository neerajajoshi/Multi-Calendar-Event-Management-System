package util;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for parsing weekday strings into DayOfWeek sets.
 * Design rationale:
 * - Pure utility class with static methods
 * - No state, thread-safe
 * - Single responsibility: weekday parsing
 * - Easy to test and reuse
 */
public final class WeekdayParser {
  

  private WeekdayParser() {
    throw new AssertionError("Utility class should not be instantiated");
  }
  
  /**
   * Parses a weekday string (e.g., "MRU") into a set of DayOfWeek objects.
   * M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday.
   *
   * @param weekdayString string representation of weekdays
   * @return set of corresponding DayOfWeek objects
   * @throws IllegalArgumentException if any character is invalid
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdayString) {
    if (weekdayString == null || weekdayString.isEmpty()) {
      throw new IllegalArgumentException("Weekday string cannot be null or empty");
    }
    
    Set<DayOfWeek> days = new HashSet<>();
    
    for (char c : weekdayString.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }
    
    return days;
  }

  /**
   * Converts a set of DayOfWeek objects back to the weekday string format.
   *
   *
   * @param days set of DayOfWeek objects
   * @return string representation
   */
  public static String formatWeekdays(Set<DayOfWeek> days) {
    if (days == null || days.isEmpty()) {
      return "";
    }
    
    StringBuilder sb = new StringBuilder();
    
    if (days.contains(DayOfWeek.MONDAY)) {
      sb.append('M');
    }
    if (days.contains(DayOfWeek.TUESDAY)) {
      sb.append('T');
    }
    if (days.contains(DayOfWeek.WEDNESDAY)) {
      sb.append('W');
    }
    if (days.contains(DayOfWeek.THURSDAY)) {
      sb.append('R');
    }
    if (days.contains(DayOfWeek.FRIDAY)) {
      sb.append('F');
    }
    if (days.contains(DayOfWeek.SATURDAY)) {
      sb.append('S');
    }
    if (days.contains(DayOfWeek.SUNDAY)) {
      sb.append('U');
    }
    
    return sb.toString();
  }
}