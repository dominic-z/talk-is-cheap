CREATE DATABASE IF NOT EXISTS travel_mouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel_mouse;

CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    category VARCHAR(50),
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE travel_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE daily_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    travel_plan_id BIGINT NOT NULL,
    plan_date DATE NOT NULL,
    sort_order INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    FOREIGN KEY (travel_plan_id) REFERENCES travel_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE destinations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_plan_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    category VARCHAR(50),
    note_text TEXT,
    arrive_time TIME,
    duration_minutes INT,
    leave_time TIME,
    sort_order INT NOT NULL DEFAULT 0,
    in_route BOOLEAN NOT NULL DEFAULT FALSE,
    route_order INT,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE destination_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destination_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE route_segments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_plan_id BIGINT NOT NULL,
    from_dest_id BIGINT NOT NULL,
    to_dest_id BIGINT NOT NULL,
    transport_mode VARCHAR(20) NOT NULL,
    duration_minutes INT NOT NULL,
    distance_meters INT NOT NULL,
    route_order INT NOT NULL,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (from_dest_id) REFERENCES destinations(id) ON DELETE CASCADE,
    FOREIGN KEY (to_dest_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
