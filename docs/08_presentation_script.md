# Script Trình Bày - 5 Cơ Chế Backend Nổi Bật

> Thời lượng ước tính: 10-12 phút
> Ghi chú: [SLIDE] = chuyển slide, [CHẬM] = nhấn mạnh, nói chậm

---

## Mở đầu (30 giây)

Phần tiếp theo em xin trình bày về 5 cơ chế kỹ thuật nổi bật trong backend của hệ thống CareTalk. Đây là những giải pháp mà nhóm em đã thiết kế và triển khai để giải quyết các bài toán thực tế khi xây dựng một chatbot tư vấn y tế.

[SLIDE]

---

## Feature 1: RAG Pipeline (2 phút)

### Đặt vấn đề

Bài toán đầu tiên mà nhóm gặp phải là: chatbot AI thông thường chỉ trả lời dựa trên kiến thức đã được huấn luyện sẵn. Nhưng trong lĩnh vực y tế, kiến thức liên tục được cập nhật, và mỗi phòng khám có quy trình, phác đồ điều trị riêng. Nếu mỗi lần thêm tài liệu mới mà phải huấn luyện lại mô hình thì vừa tốn kém, vừa không thực tế.

### Giải pháp

[SLIDE]

Nhóm em đã triển khai kỹ thuật RAG - Retrieval Augmented Generation. Ý tưởng cốt lõi là: thay vì nhồi kiến thức vào mô hình, chúng em [CHẬM] tìm kiếm tài liệu liên quan mỗi khi có câu hỏi, rồi đưa tài liệu đó vào prompt cho LLM tham khảo.

Quy trình gồm 2 giai đoạn:

**Giai đoạn nạp tài liệu:** Tài liệu y tế được đưa qua TextChunker - chia thành các đoạn nhỏ 800 tokens, có phần trùng lặp 100 tokens giữa các đoạn để không bị mất ngữ cảnh tại điểm cắt. Một điểm đặc biệt là TextChunker ưu tiên cắt tại ranh giới tự nhiên: cuối đoạn văn trước, rồi mới đến cuối câu, chứ không cắt bừa giữa câu. Sau đó, từng đoạn được chuyển thành vector embedding bằng OpenAI và lưu vào PostgreSQL với extension pgvector.

**Giai đoạn truy vấn:** Khi bệnh nhân hỏi, câu hỏi cũng được chuyển thành vector, rồi dùng cosine distance để tìm 3 tài liệu có nội dung gần nhất. Chỉ lấy tài liệu có similarity từ 0.75 trở lên để đảm bảo chất lượng.

[CHẬM] Điểm quan trọng: nếu RAG không tìm được tài liệu nào phù hợp, hoặc nếu hệ thống RAG gặp lỗi, chatbot vẫn trả lời bình thường - chỉ là không có thêm kiến thức tham khảo. Đây là thiết kế graceful fallback.

---

## Feature 2: Prompt Assembly Pipeline (2 phút)

### Đặt vấn đề

[SLIDE]

Bài toán thứ hai: mỗi câu trả lời của chatbot cần kết hợp rất nhiều nguồn thông tin khác nhau. Có chỉ dẫn hệ thống - ví dụ "bạn là chatbot y tế, hãy trả lời bằng tiếng Việt". Có kiến thức từ RAG. Có lịch sử hội thoại. Và có câu hỏi hiện tại của bệnh nhân. Nếu ghép các thông tin này một cách thủ công sẽ rất rối, khó bảo trì, và dễ vượt giới hạn token.

### Giải pháp

Nhóm em xây dựng PromptAssembler - một pipeline lắp ráp prompt tự động gồm 4 lớp:

**Lớp 1** là System Prompt - được lưu trong database, không hardcode trong code. Nghĩa là có thể thay đổi hành vi chatbot mà không cần deploy lại ứng dụng.

**Lớp 2** là RAG Context - kiến thức y tế liên quan từ Feature 1.

**Lớp 3** là lịch sử hội thoại. Ở đây có một logic thông minh: ContextBuilder tự động chọn chiến lược. Nếu hội thoại có 20 tin nhắn trở xuống thì gửi toàn bộ lịch sử. Nếu vượt 20 tin nhắn thì chỉ gửi bản tóm tắt cộng với những tin nhắn gần nhất.

**Lớp 4** là tin nhắn hiện tại của bệnh nhân.

[CHẬM] Điểm hay của thiết kế này là mỗi lớp đều là optional. RAG có thể không tìm được gì - null. Summary có thể chưa được tạo - null. Hệ thống vẫn hoạt động bình thường. Mỗi lớp độc lập, dễ test riêng, dễ mở rộng.

---

## Feature 3: Auto-Summary (2 phút)

### Đặt vấn đề

[SLIDE]

Đây là feature mà nhóm em tâm đắc nhất. Trong y tế, một cuộc hội thoại có thể kéo dài hàng trăm tin nhắn. Bệnh nhân mô tả triệu chứng, bot hỏi thêm, bệnh nhân bổ sung thông tin, rồi thảo luận về thuốc, về tiền sử bệnh. Nếu gửi toàn bộ lịch sử cho LLM thì sẽ vượt giới hạn token và tốn rất nhiều chi phí API. Nhưng nếu cắt bỏ tin nhắn cũ thì sẽ mất thông tin y tế quan trọng - ví dụ bệnh nhân nói "tôi bị dị ứng penicillin" ở tin nhắn thứ 5, mà đến tin nhắn thứ 50 bot lại quên mất.

