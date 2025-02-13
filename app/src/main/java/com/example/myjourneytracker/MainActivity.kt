package com.example.myjourneytracker

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

// Data model remains the same
data class Stop(
    val name: String,
    val distanceFromPrevious: Double,
    val visaRequired: Boolean,
    val transitTime: Int
)

// Define custom colors (adjust these values to match your design)
val DarkViolet = Color(0xFF673AB7)
val MediumViolet = Color(0xFF9575CD)
val LightViolet = Color(0xFFD1C4E9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JourneyTrackerApp()
        }
    }
}

@Composable
fun JourneyTrackerApp() {
    val context = LocalContext.current
    var stops by remember { mutableStateOf<List<Stop>>(emptyList()) }
    // Load stops from raw JSON when the composable is first launched
    LaunchedEffect(Unit) {
        stops = loadStopsFromJson(context)
    }
    // UI state variables
    var currentStopIndex by remember { mutableStateOf(0) }
    var showDistanceInKm by remember { mutableStateOf(true) }

    if (stops.isEmpty()) {
        // Simple loading indicator while stops are loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        JourneyTrackerScreen(
            stops = stops,
            currentStopIndex = currentStopIndex,
            showDistanceInKm = showDistanceInKm,
            onToggleUnits = { showDistanceInKm = !showDistanceInKm },
            onNextStop = {
                if (currentStopIndex < stops.size - 1) {
                    currentStopIndex++
                } else {
                    Toast.makeText(context, "Already at final destination!", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onReset = {
                currentStopIndex = 0
                Toast.makeText(context, "Journey reset to start!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun JourneyTrackerScreen(
    stops: List<Stop>,
    currentStopIndex: Int,
    showDistanceInKm: Boolean,
    onToggleUnits: () -> Unit,
    onNextStop: () -> Unit,
    onReset: () -> Unit
) {
    // Calculate distance metrics
    val totalDistance = stops.sumOf { it.distanceFromPrevious }
    val coveredDistance = stops.take(currentStopIndex + 1).sumOf { it.distanceFromPrevious }
    val remainingDistance = totalDistance - coveredDistance
    val progressPercent =
        if (totalDistance > 0) ((coveredDistance / totalDistance) * 100).toInt() else 0

    // Animate the progress change
    val animatedProgress by animateFloatAsState(targetValue = progressPercent / 100f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top row: Unit toggle and Reset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onToggleUnits) {
                Text(text = if (showDistanceInKm) "Switch to Miles" else "Switch to Km")
            }
            Button(onClick = onReset) {
                Text("Reset Journey")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Current Stop display
        Text(
            text = "Current Stop: ${stops[currentStopIndex].name}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Progress indicator and percentage
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$progressPercent%", fontSize = 20.sp, color = Color(0xFF66407C))
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Distance statistics
        Text(
            text = "Distance covered: ${formatDistance(coveredDistance, showDistanceInKm)} ($progressPercent%)\n" +
                    "Distance remaining: ${formatDistance(remainingDistance, showDistanceInKm)}\n" +
                    "Total distance: ${formatDistance(totalDistance, showDistanceInKm)}",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Next stop button (only visible if not at final destination)
        if (currentStopIndex < stops.size - 1) {
            Button(
                onClick = onNextStop,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Proceed to Next Stop")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Display stops list: LazyColumn if more than 3 stops; otherwise a static Column
        if (stops.size > 3) {
            val listState = rememberLazyListState()
            // Animate scrolling to the current stop when it changes
            LaunchedEffect(currentStopIndex) {
                listState.animateScrollToItem(currentStopIndex)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(stops) { index, stop ->
                    StopCard(
                        stop = stop,
                        index = index,
                        currentStopIndex = currentStopIndex,
                        showDistanceInKm = showDistanceInKm
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Column {
                stops.forEachIndexed { index, stop ->
                    StopCard(
                        stop = stop,
                        index = index,
                        currentStopIndex = currentStopIndex,
                        showDistanceInKm = showDistanceInKm
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun StopCard(stop: Stop, index: Int, currentStopIndex: Int, showDistanceInKm: Boolean) {
    // Determine the background color based on progress
    val backgroundColor = when {
        index == currentStopIndex -> DarkViolet
        index < currentStopIndex -> MediumViolet
        else -> LightViolet
    }
    Card(
        backgroundColor = backgroundColor,
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stop.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Distance from previous: ${formatDistance(stop.distanceFromPrevious, showDistanceInKm)}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Transit time: ${formatTime(stop.transitTime)}")
            if (stop.visaRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Visa required!", color = Color.Red)
            }
        }
    }
}

// Helper functions to format time and distance
fun formatTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "$hours h $mins min" else "$mins min"
}

fun formatDistance(distance: Double, showDistanceInKm: Boolean): String {
    return if (showDistanceInKm) {
        String.format("%.1f km", distance)
    } else {
        String.format("%.1f mi", distance * 0.621371)
    }
}

// Function to load stops from the raw JSON file
fun loadStopsFromJson(context: Context): List<Stop> {
    val inputStream = context.resources.openRawResource(R.raw.stops)
    val json = inputStream.bufferedReader().use { it.readText() }
    val jsonObject = JSONObject(json)
    val stopsArray = jsonObject.getJSONArray("stops")
    val stops = mutableListOf<Stop>()
    for (i in 0 until stopsArray.length()) {
        val stopObject = stopsArray.getJSONObject(i)
        stops.add(
            Stop(
                name = stopObject.getString("name"),
                distanceFromPrevious = stopObject.getDouble("distanceFromPrevious"),
                visaRequired = stopObject.getBoolean("visaRequired"),
                transitTime = stopObject.getInt("transitTime")
            )
        )
    }
    return stops
}
