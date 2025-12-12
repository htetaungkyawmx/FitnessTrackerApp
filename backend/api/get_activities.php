<?php
include '../db_connection.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 50;

    $sql = "SELECT * FROM activities WHERE user_id = $user_id ORDER BY activity_date DESC LIMIT $limit";
    $result = $conn->query($sql);

    $activities = [];
    while($row = $result->fetch_assoc()) {
        $activities[] = $row;
    }

    echo json_encode([
        "success" => true,
        "activities" => $activities,
        "count" => count($activities)
    ]);
}
$conn->close();
?>