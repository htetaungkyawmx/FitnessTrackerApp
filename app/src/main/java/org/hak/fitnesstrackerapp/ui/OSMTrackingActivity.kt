package org.hak.fitnesstrackerapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.adapters.WorkoutHistoryAdapter
import org.hak.fitnesstrackerapp.database.SQLiteHelper
import org.hak.fitnesstrackerapp.model.Workout
import org.hak.fitnesstrackerapp.utils.SharedPrefManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class OSMTrackingActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvTimer: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvPace: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var fabStartStop: Button
    private lateinit var fabPause: Button
    private lateinit var animationView: LottieAnimationView
    private lateinit var summaryCard: androidx.cardview.widget.CardView
    private lateinit var tvWorkoutCount: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var noWorkoutsCard: androidx.cardview.widget.CardView

    // GPS/Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private var isPaused = false

    // Stats
    private var totalDistance = 0.0f // in meters
    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var totalPauseTime: Long = 0
    private var currentSpeed = 0.0f
    private var maxSpeed = 0.0f
    private var totalCalories = 0
    private var locations = mutableListOf<Location>()
    private var speedHistory = mutableListOf<Float>()

    // Timers
    private lateinit var handler: Handler
    private lateinit var timerRunnable: Runnable
    private lateinit var updateRunnable: Runnable
    private val updateInterval = 10000L // 10 seconds for data update

    // Database
    private lateinit var dbHelper: SQLiteHelper
    private var currentUserId: Int = 0
    private var workoutType: String = ""

    // Map
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var routeOverlay: Polyline

    companion object {
        private const val TAG = "OSMTrackingActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_UPDATE_INTERVAL = 1000L // 1 second for location
        private const val LOCATION_FASTEST_INTERVAL = 500L
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_tracking)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Get workout type
        workoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "Running"
        val animationRes = intent.getIntExtra("ANIMATION_RES", -1)

        Log.d(TAG, "Starting tracking for: $workoutType")

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$workoutType Tracking"

        // Initialize OSM
        Configuration.getInstance().load(this, getSharedPreferences("osm", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        // Initialize database
        dbHelper = SQLiteHelper(this)

        // Get current user
        val user = SharedPrefManager.getInstance(this).user
        currentUserId = user?.id ?: 0

        if (currentUserId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        initViews()

        // Setup animation
        setupAnimation(animationRes)

        // Setup Map
        mapView = findViewById(R.id.mapView)
        setupMap()

        // Setup Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Handlers
        handler = Handler(Looper.getMainLooper())

        // Setup click listeners
        setupClickListeners()

        // Show summary
        showSummaryOrNoWorkouts()

        // Check permission
        checkLocationPermission()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        tvDistance = findViewById(R.id.tvDistance)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvCalories = findViewById(R.id.tvCalories)
        tvPace = findViewById(R.id.tvPace)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvAltitude = findViewById(R.id.tvAltitude)
        fabStartStop = findViewById(R.id.fabStartStop)
        fabPause = findViewById(R.id.fabPause)
        animationView = findViewById(R.id.animationView)
        summaryCard = findViewById(R.id.summaryCard)
        tvWorkoutCount = findViewById(R.id.tvWorkoutCount)
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        noWorkoutsCard = findViewById(R.id.noWorkoutsCard)
    }

    private fun setupMap() {
        try {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            mapView.setBuiltInZoomControls(true)
            mapView.setMultiTouchControls(true)
            mapView.controller.setZoom(17.0)

            // Default location (Yangon)
            val defaultLocation = GeoPoint(16.8409, 96.1735)
            mapView.controller.setCenter(defaultLocation)

            // Location overlay
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
            myLocationOverlay.enableMyLocation()
            mapView.overlays.add(myLocationOverlay)

            // Route overlay
            routeOverlay = Polyline()
            routeOverlay.color = Color.parseColor("#FF3B30")
            routeOverlay.width = 8.0f
            mapView.overlays.add(routeOverlay)

            Log.d(TAG, "Map setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Map setup error: ${e.message}", e)
        }
    }

    private fun showSummaryOrNoWorkouts() {
        val recentWorkouts = dbHelper.getRecentWorkoutsByType(currentUserId, workoutType, 30)

        if (recentWorkouts.isNotEmpty()) {
            showRecentWorkoutsSummary(recentWorkouts)
            summaryCard.visibility = View.VISIBLE
            noWorkoutsCard.visibility = View.GONE
        } else {
            summaryCard.visibility = View.GONE
            noWorkoutsCard.visibility = View.VISIBLE
        }
    }

    private fun showRecentWorkoutsSummary(workouts: List<Workout>) {
        tvWorkoutCount.text = "${workouts.size}"

        val totalDuration = workouts.sumOf { it.duration }
        val totalDistance = workouts.sumOf { it.distance ?: 0.0 }
        val totalCalories = workouts.sumOf { it.calories }

        tvTotalDuration.text = "${totalDuration} min"
        tvTotalDistance.text = String.format("%.1f km", totalDistance)
        tvTotalCalories.text = "$totalCalories cal"
    }

    private fun setupAnimation(animationRes: Int) {
        val defaultAnimation = when (workoutType.lowercase()) {
            "running" -> R.raw.running_animation
            "cycling" -> R.raw.cycling_animation
            "swimming" -> R.raw.swimming_animation
            else -> R.raw.running_animation
        }

        val finalAnimationRes = if (animationRes != -1) animationRes else defaultAnimation

        try {
            animationView.setAnimation(finalAnimationRes)
            animationView.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Animation error: ${e.message}", e)
            animationView.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        fabStartStop.setOnClickListener {
            if (!isTracking) {
                startTracking()
            } else {
                stopTracking()
            }
        }

        fabPause.setOnClickListener {
            if (isPaused) {
                resumeTracking()
            } else {
                pauseTracking()
            }
        }

        summaryCard.setOnClickListener {
            val recentWorkouts = dbHelper.getRecentWorkoutsByType(currentUserId, workoutType, 30)
            if (recentWorkouts.isNotEmpty()) {
                showWorkoutHistoryDialog(recentWorkouts)
            }
        }

        noWorkoutsCard.setOnClickListener {
            startTracking()
        }
    }

    private fun showWorkoutHistoryDialog(workouts: List<Workout>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_workout_history, null)

        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewHistory)
        val tvTotalWorkouts = dialogView.findViewById<TextView>(R.id.tvTotalWorkouts)
        val tvTotalCalories = dialogView.findViewById<TextView>(R.id.tvTotalCalories)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WorkoutHistoryAdapter(workouts.take(10))

        val totalCalories = workouts.sumOf { it.calories }
        tvTotalWorkouts.text = "${workouts.size} workouts"
        tvTotalCalories.text = "$totalCalories calories"

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Recent $workoutType Workouts")
            .setView(dialogView)
            .setPositiveButton("Start GPS") { dialog, which ->
                startTracking()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (!hasLocationPermission()) {
            checkLocationPermission()
            return
        }

        try {
            // Reset stats
            resetStats()

            // Start tracking
            isTracking = true
            isPaused = false
            startTime = System.currentTimeMillis()

            // Update UI
            fabStartStop.text = "STOP"
            fabPause.visibility = View.VISIBLE

            // Show animation
            animationView.visibility = View.VISIBLE
            animationView.playAnimation()

            // Hide cards
            summaryCard.visibility = View.GONE
            noWorkoutsCard.visibility = View.GONE

            // Start location updates
            startLocationUpdates()

            // Start timers
            startTimer()
            startDataUpdates()

            Toast.makeText(this, "Started $workoutType tracking", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Start tracking error: ${e.message}", e)
        }
    }

    private fun pauseTracking() {
        try {
            isPaused = true
            pauseTime = System.currentTimeMillis()
            fabPause.text = "▶"
            animationView.pauseAnimation()
            stopLocationUpdates()
            handler.removeCallbacks(updateRunnable)

            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Pause error: ${e.message}", e)
        }
    }

    private fun resumeTracking() {
        try {
            isPaused = false
            totalPauseTime += System.currentTimeMillis() - pauseTime
            fabPause.text = "⏸"
            animationView.resumeAnimation()
            startLocationUpdates()
            startDataUpdates()

            Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Resume error: ${e.message}", e)
        }
    }

    private fun stopTracking() {
        try {
            isTracking = false
            isPaused = false

            // Update UI
            fabStartStop.text = "START"
            fabPause.visibility = View.GONE

            animationView.visibility = View.GONE
            animationView.cancelAnimation()

            // Stop updates
            stopLocationUpdates()
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(updateRunnable)

            // Save workout
            saveWorkout()

            // Show summary
            showSummaryDialog()
        } catch (e: Exception) {
            Log.e(TAG, "Stop error: ${e.message}", e)
        }
    }

    private fun resetStats() {
        totalDistance = 0.0f
        totalPauseTime = 0
        totalCalories = 0
        currentSpeed = 0.0f
        maxSpeed = 0.0f
        locations.clear()
        speedHistory.clear()

        try {
            routeOverlay.points.clear()
            mapView.invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "Reset map error: ${e.message}", e)
        }

        // Reset UI
        tvTimer.text = "00:00:00"
        tvDistance.text = "0.00"
        tvSpeed.text = "0.0"
        tvCalories.text = "0"
        tvPace.text = "0:00"
        tvMaxSpeed.text = "0.0"
        tvAltitude.text = "0 m"
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.create().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = LOCATION_FASTEST_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        updateLocation(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Location updates error: ${e.message}", e)
        }
    }

    private fun stopLocationUpdates() {
        try {
            if (::locationCallback.isInitialized) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Stop location error: ${e.message}", e)
        }
    }

    private fun updateLocation(location: Location) {
        try {
            locations.add(location)

            if (locations.size > 1) {
                val previousLocation = locations[locations.size - 2]
                val distance = location.distanceTo(previousLocation)
                totalDistance += distance

                currentSpeed = location.speed * 3.6f // Convert m/s to km/h
                speedHistory.add(currentSpeed)

                // Update max speed
                if (currentSpeed > maxSpeed) {
                    maxSpeed = currentSpeed
                }

                updateRealTimeStats(location)
                updateMap(location)
            } else {
                // First location
                updateMap(location)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update location error: ${e.message}", e)
        }
    }

    private fun updateRealTimeStats(location: Location) {
        try {
            val distanceKm = totalDistance / 1000
            tvDistance.text = String.format("%.2f", distanceKm)

            tvSpeed.text = String.format("%.1f", currentSpeed)
            tvMaxSpeed.text = String.format("%.1f", maxSpeed)

            // Update calories every 10 seconds (in startDataUpdates)

            // Calculate pace (min/km)
            val elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime
            if (distanceKm > 0.01f && elapsedTime > 0) {
                val paceSeconds = (elapsedTime / 1000) / distanceKm
                val minutes = (paceSeconds / 60).toInt()
                val seconds = (paceSeconds % 60).toInt()
                tvPace.text = String.format("%d:%02d", minutes, seconds)
            }

            // Altitude
            val altitude = location.altitude
            tvAltitude.text = String.format("%.0f m", altitude)
        } catch (e: Exception) {
            Log.e(TAG, "Update stats error: ${e.message}", e)
        }
    }

    private fun updateMap(location: Location) {
        try {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            routeOverlay.addPoint(geoPoint)

            if (locations.size == 1) {
                mapView.controller.setCenter(geoPoint)
                mapView.controller.setZoom(18.0)
            }

            mapView.postInvalidate()
        } catch (e: Exception) {
            Log.e(TAG, "Update map error: ${e.message}", e)
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (isTracking && !isPaused) {
                    val elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime
                    updateTimer(elapsedTime)
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(timerRunnable)
    }

    private fun startDataUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                if (isTracking && !isPaused) {
                    updateCaloriesAndStats()
                    handler.postDelayed(this, updateInterval) // Update every 10 seconds
                }
            }
        }
        handler.post(updateRunnable)
    }

    private fun updateCaloriesAndStats() {
        try {
            val elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime
            val hours = elapsedTime.toFloat() / (1000 * 60 * 60)
            val distanceKm = totalDistance / 1000

            // Calculate calories based on MET (Metabolic Equivalent of Task)
            val caloriesPerHour = when (workoutType.lowercase()) {
                "running" -> 600 * hours // 600 cal/hour for running
                "cycling" -> 400 * hours // 400 cal/hour for cycling
                "walking" -> 300 * hours // 300 cal/hour for walking
                else -> 500 * hours // Default
            }

            // Add distance-based calories
            val distanceCalories = when (workoutType.lowercase()) {
                "running" -> distanceKm * 60 // 60 cal/km
                "cycling" -> distanceKm * 30 // 30 cal/km
                "walking" -> distanceKm * 50 // 50 cal/km
                else -> distanceKm * 40 // Default
            }

            totalCalories = (caloriesPerHour + distanceCalories).roundToInt()
            tvCalories.text = totalCalories.toString()

            // Log the update
            Log.d(TAG, "Data update - Distance: $distanceKm km, Calories: $totalCalories, Time: ${elapsedTime/1000}s")

        } catch (e: Exception) {
            Log.e(TAG, "Update calories error: ${e.message}", e)
        }
    }

    private fun updateTimer(millis: Long) {
        try {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = (millis / (1000 * 60 * 60))

            tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            Log.e(TAG, "Update timer error: ${e.message}", e)
        }
    }

    private fun saveWorkout() {
        if (currentUserId == 0 || locations.isEmpty()) return

        try {
            val duration = ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toInt()
            val distanceKm = totalDistance / 1000
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Calculate average speed
            val avgSpeed = if (duration > 0) {
                (distanceKm / (duration / 3600.0))
            } else 0.0

            val workout = Workout(
                userId = currentUserId,
                type = workoutType,
                duration = duration,
                distance = distanceKm.toDouble(),
                calories = totalCalories,
                notes = "GPS Tracked - Distance: ${String.format("%.2f", distanceKm)} km, Time: ${duration/60} min",
                date = date,
                timestamp = System.currentTimeMillis(),
                synced = false
            )

            val result = dbHelper.insertWorkout(workout)
            if (result > 0) {
                Log.d(TAG, "Workout saved: $workout")
                showSummaryOrNoWorkouts()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Save workout error: ${e.message}", e)
        }
    }

    private fun showSummaryDialog() {
        try {
            val duration = ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toInt()
            val distanceKm = totalDistance / 1000
            val avgSpeed = if (duration > 0) {
                String.format("%.1f", (distanceKm / (duration / 3600.0)))
            } else "0.0"

            val message = """
                🏆 Workout Completed!
                
                Duration: ${duration / 60} min ${duration % 60} sec
                Distance: ${String.format("%.2f", distanceKm)} km
                Avg Speed: $avgSpeed km/h
                Max Speed: ${String.format("%.1f", maxSpeed)} km/h
                Calories: $totalCalories cal
                
                Great job! Keep it up!
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Workout Summary")
                .setMessage(message)
                .setPositiveButton("Save & View") { _, _ ->
                    saveWorkout()
                    showSummaryOrNoWorkouts()
                }
                .setNegativeButton("Done") { _, _ ->
                    finish()
                }
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Summary dialog error: ${e.message}", e)
        }
    }

    // Permission methods
    private fun checkLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (isTracking) {
            AlertDialog.Builder(this)
                .setTitle("Stop Tracking?")
                .setMessage("Are you sure you want to stop tracking and exit?")
                .setPositiveButton("Stop & Exit") { _, _ ->
                    stopTracking()
                    super.onBackPressed()
                }
                .setNegativeButton("Continue", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mapView.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "Resume error: ${e.message}", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mapView.onPause()
            if (isTracking && !isPaused) {
                pauseTracking()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pause error: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mapView.onDetach()
            handler.removeCallbacks(timerRunnable)
            handler.removeCallbacks(updateRunnable)
            stopLocationUpdates()
            dbHelper.closeDB()
        } catch (e: Exception) {
            Log.e(TAG, "Destroy error: ${e.message}", e)
        }
    }
}