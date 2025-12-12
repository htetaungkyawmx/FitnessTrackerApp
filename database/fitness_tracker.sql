-- Database: fitness_tracker
CREATE DATABASE IF NOT EXISTS fitness_tracker;
USE fitness_tracker;

-- Table: users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: activities
CREATE TABLE activities (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    activity_type ENUM('running', 'cycling', 'weightlifting') NOT NULL,
    duration INT NOT NULL COMMENT 'Duration in minutes',
    distance DECIMAL(8,2) COMMENT 'Distance in km (for running/cycling)',
    weight DECIMAL(8,2) COMMENT 'Weight in kg (for weightlifting)',
    sets INT COMMENT 'Number of sets (for weightlifting)',
    reps INT COMMENT 'Number of reps (for weightlifting)',
    calories_burned INT,
    notes TEXT,
    activity_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: goals
CREATE TABLE goals (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_value DECIMAL(10,2) NOT NULL,
    current_value DECIMAL(10,2) DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    deadline DATE,
    status ENUM('active', 'completed', 'failed') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: user_stats
CREATE TABLE user_stats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_workouts INT DEFAULT 0,
    total_calories INT DEFAULT 0,
    total_distance DECIMAL(10,2) DEFAULT 0,
    last_updated DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert sample user (password: 123456)
INSERT INTO users (username, email, password)
VALUES ('Htet Aung Kyaw', 'htet@gmail.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');