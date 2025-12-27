-- Create assignment_group_assignments junction table
CREATE TABLE assignment_group_assignments (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aga_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_aga_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT unique_assignment_group UNIQUE (assignment_id, group_id)
);

-- Create indexes for performance
CREATE INDEX idx_aga_assignment ON assignment_group_assignments(assignment_id);
CREATE INDEX idx_aga_group ON assignment_group_assignments(group_id);