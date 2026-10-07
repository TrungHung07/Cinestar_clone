import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import dayjs from 'dayjs';
import client, { errorMessage, imgUrl } from '../api/client.js';

export default function TicketDetailPage() {
  const { bookingCode } = useParams();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    client
      .get(`/api/v1/bookings/${bookingCode}`)
      .then((res) => setBooking(res.data.data))
      .catch((e) => setError(errorMessage(e)));
  }, [bookingCode]);

  if (error) return <p className="page muted">{error}</p>;
  if (!booking) return <p className="page muted">Đang tải...</p>;

  const confirmed = booking.status === 'CONFIRMED';

  return (
    <div className="page">
      <div className="section booking-box narrow ticket-detail">
        <h2>{booking.showtime.movieTitle}</h2>
        <div className="ticket-body">
          <div className="ticket-fields">
            <p><strong>Rạp:</strong> {booking.showtime.cinemaName}</p>
            <p><strong>Phòng:</strong> {booking.showtime.roomName}</p>
            <p><strong>Suất chiếu:</strong> {dayjs(booking.showtime.startTime).format('HH:mm · DD/MM/YYYY')}</p>
            <p><strong>Ghế:</strong> {booking.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(', ')}</p>
            <p><strong>Khách:</strong> {booking.bookedBy}</p>
            <p><strong>Tổng:</strong> {Number(booking.totalAmount).toLocaleString('vi')}đ</p>
            {booking.paymentMethod && <p className="muted">Thanh toán: {booking.paymentMethod} — {booking.paymentStatus}</p>}
          </div>
          <div className={`ticket-qr ${confirmed ? '' : 'disabled'}`}>
            {confirmed ? (
              <>
                <QRCodeSVG value={booking.bookingCode} size={140} />
                <p className="muted small">Xuất trình mã này tại quầy check-in</p>
              </>
            ) : (
              <p className="muted">QR khả dụng khi vé ở trạng thái CONFIRMED</p>
            )}
          </div>
        </div>
        <p className="booking-code-line">Mã vé: <strong>{booking.bookingCode}</strong></p>
        {confirmed && (
          <button
            className="btn btn-danger"
            onClick={async () => {
              if (window.confirm('Hủy vé này? Ghế sẽ được giải phóng.')) {
                try {
                  await client.post(`/api/v1/bookings/${booking.bookingCode}/cancel`);
                  window.location.reload();
                } catch (e) {
                  setError(errorMessage(e));
                }
              }
            }}
          >
            Hủy vé
          </button>
        )}
        {error && <p className="alert error">{error}</p>}
      </div>
    </div>
  );
}
