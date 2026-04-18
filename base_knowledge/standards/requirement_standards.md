# Requirement Standards

_File này định nghĩa danh sách các standard PHẢI được reasoning và derive từ codebase khi chạy skill `initial-project`._

## Base Requirements (Bắt buộc)

Các standard sau PHẢI được reasoning và sinh file output:

| # | File Name                    | Mô tả                                                                          |
|---|------------------------------|---------------------------------------------------------------------------------|
| 1 | `coding_standard.md`        | Lưu trữ coding convention chung của dự án (naming, format, package structure)   |
| 2 | `logging_standard.md`       | Lưu trữ cách ghi log của dự án (format, level, masking, best practices)         |
| 3 | `error_handling_standard.md` | Lưu trữ cách bắt lỗi của dự án (exception types, error codes, handler patterns) |

## Extend Requirements (Tùy chọn)

Các standard sau là TÙY CHỌN — từng dự án có thể thêm hoặc bớt tùy theo nhu cầu.
Format giống Base Requirements: tên file + mô tả tác dụng.

| # | File Name                    | Mô tả                                                                          |
|---|------------------------------|---------------------------------------------------------------------------------|
| 1 | `dto_standard.md`           | Lưu trữ quy tắc đặt tên và cấu trúc DTO (request/response, inheritance)       |
| 2 | `database_standard.md`      | Lưu trữ cách tương tác database (repository, entity, transaction patterns)     |
| 3 | `error_code_standard.md`    | Lưu trữ quy tắc đặt tên error code và mapping HTTP status, các mã lỗi đặc biệt như hết phiên, sai OTP...                     |
| 4 | `security_standard.md`      | Lưu trữ các quy tắc bảo mật (authentication, authorization, data masking)     |
| 5 | `api_standard.md`           | Lưu trữ quy tắc thiết kế API (endpoint naming, versioning, response format)   |
| 6 | `bank_integration_standards.md` | Các tiêu chuẩn tích hợp API CoreBanking, bao gồm tất cả các kênh, phương thức kết nối sang bank   |

> **Hướng dẫn mở rộng**: Để thêm standard mới, thêm một dòng vào bảng Extend Requirements
> với tên file (dạng `<name>_standard.md`) và mô tả tác dụng. Flow reasoning của skill
> `initial-project` sẽ tự động nhận diện và sinh file tương ứng.

- **`coding_standard.md`** (Base):
    - Scan class/method naming conventions, package structure, code formatting.
    - Class naming conventions, class organization methods within modules and submodules.
    - Check annotation usage patterns, import organization.
    - Check code comment patterns.
    - Write findings to `base_knowledge/standards/coding_standard.md`.

- **`logging_standard.md`** (Base):
    - Scan common log format usage (`log.info`, `log.error`, `log.debug`).
    - Check log message patterns (prefix usage, JSON serialization, etc.).
    - Check what data is logged vs. masked.
    - Write findings to `base_knowledge/standards/logging_standard.md`.

- **`error_handling_standard.md`** (Base):
    - Scan how exceptions are thrown (`UserException`, custom exceptions).
    - Check error code definitions (`Constants.ResCode`).
    - Check how error messages are retrieved (`CommonService.getMessage()`).
    - Check global exception handler patterns (`@ControllerAdvice`).
    - Write findings to `base_knowledge/standards/error_handling_standard.md`.

- **Extend Requirements** — for each item listed in the Extend table above, scan the codebase for patterns. If clear patterns are found → create the standard file. If insufficient patterns → skip.

- **`dto_standard.md`** (Extend):
    - Scan request/response DTO classes: naming conventions (`*Request`, `*Response`, `*DTO`), package location.
    - Check DTO inheritance patterns (common base class, generic wrappers).
    - Check validation annotations (`@NotNull`, `@Valid`, custom validators).
    - Check DTO mapping patterns (manual mapping, MapStruct, ModelMapper).
    - Write findings to `base_knowledge/standards/dto_standard.md`.

