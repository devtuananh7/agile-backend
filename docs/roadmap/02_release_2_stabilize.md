# Release 2 - Hoàn Thiện Trải Nghiệm

> **Mục tiêu:** Lưu lịch sử, nâng trải nghiệm, bổ sung ảnh/file và hồ sơ.
> **Sprint:** 3 | **Thời gian:** 02/04/2026 - 10/04/2026
> **Tổng:** 8 User Stories, 23 Story Points

---

## Sprint 3 - Lịch sử ca bệnh và gửi ảnh/file

**Sprint Goal:** "Hoàn thiện lịch sử ca bệnh và gửi ảnh/file"
**Velocity:** 23 SP | **Kết quả:** ĐẠT - 8/8 US + 6/6 Issues S2 đã sửa

### Sprint Backlog

| US ID | User Story | Epic | SP | Phân công | Kết quả |
|-------|-----------|------|----|-----------|---------|
| US-18 | Là BN, tôi muốn xem danh sách ca bệnh để theo dõi | EP-05 | 3 | FE: Giao diện danh sách ca BN. BE: API my cases | Hoàn thành |
| US-19 | Là BN, tôi muốn tìm kiếm/lọc lịch sử ca bệnh | EP-12 | 3 | FE: Giao diện search/filter. BE: API tìm kiếm | Hoàn thành |
| US-20 | Là BS, tôi muốn lọc danh sách ca theo trạng thái | EP-07 | 2 | FE: Giao diện bộ lọc BS. BE: API bộ lọc | Hoàn thành |
| US-21 | Là BN, tôi muốn gửi ảnh/file trong tư vấn | EP-09 | 4 | FE: Giao diện chọn ảnh/file. BE: API lưu trữ file (S3) | Hoàn thành |
| US-22 | Là BS, tôi muốn xem ảnh/file đính kèm của BN | EP-09 | 3 | FE: Giao diện xem file. BE: API cung cấp file | Hoàn thành |
| US-23 | Là hệ thống, tôi muốn khóa chat sau khi ca Hoàn thành | EP-05 | 2 | BE: Logic khóa chat. FE: Giao diện vô hiệu hóa | Hoàn thành |
| US-24 | Là BN, tôi muốn cập nhật hồ sơ để BS có thông tin chính xác | EP-02 | 3 | FE: Giao diện hồ sơ. BE: API cập nhật hồ sơ | Hoàn thành |
| US-25 | Là BS, tôi muốn xem thông tin y tế nền (dị ứng/bệnh nền) | EP-02 | 3 | FE: Giao diện thông tin y tế. BE: API hồ sơ sức khỏe | Hoàn thành |

*Ghi chú: Sprint 3 bao gồm sửa Usability Issues từ Sprint 2 (I-2.1 đến I-2.5)*

### Nhật ký phát triển (Daily Scrum)

| Ngày | Sự kiện chính |
|------|--------------|
| 03/04 | Sửa issues S2 (nhãn AI, thông báo, mẫu kết luận, màn hình hoàn thành) |
| 06/04 | Tất cả fix S2 ĐẠT. Giao diện lịch sử + tìm kiếm xong. Cấu hình S3 OK |
| 07/04 | Nghiệm thu US-18, US-19 ĐẠT. Upload ảnh/file hoạt động E2E |
| 08/04 | Nghiệm thu US-20, US-21, US-22 ĐẠT. Hồ sơ + khóa chat đang làm |
| 09/04 | Nghiệm thu US-23, US-24, US-25 ĐẠT. Tổng duyệt 25/25 US toàn dự án |

### Sprint 3 Review (09/04/2026)

**Kết quả:** ĐẠT - 8/8 US + 6/6 Issues S2

**Demo Flow TOÀN BỘ sản phẩm:** Đăng ký -> Chatbot -> Tạo ca -> Chat realtime (gửi ảnh bệnh) -> BS xem đính kèm + bệnh nền BN -> BS kết luận -> BN xem kết luận -> BN xem lịch sử ca bệnh cũ -> Tìm kiếm/lọc -> Chat cũ đã khóa. TOÀN BỘ LUỒNG THÀNH CÔNG.

