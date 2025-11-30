<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, PUT, OPTIONS");
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
        // Get user profile
        $userId = $_GET['user_id'] ?? null;

        if (!$userId || !is_numeric($userId)) {
            sendError("Valid user ID is required");
        }

        $query = "SELECT id, username, email, height, weight, age, created_at, last_login
                  FROM users WHERE id = :user_id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $userId, PDO::PARAM_INT);
        $stmt->execute();

        if ($stmt->rowCount() === 0) {
            sendError("User not found", 404);
        }

        $user = $stmt->fetch();
        $userData = [
            "id" => (int)$user['id'],
            "username" => $user['username'],
            "email" => $user['email'],
            "height" => $user['height'] ? (float)$user['height'] : null,
            "weight" => $user['weight'] ? (float)$user['weight'] : null,
            "age" => $user['age'] ? (int)$user['age'] : null,
            "created_at" => $user['created_at'],
            "last_login" => $user['last_login']
        ];

        sendSuccess($userData);

    } elseif ($_SERVER['REQUEST_METHOD'] === 'PUT') {
        // Update user profile
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['user_id'])) {
            sendError("User ID is required");
        }

        $query = "UPDATE users SET
                 height = :height,
                 weight = :weight,
                 age = :age
                 WHERE id = :user_id";

        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
        $stmt->bindParam(":height", $input['height'] ?? null);
        $stmt->bindParam(":weight", $input['weight'] ?? null);
        $stmt->bindParam(":age", $input['age'] ?? null);

        if ($stmt->execute()) {
            // Get updated user data
            $userQuery = "SELECT id, username, email, height, weight, age, created_at, last_login
                         FROM users WHERE id = :user_id";
            $userStmt = $db->prepare($userQuery);
            $userStmt->bindParam(":user_id", $input['user_id'], PDO::PARAM_INT);
            $userStmt->execute();

            $user = $userStmt->fetch();
            $userData = [
                "id" => (int)$user['id'],
                "username" => $user['username'],
                "email" => $user['email'],
                "height" => $user['height'] ? (float)$user['height'] : null,
                "weight" => $user['weight'] ? (float)$user['weight'] : null,
                "age" => $user['age'] ? (int)$user['age'] : null,
                "created_at" => $user['created_at'],
                "last_login" => $user['last_login']
            ];

            sendSuccess($userData, "Profile updated successfully");
        } else {
            sendError("Failed to update profile", 500);
        }
    }
} catch (Exception $e) {
    error_log("User API error: " . $e->getMessage());
    sendError("Server error: " . $e->getMessage(), 500);
}
?>
