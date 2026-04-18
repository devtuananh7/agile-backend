## 1. Database & Entity Setup

- [x] 1.1 Thêm pgvector dependency vào build.gradle (hibernate-vector, pgvector JDBC)
- [x] 1.2 Tạo database migration: CREATE EXTENSION IF NOT EXISTS vector; CREATE TABLE conversations, messages, system_prompts, knowledge_documents
- [x] 1.3 Tạo entity Conversation (JPA, với enum ConversationStatus, ConversationType)
- [x] 1.4 Tạo entity Message (JPA, với enum SenderRole, ContentType)
- [x] 1.5 Tạo entity SystemPrompt (JPA, JSONB metadata field)
- [x] 1.6 Tạo entity KnowledgeDocument (JPA, pgvector embedding column)
- [x] 1.7 Tạo ConversationRepository, MessageRepository, SystemPromptRepository, KnowledgeDocumentRepository

## 2. Conversation Management Service

- [x] 2.1 Tạo ConversationService: createConversation(), findBySessionId(), updateStatus()
- [x] 2.2 Implement session resolution logic: có session_id → tìm active, không có → tạo mới + generate UUID
- [x] 2.3 Implement escalation flow: closeBotConversation() + createEscalatedConversation(refId, doctorId)
- [x] 2.4 Implement conversation listing API với pagination (findByUserId, Pageable)

## 3. Message Management Service

- [x] 3.1 Tạo MessageService: saveMessage(), getRecentMessages(), countByConversationId()
- [x] 3.2 Implement pagination cho message retrieval (default 20, order by created_at ASC)
- [x] 3.3 Implement getMessagesAfterSummary(conversationId, summaryUntilId) cho context building

## 4. System Prompt & Config

- [x] 4.1 Tạo SystemPromptResolver service: resolveByName(), fallback to default prompt
- [x] 4.2 Tạo seed data migration cho system_prompts (medical_general, symptom_triage)
- [x] 4.3 Parse metadata JSONB để extract model name và temperature

## 5. Knowledge RAG

- [x] 5.1 Tạo EmbeddingService: interface + implementation gọi embedding API (text-embedding-3-small)
- [x] 5.2 Tạo KnowledgeService: saveDocument() với auto-embed, searchSimilar(queryVector, threshold, topK)
- [x] 5.3 Tạo RagRetriever: embed user message → search pgvector → format top-3 results thành [MEDICAL KNOWLEDGE] section
- [x] 5.4 Tạo seed data migration cho sample knowledge_documents (test data y tế)

## 6. Auto-Summary

- [x] 6.1 Tạo SummaryGenerator service: generateSummary(), regenerateSummary()
- [x] 6.2 Implement threshold logic: kiểm tra tổng messages > 20, và messages mới since summary > 20
- [x] 6.3 Implement structured medical summary prompt: extract triệu chứng, thuốc, chỉ số, tiền sử
- [x] 6.4 Implement async execution (@Async) để không block chat response
- [x] 6.5 Update conversations.summary và summary_until_id sau khi generate xong

## 7. Prompt Assembly Pipeline

- [x] 7.1 Tạo ContextBuilder: build conversation context (full nếu ≤20, summary+recent nếu >20)
- [x] 7.2 Tạo LlmRequest DTO: systemPrompt, ragContext, summary, recentMessages, userMessage, model, temperature, stream
- [x] 7.3 Tạo PromptAssembler orchestrator: gom SystemPromptResolver + RagRetriever + ContextBuilder → LlmRequest

## 8. LLM Client Integration

- [x] 8.1 Tạo LlmClient interface: Flux<String> streamChat(LlmRequest)
- [x] 8.2 Implement OpenAiClient: gọi OpenAI Chat Completions API with streaming (WebClient)
- [x] 8.3 Thêm config properties cho LLM API keys, base URLs, model-to-provider mapping
- [x] 8.4 Implement model-to-provider routing: chọn LlmClient implementation dựa trên model name

## 9. Controller & API Endpoints

- [x] 9.1 Refactor ChatbotController: inject ConversationService, MessageService, PromptAssembler, LlmClient
- [x] 9.2 Implement POST /api/v1/chatbot/chat: session resolution → save message → assemble → stream → save response → check summary
- [x] 9.3 Tạo ConversationController: GET /conversations (list), GET /conversations/{id} (detail + messages)
- [x] 9.4 Tạo EscalationController: PUT /conversations/{id}/escalate
- [x] 9.5 Refactor ConsultationWsController: dùng ConversationService + MessageService cho STOMP messages

## 10. Testing & Verification

- [ ] 10.1 Viết unit tests cho ConversationService (session resolution, escalation, status lifecycle)
- [ ] 10.2 Viết unit tests cho PromptAssembler (short conversation, long conversation with summary)
- [ ] 10.3 Viết unit tests cho SummaryGenerator (threshold check, regeneration logic)
- [ ] 10.4 Viết integration test: full flow POST /chat → SSE response
- [ ] 10.5 Verify database migration chạy thành công trên PostgreSQL với pgvector
