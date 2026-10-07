# 🚀 Hướng dẫn Deploy lên Internet

> ## ⚡ PHƯƠNG ÁN MIỄN PHÍ TRỌN VẸN (khuyên dùng thử trước)
>
> **Render (backend) + Neon (PostgreSQL) + Upstash (Redis) + Vercel (web)** — tất cả free, không cần thẻ:
>
> 1. **Push code lên GitHub** (Bước 0 bên dưới)
> 2. **Neon** — [neon.tech](https://neon.tech) → Create project → copy *Connection string*
>    → đổi thành JDBC: `jdbc:postgresql://ep-xxx.aws.neon.tech/neondb?sslmode=require`
>    (user/password nằm trong chuỗi, tách ra 2 biến riêng)
> 3. **Upstash** — [console.upstash.com](https://console.upstash.com) → **Redis → Create** (Region gần VN: ap-southeast-1)
>    → copy **Host**, **Port 6379**, **Password**
> 4. **Render** — [dashboard.render.com](https://dashboard.render.com) → **New → Blueprint** → chọn repo
>    (Render đọc sẵn file `render.yaml` của dự án: Docker + rootDir `javabackend` + health check)
>    → điền các biến `sync: false` mà nó hỏi:
>    - `DB_URL` = chuỗi JDBC Neon ở trên (kèm `?sslmode=require`)
>    - `DB_USERNAME` / `DB_PASSWORD` = của Neon
>    - `REDIS_HOST` / `SPRING_DATA_REDIS_PASSWORD` = của Upstash (đã có sẵn `SSL=true`)
>    - `JWT_SECRET` = chuỗi bí mật bất kỳ ≥ 32 ký tự
>    - `VNPAY_TMN_CODE` / `VNPAY_HASH_SECRET` = trong file `.env` local
>    - `VNPAY_RETURN_URL` = `https://<ten-web>.vercel.app/payment/result` (điền sau, sửa lại được)
>    - `VNPAY_IPN_URL` = `https://cinestar-backend.onrender.com/api/v1/payments/vnpay/ipn`
>    - Các biến Kafka cứ để trống (listener đã cấu hình auto-startup=false)
> 5. **Create Resources** → Render build Docker (~10-15 phút lần đầu) → nhận URL
>    `https://cinestar-backend.onrender.com` → test: mở `/swagger-ui.html`
> 6. Quay lại **Vercel**: biến `VITE_API_URL` = URL trên → Redeploy web → hoàn tất
>
> ⚠️ Gói free của Render: service **ngủ sau 15 phút không ai gọi** (lần truy cập sau chờ ~1 phút
> dậy) — chấp nhận được cho demo; 512MB RAM đủ cho Spring Boot của dự án.

Deploy dự án Cinestar lên cloud, tách 3 phần:

| Thành phần | Nơi deploy | Lý do |
|---|---|---|
| **Web** (`web/`) | **Vercel** | Site tĩnh React+Vite — Vercel là lựa chọn hoàn hảo, free |
| **Backend** (`javabackend/`) | **Railway** | Spring Boot cần chạy Docker/JVM liên tục — Vercel KHÔNG chạy được |
| **PostgreSQL + Redis** | Railway add-on (cùng chỗ backend) | Free trong gói trial của Railway |
| **Kafka** | Upstash Kafka (free) | Railway không có Kafka |
| **Mobile** (`frontend/`) | Không cần deploy | Chạy bằng Expo Go trên điện thoại, trỏ tới API public |

> ⚠️ Vercel là nền tảng **serverless** — không chạy được Docker/JVM dài hạn hay database,
> nên backend bắt buộc phải nằm ở Railway/Render/Fly.io.

---

## Bước 0 — Đưa code lên GitHub (bắt buộc cho cả Vercel & Railway)

```bash
cd Cinema
git init
git add .
git commit -m "Cinestar clone - initial commit"
# Tạo repo trống trên github.com (ví dụ: tenban/cinestar-clone) rồi:
git remote add origin https://github.com/tenban/cinestar-clone.git
git branch -M main
git push -u origin main
```

> 💡 Nhớ kiểm tra `.gitignore` không push `.env` (chứa Hash Secret VNPAY).

---

## Bước 1 — Deploy Web lên Vercel (bạn đã có tài khoản)

1. Vào [vercel.com/new](https://vercel.com/new) → **Import** repo vừa push
2. Cấu hình project:
   - **Root Directory**: `web`
   - Framework Preset: **Vite** (tự nhận)
   - Build Command: `npm run build` · Output: `dist`
3. Mục **Environment Variables** thêm:
   ```
   VITE_API_URL = https://<tên-backend>.up.railway.app
   ```
   (URL backend sẽ có sau Bước 2 — deploy backend trước rồi quay lại điền cũng được)
4. Bấm **Deploy** → xong có link `https://<ten-project>.vercel.app`

File `web/vercel.json` đã chuẩn bị sẵn (SPA rewrite cho react-router).

---

## Bước 2 — Deploy Backend + PostgreSQL + Redis lên Railway

1. Vào [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo** → chọn repo
2. Trong service backend: **Settings → Root Directory** = `javabackend` (Railway sẽ tự build bằng Dockerfile có sẵn)
3. Thêm hạ tầng trong cùng project: **New → Database → PostgreSQL** và **New → Database → Redis**
4. Vào tab **Variables** của service backend, thêm:

   | Biến | Giá trị |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://<host>:<port>/railway?sslmode=require` (copy từ Postgres → Connect → JDBC URL, thêm `?sslmode=require`) |
   | `DB_USERNAME` / `DB_PASSWORD` | theo Postgres của Railway |
   | `REDIS_HOST` / `REDIS_PORT` | theo Redis của Railway |
   | `KAFKA_BOOTSTRAP_SERVERS` | từ Upstash Kafka (Bước 3) |
   | `JWT_SECRET` | chuỗi bí mật bất kỳ dài ≥ 32 ký tự |
   | `TZ` | `Asia/Ho_Chi_Minh` |
   | `VNPAY_TMN_CODE` / `VNPAY_HASH_SECRET` | như file `.env` hiện tại |
   | `VNPAY_RETURN_URL` | `https://<ten-web>.vercel.app/payment/result` |
   | `VNPAY_IPN_URL` | `https://<ten-backend>.up.railway.app/api/v1/payments/vnpay/ipn` |

5. **Settings → Networking → Generate Domain** → nhận URL public, ví dụ
   `https://cinestar-backend.up.railway.app` — đây chính là giá trị cho `VITE_API_URL` ở Bước 1
   (nhớ quay lại Vercel thêm biến rồi **Redeploy**)

> Sau khi deploy lần đầu, backend tự tạo schema + seed dữ liệu (DataSeeder chạy khi DB trống).
> Swagger sẽ ở: `https://<backend>.up.railway.app/swagger-ui.html`

---

## Bước 3 — Kafka miễn phí trên Upstash

1. [console.upstash.com](https://console.upstash.com) → **Kafka → Create Database** (gói Free)
2. Tạo topic `booking-events` (3 partitions)
3. Copy **Bootstrap Server + username + password** → Spring cần SASL, bổ sung vào Railway Variables:

   ```
   KAFKA_BOOTSTRAP_SERVERS=<host>:<port>
   SPRING_KAFKA_PROPERTIES_SASL_MECHANISM=SCRAM-SHA-256
   SPRING_KAFKA_PROPERTIES_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username="<user>" password="<pass>";
   SPRING_KAFKA_SECURITY_PROTOCOL=SASL_SSL
   ```

> 💡 Nếu chỉ cần demo nhanh và không muốn lo Kafka cloud: vẫn chạy được không có Kafka —
> gửi email xác nhận sẽ lỗi (đã try/catch, không làm hỏng đặt vé), chỉ thêm 2 biến
> `SPRING_KAFKA_LISTENER_AUTO_STARTUP=false` và `SPRING_KAFKA_PRODUCER_ENABLED=false`
> để tắt tiếng ồn log. Khi nộp đồ án, demo Kafka trên máy local bằng docker-compose là đủ.

---

## Bước 4 — Kiểm chứng sau deploy

1. Mở `https://<web>.vercel.app` — trang chủ load phim (API đã trỏ tới backend public)
2. Mở Swagger `https://<backend>.up.railway.app/swagger-ui.html` — login + test API
3. Đặt vé → Thanh toán VNPAY → thẻ demo NCB → vì backend có domain public,
   **VNPAY sẽ gọi IPN server-to-server thật** (lần đầu luồng IPN chạy đúng chuẩn production)
4. Đăng nhập admin → báo cáo doanh thu

## Lưu ý quan trọng

- **CORS**: backend đang mở cho mọi origin (dev). Khi deploy thật, sửa `SecurityConfig`
  `setAllowedOriginPatterns("*")` → chỉ giữ domain Vercel của bạn.
- **JWT_SECRET**: đặt chuỗi mạnh riêng, không dùng mặc định trong repo.
- **Railway free trial** hết credit ($5) thì service dừng — để demo nộp bài thì thoải mái;
  muốn chạy lâu dài hơn có thể xem Render (free tier nhưng ngủ sau 15 phút không dùng).
- **Mobile**: trong `frontend/src/config.js`, đổi `LAN_IP` thành domain Railway
  (`https://cinestar-backend.up.railway.app`) rồi build lại — app trên điện thoại dùng được API public.
