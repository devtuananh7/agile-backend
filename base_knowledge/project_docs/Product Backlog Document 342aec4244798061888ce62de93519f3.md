# Product Backlog Document

---

**Phiên bản:** v1.0

**Ngày tạo:** 14/04/2026

**Project: CareTalk - Chatbot phòng khám gia đình**

**Người tạo:** Phạm Thanh Nhã

**Trạng thái:** Draft

---

## 1. Product Themes (Nhóm chủ đề sản phẩm)

| Theme ID | Chủ đề (Theme) | Mô tả |
| --- | --- | --- |
| TH-01 | Đăng nhập & Hồ sơ | Đăng ký/đăng nhập và quản lý hồ sơ người dùng (bệnh nhân/bác sĩ) |
| TH-02 | Chatbot AI & Sàng lọc (Triage) | Chatbot tiếp nhận triệu chứng, hỏi bổ sung và phân loại mức độ |
| TH-03 | Quản lý ca bệnh (Case Management) | Tạo ca bệnh, điều phối bác sĩ, quản lý trạng thái ca |
| TH-04 | Tư vấn trực tuyến (Realtime Chat) | Chat trực tiếp giữa bệnh nhân và bác sĩ theo từng ca bệnh |
| TH-05 | Kết luận & Lịch sử | Bác sĩ lập kết luận, bệnh nhân xem kết luận và tra cứu lịch sử |
| TH-06 | Thông báo (Notification) | Push notification theo các sự kiện nghiệp vụ |
| TH-07 | Quản trị hệ thống (Admin) | Web quản trị: quản lý user, duyệt bác sĩ, quản lý rule |
| TH-08 | Báo cáo & Giám sát | Dashboard thống kê, đo hiệu suất phản hồi, audit log |

---

## 2. Danh sách Epic (Epic List)

| Epic ID | Tên Epic | Thuộc Theme | Ưu tiên |
| --- | --- | --- | --- |
| EP-01 | Đăng ký/Đăng nhập người dùng | TH-01 | P0 |
| EP-02 | Hồ sơ bệnh nhân & thông tin y tế cơ bản | TH-01 | P1 |
| EP-03 | Chatbot tiếp nhận triệu chứng & hỏi bổ sung | TH-02 | P0 |
| EP-04 | Phân loại mức độ bệnh (Triage Engine) | TH-02 | P0 |
| EP-05 | Tạo ca bệnh & quản lý trạng thái ca | TH-03 | P0 |
| EP-06 | Điều phối/gán bác sĩ (Assign Doctor) | TH-03 | P0 |
| EP-07 | Danh sách ca bệnh cho bác sĩ (Case Queue) | TH-03 | P0 |
| EP-08 | Chat trực tuyến giữa bác sĩ và bệnh nhân | TH-04 | P0 |
| EP-09 | Đính kèm ảnh/file trong tư vấn | TH-04 | P1 |
| EP-10 | Bác sĩ lập kết luận và gửi kết luận | TH-05 | P0 |
| EP-11 | Bệnh nhân xem kết luận | TH-05 | P0 |
| EP-12 | Lịch sử ca bệnh & tìm kiếm | TH-05 | P1 |
| EP-13 | Luồng gửi thông báo theo sự kiện | TH-06 | P0 |
| EP-14 | Admin quản lý user & ca bệnh | TH-07 | P1 |
| EP-15 | Admin duyệt tài khoản bác sĩ | TH-07 | P1 |
| EP-16 | Admin cấu hình rule cảnh báo nguy hiểm | TH-07 | P1 |
| EP-17 | Admin dashboard thống kê | TH-08 | P1 |
| EP-18 | Audit Log (log thao tác hệ thống) | TH-08 | P2 |

**Giải thích mức ưu tiên:**

- **P0:** Bắt buộc có để demo được MVP end-to-end
- **P1:** Nên có để hoàn thiện trải nghiệm và tăng chất lượng sản phẩm
- **P2:** Có thể làm sau nếu còn thời gian