### Giải pháp

SummaryGenerator giải quyết bài toán này bằng cơ chế tóm tắt tự động. Khi hội thoại vượt 20 tin nhắn, hệ thống sẽ tạo bản tóm tắt có cấu trúc y tế: liệt kê riêng triệu chứng, thuốc đã dùng, chỉ số y tế, tiền sử bệnh, và diễn biến. Bản tóm tắt này được lưu vào database.

Ba điểm kỹ thuật đáng chú ý:

**Thứ nhất**, tóm tắt chạy bất đồng bộ bằng annotation @Async. Nghĩa là khi bệnh nhân gửi tin nhắn, họ nhận được phản hồi ngay lập tức, việc tóm tắt chạy ngầm phía sau, không ảnh hưởng trải nghiệm.

**Thứ hai**, nhóm dùng model gpt-4o-mini cho tóm tắt thay vì model chính gpt-4o. Chi phí rẻ hơn khoảng 10 lần mà chất lượng tóm tắt vẫn đảm bảo.

**Thứ ba**, khi hội thoại tiếp tục dài hơn, hệ thống không tóm tắt lại từ đầu mà regenerate: kết hợp bản tóm tắt cũ với các tin nhắn mới để tạo bản tóm tắt mới. Tiết kiệm token đáng kể.

---

## Feature 4: SSE Streaming (1.5 phút)

### Đặt vấn đề

[SLIDE]

Feature thứ tư liên quan đến trải nghiệm người dùng. Khi gọi API của OpenAI, thời gian sinh câu trả lời đầy đủ mất từ 3 đến 10 giây. Nếu bắt người dùng chờ hết mới hiển thị, ứng dụng sẽ có cảm giác bị treo.

### Giải pháp

Nhóm triển khai SSE - Server-Sent Events. Giống như ChatGPT, câu trả lời xuất hiện từng từ một ngay khi LLM sinh ra, không cần chờ hoàn thành.

Về mặt kỹ thuật, nhóm dùng WebClient reactive của Spring WebFlux. Backend nhận stream từ OpenAI, parse từng chunk SSE để trích xuất nội dung, rồi chuyển tiếp cho mobile app. Toàn bộ pipeline là reactive - không block thread nào, xử lý rất hiệu quả.

Một điểm thiết kế quan trọng: nếu stream bị lỗi giữa chừng, hệ thống không crash mà trả về thông báo lỗi. Người dùng vẫn nhận được phản hồi thay vì màn hình trống.

---

## Feature 5: Dual Authentication (2 phút)

### Đặt vấn đề

[SLIDE]

Feature cuối cùng giải quyết bài toán: làm sao để cùng một backend phục vụ 2 loại người dùng hoàn toàn khác nhau?

Một bên là người dùng đã đăng ký - bệnh nhân, bác sĩ, admin - cần xác thực chặt chẽ, lưu lịch sử, quản lý quyền. Bên kia là khách vãng lai chỉ muốn thử chatbot nhanh, không muốn đăng ký tài khoản.

### Giải pháp

Nhóm thiết kế Security Filter Chain kép:

Với người dùng đăng nhập, hệ thống dùng Firebase Authentication. Mỗi request gửi kèm Firebase ID Token, backend xác thực token này qua Firebase Admin SDK. Có một cơ chế đặc biệt là UserSyncService: lần đầu tiên user đăng nhập, hệ thống tự động tạo bản ghi user trong PostgreSQL với role mặc định là PATIENT. [CHẬM] PostgreSQL là nguồn dữ liệu chính cho role và trạng thái, không phụ thuộc vào Firebase Claims.

Với người dùng ẩn danh, chỉ cần một API Key đơn giản trong header. Hệ thống tạo session tạm, giới hạn tối đa 10 tin nhắn. Và đây là phần hay: có một AnonymousCleanupJob chạy định kỳ mỗi giờ, tự động xóa toàn bộ dữ liệu ẩn danh quá 24 giờ. Hard-delete - xóa tin nhắn trước do ràng buộc khóa ngoại, rồi xóa conversation. Đảm bảo không tích tụ dữ liệu rác và tuân thủ quyền riêng tư.

---

## Kết (30 giây)

[SLIDE]

Tóm lại, 5 cơ chế này giải quyết 5 bài toán cốt lõi khi xây dựng chatbot y tế:

- RAG giúp chatbot có kiến thức chuyên sâu mà không cần huấn luyện lại
- Prompt Assembly giúp tổ chức ngữ cảnh phức tạp một cách có hệ thống
- Auto-Summary giữ được thông tin y tế quan trọng trong hội thoại dài
- SSE Streaming mang lại trải nghiệm mượt mà cho người dùng
- Dual Auth cho phép phục vụ cả người dùng chính thức lẫn khách vãng lai trên cùng một hệ thống

Trên đây là phần trình bày về backend. Em xin cảm ơn.
