# CareTalk Backend - 5 Cơ Chế Kỹ Thuật Nổi Bật

> Tài liệu giới thiệu 5 key features/cơ chế kỹ thuật đặc biệt của hệ thống CareTalk Backend,
> phục vụ trình bày báo cáo.

---

## Tổng quan kiến trúc

```
                        ┌─────────────────────────────────────────────┐
  Mobile App ──────────>│              Spring Boot 4.0                │
  (HTTP/SSE)            │                                             │
                        │  ┌─────────┐  ┌──────────┐  ┌───────────┐  │
                        │  │ Firebase│  │  Prompt   │  │    RAG    │  │
                        │  │  Auth   │  │ Assembler │  │ Pipeline  │  │
                        │  │  Filter │  │           │  │           │  │
                        │  └────┬────┘  └─────┬─────┘  └─────┬─────┘  │
                        │       │             │              │        │
                        │  ┌────┴────┐  ┌─────┴─────┐  ┌────┴──────┐ │
                        │  │  User   │  │    LLM    │  │ pgvector  │ │
                        │  │  Sync   │  │  Router   │  │ Cosine    │ │
                        │  │ Service │  │  (SSE)    │  │ Search    │ │
                        │  └────┬────┘  └─────┬─────┘  └────┬──────┘ │
                        │       │             │              │        │
                        │  ┌────┴─────────────┴──────────────┴──────┐ │
                        │  │           PostgreSQL + pgvector         │ │
                        │  └────────────────────────────────────────┘ │
                        └─────────────────────────────────────────────┘
```

---

## Feature 1: RAG Pipeline - Truy xuất tri thức y tế tăng cường

### Vấn đề giải quyết

Chatbot AI thông thường chỉ trả lời dựa trên kiến thức huấn luyện sẵn. Trong lĩnh vực y tế, cần bổ sung kiến thức chuyên ngành cập nhật mà không phải huấn luyện lại mô hình.

### Cơ chế hoạt động

```
Tài liệu y tế                    Câu hỏi bệnh nhân
     │                                  │
     v                                  v
┌──────────┐                    ┌──────────────┐
│  Text    │                    │   Embedding  │
│ Chunker  │                    │  (OpenAI)    │
│ 800 token│                    └──────┬───────┘
│ overlap  │                           │
│  100     │                           v
└────┬─────┘                    ┌──────────────┐
     │                          │  Similarity  │
     v                          │  Search      │
┌──────────┐                    │  pgvector    │
│  Batch   │───── lưu vào ────> │  cosine <=>  │
│ Embedding│     PostgreSQL     │  top-3       │
│ (OpenAI) │                    │  >= 0.75     │
└──────────┘                    └──────┬───────┘
                                       │
                                       v
                                ┌──────────────┐
                                │[MEDICAL      │
                                │ KNOWLEDGE]   │
                                │ context      │
                                └──────────────┘
```

### Chi tiết kỹ thuật trong code

**TextChunker** - Chia nhỏ tài liệu thông minh:
- Kích thước mỗi chunk: 800 tokens, overlap 100 tokens
- Tỷ lệ token/word cho tiếng Việt: 0.75 (được tinh chỉnh riêng)
- Ưu tiên cắt tại ranh giới tự nhiên: đoạn văn > câu > từ
- Tránh cắt giữa câu để giữ ngữ nghĩa

**RagRetriever** - Truy xuất tài liệu liên quan:
- Dùng pgvector với cosine distance (`<=>`) để tìm kiếm ngữ nghĩa
- Ngưỡng similarity: >= 0.75 (chỉ lấy tài liệu thực sự liên quan)
- Top-K: 3 tài liệu (cân bằng giữa chất lượng và token cost)
- Graceful fallback: nếu RAG thất bại, chatbot vẫn trả lời bình thường

### Điểm nổi bật để trình bày