---

## 3. Product Backlog (Danh sách User Stories theo mức ưu tiên)

### 3.1 Release 1 – MVP Core (Tuần 1–2)

Mục tiêu Release 1: **Demo được toàn bộ 1 ca tư vấn hoàn chỉnh**

(đăng nhập → chatbot → tạo ca → bác sĩ nhận ca → chat → kết luận → thông báo)

| US ID | User Story | Epic | Priority | Giá trị (Value) | Độ khó (Effort) | Release |
| --- | --- | --- | --- | --- | --- | --- |
| US-01 | Là bệnh nhân, tôi muốn đăng ký/đăng nhập để sử dụng tính năng tư vấn | EP-01 | P0 | 5 | 3 | R1 |
| US-02 | Là bác sĩ, tôi muốn đăng nhập để truy cập danh sách ca bệnh được gán | EP-01 | P0 | 5 | 2 | R1 |
| US-03 | Là bệnh nhân, tôi muốn chat với chatbot để mô tả triệu chứng | EP-03 | P0 | 5 | 4 | R1 |
| US-04 | Là hệ thống, tôi muốn chatbot đặt câu hỏi bổ sung để thu thập đủ dữ liệu | EP-03 | P0 | 5 | 4 | R1 |
| US-05 | Là hệ thống, tôi muốn phân loại mức độ bệnh để đưa ra hướng xử lý phù hợp | EP-04 | P0 | 5 | 4 | R1 |
| US-06 | Là bệnh nhân, tôi muốn tạo ca tư vấn để được kết nối bác sĩ khi cần | EP-05 | P0 | 5 | 3 | R1 |
| US-07 | Là hệ thống, tôi muốn sinh Case ID và set trạng thái Pending (Chờ tư vấn) | EP-05 | P0 | 5 | 2 | R1 |
| US-08 | Là hệ thống, tôi muốn tự động gán ca cho bác sĩ phù hợp đang online | EP-06 | P0 | 5 | 4 | R1 |
| US-09 | Là bác sĩ, tôi muốn xem danh sách ca bệnh đang chờ để xử lý kịp thời | EP-07 | P0 | 5 | 3 | R1 |
| US-10 | Là bác sĩ, tôi muốn xem chi tiết ca bệnh để hiểu triệu chứng trước khi tư vấn | EP-07 | P0 | 5 | 3 | R1 |
| US-11 | Là bác sĩ, tôi muốn nhấn “Bắt đầu tư vấn” để chuyển ca sang trạng thái Đang tư vấn | EP-05 | P0 | 5 | 2 | R1 |
| US-12 | Là bệnh nhân, tôi muốn nhận thông báo khi bác sĩ phản hồi để vào chat ngay | EP-13 | P0 | 5 | 3 | R1 |
| US-13 | Là bệnh nhân và bác sĩ, tôi muốn chat trực tuyến theo từng ca bệnh | EP-08 | P0 | 5 | 5 | R1 |
| US-14 | Là bác sĩ, tôi muốn lập kết luận và gửi kết luận để đóng ca tư vấn | EP-10 | P0 | 5 | 3 | R1 |
| US-15 | Là hệ thống, tôi muốn chuyển trạng thái ca sang Completed khi bác sĩ gửi kết luận | EP-05 | P0 | 5 | 2 | R1 |
| US-16 | Là bệnh nhân, tôi muốn nhận thông báo khi kết luận sẵn sàng để xem ngay | EP-13 | P0 | 5 | 2 | R1 |
| US-17 | Là bệnh nhân, tôi muốn xem kết luận tư vấn để biết hướng xử lý tiếp theo | EP-11 | P0 | 5 | 2 | R1 |

---

### 3.2 Release 2 – Hoàn thiện trải nghiệm & Lịch sử (Tuần 3)

