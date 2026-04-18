## Why

Hệ thống CareTalk backend hiện tại chỉ có SSE/WebSocket placeholder, chưa có data model hay business logic nào cho quản lý hội thoại. Cần xây dựng hệ thống conversation core để:
- Lưu trữ và quản lý các cuộc hội thoại giữa user-AI Bot và user-Doctor
- Hỗ trợ escalation từ bot sang bác sĩ với context liên tục
- Tối ưu chi phí LLM bằng auto-summary khi conversation dài
- Cung cấp pipeline linh hoạt để gom system prompt + RAG context trước khi gọi LLM

## What Changes

- **Thêm entity `Conversation`**: Quản lý cuộc hội thoại với session tracking, status lifecycle, escalation via `ref_id`, auto-summary
- **Thêm entity `Message`**: Tách message ra bảng riêng (thay vì JSONB metadata), hỗ trợ pagination, search, concurrent writes
- **Thêm entity `SystemPrompt`**: Quản lý system prompt theo name/version, cho phép mỗi conversation type dùng prompt khác nhau, lưu model config (model name, temperature) trong metadata
- **Thêm entity `KnowledgeDocument`**: Lưu tài liệu y tế cho RAG, sử dụng pgvector để lưu embedding và search semantic
- **Thêm Prompt Assembly Pipeline**: Orchestrator gom system prompt + RAG context + conversation summary + recent messages thành final LLM request
- **Thêm Summary Generator**: Auto-generate/regenerate summary khi conversation vượt 20 messages, sử dụng lightweight model
- **Thêm Conversation Service**: CRUD + session resolution logic (có session_id → tìm active conversation, không có → tạo mới)
- **Thêm LLM Client abstraction**: Interface cho LLM streaming, impl cho OpenAI/Gemini

## Capabilities

### New Capabilities
- `conversation-management`: CRUD conversations, session resolution, status lifecycle (ACTIVE/INACTIVE/DONE), escalation flow (BOT → DOCTOR với ref_id và session_id riêng)
- `message-management`: CRUD messages tách bảng riêng, pagination, query theo conversation_id
- `prompt-assembly`: Pipeline gom system prompt (từ DB) + RAG context (pgvector search) + conversation context (summary + recent messages) thành final LLM request
- `auto-summary`: Tự động tóm tắt conversation khi vượt 20 messages, re-generate khi thêm 20 messages mới, lưu structured medical summary
- `knowledge-rag`: Quản lý knowledge documents với pgvector embedding, semantic search để inject context y tế vào prompt
- `llm-integration`: Abstraction layer cho LLM API streaming (SSE), hỗ trợ multi-model config từ system_prompts metadata

### Modified Capabilities
_(Không có capability cũ nào bị thay đổi — đây là hệ thống mới hoàn toàn)_

## Impact

- **Database**: Thêm 4 bảng mới (conversations, messages, system_prompts, knowledge_documents), cần enable pgvector extension
- **Dependencies**: Thêm pgvector dependency, OpenAI/Gemini client SDK
- **Existing code**: `ChatbotController` và `ConsultationWsController` sẽ cần refactor để sử dụng ConversationService và PromptAssembler thay vì placeholder logic hiện tại
- **Infrastructure**: PostgreSQL cần enable `vector` extension cho RAG feature
- **API surface**: Thêm REST endpoints cho conversation CRUD, escalation, message history
