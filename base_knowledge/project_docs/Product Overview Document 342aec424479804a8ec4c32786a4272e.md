# Product Overview Document

---

# PRODUCT OVERVIEW

**Tên dự án:** CareTalk – Chatbot Phòng khám gia đình

**Version:** v1.0

**Ngày tạo:** 12/04/2025

**Người lập:** Phạm Thanh Nhã

**Trạng thái:** Draft

---

## 1. Giới thiệu & Bối cảnh dự án

### 1.1 Bối cảnh thực tế

- Trong đời sống hiện nay, nhu cầu chăm sóc sức khỏe và tư vấn y tế từ xa ngày càng tăng cao, đặc biệt là sau các biến động về dịch bệnh khiến người dân hình thành thói quen giao tiếp trực tuyến.
- Mô hình "Phòng khám gia đình" đang trở thành xu hướng vì tính cá nhân hóa và khả năng theo dõi sức khỏe dài hạn, tuy nhiên việc kết nối giữa bệnh nhân và bác sĩ vẫn còn nhiều rào cản về mặt địa lý và thời gian.
- Sự phát triển của Trí tuệ nhân tạo (AI) và Chatbot mở ra cơ hội để sàng lọc bệnh lý ban đầu, giúp tối ưu hóa nguồn lực y tế vốn đang khan hiếm.

### 1.2 Vấn đề tồn tại (Problem Statement)

- **Vấn đề 1 - Quá tải kênh truyền thống:** Hệ thống hotline và tiếp nhận tại chỗ thường xuyên rơi vào tình trạng quá tải, khiến bệnh nhân phải chờ đợi lâu chỉ để hỏi các thông tin cơ bản hoặc triệu chứng nhẹ.
- **Vấn đề 2 - Rào cản tiếp cận:** Bệnh nhân ở xa hoặc có lịch trình bận rộn khó có thể sắp xếp thời gian đến phòng khám trực tiếp cho những lần tư vấn định kỳ hoặc hỏi đáp nhanh.
- **Vấn đề 3 - Thiếu tính liên tục trong dữ liệu:** Thông tin tư vấn qua điện thoại hoặc chat rời rạc thường không được lưu trữ tập trung, dẫn đến việc bác sĩ khó nắm bắt toàn bộ lịch sử bệnh lý trong những lần tư vấn sau.
- **Vấn đề 4 - Tâm lý e ngại:** Nhiều bệnh nhân có tâm lý tự tra cứu triệu chứng trên mạng (Google) dẫn đến việc hiểu sai lệch về bệnh tình và gây lo lắng không cần thiết thay vì được tư vấn từ nguồn uy tín.

### 1.3 Lý do chọn đề tài

- Đề bài cô ra

### 1.4 Mục tiêu dự án

**Mục tiêu tổng quát:** Xây dựng hệ thống Chatbot phòng khám gia đình thông minh (CareTalk) nhằm hỗ trợ tư vấn sức khỏe ban đầu, kết nối bệnh nhân với bác sĩ chuyên khoa và quản lý tập trung lịch sử ca bệnh trực tuyến.

**Mục tiêu cụ thể:**

- **Hỗ trợ 24/7:** Chatbot tự động trả lời các câu hỏi thường gặp (FAQ) và hướng dẫn sơ bộ triệu chứng cho bệnh nhân mọi lúc, mọi nơi.
- **Tối ưu hóa kết nối:** Xây dựng quy trình tạo ca tư vấn (Case) và gán bác sĩ phản hồi trong thời gian nhanh nhất.
- **Quản lý dữ liệu tập trung:** Đảm bảo mọi ca tư vấn đều được ghi nhận trạng thái (Lifecycle) và có kết luận từ bác sĩ để phục vụ tra cứu lịch sử.
- **Nâng cao trải nghiệm người dùng:** Cung cấp giao diện trực quan cho cả Bệnh nhân (Mobile) và Bác sĩ (Mobile), đồng thời có hệ thống quản trị (Admin Web) để theo dõi tổng thể hiệu quả dự án.

---

## 2. Product Overview (Tổng quan sản phẩm)

### 2.1 Product Vision

Cho những cá nhân và gia đình có nhu cầu tư vấn y tế nhanh chóng nhưng ngại các thủ tục hành chính tại bệnh viện, CareTalk là hệ thống trợ lý y tế thông minh giúp xóa bỏ nỗi lo lắng về bệnh tật và giảm tải cho hệ thống y tế bằng giải pháp sàng lọc triệu chứng bằng AI (RAG) kết hợp kết nối trực tiếp với đội ngũ bác sĩ tình nguyện.

### 2.2 Product Description (Mô tả hệ thống)

Hệ thống được thiết kế theo kiến trúc đa nền tảng để phục vụ các nhóm đối tượng khác nhau:

- **Patient Mobile App (Ứng dụng cho Bệnh nhân):**
    - Giao diện chat thân thiện, hỗ trợ nhập liệu bằng văn bản và hình ảnh.
    - Cung cấp luồng tư vấn tự động (AI Chatbot) và luồng kết nối bác sĩ trực tuyến khi có yêu cầu.
    - Quản lý hồ sơ sức khỏe cá nhân, tiền sử bệnh và lịch sử các ca tư vấn.
- **Doctor Mobile App (Ứng dụng cho Bác sĩ):**
    - Hệ thống quản lý danh sách ca bệnh (Case Queue) được phân loại theo mức độ ưu tiên (Nhẹ/Trung bình/Nặng).
    - Công cụ chat trực tiếp với bệnh nhân kèm theo thông tin tóm tắt triệu chứng đã được Chatbot thu thập.
    - Tính năng kết luận ca bệnh và chuyển trạng thái hồ sơ.
