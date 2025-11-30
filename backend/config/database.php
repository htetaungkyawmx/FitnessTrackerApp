<?php
class Database {
    private $host = "localhost";
    private $db_name = "fitness_tracker";
    private $username = "root";
    private $password = "";
    public $conn;

    public function getConnection() {
        $this->conn = null;
        try {
            $this->conn = new PDO(
                "mysql:host=" . $this->host . ";dbname=" . $this->db_name,
                $this->username,
                $this->password
            );
            $this->conn->exec("set names utf8");
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->conn->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
            $this->conn->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
        } catch(PDOException $exception) {
            error_log("Connection error: " . $exception->getMessage());
            throw new Exception("Database connection failed");
        }
        return $this->conn;
    }

    public function closeConnection() {
        $this->conn = null;
    }
}

// Helper function for API responses
function sendResponse($success, $data = null, $error = null, $message = null) {
    header('Content-Type: application/json');
    $response = ['success' => $success];

    if ($data !== null) {
        $response['data'] = $data;
    }

    if ($error !== null) {
        $response['error'] = $error;
    }

    if ($message !== null) {
        $response['message'] = $message;
    }

    echo json_encode($response);
    exit;
}

// Helper function for error responses
function sendError($error, $code = 400) {
    http_response_code($code);
    sendResponse(false, null, $error);
}

// Helper function for success responses
function sendSuccess($data = null, $message = null) {
    sendResponse(true, $data, null, $message);
}
?>
