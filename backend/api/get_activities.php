<?php
require_once 'config.php';

$userId = validateToken($pdo);
if (!$userId) {
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit();
}

$stmt = $pdo->prepare("
    SELECT a.*,
           DATE_FORMAT(a.created_at, '%Y-%m-%d %H:%i:%s') as formatted_date,
           CASE
               WHEN a.activity_type = 'running' THEN '🏃 Running'
               WHEN a.activity_type = 'cycling' THEN '🚴 Cycling'
               WHEN a.activity_type = 'weightlifting' THEN '🏋️ Weightlifting'
               ELSE a.activity_type
           END as display_name
    FROM activities a
    WHERE a.user_id = ?
    ORDER BY a.created_at DESC
    LIMIT 50
");

$stmt->execute([$userId]);
$activities = $stmt->fetchAll();

// Get statistics
$statsStmt = $pdo->prepare("
    SELECT
        COUNT(*) as total_activities,
        SUM(duration_minutes) as total_minutes,
        SUM(calories_burned) as total_calories,
        AVG(duration_minutes) as avg_duration
    FROM activities
    WHERE user_id = ?
");

$statsStmt->execute([$userId]);
$stats = $statsStmt->fetch();

echo json_encode([
    'success' => true,
    'activities' => $activities,
    'statistics' => $stats
]);
?>