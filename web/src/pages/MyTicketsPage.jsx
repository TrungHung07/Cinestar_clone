import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import client, { errorMessage, imgUrl } from '../api/client.js';

const statusColors = {
  CONFIRMED: '#15803d',
  PENDING: '#a16207',
  CANCELLED: '#7a1020',
  EXPIRED: '#555',
};

export default function MyTicketsPage() {
  const [bookings, setBookings] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    client
      .get('/api/v1/bookings/me')
      .then((res) => setBookings(res.data.data.content))
      .catch((e) => setError(errorMessage(e)));
  }, []);

  return (
    <div className="page">
      <h1>Vé của tôi</h1>
      {error && <p className="alert error">{error}</p>}
      {bookings.length === 0 && !error && <p className="muted">Bạn chưa có vé nào.</p>}
      <div className="ticket-list">
        {bookings.map((b) => (
          <Link to={`/tickets/${b.bookingCode}`} className="ticket-card" key={b.id}>
            <img src={imgUrl(b.showtime.moviePosterUrl)} alt={b.showtime.movieTitle} />
            <div className="ticket-info">
              <h3>{b.showtime.movieTitle}</h3>
              <p className="muted">{b.showtime.cinemaName} · {b.showtime.roomName}</p>
              <p className="muted">{dayjs(b.showtime.startTime).format('HH:mm · DD/MM/YYYY')} · Ghế {b.seats.map((s) => `${s.rowLabel}${s.seatNumber}`).join(', ')}</p>
            </div>
            <div className="ticket-side">
              <span className="status-tag" style={{ background: statusColors[b.status] || '#555' }}>
                {b.status}
              </span>
              <strong>{Number(b.totalAmount).toLocaleString('vi')}đ</strong>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
