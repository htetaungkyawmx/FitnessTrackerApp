<?php
require_once 'config.php';

$userId = validateToken($pdo);
if (!$userId) {
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit();
}

$data = json_decode(file_get_contents("php://input"));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $required = ['activity_type', 'duration_minutes'];
    foreach ($required as $field) {
        if (empty($data->$field)) {
            echo json_encode(['success' => false, 'message' => "$field is required"]);
            exit();
        }
    }

    // Calculate calories if not provided
    $calories = $data->calories_burned ?? calculateCalories($data->activity_type, $data->duration_minutes, $data->distance_km ?? 0);

    $stmt = $pdo->prepare("
        INSERT INTO activities (user_id, activity_type, duration_minutes, distance_km, calories_burned, notes)
        VALUES (?, ?, ?, ?, ?, ?)
    ");

    if ($stmt->execute([
        $userId,
        $data->activity_type,
        $data->duration_minutes,
        $data->distance_km ?? null,
        $calories,
        $data->notes ?? ''
    ])) {
        echo json_encode([
            'success' => true,
            'message' => 'Activity logged successfully',
            'activity_id' => $pdo->lastInsertId()
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to log activity']);
    }
}

function calculateCalories($type, $duration, $distance) {
    // Simple calorie calculation
    $caloriesPerMinute = [
        'running' => 10,
        'cycling' => 8,
        'weightlifting' => 5,
        'swimming' => 7,
        'yoga' => 3,
        'walking' => 4
    ];

    $base = $caloriesPerMinute[$type] ?? 5;
    return $base * $duration;
}
?>