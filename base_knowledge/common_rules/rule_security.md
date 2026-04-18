# Security Checklist Rules

> Source: [Checklist yêu cầu bảo mật](https://wiki.servicehub.vn/spaces/DVNHKS/pages/535560357/1.2.+Checklist+y%C3%AAu+c%E1%BA%A7u+b%E1%BA%A3o+m%E1%BA%ADt)
> Phạm vi: Các yêu cầu bắt buộc khi xử lý logic nghiệp vụ — áp dụng cho mọi tính năng.

---

## 1. Auth (Xác thực)

- **[High]** Thay đổi clientId nhưng không thay đổi key mã hóa → Validate theo hd_valid_less1
- **[High]** Sử dụng bộ key mã hóa của user khác → Validate theo hd_valid_less1
- **[High]** Đổi mật khẩu không nhập mật khẩu cũ hoặc sai nhiều lần mật khẩu cũ → Validate session đúng User, kiểm tra MK cũ/mới != null, sai quá N lần → xóa session hoặc khóa
- **[Medium]** Xóa phiên khi user chủ động logout → Xóa session trong cache (recommend không dùng DB cho session check hiệu năng)
- **[High]** Không lưu trữ clear OTP trong DB → Sử dụng SHA256 băm khi lưu, so sánh giá trị băm khi verify
- **[Medium]** Gửi lại OTP → Client bắt buộc truyền lại đúng giá trị token OTP trước đó

## 2. Tài khoản (Account)

- **[High]** Truy vấn danh sách tài khoản của user khác → Validate theo hd_valid_less2 (2.1)
- **[High]** Lấy chi tiết tài khoản của user khác → Validate theo hd_valid_less2 (2.2)
- **[High]** Truy vấn lịch sử giao dịch của user khác → Validate theo hd_valid_less2 (2.3)

## 3. Báo cáo giao dịch (Transaction Report)

- **[High]** Truy vấn báo cáo giao dịch của người khác → Validate theo hd_valid_less2 (2.3)
- **[Medium]** Validate fromDate, toDate trong vòng N ngày, fromDate không nhỏ hơn giới hạn cho phép → hd_valid_less2 (2.3)

## 4. Validate chung (General Validation)

- **[High]** Validate số tài khoản/thẻ (cardtoken) nguồn, đích với TK tiết kiệm, TK vay của chính User
- **[High]** Validate số tiền > 0, tài khoản đủ số dư
- **[High]** Validate phí, lãi >= 0
- **[High]** Validate transactionId chống duplicate giao dịch — sau confirm cần remove hoặc update trạng thái để không confirm lại

## 5. Danh bạ (Contact)

- **[High]** Truy vấn danh bạ của user khác → hd_valid_less2 (2.4)
- **[High]** Thêm danh bạ cho user khác → hd_valid_less2 (2.5)
- **[High]** Chỉnh sửa danh bạ của user khác → hd_valid_less2 (2.6)
- **[High]** Xóa danh bạ của user khác → hd_valid_less2 (2.6)

## 6. Giao dịch tài chính (Financial Transaction)

- **[High]** Chặn trùng giao dịch → Sử dụng INCR Redis: khi 1 transId/transToken đang xử lý (đặc biệt confirm), chặn request đúp
- **[High]** Chặn khởi tạo 2 giao dịch cùng lúc → Nếu có transId1 đang xử lý + transId2 mới gửi → hủy transId1 (hoặc hủy cả 2 để tránh nhận nhầm giá trị)
- **[Medium]** Cơ chế sinh trace → Sử dụng Seq DB
- **[High]** Khởi tạo giao dịch với tài khoản nguồn thuộc user khác → Luôn validate hd_valid_less2 (2.2)
- **[High]** Khởi tạo giao dịch với số tiền âm → Validate số tiền luôn > 0
- **[High]** Khởi tạo giao dịch với số tiền tràn số → Validate số tiền < MAX AMOUNT cho phép
- **[Medium]** Khởi tạo và xác nhận với 2 số tiền khác nhau → Init: validate > 0 && < MAX; Confirm: validate > 0 && < MAX && AmountInit == AmountConfirm
- **[High]** Trừ tiền với số tiền lớn hơn số dư → Validate số dư từ danh sách tài khoản
- **[Medium]** Sử dụng tranToken thay vì tranId → tranId là sequence tự tăng (có thể đoán) → dùng giá trị random số/chữ
- **[High]** Xác nhận giao dịch với tranToken khác → Validate transToken đúng của User thực hiện
- **[Medium]** TTHĐ số tiền nhỏ hơn kỳ cước nhưng vẫn gạch nợ → Validate lại số tiền gạch nợ
- **[High]** Truy vấn báo cáo giao dịch của user khác → hd_valid_less2 (2.3)
- **[High]** Validate danh bạ thụ hưởng (beneId) → Validate đúng beneId của chính user + validate giá trị quan trọng: toAccount, Name, invoiceNo khớp server trả về

## 7. Tiết kiệm (Saving)

- **[High]** Mở TK tiết kiệm, TK tất toán thuộc user khác → hd_valid_less2 (2.2)
- **[High]** Tất toán tiết kiệm với TK nhận user khác → hd_valid_less2 (2.2)
- **[High]** Thay đổi % lãi suất tiết kiệm khi mở → Validate lại % lãi suất, validate số tiền > 0

## 8. Vay & Thấu chi (Loan & Overdraft)

- **[High]** Truy vấn TK vay người khác → hd_valid_less2 (2.2)
- **[High]** Thay đổi lãi suất vay → Validate số tiền > 0, phí >= 0, validate lại % lãi suất (lấy lại giá trị từ server)
- **[High]** Thay đổi phí giao dịch → Validate lại phí dựa trên số tiền (lấy lại giá trị từ server)

## 9. Thẻ (Card)

- **[High]** Truy vấn danh sách/chi tiết thẻ của user khác → hd_valid_less2 (2.1 / 2.2)
- **[High]** Kích hoạt thẻ user khác → hd_valid_less2 (2.2)
- **[High]** Khóa/mở khóa tính năng thẻ user khác → hd_valid_less2 (2.2)

## 10. Nạp tiền / Mua mã thẻ / Nạp đại lý

- **[Medium]** Số tiền không nằm trong danh sách mệnh giá → Validate số tiền > 0, phí >= 0, validate mệnh giá nằm trong danh sách, validate lại phí giao dịch

## 11. Thanh toán QR (QR Payment)

- **[High]** Đảo tiền khi lỗi pha 2 → Nếu QR Gateway trả error code 08 hoặc HTTP Status >= 500 → hold tiền, KHÔNG đảo giao dịch, trạng thái = Timeout (chờ đối soát hoàn tiền)

## 12. Soft OTP

- **[High]** Thay đổi CIF vẫn kích hoạt được Soft OTP → CIF phải lấy từ Server, KHÔNG lấy từ Request Client

## 13. Key Exchange & Validation

- **[High]** Cơ chế trao đổi key → Client sinh (private1, public1), gửi public1 dùng mã hóa/giải mã request; Server sinh (private2, public2), trả public2 dùng mã hóa/giải mã response
- **[High]** Validate keyId → keyId = 1 chỉ dùng cho key default khi activate; KH đã active phải kiểm tra keyId != 1 → hd_valid_less1

## 14. TouchId / TouchPin

- **[Medium]** Lưu trữ TouchId, TouchPin → Sử dụng SHA256 với thông tin động gắn với KH (key cứng + giá trị ngẫu nhiên theo KH); sinh touchId/touchPin bằng randomUtils.random(size)

## 15. eKYC

- **[High]** Cùng 1 giấy tờ, cùng 1 người trên nhiều thiết bị đồng thời → Cache Redis key = số giấy tờ, value = IMEI thiết bị; bước xác nhận get cache compare IMEI khớp mới cho đi. Hoặc dùng INCR (limit=1) với key = số giấy tờ, decrement khi trả response

## 16. Password Expiry

- **[High]** Mật khẩu hết hạn nhưng vẫn đăng nhập được → Login: Validate password expire or not

## 17. Session Channel Separation

- **[Medium]** Phiên sinh trên kênh nào chỉ sử dụng được trên kênh đó → hd_valid_less1 (session)
