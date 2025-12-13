<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    $userId = $_GET['user_id'] ?? '';

    if (empty($userId)) {
        sendResponse(false, "User ID is required");
    }

    // Get today's date
    $today = date('Y-m-d');

    // Get total stats
    $stmt = $conn->prepare("
        SELECT
            COUNT(*) as total_activities,
            SUM(duration) as total_duration,
            SUM(distance) as total_distance,
            SUM(calories) as total_calories
        FROM activities
        WHERE user_id = ? AND DATE(date) = ?
    ");
    $stmt->bind_param("is", $userId, $today);
    $stmt->execute();
    $result = $stmt->get_result();
    $stats = $result->fetch_assoc();

    // Default values if no activities
    $response = [
        "steps" => rand(5000, 15000), // Simulated steps
        "calories" => $stats['total_calories'] ?? 0,
        "distance" => (float)($stats['total_distance'] ?? 0),
        "duration" => $stats['total_duration'] ?? 0,
        "total_activities" => $stats['total_activities'] ?? 0
    ];

    sendResponse(true, "Stats retrieved successfully", $response);
} else {
    sendResponse(false, "Invalid request method");
}
?>