- **`database_standard.md`** (Extend):
    - Scan Repository interfaces: naming conventions, custom query methods, `@Query` usage.
    - Check Entity classes: annotation patterns (`@Entity`, `@Table`, `@Column`), naming conventions.
    - Check transaction management: `@Transactional` usage, isolation levels, propagation.
    - Check database access patterns: JPA, native queries, stored procedures.
    - Write findings to `base_knowledge/standards/database_standard.md`.

- **`error_code_standard.md`** (Extend):
    - Scan error code constants: naming patterns, numeric ranges, grouping strategy.
    - Check HTTP status mapping: which error codes map to which HTTP statuses.
    - Check special error codes: session expired, invalid OTP, rate limiting, etc.
    - Check error message resolution: message bundles, i18n patterns.
    - Write findings to `base_knowledge/standards/error_code_standard.md`.

- **`security_standard.md`** (Extend):
    - Scan authentication mechanisms: JWT, session-based, OAuth2.
    - Check authorization patterns: role-based, permission-based, `@PreAuthorize`.
    - Check data masking patterns: which fields are masked in logs/responses (card numbers, phone, etc.).
    - Check security filters and interceptors in the request chain.
    - Write findings to `base_knowledge/standards/security_standard.md`.

- **`api_standard.md`** (Extend):
    - Scan API endpoint patterns: URL naming (`/v1/`, `/api/`), HTTP methods usage.
    - Check request/response envelope: common wrapper structure, status codes, message format.
    - Check API versioning strategy.
    - Check content negotiation, media types, serialization settings.
    - Write findings to `base_knowledge/standards/api_standard.md`.

- **`bank_integration_standards.md`** (Extend):
    - Scan classes that call external bank/CoreBanking APIs.
    - Check connection methods: REST, SOAP, socket, message queue.
    - Check request signing, encryption, and authentication patterns.
    - Check timeout, retry, and fallback handling for bank calls.
    - Check transaction reconciliation and logging patterns.
    - Write findings to `base_knowledge/standards/bank_integration_standards.md`.

- **`financial_flow_standard.md`** (Extend):
    - Tìm các Controller xử lý giao dịch tài chính (chuyển tiền, thanh toán, nạp tiền, rút tiền...).
    - Trace luồng hoàn chỉnh: Controller → validate request, security → Khởi tạo giao dịch -> Business logic -> Xác thực giao dịch -> Hạch toán giao dịch -> xử lý nghiệp vụ sau giao dịch → trả kết quả.
    - Tìm và phân tích chi tiết các nghiệp vụ: Validate số tài khoản, validate request, Kiểm tra số dư, đồng sở hữu, Kiểm tra giao dịch (gói dịch vụ, phí hạn mức, cot...), lưu dữ liệu giao dịch, xác nhận giao dịch, hạch toán timeout, rollback, các nghiệp vụ cần xử lý sau giao dịch(cập nhật trạng thái, gửi email...)
    - Tìm và mô tả flow về trạng thái giao dịch (khởi tạo, chờ duyệt, thành công, thất bại, timeout, rollback...)
    - Ghi nhận cấu trúc Controller: endpoint naming, method naming, request/response DTO pattern.
    - **Ví dụ Controller tài chính cần scan**: `TransferController`, `PaymentController`, `WithdrawController`, `TopupController` hoặc tương đương.
    - Write findings to `base_knowledge/standards/financial_flow_standard.md`.

- **`non_financial_flow_standard.md`** (Extend):
    - Tìm các Controller xử lý nghiệp vụ phi tài chính (tra cứu số dư, lịch sử giao dịch, cập nhật thông tin, quản lý tài khoản...).
    - Trace luồng: Controller → validate request → Business logic → query data / gọi service → build response → trả kết quả.
    - Tìm patterns: phân trang (pagination), filter/search, caching, response DTO structure.
    - Ghi nhận cấu trúc Controller: endpoint naming, method naming, request/response DTO pattern.
    - **Ví dụ Controller phi tài chính cần scan**: `AccountController`, `HistoryController`, `ProfileController`, `InquiryController` hoặc tương đương.
    - Write findings to `base_knowledge/standards/non_financial_flow_standard.md`.