# Hướng dẫn Thu thập & Import Dữ liệu RAG — CareTalk

> Tài liệu hướng dẫn chi tiết cách chuẩn bị, định dạng, xử lý và import dữ liệu y tế vào hệ thống RAG của CareTalk Backend.

---

## 1. Tổng quan hệ thống

Khi user gửi tin nhắn, hệ thống sẽ:
1. Embed câu hỏi user thành vector 1536 chiều (OpenAI `text-embedding-3-small`)
2. Tìm top-3 documents có cosine similarity > 0.75 trong PostgreSQL (pgvector)
3. Inject nội dung documents vào prompt dưới dạng `[MEDICAL KNOWLEDGE]`
4. GPT-4o trả lời dựa trên knowledge đó

**Hệ quả quan trọng:** Chất lượng câu trả lời phụ thuộc trực tiếp vào chất lượng documents bạn import.

---

## 2. Thu thập — Cần những gì?

### 2.1 Danh mục cần thu thập

| Category | Mô tả | Số lượng gợi ý | Ví dụ |
|---|---|---|---|
| `symptoms` | Triệu chứng & hướng xử trí | 30-40 docs | Đau đầu, sốt, ho, đau ngực, khó thở, tiêu chảy |
| `drugs` | Thông tin thuốc phổ biến | 20-30 docs | Paracetamol, Ibuprofen, Amoxicillin, Omeprazole |
| `diseases` | Bệnh lý thường gặp | 20-30 docs | Tiểu đường, tăng huyết áp, viêm phổi, hen suyễn |
| `first-aid` | Hướng dẫn sơ cứu | 10-15 docs | Bỏng, gãy xương, ngộ độc, đuối nước |
| `faq` | Câu hỏi thường gặp | 15-20 docs | "Khi nào cần cấp cứu?", "Trẻ bị sốt cao" |

### 2.2 Nguồn dữ liệu đáng tin cậy

- **Bộ Y tế Việt Nam** — Phác đồ điều trị, hướng dẫn chẩn đoán
- **WHO** — Guidelines quốc tế (dịch sang tiếng Việt)
- **BNF / MIMS** — Thông tin thuốc (tóm tắt, không copy nguyên văn)
- **Sách giáo khoa y khoa** — Triệu chứng học, nội khoa cơ bản
- **Bệnh viện lớn** — Tài liệu patient education (Bạch Mai, Chợ Rẫy)

> ⚠️ **KHÔNG dùng**: Wikipedia y tế, blog cá nhân, bài viết không có nguồn.

---

## 3. Định dạng dữ liệu — JSON chuẩn

### 3.1 Cấu trúc 1 document

```json
{
  "title": "Tiêu đề ngắn gọn, mô tả rõ nội dung",
  "content": "Nội dung chi tiết dạng plain text...",
  "category": "symptoms",
  "tags": ["keyword1", "keyword2", "keyword3"]
}
```

### 3.2 Quy tắc cho từng field

**`title`** — Tiêu đề duy nhất (hệ thống check trùng case-insensitive)
- ✅ `"Hướng dẫn xử trí ho kéo dài ở người lớn"`
- ✅ `"Thông tin thuốc Ibuprofen — chỉ định và chống chỉ định"`
- ❌ `"Ho"` (quá ngắn, mơ hồ)
- ❌ `"Tài liệu y tế số 5"` (không mô tả nội dung)

> **Tại sao quan trọng:** Title được hiển thị trực tiếp cho LLM dưới dạng `"Tài liệu 1: <title>"`. Title rõ ràng giúp LLM hiểu context nhanh hơn.

**`content`** — Nội dung chính (xem Section 4 về viết content)

**`category`** — Phân loại, dùng giá trị cố định:
- `symptoms` — Triệu chứng
- `drugs` — Thuốc
- `diseases` — Bệnh lý
- `first-aid` — Sơ cứu
- `faq` — Câu hỏi thường gặp

**`tags`** — Mảng keywords cho quản lý, 3-6 tags mỗi doc:
- Bao gồm cả tiếng Việt + tiếng Anh
- Ví dụ: `["đau đầu", "migraine", "headache", "nhức đầu"]`

### 3.3 File JSON mẫu hoàn chỉnh

Lưu thành file `knowledge_symptoms.json`:

```json
[
  {
    "title": "Hướng dẫn xử trí ho kéo dài",
    "content": "Ho kéo dài là tình trạng ho liên tục trên 3 tuần.\n\nNguyên nhân phổ biến:\n- Viêm mũi dị ứng, chảy dịch mũi sau (post-nasal drip)\n- Hen suyễn (ho khan, khò khè, nặng về đêm)\n- Trào ngược dạ dày - thực quản (GERD)\n- Nhiễm trùng đường hô hấp kéo dài\n- Dùng thuốc ACE inhibitor (enalapril, captopril)\n\nDấu hiệu cần đi khám ngay:\n- Ho ra máu\n- Sụt cân không rõ nguyên nhân\n- Khó thở tăng dần\n- Sốt kéo dài > 2 tuần\n- Tiền sử hút thuốc lá\n\nXử trí ban đầu:\n- Uống đủ nước ấm, mật ong chanh (người lớn)\n- Tránh khói bụi, không khí lạnh\n- Nếu nghi ngờ dị ứng: thử antihistamine (cetirizine 10mg/ngày)\n- Nếu không đỡ sau 1 tuần tự xử trí: đi khám chuyên khoa hô hấp",
    "category": "symptoms",
    "tags": ["ho", "ho kéo dài", "cough", "đường hô hấp", "chronic cough"]
  },
  {
    "title": "Thông tin thuốc Ibuprofen",
    "content": "Ibuprofen — Thuốc giảm đau, hạ sốt, chống viêm (nhóm NSAID).\n\nChỉ định: Đau đầu, đau răng, đau cơ xương khớp, đau bụng kinh, sốt.\n\nLiều dùng:\n- Người lớn: 200-400mg/lần, cách 4-6 giờ, tối đa 1200mg/ngày (OTC) hoặc 3200mg/ngày (theo chỉ định BS).\n- Trẻ em > 6 tháng: 5-10mg/kg/lần, cách 6-8 giờ.\n\nChống chỉ định:\n- Loét dạ dày đang tiến triển\n- Suy thận nặng (GFR < 30)\n- Tam cá nguyệt thứ 3 thai kỳ\n- Dị ứng aspirin hoặc NSAID khác\n- Xuất huyết tiêu hóa\n\nTương tác thuốc quan trọng:\n- Warfarin: tăng nguy cơ chảy máu\n- Methotrexate: tăng độc tính\n- Lithium: tăng nồng độ lithium máu\n\nLưu ý: Uống sau ăn. Không dùng đồng thời 2 loại NSAID.",
    "category": "drugs",
    "tags": ["ibuprofen", "NSAID", "giảm đau", "chống viêm", "hạ sốt"]
  }
]
```

---

## 4. Viết Content — Nguyên tắc vàng

### 4.1 Độ dài tối ưu

```
     Quá ngắn              Sweet Spot             Quá dài
    < 100 từ             200 — 600 từ            > 800 từ
  ┌──────────┐        ┌──────────────┐        ┌──────────────┐
  │ Embedding │        │  Embedding   │        │  Tự động     │
  │ thiếu     │        │  tập trung   │        │  CHUNKED     │
  │ context   │        │  chất lượng  │        │  thành nhiều │
  │           │        │  cao nhất    │        │  records     │
  └──────────┘        └──────────────┘        └──────────────┘
```

- **200-600 từ** (~150-450 tokens): Lý tưởng, embedding capture toàn bộ ý nghĩa
- **> ~1067 từ** (~800 tokens): `TextChunker` tự chia thành nhiều records với overlap 100 tokens (~133 từ)
- **< 100 từ**: Embedding quá "loãng", dễ match nhầm

### 4.2 Cấu trúc content theo category

**Symptoms (triệu chứng):**
```
[Tên triệu chứng] — mô tả ngắn.

Nguyên nhân phổ biến:
- Nguyên nhân 1
- Nguyên nhân 2

Dấu hiệu cảnh báo cần cấp cứu:
- Dấu hiệu 1
- Dấu hiệu 2

Xử trí ban đầu:
- Bước 1
- Bước 2

Khi nào cần đi khám:
- Điều kiện 1
```

**Drugs (thuốc):**
```
[Tên thuốc] — Nhóm thuốc.

Chỉ định: ...
Liều dùng:
- Người lớn: ...
- Trẻ em: ...

Chống chỉ định:
- ...

Tác dụng phụ thường gặp:
- ...

Lưu ý quan trọng: ...
```

**FAQ:**
```
Câu hỏi: [Câu hỏi thường gặp]

Trả lời:
[Nội dung trả lời chi tiết, dễ hiểu]

Lưu ý:
- Điểm quan trọng 1
- Điểm quan trọng 2
```

