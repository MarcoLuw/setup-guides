-- Initial schema for chat messages table

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender VARCHAR(255) NOT NULL,
    content TEXT,
    message_type VARCHAR(50) NOT NULL,
    room_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster queries
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_chat_messages_room_id ON chat_messages(room_id);