- **Admin Web Portal (Cổng quản trị hệ thống):**
    - Dashboard theo dõi tổng thể hoạt động: số lượng hội thoại, phân bổ mức độ bệnh và hiệu suất phản hồi của bác sĩ.
    - Quản lý danh mục người dùng, phê duyệt hồ sơ bác sĩ sau khi xác minh offline và quản lý các quy tắc (Rules) cảnh báo nguy hiểm.
- **AI Chatbot Service (Dịch vụ Chatbot thông minh):**
    - Sử dụng công nghệ RAG (Retrieval-Augmented Generation) để truy xuất câu trả lời từ Knowledge Base y tế chuẩn, đảm bảo tính chính xác và an toàn.
    - Bộ máy phân loại mức độ bệnh (Triage Engine) để điều hướng người dùng: tự chăm sóc, kết nối bác sĩ hoặc đi cấp cứu ngay lập tức.

### 2.3 Product Goals (Mục tiêu sản phẩm)

- **Tính Khả dụng & Kết nối (Availability & Connectivity):**
    - **Phản hồi tức thì:** Đảm bảo bệnh nhân luôn nhận được phản hồi sơ bộ ngay lập tức từ Chatbot bất kể khung giờ nào (24/7).
    - **Kết nối linh hoạt:** Đảm bảo bệnh nhân có thể kết nối với bác sĩ tình nguyện để tư vấn trực tiếp khi Chatbot nhận diện mức độ bệnh từ Trung bình trở lên.
    - **Truy xuất thông tin:** Cho phép bệnh nhân dễ dàng truy xuất và tải lại kết luận tư vấn chính thức từ bác sĩ ngay sau khi ca bệnh kết thúc.
- **Chính xác & An toàn (Accuracy & Safety):**
    - **Kiểm soát thông tin:** Chatbot chỉ cung cấp thông tin tư vấn dựa trên dữ liệu y khoa đã kiểm duyệt (RAG), tuyệt đối không đưa ra các thông tin gây hại hoặc đoán mò.
    - **Sàng lọc nguy cơ:** Hệ thống tự động nhận diện và đưa ra cảnh báo khẩn cấp, hướng dẫn đi cấp cứu đối với các danh mục triệu chứng nguy hiểm (Đau ngực, khó thở, nôn ra máu...).
- **Tối ưu hóa nguồn lực (Efficiency):**
    - **Sàng lọc thông minh:** Giảm ít nhất **60%** các câu hỏi lặp lại và các ca bệnh nhẹ (Common Cold) cho đội ngũ bác sĩ thông qua việc sàng lọc và giải đáp tự động bởi AI.
    - **Điều phối tự động:** Tự động gán ca bệnh cho bác sĩ có chuyên môn phù hợp và đang có trạng thái online để tối ưu thời gian chờ của bệnh nhân.
- **Tính liên tục của thông tin:** Toàn bộ lịch sử tư vấn được lưu trữ để phục vụ cho các lần chăm sóc sức khỏe định kỳ sau này của gia đình.

### 2.4 Assumptions & Constraints (Giả định & ràng buộc)

- **Giả định (Assumptions):**
    - **Hạ tầng kỹ thuật:** Người dùng đảm bảo có kết nối internet ổn định để duy trì luồng hội thoại thời gian thực (real-time) với chatbot và bác sĩ.
    - **Nhân sự tình nguyện:** Đội ngũ bác sĩ tham gia với tinh thần đóng góp cộng đồng, cam kết hoạt động trong các khung giờ nhất định đã đăng ký.
    - **Độ tin cậy dữ liệu:** Cơ sở dữ liệu tri thức (Knowledge Base) phục vụ AI trả lời được giả định là đã qua thẩm định của chuyên gia y tế.
    - **Quy tắc xử lý ca bệnh:**
        - Mỗi ca tư vấn chỉ được phụ trách bởi **01 bác sĩ duy nhất** và kết thúc bằng **01 bản kết luận cuối cùng**.
        - Sau khi trạng thái ca bệnh chuyển sang Hoàn thành, cửa sổ chat sẽ tự động khóa (chế độ Read-only) để đảm bảo tính lưu trữ dữ liệu.
        - Nếu bệnh nhân phát sinh nhu cầu tư vấn tiếp, hệ thống sẽ thực hiện luồng khởi tạo **ca bệnh mới và phiên chat mới** thay vì dùng lại ca cũ.
- **Ràng buộc (Constraints):**
    - **Pháp lý:** Hệ thống chỉ mang tính chất tư vấn, không thay thế chẩn đoán lâm sàng tại bệnh viện; bác sĩ chỉ được kê đơn đối với các tình trạng đơn giản theo quy định. Với các triệu chứng nguy hiểm, hệ thống sẽ hiển thị cảnh báo đi cấp cứu.
    - **Kỹ thuật:** MVP tập trung vào giao diện di động (Mobile App) cho người dùng cuối và giao diện Web cho quản trị viên.
    - **Phạm vi:** MVP chỉ hỗ trợ chat text và gửi ảnh, không hỗ trợ video call trong giai đoạn này.

### **2.5 Product Roadmap & Target Releases (01 tháng)**

