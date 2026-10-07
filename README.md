# 🎬 CineStar Cinema — Clone cinestar.com.vn

Hệ thống đặt vé xem phim đầy đủ chức năng, clone theo luồng đặt vé 4 bước của cinestar.com.vn
(chọn **rạp → phim → ngày → suất → ghế**) kèm đặt bắp nước, khuyến mãi và báo cáo doanh thu.

## 🏗️ Kiến trúc & Công nghệ

| Thành phần | Công nghệ |
|---|---|
| **Mobile** (`frontend/`) | React Native **Expo**, React Navigation, Zustand, Axios, WebView (VNPAY), QRCode |
| **Web** (`web/`) | React 18 + Vite, React Router, Zustand, Axios, qrcode.react |
| **Backend** (`javabackend/`) | Java 21, Spring Boot 3.5 (MVC: Controller → Service → ServiceImpl → Repository), Spring Security + JWT |
| **Database** | PostgreSQL 16 (JPA/Hibernate) |
| **Cache & Lock** | Redis 7 — cache dữ liệu đọc nhiều + **giữ ghế (seat hold)** |
| **Message Broker** | Apache Kafka (KRaft) — sự kiện đặt vé |
| **Thanh toán** | VNPAY Sandbox |
| **Triển khai** | Docker Compose (postgres + redis + kafka + backend) |

```
frontend/          React Native Expo (mobile)
web/               React + Vite (web)
javabackend/       Spring Boot backend
docker-compose.yml postgres + redis + kafka + backend
```

## 🚀 Chạy dự án

### 1. Hạ tầng + Backend (Docker)

```bash
docker compose up -d
```

Câu lệnh trên khởi động: PostgreSQL (port host **5433**), Redis (6379), Kafka (9092) và backend
(port host **8090**). Lần chạy đầu, backend tự tạo schema + seed dữ liệu mẫu.

> ⚠️ Port 5432/8080 có thể bị service khác trên Windows chiếm nên dự án dùng **5433** (postgres)
> và **8090** (backend host).

Chạy backend **ngoài Docker** (khi dev):

```bash
cd javabackend
./mvnw spring-boot:run          # cần postgres/redis/kafka đang chạy trong docker
```

### 2. Web

```bash
cd web
npm install
npm run dev                     # http://localhost:5173
```

### 3. Mobile (Expo)

```bash
cd frontend
npm install
npx expo start
```

- **Android Emulator**: chạy được ngay (API = `http://10.0.2.2:8090`).
- **Điện thoại thật (Expo Go)**: mở `frontend/src/config.js`, đổi `LAN_IP` thành IP máy tính
  (`ipconfig` → IPv4), rồi quét QR.

## 👤 Tài khoản demo

| Tài khoản | Mật khẩu | Vai trò |
|---|---|---|
| `admin` | `admin123` | ADMIN — CRUD phim/suất chiếu, báo cáo doanh thu |
| `user1` (Nguyễn Văn A) | `user123` | USER — đặt vé, xem vé, hủy vé |

## 💳 Thanh toán VNPAY Sandbox

> ⚠️ **Nếu bấm "Thanh toán VNPAY" mà VNPAY báo "Không tìm thấy website" (Error.html?code=72)**
> → bạn đang dùng TmnCode mặc định. Bắt buộc phải đăng ký sandbox (miễn phí, 2 phút):

1. Mở <https://sandbox.vnpayment.vn/devreg/> → điền thông tin cá nhân + website
   (nhập `http://localhost:5173`), submit → nhận **TmnCode** + **Hash Secret** ngay qua email.
2. Mở file `.env` ở gốc dự án, dán 2 giá trị vào `VNPAY_TMN_CODE` và `VNPAY_HASH_SECRET`.
3. Chạy `docker compose up -d backend` để backend nhận cấu hình mới.
4. Đặt vé lại → bấm Thanh toán VNPAY → dùng **thẻ demo NCB**:
   - Số thẻ: `9704198526171432198`
   - Tên chủ thẻ: `NGUYEN VAN A`
   - Ngày hết hạn: bất kỳ (VD `03/07`), OTP: `123456`

