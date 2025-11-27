import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import model.CalendarManager;
import model.CalendarModel;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarManager.
 */
public class CalendarManagerTest {
  private CalendarManager manager;

  /**
   * Set up test fixtures.
   */
  @Before
  public void setUp() {
    manager = new CalendarManager();
  }

  // ========== Basic Calendar Creation Tests ==========

  @Test
  public void testCreateCalendar() {
    manager.createCalendar("Work", "America/New_York");
    assertTrue(manager.hasCalendar("Work"));
    assertEquals(1, manager.getCalendarCount());
  }

  @Test
  public void testCreateCalendarWithDifferentTimezone() {
    manager.createCalendar("Personal", "Europe/London");
    assertTrue(manager.hasCalendar("Personal"));
    CalendarModel calendar = manager.getCalendar("Personal");
    assertEquals("Europe/London", calendar.getTimezone());
  }

  @Test
  public void testCreateCalendarNullName() {
    assertThrows(NullPointerException.class, () -> {
      manager.createCalendar(null, "America/New_York");
    });
  }

  @Test
  public void testCreateCalendarNullTimezone() {
    assertThrows(NullPointerException.class, () -> {
      manager.createCalendar("Work", null);
    });
  }

  @Test
  public void testCreateCalendarEmptyName() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.createCalendar("", "America/New_York");
    });
  }

  @Test
  public void testCreateCalendarWhitespaceName() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.createCalendar("   ", "America/New_York");
    });
  }

  @Test
  public void testCreateCalendarDuplicateName() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.createCalendar("Work", "Europe/London");
    });
  }

  @Test
  public void testCreateCalendarInvalidTimezone() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.createCalendar("Work", "Invalid/Timezone");
    });
  }

  // ========== Get Calendar Tests ==========

  @Test
  public void testGetCalendar() {
    manager.createCalendar("Work", "America/New_York");
    CalendarModel calendar = manager.getCalendar("Work");
    assertNotNull(calendar);
    assertEquals("Work", calendar.getName());
  }

  @Test
  public void testGetCalendarNotFound() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.getCalendar("NonExistent");
    });
  }

  // ========== Use Calendar Tests ==========

  @Test
  public void testUseCalendar() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");
    assertEquals("Work", manager.getCurrentCalendarName());
  }

  @Test
  public void testUseCalendarNotFound() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.useCalendar("NonExistent");
    });
  }

  @Test
  public void testGetCurrentCalendarNoSelection() {
    assertThrows(IllegalStateException.class, () -> {
      manager.getCurrentCalendar();
    });
  }

  @Test
  public void testGetCurrentCalendar() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");
    CalendarModel current = manager.getCurrentCalendar();
    assertNotNull(current);
    assertEquals("Work", current.getName());
  }

  @Test
  public void testGetCurrentCalendarName() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");
    assertEquals("Work", manager.getCurrentCalendarName());
  }

  @Test
  public void testGetCurrentCalendarNameNoSelection() {
    assertNull(manager.getCurrentCalendarName());
  }

  // ========== Edit Calendar Name Tests ==========

  @Test
  public void testEditCalendarName() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "name", "WorkCalendar");
    
    assertFalse(manager.hasCalendar("Work"));
    assertTrue(manager.hasCalendar("WorkCalendar"));
    assertEquals("WorkCalendar", manager.getCalendar("WorkCalendar").getName());
  }

  @Test
  public void testEditCalendarNameUpdatesCurrentCalendar() {
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");
    manager.editCalendar("Work", "name", "WorkCalendar");
    
    assertEquals("WorkCalendar", manager.getCurrentCalendarName());
  }

  @Test
  public void testEditCalendarNameNullNewName() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(NullPointerException.class, () -> {
      manager.editCalendar("Work", "name", null);
    });
  }

  @Test
  public void testEditCalendarNameEmptyNewName() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "name", "");
    });
  }

  @Test
  public void testEditCalendarNameWhitespaceNewName() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "name", "   ");
    });
  }

  @Test
  public void testEditCalendarNameDuplicateName() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "name", "Personal");
    });
  }

  @Test
  public void testEditCalendarNameNotCurrentCalendar() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/New_York");
    manager.useCalendar("Personal");
    
    manager.editCalendar("Work", "name", "WorkCalendar");
    
    // Current calendar should remain unchanged
    assertEquals("Personal", manager.getCurrentCalendarName());
    assertTrue(manager.hasCalendar("WorkCalendar"));
  }

  // ========== Edit Calendar Timezone Tests ==========

  @Test
  public void testEditCalendarTimezone() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "timezone", "Europe/London");
    
    CalendarModel calendar = manager.getCalendar("Work");
    assertEquals("Europe/London", calendar.getTimezone());
  }

  @Test
  public void testEditCalendarTimezoneNullTimezone() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(NullPointerException.class, () -> {
      manager.editCalendar("Work", "timezone", null);
    });
  }

  @Test
  public void testEditCalendarTimezoneInvalidTimezone() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "timezone", "Invalid/Timezone");
    });
  }

  // ========== Edit Calendar Invalid Property Tests ==========

  @Test
  public void testEditCalendarInvalidProperty() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "invalid", "value");
    });
  }

  @Test
  public void testEditCalendarInvalidPropertyMixedCase() {
    manager.createCalendar("Work", "America/New_York");
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("Work", "InvalidProperty", "value");
    });
  }

  @Test
  public void testEditCalendarNotFound() {
    assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("NonExistent", "name", "NewName");
    });
  }

  // ========== Calendar Names Tests ==========

  @Test
  public void testGetCalendarNamesEmpty() {
    Set<String> names = manager.getCalendarNames();
    assertNotNull(names);
    assertTrue(names.isEmpty());
  }

  @Test
  public void testGetCalendarNamesSingle() {
    manager.createCalendar("Work", "America/New_York");
    Set<String> names = manager.getCalendarNames();
    assertEquals(1, names.size());
    assertTrue(names.contains("Work"));
  }

  @Test
  public void testGetCalendarNamesMultiple() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    manager.createCalendar("Family", "Asia/Tokyo");
    
    Set<String> names = manager.getCalendarNames();
    assertEquals(3, names.size());
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("Personal"));
    assertTrue(names.contains("Family"));
  }

  // ========== Has Calendar Tests ==========

  @Test
  public void testHasCalendarTrue() {
    manager.createCalendar("Work", "America/New_York");
    assertTrue(manager.hasCalendar("Work"));
  }

  @Test
  public void testHasCalendarFalse() {
    assertFalse(manager.hasCalendar("NonExistent"));
  }

  @Test
  public void testHasCalendarAfterEdit() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "name", "WorkCalendar");
    
    assertFalse(manager.hasCalendar("Work"));
    assertTrue(manager.hasCalendar("WorkCalendar"));
  }

  // ========== Calendar Count Tests ==========

  @Test
  public void testGetCalendarCountZero() {
    assertEquals(0, manager.getCalendarCount());
  }

  @Test
  public void testGetCalendarCountOne() {
    manager.createCalendar("Work", "America/New_York");
    assertEquals(1, manager.getCalendarCount());
  }

  @Test
  public void testGetCalendarCountMultiple() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    manager.createCalendar("Family", "Asia/Tokyo");
    assertEquals(3, manager.getCalendarCount());
  }

  @Test
  public void testGetCalendarCountAfterEdit() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "name", "WorkCalendar");
    assertEquals(1, manager.getCalendarCount());
  }

  // ========== Property Case Sensitivity Tests ==========

  @Test
  public void testEditCalendarPropertyNameLowerCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "name", "WorkCalendar");
    assertTrue(manager.hasCalendar("WorkCalendar"));
  }

  @Test
  public void testEditCalendarPropertyNameUpperCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "NAME", "WorkCalendar");
    assertTrue(manager.hasCalendar("WorkCalendar"));
  }

  @Test
  public void testEditCalendarPropertyNameMixedCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "NaMe", "WorkCalendar");
    assertTrue(manager.hasCalendar("WorkCalendar"));
  }

  @Test
  public void testEditCalendarPropertyTimezoneLowerCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "timezone", "Europe/London");
    assertEquals("Europe/London", manager.getCalendar("Work").getTimezone());
  }

  @Test
  public void testEditCalendarPropertyTimezoneUpperCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "TIMEZONE", "Europe/London");
    assertEquals("Europe/London", manager.getCalendar("Work").getTimezone());
  }

  @Test
  public void testEditCalendarPropertyTimezoneMixedCase() {
    manager.createCalendar("Work", "America/New_York");
    manager.editCalendar("Work", "TimeZone", "Europe/London");
    assertEquals("Europe/London", manager.getCalendar("Work").getTimezone());
  }


  @Test
  public void testMultipleCalendarsWithSwitching() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    
    manager.useCalendar("Work");
    assertEquals("Work", manager.getCurrentCalendarName());
    
    manager.useCalendar("Personal");
    assertEquals("Personal", manager.getCurrentCalendarName());
  }

  @Test
  public void testEditCalendarNameWithMultipleCalendars() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    manager.useCalendar("Work");
    
    manager.editCalendar("Work", "name", "Office");
    
    assertEquals("Office", manager.getCurrentCalendarName());
    assertTrue(manager.hasCalendar("Office"));
    assertTrue(manager.hasCalendar("Personal"));
    assertEquals(2, manager.getCalendarCount());
  }

  @Test
  public void testEditCalendarTimezoneWithMultipleCalendars() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    
    manager.editCalendar("Work", "timezone", "Asia/Tokyo");
    
    assertEquals("Asia/Tokyo", manager.getCalendar("Work").getTimezone());
    assertEquals("Europe/London", manager.getCalendar("Personal").getTimezone());
  }
}

