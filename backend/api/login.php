<?php
require_once 'config.php';

$data = json_decode(file_get_contents("php://input"));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (empty($data->username) || empty($data->password)) {
        echo json_encode(['success' => false, 'message' => 'Username and password are required']);
        exit();
    }

    // Find user
    $stmt = $pdo->prepare("SELECT id, username, email, password_hash FROM users WHERE username = ? OR email = ?");
    $stmt->execute([$data->username, $data->username]);
    $user = $stmt->fetch();

    if (!$user || !password_verify($data->password, $user['password_hash'])) {
        echo json_encode(['success' => false, 'message' => 'Invalid credentials']);
        exit();
    }

    // Generate new token
    $token = bin2hex(random_bytes(32));
    $tokenStmt = $pdo->prepare("INSERT INTO user_tokens (user_id, token, expires_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 7 DAY))");
    $tokenStmt->execute([$user['id'], $token]);

    echo json_encode([
        'success' => true,
        'message' => 'Login successful',
        'user' => [
            'id' => $user['id'],
            'username' => $user['username'],
            'email' => $user['email']
        ],
        'token' => $token
    ]);
}
?>