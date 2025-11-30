-- Fitness Tracker Database Schema
-- Version: 1.0
-- Created: 2025

-- Create database
CREATE DATABASE IF NOT EXISTS fitness_tracker;
USE fitness_tracker;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    height DECIMAL(5,2) NULL COMMENT 'Height in centimeters',
    weight DECIMAL(5,2) NULL COMMENT 'Weight in kilograms',
    age INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Workouts table
CREATE TABLE IF NOT EXISTS workouts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('RUNNING', 'CYCLING', 'WEIGHTLIFTING') NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    calories DECIMAL(8,2) NOT NULL COMMENT 'Calories burned',
    date DATETIME NOT NULL,
    notes TEXT,
    distance DECIMAL(6,2) NULL COMMENT 'Distance in kilometers',
    average_speed DECIMAL(5,2) NULL COMMENT 'Average speed in km/h',
    elevation DECIMAL(6,2) NULL COMMENT 'Elevation in meters',
    exercises JSON NULL COMMENT 'JSON array of exercises for weightlifting',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_date (date),
    INDEX idx_type (type),
    INDEX idx_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Goals table
CREATE TABLE IF NOT EXISTS goals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    target_value DECIMAL(8,2) NOT NULL,
    current_value DECIMAL(8,2) DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    deadline DATE NOT NULL,
    type ENUM('WEIGHT_LOSS', 'DISTANCE', 'WORKOUT_COUNT', 'CALORIES') NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_deadline (deadline),
    INDEX idx_completed (is_completed),
    INDEX idx_user_completed (user_id, is_completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO users (username, email, password, height, weight, age) VALUES
('demo', 'demo@example.com', 'password', 175.0, 70.0, 25),
('john_doe', 'john@example.com', 'password123', 180.0, 75.0, 30),
('jane_smith', 'jane@example.com', 'securepass', 165.0, 60.0, 28),
('mike_wilson', 'mike@example.com', 'mikepass', 182.0, 80.0, 35);

INSERT INTO workouts (user_id, type, duration, calories, date, distance, average_speed, notes) VALUES
(1, 'RUNNING', 30, 300.0, NOW() - INTERVAL 1 DAY, 5.0, 10.0, 'Morning run in the park'),
(1, 'CYCLING', 45, 400.0, NOW() - INTERVAL 2 DAY, 15.0, 20.0, 'Evening cycling session'),
(1, 'WEIGHTLIFTING', 60, 250.0, NOW() - INTERVAL 3 DAY, NULL, NULL, 'Chest and triceps workout'),
(2, 'RUNNING', 25, 250.0, NOW() - INTERVAL 1 DAY, 4.0, 9.6, 'Interval training'),
(2, 'CYCLING', 60, 500.0, NOW() - INTERVAL 4 DAY, 25.0, 25.0, 'Long distance cycling'),
(3, 'WEIGHTLIFTING', 45, 200.0, NOW() - INTERVAL 2 DAY, NULL, NULL, 'Leg day'),
(3, 'RUNNING', 35, 350.0, NOW() - INTERVAL 5 DAY, 6.0, 10.3, 'Trail running'),
(4, 'CYCLING', 30, 300.0, NOW() - INTERVAL 3 DAY, 12.0, 24.0, 'Mountain biking');

-- Insert sample exercises for weightlifting workouts
UPDATE workouts SET exercises = '[
    {"name": "Bench Press", "sets": 3, "reps": 10, "weight": 60.0},
    {"name": "Incline Press", "sets": 3, "reps": 12, "weight": 45.0},
    {"name": "Tricep Pushdown", "sets": 3, "reps": 15, "weight": 25.0}
]' WHERE id = 3;

UPDATE workouts SET exercises = '[
    {"name": "Squats", "sets": 4, "reps": 8, "weight": 80.0},
    {"name": "Lunges", "sets": 3, "reps": 12, "weight": 20.0},
    {"name": "Leg Press", "sets": 3, "reps": 10, "weight": 100.0}
]' WHERE id = 6;

