-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================
-- Conversations table
-- ============================================
CREATE TABLE conversations (
    id              BIGSERIAL PRIMARY KEY,
    session_id      UUID NOT NULL UNIQUE,
    ref_id          BIGINT REFERENCES conversations(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id         VARCHAR(100) NOT NULL,
    username        VARCHAR(255) NOT NULL,
    doctor          VARCHAR(100) NOT NULL DEFAULT 'BOT',
    type            VARCHAR(20) NOT NULL DEFAULT 'BOT',
    prompt_name     VARCHAR(100) NOT NULL DEFAULT 'medical_general',
    conclusion      TEXT,
    summary         TEXT,
    summary_until_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_session_id ON conversations(session_id);
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_ref_id ON conversations(ref_id);

-- ============================================
-- Messages table
-- ============================================
CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    sender_id       VARCHAR(100) NOT NULL,
    sender_role     VARCHAR(20) NOT NULL,
    content         TEXT NOT NULL,
    content_type    VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(conversation_id, created_at);

-- Add FK from conversations.summary_until_id to messages.id
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_summary_until_id
    FOREIGN KEY (summary_until_id) REFERENCES messages(id);

-- ============================================
-- System Prompts table
-- ============================================
CREATE TABLE system_prompts (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    content         TEXT NOT NULL,
    version         INT NOT NULL DEFAULT 1,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_system_prompts_name ON system_prompts(name);

-- ============================================
-- Knowledge Documents table (with pgvector)
-- ============================================
CREATE TABLE knowledge_documents (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    content         TEXT NOT NULL,
    category        VARCHAR(100),
    tags            TEXT[],
    embedding       vector(1536),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_knowledge_documents_category ON knowledge_documents(category);
CREATE INDEX idx_knowledge_documents_active ON knowledge_documents(is_active);
