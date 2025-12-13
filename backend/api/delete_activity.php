<?php
require_once 'config.php';

$userId = validateToken($pdo);
if (!$userId) {
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit();
}

$data = json_decode(file_get_contents("php://input"));

if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    if (empty($data->activity_id)) {
        echo json_encode(['success' => false, 'message' => 'Activity ID is required']);
        exit();
    }

    // Verify the activity belongs to the user
    $checkStmt = $pdo->prepare("SELECT id FROM activities WHERE id = ? AND user_id = ?");
    $checkStmt->execute([$data->activity_id, $userId]);

    if ($checkStmt->rowCount() === 0) {
        echo json_encode(['success' => false, 'message' => 'Activity not found or unauthorized']);
        exit();
    }

    $stmt = $pdo->prepare("DELETE FROM activities WHERE id = ?");

    if ($stmt->execute([$data->activity_id])) {
        echo json_encode([
            'success' => true,
            'message' => 'Activity deleted successfully'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to delete activity']);
    }
}
?>