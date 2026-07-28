# 🪦 BackOnDie - Minecraft Plugin (1.21.1+)

**BackOnDie** là plugin hỗ trợ lệnh `/back` nâng cao dành riêng cho máy chủ Paper/Purpur 1.21.1+. Plugin kiểm tra xem mộ của người chơi có còn tồn tại không trước khi dịch chuyển.

---

## ✨ Tính năng nổi bật
* **Tích hợp Plugin Mộ:** Hỗ trợ kiểm tra sự tồn tại của mộ qua API của plugin `Graves`/`GraveStone`.
* **Mỗi lần chết là 1 bản ghi riêng:** ID Mộ duy nhất cho từng lần chết, hoàn toàn không đè dữ liệu cũ.
* **Cơ sở dữ liệu Asynchronous:** Sử dụng HikariCP với SQLite mặc định hoặc MySQL. Đảm bảo server 100+ người chơi hoạt động mượt mà không bị lag tick.
* **Hệ thống Teleport An Toàn:** Hỗ trợ Warmup (thời gian chờ), Cooldown (thời gian hồi), và tự động hủy dịch chuyển nếu di chuyển.
* **Định dạng MiniMessage & PAPI:** Dễ dàng chỉnh sửa giao diện tin nhắn, ActionBar, Title bằng màu RGB/Gradient và tích hợp PlaceholderAPI.

---

## 📜 Lệnh & Quyền hạn (Permissions)

| Lệnh | Mô tả | Permission |
| :--- | :--- | :--- |
| `/back` | Dịch chuyển về vị trí chết gần nhất | `backondie.use` |
| `/back reload` | Tải lại cấu hình plugin | `backondie.admin` |
| `/back clear <player>` | Xóa dữ liệu lịch sử chết của người chơi | `backondie.admin` |

---

## 🧩 Placeholders (PlaceholderAPI)
- `%backondie_last_death_world%` - Tên thế giới của lần chết gần nhất.
- `%backondie_last_death_x%` - Tọa độ X.
- `%backondie_last_death_y%` - Tọa độ Y.
- `%backondie_last_death_z%` - Tọa độ Z.

---

## 🛠️ Biên dịch (Build)
Yêu cầu **JDK 21** và **Maven**:
```bash
mvn clean package
