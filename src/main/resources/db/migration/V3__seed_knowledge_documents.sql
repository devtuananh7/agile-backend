-- Seed sample knowledge documents for RAG testing
-- Note: embedding vectors are NOT included here.
-- They will be generated via the EmbeddingService when the application runs
-- or via a separate data loading script.

INSERT INTO knowledge_documents (title, content, category, tags, is_active, created_at, updated_at) VALUES
(
    'Hướng dẫn xử trí đau đầu',
    'Đau đầu là triệu chứng phổ biến. Phân loại:
- Đau đầu nguyên phát: Migraine (đau nửa đầu, nhói, kèm buồn nôn), đau đầu căng thẳng (đau ép hai bên), đau đầu cụm (đau dữ dội quanh mắt).
- Đau đầu thứ phát: Do nhiễm trùng (viêm xoang, viêm màng não), chấn thương, tăng huyết áp, u não.

Dấu hiệu cảnh báo cần đi cấp cứu: đau đầu đột ngột dữ dội nhất từ trước đến nay (thunderclap headache), kèm sốt cao + cứng cổ, sau chấn thương đầu, kèm thay đổi ý thức, yếu liệt chi.

Xử trí ban đầu: Nghỉ ngơi, uống đủ nước, Paracetamol 500mg-1g (tối đa 4g/ngày), tránh ánh sáng mạnh nếu migraine. Nếu không đỡ sau 3 ngày: đi khám.',
    'symptoms',
    ARRAY['đau đầu', 'migraine', 'headache', 'thần kinh'],
    true,
    NOW(),
    NOW()
),
(
    'Hướng dẫn xử trí sốt',
    'Sốt là phản ứng cơ thể khi nhiệt độ > 37.5°C (đo nách) hoặc > 38°C (đo tai/miệng).

Phân loại mức độ:
- Sốt nhẹ: 37.5-38.5°C → Theo dõi, uống nhiều nước, chườm ấm
- Sốt vừa: 38.5-39.5°C → Paracetamol/Ibuprofen, theo dõi sát
- Sốt cao: > 39.5°C → Cần đi khám, nguy cơ co giật (trẻ em)
- Sốt kéo dài > 3 ngày → Bắt buộc đi khám

Thuốc hạ sốt an toàn: Paracetamol (10-15mg/kg/lần, cách 4-6h). Ibuprofen (thay thế, 5-10mg/kg/lần).
Lưu ý: KHÔNG dùng Aspirin cho trẻ dưới 18 tuổi (nguy cơ hội chứng Reye).

Dấu hiệu cần cấp cứu: Sốt > 41°C, co giật, nổi ban xuất huyết, lơ mơ, khó thở.',
    'symptoms',
    ARRAY['sốt', 'nhiệt độ', 'fever', 'hạ sốt'],
    true,
    NOW(),
    NOW()
),
(
    'Thông tin thuốc Paracetamol',
    'Paracetamol (Acetaminophen) - Thuốc giảm đau, hạ sốt phổ biến nhất.

Liều dùng người lớn: 500mg-1g/lần, cách 4-6 giờ, tối đa 4g/ngày.
Liều dùng trẻ em: 10-15mg/kg/lần, cách 4-6 giờ, tối đa 5 lần/ngày.

Chống chỉ định: Suy gan nặng, thiếu men G6PD, quá mẫn.
Tác dụng phụ: Hiếm gặp ở liều thường. Quá liều gây tổn thương gan nghiêm trọng.
Tương tác: Rượu (tăng độc tính gan), Warfarin (tăng tác dụng chống đông).

An toàn cho thai phụ: Có thể dùng trong thai kỳ (category B).
Lưu ý: Nhiều thuốc cảm cúm đã chứa Paracetamol - cẩn thận liều tích lũy.',
    'drugs',
    ARRAY['paracetamol', 'acetaminophen', 'giảm đau', 'hạ sốt'],
    true,
    NOW(),
    NOW()
),
(
    'Hướng dẫn xử trí đau bụng',
    'Đau bụng - phân vùng và nguyên nhân phổ biến:

Thượng vị (trên rốn): Viêm dạ dày, loét dạ dày-tá tràng, viêm tụy, trào ngược.
Hạ sườn phải: Viêm túi mật, sỏi mật, viêm gan, áp xe gan.
Hạ sườn trái: Viêm lách, viêm đại tràng, sỏi thận trái.
Quanh rốn: Viêm ruột thừa sớm, tắc ruột, viêm ruột.
Hố chậu phải: Viêm ruột thừa, buồng trứng (nữ), thoát vị bẹn.
Hố chậu trái: Viêm túi thừa, buồng trứng (nữ), viêm đại tràng.
Hạ vị (dưới rốn): Viêm bàng quang, sỏi niệu quản, phụ khoa.

Dấu hiệu cần cấp cứu: Đau dữ dội đột ngột, bụng cứng như gỗ, nôn ra máu/đi ngoài phân đen, sốt cao + đau bụng, ngất xỉu.',
    'symptoms',
    ARRAY['đau bụng', 'tiêu hóa', 'dạ dày', 'ruột thừa'],
    true,
    NOW(),
    NOW()
);