| Release | Thời gian | Mục tiêu | Scope chính |
| --- | --- | --- | --- |
| Release 1 – MVP Core | Tuần 1–2 | Có thể demo end-to-end 1 ca tư vấn | Đăng nhập, Chatbot basic, Create Case, Doctor nhận ca, Chat, Conclusion, Notification |
| Release 2 – Stabilize & History | Tuần 3 | Hoàn thiện trải nghiệm và lưu trữ | Đăng ký, Quên mật khẩu, Case history, View conclusion, Attachment, Search/Filter case list |
| Release 3 – Admin & Optimization | Tuần 4 | Có vận hành cơ bản | Admin web, doctor approval, dashboard basic, triage rules confi |

---

## 3. Target Users & Personas (Đối tượng sử dụng)

### 3.1 Target User Segments

| Segment | Mô tả | Nhu cầu | Ưu tiên |
| --- | --- | --- | --- |
| Bệnh nhân (Patients) | Người gặp vấn đề sức khỏe nhẹ hoặc chưa rõ mức độ. | Được tư vấn nhanh 24/7, biết khi nào cần đi khám, nhận hướng dẫn chăm sóc tại nhà. | P0 |
| Bác sĩ (Doctors) | Bác sĩ hoặc sinh viên y khoa năm cuối tham gia tình nguyện. | Đóng góp cộng đồng, nhận ca đã được lọc thông tin rõ ràng, giao diện đơn giản. | P0 |
| Quản trị viên (Admin) | Người vận hành và kiểm soát chất lượng hệ thống. | Theo dõi hoạt động, quản lý người dùng, kiểm duyệt nội dung và quản lý rule cảnh báo. | P1 |

<aside>
💡

Với P0 (Priority 0) - Bắt buộc phải có, P1 (Priority 1) - Nên có

</aside>

### 3.2 Personas

#### Nhóm 1: Bệnh nhân (Patient)

**Persona 1: Mẹ bỉm sữa (Mẹ bận rộn)**

- **Mục tiêu:** Muốn có nguồn tư vấn đáng tin cậy để đánh giá nhanh tình trạng của con mà không phải vội vàng đưa đi viện.
- **Pain points:** Lo lắng khi con có triệu chứng bất thường vào ban đêm; ngại đi viện vì chờ lâu; thông tin trên mạng mâu thuẫn.
- **Hành vi sử dụng:** Thường sử dụng vào ban đêm hoặc giờ nghỉ; mô tả triệu chứng bằng ngôn ngữ tự nhiên; thường gửi ảnh (phát ban, họng, nhiệt kế…).
- **Rào cản:** Dễ hoang mang, có xu hướng hỏi nhiều lần để xác nhận.
- **Kỳ vọng:** Được hướng dẫn rõ ràng “nên theo dõi tại nhà hay cần đi khám”.

**Persona 2: Người cao tuổi / Người có bệnh nền**

- **Mục tiêu:** Muốn được tư vấn khi có triệu chứng bất thường liên quan bệnh nền và cần lời khuyên an toàn.
- **Pain points:** Khó phân biệt triệu chứng nhẹ và dấu hiệu nguy hiểm; không quen mô tả rõ; ngại đi bệnh viện.
- **Hành vi sử dụng:** Thao tác chậm; ưu tiên nút bấm đơn giản; trả lời theo câu hỏi gợi ý.
- **Động lực:** Muốn yên tâm về sức khỏe và giảm phụ thuộc vào con cái.
- **Rào cản:** Không quen dùng ứng dụng, dễ bỏ cuộc nếu nhiều bước.

**Persona 3 (Patient): Người đi làm bận rộn (Gen Z/Millennial)**

- **Mục tiêu:** Muốn được tư vấn nhanh về các triệu chứng thường gặp (đau họng, sốt, đau dạ dày, stress, mất ngủ…) mà không phải xin nghỉ làm đi khám.
- **Pain points:** Không có thời gian chờ khám; khó sắp xếp lịch; lo ngại triệu chứng kéo dài nhưng chưa đủ nghiêm trọng để đi viện; thiếu nguồn tư vấn đáng tin cậy.
- **Hành vi sử dụng:** Thường dùng vào buổi tối sau giờ làm hoặc sáng sớm; thích trả lời nhanh theo câu hỏi gợi ý; ưu tiên trải nghiệm giống chat messenger; có xu hướng muốn “câu trả lời ngắn, rõ, dễ làm theo”.
- **Động lực:** Muốn xử lý nhanh vấn đề sức khỏe để không ảnh hưởng công việc và cuộc sống.
- **Rào cản:** Dễ bỏ dở nếu chatbot hỏi quá nhiều hoặc quy trình tạo ca quá dài; ngại cung cấp thông tin cá nhân.
- **Kỳ vọng:** Có hướng dẫn rõ ràng “nên tự theo dõi, mua thuốc gì, hay đi khám ngay” + có thể hỏi bác sĩ nếu cần.

---

#### Nhóm 2: Bác sĩ (Doctor)

**Persona 4: Bác sĩ trẻ (bác sĩ mới đi làm)**

- **Mục tiêu:** Tư vấn online nhanh gọn, nâng cao kinh nghiệm và xây dựng uy tín chuyên môn.
- **Pain points:** Thời gian không cố định; lo ngại tư vấn sai hoặc thiếu dữ liệu; cần thông tin bệnh nhân rõ ràng.
- **Hành vi sử dụng:** Online linh hoạt; ưu tiên case có triệu chứng cụ thể; thích giao diện rõ ràng, ít thao tác.
- **Rào cản:** Không muốn mất thời gian cho case thiếu thông tin hoặc spam.

