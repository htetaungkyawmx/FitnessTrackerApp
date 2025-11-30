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
        $email = $input['email'] ?? '';
        $password = $input['password'] ?? '';

        // Validation
        if (empty($username) || empty($email) || empty($password)) {
            sendError("All fields are required");
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            sendError("Invalid email format");
        }

        if (strlen($username) < 3) {
            sendError("Username must be at least 3 characters long");
        }

        if (strlen($password) < 6) {
            sendError("Password must be at least 6 characters long");
        }

        $database = new Database();
        $db = $database->getConnection();

        // Check if username or email already exists
        $checkQuery = "SELECT id FROM users WHERE username = :username OR email = :email";
        $checkStmt = $db->prepare($checkQuery);
        $checkStmt->bindParam(":username", $username);
        $checkStmt->bindParam(":email", $email);
        $checkStmt->execute();

        if ($checkStmt->rowCount() > 0) {
            sendError("Username or email already exists", 409);
        }

        // Insert new user
        $insertQuery = "INSERT INTO users (username, email, password, created_at)
                       VALUES (:username, :email, :password, NOW())";
        $insertStmt = $db->prepare($insertQuery);
        $insertStmt->bindParam(":username", $username);
        $insertStmt->bindParam(":email", $email);
        $insertStmt->bindParam(":password", $password);

        if (!$insertStmt->execute()) {
            sendError("Failed to create user account", 500);
        }

        $userId = $db->lastInsertId();

        // Get the created user
        $userQuery = "SELECT id, username, email, created_at FROM users WHERE id = :id";
        $userStmt = $db->prepare($userQuery);
        $userStmt->bindParam(":id", $userId);
        $userStmt->execute();

        $user = $userStmt->fetch();
        $user['id'] = (int)$user['id'];

        sendSuccess($user, "Registration successful");

    } catch (Exception $e) {
        error_log("Registration error: " . $e->getMessage());
        sendError("Server error: " . $e->getMessage(), 500);
    }
} else {
    sendError("Method not allowed", 405);
}
?>
