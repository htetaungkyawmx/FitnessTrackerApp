<?php
include '../db_connection.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"));

    $user_id = intval($data->user_id);
    $activity_type = $conn->real_escape_string($data->activity_type);
    $duration = intval($data->duration);
    $activity_date = $conn->real_escape_string($data->activity_date);

    // Calculate calories based on activity type
    $calories_burned = calculateCalories($activity_type, $duration, $data);

    $sql = "INSERT INTO activities (user_id, activity_type, duration, calories_burned, activity_date";

    // Add activity-specific fields
    if ($activity_type == 'running' || $activity_type == 'cycling') {
        $distance = floatval($data->distance);
        $sql .= ", distance) VALUES ($user_id, '$activity_type', $duration, $calories_burned, '$activity_date', $distance)";
    } else if ($activity_type == 'weightlifting') {
        $weight = floatval($data->weight);
        $sets = intval($data->sets);
        $reps = intval($data->reps);
        $sql .= ", weight, sets, reps) VALUES ($user_id, '$activity_type', $duration, $calories_burned, '$activity_date', $weight, $sets, $reps)";
    }

    if ($conn->query($sql) === TRUE) {
        // Update user stats
        updateUserStats($conn, $user_id, $activity_type, $duration, $data);

        echo json_encode([
            "success" => true,
            "message" => "Activity added successfully",
            "activity_id" => $conn->insert_id
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Failed to add activity: " . $conn->error
        ]);
    }
}

function calculateCalories($type, $duration, $data) {
    // Simplified calorie calculation
    switch($type) {
        case 'running':
            return $duration * 10; // 10 calories per minute
        case 'cycling':
            return $duration * 8; // 8 calories per minute
        case 'weightlifting':
            return $duration * 5; // 5 calories per minute
        default:
            return $duration * 7;
    }
}

function updateUserStats($conn, $user_id, $type, $duration, $data) {
    // Update total workouts
    $conn->query("UPDATE user_stats SET total_workouts = total_workouts + 1 WHERE user_id = $user_id");

    // Update total calories
    $calories = calculateCalories($type, $duration, $data);
    $conn->query("UPDATE user_stats SET total_calories = total_calories + $calories WHERE user_id = $user_id");

    // Update distance for running/cycling
    if (($type == 'running' || $type == 'cycling') && isset($data->distance)) {
        $distance = floatval($data->distance);
        $conn->query("UPDATE user_stats SET total_distance = total_distance + $distance WHERE user_id = $user_id");
    }

    // Update last updated date
    $conn->query("UPDATE user_stats SET last_updated = CURDATE() WHERE user_id = $user_id");
}

$conn->close();
?>