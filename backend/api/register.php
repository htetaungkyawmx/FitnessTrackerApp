<?php
include '../db_connection.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"));

    $username = $conn->real_escape_string($data->username);
    $email = $conn->real_escape_string($data->email);
    $password = password_hash($data->password, PASSWORD_DEFAULT);

    // Check if user exists
    $checkQuery = "SELECT * FROM users WHERE email = '$email' OR username = '$username'";
    $checkResult = $conn->query($checkQuery);

    if ($checkResult->num_rows > 0) {
        echo json_encode([
            "success" => false,
            "message" => "User already exists"
        ]);
    } else {
        $sql = "INSERT INTO users (username, email, password)
                VALUES ('$username', '$email', '$password')";

        if ($conn->query($sql) === TRUE) {
            // Create user stats entry
            $user_id = $conn->insert_id;
            $stats_sql = "INSERT INTO user_stats (user_id) VALUES ($user_id)";
            $conn->query($stats_sql);

            echo json_encode([
                "success" => true,
                "message" => "Registration successful",
                "user_id" => $user_id
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "Registration failed: " . $conn->error
            ]);
        }
    }
}
$conn->close();
?>