**Persona 5: Bác sĩ về hưu**

- **Mục tiêu:** Tiếp tục đóng góp kiến thức chuyên môn cho cộng đồng.
- **Pain points:** Ngại thao tác công nghệ phức tạp; khó đọc chữ nhỏ; không quen thao tác nhiều bước.
- **Hành vi sử dụng:** Thích xem thông tin ca bệnh được tóm tắt rõ ràng; ưu tiên các case đơn giản, phổ biến.
- **Động lực:** Muốn giúp người bệnh bằng kinh nghiệm lâu năm và định hướng an toàn.
- **Rào cản:** Dễ gặp khó khăn khi app có nhiều tính năng hoặc UI rối.

---

#### Nhóm 3: Vận hành hệ thống (Admin/Operator)

**Persona 6: Người có hiểu biết về CNTT**

- **Mục tiêu:** Đảm bảo hệ thống hoạt động ổn định, kiểm soát chất lượng chatbot và quản lý dữ liệu.
- **Pain points:** Khó kiểm soát phản hồi AI nếu dữ liệu đầu vào không chuẩn; cần giám sát các ca bất thường.
- **Hành vi sử dụng:** Theo dõi dashboard; kiểm tra log; điều chỉnh rule/prompt; xử lý tài khoản vi phạm.
- **Động lực:** Giảm rủi ro hệ thống và đảm bảo an toàn cho bệnh nhân và bác sĩ.

---

## 4. Scope Definition (Phạm vi sản phẩm)

### 4.1 In Scope (MVP)

Phạm vi MVP bao gồm các tính năng tối thiểu nhưng đủ để hình thành một **chu trình tư vấn y tế trực tuyến khép kín**, đảm bảo người dùng có thể: mô tả triệu chứng → được sàng lọc ban đầu → kết nối bác sĩ → trao đổi trực tuyến → nhận kết luận → lưu lịch sử tra cứu.

#### A. Hệ thống AI Chatbot & Sàng lọc ban đầu (Triage)

MVP sẽ triển khai chatbot AI nhằm hỗ trợ bệnh nhân tư vấn sơ bộ và sàng lọc mức độ triệu chứng trước khi chuyển tiếp cho bác sĩ.

Các chức năng bao gồm:

- **Tư vấn triệu chứng:** Tiếp nhận mô tả bệnh lý của người dùng thông qua văn bản; hỗ trợ đính kèm hình ảnh để phục vụ tham khảo (ưu tiên phục vụ bác sĩ trong quá trình tư vấn).
- **Hỏi đáp bổ sung (Follow-up):** Chatbot tự động đặt các câu hỏi bổ sung nhằm thu thập thêm thông tin (ví dụ: thời gian xuất hiện triệu chứng, mức độ đau, dấu hiệu kèm theo…).
- **Knowledge Base (RAG):** Chatbot phản hồi dựa trên nguồn tài liệu y khoa chuẩn đã được kiểm duyệt nhằm đảm bảo nội dung tư vấn mang tính định hướng và an toàn.
- **Phân loại mức độ (Triage Level):** Hệ thống tự động phân loại tình trạng thành 4 cấp độ:
    - **Nhẹ:** chatbot hướng dẫn theo dõi tại nhà.
    - **Trung bình:** khuyến nghị tạo ca tư vấn với bác sĩ.
    - **Nặng:** ưu tiên tạo ca tư vấn và gán bác sĩ xử lý sớm.
    - **Nguy hiểm:** cảnh báo tình huống khẩn cấp, khuyến nghị gọi cấp cứu hoặc đến cơ sở y tế ngay.

#### B. Quản lý ca bệnh & Kết nối bác sĩ (Case Management)

Hệ thống hỗ trợ quản lý ca bệnh theo quy trình chuẩn, giúp bác sĩ và bệnh nhân theo dõi tiến trình tư vấn.

Các chức năng bao gồm:

- **Khởi tạo ca bệnh (Create Case):** Người dùng đã đăng nhập có thể tạo ca bệnh để gặp bác sĩ, đặc biệt trong trường hợp triage ở mức Trung bình/Nặng.
- **Điều phối bác sĩ (Assign Doctor):** Hệ thống tự động gán ca cho bác sĩ đang online và có chuyên môn phù hợp (dựa trên cấu hình chuyên khoa).
- **Theo dõi trạng thái ca bệnh (Lifecycle):** Case được quản lý theo các trạng thái nghiệp vụ (Chờ tư vấn → Đang tư vấn → Chờ xác nhận kết thúc ca tư vấn → Hoàn thành) để đảm bảo quá trình xử lý rõ ràng và có điểm kết thúc.
- **Tra cứu kết luận:** Các kết luận tư vấn sẽ được lưu trữ để bệnh nhân có thể xem lại khi cần.

#### C. Kênh hội thoại trực tuyến (Consultation)

MVP triển khai kênh chat trực tiếp giữa bệnh nhân và bác sĩ theo từng ca bệnh.

Các chức năng bao gồm:

- **Chat realtime:** Hỗ trợ trao đổi tin nhắn văn bản và gửi ảnh trong quá trình tư vấn.
- **Kết luận ca bệnh (Conclusion):** Bác sĩ ghi nhận nhận định chuyên môn, hướng xử trí và khuyến nghị theo dõi; sau đó đóng ca tư vấn.
- **Lịch sử tư vấn:** Lưu trữ toàn bộ nội dung chat và kết luận để bệnh nhân có thể xem lại khi cần.

