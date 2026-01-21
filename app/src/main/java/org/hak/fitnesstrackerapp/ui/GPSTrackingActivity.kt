package org.hak.fitnesstrackerapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import org.hak.fitnesstrackerapp.R
import org.hak.fitnesstrackerapp.adapters.WorkoutHistoryAdapter
import org.hak.fitnesstrackerapp.database.SQLiteHelper
import org.hak.fitnesstrackerapp.model.Workout
import org.hak.fitnesstrackerapp.utils.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GPSTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
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
    private lateinit var tvLastWorkout: TextView
    private lateinit var noWorkoutsCard: androidx.cardview.widget.CardView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private var isPaused = false

    private var totalDistance = 0.0f
    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var totalPauseTime: Long = 0
    private val locations = mutableListOf<Location>()
    private var currentSpeed = 0.0f

    private lateinit var handler: Handler
    private lateinit var timerRunnable: Runnable

    private lateinit var dbHelper: SQLiteHelper
    private var currentUserId: Int = 0
    private var workoutType: String = ""

    private lateinit var polylineOptions: PolylineOptions

    private var recentWorkouts = listOf<Workout>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val UPDATE_INTERVAL = 1000L
        private const val FASTEST_INTERVAL = 500L
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_tracking)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        workoutType = intent.getStringExtra("WORKOUT_TYPE") ?: "Running"
        val animationRes = intent.getIntExtra("ANIMATION_RES", -1)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$workoutType Tracking"

        polylineOptions = PolylineOptions().width(10f).color(ContextCompat.getColor(this, R.color.primary))

        dbHelper = SQLiteHelper(this)

        val user = SharedPrefManager.getInstance(this).user
        currentUserId = user?.id ?: 0

        if (currentUserId == 0) {
            finish()
            return
        }

        initViews()

        checkRecentWorkoutsAndSetup()

        setupAnimation(animationRes)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        handler = Handler(Looper.getMainLooper())

        setupClickListeners()

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

    private fun checkRecentWorkoutsAndSetup() {

        recentWorkouts = dbHelper.getRecentWorkoutsByType(currentUserId, workoutType, 30)

        Log.d(TAG, "Found ${recentWorkouts.size} recent workouts for $workoutType")

        if (recentWorkouts.isNotEmpty()) {
            showRecentWorkoutsSummary(recentWorkouts)
            summaryCard.visibility = View.VISIBLE
            noWorkoutsCard.visibility = View.GONE

            showRecentWorkoutsDialog()
        } else {
            summaryCard.visibility = View.GONE
            noWorkoutsCard.visibility = View.VISIBLE

            Toast.makeText(this, "No recent $workoutType workouts found. Start your first one!", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRecentWorkoutsDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Recent $workoutType Workouts Found")
            .setMessage("You have ${recentWorkouts.size} recent $workoutType workouts.\n\nWould you like to:\n1. Start GPS tracking now\n2. View workout history\n3. Add new workout manually?")
            .setPositiveButton("Start GPS") { dialog, which ->
                startTracking()
            }
            .setNegativeButton("View History") { dialog, which ->
                showWorkoutHistoryDialog(recentWorkouts)
            }
            .setNeutralButton("Add Manual") { dialog, which ->
                showAddManualWorkoutDialog()
            }
            .show()
    }

    private fun showAddManualWorkoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout, null)

        val spWorkoutType = dialogView.findViewById<android.widget.Spinner>(R.id.spWorkoutType)
        val etDuration = dialogView.findViewById<android.widget.EditText>(R.id.etDuration)
        val etDistance = dialogView.findViewById<android.widget.EditText>(R.id.etDistance)
        val etCalories = dialogView.findViewById<android.widget.EditText>(R.id.etCalories)
        val etNotes = dialogView.findViewById<android.widget.EditText>(R.id.etNotes)
        val etDate = dialogView.findViewById<android.widget.EditText>(R.id.etDate)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(today)

        val workoutTypes = arrayOf("Running", "Cycling", "Swimming", "Walking", "Weightlifting", "Yoga", "HIIT", "Other")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, workoutTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spWorkoutType.adapter = adapter

        val position = workoutTypes.indexOfFirst { it.equals(workoutType, ignoreCase = true) }
        if (position >= 0) {
            spWorkoutType.setSelection(position)
        }

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Add Manual Workout")
            .setView(dialogView)
            .setPositiveButton("Save") { dialogInterface, which ->
                saveManualWorkout(
                    spWorkoutType.selectedItem.toString(),
                    etDuration.text.toString(),
                    etDistance.text.toString(),
                    etCalories.text.toString(),
                    etNotes.text.toString(),
                    etDate.text.toString()
                )
            }
            .setNegativeButton("Cancel") { dialog, which ->
                checkRecentWorkoutsAndSetup()
            }
            .show()
    }

    private fun saveManualWorkout(
        type: String,
        durationStr: String,
        distanceStr: String,
        caloriesStr: String,
        notes: String,
        date: String
    ) {
        val duration = durationStr.toIntOrNull() ?: 0
        val distance = distanceStr.toDoubleOrNull()
        val calories = caloriesStr.toIntOrNull() ?: 0

        val workout = Workout(
            userId = currentUserId,
            type = type,
            duration = duration,
            distance = distance,
            calories = calories,
            notes = notes,
            date = date,
            timestamp = System.currentTimeMillis(),
            synced = false
        )

        val result = dbHelper.insertWorkout(workout)
        if (result > 0) {
            Toast.makeText(this, "$type workout added!", Toast.LENGTH_SHORT).show()
            checkRecentWorkoutsAndSetup()
        }
    }

    private fun setupAnimation(animationRes: Int) {
        val defaultAnimation = when (workoutType.lowercase()) {
            "running" -> R.raw.running_animation
            "cycling" -> R.raw.cycling_animation
            "walking" -> R.raw.yoga_animation
            "swimming" -> R.raw.swimming_animation
            else -> -1
        }

        val finalAnimationRes = if (animationRes != -1) animationRes else defaultAnimation

        if (finalAnimationRes != -1) {
            try {
                animationView.setAnimation(finalAnimationRes)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading animation: ${e.message}")
                animationView.visibility = View.GONE
            }
        } else {
            animationView.visibility = View.GONE
        }
    }

    private fun showRecentWorkoutsSummary(workouts: List<Workout>) {
        tvWorkoutCount.text = "${workouts.size}"

        val totalDuration = workouts.sumOf { it.duration }
        tvTotalDuration.text = "${totalDuration} min"

        val lastWorkout = workouts.maxByOrNull { it.timestamp }
        lastWorkout?.let {
            tvLastWorkout.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.timestamp))
        } ?: run {
            tvLastWorkout.text = "N/A"
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
            showWorkoutHistoryDialog(recentWorkouts)
        }

        noWorkoutsCard.setOnClickListener {
            startTracking()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (!hasLocationPermission()) {
            checkLocationPermission()
            return
        }

        resetStats()

        isTracking = true
        isPaused = false
        startTime = System.currentTimeMillis()

        fabStartStop.text = "STOP"
        fabPause.visibility = View.VISIBLE

        animationView.visibility = View.VISIBLE
        animationView.playAnimation()
        summaryCard.visibility = View.GONE
        noWorkoutsCard.visibility = View.GONE

        startLocationUpdates()

        startTimer()

        Toast.makeText(this, "Started $workoutType tracking", Toast.LENGTH_SHORT).show()
    }

    private fun pauseTracking() {
        isPaused = true
        pauseTime = System.currentTimeMillis()
        fabPause.text = "▶"
        animationView.pauseAnimation()
        stopLocationUpdates()

        Toast.makeText(this, "Tracking paused", Toast.LENGTH_SHORT).show()
    }

    private fun resumeTracking() {
        isPaused = false
        totalPauseTime += System.currentTimeMillis() - pauseTime
        fabPause.text = "⏸"
        animationView.resumeAnimation()
        startLocationUpdates()

        Toast.makeText(this, "Tracking resumed", Toast.LENGTH_SHORT).show()
    }

    private fun stopTracking() {
        isTracking = false
        isPaused = false

        fabStartStop.text = "START"
        fabPause.visibility = View.GONE
        animationView.visibility = View.GONE
        animationView.cancelAnimation()

        stopLocationUpdates()
        handler.removeCallbacks(timerRunnable)

        saveWorkout()

        showSummaryDialog()
    }

    private fun resetStats() {
        totalDistance = 0.0f
        totalPauseTime = 0
        currentSpeed = 0.0f
        locations.clear()
        polylineOptions.points.clear()

        if (::googleMap.isInitialized) {
            googleMap.clear()
        }

        tvTimer.text = "00:00:00"
        tvDistance.text = "0.00"
        tvSpeed.text = "0.0"
        tvCalories.text = "0"
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
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
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateLocation(location: Location) {
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
    }

    private fun updateStats() {
        val distanceKm = totalDistance / 1000
        tvDistance.text = String.format("%.2f", distanceKm)

        tvSpeed.text = String.format("%.1f", currentSpeed)

        val calories = calculateCalories(distanceKm)
        tvCalories.text = calories.toString()
    }

    private fun calculateCalories(distanceKm: Float): Int {
        val caloriesPerKm = when (workoutType.lowercase()) {
            "running" -> 60
            "cycling" -> 30
            "walking" -> 40
            "swimming" -> 70
            "hiking" -> 55
            else -> 50
        }

        val duration = if (isTracking && !isPaused) {
            ((System.currentTimeMillis() - startTime - totalPauseTime) / 1000).toFloat() / 3600
        } else 0.0f

        return (distanceKm * caloriesPerKm).roundToInt()
    }

    private fun updateMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        polylineOptions.add(latLng)
        googleMap.addPolyline(polylineOptions)

        if (locations.size == 1) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            googleMap.animateCamera(cameraUpdate)
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
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun saveWorkout() {
        if (currentUserId == 0 || locations.isEmpty()) return

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
        }
    }

    private fun showSummaryDialog() {
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

        android.app.AlertDialog.Builder(this)
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
                checkRecentWorkoutsAndSetup()
            }
            .show()
    }

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
        } else {
            if (::googleMap.isInitialized) {
                @SuppressLint("MissingPermission")
                googleMap.isMyLocationEnabled = true
            }
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
                if (::googleMap.isInitialized) {
                    @SuppressLint("MissingPermission")
                    googleMap.isMyLocationEnabled = true
                }
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission required for GPS tracking", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        if (hasLocationPermission()) {
            @SuppressLint("MissingPermission")
            googleMap.isMyLocationEnabled = true
        }

        val defaultLocation = LatLng(16.8409, 96.1735) // Yangon, Myanmar
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f)
        googleMap.moveCamera(cameraUpdate)
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (isTracking && !isPaused) {
            pauseTracking()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        if (::handler.isInitialized) {
            handler.removeCallbacks(timerRunnable)
        }
        stopLocationUpdates()
        dbHelper.closeDB()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}