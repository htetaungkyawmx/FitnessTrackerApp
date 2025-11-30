<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

try {
    $database = new Database();
    $db = $database->getConnection();

    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        // Get workouts for user
        $userId = $_GET['user_id'] ?? null;

        if (!$userId || !is_numeric($userId)) {
            sendError("Valid user ID is required");
        }

        $query = "SELECT * FROM workouts WHERE user_id = :user_id ORDER BY date DESC";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $userId, PDO::PARAM_INT);
        $stmt->execute();

        $workouts = [];
        while ($row = $stmt->fetch()) {
            $workouts[] = [
                "id" => (int)$row['id'],
                "user_id" => (int)$row['user_id'],
                "type" => $row['type'],
                "duration" => (int)$row['duration'],
                "calories" => (float)$row['calories'],
                "date" => $row['date'],
                "notes" => $row['notes'] ?: "",
                "distance" => $row['distance'] ? (float)$row['distance'] : null,
                "average_speed" => $row['average_speed'] ? (float)$row['average_speed'] : null,
                "elevation" => $row['elevation'] ? (float)$row['elevation'] : null,
                "exercises" => $row['exercises'] ? json_decode($row['exercises'], true) : []
            ];
        }

        sendSuccess($workouts);

    } elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
        // Save new workout
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input) {
            sendError("Invalid JSON input");
        }

        $required = ['user_id', 'type', 'duration', 'calories', 'date'];
        foreach ($required as $field) {
            if (!isset($input[$field])) {
                sendError("Missing required field: $field");
            }
        }

        $query = "INSERT INTO workouts (user_id, type, duration, calories, date, notes, distance, average_speed, elevation, exercises)
                 VALUES (:user_id, :type, :duration, :calories, :date, :notes, :distance, :average_speed, :elevation, :exercises)";

        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
        $stmt->bindParam(":type", $input['type']);
        $stmt->bindParam(":duration", $input['duration'], PDO::PARAM_INT);
        $stmt->bindParam(":calories", $input['calories']);
        $stmt->bindParam(":date", $input['date']);
        $stmt->bindParam(":notes", $input['notes'] ?? '');
        $stmt->bindParam(":distance", $input['distance'] ?? null);
        $stmt->bindParam(":average_speed", $input['average_speed'] ?? null);
        $stmt->bindParam(":elevation", $input['elevation'] ?? null);
        $stmt->bindParam(":exercises", $input['exercises'] ? json_encode($input['exercises']) : json_encode([]));

        if ($stmt->execute()) {
            $workoutId = $db->lastInsertId();
            $input['id'] = (int)$workoutId;

            sendSuccess($input, "Workout saved successfully");
        } else {
            sendError("Failed to save workout", 500);
        }

    } elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
        // Update workout
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['id'])) {
            sendError("Workout ID is required");
        }

        $query = "UPDATE workouts SET
                 type = :type,
                 duration = :duration,
                 calories = :calories,
                 date = :date,
                 notes = :notes,
                 distance = :distance,
                 average_speed = :average_speed,
                 elevation = :elevation,
                 exercises = :exercises
                 WHERE id = :id AND user_id = :user_id";

        $stmt = $db->prepare($query);
        $stmt->bindParam(":id", $input['id'], PDO::PARAM_INT);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
        $stmt->bindParam(":type", $input['type']);
        $stmt->bindParam(":duration", $input['duration'], PDO::PARAM_INT);
        $stmt->bindParam(":calories", $input['calories']);
        $stmt->bindParam(":date", $input['date']);
        $stmt->bindParam(":notes", $input['notes'] ?? '');
        $stmt->bindParam(":distance", $input['distance'] ?? null);
        $stmt->bindParam(":average_speed", $input['average_speed'] ?? null);
        $stmt->bindParam(":elevation", $input['elevation'] ?? null);
        $stmt->bindParam(":exercises", $input['exercises'] ? json_encode($input['exercises']) : json_encode([]));

        if ($stmt->execute()) {
            sendSuccess($input, "Workout updated successfully");
        } else {
            sendError("Failed to update workout", 500);
        }

    } elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
        // Delete workout
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['id']) || !isset($input['user_id'])) {
            sendError("Workout ID and user ID are required");
        }

        $query = "DELETE FROM workouts WHERE id = :id AND user_id = :user_id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":id", $input['id'], PDO::PARAM_INT);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);

        if ($stmt->execute()) {
            sendSuccess(null, "Workout deleted successfully");
        } else {
            sendError("Failed to delete workout", 500);
        }
    }
} catch (Exception $e) {
    error_log("Workouts API error: " . $e->getMessage());
    sendError("Server error: " . $e->getMessage(), 500);
}
?>