#### D. Quản trị & Vận hành hệ thống (Admin Control)

MVP cung cấp các chức năng quản trị tối thiểu nhằm đảm bảo vận hành hệ thống an toàn và kiểm soát chất lượng.

Các chức năng bao gồm:

- **Phê duyệt tài khoản bác sĩ:** Admin thực hiện quy trình duyệt tài khoản sau khi xác minh danh tính bác sĩ (thực hiện offline).
- **Quản lý Rule cảnh báo nguy hiểm:** Admin cấu hình danh mục triệu chứng/dấu hiệu nguy hiểm để chatbot hiển thị cảnh báo cấp cứu phù hợp.
- **Dashboard vận hành:** Theo dõi số lượng ca bệnh theo trạng thái và hiệu suất phản hồi cơ bản của hệ thống.
- **Quản lý Rule điều hướng bác sĩ:** Admin được phép cấu hình phân loại bác sĩ vào các chuyên khoa phù hợp và được cấu hình các mức độ ca bệnh được phép điều hướng bác sĩ; Rule ưu tiên gán ca. (Có thể để phase sau, phase đầu có thể mặc định)

### 4.2 Out of Scope

Để đảm bảo tiến độ triển khai MVP và phù hợp timeline nộp bài, các hạng mục dưới đây sẽ được tạm hoãn và đưa vào các giai đoạn phát triển sau:

| Hạng mục | Lý do tạm hoãn | Giai đoạn dự kiến |
| --- | --- | --- |
| Cuộc gọi Video/Voice | Yêu cầu hạ tầng truyền tải phức tạp, tối ưu băng thông và trải nghiệm realtime | Phase 2 |
| Đặt lịch khám offline | Cần tích hợp sâu với lịch phòng khám và quản lý thời gian bác sĩ | Phase 2 |
| Hồ sơ bệnh án chuyên sâu | MVP chỉ lưu dữ liệu cơ bản; chưa tích hợp chuẩn HL7/DICOM hoặc hệ thống bệnh viện | Backlog |
| Quản lý rule điều hướng bác sĩ | Chưa đủ thời gian làm hoàn chỉnh | Phase 2 |

---

## 5. Business Process (Quy trình nghiệp vụ tổng quan)

### 5.1 Business Flow – Patient

1. Bệnh nhân đăng nhập ứng dụng.
2. Bệnh nhân chat với chatbot để mô tả triệu chứng.
3. Chatbot thu thập thêm dữ liệu bổ sung (nếu cần) 
4. Chatbot sàng lọc mức độ và xử lý theo kết quả phân loại 
    1. **Nhẹ:** Chatbot đưa ra hướng dẫn chăm sóc và theo dõi tại nhà, đồng thời khuyến nghị bệnh nhân tạo ca tư vấn nếu triệu chứng kéo dài hoặc chuyển nặng.
    2. **Trung bình/Nặng:** Chatbot khuyến nghị bệnh nhân tạo ca tư vấn để kết nối bác sĩ.
    3. **Nguy hiểm:** Hệ thống hiển thị cảnh báo khẩn cấp (đi cấp cứu/cơ sở y tế gần nhất) và cung cấp hotline hỗ trợ khẩn cấp.
5. Bệnh nhân yêu cầu Liên hệ với bác sĩ để tạo ca tư vấn → hệ thống sinh Case ID và chuyển trạng thái Pending (Chờ tư vấn)
6. Khi bác sĩ phản hồi, bệnh nhân nhận notification và vào màn hình chat.
7. Bệnh nhân chat với bác sĩ, gửi ảnh nếu cần.
8. Bác sĩ gửi kết luận và đóng ca → bệnh nhân nhận notification.
9. Bệnh nhân vào màn hình kết luận và chọn Kết thúc tư vấn → Ca tư vấn chuyển trạng thái Completed (Đã tư vấn)

**Notes:** Sau 24h nếu bệnh nhân không xác nhận kết thúc ca thì có trạng thái sẽ tự động chuyển sang Kết thúc tư vấn. Giai đoạn này có thể bỏ xác nhận kết thúc tư vấn ở bệnh nhân, chỉ cần bác sĩ gửi kết luận là chuyển trạng thái Đã tư vấn 

### 5.2 Business Flow – Doctor

1. Bác sĩ đăng nhập ứng dụng.
2. Bác sĩ xem danh sách ca bệnh đang chờ.
3. Bác sĩ chọn ca và phản hồi bệnh nhân → ca chuyển trạng thái In Consultation (Đang tư vấn)
4. Bác sĩ chat với bệnh nhân và thu thập thông tin cần thiết.
5. Bác sĩ lập kết luận và nhấn hoàn thành ca → ca chuyển trạng thái Chờ xác nhận hoàn thành tư vấn
6. Hệ thống gửi thông báo tới bệnh nhân để bệnh nhân xác nhận Hoàn thành ca tư vấn.

### 5.3 Business Flow – Admin

1. Admin đăng nhập web dashboard.
2. Admin xem danh sách ca bệnh theo trạng thái.
3. Admin quản lý danh sách bác sĩ và bệnh nhân.
4. Admin theo dõi hoạt động hệ thống và kiểm tra dữ liệu khi cần.

---

## 6. Case Status Lifecycle (Trạng thái ca bệnh)

### 6.1 Danh sách trạng thái

