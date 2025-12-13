<?php
require_once 'config.php';

$data = json_decode(file_get_contents("php://input"));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (empty($data->username) || empty($data->email) || empty($data->password)) {
        echo json_encode(['success' => false, 'message' => 'All fields are required']);
        exit();
    }

    // Check if user exists
    $checkStmt = $pdo->prepare("SELECT id FROM users WHERE username = ? OR email = ?");
    $checkStmt->execute([$data->username, $data->email]);

    if ($checkStmt->rowCount() > 0) {
        echo json_encode(['success' => false, 'message' => 'Username or email already exists']);
        exit();
    }

    // Hash password
    $hashedPassword = password_hash($data->password, PASSWORD_BCRYPT);

    // Insert user
    $stmt = $pdo->prepare("INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)");

    if ($stmt->execute([$data->username, $data->email, $hashedPassword])) {
        $userId = $pdo->lastInsertId();

        // Generate token
        $token = bin2hex(random_bytes(32));
        $tokenStmt = $pdo->prepare("INSERT INTO user_tokens (user_id, token, expires_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 7 DAY))");
        $tokenStmt->execute([$userId, $token]);

        echo json_encode([
            'success' => true,
            'message' => 'Registration successful',
            'user' => [
                'id' => $userId,
                'username' => $data->username,
                'email' => $data->email
            ],
            'token' => $token
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Registration failed']);
    }
}
?>