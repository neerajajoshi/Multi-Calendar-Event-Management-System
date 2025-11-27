# Calendar Application Usage Guide

## Building the Application

To build the JAR file, run:
```bash
./gradlew jar
```

This creates a JAR file in the `build/libs/` directory.

## Running the Application

The application supports three modes: GUI, interactive, and headless.

### GUI Mode (Graphical User Interface)

Run the application with no arguments to launch the graphical interface:

```bash
java -jar build/libs/calendar-1.0.jar
```

Or simply double-click the JAR file. The GUI will open automatically with a default calendar.

### Interactive Mode

Run the application in interactive mode to enter commands manually:

```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

In interactive mode, you can type commands and see results immediately. Type `exit` to quit.

### Headless Mode

Run the application in headless mode to execute commands from a file:

```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
java -jar build/libs/calendar-1.0.jar --mode headless res/invalid.txt
```

The command file must end with an `exit` command.

## Using the GUI

### Calendar Management
- **Create a new calendar:** Click "New Calendar" button, enter name and select timezone from dropdown
- **Select a calendar:** Click "Select Calendar" button, choose from list of available calendars
- **Default calendar:** A default calendar is automatically created in your system timezone when you first launch the GUI

### Navigating the Calendar
- **View different months:** Click "◀ Previous" or "Next ▶" buttons
- **Return to current month:** Click "Today" button
- **Select a day:** Click on any day in the calendar grid to view events on that date
- **Visual indicators:**
  - **Current day:** Highlighted in light blue
  - **Selected day:** Shown with blue border
  - **Days with events:** Highlighted in light green with a green dot (●) below the day number
  - **Empty days:** No special highlighting

### Creating Events
- **Create an event:** Click "Create Event" button (events are created on the currently selected day)
- **Event subject:** Enter the event name/title
- **Start/End time:** Enter times in HH:mm format (e.g., 09:00, 17:30)
- **All-day event:** Check the "All Day Event" checkbox (disables time fields)
- **Recurring event:** Check the "Recurring Event" checkbox to show additional options
  - Select weekdays using checkboxes (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
  - Choose frequency: either "Number of occurrences" (e.g., 4 times) or "Until end date" (e.g., 2025-12-31)

### Viewing Events
- **View events on a day:** Click on a day in the calendar to see all events scheduled for that date
- **Event details:** The right panel shows event subject, time, location, and description
- **Refresh events:** Click "Refresh Events" button to reload the event list

### Editing Events
- **Edit an event:** Click "Edit Event" button
- **Select event to edit:** Choose from the list of events on the selected day
- **Edit fields:** Modify subject, description, location, start time, or end time
- **Edit scope:** Choose one of three options:
  - "This event only" - Edits just the selected occurrence
  - "This and future events" - Edits this occurrence and all future occurrences in the series
  - "All events in series" - Edits every occurrence in the recurring series

### Deleting Events
- **Delete an event:** Click "Delete Event" button
- **Select event to delete:** Choose from the list of events on the selected day
- **Delete scope:** Choose one of three options:
  - "This event only" - Deletes just the selected occurrence
  - "This and future events" - Deletes this occurrence and all future occurrences in the series
  - "All events in series" - Deletes every occurrence in the recurring series
- **Confirmation:** A confirmation dialog will appear before deletion

### Copying Events
- **Copy an event:** Click "Copy Event" button
- **Select event to copy:** Choose from the list of events on the selected day
- **Target calendar:** Select the destination calendar from the dropdown
- **Automatic timezone conversion:** 
  - When you select a target calendar, the time is automatically converted to that calendar's timezone
  - Example: 8:00 AM in Asia/Kolkata becomes 9:30 PM (previous day) in America/New_York
  - The conversion is shown in the dialog (e.g., "Asia/Kolkata → America/New_York")
- **Target date/time:** Pre-filled with converted time, but you can adjust if needed
- **Note:** You must have at least two calendars to copy events

### Visual Feedback
- **Status bar:** Bottom of window shows current operation status and messages
- **Dialog messages:** Success and error messages appear in popup dialogs
- **Active calendar:** Shown in calendar selection dialog with current timezone

## Example Commands

### Calendar Management

**Create Calendar:**
```
create calendar --name Work --timezone America/New_York
create calendar --name Personal --timezone America/Los_Angeles
```

**List All Calendars:**
```
list calendars
show calendars
```
This shows all calendars with their timezones and marks the currently active one with [ACTIVE].

**Edit Calendar:**
```
edit calendar --name Work --property name WorkCalendar
edit calendar --name Personal --property timezone America/Denver
```

**Use Calendar:**
```
use calendar --name Work
```

### Creating Events

**Single Event:**
```
create event "Team Meeting" from 2025-01-15T10:00 to 2025-01-15T11:00
```

**All-Day Event:**
```
create event "Holiday" on 2025-01-15
```

**Recurring Event (by count):**
```
create event "Weekly Standup" from 2025-01-13T09:00 to 2025-01-13T09:30 repeats MW for 4 times
```

**Recurring Event (by end date):**
```
create event "Daily Exercise" on 2025-01-13 repeats MTWRF until 2025-01-31
```

### Querying Events

**Events on a specific date:**
```
print events on 2025-01-15
```

**Events in a date range:**
```
print events from 2025-01-13T08:00 to 2025-01-15T18:00
```

**Check availability:**
```
show status on 2025-01-15T10:30
```

### Editing Events

**Edit single event:**
```
edit event subject "Team Meeting" from 2025-01-15T10:00 with "Project Review"
```

**Edit all events in series from a date:**
```
edit events location "Weekly Standup" from 2025-01-15T09:00 with "Conference Room A"
```

**Edit entire series:**
```
edit series description "Daily Exercise" from 2025-01-13T08:00 with "Morning workout routine"
```

### Event Copying

**Copy Single Event:**
```
copy event "Team Meeting" on 2025-01-15T10:00 --target Personal to 2025-01-15T07:00
```
Copies a specific event with the given name and start date/time from the current calendar to the target calendar. The "to" date/time is in the target calendar's timezone.

**Copy Events on Date:**
```
copy events on 2025-01-15 --target Personal to 2025-01-20
```
Copies all events scheduled on that day. Times are converted to the target calendar's timezone (e.g., 2pm EST becomes 11am PST).

**Copy Events in Range:**
```
copy events between 2025-01-15 and 2025-01-16 --target Personal to 2025-01-22
```
Copies all events in the specified date interval (inclusive). The target date corresponds to the start of the interval.

### Export Calendar

**CSV Export:**
```
export cal my_calendar.csv
```

**iCal Export:**
```
export cal my_calendar.ics
export cal my_calendar.ical
```

### Exit

```
exit
```

## Date and Time Formats

- **Date:** YYYY-MM-DD (e.g., 2025-01-15)
- **Time:** HH:mm (e.g., 10:30)
- **DateTime:** YYYY-MM-DDTHH:mm (e.g., 2025-01-15T10:30)
- **Weekdays:** M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday

## Important Notes

### Calendar Context Requirement
**You MUST create and select a calendar before performing any event operations:**
1. First, create a calendar: `create calendar --name <name> --timezone <timezone>`
2. Then, select it: `use calendar --name <name>`
3. Only then can you create events, print events, export, or perform other operations

Without selecting a calendar first, commands like `create event`, `print events`, `show status`, and `export cal` will fail with an error message.

### Other Notes
- Event subjects with spaces must be enclosed in double quotes
- Each calendar has its own timezone (IANA format: America/New_York, Europe/London, etc.)
- Event copying automatically handles timezone conversion
- Both CSV and iCal exports are compatible with Google Calendar
- Export format is auto-detected by file extension (.csv, .ics, .ical)
- All file paths are platform independent