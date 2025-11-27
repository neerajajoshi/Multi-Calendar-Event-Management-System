import static org.junit.Assert.assertEquals;

import model.EventStatus;
import org.junit.Test;

/**
 * Test class for EventStatus enum.
 */
public class EventStatusTest {

  @Test
  public void testFromString() {
    assertEquals(EventStatus.PUBLIC, EventStatus.fromString("public"));
    assertEquals(EventStatus.PUBLIC, EventStatus.fromString("PUBLIC"));
    assertEquals(EventStatus.PRIVATE, EventStatus.fromString("private"));
    assertEquals(EventStatus.PRIVATE, EventStatus.fromString("PRIVATE"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromStringInvalid() {
    EventStatus.fromString("invalid");
  }

  @Test
  public void testGetValue() {
    assertEquals("public", EventStatus.PUBLIC.getValue());
    assertEquals("private", EventStatus.PRIVATE.getValue());
  }

  @Test
  public void testToString() {
    assertEquals("public", EventStatus.PUBLIC.toString());
    assertEquals("private", EventStatus.PRIVATE.toString());
  }
}