Mục tiêu Release 2: **Lưu lịch sử, nâng trải nghiệm, bổ sung ảnh/file và hồ sơ**

| US ID | User Story | Epic | Priority | Value | Effort | Release |
| --- | --- | --- | --- | --- | --- | --- |
| US-18 | Là bệnh nhân, tôi muốn xem danh sách ca bệnh của mình để theo dõi tiến trình | EP-05 | P0 | 5 | 3 | R2 |
| US-19 | Là bệnh nhân, tôi muốn tìm kiếm/lọc lịch sử ca bệnh để xem lại kết luận cũ | EP-12 | P1 | 4 | 3 | R2 |
| US-20 | Là bác sĩ, tôi muốn lọc danh sách ca theo trạng thái để xử lý nhanh hơn | EP-07 | P1 | 4 | 2 | R2 |
| US-21 | Là bệnh nhân, tôi muốn gửi ảnh/file trong quá trình tư vấn để bác sĩ hiểu rõ hơn | EP-09 | P1 | 4 | 4 | R2 |
| US-22 | Là bác sĩ, tôi muốn xem ảnh/file đính kèm của bệnh nhân để tư vấn chính xác hơn | EP-09 | P1 | 4 | 3 | R2 |
| US-23 | Là hệ thống, tôi muốn khóa khung chat sau khi ca Completed để đảm bảo kết thúc tư vấn | EP-05 | P1 | 4 | 2 | R2 |
| US-24 | Là bệnh nhân, tôi muốn cập nhật hồ sơ cá nhân để bác sĩ có thông tin chính xác | EP-02 | P1 | 4 | 3 | R2 |
| US-25 | Là bác sĩ, tôi muốn xem thông tin y tế nền (dị ứng/bệnh nền) của bệnh nhân | EP-02 | P1 | 4 | 3 | R2 |

---

### 3.3 Release 3 – Admin & Tối ưu vận hành (Tuần 4)

Mục tiêu Release 3: **Có hệ thống vận hành cơ bản cho admin**

| US ID | User Story | Epic | Priority | Value | Effort | Release |
| --- | --- | --- | --- | --- | --- | --- |
| US-26 | Là admin, tôi muốn đăng nhập web portal để quản trị hệ thống | EP-14 | P0 | 5 | 2 | R3 |
| US-27 | Là admin, tôi muốn xem danh sách toàn bộ ca bệnh để theo dõi vận hành | EP-14 | P1 | 4 | 3 | R3 |
| US-28 | Là admin, tôi muốn duyệt tài khoản bác sĩ để đảm bảo chỉ bác sĩ hợp lệ mới tư vấn | EP-15 | P1 | 5 | 3 | R3 |
| US-29 | Là admin, tôi muốn khóa/mở tài khoản user để ngăn spam và hành vi sai phạm | EP-14 | P1 | 4 | 2 | R3 |
| US-30 | Là admin, tôi muốn cấu hình rule triệu chứng nguy hiểm để chatbot cảnh báo cấp cứu | EP-16 | P1 | 5 | 4 | R3 |
| US-31 | Là admin, tôi muốn xem dashboard thống kê để đo hiệu quả hệ thống | EP-17 | P1 | 4 | 4 | R3 |
| US-32 | Là hệ thống, tôi muốn lưu lịch sử notification để phục vụ kiểm tra và truy vết | EP-18 | P2 | 3 | 3 | R3 |

---

## 4. Product Roadmap (3 Release trong 1 tháng)

| Release | Thời gian | Mục tiêu | Scope chính |
| --- | --- | --- | --- |
| Release 1 – MVP Core | Tuần 1–2 | Demo end-to-end 1 ca tư vấn | Auth, Chatbot, Triage, Create Case, Assign Doctor, Doctor Queue, Chat, Conclusion, Notification |
| Release 2 – Stabilize & History | Tuần 3 | Hoàn thiện trải nghiệm và lưu lịch sử | Case history, attachment, profile update, search/filter, lock chat |
| Release 3 – Admin & Optimization | Tuần 4 | Vận hành hệ thống cơ bản | Admin web portal, doctor approval, dashboard, rule config, audit |

