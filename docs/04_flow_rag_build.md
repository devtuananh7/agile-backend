# Luồng 4: Build RAG — Knowledge Base Management

> Mô tả chi tiết luồng import, embedding, chunking, search và quản lý knowledge documents cho hệ thống RAG.

---

## 4.1 Tổng quan kiến trúc RAG

```
┌─────────────────────────────────────────────────────────┐
│                    INGEST PIPELINE                       │
│                                                          │
│  Admin POST /knowledge                                   │
│       │                                                  │
│       ▼                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ Dedup Check  │─▶│ TextChunker  │─▶│ EmbeddingService│ │
│  │(title unique)│  │(>800 tokens  │  │(OpenAI API)   │  │
│  │              │  │ → split)     │  │text-embedding │  │
│  │              │  │              │  │-3-small       │  │
│  └──────────────┘  └──────────────┘  └───────┬───────┘  │
│                                              │          │
│                                              ▼          │
│                                     ┌──────────────┐    │
│                                     │ PostgreSQL   │    │
│                                     │ + pgvector   │    │
│                                     │ vector(1536) │    │
│                                     └──────────────┘    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   RETRIEVAL PIPELINE                     │
│                                                          │
│  User message                                            │
│       │                                                  │
│       ▼                                                  │
│  EmbeddingService.embed(message) → float[1536]           │
│       │                                                  │
│       ▼                                                  │
│  pgvector cosine similarity search                       │
│  WHERE (1 - (embedding <=> query)) > 0.75                │
│  ORDER BY distance LIMIT 3                               │
│       │                                                  │
│       ▼                                                  │
│  Top-3 documents → format → [MEDICAL KNOWLEDGE] section  │
│       │                                                  │
│       ▼                                                  │
│  Inject vào System Prompt → gửi GPT-4o                   │
└─────────────────────────────────────────────────────────┘
```

---

## 4.2 Sequence Diagram — Import Single Document

```
┌────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌────────┐  ┌──────────┐
│ Admin  │  │KnowledgeController│ │ KnowledgeService │  │ TextChunker  │  │Embedding│ │PostgreSQL│
└───┬────┘  └────────┬─────────┘  └────────┬─────────┘  └──────┬───────┘  └────┬───┘  └────┬─────┘
    │               │                     │                   │                │           │
    │ POST /api/v1/admin/knowledge        │                   │                │           │
    │ {title, content, category, tags}    │                   │                │           │
    │──────────────▶│                     │                   │                │           │
    │               │                     │                   │                │           │
    │               │ saveDocument(...)   │                   │                │           │
    │               │────────────────────▶│                   │                │           │
    │               │                     │                   │                │           │
    │               │                     │ ① existsByTitleIgnoreCase(title)   │           │
    │               │                     │──────────────────────────────────────────────▶│
    │               │                     │   (true → throw "Duplicate title")│           │
    │               │                     │                   │                │           │
    │               │                     │ ② chunk(content)  │                │           │
    │               │                     │──────────────────▶│                │           │
    │               │                     │                   │                │           │
    │               │                     │   estimateTokens  │                │           │
    │               │                     │   ≤ 800 → [content]               │           │
    │               │                     │   > 800 → [chunk1, chunk2, ...]   │           │
    │               │                     │◀──────────────────│                │           │
    │               │                     │                   │                │           │
    │               │                     │ ③a. Single: embed(content)        │           │
    │               │                     │─────────────────────────────────▶│           │
    │               │                     │    POST /v1/embeddings            │           │
    │               │                     │    → float[1536]                  │           │
    │               │                     │◀─────────────────────────────────│           │
    │               │                     │                   │                │           │
    │               │                     │ ③b. Chunked: embedBatch(chunks)   │           │
    │               │                     │─────────────────────────────────▶│           │
    │               │                     │    → List<float[1536]>            │           │
    │               │                     │◀─────────────────────────────────│           │
    │               │                     │                   │                │           │
    │               │                     │ ④ INSERT knowledge_documents      │           │
    │               │                     │   (title, content, category,      │           │
    │               │                     │    tags, embedding, is_active)    │           │
    │               │                     │──────────────────────────────────────────────▶│
    │               │                     │                   │                │           │
    │ 201: KnowledgeDocumentResponse      │                   │                │           │
    │◀──────────────│                     │                   │                │           │
```

### Chi tiết code

**File:** `service/knowledge/KnowledgeService.java` → `saveDocument()`

