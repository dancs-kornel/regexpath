-- V7: Add difficulty tracking for adaptive learning

-- Add difficulty_level column to users table
ALTER TABLE users 
ADD COLUMN difficulty_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' 
CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD'));

-- Create index for difficulty level
CREATE INDEX idx_users_difficulty ON users(difficulty_level);

-- Create exercise_attempts table to track user performance
CREATE TABLE exercise_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id VARCHAR(100) NOT NULL,
    exercise_id VARCHAR(100) NOT NULL,
    attempt_number INTEGER NOT NULL DEFAULT 1,
    is_correct BOOLEAN NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_exercise_attempt_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Business rule: attempt_number must be positive
    CONSTRAINT chk_attempt_number_positive CHECK (attempt_number > 0)
);

-- Create indexes for common queries
CREATE INDEX idx_exercise_attempts_user ON exercise_attempts(user_id);
CREATE INDEX idx_exercise_attempts_user_time ON exercise_attempts(user_id, attempted_at DESC);
CREATE INDEX idx_exercise_attempts_lesson ON exercise_attempts(lesson_id);
CREATE INDEX idx_exercise_attempts_exercise ON exercise_attempts(lesson_id, exercise_id);

-- Add metadata columns for tracking when difficulty was last changed
ALTER TABLE users
ADD COLUMN difficulty_changed_at TIMESTAMP,
ADD COLUMN difficulty_prompt_shown_at TIMESTAMP;

-- Add comments
COMMENT ON TABLE exercise_attempts IS 'Tracks individual exercise attempts for adaptive difficulty';
COMMENT ON COLUMN users.difficulty_level IS 'Current difficulty level: EASY, MEDIUM, or HARD';
COMMENT ON COLUMN users.difficulty_changed_at IS 'When the difficulty level was last changed';
COMMENT ON COLUMN users.difficulty_prompt_shown_at IS 'When user was last prompted to change difficulty';
COMMENT ON COLUMN exercise_attempts.attempt_number IS 'Which attempt this is for the current exercise (resets when moving to next exercise)';