| Status | Mô tả |
| --- | --- |
| Pending | Ca đã tạo, chờ bác sĩ tiếp nhận |
| In Consultation | Bác sĩ đã phản hồi và đang chat với bệnh nhân |
| Completed | Bác sĩ đã gửi kết luận và đóng ca |
| Cancelled | Ca bị hủy bởi bệnh nhân hoặc hệ thống (phase 1 chưa làm) |

### 6.2 Transition Rules

| From | To | Trigger |
| --- | --- | --- |
| Pending | In Consultation | Bác sĩ nhấn vào Bắt đầu tư vấn |
| In Consultation | Completed | Bác sĩ nhấn “Gửi kết luận” |
| In Consultation | Cancelled | Bệnh nhân yêu cầu hủy (trước khi có kết luận) |
| Pending | Cancelled | Bệnh nhân hủy ca |

---

## 7. Feature List (Danh sách tính năng theo module)

### 7.1 Patient App

| Nhóm tính năng | Feature | Mô tả | Priority |
| --- | --- | --- | --- |
| Authentication | Đăng ký/Đăng nhập/Quên mật khẩu | Quản lý tài khoản bệnh nhân | P0 |
| Chatbot | Chat FAQ & hỏi triệu chứng | Chat với AI để nhận hướng dẫn sơ bộ | P0 |
| Case Management | Tạo ca bệnh | Bệnh nhân tạo case gửi tới bác sĩ | P0 |
| Case Management | Danh sách ca bệnh | Xem ca đang xử lý/đã hoàn thành | P0 |
| Consultation | Chat với bác sĩ | Chat theo từng case | P0 |
| Consultation | Gửi ảnh/file | Gửi hình ảnh liên quan triệu chứng | P0 |
| Conclusion | Xem kết luận | Xem nội dung kết luận từ bác sĩ | P0 |
| History | Xem lịch sử tư vấn | Tra cứu kết luận cũ theo ca | P0 |
| Profile | Hồ sơ cơ bản | Xem và cập nhật thông tin cá nhân | P1 |

### 7.2 Doctor App

| Nhóm tính năng | Feature | Mô tả | Priority |
| --- | --- | --- | --- |
| Authentication | Đăng nhập bác sĩ | Xác thực bác sĩ | P0 |
| Case List | Danh sách ca bệnh | Pending / In Consultation / Completed | P0 |
| Consultation | Chat với bệnh nhân | Trao đổi theo case | P0 |
| Consultation | Xem thông tin ca | Triệu chứng ban đầu, ảnh đính kèm | P0 |
| Conclusion | Lập kết luận ca | Nhập kết luận, hướng dẫn, cảnh báo | P0 |
| Case Management | Hoàn thành ca | Đóng ca và chuyển Completed | P0 |

### 7.3 Admin Web

| Nhóm tính năng | Feature | Mô tả | Priority |
| --- | --- | --- | --- |
| Admin Login | Đăng nhập | Xác thực admin | P0 |
| Case Management | Quản lý ca bệnh | Lọc theo trạng thái, tra cứu case | P0 |
| Doctor Management | Quản lý bác sĩ | Xem danh sách, khóa/mở | P1 |
| Patient Management | Quản lý bệnh nhân | Tra cứu user và lịch sử case | P1 |
| Dashboard | Báo cáo cơ bản | Thống kê số case theo ngày | P1 |

---

## 8. Notification Rules (Thông báo)

### 8.1 Events gửi notification

| Event | Receiver | Message Title | Action  |
| --- | --- | --- | --- |
| Doctor replied | Patient | Tin nhắn mới từ bác sĩ | Điều hướng tới MH Chat theo Case ID |
| Doctor completed case | Patient | Bác sĩ hoàn thành kết luận và Gửi kết luận | Điều hướng tới MH Xem kết luận |
| New case assigned | Doctor | Có ca bệnh mới cần tư vấn | Điều hướng tới MH Chi tiết ca bệnh |

---

## 9. Data Entities Overview (Thực thể dữ liệu chính)

| Entity | Mô tả |
| --- | --- |
| User | Tài khoản hệ thống (Patient/Doctor/Admin) |
| Chuyên khoa | Thông tin chuyên môn bác sĩ (chuyên khoa, mã bác sĩ, chứng chỉ, mô tả) |
| Case | Ca tư vấn gồm trạng thái, triệu chứng ban đầu, timestamps, mã bệnh nhân, mã bác sĩ |
| Message | Tin nhắn chat theo case_id |
| Attachment | Ảnh/file đính kèm trong case hoặc message |
| Conclusion | Kết luận tư vấn của bác sĩ, gắn với case_id |
| Notification | Lịch sử thông báo gửi và trạng thái đã đọc |
| AuditLog (Optional) | Log thao tác admin/bác sĩ phục vụ kiểm tra |

---

## 10. Permission Matrix (Phân quyền cơ bản)

| Role | Quyền |
| --- | --- |
| Patient | Chat, tạo case tư vấn, xem kết luận ca bệnh, xem hồ sơ, chỉnh sửa hồ sơ |
| Doctor | Xem thông tin ca bệnh được assign, chat, lập kết luận, hoàn thành ca |
| Admin | Quản lý user, quản lý rule cảnh báo, quản lý rule điều hướng, quản lý hội thoại, Xem thống kê |

---

## 11. System Integration Overview (Hệ thống liên quan)

