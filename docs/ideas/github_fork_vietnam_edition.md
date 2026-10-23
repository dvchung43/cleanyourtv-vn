# CleanYourTV - Vietnam Edition (GitHub Public Release Plan)

## Problem Statement
> **How Might We** biến dự án launcher CleanYourTV đã được Việt hóa và tối ưu cho gia đình thành một bản Fork mã nguồn mở độc lập (**CleanYourTV - Vietnam Edition**) trên GitHub, đảm bảo sạch 100% dữ liệu riêng tư, giữ trọn vẹn các tính năng cao cấp (Lịch Âm, Khóa trẻ em, Giao diện Kính mờ), và bất kỳ người dùng TV nào tại Việt Nam cũng có thể tải về dùng ngay?

---

## Recommended Direction: "Vietnam Community Edition (All-in-One Clean Fork)"
Thay vì chỉ lưu trữ thô sơ hoặc gửi PR lẻ tẻ về repo gốc (vốn có các rào cản gắt gao về việc tắt tính năng theo mặc định), ta sẽ thiết lập **một Repository GitHub độc lập (Fork)** với định vị:
- **Tên dự án đề xuất:** `CleanYourTV - Vietnam Edition` (hoặc `TVHome VN`).
- **Mục tiêu:** Mang lại trải nghiệm TV siêu mượt, nhẹ (<500 KB), không quảng cáo, tối ưu 100% cho thói quen xem TV của các gia đình Việt Nam.
- **Tính năng nổi bật:**
  1. **Lịch Âm Thiên Văn (Hồ Ngọc Đức):** Tự động hiển thị song song ngày âm/dương, chuẩn giờ VN (UTC+7).
  2. **Khóa trẻ em thông minh (Kids Lock):** Khóa app bằng mã PIN động `ddMMHH` hoặc mật khẩu tùy chọn, phím tắt mở nhanh bằng điều khiển.
  3. **Thanh Dock ứng dụng Việt:** Tự động ưu tiên YouTube, SmartTube, VTV Go, VieON, FPT Play, Zing MP3.
  4. **Giao diện Glass tuyệt đẹp:** Nền kính mờ hiện đại, hiệu ứng ánh sáng động ambient glow.

---

## Danh Sách Dọn Dẹp Chi Tiết (Sanitization & Cleanup Checklist)

### 1. Dữ liệu cá nhân cần loại bỏ hoàn toàn (Privacy & Hard Rules)
- [x] **Tên riêng cá nhân:** Xóa `"Min Mỡ"` khỏi [themes/glass/config.json](file:///C:/Users/chungdinh/Desktop/CLTV/themes/glass/config.json), chuyển thành lời chào chuẩn của hệ thống (`XIN CHÀO`, `BUỔI TỐI`).
- [x] **Hình ảnh & Ảnh chụp riêng tư:** Thư mục `shots/` đã nằm trong `.gitignore`, đảm bảo không `git add` bất kỳ ảnh chụp màn hình phòng khách / TV thật nào.
- [x] **Địa chỉ IP & Cổng mạng:** Đảm bảo tài liệu hướng dẫn chỉ dùng IP giả định (`192.168.1.50` hoặc `<tv-ip>`).
- [x] **File Backup & Thiết bị:** Thư mục `backup/` và khoá `tools/adbkey/` đã nằm trong `.gitignore`, không đưa lên repo.

### 2. Chuẩn hoá cấu hình mã nguồn mở
- [x] **File cấu hình mặc định ([config.xml](file:///C:/Users/chungdinh/Desktop/CLTV/launcher/app/src/main/res/values/config.xml)):**
  - Giữ Lịch Âm `lunar_date` = `true`, `lunar_utc_offset` = `7`.
  - Giữ danh sách `dock_apps` mặc định: YouTube, SmartTube, VTV Go, VieON, Zing MP3, Netflix.
  - `greeting_name` = `""` (lời chào trung tính).
- [x] **Chủ đề Kính mờ ([themes/glass/config.json](file:///C:/Users/chungdinh/Desktop/CLTV/themes/glass/config.json)):**
  - Xóa dòng `"greeting_name": "Min Mỡ"`.
  - Giữ nguyên cấu hình màu sắc, dock glass, ambient glow và hình nền 4K sạch đẹp.

### 3. Cấu trúc Commit chuẩn mực (Semantic & Clean Commits)
Thay vì một commit lộn xộn, mã nguồn sẽ được chia thành các commit độc lập, rõ ràng:
1. `feat(lunar): add Vietnamese lunar calendar with astronomical algorithm` (Chứa `Lunar.java`, `LunarTest.java`, layout và tài nguyên).
2. `feat(kids-mode): implement kids lock and dynamic PIN protection` (Chứa `KidsLockManager.java`, `KidsLockManagerTest.java`, icon khóa).
3. `theme(glass): polish modern glass theme and default Vietnam dock apps` (Chứa `themes/glass/`, hình nền chuẩn và cấu hình dock).
4. `docs: update Vietnamese documentation and community installation guide` (Cập nhật README tiếng Việt).

---

## MVP Scope (Phạm Vi Bản Phát Hành Công Khai V1.0)
- **Codebase:** Mã nguồn Java thuần túy, Android SDK 34, không phụ thuộc thư viện bên ngoài nặng nề.
- **Dung lượng APK:** Dưới 485 KB (siêu nhẹ, tải trong 1 giây).
- **Hỗ trợ thiết bị:** Tất cả Android TV / Google TV từ Android 8.0 (API 26) đến Android 14.
- **Tài liệu đính kèm:** `README.md` tiếng Việt có sẵn 4 ảnh hướng dẫn (bật nhà phát triển, cấp quyền, cài qua USB, cài qua Downloader).
- **GitHub Release:** Đính kèm sẵn file `CleanYourTV_Vietnam_Edition.apk` để người dùng phổ thông tải về không cần build.

---

## Not Doing (Những Thứ Quyết Định KHÔNG Làm & Lý Do)
- **Không nhúng quyền INTERNET vào Launcher:** Giữ nguyên triết lý bảo mật của CleanYourTV: launcher tuyệt đối không kết nối mạng, không gửi dữ liệu người dùng đi bất cứ đâu.
- **Không tự động mở cổng hay root TV:** Giữ nguyên tắc an toàn, cài đặt như một ứng dụng bình thường (`apk install` hoặc qua USB).
- **Không commit file nhị phân APK vào git tree:** File `.apk` chỉ được đính kèm ở mục **GitHub Releases**, giúp repo git luôn nhẹ (<15 MB).

---

## Các Bước Triển Khai Tiếp Theo
1. **Dọn file cấu hình:** Xóa chữ `Min Mỡ` trong `themes/glass/config.json`.
2. **Kiểm tra build & tests:** Chạy `./gradlew testReleaseUnitTest assembleRelease` xác nhận pass 100%.
3. **Tạo GitHub Repo:** Hướng dẫn lệnh khởi tạo remote (`git remote add origin https://github.com/<username>/cleanyourtv-vn.git`) và đẩy code lên an toàn.
