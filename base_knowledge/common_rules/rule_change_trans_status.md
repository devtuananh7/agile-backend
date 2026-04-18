# Transaction Status Change Rules

> Source: [Nguyên tắc xử lý giao dịch và cập nhật trạng thái](https://wiki.servicehub.vn/spaces/DVNHKS/pages/778241323/Nguy%C3%AAn+t%E1%BA%AFc+x%E1%BB%AD+l%C3%BD+giao+d%E1%BB%8Bch+v%C3%A0+c%E1%BA%ADp+nh%E1%BA%ADt+tr%E1%BA%A1ng+th%C3%A1i)
> Phạm vi: Nguyên tắc bắt buộc cho mọi logic xử lý giao dịch — áp dụng cho cả batch job và API xử lý real-time.

---

## 1. Nguyên tắc chung

- **Tất cả giao dịch** yêu cầu PHẢI cập nhật sang trạng thái khác so với trạng thái lúc lấy giao dịch ra xử lý
- Việc cập nhật trạng thái (database, cache — tùy bài toán) PHẢI thực hiện **TRƯỚC KHI** xử lý logic nghiệp vụ (Authen, chuyển tiền, v.v.)

```
┌────────────────────┐     ┌────────────────────┐     ┌────────────────────┐
│  Lấy giao dịch     │────▶│  Cập nhật trạng     │────▶│  Xử lý logic       │
│  (status = Init)   │     │  thái NGAY LẬP TỨC  │     │  nghiệp vụ         │
│                    │     │  (status = In-Proc)  │     │  (Validate, Transfer│
│                    │     │                      │     │   Confirm, v.v.)    │
└────────────────────┘     └────────────────────┘     └────────────────────┘
       ✅ ĐÚNG: Thay đổi trạng thái TRƯỚC → tránh xử lý trùng lặp
```

## 2. Bad Case — Job xử lý giao dịch chuyển khoản định kỳ

Ví dụ tình huống phát sinh thực tế nếu làm SAI:

```
B1. Lấy giao dịch trạng thái Init

B2. Thực hiện xử lý theo logic    ← SAI: Chưa đổi trạng thái trước khi xử lý
    B2.1 Validate
         - Thành công → update trạng thái = In-Process   ← Quá muộn!
         - Fail → update trạng thái = Fail
    B2.2 Chuyển khoản
    B2.3 Update trạng thái vào database
         - Thành công → update = Success
         - Không thành công → update = Fail
         - Timeout → update = Timeout

B3. Thông báo qua OTT cho khách hàng
```

**Rủi ro**: Nếu ở bước B2.3 database bị lỗi hoặc Service bị Stop rồi Start lại → giao dịch vẫn ở trạng thái Init → Job sẽ lấy lại và xử lý **LẦN NỮA** → Chuyển tiền cho KH 2+ lần do không change/khóa trạng thái đúng cách.

## 3. Pattern đúng

```
B1. Lấy giao dịch trạng thái Init

B2. CẬP NHẬT TRẠNG THÁI = In-Process ngay lập tức      ← ĐÚNG: Đổi trạng thái ĐẦU TIÊN
    (database hoặc cache tùy bài toán)

B3. Thực hiện xử lý theo logic
    B3.1 Validate
         - Fail → update trạng thái = Fail, RETURN
    B3.2 Chuyển khoản
    B3.3 Update trạng thái vào database
         - Thành công → update = Success
         - Không thành công → update = Fail
         - Timeout → update = Timeout

B4. Thông báo qua OTT cho khách hàng
```

**Lợi ích**: Nếu Service bị Stop/Restart → giao dịch đã ở trạng thái In-Process → Job KHÔNG lấy lại → KHÔNG xử lý trùng.
