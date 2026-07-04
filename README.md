<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: 5;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<!-- Title -->
<h1 align="center"><b>NT118 - PHÁT TRIỂN ỨNG DỤNG TRÊN THIẾT BỊ DI ĐỘNG</b></h1>



## BẢNG MỤC LỤC
* [ Giới thiệu môn học](#gioithieumonhoc)
* [ Giảng viên hướng dẫn](#giangvien)
* [ Thành viên nhóm](#thanhvien)
* [ Đồ án môn học](#doan)


## GIỚI THIỆU MÔN HỌC
<a name="gioithieumonhoc"></a>
* **Tên môn học**: Phát triển ứng dụng trên thiết bị di động - Mobile Application Development
* **Mã môn học**: NT118
* **Lớp học**: NT118.Q22
* **Năm học**: 2025-2026


## GIẢNG VIÊN HƯỚNG DẪN
<a name="giangvien"></a>
* ThS. **Trần Hồng Nghi** - *nghith@uit.edu.vn*


## THÀNH VIÊN NHÓM
<a name="thanhvien"></a>
| STT    | MSSV          | Họ và Tên              | Github                                               | Email                   |
| ------ |:-------------:| ----------------------:|-----------------------------------------------------:|-------------------------:
| 1      | 23521378      | Thiều Đình Nam Tài     |[Blackdark](https://github.com/Blackdarked)           |23521378@gm.uit.edu.vn   |
| 2      | 23520588      | Nguyễn Lan Hương       |[Hinno](https://github.com/HinnoNguyen)               |23520588@gm.uit.edu.vn   |
| 3      | 23520225      | Đỗ Hải Đăng            |[dohaidang-git](https://github.com/dohaidang-git)     |23520225@gm.uit.edu.vn   |


## ĐỒ ÁN MÔN HỌC
<a name="doan"></a>

### 📌 Đề tài: **Narrativize** — Ứng dụng quản lý cá nhân phong cách nhập vai (RPG-themed Productivity App)

#### 📖 Giới thiệu đề tài
- **Narrativize** là một ứng dụng quản lý năng suất cá nhân độc đáo mang phong cách nhập vai (RPG), giúp người dùng biến các nhiệm vụ thường nhật thành các chuyến phiêu lưu và thử thách thú vị. Ứng dụng được xây dựng trên nền tảng Android (Native) bằng ngôn ngữ **Kotlin** và áp dụng kiến trúc **Clean Architecture + MVVM**. 
- Chi tiết về quá trình xây dựng, thiết kế và kết quả thử nghiệm được trình bày trong [Báo Cáo Đồ Án (Final)](NT118.Q22%20-%20Nh%C3%B3m%2015%20-%20B%C3%A1o%20C%C3%A1o%20%C4%90%E1%BB%93%20%C3%81n%20(Final).docx).

#### 🌟 Các tính năng cốt lõi
1. **Xác thực người dùng (Firebase Authentication):** Đăng nhập/Đăng ký tài khoản bảo mật, hỗ trợ đăng nhập qua Google.
2. **Ghi chú cá nhân (Notes CRUD):** Lưu trữ đám mây qua Firestore, hỗ trợ tạo, đọc, cập nhật, xóa và tìm kiếm nhanh chóng.
3. **Quản lý nhiệm vụ (Quest/Task):** Phân loại và quản lý các công việc hàng ngày theo mức độ ưu tiên và thuộc tính RPG.
4. **Đồng hồ Pomodoro:** Hỗ trợ quản lý thời gian tập trung với cơ chế đếm ngược (làm việc/nghỉ ngơi) và gửi thông báo cục bộ khi hoàn thành.
5. **Lịch sự kiện (Calendar):** Giao diện lịch trực quan theo tháng, quản lý các sự kiện được đồng bộ hóa thời gian thực qua Firestore.
6. **Hồ sơ cá nhân (Profile):** Quản lý thông tin tài khoản và cập nhật hồ sơ cá nhân.
7. **Sáng tạo câu chuyện tích hợp Trí tuệ nhân tạo (Groq AI Assistant):** Trình soạn thảo câu chuyện (Story Editor) được tích hợp Groq API nhằm hỗ trợ gợi ý phát triển cốt truyện, đề xuất ý tưởng viết tiếp (prompts) và tự động tìm kiếm các ghi chú có liên quan để làm tài liệu tham khảo phong phú cho câu chuyện.
8. **Chia sẻ mạng xã hội (Social Sharing):** Xuất nội dung câu chuyện hoặc ghi chú thành dạng thẻ ảnh nghệ thuật để chia sẻ nhanh lên Facebook và Instagram.

#### 🛠️ Công nghệ sử dụng
* **Ngôn ngữ:** Kotlin
* **Nền tảng:** Android (minSdk 26, targetSdk 36)
* **Cơ sở dữ liệu & Backend:** Firebase Authentication, Cloud Firestore
* **Trí tuệ nhân tạo (AI):** Groq API (sử dụng Retrofit)
* **Mẫu thiết kế & Kiến trúc:** Clean Architecture & MVVM (phân lớp Presentation, Domain, Data)
* **Kiểm thử tự động:** Unit Test (Mockito) và UI Test (Espresso)

#### 🎥 Video Demo
* [Video Demo chức năng chính (Phần 1)](VideoDemo/Screen_Recording_20260703_183250_Narrativize.mp4)
* [Video Demo chức năng chính (Phần 2)](VideoDemo/Screen_Recording_20260703_202615_Narrativize.mp4)
