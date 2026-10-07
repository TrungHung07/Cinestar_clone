#!/bin/bash
# ============================================================
# TEST TOAN BO API CINESTAR - phat hien moi loi 5xx
# Chay voi 3 vai tro (anonymous / user / admin), GET endpoints
# chay 2 lan de test ca cache MISS lan cache HIT.
# ============================================================
BASE="http://localhost:8090"
TODAY=$(date +%Y-%m-%d)
TOMORROW=$(date -d "+1 day" +%Y-%m-%d 2>/dev/null || date -v+1d +%Y-%m-%d)
TS=$(date +%s)
TOTAL=0; FAIL5=0; PASS=0

check() {
  local method="$1" url="$2" token="$3" body="$4"
  TOTAL=$((TOTAL+1))
  local args=(-s -o /tmp/resp_body.json -w "%{http_code}" -X "$method" "$BASE$url")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ] && args+=(-H "Content-Type: application/json" -d "$body")
  local code=$(curl "${args[@]}")
  if [[ "$code" =~ ^5 ]]; then
    FAIL5=$((FAIL5+1))
    echo "  [5xx!] $method $url -> $code  BODY: $(head -c 160 /tmp/resp_body.json)"
  else
    PASS=$((PASS+1))
    echo "  [$code] $method $url"
  fi
}

echo "########## VONG 1: PUBLIC + AUTH ##########"
check GET /api/v1/movies/now-showing
check GET /api/v1/movies/upcoming
check GET /api/v1/movies/featured
check GET /api/v1/movies
check GET "/api/v1/movies?status=NOW_SHOWING&page=0&size=5"
check GET "/api/v1/movies?search=%C4%90i%E1%BB%87p"
check GET /api/v1/movies/diep-vien-bong-dem
check GET /api/v1/cinemas
check GET /api/v1/cinemas/cinestar-hai-ba-trung
check GET "/api/v1/showtimes?date=$TODAY"
check GET "/api/v1/showtimes?cinemaId=1&date=$TOMORROW"
check GET /api/v1/combos
check GET /api/v1/promotions

echo "########## EDGE CASES (phai tra 4xx, khong 5xx) ##########"
check GET "/api/v1/movies?status=GARBAGE"
check GET "/api/v1/movies/khong-ton-tai-xyz"
check GET "/api/v1/movies/khong-ton-tai-xyz"
check GET "/api/v1/cinemas/khong-ton-tai-xyz"
check GET "/api/v1/showtimes?date=not-a-date"
check GET /api/v1/showtimes/99999/seats
check GET /api/v1/bookings/me
check GET /api/v1/admin/reports/revenue?from=2026-01-01\&to=2026-01-31

echo "########## AUTH ##########"
USER_TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"user1","password":"user123"}' | python -c "import json,sys; print(json.load(sys.stdin)['data']['accessToken'])")
ADMIN_TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' | python -c "import json,sys; print(json.load(sys.stdin)['data']['accessToken'])")
REFRESH=$(curl -s -X POST $BASE/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"user1","password":"user123"}' | python -c "import json,sys; print(json.load(sys.stdin)['data']['refreshToken'])")
[ -n "$USER_TOKEN" ] && echo "  (user1 login OK)" || echo "  (user1 login FAIL)"
check POST /api/v1/auth/login "" '{"username":"user1","password":"SAI_MAT_KHAU"}'
check POST /api/v1/auth/register "" "{\"username\":\"testts$TS\",\"email\":\"testts$TS@test.vn\",\"password\":\"pass123\",\"fullName\":\"Test User\",\"phone\":\"0912345678\"}"
check POST /api/v1/auth/register "" '{"username":"user1","email":"dup@test.vn","password":"pass123","fullName":"Dup"}'
check POST /api/v1/auth/refresh "" "{\"refreshToken\":\"$REFRESH\"}"
check POST /api/v1/auth/refresh "" '{"refreshToken":"garbage.token.here"}'

