## Context

CareTalk backend (Spring Boot 4.0.5, Java 21) hiện có:
- SSE endpoint placeholder (`ChatbotController`) cho AI chatbot
- WebSocket/STOMP placeholder (`ConsultationWsController`) cho doctor consultation
- PostgreSQL + JPA đã config, Kafka dependency đã có
- Chưa có data model, service layer, hay LLM integration nào

Cần xây dựng conversation system core: data model, prompt assembly pipeline, LLM integration, và session management.

## Goals / Non-Goals

**Goals:**
- Xây dựng data model cho conversations, messages, system_prompts, knowledge_documents
- Implement prompt assembly pipeline: system prompt → RAG context → conversation context → final LLM request
- Session management: tạo mới hoặc resume conversation dựa trên session_id
- Escalation flow: BOT → DOCTOR với ref_id link và session_id riêng
- Auto-summary: threshold 20 messages, re-generate mỗi 20 tin mới
- LLM abstraction: interface chuẩn cho multi-provider (OpenAI, Gemini)

**Non-Goals:**
- Admin panel UI cho quản lý system prompts / knowledge documents (dùng seed data / migration)
- Authentication / Authorization (sẽ là change riêng)
- Kafka integration cho message pipeline (bắt đầu đồng bộ, Kafka sẽ thêm sau)
- Doctor matching / queue system (manual assign trong MVP)
- File upload / Image messages (chỉ hỗ trợ TEXT trong MVP)

## Decisions

### D1: Tách bảng `messages` thay vì JSONB metadata

**Quyết định:** Messages lưu riêng trong bảng `messages` với FK `conversation_id`.

**Lý do:** 
- Hỗ trợ pagination (lấy 10 tin cuối thay vì load toàn bộ)
- Tránh race condition khi concurrent write (INSERT vs JSONB append)
- Cho phép full-text search trên message content
- Scale tốt hơn khi conversation dài (>100 messages)

**Đã xem xét:** Lưu JSONB trong conversations — đơn giản hơn nhưng không scale, không search được, race condition khi multiple users write.

### D2: Escalation tạo conversation mới với ref_id và session_id riêng

**Quyết định:** Khi escalate BOT → DOCTOR:
1. Đánh conversation BOT → status=DONE
2. Tạo conversation mới (type=ESCALATED, ref_id=conversation_BOT.id)
3. Session_id mới (khác session bot) cho WebSocket topic riêng

**Lý do:**
- Clean separation giữa bot history và doctor conversation
- Session_id riêng → WebSocket topic riêng → không conflict
- Bác sĩ query ref_id để xem summary + context bot

**Đã xem xét:** Gộp cùng conversation, đổi doctor field — đơn giản nhưng khó audit, doctor field thay đổi gây confusion.

### D3: Session resolution logic

**Quyết định:**
- Request có `session_id` → tìm conversation ACTIVE với session đó → dùng nếu tìm thấy, tạo mới cùng session nếu không
- Request không có `session_id` → tạo conversation mới + generate UUID mới → return cho client lưu

**Lý do:** Client-driven session management, đơn giản, stateless backend.

### D4: Auto-summary với threshold 20, re-generate mỗi 20 tin mới

**Quyết định:**
- Threshold: 20 messages → bắt đầu summary
- Lưu: `conversations.summary` (text) + `summary_until_id` (message ID)
- Re-generate: khi messages mới kể từ `summary_until_id` > 20
- Input re-generate: old_summary + messages mới → new_summary (replace old)
- Model: dùng lightweight model (GPT-4o-mini / Gemini Flash)
- Medical-focused: structured extraction (triệu chứng, thuốc, chỉ số, tiền sử)

**Lý do:** Cân bằng giữa an toàn thông tin y tế (giữ tất cả chi tiết quan trọng) và chi phí token.

### D5: Prompt Assembly Pipeline — 4 stages

**Quyết định:** Pipeline gồm 4 stages chạy tuần tự:
1. **SystemPromptResolver**: Load system prompt từ bảng `system_prompts` theo `conversation.prompt_name`
2. **RagRetriever**: Embed user message → search pgvector → format top-K results
3. **ContextBuilder**: Lấy summary (nếu có) + recent messages (≤20 hoặc từ sau summary)
4. **PromptAssembler**: Gom tất cả thành final LLM request

**Lý do:** 
- Tách riêng → dễ test, dễ thay thế từng component
- pgvector cho RAG — đã có PostgreSQL, không cần thêm infra
- SystemPrompt từ DB — thay đổi prompt không cần deploy

**Đã xem xét:** 
- RAG dùng Qdrant — overkill cho MVP, cần thêm service
- System prompt từ config file — không flexible, cần redeploy để thay đổi

### D6: LLM Client abstraction

**Quyết định:** Interface `LlmClient` với method `streamChat(LlmRequest) → Flux<String>`. Implementations riêng cho từng provider.

**Lý do:** Cho phép switch model dễ dàng, mỗi system prompt có thể config model riêng trong metadata JSONB.

### D7: pgvector cho RAG embedding

**Quyết định:** Dùng pgvector extension trong PostgreSQL hiện có thay vì vector DB riêng.

**Lý do:** Không cần thêm infrastructure, đủ tốt cho MVP với dataset knowledge < 10K documents. Migration sang Qdrant sau nếu cần.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| pgvector performance kém với dataset lớn (>50K docs) | Giới hạn MVP < 10K docs, migration path sang Qdrant đã design sẵn (interface abstraction) |
| Summary mất thông tin y tế quan trọng | Dùng structured extraction prompt, giữ tất cả triệu chứng/thuốc/chỉ số. Có thể verify bằng cách so sánh summary vs full messages |
| LLM API downtime | Implement retry + circuit breaker pattern. Lưu user message trước khi gọi LLM → không mất data |
| Concurrent escalation race condition (2 doctors accept cùng lúc) | Optimistic locking trên conversation status. Chỉ doctor đầu tiên update thành công |
| Token cost tăng nếu RAG trả nhiều documents | Limit top-K = 3, similarity threshold > 0.75, max token per RAG context |
