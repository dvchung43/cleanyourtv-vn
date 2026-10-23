# Chế Độ Trẻ Em (Kids Mode & Time-Based Lock Screen)

## Problem Statement
Làm thế nào để cha mẹ quản lý thời gian xem TV của con cái một cách tự động, văn minh và không thể bị trẻ qua mặt bằng cách nhìn trộm mật khẩu hay bấm loạn remote?

## Recommended Direction: "Curtain Lock & Dynamic PIN"
Xây dựng tính năng Kids Mode tích hợp ngay trên Thanh điều hướng / Cài đặt của CleanYourTV:
1. **Bật/Tắt trực quan trên Home:** Biểu tượng hoặc nút chuyển đổi Chế độ Trẻ Em trên Top Bar / Menu Home.
2. **Cài đặt thời gian xem:** Cho phép chọn nhanh thời lượng xem (15, 30, 45, 60, 90 phút hoặc tùy chỉnh).
3. **Màn hình khóa màn che (Curtain Mode):**
   - Hết giờ, TV tự động thu hồi ứng dụng về màn hình khóa đen tĩnh (`#000000`), tắt tiếng (mute audio), vô hiệu hóa phản hồi phím thông thường để trẻ nghĩ TV đã tắt.
4. **Mở khóa 2 lớp (Bí mật + PIN Động):**
   - **Bước 1 (Cử chỉ kích hoạt):** Nhấn **5 lần liên tiếp** nút chỉ định (nút Nguồn hoặc phím thay thế như Mute/OK trong vòng 3 giây) để đánh thức khung nhập PIN ảo.
   - **Bước 2 (Mật khẩu động):** Nhập mã PIN 6 chữ số theo định dạng **`ddMMHH`** (Ngày/Tháng/Giờ 24h).
     - *Ví dụ:* Lúc 16 giờ ngày 23 tháng 10, mật khẩu là `231016`.
     - Phụ huynh chỉ cần nhìn đồng hồ là biết mật khẩu, trẻ em nhìn trộm lúc trước cũng vô hiệu ở giờ tiếp theo.

## Key Assumptions to Validate
- [ ] Service nền hoặc AlarmManager đánh thức Launcher và hiển thị màn hình khóa đè lên ứng dụng phát video (YouTube, Netflix...) khi hết giờ.
- [ ] Quản lý trạng thái khóa bền vững qua `SharedPreferences`: Nếu trẻ rút nguồn điện cắm lại thì TV khi khởi động xong vẫn lập tức quay lại màn hình khóa nếu chưa hết thời gian phạt/chưa được mở khóa.
- [ ] Giữ dung lượng mã nguồn cực nhẹ, đảm bảo APK build ra không vi phạm kích thước tối đa 480 KB theo quy chuẩn repo.

## MVP Scope
- **Cấu hình & Bật/Tắt:** Nút gạt bật Chế độ Trẻ em trên giao diện Launcher Home / Quick Settings.
- **Bộ đếm thời gian (Countdown Timer):** Theo dõi thời gian xem thực tế và kích hoạt màn hình khóa khi về 0.
- **Curtain Lock Screen:** View toàn màn hình đen, mute volume, chặn input thông thường.
- **Phát hiện nhấn 5 lần:** Bộ đếm click trong khoảng thời gian cửa sổ ngắn (3s) để mở hộp thoại nhập PIN.
- **Bàn phím số ảo (Virtual PIN Pad):** Lưới 0-9 điều khiển trực tiếp bằng cụm D-Pad và phím Center của remote TV.
- **Xác thực PIN động ddMMHH:** Tính toán `ddMMHH` theo thời gian hệ thống thực tế tại thời điểm nhập.

## Not Doing (Những thứ KHÔNG làm để giữ launcher gọn nhẹ)
- **Không kiểm duyệt nội dung bên trong video:** Không nhúng module phân tích luồng phát hay can thiệp ứng dụng bên thứ ba.
- **Không yêu cầu mạng hay tài khoản:** Hoạt động 100% offline, bảo mật tuyệt đối và tôn trọng quyền riêng tư.
- **Không can thiệp lệnh tắt nguồn cứng nguy hiểm:** Tránh rủi ro ngắt nguồn đột ngột hay mất quyền điều khiển qua ADB.

## Open Questions / Triển khai tiếp theo
- Nút bấm 5 lần: Mặc định gán vào nút Nguồn (`KEYCODE_POWER`) và hỗ trợ dự phòng nút `Mute` / `Home` đối với một số remote TV có phần cứng ngắt tín hiệu khi màn hình đen.
- Hiển thị thông báo đếm ngược: Có cần cảnh báo nhẹ (Toast hoặc Banner) trước khi khóa 1 phút để trẻ chuẩn bị tâm lý không?