echo "########## USER: BOOKING + PAYMENT ##########"
SID=$(curl -s "$BASE/api/v1/showtimes?cinemaId=1&date=$TOMORROW" | python -c "import json,sys; d=json.load(sys.stdin)['data']; print(d[0]['id'] if d else '')")
SEATS=$(curl -s "$BASE/api/v1/showtimes/$SID/seats" | python -c "
import json,sys
seats=json.load(sys.stdin)['data']['seats']
avail=[s['seatId'] for s in seats if s['status']=='AVAILABLE'][:2]
print(f'{avail[0]},{avail[1]}' if len(avail)>=2 else '')")
S1=${SEATS%%,*}; S2=${SEATS##*,}
echo "  (showtime=$SID, seats=$S1,$S2)"
check POST /api/v1/bookings "$USER_TOKEN" "{\"showtimeId\":$SID,\"seatIds\":[$S1,$S2],\"items\":[{\"comboId\":1,\"quantity\":1}]}"
check POST /api/v1/bookings "$USER_TOKEN" "{\"showtimeId\":$SID,\"seatIds\":[$S1],\"items\":[]}"
BOOKING=$(curl -s "$BASE/api/v1/bookings/me" -H "Authorization: Bearer $USER_TOKEN" | python -c "
import json,sys
c=json.load(sys.stdin)['data']['content']
print([b for b in c if b['status']=='PENDING'][0]['bookingCode'])")
BID=$(curl -s "$BASE/api/v1/bookings/$BOOKING" -H "Authorization: Bearer $USER_TOKEN" | python -c "import json,sys; print(json.load(sys.stdin)['data']['id'])")
check GET /api/v1/bookings/me "$USER_TOKEN"
check GET /api/v1/bookings/$BOOKING "$USER_TOKEN"
check GET /api/v1/bookings/$BOOKING "" 
check GET /api/v1/bookings/CS-KHONGTONTAI999 "$USER_TOKEN"
check POST /api/v1/payments/vnpay/$BID "$USER_TOKEN"
check POST /api/v1/payments/vnpay/999999 "$USER_TOKEN"
check GET "/api/v1/payments/vnpay/return?vnp_TxnRef=FAKE123&vnp_ResponseCode=00&vnp_Amount=100&vnp_SecureHash=deadbeef"
check GET "/api/v1/payments/vnpay/ipn?vnp_TxnRef=FAKE123&vnp_SecureHash=deadbeef"
check POST /api/v1/bookings/$BOOKING/cancel "$USER_TOKEN"
check POST /api/v1/bookings/$BOOKING/cancel "$USER_TOKEN"

echo "########## ADMIN: CRUD + REPORT ##########"
check GET "/api/v1/admin/reports/revenue?from=2026-01-01&to=$TODAY" "$ADMIN_TOKEN"
check GET "/api/v1/admin/reports/revenue" "$ADMIN_TOKEN"
check GET "/api/v1/admin/reports/revenue?from=2026-99-99&to=2026-99-99" "$ADMIN_TOKEN"
check POST /api/v1/admin/movies "$ADMIN_TOKEN" "{\"title\":\"Test Movie $TS\",\"slug\":\"test-movie-$TS\",\"duration\":100,\"status\":\"NOW_SHOWING\",\"ageRating\":\"T13\",\"format\":\"2D\",\"subtitleType\":\"PHU_DE\",\"genreNames\":[\"Hành Động\"],\"posterUrl\":\"\"}"
MID=$(curl -s "$BASE/api/v1/movies/test-movie-$TS" | python -c "import json,sys; print(json.load(sys.stdin)['data']['id'])")
check PUT /api/v1/admin/movies/$MID "$ADMIN_TOKEN" "{\"title\":\"Test Movie Updated\",\"slug\":\"test-movie-$TS\",\"duration\":110,\"status\":\"NOW_SHOWING\",\"ageRating\":\"T16\",\"format\":\"2D\",\"subtitleType\":\"PHU_DE\",\"genreNames\":[\"Hành Động\"],\"posterUrl\":\"\"}"
check POST /api/v1/admin/showtimes "$ADMIN_TOKEN" "{\"movieId\":$MID,\"roomId\":1,\"startTime\":\"$TOMORROW T03:30:00\",\"priceBase\":60000}"
check POST /api/v1/admin/showtimes "$ADMIN_TOKEN" "{\"movieId\":$MID,\"roomId\":1,\"startTime\":\"$TOMORROW T03:30:00\",\"priceBase\":60000}"
check POST /api/v1/admin/showtimes "$ADMIN_TOKEN" "{\"movieId\":$MID,\"roomId\":1,\"startTime\":\"2000-01-01T00:00:00\",\"priceBase\":60000}"
STID=$(curl -s "$BASE/api/v1/showtimes?movieId=$MID&cinemaId=1&date=$TOMORROW" | python -c "import json,sys; d=json.load(sys.stdin)['data']; print(d[0]['id'] if d else '')")
check DELETE /api/v1/admin/showtimes/$STID "$ADMIN_TOKEN"
check DELETE /api/v1/admin/movies/$MID "$ADMIN_TOKEN"
check POST /api/v1/admin/cinemas "$ADMIN_TOKEN" "{\"name\":\"Test Cinema $TS\",\"slug\":\"test-cinema-$TS\",\"address\":\"Test St 1\",\"city\":\"Hà Nội\",\"hotline\":\"1900000\"}"
CID=$(curl -s $BASE/api/v1/cinemas | python -c "
import json,sys
d=json.load(sys.stdin)['data']
print([c['id'] for c in d if c['slug']=='test-cinema-$TS'][0])")
check PUT /api/v1/admin/cinemas/$CID "$ADMIN_TOKEN" "{\"name\":\"Test Cinema Updated\",\"slug\":\"test-cinema-$TS\",\"address\":\"Test St 2\",\"city\":\"Hà Nội\"}"
check DELETE /api/v1/admin/cinemas/$CID "$ADMIN_TOKEN"
check POST /api/v1/admin/combos "$ADMIN_TOKEN" "{\"name\":\"Test Combo $TS\",\"price\":50000}"
COMBO_ID=$(curl -s $BASE/api/v1/combos | python -c "
import json,sys
d=json.load(sys.stdin)['data']
print([c['id'] for c in d if 'Test Combo' in c['name']][0])")
check PUT /api/v1/admin/combos/$COMBO_ID "$ADMIN_TOKEN" "{\"name\":\"Test Combo X\",\"price\":55000}"
check DELETE /api/v1/admin/combos/$COMBO_ID "$ADMIN_TOKEN"
check POST /api/v1/admin/promotions "$ADMIN_TOKEN" "{\"title\":\"Test Promo $TS\",\"description\":\"desc\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\"}"
PROMO_ID=$(curl -s $BASE/api/v1/promotions | python -c "
import json,sys
d=json.load(sys.stdin)['data']
print([p['id'] for p in d if 'Test Promo' in p['title']][0])")
check PUT /api/v1/admin/promotions/$PROMO_ID "$ADMIN_TOKEN" "{\"title\":\"Test Promo X\",\"description\":\"desc\"}"
check DELETE /api/v1/admin/promotions/$PROMO_ID "$ADMIN_TOKEN"

echo "########## PHAN QUYEN: user1 goi ADMIN ##########"
check GET "/api/v1/admin/reports/revenue?from=2026-01-01&to=2026-01-31" "$USER_TOKEN"
check POST /api/v1/admin/movies "$USER_TOKEN" '{"title":"Hack","slug":"hack-attempt"}'

echo "########## VONG 2: CHAY LAI GET de test CACHE HIT ##########"
check GET /api/v1/movies/now-showing
check GET /api/v1/movies/upcoming
check GET /api/v1/movies/featured
check GET /api/v1/movies
check GET /api/v1/movies/diep-vien-bong-dem
check GET /api/v1/cinemas
check GET "/api/v1/showtimes?date=$TODAY"
check GET "/api/v1/showtimes?cinemaId=1&date=$TOMORROW"
check GET /api/v1/combos
check GET /api/v1/promotions
check GET /api/v1/movies/diep-vien-bong-dem
check GET /api/v1/cinemas

echo "=========================================="
echo "TONG: $TOTAL | PASS (khong 5xx): $PASS | LOI 5xx: $FAIL5"
[ $FAIL5 -eq 0 ] && echo ">>> KET QUA: KHONG CON API NAO LOI 5xx <<<" || echo ">>> VAN CON LOI 5xx - can sua <<<"
