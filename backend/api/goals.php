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
        // Get goals for user
        $userId = $_GET['user_id'] ?? null;

        if (!$userId || !is_numeric($userId)) {
            sendError("Valid user ID is required");
        }

        $query = "SELECT * FROM goals WHERE user_id = :user_id ORDER BY deadline ASC, created_at DESC";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $userId, PDO::PARAM_INT);
        $stmt->execute();

        $goals = [];
        while ($row = $stmt->fetch()) {
            $goals[] = [
                "id" => (int)$row['id'],
                "user_id" => (int)$row['user_id'],
                "title" => $row['title'],
                "description" => $row['description'] ?: "",
                "target_value" => (float)$row['target_value'],
                "current_value" => (float)$row['current_value'],
                "unit" => $row['unit'],
                "deadline" => $row['deadline'],
                "type" => $row['type'],
                "is_completed" => (bool)$row['is_completed'],
                "created_at" => $row['created_at']
            ];
        }

        sendSuccess($goals);

    } elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
        // Create new goal
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input) {
            sendError("Invalid JSON input");
        }

        $required = ['user_id', 'title', 'target_value', 'unit', 'deadline', 'type'];
        foreach ($required as $field) {
            if (!isset($input[$field])) {
                sendError("Missing required field: $field");
            }
        }

        $query = "INSERT INTO goals (user_id, title, description, target_value, current_value, unit, deadline, type, is_completed, created_at)
                 VALUES (:user_id, :title, :description, :target_value, :current_value, :unit, :deadline, :type, :is_completed, NOW())";

        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
        $stmt->bindParam(":title", $input['title']);
        $stmt->bindParam(":description", $input['description'] ?? '');
        $stmt->bindParam(":target_value", $input['target_value']);
        $stmt->bindParam(":current_value", $input['current_value'] ?? 0.0);
        $stmt->bindParam(":unit", $input['unit']);
        $stmt->bindParam(":deadline", $input['deadline']);
        $stmt->bindParam(":type", $input['type']);
        $stmt->bindParam(":is_completed", $input['is_completed'] ?? false, PDO::PARAM_BOOL);

        if ($stmt->execute()) {
            $goalId = $db->lastInsertId();
            $input['id'] = (int)$goalId;
            $input['created_at'] = date('Y-m-d H:i:s');

            sendSuccess($input, "Goal created successfully");
        } else {
            sendError("Failed to create goal", 500);
        }

    } elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
        // Update goal
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['id'])) {
            sendError("Goal ID is required");
        }

        $query = "UPDATE goals SET
                 title = :title,
                 description = :description,
                 target_value = :target_value,
                 current_value = :current_value,
                 unit = :unit,
                 deadline = :deadline,
                 type = :type,
                 is_completed = :is_completed
                 WHERE id = :id AND user_id = :user_id";

        $stmt = $db->prepare($query);
        $stmt->bindParam(":id", $input['id'], PDO::PARAM_INT);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
        $stmt->bindParam(":title", $input['title']);
        $stmt->bindParam(":description", $input['description'] ?? '');
        $stmt->bindParam(":target_value", $input['target_value']);
        $stmt->bindParam(":current_value", $input['current_value']);
        $stmt->bindParam(":unit", $input['unit']);
        $stmt->bindParam(":deadline", $input['deadline']);
        $stmt->bindParam(":type", $input['type']);
        $stmt->bindParam(":is_completed", $input['is_completed'], PDO::PARAM_BOOL);

        if ($stmt->execute()) {
            sendSuccess($input, "Goal updated successfully");
        } else {
            sendError("Failed to update goal", 500);
        }

    } elseif ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
        // Delete goal
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['id']) || !isset($input['user_id'])) {
            sendError("Goal ID and user ID are required");
        }

        $query = "DELETE FROM goals WHERE id = :id AND user_id = :user_id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":id", $input['id'], PDO::PARAM_INT);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);

        if ($stmt->execute()) {
            sendSuccess(null, "Goal deleted successfully");
        } else {
            sendError("Failed to delete goal", 500);
        }
    }
} catch (Exception $e) {
    error_log("Goals API error: " . $e->getMessage());
    sendError("Server error: " . $e->getMessage(), 500);
}
?>