- Không cần huấn luyện lại mô hình khi thêm tài liệu y tế mới
- pgvector tích hợp trực tiếp trong PostgreSQL, không cần database vector riêng
- Chunking overlap đảm bảo ngữ cảnh không bị mất tại điểm cắt

---

## Feature 2: Prompt Assembly Pipeline - Lắp ráp ngữ cảnh 4 lớp

### Vấn đề giải quyết

Mỗi câu trả lời của chatbot cần kết hợp nhiều nguồn thông tin: chỉ dẫn hệ thống, kiến thức y tế, lịch sử hội thoại, và câu hỏi hiện tại. Nếu ghép thủ công sẽ rối, khó bảo trì, và dễ vượt giới hạn token.

### Cơ chế hoạt động

```
┌─────────────────────────────────────────────────────────┐
│                   PROMPT ASSEMBLER                      │
│                                                         │
│  Lớp 1: System Prompt                                   │
│  ┌─────────────────────────────────────────────────┐    │
│  │ "Bạn là chatbot tư vấn y tế của phòng khám..." │    │
│  │ (từ SystemPromptResolver, lưu trong DB)         │    │
│  └─────────────────────────────────────────────────┘    │
│           +                                             │
│  Lớp 2: RAG Context                                     │
│  ┌─────────────────────────────────────────────────┐    │
│  │ [MEDICAL KNOWLEDGE]                              │    │
│  │ Tài liệu 1: Triệu chứng sốt ở trẻ em...       │    │
│  │ (từ RagRetriever, có thể null)                   │    │
│  └─────────────────────────────────────────────────┘    │
│           +                                             │
│  Lớp 3: Summary / History                               │
│  ┌─────────────────────────────────────────────────┐    │
│  │ [TÓM TẮT CUỘC HỘI THOẠI TRƯỚC ĐÓ]             │    │
│  │ Triệu chứng: đau đầu 3 ngày, sốt 38.5...       │    │
│  │ HOẶC: toàn bộ lịch sử tin nhắn (nếu <= 20)     │    │
│  └─────────────────────────────────────────────────┘    │
│           +                                             │
│  Lớp 4: User Message                                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │ "Tôi uống paracetamol 2 ngày rồi mà vẫn sốt"  │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ═══════════════════════════════════════════════════     │
│  => LlmRequest (model, temperature, messages[])         │
└─────────────────────────────────────────────────────────┘
```

### Chi tiết kỹ thuật trong code

- **SystemPromptResolver**: Đọc prompt từ database, cho phép thay đổi hành vi chatbot mà không cần deploy lại
- **PromptAssembler.composeSystemMessage()**: Ghép 3 lớp đầu thành 1 system message duy nhất
- **ContextBuilder**: Tự động chọn chiến lược - gửi toàn bộ lịch sử nếu <= 20 tin nhắn, hoặc gửi tóm tắt + tin nhắn gần nhất nếu > 20 tin nhắn

### Điểm nổi bật để trình bày

- Kiến trúc pipeline giúp mỗi lớp độc lập, dễ test và bảo trì
- System prompt lưu trong DB cho phép thay đổi hành vi không cần deploy
- Mỗi lớp đều optional (RAG có thể null, summary có thể null) - hệ thống vẫn hoạt động bình thường

---

## Feature 3: Auto-Summary - Tóm tắt hội thoại dài tự động

### Vấn đề giải quyết

Hội thoại y tế có thể kéo dài hàng trăm tin nhắn. Nếu gửi toàn bộ lịch sử cho LLM, sẽ vượt giới hạn token và tốn chi phí. Nếu cắt bỏ, sẽ mất thông tin y tế quan trọng (triệu chứng, thuốc đã dùng, tiền sử).

### Cơ chế hoạt động

