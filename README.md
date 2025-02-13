**Journey Tracker**
Journey Tracker is an Android application that tracks stops along a journey. There are two implementations provided:
XML-Based Version:
Uses RecyclerView with a custom adapter (LazyStopsAdapter) to display stops.
Jetpack Compose Version:
Uses Compose components to build the UI and leverages LazyColumn for efficient, lazy loading of stop cards.

**XML-Based Version Overview**
Key Functions
loadStopsFromJson():
Reads a JSON file from the raw resources, parses it, and converts it into a list of Stop objects (each containing a name, distance, visa requirement, and transit time).
setupViews():
Initializes UI components including:
RecyclerView (with LazyStopsAdapter to display stops).
Buttons for toggling distance units, moving to the next stop, and resetting the journey.
updateJourneyProgress():
Calculates and updates:
Current, covered, and remaining distances.
Animated progress bar (using ValueAnimator) and associated text.
LazyStopsAdapter:
Implements a lazy list by:
Always showing stops up to and including the current one.
Adding a preview of upcoming stops without overloading the list.
Updating visual feedback (background color) based on whether a stop is passed, current, or upcoming.

**Jetpack Compose Version Overview**
This version is implemented in MainActivity.kt using Compose. It uses modern UI paradigms with state management and animations to build a responsive interface.
Key Functions & Components
JourneyTrackerApp():
Initializes state (e.g., list of stops, current stop index, distance unit toggle).
Loads stops from JSON using loadStopsFromJson(context) within a LaunchedEffect.
Displays a loading indicator until the stops are available.
JourneyTrackerScreen():
Calculates distance metrics (total, covered, remaining) and progress percentage.
Uses LinearProgressIndicator with an animated progress value (animateFloatAsState).
Displays buttons to toggle distance units, proceed to the next stop, and reset the journey.
Shows current stop details and overall distance statistics.
Lazy List Implementation with LazyColumn
LazyColumn Usage:
When the stops list contains more than three items, the app uses a LazyColumn for efficient list rendering. This component renders only the visible items and efficiently recycles off-screen content.
ItemsIndexed:
The itemsIndexed(stops) block is used to iterate through the list. For each stop, it creates a StopCard that displays the stop's name, distance, transit time, and visa information (if required).
Auto-Scrolling:
A rememberLazyListState is used along with a LaunchedEffect keyed on currentStopIndex to animate the scroll position. When the current stop changes, the list automatically scrolls to the relevant item using listState.animateScrollToItem(currentStopIndex).
Fallback for Small Lists:
If there are three or fewer stops, a simple Column is used to display all stops statically.
Helper Functions
formatTime() & formatDistance():
Utility functions to format transit times and distances (with toggling between kilometers and miles).
loadStopsFromJson():
Reads the JSON file from the raw resources, parses it, and returns a list of Stop objects.

**Overall Output**
Both versions of the app provide the following functionality:
Journey Display:
Shows the current stop and detailed distance statistics (covered, remaining, and total distance).
Interactive Controls:
Toggle distance units (km/mi).
Advance to the next stop with progress updates.
Reset the journey to the starting point.
Visual Feedback:
An animated progress bar that visually represents journey progress.
A lazy list (RecyclerView in the XML version and LazyColumn in the Compose version) that dynamically displays stop details with visual cues (e.g., different card background colors for past, current, and upcoming stops).


GitHub- https://github.com/Divyansh21321/MCA1-MyJourneyTracker 

2 branches - 
Compose - with compose version of the app
xml-kotlin - with the xml and kotlin version of the app