Các lỗi VNPAY hay gặp: `code=70` TMN sai định dạng · `code=72` TMN không tồn tại (chưa
đăng ký) · `code=03` / sai checksum → kiểm tra lại `VNPAY_HASH_SECRET` đã copy đủ, không
có khoảng trắng hay xuống dòng thừa.

> Trên localhost, VNPAY không gọi được IPN (server-to-server cần public URL) nên flow xác nhận
> chính là endpoint `/api/v1/payments/vnpay/return` — frontend web/mobile sẽ truyền toàn bộ tham số
> VNPAY trả về cho backend để **xác minh chữ ký HMAC-SHA512 + đối soát số tiền** rồi mới CONFIRMED.
> Endpoint `/api/v1/payments/vnpay/ipn` vẫn được hiện thực đầy đủ cho môi trường production.

## 🔑 Các yêu cầu kỹ thuật & cách hiện thực

### Phân quyền (RBAC + JWT)
- Spring Security stateless, access token 15' + refresh token 7'.
- `ROLE_ADMIN`: mọi API `/api/v1/admin/**` (`@PreAuthorize("hasRole('ADMIN')")`).
- `ROLE_USER`: đặt vé/thanh toán/hủy vé; **chỉ thao tác được đơn của chính mình** (kiểm tra ownership trong service).

### Chống N+1 query
| Vị trí | Cách làm |
|---|---|
| Lịch chiếu | `ShowtimeRepository.findWithDetails` — **1 query** JOIN FETCH movie + room + cinema |
| Danh sách phim | `@EntityGraph(attributePaths = "genres")` trên các method `findByStatus/findBySlug` |
| Movie.genres (collection) | `@BatchSize(size = 20)` — fetch theo lô thay vì 1 query/phim |
| Sơ đồ ghế | `findActiveByShowtimeIdWithHolder` — join fetch booking + user trong 1 query |
| Auth | `findByUsernameIgnoreCase` fetch sẵn roles cho Spring Security |

Bật log SQL để chứng minh: đặt `org.hibernate.SQL: DEBUG` trong `application.yml`, gọi
`GET /api/v1/showtimes?...` và quan sát chỉ có **1 query** (thay vì 1 + 3N).

