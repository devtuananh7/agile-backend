-- ================================================================
-- CareTalk Database - Complete Schema
-- Matches JPA Entity classes exactly
-- ================================================================
-- Usage: psql -U devtuananh -d caretalk_db -f init_schema.sql
--
-- Prerequisites (run as superuser):
--   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--   CREATE EXTENSION IF NOT EXISTS "vector";
-- ================================================================

-- ============================================
-- 1. users (User.java)
-- ============================================
-- Fields: id(UUID), firebase_uid, email, phone_number, password_hash,
--         role, status, auth_provider, metadata(JSONB), created_at, updated_at
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid    VARCHAR(128) UNIQUE,
    email           VARCHAR(255) UNIQUE,
    phone_number    VARCHAR(20) UNIQUE,
    password_hash   VARCHAR(255),
    role            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    auth_provider   VARCHAR(50),
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users (firebase_uid) WHERE firebase_uid IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

-- ============================================
-- 2. conversations (Conversation.java)
-- ============================================
-- Fields: id(BIGSERIAL), session_id(UUID), ref_id(FK→conversations),
--         status, user_id, username, doctor, type, prompt_name,
--         conclusion, summary, summary_until_id, created_at, updated_at
CREATE TABLE IF NOT EXISTS conversations (
    id               BIGSERIAL PRIMARY KEY,
    session_id       UUID NOT NULL UNIQUE,
    ref_id           BIGINT REFERENCES conversations(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id          VARCHAR(100) NOT NULL,
    username         VARCHAR(255) NOT NULL,
    doctor           VARCHAR(100) NOT NULL DEFAULT 'BOT',
    type             VARCHAR(20) NOT NULL DEFAULT 'BOT',
    prompt_name      VARCHAR(100) NOT NULL DEFAULT 'medical_general',
    conclusion       TEXT,
    summary          TEXT,
    summary_until_id BIGINT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_conversations_session_id ON conversations(session_id);
CREATE INDEX IF NOT EXISTS idx_conversations_user_id ON conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_ref_id ON conversations(ref_id);

-- ============================================
-- 3. messages (Message.java)
-- ============================================
-- Fields: id(BIGSERIAL), conversation_id(FK), sender_id, sender_role,
--         content, content_type, metadata(JSONB), created_at
CREATE TABLE IF NOT EXISTS messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    sender_id       VARCHAR(100) NOT NULL,
    sender_role     VARCHAR(20) NOT NULL,
    content         TEXT NOT NULL,
    content_type    VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(conversation_id, created_at);

-- FK: conversations.summary_until_id → messages.id
DO $$ BEGIN
    ALTER TABLE conversations
        ADD CONSTRAINT fk_conversations_summary_until_id
        FOREIGN KEY (summary_until_id) REFERENCES messages(id);
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ============================================
-- 4. system_prompts (SystemPrompt.java)
-- ============================================
-- Fields: id(BIGSERIAL), name, content, version, is_active,
--         metadata(JSONB), created_at, updated_at
CREATE TABLE IF NOT EXISTS system_prompts (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    content         TEXT NOT NULL,
    version         INT NOT NULL DEFAULT 1,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_system_prompts_name ON system_prompts(name);

-- ============================================
-- 5. knowledge_documents (KnowledgeDocument.java)
-- ============================================
-- Fields: id(BIGSERIAL), title, content, category, tags(TEXT[]),
--         embedding(vector(1536)), is_active, created_at, updated_at
CREATE TABLE IF NOT EXISTS knowledge_documents (
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

CREATE INDEX IF NOT EXISTS idx_knowledge_documents_category ON knowledge_documents(category);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_active ON knowledge_documents(is_active);

-- HNSW vector index for fast cosine similarity search
-- Recommended over IVFFlat for dynamic data (frequent inserts/deletes)
CREATE INDEX IF NOT EXISTS idx_knowledge_embedding_hnsw
    ON knowledge_documents
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);


-- ================================================================
-- SEED DATA
-- ================================================================

-- System Prompts
INSERT INTO system_prompts (name, content, version, is_active, metadata) VALUES
(
    'medical_general',
    'Bạn là trợ lý y tế CareTalk, được xây dựng bởi đội ngũ HUST. Nhiệm vụ của bạn:

1. **Tư vấn sơ bộ**: Thu thập triệu chứng, hỏi thêm thông tin, đưa ra đánh giá ban đầu.
2. **Không chẩn đoán chính thức**: Luôn nhấn mạnh rằng bạn chỉ hỗ trợ sơ bộ, không thay thế bác sĩ.
3. **Thu thập thông tin y tế**: Hỏi về triệu chứng, thời gian xuất hiện, mức độ, tiền sử bệnh, thuốc đang dùng.
4. **Khuyến khích đi khám**: Khi triệu chứng nghiêm trọng hoặc kéo dài, khuyên người dùng đi khám.
5. **An toàn**: Nếu phát hiện dấu hiệu cấp cứu (đau ngực, khó thở nặng, xuất huyết...), yêu cầu gọi 115 ngay.
6. **Ngôn ngữ**: Trả lời bằng tiếng Việt, rõ ràng, dễ hiểu, thân thiện.
7. **Escalation**: Nếu tình trạng phức tạp vượt quá khả năng tư vấn, đề xuất chuyển sang bác sĩ tư vấn trực tiếp.

Lưu ý: Không đưa ra đơn thuốc cụ thể. Chỉ có thể gợi ý nhóm thuốc phổ biến và khuyên tham khảo ý kiến bác sĩ.',
    1, true,
    '{"model": "gpt-4o", "temperature": 0.3}'::jsonb
),
(
    'symptom_triage',
    'Bạn là hệ thống phân loại triệu chứng CareTalk. Nhiệm vụ:

1. **Phân loại mức độ khẩn cấp** theo 4 cấp:
   - 🔴 KHẨN CẤP: Cần gọi 115 hoặc đến ER ngay
   - 🟠 CẦN KHÁM SỚM: Nên đi khám trong 24h
   - 🟡 THEO DÕI: Có thể theo dõi tại nhà 2-3 ngày
   - 🟢 TƯ VẤN: Câu hỏi sức khỏe chung

2. **Thu thập thông tin theo OPQRST**:
   - Onset: Bắt đầu khi nào?
   - Provocation: Gì làm tệ hơn/đỡ hơn?
   - Quality: Mô tả cảm giác
   - Region/Radiation: Vị trí, lan tỏa?
   - Severity: Mức độ 1-10?
   - Time: Kéo dài bao lâu?

3. Trả lời bằng tiếng Việt, ngắn gọn, tập trung vào câu hỏi.
4. Luôn kết thúc bằng phân loại mức độ và khuyến nghị hành động.',
    1, true,
    '{"model": "gpt-4o", "temperature": 0.2}'::jsonb
)
ON CONFLICT (name) DO NOTHING;

-- Knowledge Documents (RAG)
INSERT INTO knowledge_documents (title, content, category, tags, is_active) VALUES
(
    'Hướng dẫn xử trí đau đầu',
    'Đau đầu là triệu chứng phổ biến. Phân loại:
- Đau đầu nguyên phát: Migraine, đau đầu căng thẳng, đau đầu cụm.
- Đau đầu thứ phát: Do nhiễm trùng, chấn thương, tăng huyết áp, u não.

Dấu hiệu cảnh báo cần cấp cứu: đau đầu đột ngột dữ dội, kèm sốt cao + cứng cổ, sau chấn thương đầu, kèm thay đổi ý thức.

Xử trí ban đầu: Nghỉ ngơi, uống đủ nước, Paracetamol 500mg-1g (tối đa 4g/ngày). Nếu không đỡ sau 3 ngày: đi khám.',
    'symptoms', ARRAY['đau đầu', 'migraine', 'headache'], true
),
(
    'Hướng dẫn xử trí sốt',
    'Sốt là phản ứng cơ thể khi nhiệt độ > 37.5°C.
- Sốt nhẹ: 37.5-38.5°C → Theo dõi, uống nhiều nước
- Sốt vừa: 38.5-39.5°C → Paracetamol/Ibuprofen
- Sốt cao: > 39.5°C → Cần đi khám
- Sốt kéo dài > 3 ngày → Bắt buộc đi khám

Thuốc hạ sốt an toàn: Paracetamol (10-15mg/kg/lần, cách 4-6h).
KHÔNG dùng Aspirin cho trẻ dưới 18 tuổi.',
    'symptoms', ARRAY['sốt', 'fever', 'hạ sốt'], true
),
(
    'Thông tin thuốc Paracetamol',
    'Paracetamol (Acetaminophen) - Thuốc giảm đau, hạ sốt phổ biến.
Liều người lớn: 500mg-1g/lần, cách 4-6 giờ, tối đa 4g/ngày.
Liều trẻ em: 10-15mg/kg/lần, cách 4-6 giờ.
Chống chỉ định: Suy gan nặng. Quá liều gây tổn thương gan nghiêm trọng.
An toàn cho thai phụ (category B).',
    'drugs', ARRAY['paracetamol', 'giảm đau', 'hạ sốt'], true
),
(
    'Hướng dẫn xử trí đau bụng',
    'Đau bụng - phân vùng và nguyên nhân phổ biến:
Thượng vị: Viêm dạ dày, loét, trào ngược.
Hạ sườn phải: Viêm túi mật, sỏi mật, viêm gan.
Quanh rốn: Viêm ruột thừa sớm, tắc ruột.
Hố chậu phải: Viêm ruột thừa, buồng trứng.

Dấu hiệu cần cấp cứu: Đau dữ dội đột ngột, bụng cứng, nôn ra máu, sốt cao + đau bụng.',
    'symptoms', ARRAY['đau bụng', 'tiêu hóa', 'dạ dày'], true
)
ON CONFLICT DO NOTHING;

-- ================================================================
-- Schema created. Tables:
--   1. users              (UUID PK, Firebase auth)
--   2. conversations      (BIGSERIAL PK, session management)
--   3. messages           (BIGSERIAL PK, chat messages)
--   4. system_prompts     (AI prompt configuration)
--   5. knowledge_documents (RAG with pgvector embedding)
-- ================================================================
