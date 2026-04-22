# RAG Build Guide — CareTalk Backend

> Tài liệu mô tả cơ chế xây dựng RAG (Retrieval-Augmented Generation) cho dự án agile-chatbot-backend, bao gồm kiến trúc hiện tại, những gì cần chuẩn bị, và quy trình vận hành end-to-end.

---

## 1. Kiến trúc RAG hiện tại

### 1.1 Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                         CHAT PIPELINE                          │
│                                                                 │
│  User Message                                                   │
│       │                                                         │
│       ▼                                                         │
│  ┌──────────────┐    ┌───────────────┐    ┌─────────────────┐   │
│  │ PromptAssem- │───▶│ RagRetriever  │───▶│ KnowledgeService│   │
│  │ bler         │    │ (threshold=   │    │ .searchByText() │   │
│  │              │    │  0.75, topK=3)│    └────────┬────────┘   │
│  └──────┬───────┘    └───────────────┘             │            │
│         │                                          ▼            │
│         │                                ┌─────────────────┐    │
│         │                                │ EmbeddingService │    │
│         │                                │ (OpenAI API)     │    │
│         │                                │ text-embedding-  │    │
│         │                                │ 3-small (1536d)  │    │
│         │                                └────────┬────────┘    │
│         │                                         │             │
│         │                                         ▼             │
│         │                                ┌─────────────────┐    │
│         │                                │  PostgreSQL +    │    │
│         │                                │  pgvector        │    │
│         │                                │  knowledge_docs  │    │
│         │                                │  cosine distance │    │
│         │                                └─────────────────┘    │
│         ▼                                                       │
│  ┌──────────────────────────────────────────┐                   │
│  │ System Prompt + [MEDICAL KNOWLEDGE] +    │                   │
│  │ [TÓM TẮT CUỘC HỘI THOẠI] + History     │                   │
│  └──────────────────────────────────────────┘                   │
│         │                                                       │
│         ▼                                                       │
│  ┌──────────────┐                                               │
│  │ LLM (GPT-4o) │──── SSE Stream ────▶ Client                  │
│  └──────────────┘                                               │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Các thành phần chính

| Component | File | Chức năng |
|---|---|---|
| **KnowledgeDocument** | `entity/KnowledgeDocument.java` | Entity JPA — lưu title, content, category, tags, embedding `vector(1536)` |
| **KnowledgeDocumentRepository** | `repository/KnowledgeDocumentRepository.java` | Native query pgvector cosine distance search |
| **EmbeddingService** | `service/knowledge/EmbeddingService.java` | Interface embed text → float[] |
| **OpenAiEmbeddingService** | `service/knowledge/OpenAiEmbeddingService.java` | Implementation gọi OpenAI `/embeddings` API |
| **KnowledgeService** | `service/knowledge/KnowledgeService.java` | CRUD + semantic search (saveDocument, searchByText) |
| **RagRetriever** | `service/prompt/RagRetriever.java` | Retrieve + format context thành `[MEDICAL KNOWLEDGE]` section |
| **PromptAssembler** | `service/prompt/PromptAssembler.java` | Ghép: SystemPrompt + RAG + Summary + History → LlmRequest |

### 1.3 Flow chi tiết khi user gửi message

```
1. ChatbotController.chat()
2. → PromptAssembler.assemble(conversation, userMessage)
3.   → RagRetriever.retrieve(userMessage)
4.     → KnowledgeService.searchByText(userMessage, 0.75, 3)
5.       → EmbeddingService.embed(userMessage)           ← Gọi OpenAI Embedding API
6.       → KnowledgeDocumentRepository.findSimilarDocuments()  ← pgvector cosine search
7.     → formatDocuments() → "[MEDICAL KNOWLEDGE - ...]"
8.   → ContextBuilder.build(conversation) → summary + history
9.   → composeSystemMessage(systemPrompt, ragContext, summary)
10. → LlmRouter.streamChat(llmRequest) → SSE stream to client
```

---

## 2. Chuẩn bị — Checklist

### 2.1 Infrastructure

