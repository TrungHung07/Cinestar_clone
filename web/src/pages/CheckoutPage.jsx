import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import dayjs from 'dayjs';
import client, { errorMessage, imgUrl } from '../api/client.js';

export default function CheckoutPage() {
  const { bookingCode } = useParams();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [paying, setPaying] = useState(false);

  useEffect(() => {
    client
      .get(`/api/v1/bookings/${bookingCode}`)
      .then((res) => setBooking(res.data.data))
      .catch((e) => setError(errorMessage(e)));
  }, [bookingCode]);

  const payNow = async () => {
    setPaying(true);
    setError('');
    try {
      const res = await client.post(`/api/v1/payments/vnpay/${booking.id}`);
      // Chuyển sang trang VNPAY sandbox - the demo: NCB 9704198526171432198, OTP 123456
      window.location.href = res.data.data.paymentUrl;
    } catch (e) {
      setError(errorMessage(e));
      setPaying(false);
    }
  };

  if (error && !booking) return <p className="page muted">{error}</p>;
  if (!booking) return <p className="page muted">Đang tải...</p>;

  const expired = dayjs(booking.expiresAt).isBefore(dayjs());

  return (
    <div className="page">
      <div className="section booking-box narrow">
        <h2>Xác nhận thanh toán</h2>
        <div className="checkout-info">
          <p><strong>Mã đơn:</strong> {booking.bookingCode}</p>
          <p><strong>Phim:</strong> {booking.showtime.movieTitle}</p>
          <p><strong>Rạp:</strong> {booking.showtime.cinemaName} — {booking.showtime.roomName}</p>
          <p><strong>Suất chiếu:</strong> {dayjs(booking.showtime.startTime).format('HH:mm DD/MM/YYYY')}</p>
          <p><strong>Ghế:</strong> {booking.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(', ')}</p>
          {booking.items.length > 0 && (
            <p><strong>Combo:</strong> {booking.items.map((i) => `${i.comboName} x${i.quantity}`).join(', ')}</p>
          )}
          <p className="total-line"><strong>Tổng tiền:</strong> {Number(booking.totalAmount).toLocaleString('vi')}đ</p>
          <p className="muted small">Đơn giữ ghế đến {dayjs(booking.expiresAt).format('HH:mm:ss')}</p>
        </div>

        {booking.status === 'CONFIRMED' ? (
          <div className="alert success">
            Đơn này đã thanh toán thành công. <Link to={`/tickets/${booking.bookingCode}`}>Xem vé</Link>
          </div>
        ) : expired ? (
          <div className="alert error">Đơn đã hết thời gian giữ ghế. Vui lòng đặt lại.</div>
        ) : (
          <>
            {error && <p className="alert error">{error}</p>}
            <div className="vnpay-box">
              <strong>Thanh toán qua VNPAY</strong>
              <p className="muted small">
                Sandbox — thẻ demo NCB: <code>9704198526171432198</code>, tên NGUYEN VAN A, OTP <code>123456</code>
              </p>
              <button className="btn btn-primary btn-lg" onClick={payNow} disabled={paying}>
                {paying ? 'Đang tạo liên kết...' : 'Thanh toán VNPAY'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