```java
public List<KnowledgeDocument> saveDocument(String title, String content,
                                             String category, List<String> tags) {
    // ① Dedup check
    if (knowledgeDocumentRepository.existsByTitleIgnoreCase(title)) {
        throw new IllegalArgumentException("Document with title '" + title + "' already exists");
    }

    // ② Chunking
    List<String> chunks = textChunker.chunk(content);

    if (chunks.size() <= 1) {
        // Single document — embed trực tiếp
        float[] embedding = embeddingService.embed(content);
        KnowledgeDocument doc = buildDocument(title, content, category, tags, embedding);
        return List.of(knowledgeDocumentRepository.save(doc));
    }

    // Chunked — embed batch + tạo nhiều records
    List<float[]> embeddings = embeddingService.embedBatch(chunks);
    List<KnowledgeDocument> savedDocs = new ArrayList<>();
    for (int i = 0; i < chunks.size(); i++) {
        String chunkTitle = title + " [Part " + (i + 1) + "]";
        KnowledgeDocument doc = buildDocument(chunkTitle, chunks.get(i), category, tags, embeddings.get(i));
        savedDocs.add(knowledgeDocumentRepository.save(doc));
    }
    return savedDocs;
}
```

---

## 4.3 Text Chunking — Chi tiết

**File:** `service/knowledge/TextChunker.java`

### Tham số

| Param | Default | Mô tả |
|-------|---------|--------|
| `maxTokens` | 800 | Tối đa tokens/chunk |
| `overlapTokens` | 100 | Tokens overlap giữa 2 chunk liền kề |
| `TOKENS_PER_WORD` | 0.75 | Ước lượng token/từ cho tiếng Việt |

### Thuật toán

```
1. Ước lượng tokens = wordCount × 0.75
2. Nếu ≤ 800 tokens → return [content] (không chunk)
3. Nếu > 800 tokens:
   a. Chia thành chunks dựa trên word count
   b. Tìm natural break point (ưu tiên: paragraph > sentence > word)
   c. Overlap 100 tokens (~133 từ) giữa các chunk liền kề
```

### Natural Break Priority

```
Priority 1: Paragraph break (\n\n)
Priority 2: Sentence boundary (. ! ?)
Priority 3: Word boundary (fallback)
```

### Ví dụ

```
Document: 1200 từ ≈ 900 tokens > 800 threshold

→ Chunk 1: từ 1 - 1067  (800 tokens, tìm break gần nhất)
→ Chunk 2: từ 934 - 1200 (overlap 133 từ ≈ 100 tokens)
```

---

## 4.4 Sequence Diagram — Bulk Import

```
┌────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌────────┐
│ Admin  │  │KnowledgeController│ │ KnowledgeService │  │EmbeddingService│ │  DB    │
└───┬────┘  └────────┬─────────┘  └────────┬─────────┘  └──────┬───────┘  └───┬────┘
    │               │                     │                   │               │
    │ POST /admin/knowledge/bulk          │                   │               │
    │ Body: [{doc1}, {doc2}, ..., {docN}] │                   │               │
    │──────────────▶│                     │                   │               │
    │               │                     │                   │               │
    │               │ saveDocumentsBulk(requests)             │               │
    │               │────────────────────▶│                   │               │
    │               │                     │                   │               │
    │               │  Phase 1: Validate + Chunk              │               │
    │               │  ─────────────────────                  │               │
    │               │  FOR each request:                      │               │
    │               │    ① dedup check (title)                │               │
    │               │    ② chunk(content)                     │               │
    │               │    → allChunks[] (flattened list)       │               │
    │               │                     │                   │               │
    │               │  Phase 2: Batch Embed                   │               │
    │               │  ────────────────────                   │               │
    │               │                     │ embedBatch(       │               │
    │               │                     │   allChunks.      │               │
    │               │                     │   contents)       │               │
    │               │                     │──────────────────▶│               │
    │               │                     │                   │               │
    │               │                     │  (sub-batch       │               │
    │               │                     │   max 100/call)   │               │
    │               │                     │                   │               │
    │               │                     │ List<float[1536]> │               │
    │               │                     │◀──────────────────│               │
    │               │                     │                   │               │
    │               │  Phase 3: Persist                       │               │
    │               │  ────────────────                       │               │
    │               │  FOR each chunk + embedding:            │               │
    │               │    INSERT knowledge_documents           │               │
    │               │                     │──────────────────────────────────▶│
    │               │                     │                   │               │
    │ 200: {imported: N, failed: M, errors: [...]}           │               │
    │◀──────────────│                     │                   │               │
```

### Batch Embedding — Sub-batching

**File:** `service/knowledge/OpenAiEmbeddingService.java`

```java
// OpenAI API limit: max 100 texts per call
for (int batchStart = 0; batchStart < validTexts.size(); batchStart += 100) {
    int batchEnd = Math.min(batchStart + 100, validTexts.size());
    List<String> subBatch = validTexts.subList(batchStart, batchEnd);
    List<float[]> subResult = callBatchApi(subBatch);
    validEmbeddings.addAll(subResult);
}
```