**Phản hồi Stakeholder:**
- Giảng viên: Sản phẩm hoàn chỉnh, cải thiện rõ rệt qua 3 Sprint.
- Giảng viên: Upload ảnh là tính năng hữu ích. Nên thêm preview trước khi gửi.
- Giảng viên: Lịch sử ca bệnh trực quan. Nên thêm tên BS trong danh sách.
- Giảng viên: Tổng thể: ĐẠT yêu cầu môn học. Demo E2E rất tốt.
- PO: 25/25 US hoàn thành. Sản phẩm sẵn sàng cho Release thực tế.

### Sprint 3 Retrospective

**Điều đã tốt:**
- 25/25 US hoàn thành trong 3 Sprint. Toàn bộ Sprint Goals đạt.
- 75/75 SP đốt cháy. Velocity ổn định (32 -> 20 -> 23).
- 16 Usability Issues, 11 đã giải quyết (68.75%). 0 lỗi Critical.
- Điểm SUS tăng liên tục: 70.0 -> 77.0 -> 78.5
- Code review 100%, tỷ lệ lỗi giảm 80% so với Sprint 1.

**Điều cần cải thiện:**
- Cần Usability Test sớm hơn (giữa Sprint thay vì cuối)
- Sprint 3 ngắn hơn (9 ngày) gây áp lực deadline
- Chưa có Integration Test tự động
- Chưa có CI/CD pipeline

### Đánh giá Action Items Sprint 2

| ID | Hành động | Kết quả |
|----|----------|---------|
| AI-2.1 | Nghiên cứu upload file trước Sprint | Hoàn thành - Cài đặt multer + S3 trước 02/04 |
| AI-2.2 | Sửa issues trong 2 ngày đầu | Hoàn thành - 6/6 issues sửa xong |
| AI-2.3 | Màn hình "Hoàn thành tư vấn" | Hoàn thành - 3 CTA: Xem KL / Tạo ca mới / Trang chủ |
| AI-2.4 | Chạy Usability Test | Hoàn thành - 5 người test, SUS = 78.5 |

---

## Tổng kết 3 Sprint

| Chỉ số | Giá trị | Nhận xét |
|--------|---------|----------|
| Thời lượng Sprint | 12 - 11 - 9 ngày | Ổn định S1-S2. S3 hơi ngắn |
| Velocity | 32 -> 20 -> 23 SP | Giảm do phạm vi nhỏ hơn, không phải giảm năng suất |
| Tỷ lệ lỗi | 5 (S1) -> 1 (S2) -> 0 (S3) | Giảm liên tục nhờ code review và unit test |
| Sự kiện Scrum | 4 sự kiện / Sprint | Tuân thủ đúng framework |
| Giải quyết trở ngại | TB 0.5 ngày | SM xử lý nhanh |
| Hài lòng nhóm | 4.2/5 | Khảo sát ẩn danh cuối Sprint 3 |

### Bài học rút ra

- Scrum giúp nhóm tự tổ chức và thích ứng nhanh. Daily Scrum là công cụ quan trọng nhất.
- Definition of Done phải rõ ràng từ đầu.
- Code review và unit test sớm giúp giảm tỷ lệ lỗi.
- Working Agreement là nền tảng - nhóm ít xung đột vì đã thống nhất quy tắc.
- Sprint ngắn (< 10 ngày) gây áp lực. Nên giữ ít nhất 2 tuần/Sprint.

---

## Tiêu chí hoàn thành - Release 2

- [x] BN xem danh sách ca bệnh (trạng thái, ngày, triệu chứng)
- [x] Tìm kiếm + lọc lịch sử ca (từ khóa, trạng thái)
- [x] BS lọc danh sách ca (trạng thái + mức độ + thời gian)
- [x] Upload ảnh/file trong chat (BN gửi, BS xem + tải)
- [x] Khóa chat sau khi ca Hoàn thành
- [x] BN cập nhật hồ sơ (bệnh nền, dị ứng, thông tin cá nhân)
- [x] BS xem thông tin y tế nền của BN
- [x] Toàn bộ 16 Usability Issues đã xử lý 11/16
