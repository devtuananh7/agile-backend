# Tài Liệu Thiết Kế Kỹ Thuật: CareTalk - Chatbot Phòng Khám Gia Đình

Tài liệu này định nghĩa kiến trúc công nghệ (Tech Stack), thiết kế cơ sở dữ liệu (Database Schema) và danh sách các API (API Map) cốt lõi dựa trên tài liệu **Product Overview Document** (v1.0).

---

## 1. Đề Xuất Công Nghệ (Tech Stack)

Kiến trúc kết hợp hệ thống hướng luồng nghiệp vụ khắt khe (Transaction) và trí tuệ nhân tạo (AI/LLM).

- **Backend (Core & Transaction):** `Spring Boot (Java)` hoặc `NestJS (Node.js)`. Quản lý luồng ca bệnh, xác thực tài khoản và tính toàn vẹn dữ liệu y tế.
- **Backend (AI & Triage):** `FastAPI (Python)`. Tổ chức thành một microservice nội bộ phục vụ RAG (Retrieval-Augmented Generation) và xử lý Prompt cho Chatbot.
- **Database (Relational):** `PostgreSQL`. Đảm bảo chuẩn giao dịch ACID. Kết hợp extension `PgVector` để lưu trữ tài liệu y khoa cho Vector Query (RAG).
- **Caching & Real-time:** `Redis` (Pub/Sub cho tin nhắn chat) + `WebSockets` / `Firebase Cloud Messaging (FCM)`.
- **LLM Engine:** `OpenAI API (GPT-4o)` kết hợp `LangChain` framework.

---

## 2. Thiết Kế Cơ Sở Dữ Liệu (Database Schema)

Hệ thống sử dụng cơ sở dữ liệu quan hệ. Dưới đây là các bảng (tables) quan trọng và mối quan hệ thực thể (ER).

