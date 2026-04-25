# Tài liệu API Reference — CareTalk Backend

> API bàn giao chi tiết cho tất cả endpoints. Base URL: `http://localhost:8080`

---

## Authentication

### Authenticated Endpoints
Tất cả endpoints (trừ `/api/v1/public/**`) yêu cầu Firebase ID Token:
```
Authorization: Bearer <firebase-id-token>
```

### Public Endpoints
Endpoints `/api/v1/public/**` yêu cầu API Key:
```
X-API-Key: <configured-api-key>
```

### Role-based Access
- `/api/v1/admin/**` → yêu cầu `ROLE_ADMIN`
- Tất cả endpoints khác → yêu cầu authenticated (any role)

---

## 1. User APIs

### 1.1 GET /api/v1/users/me
Lấy profile user hiện tại.

**Auth:** Bearer Token (any role)

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firebaseUid": "abc123xyz",
  "email": "user@example.com",
  "phoneNumber": "0901234567",
  "role": "PATIENT",
  "status": "ACTIVE",
  "authProvider": "google.com",
  "metadata": {
    "fullName": "Nguyễn Văn A",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE"
  },
  "createdAt": "2026-04-20T10:00:00",
  "updatedAt": "2026-04-20T10:00:00"
}
```

**Response 404:** User không tồn tại

---

### 1.2 PUT /api/v1/users/me
Cập nhật profile user.

**Auth:** Bearer Token (any role)

**Request:**
```json
{
  "phoneNumber": "0901234567",
  "metadata": {
    "fullName": "Nguyễn Văn A",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "Hà Nội",
    "bloodType": "O+",
    "allergies": "Penicillin",
    "medicalHistory": "Cao huyết áp"
  }
}
```

**Response 200:** Updated User object (giống GET /me)

---

### 1.3 POST /api/v1/users/register-firebase
Đăng ký thông tin bổ sung sau khi login Firebase lần đầu.

**Auth:** Bearer Token

**Request:** Giống PUT /me

**Response 200:** Updated User object

---

### 1.4 PUT /api/v1/users/doctor/profile
Doctor cập nhật profile chuyên môn. Status tự động chuyển sang `PENDING_APPROVAL`.

**Auth:** Bearer Token (DOCTOR)

**Request:**
```json
{
  "metadata": {
    "fullName": "BS. Trần Thị B",
    "specialization": "Tim mạch",
    "hospital": "Bệnh viện Bạch Mai",
    "licenseNumber": "BS-12345",
    "yearsOfExperience": 10,
    "bio": "Bác sĩ chuyên khoa Tim mạch với 10 năm kinh nghiệm."
  }
}
```

**Response 200:** Updated User (status = `PENDING_APPROVAL`)

---

## 2. Chat APIs

### 2.1 POST /api/v1/chatbot/chat
Chat với AI bot (SSE streaming). Dành cho authenticated users.

**Auth:** Bearer Token (any role)

**Request:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Tôi bị đau đầu 3 ngày không bớt"
}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|--------|
| `sessionId` | UUID | No | ID session để resume conversation. Null = tạo mới |
| `message` | String | Yes | Nội dung tin nhắn |

**Response:** SSE Stream (`text/event-stream`)

```
event: session
data: {"sessionId":"550e8400-...", "conversationId":42}

event: message
data: Xin

event: message
data:  chào

event: message
data: ! Tôi

event: message
data:  sẽ hỗ trợ...

