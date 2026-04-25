# Release 1 - MVP Core

> **Mục tiêu:** Demo được toàn bộ 1 ca tư vấn hoàn chỉnh end-to-end.
> **Sprint:** 1 + 2 | **Thời gian:** 10/03/2026 - 01/04/2026
> **Tổng:** 17 User Stories, 52 Story Points

---

## Themes và Epics

| Theme | Epic | Mô tả |
|-------|------|-------|
| TH-01 Đăng nhập và Hồ sơ | EP-01 Đăng ký/Đăng nhập | Xác thực BN + BS |
| TH-02 Chatbot AI và Sàng lọc | EP-03 Chatbot triệu chứng | NLP chat + hỏi bổ sung |
| TH-02 Chatbot AI và Sàng lọc | EP-04 Phân loại bệnh | Triage Engine (3 mức độ) |
| TH-03 Quản lý ca bệnh | EP-05 Tạo ca và trạng thái | Vòng đời ca bệnh |
| TH-03 Quản lý ca bệnh | EP-06 Điều phối BS | Tự động gán round-robin |
| TH-03 Quản lý ca bệnh | EP-07 Danh sách ca BS | Dashboard bác sĩ |
| TH-04 Tư vấn trực tuyến | EP-08 Chat realtime | Socket.IO BN-BS |
| TH-05 Kết luận và Lịch sử | EP-10 BS lập kết luận | Form chẩn đoán |
| TH-05 Kết luận và Lịch sử | EP-11 BN xem kết luận | Giao diện bệnh nhân |
| TH-06 Thông báo | EP-13 Push notification | Firebase Cloud Messaging |

---

## Sprint 1 - Luồng tạo ca bệnh và BS nhận ca

**Thời gian:** 10/03/2026 - 21/03/2026
**Sprint Goal:** "Hoàn thiện luồng tạo ca bệnh và bác sĩ nhận ca"
**Velocity:** 32 SP | **Kết quả:** ĐẠT - 10/10 US

### Sprint Backlog

| US ID | User Story | Epic | SP | Phân công | Kết quả |
|-------|-----------|------|----|-----------|---------|
| US-01 | Là BN, tôi muốn đăng ký/đăng nhập để sử dụng tư vấn | EP-01 | 3 | FE: Giao diện đăng ký/đăng nhập. BE: API xác thực + OTP | Hoàn thành |
| US-02 | Là BS, tôi muốn đăng nhập để truy cập danh sách ca | EP-01 | 2 | FE: Giao diện đăng nhập BS. BE: API xác thực BS | Hoàn thành |
| US-03 | Là BN, tôi muốn chat với chatbot để mô tả triệu chứng | EP-03 | 4 | FE: Giao diện chat chatbot. AI: Mô hình NLP | Hoàn thành |
| US-04 | Là hệ thống, tôi muốn chatbot hỏi bổ sung để thu thập dữ liệu | EP-03 | 4 | AI: Logic hỏi bổ sung. BE: API cuộc hội thoại | Hoàn thành |
| US-05 | Là hệ thống, tôi muốn phân loại mức độ bệnh | EP-04 | 4 | AI: Triage Engine. FE: Giao diện kết quả | Hoàn thành |
| US-06 | Là BN, tôi muốn tạo ca tư vấn để kết nối BS | EP-05 | 3 | BE: API tạo ca. FE: Giao diện tạo ca | Hoàn thành |
| US-07 | Là hệ thống, tôi muốn sinh Case ID và set Chờ tư vấn | EP-05 | 2 | BE: Logic sinh ID + trạng thái | Hoàn thành |
| US-08 | Là hệ thống, tôi muốn tự động gán ca cho BS phù hợp | EP-06 | 4 | BE: Rule gán BS. AI: Logic matching | Hoàn thành |
| US-09 | Là BS, tôi muốn xem danh sách ca đang chờ | EP-07 | 3 | FE: Giao diện danh sách. BE: API danh sách ca | Hoàn thành |
| US-10 | Là BS, tôi muốn xem chi tiết ca bệnh | EP-07 | 3 | FE: Giao diện chi tiết. BE: API chi tiết ca | Hoàn thành |

