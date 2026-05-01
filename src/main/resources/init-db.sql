-- Initialize MySQL database for POS System
-- This script runs when the MySQL container starts for the first time

CREATE DATABASE IF NOT EXISTS pos_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pos_system;

-- Create user if not exists
CREATE USER IF NOT EXISTS 'tbricks'@'%' IDENTIFIED BY 'DrA7#K1i$';
GRANT ALL PRIVILEGES ON pos_system.* TO 'tbricks'@'%';
FLUSH PRIVILEGES;

-- Set timezone
SET time_zone = '+00:00';
