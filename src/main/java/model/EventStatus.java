package model;

/**
 * Enumeration representing the privacy status of an event.
 * Design rationale: Using enum provides type safety and prevents invalid values.
 */
public enum EventStatus {
  PUBLIC("public"),
  PRIVATE("private");

  private final String value;

  EventStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Parses a string to EventStatus.
   *
   * @param status string representation
   * @return corresponding EventStatus
   * @throws IllegalArgumentException if status is invalid
   */
  public static EventStatus fromString(String status) {
    for (EventStatus eventStatus : EventStatus.values()) {
      if (eventStatus.value.equalsIgnoreCase(status)) {
        return eventStatus;
      }
    }
    throw new IllegalArgumentException("Invalid status: " + status);
  }

  @Override
  public String toString() {
    return value;
  }
}