### 4.3 Nguyên tắc viết cho embedding tốt

| Nên | Không nên |
|---|---|
| Viết thuần tiếng Việt (hoặc thuần tiếng Anh) | Trộn lẫn 2 ngôn ngữ trong 1 đoạn |
| Dùng bullet points có cấu trúc | Viết liền 1 đoạn văn dài |
| Đi thẳng vào nội dung y tế | Mở đầu kiểu "Xin chào, hôm nay..." |
| Sử dụng thuật ngữ y tế chuẩn | Dùng slang hoặc viết tắt không chuẩn |
| Tách paragraph bằng `\n\n` | Nhồi nhét mọi thứ vào 1 paragraph |
| 1 document = 1 chủ đề | 1 document nói về 5 bệnh khác nhau |

### 4.4 Ví dụ TRÁNH

```
❌ BAD: "paracetamol tốt lắm uống đi bạn ơi 500mg cách 4h"
   → Quá ngắn, không cấu trúc, thiếu context

❌ BAD: "Đau đầu. Sốt. Ho. Đau bụng. Tất cả đều cần đi khám."
   → 1 document nói về 4 triệu chứng → embedding loãng

✅ GOOD: Xem file mẫu ở Section 3.3
```

---

## 5. Xử lý trước khi import

### 5.1 Checklist trước import

- [ ] Mỗi document có title DUY NHẤT (không trùng với docs đã có)
- [ ] Content dài 200-600 từ (docs dài hơn sẽ tự bị chunk)
- [ ] Category là 1 trong: `symptoms`, `drugs`, `diseases`, `first-aid`, `faq`
- [ ] Tags có 3-6 keywords (cả Việt + Anh)
- [ ] Không có HTML tags trong content
- [ ] Không có ký tự đặc biệt lạ (emoji OK, nhưng tránh lạm dụng)
- [ ] JSON hợp lệ (validate bằng tool online)

### 5.2 Tổ chức file

```
data/
├── knowledge_symptoms.json      ← 30-40 docs về triệu chứng
├── knowledge_drugs.json         ← 20-30 docs về thuốc
├── knowledge_diseases.json      ← 20-30 docs về bệnh lý
├── knowledge_first_aid.json     ← 10-15 docs về sơ cứu
└── knowledge_faq.json           ← 15-20 docs FAQ
```

### 5.3 Validate JSON

Dùng `jq` trên terminal:
```bash
# Kiểm tra JSON hợp lệ
cat knowledge_symptoms.json | jq '.' > /dev/null && echo "OK" || echo "INVALID"

# Đếm số documents
cat knowledge_symptoms.json | jq 'length'

# Kiểm tra title trùng nhau trong file
cat knowledge_symptoms.json | jq '[.[].title] | group_by(.) | map(select(length>1)) | .[][0]'

# Kiểm tra docs thiếu field
cat knowledge_symptoms.json | jq '[.[] | select(.title == null or .content == null)] | length'
```

---

## 6. Import vào hệ thống

### 6.1 Yêu cầu trước khi import

1. **App đang chạy** — `./gradlew bootRun` hoặc deploy trên server
2. **OPENAI_API_KEY** — Đã set trong environment (cần cho embedding)
3. **Token admin** — Firebase auth token của user có `ROLE_ADMIN`

### 6.2 Import từng file (khuyến nghị)

```bash
# Lấy admin token (từ Firebase)
TOKEN="eyJhbGci..."

# Import từng batch
curl -X POST http://localhost:8080/api/v1/admin/knowledge/bulk \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d @data/knowledge_symptoms.json

# Response mẫu:
# { "imported": 35, "failed": 2, "errors": [
#   {"title": "Đau đầu...", "reason": "Duplicate title"}
# ]}
```

**Import theo thứ tự:**
1. `knowledge_symptoms.json` — Quan trọng nhất (user hỏi triệu chứng nhiều)
2. `knowledge_drugs.json` — Thường liên quan đến triệu chứng
3. `knowledge_diseases.json`
4. `knowledge_first_aid.json`
5. `knowledge_faq.json`

### 6.3 Import từng document (cho test)

```bash
curl -X POST http://localhost:8080/api/v1/admin/knowledge \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test: Hướng dẫn xử trí ho kéo dài",
    "content": "Ho kéo dài > 3 tuần có thể do...",
    "category": "symptoms",
    "tags": ["ho", "cough"]
  }'
```

### 6.4 Kiểm tra sau import