| Item | Status | Chi tiết |
|---|---|---|
| **PostgreSQL** | ✅ Đã có | `jdbc:postgresql://localhost:5432/caretalk_db` |
| **pgvector extension** | ✅ Đã có | `CREATE EXTENSION IF NOT EXISTS "vector"` trong `init_schema.sql` |
| **Table knowledge_documents** | ✅ Đã có | Có cột `embedding vector(1536)`, indexes trên `category` và `is_active` |
| **IVFFlat/HNSW Index** | ❌ CHƯA CÓ | Cần tạo vector index để search nhanh khi data lớn |

> **⚠️ Quan trọng:** Hiện tại chưa có vector index (IVFFlat hoặc HNSW) trên cột `embedding`. Với dưới ~10K documents thì OK, nhưng khi scale lên cần thêm:
>
> ```sql
> -- HNSW index (recommended, tốt hơn IVFFlat cho dynamic data)
> CREATE INDEX IF NOT EXISTS idx_knowledge_embedding_hnsw
>   ON knowledge_documents
>   USING hnsw (embedding vector_cosine_ops)
>   WITH (m = 16, ef_construction = 64);
> ```

### 2.2 API Keys & Configuration

| Config Key | Giá trị | Ghi chú |
|---|---|---|
| `OPENAI_API_KEY` | Environment variable | Dùng chung cho Embedding + LLM + Summary |
| `caretalk.embedding.base-url` | `https://api.openai.com/v1` | Có thể thay bằng local proxy / Azure |
| `caretalk.embedding.model` | `text-embedding-3-small` | 1536 dimensions, giá rẻ |

**Chi phí ước tính (text-embedding-3-small):**
- Giá: ~$0.02 / 1M tokens
- 1 document ~500 tokens → 1M docs ≈ $10
- Mỗi query cũng cần embed 1 lần → negligible

### 2.3 Knowledge Data — Nguồn dữ liệu cần chuẩn bị

Hiện tại chỉ có **4 seed documents** trong `init_schema.sql`:

| # | Title | Category | Tags |
|---|---|---|---|
| 1 | Hướng dẫn xử trí đau đầu | symptoms | đau đầu, migraine, headache |
| 2 | Hướng dẫn xử trí sốt | symptoms | sốt, fever, hạ sốt |
| 3 | Thông tin thuốc Paracetamol | drugs | paracetamol, giảm đau, hạ sốt |
| 4 | Hướng dẫn xử trí đau bụng | symptoms | đau bụng, tiêu hóa, dạ dày |

**Cần chuẩn bị thêm:**

| Loại | Ví dụ | Gợi ý định dạng |
|---|---|---|
| Triệu chứng thường gặp | Ho, sổ mũi, đau ngực, khó thở, tiêu chảy... | Markdown/JSON, mỗi entry 200-800 từ |
| Bệnh lý | Tiểu đường, tăng huyết áp, viêm phổi... | Structured text với sections |
| Thuốc phổ biến | Ibuprofen, Amoxicillin, Omeprazole... | Bao gồm liều dùng, chống chỉ định |
| FAQ y tế | "Khi nào cần đi cấp cứu?", "Trẻ bị sốt..." | Q&A format |
| Hướng dẫn sơ cứu | Bỏng, gãy xương, ngộ độc... | Step-by-step |

### 2.4 Thiếu hụt quan trọng (⚠️ GAP)

| Gap | Mô tả | Impact |
|---|---|---|
| **Không có API ingest** | Chưa có `KnowledgeController` → chỉ insert được qua SQL | Không thể bulk-load qua REST API |
| **Không có chunking** | Mỗi document là 1 record nguyên vẹn | Document dài sẽ bị embedding kém chất lượng |
| **Không có batch embedding** | `saveDocument()` embed 1-by-1 | Ingest chậm với lượng data lớn |
| **Không có metadata filtering** | Search chỉ dùng cosine similarity | Không thể filter theo category/tags trước khi search |
| **Không có deduplication** | Không check title/content trùng | Có thể insert duplicate |
| **Embedding chưa indexed** | Chưa có HNSW/IVFFlat index | Search chậm khi > vài ngàn docs |

---

## 3. Process xây dựng RAG — End-to-End