```
Tin nhắn 1-20: Gửi toàn bộ lịch sử
                │
                v (vượt 20 tin nhắn)

Tin nhắn 21+:  Kích hoạt Auto-Summary
                │
                v
┌──────────────────────────────────────────────┐
│  SummaryGenerator (chạy bất đồng bộ @Async) │
│                                              │
│  Lần đầu:                                   │
│  M1..M10 ──> LLM (gpt-4o-mini) ──> Summary  │
│  M11..M20 giữ nguyên làm recent             │
│                                              │
│  Lần sau (regenerate):                       │
│  Summary cũ + M mới ──> LLM ──> Summary mới │
│                                              │
│  Kết quả lưu vào:                            │
│  conversation.summary (text)                 │
│  conversation.summary_until_id (Long)        │
└──────────────────────────────────────────────┘
```

### Chi tiết kỹ thuật trong code

- **Ngưỡng kích hoạt**: > 20 tin nhắn (SUMMARY_THRESHOLD = 20)
- **Giữ lại gần nhất**: 10 tin nhắn (RECENT_MESSAGES_TO_KEEP = 10)
- **Chạy bất đồng bộ**: `@Async` - không ảnh hưởng thời gian phản hồi chat
- **Model tối ưu chi phí**: dùng gpt-4o-mini (rẻ hơn 10x so với gpt-4o)
- **Prompt chuyên dụng y tế**: bắt buộc giữ lại triệu chứng, thuốc, chỉ số y tế, tiền sử, diễn biến
- **Regenerate thông minh**: kết hợp tóm tắt cũ + tin nhắn mới thay vì tóm tắt lại từ đầu

### Điểm nổi bật để trình bày

- Giải quyết bài toán "lost context" trong hội thoại dài mà không mất thông tin y tế
- Chi phí thấp nhờ dùng model mini + chạy nền không chặn user
- Cấu trúc tóm tắt y khoa chuẩn (triệu chứng/thuốc/tiền sử/diễn biến)

---

## Feature 4: SSE Streaming - Phản hồi thời gian thực

### Vấn đề giải quyết

LLM mất 3-10 giây để sinh câu trả lời đầy đủ. Nếu chờ hết mới gửi, người dùng sẽ thấy ứng dụng "đứng". SSE Streaming cho phép gửi từng từ ngay khi LLM sinh ra.

### Cơ chế hoạt động

```
Mobile App                    Backend                    OpenAI
    │                            │                          │
    │── POST /chat ──────────>   │                          │
    │                            │── POST /completions ──>  │
    │                            │   (stream: true)         │
    │                            │                          │
    │   <── SSE: "Triệu" ─────  │  <── chunk: "Triệu" ──  │
    │   <── SSE: " chứng" ─────  │  <── chunk: " chứng" ── │
    │   <── SSE: " của" ───────  │  <── chunk: " của" ────  │
    │   <── SSE: " bạn..." ───── │  <── chunk: " bạn..." ── │
    │   ...                      │   ...                    │
    │   <── SSE: [DONE] ───────  │  <── [DONE] ───────────  │
    │                            │                          │
    │                            │── Lưu message vào DB ──> │
    │                            │   (sau khi stream xong)  │
```

### Chi tiết kỹ thuật trong code

- **OpenAiClient**: Sử dụng WebClient (reactive) + `Flux<String>` cho streaming
- **extractContent()**: Parse từng SSE chunk, trích xuất `choices[0].delta.content`
- **Error handling**: `onErrorResume` - nếu streaming lỗi giữa chừng, trả về thông báo lỗi thay vì crash
- **Nginx config**: `proxy_buffering off` + `proxy_cache off` để đảm bảo SSE không bị buffer

### Điểm nổi bật để trình bày

- Trải nghiệm "ChatGPT-like" - người dùng thấy câu trả lời xuất hiện từng từ
- Reactive pipeline (WebFlux) xử lý hiệu quả, không block thread
- Graceful error: lỗi giữa stream vẫn trả về thông báo thay vì treo

---

