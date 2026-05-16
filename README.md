# 🍀 Clover Chatty — RealtimeChatApp Android

Ứng dụng nhắn tin thời gian thực xây dựng bằng **Kotlin** và **Jetpack Compose**, sử dụng kiến trúc **Clean Architecture** kết hợp **MVVM**.

---

## 📸 Tổng Quan

Clover Chatty là ứng dụng chat Android cho phép người dùng nhắn tin trực tiếp theo thời gian thực thông qua WebSocket (Socket.IO). Ứng dụng hỗ trợ cả nhắn tin 1-1 và nhóm, với khả năng lưu trữ cục bộ để hoạt động ổn định kể cả khi mất mạng tạm thời.

---

## 📸 Ảnh chụp màn hình

<p align="center">
  <img src="https://github.com/user-attachments/assets/a7f0d2f8-b014-475b-ad73-103c73cb01a2" width="19%"/>
  <img src="https://github.com/user-attachments/assets/1beaf1ed-4dc1-42a7-aaf8-65f001a5903e" width="19%"/>
  <img src="https://github.com/user-attachments/assets/4345d5f5-c46e-470d-a32b-58fe039fe56b" width="19%"/>
  <img src="https://github.com/user-attachments/assets/2af05643-96e1-4c5a-be5d-13ec8d172fa3" width="19%"/>
  <img src="https://github.com/user-attachments/assets/490bad01-25a1-4b5b-9103-f7978f748377" width="19%"/>
</p>

---

## 🏗️ Kiến Trúc

Dự án tuân theo **Clean Architecture** với 3 tầng rõ ràng:

```
app/
├── data/               # Tầng dữ liệu
│   ├── local/          # Room Database, DataStore, DAO
│   ├── remote/         # Retrofit API, Socket.IO, DTO
│   └── repository/     # Implementations của repository
├── domain/             # Tầng nghiệp vụ
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   ├── usecase/        # Use cases
│   ├── validation/     # Business validators
│   └── exception/      # Custom exceptions
├── ui/                 # Tầng giao diện
│   ├── components/     # Composable components tái sử dụng
│   ├── navigation/     # Navigation graph
│   ├── screens/        # Màn hình và ViewModel
│   └── theme/          # Material 3 theme, màu sắc, font
└── di/                 # Dependency Injection (Hilt)
```

---

## 🛠️ Công Nghệ Sử Dụng

| Thư viện / Công nghệ | Mục đích |
|---|---|
| **Kotlin** | Ngôn ngữ chính |
| **Jetpack Compose** | Xây dựng giao diện |
| **Hilt** | Dependency Injection |
| **Socket.IO Client** | Nhắn tin thời gian thực |
| **Retrofit + OkHttp** | REST API |
| **Room** | Local database |
| **DataStore Preferences** | Lưu token, ngôn ngữ, user hiện tại |
| **Coil** | Load ảnh |
| **Navigation Compose** | Điều hướng màn hình |
| **Timber** | Logging |
| **Gson** | JSON serialization |

---

## ✅ Tính Năng Đã Hoàn Thành

### 🔐 Xác Thực
- Đăng ký tài khoản với ảnh đại diện tùy chọn
- Đăng nhập bằng tên đăng nhập và mật khẩu
- Tự động đăng nhập bằng JWT token đã lưu
- Đăng xuất và xóa toàn bộ dữ liệu cục bộ

### 💬 Nhắn Tin 1-1
- Gửi và nhận tin nhắn theo thời gian thực qua Socket.IO
- Hiển thị danh sách cuộc trò chuyện với tin nhắn cuối
- Trạng thái đã đọc / chưa đọc (seen/unseen) với icon tick
- Badge thông báo số tin nhắn chưa đọc
- Hiệu ứng typing indicator (đang nhập...)
- Trạng thái online/offline của bạn bè theo thời gian thực
- Cache tin nhắn cục bộ với Room (hoạt động offline)

### 👥 Nhóm Chat (Cơ Bản)
- Xem danh sách nhóm đã tham gia
- Xem tin nhắn trong nhóm
- Hiển thị thông tin nhóm: tên, ảnh đại diện, số thành viên
- Phân biệt tin nhắn của mình và người khác trong nhóm
- Hiển thị avatar và tên người gửi trong nhóm

### 👤 Hồ Sơ & Cài Đặt
- Xem thông tin cá nhân (tên, username, email, ngày tham gia)
- Cập nhật họ tên và email
- Thay đổi ảnh đại diện (chọn từ thư viện + nén ảnh tự động)
- Đổi mật khẩu với xác nhận
- Dialog xác nhận trước khi thực hiện thao tác nhạy cảm

### 🌐 Đa Ngôn Ngữ
- Hỗ trợ Tiếng Việt và English
- Chuyển đổi ngôn ngữ trong runtime (không cần khởi động lại)
- Lưu lựa chọn ngôn ngữ vào DataStore