**API call:**
```json
POST https://api.openai.com/v1/embeddings
{
  "input": ["text1", "text2", ...],
  "model": "text-embedding-3-small"
}
```

---

## 4.5 Sequence Diagram — Semantic Search (Runtime)

```
┌────────────┐  ┌──────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌────────┐
│RagRetriever│  │KnowledgeService│ │EmbeddingService  │  │   pgvector   │  │  LLM   │
└─────┬──────┘  └──────┬───────┘  └────────┬─────────┘  └──────┬───────┘  └───┬────┘
      │                │                   │                   │               │
      │ retrieve(userMessage)              │                   │               │
      │                │                   │                   │               │
      │ searchByText(msg, 0.75, 3)         │                   │               │
      │───────────────▶│                   │                   │               │
      │                │                   │                   │               │
      │                │ embed(userMessage) │                   │               │
      │                │──────────────────▶│                   │               │
      │                │                   │                   │               │
      │                │  POST /v1/embeddings                  │               │
      │                │  → float[1536]    │                   │               │
      │                │◀──────────────────│                   │               │
      │                │                   │                   │               │
      │                │ findSimilarDocuments(vector, 0.75, 3) │               │
      │                │──────────────────────────────────────▶│               │
      │                │                   │                   │               │
      │                │   SQL:                                │               │
      │                │   SELECT * FROM knowledge_documents   │               │
      │                │   WHERE is_active = true              │               │
      │                │     AND embedding IS NOT NULL         │               │
      │                │     AND (1-(embedding<=>:qv)) > 0.75  │               │
      │                │   ORDER BY embedding <=> :qv          │               │
      │                │   LIMIT 3                             │               │
      │                │                   │                   │               │
      │                │  Top-3 docs       │                   │               │
      │                │◀──────────────────────────────────────│               │
      │                │                   │                   │               │
      │ formatDocuments()                  │                   │               │
      │ → "[MEDICAL KNOWLEDGE - ...]       │                   │               │
      │    Tài liệu 1: <title>            │                   │               │
      │    <content>                       │                   │               │
      │    Tài liệu 2: ..."               │                   │               │
      │                │                   │                   │               │
      │ ragContext ────────────────────────────────────────────────────────────▶│
      │ (injected vào system prompt)       │                   │               │
```

### Graceful Fallback

```java
// RagRetriever.retrieve() — nếu embedding API fail → return null
try {
    List<KnowledgeDocument> documents = knowledgeService.searchByText(...);
    if (documents.isEmpty()) return null;
    return formatDocuments(documents);
} catch (Exception e) {
    log.error("RAG retrieval failed, proceeding without RAG context");
    return null; // Chat vẫn hoạt động, chỉ không có RAG context
}
```

---

## 4.6 CRUD Operations — Update & Delete

### Update Document

```java
// KnowledgeService.updateDocument()
public KnowledgeDocument updateDocument(Long id, String title, String content,
                                         String category, List<String> tags) {
    KnowledgeDocument existing = findById(id);
    boolean contentChanged = !existing.getContent().equals(content);

    existing.setTitle(title);
    existing.setContent(content);
    existing.setCategory(category);
    existing.setTags(tags);

    if (contentChanged) {
        // Re-embed chỉ khi content thay đổi
        float[] newEmbedding = embeddingService.embed(content);
        existing.setEmbedding(newEmbedding);
    }
    return knowledgeDocumentRepository.save(existing);
}
```

### Soft Delete

```java
// KnowledgeService.softDelete() — is_active = false, KHÔNG xóa khỏi DB
public void softDelete(Long id) {
    KnowledgeDocument document = findById(id);
    document.setIsActive(false);
    knowledgeDocumentRepository.save(document);
}
```

Document bị soft-delete sẽ **tự động bị loại khỏi search** vì query có `WHERE is_active = true`.

---

## 4.7 Tổng hợp APIs

| Method | Path | Mô tả |
|--------|------|--------|
| `POST` | `/api/v1/admin/knowledge` | Tạo 1 document (auto-chunk + embed) |
| `POST` | `/api/v1/admin/knowledge/bulk` | Bulk import nhiều documents |
| `GET` | `/api/v1/admin/knowledge?category=&page=&size=` | List documents (phân trang) |
| `GET` | `/api/v1/admin/knowledge/{id}` | Lấy 1 document |
| `PUT` | `/api/v1/admin/knowledge/{id}` | Update document (auto re-embed) |
| `DELETE` | `/api/v1/admin/knowledge/{id}` | Soft delete |
| `POST` | `/api/v1/admin/knowledge/search` | Semantic search |

---

*File: 04_flow_rag_build.md | Project: CareTalk Backend*