### 2.1 Bảng `users` (Danh tính người dùng)
Dùng chung cho cả 3 roles (Patient, Doctor, Admin). Định danh và xác thực.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `email` | VARCHAR | UNIQUE, NOT NULL | Tài khoản đăng nhập |
| `password_hash` | VARCHAR | NOT NULL | Mật khẩu đã mã hóa (BCrypt) |
| `role` | VARCHAR | NOT NULL | Enum: `PATIENT`, `DOCTOR`, `ADMIN` |
| `full_name` | VARCHAR | NOT NULL | Họ và tên |
| `phone` | VARCHAR | | Số điện thoại liên hệ |
| `date_of_birth`| DATE | | Ngày sinh (để phục vụ chuẩn đoán) |
| `status` | VARCHAR | DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE`, `PENDING_APPROVAL`|
| `created_at` | TIMESTAMP | | Ngày tạo tài khoản |

### 2.2 Bảng `doctor_profiles` (Hồ sơ bác sĩ)
Lưu các thông tin chuyên ngành của bác sĩ, quan hệ `1 - 1` với bảng `users`.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `user_id` | UUID | FK -> `users(id)` | Liên kết tới user (role=DOCTOR) |
| `specialty` | VARCHAR | NOT NULL | Chuyên khoa (VD: Đa khoa, Nhi khoa) |
| `license_number`| VARCHAR | UNIQUE | Mã số chứng chỉ hành nghề y |
| `description` | TEXT | | Thông tin kinh nghiệm làm việc |

### 2.3 Bảng `chatbot_sessions` (Phiên hội thoại AI)
Quản lý lịch sử hỏi bệnh với AI trước khi bệnh nhân quyết định tạo ca nội trú từ kết quả này.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính (Session ID) |
| `patient_id` | UUID | FK -> `users(id)` | Bệnh nhân đang chat |
| `triage_level` | VARCHAR | | Enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `ai_summary` | TEXT | | AI tự động tóm tắt các triệu chứng rút ra từ chat |
| `status` | VARCHAR | DEFAULT 'ACTIVE' | `ACTIVE`, `CONVERTED_TO_CASE` |
| `created_at` | TIMESTAMP | | Khởi tạo phiên |

### 2.4 Bảng `consultation_cases` (Ca Tư Vấn / Khám Bệnh)
Trung tâm của hệ thống, lưu trạng thái quá trình kết nối Patient và Doctor.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính (Case ID) |
| `patient_id` | UUID | FK -> `users(id)` | Bệnh nhân tạo ca |
| `doctor_id` | UUID | FK -> `users(id)` | Bác sĩ tiếp nhận ca (ban đầu là NULL) |
| `session_id` | UUID | FK -> `chatbot_sessions`| Link lưu vết quá trình chat AI Triage |
| `status` | VARCHAR | NOT NULL | Enum: `PENDING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED` |
| `symptom_desc` | TEXT | | Mô tả lúc khởi tạo ca của bệnh nhân |
| `priority` | VARCHAR | | Ưu tiên của ca khám (sinh từ Triage Level của AI) |
| `created_at` | TIMESTAMP | | Ngày tạo ca |

### 2.5 Bảng `messages` (Tin nhắn hội thoại y tế)
Các nội dung chat thực tế theo **Case** giữa Doctor và Patient.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `case_id` | UUID | FK -> `consultation_cases`| Tin nhắn thuộc ca bệnh nào |
| `sender_id` | UUID | FK -> `users(id)` | Sinh ra từ chiều Bệnh nhân hoặc Bác sĩ |
| `content` | TEXT | | Nội dung văn bản |
| `attachment_url`| VARCHAR | | Link ảnh/video khám bệnh (nếu có) |
| `created_at` | TIMESTAMP | | Thời gian gửi |

### 2.6 Bảng `conclusions` (Kết luận y khoa)
Kết quả cuối cùng do bác sĩ đưa ra trước khi đóng ca.

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `case_id` | UUID | UNIQUE, FK -> `cases(id)` | *Note:* 1 Case chỉ có đúng 1 Conclusion |
| `doctor_id` | UUID | FK -> `users(id)` | Bác sĩ chuẩn đoán |
| `diagnosis` | TEXT | NOT NULL | Bệnh lý được chẩn đoán |
| `advice` | TEXT | NOT NULL | Lời khuyên, thuốc sử dụng tại nhà |
| `created_at` | TIMESTAMP | | Ngày lập kết luận |

---

## 3. Biểu Đồ API (API Map)

Được gom nhóm thành 5 modules phục vụ Client (Web/App).

### 👥 Nhóm 1: Xác Thực & Người dùng (`/api/v1/auth`, `/api/v1/users`)
*   `POST /auth/register` - Đăng ký tài khoản (Patient).
*   `POST /auth/login` - Đăng nhập (trả về JWT Token + Role).
*   `POST /auth/forgot-password` - Quên mật khẩu.
*   `GET /users/me` - Lấy Profile cá nhân hiện tại.
*   `PUT /users/me` - Cập nhật thông tin Profile (thân nhiệt, tiền sử bệnh nền...).
*   `GET /doctors` - Lấy danh sách bác sĩ công khai để xem chuyên khoa.

### 🤖 Nhóm 2: Chatbot Triage Sàng Lọc (`/api/v1/chatbot`)
*   `POST /chatbot/chat` 
    *   *Mục đích:* Gửi tin cho AI. Backend gọi LLM RAG, phân tích triệu chứng.
    *   *Payload:* `{ sessionId?, message: "Tôi bị ho 3 ngày nay" }`
    *   *Response:* `{ reply: "...", triageLevel: "MEDIUM", summary: "..." }`
*   `GET /chatbot/sessions/{sessionId}/history` - Load lại nội dung báo cáo hoặc chat dở của quá trình AI sàng lọc.

### 🏥 Nhóm 3: Quản lý Ca Bệnh (`/api/v1/cases`)
*   **[Patient]** `POST /cases` - Tạo ca bệnh mới gửi lên hệ thống kết nối Bác sĩ. Payload đính kèm `sessionId` từ AI để kéo thông tin. (Status = PENDING).
*   **[Patient/Doctor]** `GET /cases` - Lấy danh sách Ca (Bệnh nhân xem ca mình, Bác sĩ xem ca chờ/ca đang xử lý).
*   **[Doctor]** `POST /cases/{id}/assign` - Bác sĩ nhận ấn nút "nhận ca" từ hàng đợi (Status = IN_CONSULTATION).
*   **[Doctor]** `POST /cases/{id}/conclusion` - Viết kết luận cho bệnh nhân lúc khám xong.
*   **[Doctor]** `POST /cases/{id}/complete` - Chốt hoàn thành và khóa ca (Status = COMPLETED). Bắn Notification cho bệnh nhân.

### 💬 Nhóm 4: Hội Thoại Trực Tuyến (`/api/v1/cases/{caseId}/messages`)
*   `GET /cases/{id}/messages` - Tải lịch sử chat y tế giữa Bác sĩ và Bệnh nhân.
*   `POST /cases/{id}/messages` - Gửi tin nhắn mới trong phòng chat bệnh án (Trigger WebSocket / FCM báo Noti cho người kia).
*   `POST /media/upload` - Dùng tải ảnh/file chung, trả về URL AWS S3 / Cloudinary.

### ⚙️ Nhóm 5: Quản trị Hệ Thống Admin (`/api/v1/admin`)
*   `GET /admin/dashboard` - Thông kê KPI, số lượng ca theo ngày, tốc độ phản hồi.
*   `GET /admin/users` - Danh sách toàn hệ thống, audit log người dùng.
*   `PUT /admin/users/{userId}/status` - Quản trị trạng thái kích hoạt account (Đặc biệt để approve cho tài khoản Bác sĩ tình nguyện).
*   `GET|POST /admin/rules` - (Quản lý các từ khóa / Rule để override AI - ví dụ Triage bắt buộc mức độ "NGUY HIỂM").
