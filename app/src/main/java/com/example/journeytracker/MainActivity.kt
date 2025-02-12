package com.example.journeytracker

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class Stop(
    val name: String,
    val distanceFromPrevious: Double,
    val visaRequired: Boolean,
    val transitTime: Int
)

class MainActivity : AppCompatActivity() {
    private var stops = mutableListOf<Stop>()
    private var currentStopIndex = 0
    private var showDistanceInKm = true
    private lateinit var adapter: LazyStopsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Load stops from JSON
        loadStopsFromJson()

        // Initialize views
        setupViews()

        // Initial UI update with 0 progress
        resetJourney()
    }

    private fun loadStopsFromJson() {
        try {
            val inputStream = resources.openRawResource(R.raw.stops)
            val json = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val jsonObject = JSONObject(json)
            val stopsArray = jsonObject.getJSONArray("stops")

            stops.clear() // Clear existing stops before loading
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupViews() {
        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.stopsRecyclerView)
        adapter = LazyStopsAdapter(stops, currentStopIndex, showDistanceInKm)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup unit toggle button
        findViewById<View>(R.id.unitToggleButton).setOnClickListener {
            showDistanceInKm = !showDistanceInKm
            updateJourneyProgress()
            adapter.updateUnits(showDistanceInKm)
        }

        // Setup next stop button
        findViewById<View>(R.id.nextStopButton).setOnClickListener {
            if (currentStopIndex < stops.size - 1) {
                val previousIndex = currentStopIndex  // CHANGE: Save previous index for animation
                currentStopIndex++
                animateProgress(previousIndex, currentStopIndex)  // CHANGE: Animate progress change
                updateJourneyProgress()
                adapter.updateCurrentStop(currentStopIndex)

                if (currentStopIndex == stops.size - 1) {
                    showToast("You have reached the final destination!")
                }
            } else {
                showToast("Already at final destination!")
            }
        }

        // Setup reset button
        findViewById<View>(R.id.resetButton).setOnClickListener {
            val previousIndex = currentStopIndex  // CHANGE: Save previous index for animation
            currentStopIndex = 0
            animateProgress(previousIndex, currentStopIndex)  // CHANGE: Animate progress change
            resetJourney()
            showToast("Journey reset to start!")
        }
    }

    private fun resetJourney() {
        currentStopIndex = 0
        updateJourneyProgress()
        adapter.updateCurrentStop(currentStopIndex)
    }

    private fun updateJourneyProgress() {
        // Update current stop text
        findViewById<TextView>(R.id.currentStopText).text = "Current Stop: ${stops[currentStopIndex].name}"

        // Calculate distances
        val totalDistance = stops.sumOf { it.distanceFromPrevious }
        val coveredDistance = stops.take(currentStopIndex + 1).sumOf { it.distanceFromPrevious }
        val remainingDistance = totalDistance - coveredDistance

        // Update progress bar based on ACTUAL DISTANCE (new code)
        val progress = if (totalDistance > 0) {
            ((coveredDistance / totalDistance) * 100).toInt()
        } else {
            0
        }
        findViewById<android.widget.ProgressBar>(R.id.journeyProgress).progress = progress

        // Update distance stats with percentage (modified code)
        findViewById<TextView>(R.id.distanceStats).text = """
        Distance covered: ${formatDistance(coveredDistance)} ($progress%)
        Distance remaining: ${formatDistance(remainingDistance)}
        Total distance: ${formatDistance(totalDistance)}
    """.trimIndent()

        // Add this line after setting the progress bar value
        findViewById<TextView>(R.id.progressPercentage).text = "$progress%"
        // Update next stop button visibility

        findViewById<View>(R.id.nextStopButton).visibility =
            if (currentStopIndex < stops.size - 1) View.VISIBLE else View.GONE
    }

    private fun animateProgress(fromIndex: Int, toIndex: Int) {
        val totalDistance = stops.sumOf { it.distanceFromPrevious }
        val fromDistance = stops.take(fromIndex + 1).sumOf { it.distanceFromPrevious }
        val toDistance = stops.take(toIndex + 1).sumOf { it.distanceFromPrevious }

        val fromProgress = ((fromDistance / totalDistance) * 100).toInt()
        val toProgress = ((toDistance / totalDistance) * 100).toInt()

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.journeyProgress)
        val progressText = findViewById<TextView>(R.id.progressPercentage)

        ValueAnimator.ofInt(fromProgress, toProgress).apply {
            duration = 1000 // 1 second animation
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Int
                progressBar.progress = progress
                progressText.text = "$progress%"
            }
            start()
        }
    }

    private fun formatDistance(distance: Double): String {
        return if (showDistanceInKm) {
            "%.1f km".format(distance)
        } else {
            "%.1f mi".format(distance * 0.621371)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

class LazyStopsAdapter(
    private val allStops: List<Stop>,
    private var currentStopIndex: Int,
    private var showDistanceInKm: Boolean
) : RecyclerView.Adapter<LazyStopsAdapter.StopViewHolder>() {

    // Maintain a list of visible stops
    private var visibleStops = mutableListOf<Stop>()

    init {
        // Initially show first 5 stops (NY, London, Dubai, Singapore)
        updateVisibleStops()
    }

    private fun updateVisibleStops() {
        visibleStops.clear()

        // Always include all stops up to and including current stop
        visibleStops.addAll(allStops.take(currentStopIndex + 1))

        // If we haven't shown the first 3 stops yet, show them all
        if (allStops.size >= 2 && visibleStops.size < 2) {
            visibleStops.clear()
            visibleStops.addAll(allStops.take(2))
        }

        // Add next 2 upcoming stops after current stop for preview
        val nextStopsStart = maxOf(currentStopIndex + 1, 2)
        val nextStops = allStops.subList(
            fromIndex = nextStopsStart,
            toIndex = minOf(nextStopsStart + 1, allStops.size)
        )
        visibleStops.addAll(nextStops)
    }

    class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val nameText: TextView = itemView.findViewById(R.id.stopName)
        val distanceText: TextView = itemView.findViewById(R.id.stopDistance)
        val transitTimeText: TextView = itemView.findViewById(R.id.stopTransitTime)
        val visaText: TextView = itemView.findViewById(R.id.visaRequired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stop, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = visibleStops[position]
        holder.nameText.text = stop.name
        holder.distanceText.text = "Distance from previous: ${formatDistance(stop.distanceFromPrevious)}"
        holder.transitTimeText.text = "Transit time: ${formatTime(stop.transitTime)}"

        holder.visaText.apply {
            visibility = if (stop.visaRequired) View.VISIBLE else View.GONE
            text = "Visa required!"
        }

        // Update card background with violet shades
        val stopIndex = allStops.indexOf(stop)
        holder.card.setCardBackgroundColor(
            when {
                stopIndex == currentStopIndex -> holder.itemView.context.getColor(R.color.dark_violet)
                stopIndex < currentStopIndex -> holder.itemView.context.getColor(R.color.medium_violet)
                else -> holder.itemView.context.getColor(R.color.light_violet)
            }
        )

        // Add a visual separator if this is the last of the first 4 stops
//        if (position == 3 && allStops.size > 4) {
//            holder.card.strokeWidth = 4  // Add a thicker border
//            holder.card.strokeColor = holder.itemView.context.getColor(R.color.dark_violet)
//        } else {
//            holder.card.strokeWidth = 0
//        }
    }

    override fun getItemCount() = visibleStops.size

    fun updateUnits(showInKm: Boolean) {
        showDistanceInKm = showInKm
        notifyDataSetChanged()
    }

    fun updateCurrentStop(newIndex: Int) {
        currentStopIndex = newIndex
        updateVisibleStops()
        notifyDataSetChanged()
    }

    private fun formatDistance(distance: Double): String {
        return if (showDistanceInKm) {
            "%.1f km".format(distance)
        } else {
            "%.1f mi".format(distance * 0.621371)
        }
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            "$hours h $mins min"
        } else {
            "$mins min"
        }
    }
}