event: done
data: {"messageId":123}
```

**Error Events:**
```
event: error
data: {"error":"Chat pipeline error message"}
```

---

### 2.2 POST /api/v1/public/chat
Chat ẩn danh (SSE streaming). Không cần Firebase auth.

**Auth:** `X-API-Key` header

**Request:** Giống `/api/v1/chatbot/chat`

**Response:** Giống authenticated chat, với thêm event:

```
event: limit_reached
data: {"error": "Message limit reached", "suggestion": "register"}
```

**Giới hạn:** 10 tin nhắn USER/session. Dữ liệu tự xóa sau 24h.

---

## 3. Conversation APIs

### 3.1 GET /api/v1/conversations
Danh sách conversations của user hiện tại (phân trang).

**Auth:** Bearer Token

**Query Params:**

| Param | Default | Mô tả |
|-------|---------|--------|
| `page` | 0 | Trang (0-indexed) |
| `size` | 10 | Số items/trang |

**Response 200:**
```json
{
  "content": [
    {
      "id": 42,
      "sessionId": "550e8400-...",
      "refId": null,
      "status": "ACTIVE",
      "userId": "firebase-uid-123",
      "username": "Nguyễn Văn A",
      "doctor": "BOT",
      "type": "BOT",
      "promptName": "medical_general",
      "conclusion": null,
      "summary": null,
      "createdAt": "2026-04-20T10:00:00",
      "updatedAt": "2026-04-20T10:30:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### 3.2 GET /api/v1/conversations/{id}
Chi tiết 1 conversation.

**Response 200:** Single ConversationResponse object

**Response 404:** Conversation không tồn tại

---

### 3.3 GET /api/v1/conversations/{id}/messages
Lịch sử tin nhắn (phân trang, sắp xếp theo thời gian ASC).

**Query Params:** `page` (default 0), `size` (default 20)

**Response 200:**
```json
{
  "content": [
    {
      "id": 100,
      "conversationId": 42,
      "senderId": "firebase-uid-123",
      "senderRole": "USER",
      "content": "Tôi bị đau đầu",
      "contentType": "TEXT",
      "metadata": null,
      "createdAt": "2026-04-20T10:00:00"
    },
    {
      "id": 101,
      "conversationId": 42,
      "senderId": "BOT",
      "senderRole": "BOT",
      "content": "Xin chào! Tôi sẽ hỗ trợ bạn...",
      "contentType": "TEXT",
      "metadata": null,
      "createdAt": "2026-04-20T10:00:05"
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

**Enum Values:**
- `senderRole`: `USER`, `BOT`, `DOCTOR`, `SYSTEM`
- `contentType`: `TEXT`

---

### 3.4 PUT /api/v1/conversations/{id}/escalate
Chuyển conversation BOT sang bác sĩ tư vấn.

**Request:**
```json
{
  "doctorId": "dr-firebase-uid-456"
}
```

**Response 200:** ConversationResponse mới (type = `ESCALATED`, refId = old conv ID)

**Response 404:** Conversation không tồn tại

**Response 400:** Conversation không phải BOT hoặc không ACTIVE

---

### 3.5 PUT /api/v1/conversations/{id}/close
Đóng conversation (status → `DONE`).

**Response 200:** Updated ConversationResponse

---

## 4. Admin — User Management APIs

### 4.1 POST /api/v1/admin/users/doctor
Admin tạo tài khoản bác sĩ.

**Auth:** Bearer Token (ADMIN only)

**Request:**
```json
{
  "email": "doctor@hospital.vn",
  "phoneNumber": "0987654321",
  "initialPassword": "TempPass123!"
}
```

**Response 201:** User object (role = `DOCTOR`, status = `PENDING_ACTIVATION`)

**Response 409:**
```json
{"error": "Email is already in use."}
```

---

### 4.2 PATCH /api/v1/admin/users/{id}/approve
Admin phê duyệt bác sĩ.

**Auth:** Bearer Token (ADMIN only)

**Path Param:** `id` — UUID của doctor

**Response 200:** User object (status = `ACTIVE`)

---

## 5. Admin — Knowledge Management APIs

### 5.1 POST /api/v1/admin/knowledge
Tạo 1 knowledge document. Tự động chunk nếu content > 800 tokens.

**Auth:** Bearer Token (ADMIN only)

**Request:**
```json
{
  "title": "Hướng dẫn xử trí ho kéo dài",
  "content": "Ho kéo dài là tình trạng ho liên tục trên 3 tuần...",
  "category": "symptoms",
  "tags": ["ho", "cough", "ho kéo dài"]
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `title` | String | Yes | Unique (case-insensitive) |
| `content` | String | Yes | Non-blank |
| `category` | String | No | `symptoms`, `drugs`, `diseases`, `first-aid`, `faq` |
| `tags` | String[] | No | 3-6 keywords |

**Response 201 (single):**
```json
{
  "id": 5,
  "title": "Hướng dẫn xử trí ho kéo dài",
  "content": "Ho kéo dài...",
  "category": "symptoms",
  "tags": ["ho", "cough", "ho kéo dài"],
  "isActive": true,
  "createdAt": "2026-04-20T10:00:00",
  "updatedAt": "2026-04-20T10:00:00"
}
```

**Response 201 (chunked):**
```json
{
  "chunked": true,
  "totalParts": 3,
  "documents": [
    {"id": 5, "title": "Hướng dẫn xử trí ho kéo dài [Part 1]", ...},
    {"id": 6, "title": "Hướng dẫn xử trí ho kéo dài [Part 2]", ...},
    {"id": 7, "title": "Hướng dẫn xử trí ho kéo dài [Part 3]", ...}
  ]
}
```

**Response 409:**
```json
{"error": "Document with title 'xxx' already exists"}
```

---

### 5.2 POST /api/v1/admin/knowledge/bulk
Bulk import nhiều documents.

**Request:** Array of KnowledgeDocumentRequest
```json
[
  {"title": "Doc 1", "content": "...", "category": "symptoms", "tags": [...]},
  {"title": "Doc 2", "content": "...", "category": "drugs", "tags": [...]}
]
```

**Response 200:**
```json
{
  "imported": 8,
  "failed": 2,
  "errors": [
    {"title": "Doc bị trùng", "reason": "Duplicate title"},
    {"title": "Doc khác", "reason": "Duplicate title"}
  ]
}
```

---

### 5.3 GET /api/v1/admin/knowledge
List documents (phân trang).

**Query Params:**

| Param | Default | Mô tả |
|-------|---------|--------|
| `category` | (all) | Filter theo category |
| `page` | 0 | Trang |
| `size` | 20 | Items/trang |
| `sort` | `createdAt,DESC` | Sắp xếp |

**Response 200:** Paginated KnowledgeDocumentResponse

---

### 5.4 GET /api/v1/admin/knowledge/{id}
Lấy 1 document theo ID.

**Response 200:** KnowledgeDocumentResponse

**Response 404:** `{"error": "Document not found with id: X"}`

---

### 5.5 PUT /api/v1/admin/knowledge/{id}
Update document. Tự động re-embed nếu content thay đổi.

**Request:** Giống POST (title, content, category, tags)

**Response 200:** Updated KnowledgeDocumentResponse

---

### 5.6 DELETE /api/v1/admin/knowledge/{id}
Soft delete (is_active = false).

**Response 204:** No Content

**Response 404:** Document không tồn tại

---

### 5.7 POST /api/v1/admin/knowledge/search
Semantic search trong knowledge base.

**Request:**
```json
{
  "query": "tôi bị ho 2 tuần không bớt",
  "category": "symptoms",
  "threshold": 0.65,
  "topK": 5
}
```

| Field | Type | Default | Mô tả |
|-------|------|---------|--------|
| `query` | String | (required) | Câu truy vấn |
| `category` | String | null | Filter category trước khi search |
| `threshold` | Double | 0.75 | Cosine similarity tối thiểu (0.0-1.0) |
| `topK` | Integer | 3 | Số kết quả tối đa |

**Response 200:**
```json
[
  {
    "id": 1,
    "title": "Hướng dẫn xử trí ho kéo dài",
    "content": "Ho kéo dài > 3 tuần...",
    "category": "symptoms",
    "tags": ["ho", "cough"],
    "isActive": true,
    "createdAt": "2026-04-20T10:00:00",
    "updatedAt": "2026-04-20T10:00:00"
  }
]
```

---

## 6. Enum Reference

### Role
| Value | Mô tả |
|-------|--------|
| `PATIENT` | Bệnh nhân (auto-created) |
| `DOCTOR` | Bác sĩ (admin-created) |
| `ADMIN` | Quản trị viên (manual SQL) |

### AccountStatus
| Value | Mô tả |
|-------|--------|
| `ACTIVE` | Hoạt động |
| `INACTIVE` | Ngưng hoạt động |
| `BLOCKED` | Bị khóa |
| `PENDING_ACTIVATION` | Doctor mới tạo, chưa login |
| `PENDING_APPROVAL` | Doctor đã cập nhật profile, chờ admin duyệt |

### ConversationStatus
| Value | Mô tả |
|-------|--------|
| `ACTIVE` | Đang mở |
| `DONE` | Đã đóng |

### ConversationType
| Value | Mô tả |
|-------|--------|
| `BOT` | Chat với AI |
| `ESCALATED` | Chuyển sang bác sĩ |
| `ANONYMOUS` | Chat ẩn danh |

### SenderRole
| Value | Mô tả |
|-------|--------|
| `USER` | Người dùng (bệnh nhân) |
| `BOT` | AI chatbot |
| `DOCTOR` | Bác sĩ |
| `SYSTEM` | Tin nhắn hệ thống |

### ContentType
| Value | Mô tả |
|-------|--------|
| `TEXT` | Tin nhắn văn bản |

---

## 7. Error Response Format

Tất cả lỗi trả về dạng JSON:
```json
{"error": "Mô tả lỗi"}
```

| HTTP Code | Mô tả |
|-----------|--------|
| 401 | Token expired / Invalid token / Missing API key |
| 403 | Account disabled / Insufficient role |
| 404 | Resource not found |
| 409 | Duplicate (email, title) |
| 400 | Invalid request |

---

*File: 05_api_reference.md | Project: CareTalk Backend*