### Phase 1: Chuẩn bị dữ liệu

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Thu thập      │    │ Làm sạch     │    │ Chunking     │
│ nguồn y tế   │───▶│ chuẩn hóa    │───▶│ (nếu >800    │
│ (PDF, web,   │    │ format       │    │  tokens/doc) │
│  sách, WHO)  │    │              │    │              │
└──────────────┘    └──────────────┘    └──────────────┘
        ↓                                      ↓
   Raw sources                            Ready chunks
   (.md, .json, .csv)                     (title, content, category, tags)
```

**Định dạng input recommened (JSON):**
```json
[
  {
    "title": "Hướng dẫn xử trí ho kéo dài",
    "content": "Ho kéo dài > 3 tuần có thể do...",
    "category": "symptoms",
    "tags": ["ho", "ho kéo dài", "cough"]
  }
]
```

**Chunking strategy:**
- Mỗi chunk nên 200–800 tokens (~150–600 từ tiếng Việt)
- Chunk lớn hơn → embedding mất focus
- Chunk nhỏ hơn → thiếu context
- Overlap 10-20% giữa các chunk liền kề

### Phase 2: Ingest vào Database

#### Cách 1: SQL Trực tiếp (hiện tại)

```sql
-- Insert WITHOUT embedding (cần app code embed sau)
INSERT INTO knowledge_documents (title, content, category, tags, is_active)
VALUES (
    'Tên tài liệu',
    'Nội dung tài liệu...',
    'category_name',
    ARRAY['tag1', 'tag2'],
    true
);
```

> ⚠️ Cách này KHÔNG tạo embedding. Record sẽ bị bỏ qua khi search vì query yêu cầu `embedding IS NOT NULL`.

#### Cách 2: Qua KnowledgeService (recommended)

```java
// Gọi trong app code — sẽ tự embed + lưu
knowledgeService.saveDocument(
    "Hướng dẫn xử trí ho",
    "Nội dung chi tiết...",
    "symptoms",
    List.of("ho", "cough", "ho kéo dài")
);
```

#### Cách 3: Build API ingest (TODO — chưa có)

Cần tạo `KnowledgeController` với endpoint:
```
POST /api/v1/admin/knowledge
Body: { "title": "...", "content": "...", "category": "...", "tags": [...] }
Response: { "id": 123, "embeddingDimension": 1536 }

POST /api/v1/admin/knowledge/bulk
Body: [ { ... }, { ... } ]
Response: { "imported": 50, "failed": 2, "errors": [...] }
```

### Phase 3: Embedding Generation

```
┌───────────────┐     ┌─────────────────┐     ┌──────────────┐
│ Document text │────▶│ OpenAI API      │────▶│ float[1536]  │
│ (content)     │     │ /v1/embeddings  │     │ stored in    │
│               │     │ model: text-    │     │ pgvector     │
│               │     │ embedding-3-    │     │ column       │
│               │     │ small           │     │              │
└───────────────┘     └─────────────────┘     └──────────────┘
```

**Luồng trong code:**
1. `KnowledgeService.saveDocument()` nhận title + content
2. Gọi `EmbeddingService.embed(content)` → OpenAI API
3. Nhận `float[1536]` → lưu vào cột `embedding` kiểu `vector(1536)`
4. JPA persist toàn bộ record

### Phase 4: Search & Retrieval (runtime)

```
User message: "Tôi bị đau đầu 3 ngày không bớt"
         │
         ▼
embed("Tôi bị đau đầu...") → float[1536] (query vector)
         │
         ▼
┌─────────────────────────────────────────────────────┐
│ SQL: SELECT * FROM knowledge_documents              │
│ WHERE is_active = true AND embedding IS NOT NULL     │
│   AND (1 - (embedding <=> CAST(:qv AS vector))) > 0.75  │
│ ORDER BY embedding <=> CAST(:qv AS vector)           │
│ LIMIT 3                                              │
└─────────────────────────────────────────────────────┘
         │
         ▼
Top-3 documents: "Hướng dẫn xử trí đau đầu" (score 0.89), ...
         │
         ▼
Format → "[MEDICAL KNOWLEDGE - Sử dụng thông tin này...]"
         │
         ▼
