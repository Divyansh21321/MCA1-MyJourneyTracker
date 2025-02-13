# Journey Tracker

Journey Tracker is an Android application that tracks stops along a journey. This repository provides two implementations of the app:

- **XML-Based Version:** Uses RecyclerView with a custom adapter (LazyStopsAdapter) to display stops.
- **Jetpack Compose Version:** Uses Compose components to build the UI and leverages LazyColumn for efficient, lazy loading of stop cards.

## Repository Structure

There are two branches available in this repository:
- **Compose:** Contains the Jetpack Compose version of the app.
- **xml-kotlin:** Contains the XML and Kotlin version of the app.

## XML-Based Version Overview

### Key Functions

- **loadStopsFromJson():**  
  Reads a JSON file from the raw resources, parses it, and converts it into a list of `Stop` objects (each containing a name, distance, visa requirement, and transit time).

- **setupViews():**  
  Initializes UI components including:
  - A RecyclerView (using LazyStopsAdapter to display stops).
  - Buttons for toggling distance units, moving to the next stop, and resetting the journey.

- **updateJourneyProgress():**  
  Calculates and updates:
  - Current, covered, and remaining distances.
  - An animated progress bar (using ValueAnimator) along with associated text.

- **LazyStopsAdapter:**  
  Implements a lazy list by:
  - Always displaying stops up to and including the current one.
  - Adding a preview of upcoming stops without overloading the list.
  - Providing visual feedback through background colors that indicate whether a stop is passed, current, or upcoming.

## Jetpack Compose Version Overview

This version is implemented in `MainActivity.kt` using Compose. It leverages modern UI paradigms with state management and animations to create a responsive interface.

### Key Functions & Components

- **JourneyTrackerApp():**  
  - Initializes UI state (e.g., list of stops, current stop index, and distance unit toggle).
  - Loads stops from JSON using `loadStopsFromJson(context)` inside a `LaunchedEffect`.
  - Displays a loading indicator until the stops data is available.

- **JourneyTrackerScreen():**  
  - Calculates distance metrics (total, covered, and remaining distances) and the overall progress percentage.
  - Uses a `LinearProgressIndicator` with an animated progress value via `animateFloatAsState`.
  - Displays buttons to toggle distance units, proceed to the next stop, and reset the journey.
  - Shows the current stop details and overall distance statistics.

### Lazy List Implementation with LazyColumn

- **LazyColumn Usage:**  
  When there are more than three stops, the app uses a `LazyColumn` for efficient rendering. This component renders only the items that are visible on the screen, ensuring efficient memory and performance usage.

- **itemsIndexed:**  
  The `itemsIndexed(stops)` block iterates through the stops list. For each stop, a `StopCard` is created that displays the stop's name, distance from the previous stop, transit time, and visa information (if applicable).

- **Auto-Scrolling:**  
  A `rememberLazyListState` is used with a `LaunchedEffect` keyed on `currentStopIndex` to animate scrolling. When the current stop changes, the list automatically scrolls to bring the relevant item into view using `listState.animateScrollToItem(currentStopIndex)`.

- **Fallback for Small Lists:**  
  If there are three or fewer stops, a simple `Column` is used to display all stops statically.

### Helper Functions

- **formatTime() & formatDistance():**  
  Utility functions to format transit times and distances, with support for toggling between kilometers and miles.

- **loadStopsFromJson():**  
  Reads the JSON file from the raw resources, parses it, and returns a list of `Stop` objects.

## Overall Output

Both versions of the app offer the following functionality:

- **Journey Display:**  
  Shows the current stop along with detailed distance statistics (covered, remaining, and total distances).

- **Interactive Controls:**  
  - Toggle distance units (km/mi).
  - Advance to the next stop with progress updates.
  - Reset the journey to the starting point.

- **Visual Feedback:**  
  - An animated progress bar that visually represents the journey's progress.
  - A lazy list (RecyclerView in the XML version and LazyColumn in the Compose version) that dynamically displays stop details with visual cues (e.g., different card background colors for past, current, and upcoming stops).

## GitHub Repository

For more details, visit the GitHub repository: [Journey Tracker Repository](https://github.com/Divyansh21321/MCA1-MyJourneyTracker)

- **Branches:**
  - **Compose:** Contains the Jetpack Compose version of the app.
  - **xml-kotlin:** Contains the XML and Kotlin version of the app.