| **System** | **Type** | **Mô tả** | **Vai trò** |
| --- | --- | --- | --- |
| **CareTalk Backend** | **Owner** | Hệ thống cốt lõi bao gồm Backend (API), Web Admin và các ứng dụng di động. | Điều phối toàn bộ luồng nghiệp vụ từ đăng ký, chatbot sàng lọc đến quản lý ca tư vấn trực tuyến. |
| **OpenAI API** | **External** | Nền tảng cung cấp mô hình ngôn ngữ lớn (LLM) để xử lý ngôn ngữ tự nhiên. | Đóng vai trò là "bộ não" của Chatbot, kết hợp với Knowledge Base (RAG) để trả lời người dùng và phân loại mức độ bệnh dựa trên triệu chứng. |
| **Firebase Cloud Messaging** | **External** | Nền tảng của Google cung cấp dịch vụ Cloud Messaging (FCM) và Realtime Database. | Quản lý và gửi thông báo đẩy (Push Notification) đến Patient/Doctor khi có sự kiện mới (tin nhắn mới, bác sĩ nhận ca, kết luận sẵn sàng). |
| **Centralized Database** | **Internal** | Cơ sở dữ liệu tập trung (ví dụ: PostgreSQL hoặc MySQL) lưu trữ toàn bộ thực thể của hệ thống | Lưu trữ thông tin người dùng, lịch sử chat, trạng thái ca bệnh và kết luận của bác sĩ cho toàn bộ hệ thống. |
| **SMS/Email Gateway** | **External** | Dịch vụ gửi tin nhắn hoặc email tự động từ bên thứ ba (ví dụ: Twilio, SendGrid). | Thực hiện gửi mã xác nhận (OTP) trong luồng đăng ký tài khoản và luồng quên mật khẩu để đảm bảo tính xác thực của người dùng. |

---

## 12. Non-functional Requirements (NFR) - Update sau =))

### 12.1 Performance

- Thời gian phản hồi API CRUD thông thường < 1 giây.
- Thời gian tải danh sách case/chat message < 2 giây.
- Thời gian phản hồi chatbot AI có thể từ 3–10 giây.

### 12.2 Security & Privacy

- Giao tiếp giữa client và server phải sử dụng HTTPS/TLS.
- Dữ liệu bệnh nhân chỉ được truy cập theo đúng role và quyền.
- Bác sĩ chỉ xem được case được assign.
- Có cơ chế đăng nhập token-based (JWT/Session).

### 12.3 Availability

- Hệ thống cần đảm bảo hoạt động ổn định 24/7 cho các chức năng chính.
- Có cơ chế retry notification nếu gửi thất bại.

### 12.4 Logging & Audit

- Lưu log thao tác quan trọng: bác sĩ gửi kết luận, admin khóa user, admin thay đổi trạng thái case.
- Lưu lịch sử cập nhật kết luận (optional).

---

## 13. UI Screens List (Danh sách màn hình dự kiến)

Link figma: 