---

## 5. Sprint Backlog (Mô phỏng Scrum Plan)

### Sprint 1 (Tuần 1) – Sprint Goal

**Hoàn thiện luồng tạo ca bệnh và bác sĩ nhận ca.**

| Task | Liên quan User Story |
| --- | --- |
| Thiết kế UI đăng nhập bệnh nhân/bác sĩ | US-01, US-02 |
| Thiết kế màn chat chatbot cơ bản | US-03 |
| Xây dựng API tạo case + sinh Case ID | US-06, US-07 |
| Xây dựng logic assign bác sĩ (rule đơn giản) | US-08 |
| Thiết kế màn danh sách ca bệnh cho bác sĩ | US-09 |
| Thiết kế màn chi tiết ca bệnh | US-10 |

---

### Sprint 2 (Tuần 2) – Sprint Goal

**Hoàn thiện chat realtime và kết luận để demo end-to-end.**

| Task | Liên quan User Story |
| --- | --- |
| Xây dựng module chat realtime theo case | US-13 |
| Thiết kế UI chat bác sĩ – bệnh nhân | US-13 |
| Push notification khi bác sĩ phản hồi | US-12 |
| Thiết kế form kết luận cho bác sĩ | US-14 |
| Xây dựng API lưu kết luận + cập nhật status Completed | US-15 |
| Push notification khi kết luận sẵn sàng | US-16 |
| Thiết kế màn hình bệnh nhân xem kết luận | US-17 |

---

### Sprint 3 (Tuần 3) – Sprint Goal

**Hoàn thiện lịch sử ca bệnh và gửi ảnh/file.**

| Task | Liên quan User Story |
| --- | --- |
| Thiết kế màn danh sách ca bệnh bệnh nhân | US-18 |
| Thiết kế chức năng search/filter lịch sử | US-19 |
| Xây dựng upload ảnh/file trong chat | US-21 |
| Xây dựng màn bác sĩ xem attachment | US-22 |
| Thiết lập logic khóa chat sau Completed | US-23 |
| Thiết kế màn hồ sơ bệnh nhân | US-24 |
| Hiển thị bệnh nền/dị ứng cho bác sĩ | US-25 |

---

### Sprint 4 (Tuần 4) – Sprint Goal

**Hoàn thiện web admin để vận hành hệ thống.**

| Task | Liên quan User Story |
| --- | --- |
| Xây dựng màn admin login | US-26 |
| Xây dựng màn danh sách case admin | US-27 |
| Xây dựng luồng duyệt bác sĩ | US-28 |
| Xây dựng chức năng khóa/mở user | US-29 |
| Xây dựng cấu hình rule cảnh báo nguy hiểm | US-30 |
| Xây dựng dashboard thống kê cơ bản | US-31 |
| Lưu log notification/audit log | US-32 |

---

## 6. Ghi chú quan trọng (Notes cho báo cáo môn học)

- Các user stories được ưu tiên theo nguyên tắc:
    
    **Demo được end-to-end trước → rồi mới nâng UX và tối ưu vận hành.**
    
- Bảng backlog này có thể dùng trực tiếp để làm:
    - **Value – Effort Analysis**
    - **Prioritized Backlog**
    - **Sprint Planning**
    - **Product Roadmap 3 releases**
- Các story có độ khó cao (Effort 4–5) nên được đánh dấu là **Technical Risk**:
    - Realtime Chat
    - Assign Doctor theo rule
    - Triage engine AI

---

Nếu bạn muốn, mình sẽ viết tiếp cho bạn phần **Acceptance Criteria Given–When–Then** cho toàn bộ các user story P0 của Release 1 để bạn copy sang PRD Detail luôn.