Inject vào System Prompt → gửi LLM
```

### Phase 5: Verify & Tune

| Metric | Cách đo | Threshold |
|---|---|---|
| **Retrieval Precision** | Trong 3 docs trả về, bao nhiêu thực sự liên quan | > 80% |
| **Retrieval Recall** | Với câu hỏi X, doc đúng có nằm trong top-3 không | > 90% |
| **Cosine Threshold** | Điều chỉnh `DEFAULT_THRESHOLD` trong RagRetriever | 0.65–0.80 |
| **Top-K** | Số lượng docs inject vào prompt | 2–5 |
| **Latency** | Thời gian embed + search | < 500ms |

---

## 4. Kế hoạch Build — Step-by-step

### Giai đoạn 1: Foundation (hiện tại ✅)
- [x] Entity + Repository + pgvector setup
- [x] EmbeddingService interface + OpenAI implementation
- [x] KnowledgeService (save + search)
- [x] RagRetriever + PromptAssembler integration
- [x] Seed data 4 documents

### Giai đoạn 2: Data Pipeline (cần build)
- [ ] Tạo `KnowledgeController` (REST API ingest)
- [ ] Implement text chunking utility (split document dài)
- [ ] Implement batch embedding (reduce API calls)
- [ ] Tạo script/tool import từ JSON/CSV
- [ ] Thêm HNSW vector index
- [ ] Thêm deduplication check (title hash)

### Giai đoạn 3: Data Collection
- [ ] Thu thập 50-100 tài liệu y tế cơ bản (triệu chứng, thuốc, sơ cứu)
- [ ] Chuẩn hóa format (chuẩn JSON ở Phase 1)
- [ ] Chunk documents > 800 tokens
- [ ] Bulk import qua API hoặc script
- [ ] Verify embedding generation qua logs

### Giai đoạn 4: Quality Tuning
- [ ] Test retrieval với 20-30 câu hỏi y tế thực tế
- [ ] Tune threshold (thử 0.65, 0.70, 0.75, 0.80)
- [ ] Tune topK (thử 2, 3, 5)
- [ ] Đánh giá "hallucination rate" khi có/không có RAG
- [ ] Thêm metadata filtering (category-aware search) nếu cần

### Giai đoạn 5: Production Hardening
- [ ] Monitor embedding API cost
- [ ] Caching frequent queries (optional)
- [ ] Rate limiting trên ingest API
- [ ] Backup strategy cho knowledge_documents
- [ ] RBAC — chỉ ADMIN/DOCTOR được ingest

---

## 5. Config tham chiếu nhanh

```properties
# === application.properties ===

# Embedding
caretalk.embedding.api-key=${OPENAI_API_KEY:}
caretalk.embedding.base-url=https://api.openai.com/v1
caretalk.embedding.model=text-embedding-3-small

# RAG defaults (hardcoded in RagRetriever.java — có thể externalize)
# DEFAULT_THRESHOLD = 0.75
# DEFAULT_TOP_K = 3
```

```groovy
// === build.gradle — pgvector deps ===
implementation 'org.hibernate.orm:hibernate-vector:7.2.7.Final'
implementation 'com.pgvector:pgvector:0.1.6'
```

```sql
-- === Prerequisites ===
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
```

---

## 6. Rủi ro & Lưu ý

| Rủi ro | Xác suất | Giải pháp |
|---|---|---|
| OpenAI API down → embedding fail → RAG fail | Thấp | RagRetriever đã graceful fallback (return null → chat vẫn hoạt động không RAG) |
| Embedding model thay đổi → vector dimension khác | Thấp | Cần re-embed toàn bộ data nếu đổi model |
| Document quá dài → embedding chất lượng kém | Trung bình | Implement chunking trước khi ingest |
| Cosine threshold quá cao → miss relevant docs | Trung bình | Test và tune threshold |
| Chi phí OpenAI API tăng khi data lớn | Thấp | text-embedding-3-small rất rẻ ($0.02/1M tokens) |
| Không có API ingest → khó maintain data | **Cao** | **Ưu tiên xây KnowledgeController** |

---

*Generated: 2026-04-22 | Project: agile-chatbot-backend (CareTalk)*