### 🏠 Giao Diện
- Splash screen
- Bottom Navigation Bar (Tin nhắn / Nhóm / Tài khoản / Thêm)
- Material 3 Design với màu chủ đề xanh lá (#5CB85C)
- Font Chewy cho logo ứng dụng
- Hỗ trợ Edge-to-Edge và bàn phím không che nội dung
- Hỗ trợ chuyển đổi giữa 2 chế độ sáng / tối

### 🔧 Kỹ Thuật
- Offline-first: ưu tiên cache cục bộ, fallback khi mất mạng
- Custom `safeApiCall` và `safeDbCall` wrapper xử lý lỗi thống nhất
- Custom `UiText` để hỗ trợ string resource lẫn dynamic string trong ViewModel
- `AuthInterceptor` tự động gắn JWT và xử lý token hết hạn
- `UserAdapter` (Gson) xử lý trường hợp API trả về userId dạng string hoặc object

---

## 🚀 Hướng Phát Triển Trong Tương Lai

### 📌 Ưu Tiên Cao

#### Nhắn Tin Nhóm Đầy Đủ
- Tích hợp Socket.IO để gửi/nhận tin nhắn nhóm theo thời gian thực (hiện tại chỉ load từ API)
- Typing indicator trong nhóm
- Trạng thái đã đọc / chưa đọc cho từng thành viên nhóm

#### Quản Lý Nhóm
- Tạo nhóm mới và thêm thành viên
- Chỉnh sửa tên nhóm, ảnh đại diện nhóm
- Phân quyền: Owner, Admin, Member
- Xóa thành viên, chuyển quyền Owner
- Rời nhóm

#### Sửa Lỗi Ngôn Ngữ
- Một số string còn hardcode tiếng Việt chưa được đưa vào `strings.xml`
- Đảm bảo toàn bộ UI phản hồi đúng khi chuyển ngôn ngữ (bao gồm các màn hình detail)
- Xử lý đúng locale cho định dạng ngày giờ

---

### 📌 Cải Thiện Hiệu Suất

- **Phân trang tin nhắn**: Hiện tại load cố định 30 tin nhắn — cần implement lazy loading khi cuộn lên trên
- **Tối ưu Room query**: Thêm index phù hợp, tránh load toàn bộ bảng
- **Tối ưu Coil**: Cấu hình cache size, placeholder thống nhất
- **Giảm recomposition**: Review các `State` chưa cần thiết trong Compose

---

### 📌 Tính Năng Mới Đề Xuất

#### Tìm Kiếm
- Màn hình tìm kiếm (`Screen.Search`) đã có trong navigation nhưng chưa implement
- Tìm kiếm bạn bè, nhóm, tin nhắn theo từ khóa

#### Thông Báo Đẩy (Push Notification)
- Tích hợp Firebase Cloud Messaging (FCM) để nhận thông báo khi ứng dụng đóng
- Badge số thông báo trên icon ứng dụng

#### Chia Sẻ Media
- Gửi ảnh từ thư viện hoặc chụp trực tiếp (nút camera/gallery đã có UI, chưa có logic)
- Xem ảnh phóng to khi tap vào
- Gửi file đính kèm

#### Trả Lời Tin Nhắn (Reply)
- Cơ sở dữ liệu đã có trường `replyToId` và `replyToContent`
- Cần hoàn thiện UI hiển thị và tích hợp gửi reply qua socket

#### Gọi Thoại / Video
- Placeholder UI đã có trong `ContactHeader` (đang bị comment)
- Có thể tích hợp WebRTC hoặc Agora SDK trong tương lai

#### Bảo Mật
- Mã hóa end-to-end cho tin nhắn
- Xác thực 2 bước (2FA)
- Tự động khóa ứng dụng bằng sinh trắc học

---

## 📦 Cài Đặt & Chạy Dự Án

### Yêu Cầu
- Android Studio Hedgehog trở lên
- JDK 11
- Android SDK API 26+

### Chạy Ứng Dụng
1. Clone repository:
   ```bash
   git clone https://github.com/<your-username>/RealtimeChatApp-Android.git
   ```
2. Mở project bằng Android Studio
3. Sync Gradle
4. Chạy trên thiết bị hoặc emulator (API 26+)

> Backend mặc định trỏ đến: `https://realtimechatapp-android-backend.onrender.com`  
> Có thể thay đổi `BASE_URL` trong `NetworkModule.kt`

---

## 📁 Cấu Trúc Package Chính

```
com.example.realtimechatapp
├── common/          # Extensions, FileUtils, ImageUtils, UiText
├── data/
│   ├── adapter/     # Gson custom adapters
│   ├── local/       # Room DB, DAO, Entity, DataStore managers
│   ├── remote/      # API interfaces, DTO, interceptors
│   └── repository/  # Repository implementations
├── di/              # Hilt modules
├── domain/
│   ├── exception/   # Custom exception classes
│   ├── model/       # Domain models
│   ├── repository/  # Repository & manager interfaces
│   ├── usecase/     # Business use cases
│   └── validation/  # Input validators
└── ui/
    ├── components/  # Reusable Composables
    ├── navigation/  # NavGraph, Screen sealed class
    ├── screens/     # Feature screens + ViewModels
    └── theme/       # Colors, Typography, Theme
```

---

## 👨‍💻 Tác Giả

Dự án được phát triển bằng **Kotlin + Jetpack Compose** theo hướng Clean Architecture.
