<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input) {
            sendError("Invalid JSON input");
        }

        $username = $input['username'] ?? '';
        $password = $input['password'] ?? '';

        if (empty($username) || empty($password)) {
            sendError("Username and password are required");
        }

        $database = new Database();
        $db = $database->getConnection();

        // Query to get user by username
        $query = "SELECT id, username, email, password, height, weight, age, created_at, last_login
                  FROM users WHERE username = :username OR email = :username";
        $stmt = $db->prepare($query);
        $stmt->bindParam(":username", $username);
        $stmt->execute();

        if ($stmt->rowCount() === 0) {
            sendError("User not found", 404);
        }

        $user = $stmt->fetch();

        // In production, use password_verify() with hashed passwords
        // For demo purposes, we're using plain text comparison
        if ($password !== $user['password']) {
            sendError("Invalid password", 401);
        }

        // Update last login time
        $updateQuery = "UPDATE users SET last_login = NOW() WHERE id = :id";
        $updateStmt = $db->prepare($updateQuery);
        $updateStmt->bindParam(":id", $user['id']);
        $updateStmt->execute();

        // Prepare user data for response (excluding password)
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

        sendSuccess($userData, "Login successful");

    } catch (Exception $e) {
        error_log("Login error: " . $e->getMessage());
        sendError("Server error: " . $e->getMessage(), 500);
    }
} else {
    sendError("Method not allowed", 405);
}
?>