### Redis — cache + giữ ghế
- **Cache** (`@Cacheable`): `movies` (10'), `movie-details` (30'), `cinemas` (10'), `showtimes` (2'), `combos`, `promotions` (15'). Admin sửa dữ liệu → `@CacheEvict` tương ứng.
- **Seat hold**: `SET seat:hold:{showtimeId}:{seatId} {userId} EX 300 NX` — giữ ghế 5 phút, tự nhả khi hết TTL.

### Hai user đặt cùng một ghế — 3 lớp phòng thủ
1. **Lớp 1 — Redis lock khi chọn ghế**: `SETNX` với TTL 5 phút. User bấm sau nhận lỗi
   `409 Ghế A1 đang được người khác giữ` ngay trên sơ đồ ghế (sơ đồ tự refresh 10s).
2. **Lớp 2 — khoá bi quan khi xác nhận**: `SELECT ... FOR UPDATE`
   (`SeatRepository.findAllByIdForUpdate`) tuần tự hoá 2 giao dịch thanh toán tranh cùng ghế.
3. **Lớp 3 — database chốt hạ**: **unique partial index**
   `booking_seats(showtime_id, seat_id) WHERE status IN ('HELD','BOOKED')`
   (tạo bởi `DatabaseSchemaInitializer`) — mọi lần lọt qua 2 lớp trên đều bị PostgreSQL từ chối
   `INSERT`, backend trả `409`.

Thêm nữa: job chạy mỗi 60s chuyển đơn `PENDING` quá 5 phút sang `EXPIRED` và nhả ghế.

### Kafka — mục đích cụ thể
Topic `booking-events` (3 partitions, key = bookingCode để bảo toàn thứ tự):

- Khi VNPAY xác nhận thanh toán → backend publish `BOOKING_CONFIRMED` → API trả kết quả cho user **ngay lập tức**.
- **Consumer 1** (`cinestar-email`): gửi email xác nhận đặt vé (khi chưa cấu hình SMTP sẽ in nội dung email ra console — xem log backend).
- **Consumer 2** (`cinestar-cache-invalidator`): xoá cache Redis liên quan để mọi instance backend thấy dữ liệu mới.

→ **Tách luồng thanh toán khỏi các tác vụ phụ**: email chậm/fail không ảnh hưởng giao dịch; scale từng phần độc lập.

## 🧪 Kịch bản demo nhanh

1. **Đăng nhập** `user1/user123` (web hoặc app).
2. Chọn phim → rạp → ngày → suất → **chọn 2 ghế** + combo bắp nước → tạo đơn.
3. Mở **tab/điện thoại khác**, đăng nhập `user2`, vào cùng suất đó → thấy 2 ghế vừa chọn chuyển
   sang "Đang giữ", chọn lại → nhận **409**.
4. `user1` thanh toán VNPAY bằng thẻ demo → vé chuyển `CONFIRMED`, xuất hiện QR trong "Vé của tôi";
   console backend in **EMAIL DEMO** (Kafka consumer).
5. Đăng nhập `admin/admin123` → trang Quản trị: thêm phim, tạo suất chiếu, xem doanh thu.
6. Đặt 1 đơn nhưng **không thanh toán** → sau 5 phút job tự chuyển `EXPIRED`, ghế được nhả.

## 📚 Swagger UI (OpenAPI 3)

Backend tích hợp **springdoc-openapi** — mở:

```
http://localhost:8090/swagger-ui.html
```

- 13 nhóm API (Xác thực, Phim, Suất chiếu, Đặt vé, Thanh toán VNPAY, Quản trị...).
- Nhấn nút **Authorize** và dán access token (lấy từ `POST /api/v1/auth/login`, không cần gõ
  tiền tố `Bearer`) để test các endpoint cần đăng nhập/quyền ADMIN ngay trên UI.
- Raw OpenAPI JSON: `/v3/api-docs`.

## 🔌 API tổng quan (REST `/api/v1`)

```
POST   /auth/register | /auth/login | /auth/refresh
GET    /movies?status=&page=&size=   /movies/now-showing   /movies/upcoming   /movies/featured
GET    /movies/{slug}
GET    /cinemas                      /cinemas/{slug}
GET    /showtimes?movieId=&cinemaId=&date=        /showtimes/{id}/seats
POST   /bookings                                  (USER)
GET    /bookings/me                  /bookings/{bookingCode}    (đúng chủ đơn)
POST   /bookings/{bookingCode}/cancel             (đúng chủ đơn)
POST   /payments/vnpay/{bookingId}                (USER)
GET    /payments/vnpay/return        /payments/vnpay/ipn
GET    /combos                       /promotions
CRUD   /admin/movies | /admin/showtimes | /admin/cinemas | /admin/combos | /admin/promotions  (ADMIN)
GET    /admin/reports/revenue?from=&to=           (ADMIN)
```

Response chuẩn: `{ "success": true, "message": "...", "data": ..., "timestamp": "..." }`.

## 🗄️ Sơ đồ dữ liệu (chính)

`users` — `roles` (n-n) · `movies` — `genres` (n-n) · `cinemas` → `rooms` → `seats` ·
`showtimes` (movie, room) · `bookings` (user, showtime) → `booking_seats` (seat, **unique active**) ·
`booking_items` (combo) · `payments` (VNPAY) · `combos`, `promotions`.
