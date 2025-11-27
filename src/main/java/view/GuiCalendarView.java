package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import model.CalendarEvent;

/**
 * Swing-based GUI implementation of CalendarView.
 * Design rationale:
 * - Implements CalendarView interface for MVC compliance
 * - Month view as primary display
 * - User-friendly interactions (dropdowns, pickers, buttons)
 * - Graceful error handling with dialogs
 * - Supports multiple calendars with visual distinction
 */
public class GuiCalendarView extends JFrame implements CalendarView {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  
  private JPanel monthViewPanel;
  private JLabel monthYearLabel;
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private JTextArea eventDisplayArea;
  private JLabel statusLabel;
  
  // Callback interface for GUI actions
  private GuiActionListener actionListener;
  
  // Cache for dates with events
  private java.util.Set<LocalDate> cachedEventDates;
  
  /**
   * Creates the GUI calendar view.
   */
  public GuiCalendarView() {
    super("Calendar Application");
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();
    this.cachedEventDates = new java.util.HashSet<>();
    
    initializeUserInterface();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 700);
    setLocationRelativeTo(null);
  }
  
  /**
   * Sets the action listener for GUI events.
   */
  public void setActionListener(GuiActionListener listener) {
    this.actionListener = listener;
  }
  
  /**
   * Initializes the user interface components.
   */
  private void initializeUserInterface() {
    setLayout(new BorderLayout(10, 10));
    
    // Top panel with calendar selector and navigation
    add(createTopPanel(), BorderLayout.NORTH);
    
    // Center panel with month view
    add(createCenterPanel(), BorderLayout.CENTER);
    
    // Right panel with event details and actions
    add(createRightPanel(), BorderLayout.EAST);
    
    // Bottom status bar
    add(createBottomPanel(), BorderLayout.SOUTH);
    
    // Initial month view
    updateMonthView();
  }
  
  /**
   * Creates the top panel with calendar management and navigation.
   */
  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(5, 5));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
    
    // Calendar management panel
    JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton createCalendarBtn = new JButton("New Calendar");
    JButton selectCalendarBtn = new JButton("Select Calendar");
    
    createCalendarBtn.addActionListener(e -> showCreateCalendarDialog());
    selectCalendarBtn.addActionListener(e -> showSelectCalendarDialog());
    
    calendarPanel.add(new JLabel("Calendar:"));
    calendarPanel.add(createCalendarBtn);
    calendarPanel.add(selectCalendarBtn);
    
    // Month navigation panel
    JButton prevMonthBtn = new JButton("◀ Previous");
    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));
    
    prevMonthBtn.addActionListener(e -> navigateMonth(-1));
    
    JButton todayBtn = new JButton("Today");
    todayBtn.addActionListener(e -> navigateToToday());
    
    JButton nextMonthBtn = new JButton("Next ▶");
    nextMonthBtn.addActionListener(e -> navigateMonth(1));
    
    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    navPanel.add(prevMonthBtn);
    navPanel.add(monthYearLabel);
    navPanel.add(nextMonthBtn);
    navPanel.add(todayBtn);
    
    topPanel.add(calendarPanel, BorderLayout.WEST);
    topPanel.add(navPanel, BorderLayout.CENTER);
    
    return topPanel;
  }
  
  /**
   * Creates the center panel with month view grid.
   */
  private JPanel createCenterPanel() {
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
    
    monthViewPanel = new JPanel(new GridLayout(0, 7, 5, 5));
    monthViewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    
    centerPanel.add(monthViewPanel, BorderLayout.CENTER);
    
    return centerPanel;
  }
  
  /**
   * Creates the right panel with event details and actions.
   */
  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
    rightPanel.setPreferredSize(new Dimension(300, 0));
    
    // Selected date label
    JLabel selectedDateLabel = new JLabel("Selected Date: " + selectedDate.format(DATE_FORMATTER));
    selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 12));
    
    // Event display area
    eventDisplayArea = new JTextArea();
    eventDisplayArea.setEditable(false);
    eventDisplayArea.setLineWrap(true);
    eventDisplayArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(eventDisplayArea);
    scrollPane.setBorder(BorderFactory.createTitledBorder("Events on Selected Day"));
    
    // Action buttons
    JButton createEventBtn = new JButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog());
    
    JButton editEventBtn = new JButton("Edit Event");
    editEventBtn.addActionListener(e -> showEditEventDialog());
    
    JButton deleteEventBtn = new JButton("Delete Event");
    deleteEventBtn.addActionListener(e -> showDeleteEventDialog());
    
    JButton copyEventBtn = new JButton("Copy Event");
    copyEventBtn.addActionListener(e -> showCopyEventDialog());
    
    JButton viewEventsBtn = new JButton("Refresh Events");
    viewEventsBtn.addActionListener(e -> refreshSelectedDateEvents());
    
    JPanel actionPanel = new JPanel(new GridLayout(5, 1, 5, 5));
    actionPanel.add(createEventBtn);
    actionPanel.add(editEventBtn);
    actionPanel.add(deleteEventBtn);
    actionPanel.add(copyEventBtn);
    actionPanel.add(viewEventsBtn);
    
    rightPanel.add(selectedDateLabel, BorderLayout.NORTH);
    rightPanel.add(scrollPane, BorderLayout.CENTER);
    rightPanel.add(actionPanel, BorderLayout.SOUTH);
    
    return rightPanel;
  }
  
  /**
   * Creates the bottom status bar.
   */
  private JPanel createBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
    
    statusLabel = new JLabel("Ready");
    statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    
    bottomPanel.add(statusLabel, BorderLayout.WEST);
    
    return bottomPanel;
  }
  
  /**
   * Updates the month view grid.
   */
  private void updateMonthView() {
    monthViewPanel.removeAll();
    
    // Update month/year label
    monthYearLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
    
    // Add day headers
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String dayName : dayNames) {
      JLabel headerLabel = new JLabel(dayName, SwingConstants.CENTER);
      headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
      headerLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      headerLabel.setOpaque(true);
      headerLabel.setBackground(new Color(230, 230, 230));
      monthViewPanel.add(headerLabel);
    }
    
    // Get first day of month and number of days
    LocalDate firstOfMonth = currentMonth.atDay(1);
    int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
    int daysInMonth = currentMonth.lengthOfMonth();
    
    // Add empty cells before first day
    for (int i = 0; i < firstDayOfWeek; i++) {
      monthViewPanel.add(new JLabel(""));
    }
    
    // Add day buttons
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = createDayButton(date);
      monthViewPanel.add(dayButton);
    }
    
    monthViewPanel.revalidate();
    monthViewPanel.repaint();
  }
  
  /**
   * Creates a button for a day in the month view.
   */
  private JButton createDayButton(LocalDate date) {
    JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
    button.setPreferredSize(new Dimension(60, 60));

    boolean hasEvents = hasEventsOnDate(date);

    if (date.equals(LocalDate.now())) {
      button.setBackground(new Color(173, 216, 230)); // Light blue
      button.setOpaque(true);
    } else if (hasEvents) {
      button.setBackground(new Color(200, 255, 200)); // Light green
      button.setOpaque(true);
    }

    if (date.equals(selectedDate)) {
      button.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
    }

    if (hasEvents) {
      button.setText("<html><center>" + date.getDayOfMonth() 
          + "<br><font color='green'>●</font></center></html>");
    }
    
    button.addActionListener(e -> selectDate(date));
    
    return button;
  }
  
  /**
   * Checks if a date has any events.
   */
  private boolean hasEventsOnDate(LocalDate date) {
    if (actionListener == null) {
      return false;
    }
    
    try {

      return cachedEventDates.contains(date);
    } catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Updates the cache of dates with events for the current month.
   */
  public void updateEventCacheForMonth(List<CalendarEvent> allEvents) {
    cachedEventDates.clear();
    
    if (allEvents != null) {
      for (CalendarEvent event : allEvents) {
        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        // Only cache dates in the current month view
        if (eventDate.getYear() == currentMonth.getYear() 
            && eventDate.getMonth() == currentMonth.getMonth()) {
          cachedEventDates.add(eventDate);
        }
      }
    }
    
    // Refresh the month view to show indicators
    updateMonthView();
  }
  
  /**
   * Navigates to a different month.
   */
  private void navigateMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    updateMonthView();
    // Request event cache update from controller
    if (actionListener != null) {
      actionListener.onMonthChanged(currentMonth);
    }
  }
  
  /**
   * Navigates to today's month.
   */
  private void navigateToToday() {
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    updateMonthView();
    refreshSelectedDateEvents();
  }
  
  /**
   * Selects a date and displays its events.
   */
  private void selectDate(LocalDate date) {
    selectedDate = date;
    updateMonthView();
    refreshSelectedDateEvents();
    updateStatusLabel("Selected: " + date.format(DATE_FORMATTER));
  }
  
  /**
   * Refreshes events for the selected date.
   */
  private void refreshSelectedDateEvents() {
    if (actionListener != null) {
      actionListener.onViewEventsOnDate(selectedDate);
    }
  }
  
  /**
   * Shows dialog to create a new calendar.
   */
  private void showCreateCalendarDialog() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
    JTextField nameField = new JTextField(20);
    JComboBox<String> timezoneCombo = new JComboBox<>(getCommonTimezones());
    timezoneCombo.setEditable(true);
    
    panel.add(new JLabel("Calendar Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(timezoneCombo);
    
    int result = JOptionPane.showConfirmDialog(this, panel, "Create New Calendar",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      String timezone = (String) timezoneCombo.getSelectedItem();
      
      if (name.isEmpty()) {
        showError("Calendar name cannot be empty");
        return;
      }
      
      if (actionListener != null) {
        actionListener.onCreateCalendar(name, timezone);
      }
    }
  }
  
  /**
   * Shows dialog to select a calendar.
   */
  private void showSelectCalendarDialog() {
    if (actionListener != null) {
      actionListener.onRequestCalendarList();
    }
  }
  
  /**
   * Shows the calendar selection dialog with available calendars.
   */
  public void showCalendarSelectionDialog(List<String> calendarNames, String currentCalendar) {
    if (calendarNames.isEmpty()) {
      showMessage("No calendars available. Please create one first.");
      return;
    }
    
    String[] calendars = calendarNames.toArray(new String[0]);
    String selected = (String) JOptionPane.showInputDialog(this,
        "Select a calendar:",
        "Calendar Selection",
        JOptionPane.PLAIN_MESSAGE,
        null,
        calendars,
        currentCalendar);
    
    if (selected != null && actionListener != null) {
      actionListener.onSelectCalendar(selected);
    }
  }
  
  /**
   * Shows dialog to create a new event.
   */
  private void showCreateEventDialog() {
    JPanel panel = new JPanel(new GridLayout(10, 2, 5, 5));
    
    JTextField subjectField = new JTextField(20);
    JTextField descriptionField = new JTextField(20);
    JTextField locationField = new JTextField(20);
    JTextField startTimeField = new JTextField("09:00");
    JTextField endTimeField = new JTextField("10:00");
    JCheckBox allDayCheck = new JCheckBox();
    JCheckBox recurringCheck = new JCheckBox();
    
    panel.add(new JLabel("Event Subject:*"));
    panel.add(subjectField);
    panel.add(new JLabel("Description:"));
    panel.add(descriptionField);
    panel.add(new JLabel("Location:"));
    panel.add(locationField);
    panel.add(new JLabel("Start Time (HH:mm):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End Time (HH:mm):"));
    panel.add(endTimeField);
    panel.add(new JLabel("All Day Event:"));
    panel.add(allDayCheck);
    panel.add(new JLabel("Recurring Event:"));
    panel.add(recurringCheck);

    allDayCheck.addActionListener(e -> {
      boolean allDay = allDayCheck.isSelected();
      startTimeField.setEnabled(!allDay);
      endTimeField.setEnabled(!allDay);
    });
    
    int result = JOptionPane.showConfirmDialog(this, panel, "Create Event on " 
        + selectedDate.format(DATE_FORMATTER), JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
      String subject = subjectField.getText().trim();
      String description = descriptionField.getText().trim();
      String location = locationField.getText().trim();
      boolean isAllDay = allDayCheck.isSelected();
      boolean isRecurring = recurringCheck.isSelected();
      
      if (subject.isEmpty()) {
        showError("Event subject cannot be empty");
        return;
      }
      
      if (isRecurring) {
        showCreateRecurringEventDialog(subject, description, location, isAllDay, 
            startTimeField.getText(), endTimeField.getText());
      } else {
        createSingleEvent(subject, description, location, isAllDay, 
            startTimeField.getText(), endTimeField.getText());
      }
    }
  }
  
  /**
   * Creates a single event.
   */
  private void createSingleEvent(String subject, String description, String location, 
      boolean isAllDay, String startTime, String endTime) {
    try {
      if (isAllDay) {
        if (actionListener != null) {
          actionListener.onCreateAllDayEvent(subject, description, location, selectedDate);
        }
      } else {
        LocalDateTime startDateTime = LocalDateTime.of(selectedDate, 
            java.time.LocalTime.parse(startTime, TIME_FORMATTER));
        LocalDateTime endDateTime = LocalDateTime.of(selectedDate, 
            java.time.LocalTime.parse(endTime, TIME_FORMATTER));
        
        if (actionListener != null) {
          actionListener.onCreateEvent(subject, description, location, 
              startDateTime, endDateTime);
        }
      }
    } catch (Exception e) {
      showError("Invalid time format. Use HH:mm (e.g., 09:00)");
    }
  }
  
  /**
   * Shows dialog for creating recurring events.
   */
  private void showCreateRecurringEventDialog(String subject, String description, 
      String location, boolean isAllDay, String startTime, String endTime) {
    // Weekday selection
    JPanel weekdayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    weekdayPanel.setBorder(BorderFactory.createTitledBorder("Repeat on weekdays:"));
    JCheckBox[] weekdayBoxes = new JCheckBox[7];
    String[] weekdayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) {
      weekdayBoxes[i] = new JCheckBox(weekdayNames[i]);
      weekdayPanel.add(weekdayBoxes[i]);
    }
    
    // Frequency type
    JRadioButton countRadio = new JRadioButton("Number of weeks", true);
    JRadioButton endDateRadio = new JRadioButton("Until end date");
    ButtonGroup freqGroup = new ButtonGroup();
    freqGroup.add(countRadio);
    freqGroup.add(endDateRadio);
    
    JTextField countField = new JTextField("4", 10);
    JTextField endDateField = new JTextField(LocalDate.now().plusMonths(1)
        .format(DATE_FORMATTER), 10);
    
    countRadio.addActionListener(e -> {
      countField.setEnabled(true);
      endDateField.setEnabled(false);
    });
    endDateRadio.addActionListener(e -> {
      countField.setEnabled(false);
      endDateField.setEnabled(true);
    });
    endDateField.setEnabled(false);
    
    JPanel freqPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    freqPanel.setBorder(BorderFactory.createTitledBorder("Recurrence frequency:"));
    freqPanel.add(countRadio);
    freqPanel.add(countField);
    freqPanel.add(endDateRadio);
    freqPanel.add(endDateField);
    
    JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    topPanel.add(weekdayPanel);
    topPanel.add(freqPanel);
    
    JLabel helpLabel = new JLabel("<html><i>Note: 'Number of weeks' creates events for " 
        + "selected weekdays over that many weeks.<br>Example: Selecting Mon/Wed with 4 weeks " 
        + "creates 8 events (4 Mondays + 4 Wednesdays).</i></html>");
    
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.add(topPanel, BorderLayout.CENTER);
    mainPanel.add(helpLabel, BorderLayout.SOUTH);
    
    int result = JOptionPane.showConfirmDialog(this, mainPanel, "Recurring Event Settings",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
      // Build weekday string
      StringBuilder weekdays = new StringBuilder();
      String[] weekdayChars = {"M", "T", "W", "R", "F", "S", "U"};
      for (int i = 0; i < 7; i++) {
        if (weekdayBoxes[i].isSelected()) {
          weekdays.append(weekdayChars[i]);
        }
      }
      
      if (weekdays.length() == 0) {
        showError("Please select at least one weekday");
        return;
      }
      
      try {
        if (actionListener != null) {
          if (countRadio.isSelected()) {
            int weeks = Integer.parseInt(countField.getText().trim());
            if (weeks <= 0) {
              showError("Number of weeks must be positive");
              return;
            }
            actionListener.onCreateRecurringEvent(subject, description, location, 
                selectedDate, isAllDay, startTime, endTime, weekdays.toString(), weeks, null);
          } else {
            LocalDate endDate = LocalDate.parse(endDateField.getText().trim(), DATE_FORMATTER);
            if (endDate.isBefore(selectedDate)) {
              showError("End date must be after start date");
              return;
            }
            actionListener.onCreateRecurringEvent(subject, description, location, 
                selectedDate, isAllDay, startTime, endTime, weekdays.toString(), -1, endDate);
          }
        }
      } catch (NumberFormatException e) {
        showError("Invalid number format");
      } catch (Exception e) {
        showError("Invalid input: " + e.getMessage());
      }
    }
  }
  
  /**
   * Shows dialog to edit events.
   */
  private void showEditEventDialog() {
    if (actionListener != null) {
      actionListener.onRequestEventsForEdit(selectedDate);
    }
  }
  
  /**
   * Shows the event editing dialog with available events.
   */
  public void showEventEditDialog(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      showMessage("No events on this date to edit");
      return;
    }

    String[] eventNames = events.stream()
        .map(e -> e.getSubject() + " at " + e.getStartDateTime().toLocalTime())
        .toArray(String[]::new);
    
    String selected = (String) JOptionPane.showInputDialog(this,
        "Select an event to edit:",
        "Edit Event",
        JOptionPane.PLAIN_MESSAGE,
        null,
        eventNames,
        eventNames[0]);
    
    if (selected == null) {
      return;
    }
    
    // Find the selected event
    int index = java.util.Arrays.asList(eventNames).indexOf(selected);
    CalendarEvent event = events.get(index);
    
    // Show edit options
    showEventEditOptionsDialog(event);
  }
  
  /**
   * Shows edit options for a specific event.
   */
  private void showEventEditOptionsDialog(CalendarEvent event) {
    JTextField subjectField = new JTextField(event.getSubject(), 20);
    
    // Time fields
    String startTimeStr = event.isAllDay() ? "08:00" 
        : event.getStartDateTime().toLocalTime().format(TIME_FORMATTER);
    String endTimeStr = event.isAllDay() ? "17:00" 
        : event.getEndDateTime().toLocalTime().format(TIME_FORMATTER);
    
    JTextField startTimeField = new JTextField(startTimeStr, 10);
    JTextField endTimeField = new JTextField(endTimeStr, 10);

    if (event.isAllDay()) {
      startTimeField.setEnabled(false);
      endTimeField.setEnabled(false);
    }
    
    JTextField descField = new JTextField(event.getDescription() != null 
        ? event.getDescription() : "", 20);
    
    JTextField locationField = new JTextField(event.getLocation() != null 
        ? event.getLocation() : "", 20);
    
    String[] editScopes = {"This event only", "This and future events", "All events in series"};
    JComboBox<String> scopeCombo = new JComboBox<>(editScopes);
    
    JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
    panel.add(new JLabel("Subject:"));
    panel.add(subjectField);
    panel.add(new JLabel("Description:"));
    panel.add(descField);
    panel.add(new JLabel("Location:"));
    panel.add(locationField);
    panel.add(new JLabel("Start Time (HH:mm):"));
    panel.add(startTimeField);
    panel.add(new JLabel("End Time (HH:mm):"));
    panel.add(endTimeField);
    panel.add(new JLabel("Edit scope:"));
    panel.add(scopeCombo);
    
    int result = JOptionPane.showConfirmDialog(this, panel, "Edit Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
      String newSubject = subjectField.getText().trim();
      String newDesc = descField.getText().trim();
      String newLocation = locationField.getText().trim();
      String newStartTime = startTimeField.getText().trim();
      String newEndTime = endTimeField.getText().trim();
      int scope = scopeCombo.getSelectedIndex();
      
      if (actionListener != null) {
        actionListener.onEditEvent(event, newSubject, newDesc, newLocation, 
            newStartTime, newEndTime, scope);
      }
    }
  }
  
  /**
   * Shows the delete event dialog.
   */
  private void showDeleteEventDialog() {
    if (actionListener != null) {
      actionListener.onRequestEventsForDelete(selectedDate);
    }
  }

  /**
   * Shows the event deletion dialog with available events.
   */
  public void showEventDeleteDialog(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      showMessage("No events on this date to delete");
      return;
    }

    String[] eventNames = events.stream()
        .map(e -> e.getSubject() + " at " + e.getStartDateTime().toLocalTime())
        .toArray(String[]::new);

    String selected = (String) JOptionPane.showInputDialog(this,
        "Select an event to delete:",
        "Delete Event",
        JOptionPane.PLAIN_MESSAGE,
        null,
        eventNames,
        eventNames[0]);

    if (selected == null) {
      return;
    }

    // Find the selected event
    int index = java.util.Arrays.asList(eventNames).indexOf(selected);
    CalendarEvent event = events.get(index);

    // Show delete options
    showEventDeleteOptionsDialog(event);
  }

  /**
   * Shows delete options for a specific event.
   */
  private void showEventDeleteOptionsDialog(CalendarEvent event) {
    String[] deleteScopes = {"This event only", "This and future events", "All events in series"};
    JComboBox<String> scopeCombo = new JComboBox<>(deleteScopes);
    
    JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
    panel.add(new JLabel("Delete: " + event.getSubject()));
    panel.add(new JLabel("Delete scope:"));
    panel.add(scopeCombo);

    int result = JOptionPane.showConfirmDialog(this, panel, "Delete Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      int scope = scopeCombo.getSelectedIndex();
      if (actionListener != null) {
        actionListener.onDeleteEvent(event, scope);
      }
    }
  }

  /**
   * Shows the copy event dialog.
   */
  private void showCopyEventDialog() {
    if (actionListener != null) {
      actionListener.onRequestEventsForCopy(selectedDate);
    }
  }

  /**
   * Shows the event copy dialog with available events.
   */
  public void showEventCopyDialog(List<CalendarEvent> events, List<String> availableCalendars,
      String currentCalendar, String currentTimezone, model.CalendarManager calendarManager) {
    if (events.isEmpty()) {
      showMessage("No events on this date to copy");
      return;
    }

    if (availableCalendars.size() <= 1) {
      showMessage("No other calendars available to copy to. Create another calendar first.");
      return;
    }

    String[] eventNames = events.stream()
        .map(e -> e.getSubject() + " at " + e.getStartDateTime().toLocalTime())
        .toArray(String[]::new);

    String selected = (String) JOptionPane.showInputDialog(this,
        "Select an event to copy:",
        "Copy Event",
        JOptionPane.PLAIN_MESSAGE,
        null,
        eventNames,
        eventNames[0]);

    if (selected == null) {
      return;
    }

    // Find the selected event
    int index = java.util.Arrays.asList(eventNames).indexOf(selected);
    CalendarEvent event = events.get(index);

    // Show copy options
    showEventCopyOptionsDialog(event, availableCalendars, currentCalendar, currentTimezone,
        calendarManager);
  }

  /**
   * Shows copy options for a specific event.
   */
  private void showEventCopyOptionsDialog(CalendarEvent event, List<String> availableCalendars,
      String currentCalendar, String currentTimezone, model.CalendarManager calendarManager) {
    // Filter out current calendar
    List<String> targetCalendars = availableCalendars.stream()
        .filter(cal -> !cal.equals(currentCalendar))
        .collect(java.util.stream.Collectors.toList());

    if (targetCalendars.isEmpty()) {
      showMessage("No other calendars available to copy to");
      return;
    }

    JComboBox<String> calendarCombo = new JComboBox<>(
        targetCalendars.toArray(new String[0]));

    JTextField targetDateField = new JTextField(
        event.getStartDateTime().toLocalDate().format(DATE_FORMATTER), 10);
    JTextField targetTimeField = new JTextField(
        event.getStartDateTime().toLocalTime().format(TIME_FORMATTER), 10);
    JLabel timezoneInfoLabel = new JLabel("");

    calendarCombo.addActionListener(e -> {
      String selectedCalendar = (String) calendarCombo.getSelectedItem();
      if (selectedCalendar != null && !event.isAllDay()) {
        try {
          model.CalendarModel targetModel = calendarManager.getCalendar(selectedCalendar);
          String targetTimezone = targetModel.getTimezone();

          LocalDateTime convertedTime = util.TimezoneConverter.convertTimezone(
              event.getStartDateTime(), currentTimezone, targetTimezone);
          
          targetDateField.setText(convertedTime.toLocalDate().format(DATE_FORMATTER));
          targetTimeField.setText(convertedTime.toLocalTime().format(TIME_FORMATTER));
          timezoneInfoLabel.setText("(" + currentTimezone + " → " + targetTimezone + ")");
        } catch (Exception ex) {
          // Keep original time if conversion fails
        }
      }
    });

    // Trigger initial conversion
    calendarCombo.setSelectedIndex(0);

    if (event.isAllDay()) {
      targetTimeField.setEnabled(false);
    }

    JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
    panel.add(new JLabel("Event:"));
    panel.add(new JLabel(event.getSubject()));
    panel.add(new JLabel("Target Calendar:"));
    panel.add(calendarCombo);
    panel.add(new JLabel("Target Date (yyyy-MM-dd):"));
    panel.add(targetDateField);
    panel.add(new JLabel("Target Time (HH:mm):"));
    panel.add(targetTimeField);
    panel.add(new JLabel("Timezone Conversion:"));
    panel.add(timezoneInfoLabel);

    int result = JOptionPane.showConfirmDialog(this, panel, "Copy Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String targetCalendar = (String) calendarCombo.getSelectedItem();
      String targetDateStr = targetDateField.getText().trim();
      String targetTimeStr = targetTimeField.getText().trim();

      try {
        LocalDate targetDate = LocalDate.parse(targetDateStr, DATE_FORMATTER);
        LocalTime targetTime = event.isAllDay() ? LocalTime.of(8, 0)
            : LocalTime.parse(targetTimeStr, TIME_FORMATTER);
        LocalDateTime targetDateTime = LocalDateTime.of(targetDate, targetTime);

        if (actionListener != null) {
          actionListener.onCopyEvent(event, targetCalendar, targetDateTime);
        }
      } catch (Exception e) {
        showError("Invalid date or time format: " + e.getMessage());
      }
    }
  }

  /**
   * Updates the status label.
   */
  private void updateStatusLabel(String message) {
    statusLabel.setText(message);
  }
  
  /**
   * Returns common timezones for selection.
   */
  private String[] getCommonTimezones() {
    return new String[]{
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "America/Phoenix",
        "America/Anchorage",
        "Pacific/Honolulu",
        "Europe/London",
        "Europe/Paris",
        "Europe/Berlin",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Asia/Dubai",
        "Australia/Sydney",
        "UTC"
    };
  }
  
  // CalendarView interface implementation
  
  @Override
  public void showMessage(String message) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(this, message, "Information", 
          JOptionPane.INFORMATION_MESSAGE);
      updateStatusLabel(message);
    });
  }
  
  @Override
  public void showError(String error) {
    SwingUtilities.invokeLater(() -> {
      JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
      updateStatusLabel("Error: " + error);
    });
  }
  
  @Override
  public void showEventsOnDate(LocalDate date, List<CalendarEvent> events) {
    SwingUtilities.invokeLater(() -> {
      StringBuilder sb = new StringBuilder();
      
      if (events.isEmpty()) {
        sb.append("No events scheduled on ").append(date.format(DATE_FORMATTER));
      } else {
        sb.append("Events on ").append(date.format(DATE_FORMATTER)).append(":\n\n");
        
        for (CalendarEvent event : events) {
          sb.append("• ").append(event.getSubject()).append("\n");
          
          if (event.isAllDay()) {
            sb.append("  All day event\n");
          } else {
            sb.append("  Time: ").append(event.getStartDateTime().toLocalTime())
                .append(" - ").append(event.getEndDateTime().toLocalTime()).append("\n");
          }
          
          if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            sb.append("  Location: ").append(event.getLocation()).append("\n");
          }
          
          if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            sb.append("  Description: ").append(event.getDescription()).append("\n");
          }
          
          sb.append("\n");
        }
      }
      
      eventDisplayArea.setText(sb.toString());
      updateStatusLabel("Showing " + events.size() + " event(s) on " 
          + date.format(DATE_FORMATTER));
    });
  }
  
  @Override
  public void showEventsInRange(LocalDateTime start, LocalDateTime end, 
      List<CalendarEvent> events) {
    // Not used in GUI mode, but required by interface
    showMessage("Found " + events.size() + " events in range");
  }
  
  @Override
  public void showStatus(boolean isBusy) {
    String status = isBusy ? "Busy" : "Available";
    showMessage("Status: " + status);
  }
  
  @Override
  public void exportToFile(String filename, List<CalendarEvent> events) {
    showMessage("Calendar exported to: " + filename);
  }
  
  /**
   * Interface for handling GUI actions.
   */
  public interface GuiActionListener {
    /**
     * Called when user creates a new calendar.
     */
    void onCreateCalendar(String name, String timezone);

    /**
     * Called when user selects a calendar.
     */
    void onSelectCalendar(String name);

    /**
     * Called when user requests list of calendars.
     */
    void onRequestCalendarList();

    /**
     * Called when user creates a new event.
     */
    void onCreateEvent(String subject, String description, String location,
        LocalDateTime start, LocalDateTime end);

    /**
     * Called when user creates an all-day event.
     */
    void onCreateAllDayEvent(String subject, String description, String location, 
        LocalDate date);

    /**
     * Called when user creates a recurring event.
     */
    void onCreateRecurringEvent(String subject, String description, String location,
        LocalDate startDate, boolean isAllDay, String startTime, String endTime, 
        String weekdays, int weeks, LocalDate endDate);

    /**
     * Called when user views events on a date.
     */
    void onViewEventsOnDate(LocalDate date);

    /**
     * Called when user requests to edit events.
     */
    void onRequestEventsForEdit(LocalDate date);

    /**
     * Called when user edits an event.
     */
    void onEditEvent(CalendarEvent event, String newSubject, String newDesc, 
        String newLocation, String newStartTime, String newEndTime, int scope);

    /**
     * Called when user requests to delete events.
     */
    void onRequestEventsForDelete(LocalDate date);

    /**
     * Called when user deletes an event.
     */
    void onDeleteEvent(CalendarEvent event, int scope);

    /**
     * Called when user requests to copy events.
     */
    void onRequestEventsForCopy(LocalDate date);

    /**
     * Called when user copies an event to another calendar.
     */
    void onCopyEvent(CalendarEvent event, String targetCalendar, LocalDateTime targetDateTime);

    /**
     * Called when user navigates to a different month.
     */
    void onMonthChanged(YearMonth month);
  }
}
