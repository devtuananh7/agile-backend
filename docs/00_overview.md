# CareTalk Backend — Product Documentation

> Tài liệu mô tả chi tiết hệ thống backend cho ứng dụng CareTalk — chatbot tư vấn sức khỏe AI.

---

## 1. Tổng quan hệ thống

**CareTalk** là ứng dụng chatbot tư vấn sức khỏe sử dụng AI, hỗ trợ người dùng (bệnh nhân) nhận tư vấn y tế sơ bộ thông qua hội thoại với GPT-4o, được tăng cường bởi hệ thống RAG (Retrieval-Augmented Generation) với dữ liệu y tế chuyên ngành.

## 2. Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.0 |
| Language | Java 25 |
| Database | PostgreSQL 15+ với pgvector |
| Auth | Firebase Authentication |
| AI/LLM | OpenAI GPT-4o (SSE streaming) |
| Embedding | OpenAI text-embedding-3-small (1536d) |
| Vector Search | pgvector cosine similarity |
| Summary | GPT-4o-mini (async) |

## 3. Kiến trúc tổng quan

```
Android App ──(Firebase ID Token)──▶ Nginx :443 ──▶ Spring Boot :8080
                                                        │
                                        ┌───────────────┼───────────────┐
                                        ▼               ▼               ▼
                                   PostgreSQL     OpenAI API      Firebase Auth
                                   + pgvector     (LLM + Embed)   (verify token)
```

## 4. Database Schema

| Table | PK | Mô tả |
|-------|-----|-------|
| `users` | UUID | Thông tin user, role, status, Firebase UID |
| `conversations` | BIGSERIAL | Session hội thoại, trạng thái, loại (BOT/ESCALATED/ANONYMOUS) |
| `messages` | BIGSERIAL | Tin nhắn trong conversation |
| `system_prompts` | BIGSERIAL | Prompt template cho AI |
| `knowledge_documents` | BIGSERIAL | Tài liệu RAG với embedding vector(1536) |

## 5. Roles & Permissions

| Role | Mô tả | Endpoints |
|------|--------|-----------|
| `PATIENT` | Bệnh nhân (auto-created khi login lần đầu) | `/api/v1/users/**`, `/api/v1/chatbot/**`, `/api/v1/conversations/**` |
| `DOCTOR` | Bác sĩ (admin tạo) | Tất cả PATIENT + `/api/v1/users/doctor/**` |
| `ADMIN` | Quản trị viên | Tất cả + `/api/v1/admin/**` |
| Anonymous | Người dùng chưa đăng nhập | `/api/v1/public/**` (cần X-API-Key) |

## 6. Security Layers

```
Request
  │
  ▼
PublicApiKeyFilter ──── /api/v1/public/** → validate X-API-Key
  │
  ▼
FirebaseAuthFilter ──── Tất cả endpoints khác → verify Firebase ID Token
  │
  ▼
SecurityConfig ──── Role-based: /api/v1/admin/** → ROLE_ADMIN
  │
  ▼
Controller
```

## 7. Danh sách luồng chính

| # | Luồng | File tài liệu |
|---|-------|----------------|
| 1 | Luồng User (Auth, Profile) | [01_flow_user.md](./01_flow_user.md) |
| 2 | Luồng Chat (Authenticated + Anonymous) | [02_flow_chat.md](./02_flow_chat.md) |
| 3 | Luồng Quản lý Bác sĩ (Admin) | [03_flow_doctor_management.md](./03_flow_doctor_management.md) |
| 4 | Luồng Build RAG (Knowledge Base) | [04_flow_rag_build.md](./04_flow_rag_build.md) |
| 5 | Tài liệu API chi tiết | [05_api_reference.md](./05_api_reference.md) |

---