INSERT INTO goals (user_id, title, description, target_value, current_value, unit, deadline, type) VALUES
(1, 'Lose 5kg', 'Weight loss goal for summer vacation', 5.0, 2.5, 'kg', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'WEIGHT_LOSS'),
(1, 'Run 50km', 'Monthly running distance goal', 50.0, 25.0, 'km', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'DISTANCE'),
(1, 'Complete 20 Workouts', 'Monthly workout frequency goal', 20.0, 8.0, 'workouts', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'WORKOUT_COUNT'),
(2, 'Burn 5000 Calories', 'Monthly calorie burn goal', 5000.0, 1500.0, 'calories', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'CALORIES'),
(2, 'Cycle 100km', 'Monthly cycling distance goal', 100.0, 40.0, 'km', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'DISTANCE'),
(3, 'Lose 3kg', 'Weight loss goal for wedding', 3.0, 1.0, 'kg', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'WEIGHT_LOSS'),
(4, 'Complete 15 Workouts', 'Monthly consistency goal', 15.0, 5.0, 'workouts', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'WORKOUT_COUNT');

-- Create a view for user statistics
CREATE VIEW user_statistics AS
SELECT
    u.id as user_id,
    u.username,
    COUNT(w.id) as total_workouts,
    COALESCE(SUM(w.calories), 0) as total_calories,
    COALESCE(SUM(w.duration), 0) as total_duration,
    COALESCE(SUM(CASE WHEN w.type = 'RUNNING' THEN w.distance ELSE 0 END), 0) as total_running_distance,
    COALESCE(SUM(CASE WHEN w.type = 'CYCLING' THEN w.distance ELSE 0 END), 0) as total_cycling_distance,
    COUNT(g.id) as total_goals,
    SUM(CASE WHEN g.is_completed = 1 THEN 1 ELSE 0 END) as completed_goals
FROM users u
LEFT JOIN workouts w ON u.id = w.user_id
LEFT JOIN goals g ON u.id = g.user_id
GROUP BY u.id, u.username;

-- Create a view for weekly progress
CREATE VIEW weekly_progress AS
SELECT
    u.id as user_id,
    YEARWEEK(w.date) as week_number,
    COUNT(w.id) as weekly_workouts,
    COALESCE(SUM(w.calories), 0) as weekly_calories,
    COALESCE(SUM(w.duration), 0) as weekly_duration
FROM users u
LEFT JOIN workouts w ON u.id = w.user_id AND w.date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY u.id, YEARWEEK(w.date);

-- Create stored procedure for getting user dashboard data
DELIMITER //
CREATE PROCEDURE GetUserDashboard(IN user_id INT)
BEGIN
    -- Basic user info
    SELECT username, email, height, weight, age, created_at, last_login
    FROM users WHERE id = user_id;

    -- Recent workouts (last 5)
    SELECT * FROM workouts
    WHERE user_id = user_id
    ORDER BY date DESC
    LIMIT 5;

    -- Active goals
    SELECT * FROM goals
    WHERE user_id = user_id AND is_completed = FALSE
    ORDER BY deadline ASC;

    -- Statistics
    SELECT * FROM user_statistics WHERE user_id = user_id;
END //
DELIMITER ;

-- Create trigger to update goal completion status
DELIMITER //
CREATE TRIGGER UpdateGoalCompletion
BEFORE UPDATE ON goals
FOR EACH ROW
BEGIN
    IF NEW.current_value >= NEW.target_value THEN
        SET NEW.is_completed = TRUE;
    ELSE
        SET NEW.is_completed = FALSE;
    END IF;
END //
DELIMITER ;

-- Create event to clean up old data (optional)
DELIMITER //
CREATE EVENT IF NOT EXISTS CleanupOldWorkouts
ON SCHEDULE EVERY 1 MONTH
DO
BEGIN
    DELETE FROM workouts WHERE date < DATE_SUB(NOW(), INTERVAL 2 YEAR);
END //
DELIMITER ;

-- Create indexes for better performance
CREATE INDEX idx_workouts_user_date_type ON workouts(user_id, date, type);
CREATE INDEX idx_goals_user_deadline ON goals(user_id, deadline);
CREATE INDEX idx_users_username_email ON users(username, email);

-- Print success message
SELECT 'Database schema created successfully!' as message;