### Nhật ký phát triển (Daily Scrum)

| Ngày | Sự kiện chính |
|------|--------------|
| 11/03 | Setup dự án, cấu hình React Native + Node.js + MongoDB |
| 12/03 | FE hoàn thành giao diện đăng ký. AI có 50 mẫu triệu chứng |
| 13/03 | Nghiệm thu US-01 ĐẠT. Phát hiện bug OTP timeout Android |
| 16/03 | Nghiệm thu US-02 ĐẠT. NLP đạt 78% độ chính xác |
| 17/03 | Nghiệm thu US-03, US-04 ĐẠT. Triage 3 mức độ hoàn thành |
| 18/03 | Nghiệm thu US-05, US-06, US-07 ĐẠT. API gán BS round-robin |
| 19/03 | Nghiệm thu US-08 ĐẠT. NLP tối ưu đạt 85% |
| 20/03 | Nghiệm thu US-09, US-10 ĐẠT. Kiểm thử hồi quy toàn bộ |

### Sprint 1 Review (21/03/2026)

**Kết quả:** ĐẠT - 10/10 US hoàn thành

**Demo Flow:** Mở app -> Đăng ký (OTP) -> Đăng nhập -> Chat chatbot ("đau đầu sốt nhẹ") -> Hỏi bổ sung (3 câu) -> Triage (Trung bình - vàng) -> "Liên hệ BS" -> Case ID: #CT-20260321-001 -> BS đăng nhập -> Xem danh sách ca -> Xem chi tiết

**Phản hồi Stakeholder:**
- Giảng viên: Chat chatbot rất tự nhiên. Cần thêm thanh tiến trình khi chatbot hỏi.
- Giảng viên: Badge mức độ bệnh cần rõ ràng hơn. Thêm sắp xếp theo mức độ nặng.
- PO: Kết quả Triage chưa có phần "Tóm tắt dễ hiểu" cho người dùng.

### Sprint 1 Retrospective

**Điều đã tốt:**
- Toàn bộ 10 US hoàn thành đúng hạn
- API xác thực và chatbot NLP ổn định
- Daily Scrum phát hiện trở ngại OTP sớm (Ngày 3)

**Hành động cải tiến:**
- AI-1.1: Tạo checklist cài đặt cho mỗi Sprint
- AI-1.2: Review thiết kế với PO trước khi code
- AI-1.3: Thêm unit test cho API + chatbot
- AI-1.4: Xử lý 2 trường hợp biên chatbot
- AI-1.5: Bắt buộc code review 1 người trước khi merge

---

## Sprint 2 - Chat realtime và kết luận E2E

**Thời gian:** 22/03/2026 - 01/04/2026
**Sprint Goal:** "Hoàn thiện chat realtime và kết luận để demo end-to-end"
**Velocity:** 20 SP | **Kết quả:** ĐẠT - 7/7 US + 5/6 Issues S1 đã sửa

### Sprint Backlog

| US ID | User Story | Epic | SP | Phân công | Kết quả |
|-------|-----------|------|----|-----------|---------|
| US-11 | Là BS, tôi muốn nhấn "Bắt đầu tư vấn" để chuyển trạng thái | EP-05 | 2 | BE: API cập nhật trạng thái. FE: Giao diện nút bấm | Hoàn thành |
| US-12 | Là BN, tôi muốn nhận thông báo khi BS phản hồi | EP-13 | 3 | BE: Push notification. FE: Giao diện thông báo | Hoàn thành |
| US-13 | Là BN và BS, tôi muốn chat trực tuyến theo ca bệnh | EP-08 | 5 | BE: Socket.IO server. FE: Giao diện chat RT. AI: Tóm tắt | Hoàn thành |
| US-14 | Là BS, tôi muốn lập kết luận và gửi để đóng ca | EP-10 | 3 | FE: Form kết luận. BE: API lưu kết luận | Hoàn thành |
| US-15 | Là hệ thống, tôi muốn chuyển trạng thái sang Hoàn thành | EP-05 | 2 | BE: Logic tự động. FE: Giao diện cập nhật | Hoàn thành |
| US-16 | Là BN, tôi muốn nhận thông báo khi kết luận sẵn sàng | EP-13 | 2 | BE: Push notification. FE: Deep link | Hoàn thành |
| US-17 | Là BN, tôi muốn xem kết luận tư vấn | EP-11 | 2 | FE: Giao diện xem kết luận. BE: API lấy kết luận | Hoàn thành |