## Feature 5: Dual Authentication - Xác thực song song Firebase + API Key

### Vấn đề giải quyết

Hệ thống cần hỗ trợ 2 loại người dùng hoàn toàn khác nhau:
- **Người dùng đăng nhập** (Firebase Auth): BN, BS, Admin - có tài khoản, lưu lịch sử
- **Người dùng ẩn danh** (API Key): Khách vãng lai - không cần đăng ký, giới hạn 10 tin nhắn, tự xóa sau 24h

### Cơ chế hoạt động

```
Request đến
     │
     v
┌─────────────────────────────────┐
│     Security Filter Chain       │
│                                 │
│  /api/v1/public/** ────────────>│── PublicApiKeyFilter
│  (Anonymous chat)               │   Header: X-API-Key
│  Không cần đăng nhập            │   Giới hạn 10 msg/session
│                                 │
│  /api/v1/** ───────────────────>│── FirebaseAuthFilter
│  (Authenticated)                │   Header: Authorization
│  Firebase ID Token              │   Bearer <token>
│  Auto-sync user vào PostgreSQL  │
│                                 │
└─────────────────────────────────┘
         │                    │
         v                    v
┌──────────────┐    ┌──────────────────┐
│ UserSync     │    │ AnonymousCleanup │
│ Service      │    │ Job              │
│ (tạo user    │    │ (xóa session     │
│  lần đầu)    │    │  sau 24h,        │
│              │    │  chạy mỗi 1h)    │
└──────────────┘    └──────────────────┘
```

### Chi tiết kỹ thuật trong code

**FirebaseAuthFilter**:
- Xác thực Firebase ID Token qua Firebase Admin SDK
- Gọi `UserSyncService.syncUser()` - tự tạo user trong PostgreSQL nếu lần đầu đăng nhập
- Role mặc định: PATIENT, Admin tạo tài khoản cho DOCTOR

**PublicApiKeyFilter**:
- Xác thực bằng API Key đơn giản (header `X-API-Key`)
- Tạo conversation type = ANONYMOUS
- Giới hạn 10 tin nhắn mỗi session

**AnonymousCleanupJob**:
- Chạy mỗi 1 giờ (`@Scheduled(fixedRate = 3600000)`)
- Tìm conversation ANONYMOUS tạo trước 24h
- Hard-delete: xóa messages trước (FK constraint), sau đó xóa conversation
- Đảm bảo không tích tụ dữ liệu ẩn danh

### Điểm nổi bật để trình bày

- Cùng 1 backend phục vụ 2 loại user hoàn toàn khác nhau
- Firebase Auth cho bảo mật cấp doanh nghiệp, API Key cho trải nghiệm nhanh
- PostgreSQL là Source of Truth cho user role/status (không phụ thuộc Firebase Claims)
- Dữ liệu ẩn danh tự dọn dẹp, không vi phạm quyền riêng tư

---

## Tóm tắt 5 Features cho slide

| # | Feature | Vấn đề | Giải pháp | Công nghệ |
|---|---------|--------|-----------|-----------|
| 1 | RAG Pipeline | Chatbot thiếu kiến thức y tế chuyên sâu | Embedding + tìm kiếm ngữ nghĩa | pgvector, OpenAI Embeddings |
| 2 | Prompt Assembly | Ghép nhiều nguồn ngữ cảnh phức tạp | Pipeline 4 lớp tự động | PromptAssembler pattern |
| 3 | Auto-Summary | Hội thoại dài mất ngữ cảnh + tốn token | Tóm tắt y tế bất đồng bộ | @Async, gpt-4o-mini |
| 4 | SSE Streaming | Chờ phản hồi lâu, UX kém | Stream từng từ thời gian thực | WebFlux, Flux, SSE |
| 5 | Dual Auth | 2 loại user, yêu cầu bảo mật khác nhau | Filter chain kép + auto-cleanup | Firebase Auth, @Scheduled |