[https://www.figma.com/design/1LMqvDOHf19rod1iUbZB7h/Project-CareTalk?node-id=47-1377&t=yZnW78gwJrAi8JCs-1](https://www.figma.com/design/1LMqvDOHf19rod1iUbZB7h/Project-CareTalk?node-id=47-1377&t=yZnW78gwJrAi8JCs-1)

### 13.1 Patient App

- Đăng ký/Đăng nhập/Quên mật khẩu
- Chat với CareTalk
- Kết quả tạo ca tư vấn
- Chat với bác sĩ
- Chi tiết hồ sơ
- Chỉnh sửa hồ sơ
- Xem kết luận

### 13.2 Doctor App

- Đăng ký/Đăng nhập/Quên mật khẩu
- Danh sách ca bệnh
- Chi tiết ca bệnh
- Chat với bệnh nhân
- Tạo kết luận ca bệnh
- Popup Hoàn thành ca

### 13.3 Admin Web

- Đăng nhập
- Dashboard
- Quản lý user
- Quản lý rule
- Quản lý cảnh báo nguy hiểm
- Review và quản lý hội thoại

---

## 14. Risks & Open Questions

### 14.1 Risks

Xác định các tình huống có thể ảnh hưởng đến sự thành công hoặc tính an toàn của hệ thống.

- **Rủi ro về độ chính xác của AI (AI Hallucination):** * **Mô tả:** Chatbot có thể đưa ra các lời khuyên y tế sai lệch hoặc không phù hợp với ngữ cảnh thực tế của bệnh nhân.
    - **Giảm thiểu:** Sử dụng kỹ thuật RAG để giới hạn câu trả lời trong Knowledge Base chuẩn; thiết lập Disclaimer (Miễn trừ trách nhiệm) ngay đầu phiên chat.
- **Rủi ro về phản hồi từ Bác sĩ:**
    - **Mô tả:** Do là mô hình tình nguyện, bác sĩ có thể không online kịp thời để xử lý các ca bệnh mức độ Nặng.
    - **Giảm thiểu:** Thiết lập hệ thống thông báo đẩy (Push notification) và rule tự động chuyển hướng bệnh nhân đến cơ sở y tế gần nhất nếu sau một khoảng thời gian (VD: 60 phút) không có bác sĩ tiếp nhận.
- **Rủi ro về trách nhiệm pháp lý:**
    - **Mô tả:** Việc tư vấn hoặc kê đơn (dù đơn giản) trực tuyến có thể phát sinh tranh chấp nếu tình trạng bệnh nhân chuyển biến xấu.
    - **Giảm thiểu:** Quy định rõ phạm vi tư vấn chỉ mang tính tham khảo; lưu trữ toàn bộ lịch sử hội thoại (Audit Log) để đối soát khi cần thiết.
- **Rủi ro về bảo mật dữ liệu:**
    - **Mô tả:** Thông tin y tế nhạy cảm của bệnh nhân có thể bị rò rỉ nếu hệ thống bị tấn công.
    - **Giảm thiểu:** Sử dụng chung một Database nhưng thực hiện mã hóa dữ liệu truyền tải (HTTPS) và phân quyền truy cập nghiêm ngặt (Role-based Access Control).

### 14.2 Open Questions

Các vấn đề cần thảo luận thêm với nhóm hoặc xin ý kiến giảng viên để hoàn thiện sản phẩm.

- **Cơ chế kiểm soát chất lượng:** Làm thế nào để Admin có thể đánh giá hiệu quả tư vấn của bác sĩ tình nguyện một cách khách quan nhất? Có nên thêm tính năng cho bệnh nhân đánh giá (Rating) bác sĩ sau mỗi ca không?.
- **Giới hạn thời gian tư vấn:** Mỗi ca bệnh nên có thời gian tối đa là bao lâu (ví dụ: 24h) trước khi hệ thống tự động đóng để tối ưu tài nguyên và đảm bảo an toàn?.
- **Xác minh danh tính Bác sĩ:** Quy trình xác minh offline cụ thể sẽ bao gồm những bước nào để vừa đảm bảo uy tín, vừa không gây phiền hà cho các bác sĩ tình nguyện?.
- **Mô hình kinh doanh/Duy trì:** Trong tương lai, làm thế nào để duy trì hệ thống nếu chi phí sử dụng API (OpenAI) và Server tăng cao khi số lượng người dùng lớn?.

---

## 15. Deliverables & Next Steps

### 15.1 Deliverables cần nộp (học thuật)

### 🚩 Giai đoạn 1: Khám phá & Định hình (Design Thinking)

Tập trung vào việc thấu cảm người dùng và thiết kế giải pháp.

| **Chương** | **Sản phẩm cần giao nộp** |
| --- | --- |
| **Chương 1: Tổng quan** | 1. Stakeholder Map, Stakeholder Description Table 
2. Problem List + Problem Statement 
3. Product Vision - Value Proposition North Star / OKR 
4. Product Hypotheses + Metrics |
| **Chương 3: Thấu cảm** | 1. Interview Guide 
2. Interview Notes / Observation Data 
3. Empathy Map 
4. User Persona 
5. Customer Journey Map 
6. Affinity Map 
7. Pain Points + Unmet Needs 
8. POV Statement 
9. HMW Questions |
| **Chương 4: Ý tưởng & Prototype** | 1. Idea List (Brainstorming) 
2. Impact–Feasibility Matrix 
3. Selected Ideas + Justification 
4. Chatbot Prototype 
5. Conversation Flow / Use Scenarios 
6. Usability Testing Plan 
7. Usability Testing Report 
8. Issues + Recommendations |

---

### 🚩 Giai đoạn 2: Phát triển & Triển khai (Agile/Scrum)

Chuyển đổi ý tưởng thành kế hoạch thực hiện và xây dựng sản phẩm.

| **Chương** | **Sản phẩm cần giao nộp** |
| --- | --- |
| **Chương 2: Agile/Scrum** | 1. Scrum Team Structure 
2. Working Agreement 
3. Sprint Planning (Goal + Sprint Backlog) 
4. Daily Scrum Logs 
5. Sprint Review (kết quả + feedback) 
6. Retrospective (bài học + cải tiến) 
7. Initial Product Backlog (User Stories) |
| **Chương 5: Triển khai** | 1. Interview Guide 
2. Interview Notes / Observation Data 
3. Empathy Map 
4. User Persona 
5. Customer Journey Map 
6. Affinity Map 
7. Pain Points + Unmet Needs 
8. POV Statement 
9. HMW Questions
7. Product Roadmap (3 releases) 
8. Sprint Backlog + Sprint Goal 
9. Chatbot (code + doc + data + demo video + description + deployment address) |

---

### 🚩 Giai đoạn 3: Vận hành & Cải tiến (Ops)

Trực quan hóa luồng công việc và đo lường hiệu quả.

| **Chương** | **Sản phẩm cần giao nộp** |
| --- | --- |
| **Chương 6: Vận hành** | 1. Workflow Diagram 
2. Kanban Board 
3. WIP Limits + Explicit Policies 
4. Task Dataset (giả lập) 
5. Flow Metrics (Lead time, Cycle time, Throughput) 
6. Flow Metrics Report 
7. Bottleneck Analysis 
8. Improvement Plan |

---

### 📅 Các mốc thời gian quan trọng cần lưu ý:

- **23h59 ngày 24.4:** Hạn cuối giao nộp toàn bộ sản phẩm lên Google Drive.
- **8h00 – 9h00 sáng 25.4:** Các nhóm vào channel riêng, demo và quay video recording giới thiệu kết quả.
- **18h30 – 21h30 tối 25.4:** Bảo vệ trực tiếp với giảng viên (cô Giang/thầy Hóa).

### ⚠️ Cảnh báo đỏ từ giảng viên:

- **Tuyệt đối không lạm dụng AI:** Nhóm nào dùng AI tạo kết quả mà không kiểm chứng hoặc không kiểm soát được nội dung sẽ bị **0 điểm** và không được bảo vệ lại.
- **Đánh giá chéo:** Bạn sẽ đóng vai trò là user, tester hoặc expert để đánh giá nhóm khác, vì vậy cần nắm chắc kiến thức của nhóm mình để đặt câu hỏi ngược lại.

---