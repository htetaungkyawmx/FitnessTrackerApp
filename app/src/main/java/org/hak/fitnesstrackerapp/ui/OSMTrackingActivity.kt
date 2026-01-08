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
    private lateinit var fabStartStop: Button
    private lateinit var fabPause: Button
    private lateinit var animationView: LottieAnimationView
    private lateinit var summaryCard: androidx.cardview.widget.CardView
    private lateinit var tvWorkoutCount: TextView
    private lateinit var tvTotalDuration: TextView
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
    private val locations = mutableListOf<Location>()
    private var currentSpeed = 0.0f

    // Handler for timer
    private lateinit var handler: Handler
    private lateinit var timerRunnable: Runnable

    // Database
    private lateinit var dbHelper: SQLiteHelper
    private var currentUserId: Int = 0
    private var workoutType: String = ""

    // Map overlay
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var routeOverlay: Polyline

    companion object {
        private const val TAG = "OSMTrackingActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val UPDATE_INTERVAL = 1000L // 1 second
        private const val FASTEST_INTERVAL = 500L // 0.5 second
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_tracking)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Get workout type from intent
        workoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "Running"
        val animationRes = intent.getIntExtra("ANIMATION_RES", -1)

        Log.d(TAG, "Starting OSMTrackingActivity for workout type: $workoutType")

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$workoutType Tracking"

        // Initialize OSM configuration
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

        // Setup animation based on workout type
        setupAnimation(animationRes)

        // Setup Map
        mapView = findViewById(R.id.mapView)
        setupMap()

        // Setup Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Timer Handler
        handler = Handler(Looper.getMainLooper())

        // Setup click listeners
        setupClickListeners()

        // Show summary initially
        showSummaryOrNoWorkouts()

        // Request location permission
        checkLocationPermission()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        tvDistance = findViewById(R.id.tvDistance)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvCalories = findViewById(R.id.tvCalories)
        fabStartStop = findViewById(R.id.fabStartStop)
        fabPause = findViewById(R.id.fabPause)
        animationView = findViewById(R.id.animationView)
        summaryCard = findViewById(R.id.summaryCard)
        tvWorkoutCount = findViewById(R.id.tvWorkoutCount)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
        noWorkoutsCard = findViewById(R.id.noWorkoutsCard)
    }

    private fun setupMap() {
        try {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            mapView.setBuiltInZoomControls(true)
            mapView.setMultiTouchControls(true)

            // Set default zoom level
            mapView.controller.setZoom(17.0)

            // Set default location (Yangon, Myanmar)
            val defaultLocation = GeoPoint(16.8409, 96.1735)
            mapView.controller.setCenter(defaultLocation)

            // Initialize location overlay
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
            myLocationOverlay.enableMyLocation()
            mapView.overlays.add(myLocationOverlay)

            // Initialize route overlay
            routeOverlay = Polyline()
            routeOverlay.color = Color.parseColor("#FF6200EE")
            routeOverlay.width = 10.0f
            mapView.overlays.add(routeOverlay)

            Log.d(TAG, "Map setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map: ${e.message}", e)
            Toast.makeText(this, "Error setting up map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSummaryOrNoWorkouts() {
        val recentWorkouts = dbHelper.getRecentWorkoutsByType(currentUserId, workoutType, 30)

        Log.d(TAG, "Found ${recentWorkouts.size} recent workouts for $workoutType")

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
        tvTotalDuration.text = "${totalDuration} min"
    }

    private fun setupAnimation(animationRes: Int) {
        val defaultAnimation = when (workoutType.lowercase()) {
            "running" -> R.raw.running_animation
            "cycling" -> R.raw.cycling_animation
            else -> -1
        }

        val finalAnimationRes = if (animationRes != -1) animationRes else defaultAnimation

        if (finalAnimationRes != -1) {
            try {
                animationView.setAnimation(finalAnimationRes)
                animationView.visibility = View.GONE
                Log.d(TAG, "Animation loaded for $workoutType")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading animation: ${e.message}", e)
                animationView.visibility = View.GONE
            }
        } else {
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
        recyclerView.adapter = WorkoutHistoryAdapter(workouts.take(10)) { workout ->
            Toast.makeText(this, "${workout.date}: ${workout.duration} min", Toast.LENGTH_SHORT).show()
        }

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

        Log.d(TAG, "Starting tracking for $workoutType")

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

            animationView.alpha = 0.7f

            // Hide summary cards when tracking starts
            summaryCard.visibility = View.GONE
            noWorkoutsCard.visibility = View.GONE

            // Start location updates
            startLocationUpdates()

            // Start timer
            startTimer()

            Toast.makeText(this, "Started $workoutType tracking", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking: ${e.message}", e)
            Toast.makeText(this, "Error starting tracking: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseTracking() {
        try {
            isPaused = true
            pauseTime = System.currentTimeMillis()
            fabPause.text = "▶"
            animationView.pauseAnimation()
            stopLocationUpdates()

            Toast.makeText(this, "Tracking paused", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing tracking: ${e.message}", e)
        }
    }

    private fun resumeTracking() {
        try {
            isPaused = false
            totalPauseTime += System.currentTimeMillis() - pauseTime
            fabPause.text = "⏸"
            animationView.resumeAnimation()
            startLocationUpdates()

            Toast.makeText(this, "Tracking resumed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming tracking: ${e.message}", e)
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

            // Save workout to database
            saveWorkout()

            // Show summary dialog
            showSummaryDialog()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking: ${e.message}", e)
        }
    }

    private fun resetStats() {
        totalDistance = 0.0f
        totalPauseTime = 0
        currentSpeed = 0.0f
        locations.clear()

        try {
            routeOverlay.points.clear()
            mapView.invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting map: ${e.message}", e)
        }

        // Update UI
        tvTimer.text = "00:00:00"
        tvDistance.text = "0.00"
        tvSpeed.text = "0.0"
        tvCalories.text = "0"
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.create().apply {
                interval = UPDATE_INTERVAL
                fastestInterval = FASTEST_INTERVAL
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

            Log.d(TAG, "Location updates started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates: ${e.message}", e)
        }
    }

    private fun stopLocationUpdates() {
        try {
            if (::locationCallback.isInitialized) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                Log.d(TAG, "Location updates stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates: ${e.message}", e)
        }
    }

    private fun updateLocation(location: Location) {
        try {
            locations.add(location)

            if (locations.size > 1) {
                val previousLocation = locations[locations.size - 2]
                val distance = location.distanceTo(previousLocation)
                totalDistance += distance

                currentSpeed = (location.speed * 3.6f)

                updateStats()

                updateMap(location)
            } else {
                updateMap(location)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location: ${e.message}", e)
        }
    }

    private fun updateStats() {
        try {
            val distanceKm = totalDistance / 1000
            tvDistance.text = String.format("%.2f", distanceKm)

            tvSpeed.text = String.format("%.1f", currentSpeed)

            val calories = calculateCalories(distanceKm)
            tvCalories.text = calories.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stats: ${e.message}", e)
        }
    }

    private fun calculateCalories(distanceKm: Float): Int {
        val caloriesPerKm = when (workoutType.lowercase()) {
            "running" -> 60
            "cycling" -> 30
            else -> 50
        }

        val duration = if (isTracking && !isPaused) {
            ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toFloat() / 3600
        } else 0.0f

        return (distanceKm * caloriesPerKm).roundToInt()
    }

    private fun updateMap(location: Location) {
        try {
            val geoPoint = GeoPoint(location.latitude, location.longitude)

            // Add point to route
            routeOverlay.addPoint(geoPoint)

            // Center map on current location
            if (locations.size == 1) {
                mapView.controller.setCenter(geoPoint)
                mapView.controller.setZoom(18.0)
            }

            mapView.postInvalidate()

            Log.d(TAG, "Map updated with location: $geoPoint")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating map: ${e.message}", e)
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

    private fun updateTimer(millis: Long) {
        try {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = (millis / (1000 * 60 * 60))

            tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating timer: ${e.message}", e)
        }
    }

    private fun saveWorkout() {
        if (currentUserId == 0 || locations.isEmpty()) return

        try {
            val duration = ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toInt()
            val distanceKm = totalDistance / 1000
            val calories = calculateCalories(distanceKm)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val avgSpeed = if (duration > 0) {
                (distanceKm / (duration / 3600.0)).toFloat()
            } else 0.0f

            val workout = Workout(
                userId = currentUserId,
                type = workoutType,
                duration = duration,
                distance = distanceKm.toDouble(),
                calories = calories,
                notes = "GPS Tracked: Avg Speed ${String.format("%.1f", avgSpeed)} km/h",
                date = date,
                timestamp = System.currentTimeMillis(),
                synced = false
            )

            val result = dbHelper.insertWorkout(workout)
            if (result > 0) {
                Toast.makeText(this, "$workoutType saved successfully!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Workout saved: $workoutType, distance: $distanceKm km, duration: $duration sec")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving workout: ${e.message}", e)
            Toast.makeText(this, "Error saving workout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSummaryDialog() {
        try {
            val duration = ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toInt()
            val distanceKm = totalDistance / 1000
            val calories = calculateCalories(distanceKm)

            val message = """
                $workoutType Completed!
                
                Duration: ${tvTimer.text}
                Distance: ${String.format("%.2f", distanceKm)} km
                Avg Speed: ${tvSpeed.text} km/h
                Calories: $calories cal
                
                Route saved to history.
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Workout Summary")
                .setMessage(message)
                .setPositiveButton("Save & Exit") { _, _ ->
                    saveWorkout()
                    finish()
                }
                .setNegativeButton("Discard") { _, _ ->
                    finish()
                }
                .setNeutralButton("Continue") { _, _ ->
                    // Just close dialog, stay on screen
                    showSummaryOrNoWorkouts()
                }
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing summary dialog: ${e.message}", e)
        }
    }

    // Location Permission Methods
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
                Toast.makeText(this, "Location permission required for GPS tracking", Toast.LENGTH_SHORT).show()
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
                .setTitle("Stop Tracking")
                .setMessage("Are you sure you want to stop tracking and exit?")
                .setPositiveButton("Stop & Exit") { _, _ ->
                    stopTracking()
                    super.onBackPressed()
                }
                .setNegativeButton("Continue Tracking", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    // Activity Lifecycle Methods
    override fun onResume() {
        super.onResume()
        try {
            mapView.onResume()
            mapView.invalidate()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}", e)
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
            Log.e(TAG, "Error in onPause: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mapView.onDetach()
            if (::handler.isInitialized) {
                handler.removeCallbacks(timerRunnable)
            }
            stopLocationUpdates()
            dbHelper.closeDB()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}", e)
        }
    }
}