*Ghi chú: Sprint 2 bao gồm sửa Usability Issues từ Sprint 1 (I-1.1 đến I-1.6)*

### Nhật ký phát triển (Daily Scrum)

| Ngày | Sự kiện chính |
|------|--------------|
| 23/03 | Sửa issues S1. FE làm nút "Bắt đầu tư vấn". Nghiên cứu Socket.IO |
| 24/03 | Nghiệm thu US-11 ĐẠT. Socket.IO server cấu hình mất thời gian |
| 25/03 | Design Review giữa Sprint. Chat RT test 2 client thành công |
| 26/03 | Nghiệm thu US-13 (chat RT) ĐẠT. Push notification hoạt động |
| 27/03 | Nghiệm thu US-12, US-14 ĐẠT. Form thiếu validation -> sửa |
| 30/03 | Nghiệm thu US-14, US-15, US-16 ĐẠT. Lỗi timestamp -> sửa |
| 31/03 | Nghiệm thu US-16, US-17 ĐẠT. MVP E2E hoàn chỉnh |

### Sprint 2 Review (01/04/2026)

**Kết quả:** ĐẠT - 7/7 US + 5/6 Issues S1

**Demo Flow E2E lần đầu:** Đăng nhập BN -> Chatbot -> Triage -> Tạo ca -> BS nhận ca -> "Bắt đầu tư vấn" -> Chat realtime (BN gửi triệu chứng, BS phản hồi) -> BS "Lập kết luận" -> "Kết thúc ca" -> BN nhận thông báo -> BN xem kết luận. THÀNH CÔNG.

**Phản hồi Stakeholder:**
- Giảng viên: Rất ấn tượng với luồng E2E. Chat realtime mượt mà.
- Giảng viên: Cần phân biệt tin nhắn AI và BS. Form kết luận cần mẫu sẵn.
- PO: Milestone MVP đạt. Sản phẩm demo E2E thành công.

### Sprint 2 Retrospective

**Điều đã tốt:**
- MVP E2E demo THÀNH CÔNG
- Socket.IO ổn định, < 200ms
- Code review 100% (AI-1.5) - giảm 80% lỗi
- 5/5 Action Items S1 hoàn thành

**Hành động cải tiến:**
- AI-2.1: Nghiên cứu upload file (S3/multer) TRƯỚC Sprint
- AI-2.2: Sửa I-1.5, I-2.1, I-2.2, I-2.3 trong 2 ngày đầu S3
- AI-2.3: Thêm màn hình "Hoàn thành tư vấn" với CTA
- AI-2.4: Chạy Usability Test sau khi sửa hết issues

---

## Tiêu chí hoàn thành - Release 1

- [x] BN đăng ký/đăng nhập thành công (OTP + JWT)
- [x] Chat chatbot phân tích triệu chứng (NLP 85%)
- [x] Triage Engine phân loại 3 mức độ (đỏ/vàng/xanh)
- [x] Tạo ca bệnh + sinh Case ID tự động
- [x] Tự động gán BS (round-robin + online)
- [x] Chat realtime BN-BS (Socket.IO < 200ms)
- [x] BS lập kết luận (chẩn đoán + đơn thuốc + khuyến nghị)
- [x] BN xem kết luận
- [x] Push notification (BS phản hồi + kết luận sẵn sàng)
- [x] Toàn bộ luồng E2E chạy thành công
