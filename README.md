# 💊 Pharmacy Management System

![Version](https://img.shields.io/badge/version-1.0.2-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey)
![Status](https://img.shields.io/badge/status-Released-success)

Ứng dụng quản lý cửa hàng bán thuốc, được xây dựng nhằm hỗ trợ quản lý thuốc, nhập xuất kho, hóa đơn và dữ liệu vận hành tại các gian hàng thuốc.

---

## 📌 Thông tin phiên bản

* **Tên ứng dụng:** Pharmacy Management System
* **Phiên bản:** `1.0.2`
* **Trạng thái:** Đã phát hành
* **Nền tảng:** Windows
* **Định dạng cài đặt:** `.exe`

---

## 🎯 Đối tượng sử dụng

Phần mềm được phát triển chủ yếu dành cho:

> Các gian hàng và cửa hàng kinh doanh thuốc tại Chợ thuốc Quận 10, Thành phố Hồ Chí Minh.

Ứng dụng hướng đến việc đơn giản hóa quy trình quản lý kho thuốc, theo dõi hóa đơn và hạn chế sai sót trong quá trình nhập liệu thủ công.

---

## ✨ Các tính năng hiện có

### 💊 Quản lý thuốc

* Quản lý danh sách thuốc đang có trong kho.
* Thêm, chỉnh sửa và xóa thông tin thuốc.
* Theo dõi số lượng thuốc hiện tại.
* Tra cứu và tìm kiếm thuốc nhanh chóng.

### 📊 Nhập và xuất dữ liệu Excel

* Nhập danh sách thuốc từ file Excel.
* Xuất dữ liệu thuốc ra file Excel.
* Hỗ trợ nhập liệu hàng loạt, giảm thời gian thao tác thủ công.

### 🧾 Quản lý hóa đơn

* Tạo và quản lý hóa đơn nhập thuốc.
* Tạo và quản lý hóa đơn xuất thuốc.
* Theo dõi thông tin chi tiết của từng hóa đơn.

### 📜 Quản lý lịch sử nhập xuất

* Lưu lịch sử nhập thuốc.
* Lưu lịch sử xuất thuốc.
* Tra cứu lại các giao dịch đã thực hiện.
* Hỗ trợ kiểm tra và đối chiếu dữ liệu kho.

---

## 🛠️ Công nghệ sử dụng

* **Java 21**
* **JavaFX**
* **Spring Boot**
* **Spring Data JPA**
* **MySQL**
* **Maven**
* **jpackage**
* **WiX Toolset**

---

## 📂 Cấu trúc dự án

```text
PharmacyManagementSystem/
├── .mvn/                   # Maven Wrapper
├── src/                    # Mã nguồn chính
│   ├── main/
│   │   ├── java/           # Java source code
│   │   └── resources/      # FXML, CSS, hình ảnh và cấu hình
│   └── test/               # Mã nguồn kiểm thử
├── mvnw                    # Maven Wrapper cho Linux/macOS
├── mvnw.cmd                # Maven Wrapper cho Windows
├── pom.xml                 # Cấu hình Maven
├── HOWTODEPLOY.md          # Hướng dẫn đóng gói và triển khai
└── README.md               # Thông tin dự án
```

---

## 🚀 Chạy dự án từ mã nguồn

### Yêu cầu hệ thống

Trước khi chạy dự án, máy tính cần có:

* Java Development Kit 21
* MySQL Server
* Git
* Kết nối cơ sở dữ liệu đã được cấu hình

### Clone repository

```bash
git clone https://github.com/HiImGrass/Pharmacy-Management-System.git
```

Di chuyển vào thư mục dự án:

```bash
cd Pharmacy-Management-System
```

### Chạy trên Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Hoặc build dự án:

```powershell
.\mvnw.cmd clean package
```

---

## 📦 Cài đặt bản phát hành

Đối với người dùng thông thường, hãy sử dụng file cài đặt:

```text
PharmacyManagement-1.0.2.exe
```

Các bước cài đặt:

1. Mở file cài đặt `.exe`.
2. Cho phép ứng dụng thực hiện thay đổi trên máy tính.
3. Làm theo hướng dẫn cài đặt.
4. Khởi động ứng dụng từ shortcut trên Desktop hoặc Start Menu.

> Người dùng không cần cài đặt Maven hoặc môi trường lập trình để sử dụng bản phát hành.

---

## ⚠️ Lưu ý

* Không đưa mật khẩu cơ sở dữ liệu hoặc thông tin bảo mật lên GitHub.
* Nên sao lưu cơ sở dữ liệu trước khi cập nhật phiên bản mới.
* Không chỉnh sửa hoặc xóa dữ liệu trực tiếp trong cơ sở dữ liệu khi ứng dụng đang hoạt động.
* Kiểm tra file Excel đúng định dạng trước khi thực hiện nhập dữ liệu.

---

## 🗺️ Định hướng phát triển

Một số tính năng dự kiến có thể được bổ sung trong các phiên bản tiếp theo:

* Thống kê doanh thu.
* Báo cáo thuốc nhập và xuất theo thời gian.
* Cảnh báo thuốc sắp hết hàng.
* Cảnh báo thuốc sắp hết hạn.
* Phân quyền tài khoản nhân viên.
* Sao lưu và khôi phục dữ liệu.
* Cải thiện giao diện và trải nghiệm người dùng.

---

## 📝 Lịch sử phiên bản

### Version 1.0.2

* Sửa các lỗi được ghi nhận từ phiên bản trước.
* Cải thiện chức năng quản lý nhập thuốc.
* Bổ sung thao tác thêm, chỉnh sửa và xóa thuốc.
* Cải thiện kiểm tra dữ liệu đầu vào.
* Cập nhật bộ cài đặt cho Windows.

---

## 👨‍💻 Tác giả

Dự án được phát triển và duy trì bởi **HiImGrass**.

GitHub repository:

```text
https://github.com/HiImGrass/Pharmacy-Management-System
```

---

## 📄 Bản quyền

Dự án được phát triển phục vụ mục đích quản lý cửa hàng bán thuốc.

Không sao chép, phân phối hoặc sử dụng cho mục đích thương mại khi chưa có sự cho phép của tác giả.

---

<p align="center">
  <strong>Pharmacy Management System v1.0.2</strong>
  <br>
  Quản lý đơn giản · Vận hành hiệu quả · Dữ liệu chính xác
</p>
