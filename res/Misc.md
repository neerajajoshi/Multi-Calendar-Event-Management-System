# Miscellaneous Information

## Design Changes from Previous Iteration

### 1. Separate GUI Controller
**What changed:** Created `GuiCalendarController` as a separate class from `CalendarController`

**Why we did this:**
- GUI interactions work differently than text commands - they need different handling
- Keeps MVC clean by putting GUI-specific logic in its own controller
- We can still reuse the base `CalendarController` for shared operations
- Follows Single Responsibility Principle

### 2. Event Editing with Scope Options
**What changed:** Added scope parameter to edit operations (0=this event only, 1=this and future, 2=all in series)

**Why we did this:**
- GUI needs this to let users choose how much of a series to edit
- Matches what people expect from other calendar apps like Google Calendar
- Works for both editing and deleting events
- Without this, users couldn't edit just one occurrence of a recurring event

### 3. Fixed Delete Scope Bug
**What changed:** Fixed `deleteFutureEvents()` and `deleteAllSeriesEvents()` to find all matching events, not just series events

**Why we did this:**
- Original code missed standalone events with the same name
- Users expect "delete all" to actually delete all events with that name
- Prevents leftover events after deleting a series
- Found this bug during testing

### 4. Event Cache for Performance
**What changed:** Added `updateEventCacheForMonth()` to cache which dates have events

**Why we did this:**
- GUI was querying the model repeatedly for the same info
- Lets us show colored indicators on dates with events
- Cache updates automatically when events change
- Makes the calendar feel more responsive

### 5. GUI Action Listener Interface
**What changed:** Created `GuiActionListener` interface in `GuiCalendarView`

**Why we did this:**
- Separates the view from controller implementation details
- Makes it easier to swap in different controllers if needed
- Follows Interface Segregation Principle
- Testing is simpler with mock listeners

### 6. Excluded GUI from Mutation Testing
**What changed:** Added `excludedTestClasses = ['GuiCalendarViewTest', 'GuiCalendarControllerTest']` to build.gradle

**Why we did this:**
- PIT can't test actual GUI rendering (it runs in isolation)
- We test the controller logic separately without GUI dependencies
- Prevents false failures in mutation testing
- Keeps our mutation score accurate for testable code

### 7. Recurring Events in GUI
**What changed:** Built a dialog for creating recurring events with weekday checkboxes and frequency options

**Why we did this:**
- Assignment requires recurring event support
- Checkboxes and radio buttons are more user-friendly than typing
- Supports both "repeat X times" and "repeat until date"
- Matches the text command functionality

### 8. Default Calendar on Startup
**What changed:** GUI creates a "Default" calendar automatically if none exist

**Why we did this:**
- Users can start using the app immediately
- Uses their system timezone automatically
- Assignment says "user should not be forced to create a new calendar"
- Implemented in `GuiCalendarController.initializeDefaultCalendar()`

---

## What Works and What Doesn't

### GUI Features (All Working)
- Month view with prev/next navigation
- Click to select days and see them highlighted
- Days with events show up in green with a dot
- Create calendars with different timezones
- Switch between calendars
- Title bar shows which calendar you're using
- Create regular events with start/end times
- Create all-day events
- Create recurring events (pick weekdays, set frequency)
- View all events on a selected day
- Edit events (choose to edit just one, future ones, or the whole series)
- Delete events (same scope options as editing)
- Copy events to other calendars (times convert automatically)
- Status bar shows current calendar and timezone
- Error messages pop up when something goes wrong
- Success messages confirm operations worked

### Text Mode Features (Headless & Interactive - All Working)
- Create and list calendars
- Edit calendar name or timezone
- Switch between calendars
- Create single events, all-day events, and recurring events
- Print events for a specific date or date range
- Check if you're busy at a specific time
- Edit event properties (subject, description, location, status, times)
- Edit entire event series
- Copy events between calendars
- Copy multiple events at once
- Export to CSV or iCal format
- Comments in script files (lines starting with #)
- Blank lines in scripts are ignored
- Errors show helpful messages instead of crashing

### Command-Line Options
- No arguments = GUI launches
- `--mode interactive` = text mode with prompt
- `--mode headless <file>` = runs script and exits
- Invalid arguments = error message and exit

### Core Features (All Working)
- Multiple calendars with different timezones
- Conflict detection (won't let you double-book)
- Recurring events (by count or end date)
- Edit single occurrences without breaking the series
- Automatic timezone conversion
- Event privacy levels (PUBLIC, PRIVATE, CONFIDENTIAL)
- All-day events
- Query events by date or range

### What's Missing or Limited

**GUI limitations:**
- Only month view (no week or day view)
- No undo/redo
- Can't drag events to reschedule them
- No search box in GUI
- Can't import files through GUI (use text commands instead)

**Text mode limitations:**
- Can't delete events via text commands (GUI only)
- No undo/redo

