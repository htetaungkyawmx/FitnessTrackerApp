<?php
require_once 'config.php';

$userId = validateToken($pdo);
if (!$userId) {
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit();
}

// Weekly activity summary
$weeklyStmt = $pdo->prepare("
    SELECT
        DAYNAME(created_at) as day,
        COUNT(*) as activity_count,
        SUM(duration_minutes) as total_minutes,
        SUM(calories_burned) as total_calories
    FROM activities
    WHERE user_id = ?
    AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
    GROUP BY DAY(created_at)
    ORDER BY created_at DESC
");

$weeklyStmt->execute([$userId]);
$weeklyStats = $weeklyStmt->fetchAll();

// Activity type distribution
$typeStmt = $pdo->prepare("
    SELECT
        activity_type,
        COUNT(*) as count,
        SUM(duration_minutes) as total_minutes
    FROM activities
    WHERE user_id = ?
    GROUP BY activity_type
");

$typeStmt->execute([$userId]);
$typeStats = $typeStmt->fetchAll();

// Goals progress
$goalsStmt = $pdo->prepare("
    SELECT
        goal_type,
        target_value,
        current_value,
        ROUND((current_value / target_value) * 100, 2) as progress_percentage
    FROM goals
    WHERE user_id = ?
");

$goalsStmt->execute([$userId]);
$goals = $goalsStmt->fetchAll();

echo json_encode([
    'success' => true,
    'weekly_stats' => $weeklyStats,
    'activity_types' => $typeStats,
    'goals' => $goals
]);
?>