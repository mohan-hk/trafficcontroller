CREATE TABLE directions (
    direction_id INT AUTO_INCREMENT PRIMARY KEY,
    direction_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(100)
);

CREATE TABLE signal_phases (
    phase_id INT AUTO_INCREMENT PRIMARY KEY,
    phase_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    duration_seconds INT DEFAULT 60,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE phase_allowed_directions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phase_id INT NOT NULL,
    direction_id INT NOT NULL,
    CONSTRAINT fk_pad_phase FOREIGN KEY (phase_id) REFERENCES signal_phases(phase_id) ON DELETE CASCADE,
    CONSTRAINT fk_pad_direction FOREIGN KEY (direction_id) REFERENCES directions(direction_id) ON DELETE CASCADE,
    CONSTRAINT unique_phase_direction UNIQUE (phase_id, direction_id)
);

CREATE TABLE direction_conflicts (
    conflict_id INT AUTO_INCREMENT PRIMARY KEY,
    direction_id INT NOT NULL,
    conflicts_with_direction_id INT NOT NULL,
    conflict_reason VARCHAR(200),
    CONSTRAINT fk_dc_direction FOREIGN KEY (direction_id) REFERENCES directions(direction_id) ON DELETE CASCADE,
    CONSTRAINT fk_dc_conflicts FOREIGN KEY (conflicts_with_direction_id) REFERENCES directions(direction_id) ON DELETE CASCADE,
    CONSTRAINT unique_conflict UNIQUE (direction_id, conflicts_with_direction_id)
);

CREATE TABLE traffic_history (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     timestamp TIMESTAMP NOT NULL,
     event_type VARCHAR(50),
     details VARCHAR(500)
);

-- 1. DIRECTIONS
INSERT INTO directions (direction_name, description) VALUES
('FROM_NORTH_STRAIGHT', 'Vehicle from North going straight (to South)'),
('FROM_NORTH_LEFT', 'Vehicle from North turning left (to East)'),
('FROM_NORTH_RIGHT', 'Vehicle from North turning right (to West)'),
('FROM_SOUTH_STRAIGHT', 'Vehicle from South going straight (to North)'),
('FROM_SOUTH_LEFT', 'Vehicle from South turning left (to West)'),
('FROM_SOUTH_RIGHT', 'Vehicle from South turning right (to East)'),
('FROM_EAST_STRAIGHT', 'Vehicle from East going straight (to West)'),
('FROM_EAST_LEFT', 'Vehicle from East turning left (to South)'),
('FROM_EAST_RIGHT', 'Vehicle from East turning right (to North)'),
('FROM_WEST_STRAIGHT', 'Vehicle from West going straight (to East)'),
('FROM_WEST_LEFT', 'Vehicle from West turning left (to North)'),
('FROM_WEST_RIGHT', 'Vehicle from West turning right (to South)');

-- 2. PHASES
INSERT INTO signal_phases (phase_name, description, duration_seconds, is_active) VALUES
('Phase 1: North-South Through', 'North & South straight + right turns', 60, true),
('Phase 2: North-South Left', 'North & South left turns only', 30, true),
('Phase 3: East-West Through', 'East & West straight + right turns', 60, true),
('Phase 4: East-West Left', 'East & West left turns only', 30, true),
('Phase 5: North Only', 'All vehicles from North approach', 45, false),
('Phase 6: South Only', 'All vehicles from South approach', 45, false),
('Phase 7: East Only', 'All vehicles from East approach', 45, false),
('Phase 8: West Only', 'All vehicles from West approach', 45, false),
('Phase 9: All Red', 'All signals red - pedestrian crossing', 10, false);

-- 3. PHASE MAPPINGS
INSERT INTO phase_allowed_directions (phase_id, direction_id) VALUES
(1, 1), (1, 3), (1, 4), (1, 6),
(2, 2), (2, 5),
(3, 7), (3, 9), (3, 10), (3, 12),
(4, 8), (4, 11),
(5, 1), (5, 2), (5, 3),
(6, 4), (6, 5), (6, 6),
(7, 7), (7, 8), (7, 9),
(8, 10), (8, 11), (8, 12);

-- 4. CONFLICTS
INSERT INTO direction_conflicts (direction_id, conflicts_with_direction_id, conflict_reason) VALUES
(1, 5, 'North-straight vs South-left'),
(2, 4, 'North-left vs South-straight'),
(2, 6, 'North-left vs South-right'),
(1, 7, 'North-straight vs East-straight'),
(1, 8, 'North-straight vs East-left'),
(2, 7, 'North-left vs East-straight'),
(2, 8, 'North-left vs East-left'),
(2, 9, 'North-left vs East-right'),
(3, 7, 'North-right vs East-straight'),
(3, 8, 'North-right vs East-left'),
(1, 10, 'North-straight vs West-straight'),
(1, 11, 'North-straight vs West-left'),
(2, 10, 'North-left vs West-straight'),
(2, 11, 'North-left vs West-left'),
(2, 12, 'North-left vs West-right'),
(3, 11, 'North-right vs West-left'),
(3, 12, 'North-right vs West-right'),
(4, 7, 'South-straight vs East-straight'),
(4, 8, 'South-straight vs East-left'),
(5, 7, 'South-left vs East-straight'),
(5, 8, 'South-left vs East-left'),
(5, 9, 'South-left vs East-right'),
(6, 8, 'South-right vs East-left'),
(6, 9, 'South-right vs East-right'),
(4, 10, 'South-straight vs West-straight'),
(4, 11, 'South-straight vs West-left'),
(5, 10, 'South-left vs West-straight'),
(5, 11, 'South-left vs West-left'),
(5, 12, 'South-left vs West-right'),
(6, 10, 'South-right vs West-straight'),
(6, 11, 'South-right vs West-left'),
(7, 10, 'East-straight vs West-straight'),
(7, 11, 'East-straight vs West-left'),
(8, 10, 'East-left vs West-straight'),
(8, 11, 'East-left vs West-left'),
(8, 12, 'East-left vs West-right'),
(9, 11, 'East-right vs West-left');