-- Create assignments table
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    due_date TIMESTAMP,
    time_limit_minutes INTEGER,
    max_attempts INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for assignments
CREATE INDEX idx_assignments_teacher_id ON assignments(teacher_id);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);

-- Create exercises table
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    order_index INTEGER NOT NULL,
    points INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    question TEXT NOT NULL,
    config_json TEXT NOT NULL,
    explanation TEXT,
    CONSTRAINT fk_exercise_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE
);

-- Create indexes for exercises
CREATE INDEX idx_exercises_assignment_id ON exercises(assignment_id);
CREATE INDEX idx_exercises_order ON exercises(assignment_id, order_index);

-- Create assignment_attempts table
CREATE TABLE assignment_attempts (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    attempt_number INTEGER NOT NULL,
    score INTEGER,
    max_score INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP,
    expires_at TIMESTAMP,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_attempt_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT unique_attempt UNIQUE (assignment_id, student_id, attempt_number)
);

-- Create indexes for assignment_attempts
CREATE INDEX idx_attempts_assignment_student ON assignment_attempts(assignment_id, student_id);
CREATE INDEX idx_attempts_student ON assignment_attempts(student_id);
CREATE INDEX idx_attempts_group ON assignment_attempts(assignment_id, group_id);
CREATE INDEX idx_attempts_completed ON assignment_attempts(completed);

-- Create exercise_answers table
CREATE TABLE exercise_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    exercise_type VARCHAR(20) NOT NULL,
    answer_json TEXT NOT NULL,
    correct BOOLEAN NOT NULL,
    points_earned INTEGER,
    points_possible INTEGER NOT NULL,
    validation_result_json TEXT,
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES assignment_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

-- Create indexes for exercise_answers
CREATE INDEX idx_answers_attempt ON exercise_answers(attempt_id);
CREATE INDEX idx_answers_exercise ON exercise_answers(exercise_id);