```bash
# Đếm tổng documents
curl -s http://localhost:8080/api/v1/admin/knowledge?size=1 \
  -H "Authorization: Bearer $TOKEN" | jq '.totalElements'

# Liệt kê theo category
curl -s "http://localhost:8080/api/v1/admin/knowledge?category=symptoms&size=100" \
  -H "Authorization: Bearer $TOKEN" | jq '.content[].title'

# Test semantic search
curl -X POST http://localhost:8080/api/v1/admin/knowledge/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "tôi bị ho 2 tuần không bớt", "threshold": 0.65}'
```

---

## 7. Kiểm tra chất lượng (Verification)

### 7.1 Test cases cần thử

Sau khi import xong, test bằng endpoint `/search` với các câu hỏi thực tế:

| Câu hỏi test | Document mong đợi | Threshold thử |
|---|---|---|
| "tôi bị đau đầu 3 ngày" | Hướng dẫn xử trí đau đầu | 0.65, 0.70, 0.75 |
| "uống paracetamol bao nhiêu mg" | Thông tin thuốc Paracetamol | 0.70 |
| "trẻ 2 tuổi bị sốt cao" | Xử trí sốt + Paracetamol (trẻ em) | 0.65 |
| "khi nào cần gọi cấp cứu" | FAQ cấp cứu + docs có dấu hiệu cảnh báo | 0.60 |
| "tôi bị đau bụng bên phải" | Xử trí đau bụng (hạ sườn phải) | 0.70 |

### 7.2 Cách đánh giá

```bash
# Search với threshold thấp để xem tất cả kết quả tiềm năng
curl -X POST http://localhost:8080/api/v1/admin/knowledge/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query": "tôi bị đau đầu", "threshold": 0.5, "topK": 10}'
```

- **Precision:** Trong kết quả trả về, bao nhiêu % thực sự liên quan? Target > 80%
- **Recall:** Document đúng có nằm trong kết quả không? Target > 90%
- **Nếu miss:** Hạ threshold hoặc cải thiện content document

### 7.3 Test end-to-end qua chat

Cuối cùng, test thực tế bằng cách chat qua app:
1. Gửi câu hỏi y tế → xem log `"RAG retrieved N documents for context"`
2. Kiểm tra câu trả lời có chứa thông tin từ knowledge base không
3. Nếu không có RAG context → threshold quá cao hoặc content không match

---

## 8. Bảo trì & Cập nhật

### 8.1 Thêm document mới

Dùng `POST /api/v1/admin/knowledge` — hệ thống tự chunk + embed.

### 8.2 Sửa document

```bash
# Update — tự re-embed nếu content thay đổi
curl -X PUT http://localhost:8080/api/v1/admin/knowledge/42 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Hướng dẫn xử trí ho kéo dài (cập nhật 2026)",
    "content": "Nội dung mới...",
    "category": "symptoms",
    "tags": ["ho", "cough"]
  }'
```

### 8.3 Xóa document

```bash
# Soft delete (vẫn giữ trong DB, chỉ ẩn khỏi search)
curl -X DELETE http://localhost:8080/api/v1/admin/knowledge/42 \
  -H "Authorization: Bearer $TOKEN"
```

### 8.4 Khi nào cần re-import toàn bộ?

- Đổi embedding model (ví dụ từ `text-embedding-3-small` sang `text-embedding-3-large`)
- Thay đổi cấu trúc content toàn diện
- Phát hiện nhiều docs chất lượng kém cần viết lại

---

## 9. Lưu ý quan trọng

### ⚠️ 4 seed documents hiện tại KHÔNG có embedding

4 docs trong `init_schema.sql` được insert bằng SQL thuần → cột `embedding` = NULL → **bị bỏ qua khi search**. Cần re-import chúng qua API:

```bash
# Re-import 4 seed docs qua API (sẽ có embedding)
# Trước đó cần xóa hoặc đổi title vì dedup check sẽ chặn
```

### ⚠️ Chi phí OpenAI

- `text-embedding-3-small`: ~$0.02 / 1M tokens
- 100 documents × 400 từ/doc = ~30K tokens = **~$0.001** (rất rẻ)
- Mỗi query của user cũng embed 1 lần (không đáng kể)

### ⚠️ Chunking tạo thêm records

Document 1000 từ → TextChunker chia thành 2 records. Nếu import 100 docs dài → có thể tạo 150-200 records trong DB. Điều này bình thường và tốt cho search quality.

---

*Generated: 2026-04-22 | Project: agile-chatbot-backend (CareTalk)*
