# Requirement Structures

_File này định nghĩa danh sách các structure PHẢI được reasoning và derive từ codebase khi chạy skill `initial-project`._

## Base Requirements (Bắt buộc)

Các structure sau PHẢI được reasoning và sinh file output:

| # | File Name              | Mô tả                                                                          |
|---|------------------------|---------------------------------------------------------------------------------|
| 1 | `system_overview.md`   | Tổng quan kiến trúc hệ thống, tech stack, infrastructure dependencies, service map |

## Extend Requirements (Tùy chọn)

Các structure sau là TÙY CHỌN — từng dự án có thể thêm hoặc bớt tùy theo nhu cầu.
Format giống Base Requirements: tên file + mô tả tác dụng.
Mỗi entry tương ứng với một service/module trong dự án.

| # | File Name              | Mô tả                                                                          |
|---|------------------------|---------------------------------------------------------------------------------|

> **Hướng dẫn mở rộng**: Để thêm service/module cần reasoning, thêm một dòng vào bảng Extend Requirements
> với tên file (dạng `<service-name>.md`) và mô tả tác dụng. Flow reasoning của skill
> `initial-project` sẽ tự động nhận diện và sinh file tương ứng vào `base_knowledge/structures/`.
>
> **Ví dụ**:
> ```
> | 1 | `authen-service.md`    | Service xác thực người dùng, quản lý JWT, OTP                                 |
> | 2 | `transfer-service.md`  | Service chuyển tiền nội bộ và liên ngân hàng                                   |
> ```
