CREATE TABLE commentaries (
    id BIGSERIAL PRIMARY KEY,
    artifact_id BIGINT NOT NULL REFERENCES artifacts(id) ON DELETE CASCADE,
    commentary_text TEXT NOT NULL,
    commentary_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trigger to auto-update updated_at column
CREATE TRIGGER update_commentaries_updated_at BEFORE UPDATE ON commentaries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Index on artifact_id for lookups
CREATE INDEX idx_commentaries_artifact_id ON commentaries(artifact_id);
