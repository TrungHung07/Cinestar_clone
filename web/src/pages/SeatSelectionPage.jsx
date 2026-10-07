import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import client, { errorMessage, imgUrl } from '../api/client.js';

const seatClass = (seat, selected) => {
  if (seat.status === 'SOLD') return 'seat sold';
  if (selected) return 'seat selected';
  if (seat.status === 'HELD') return 'seat held';
  return `seat ${seat.seatType.toLowerCase()}`;
};

export default function SeatSelectionPage() {
  const { showtimeId } = useParams();
  const navigate = useNavigate();
  const [layout, setLayout] = useState(null);
  const [selected, setSelected] = useState(new Map());
  const [combos, setCombos] = useState([]);
  const [comboQty, setComboQty] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const timerRef = useRef(null);

  const loadLayout = useCallback(() => {
    client.get(`/api/v1/showtimes/${showtimeId}/seats`).then((res) => {
      setLayout(res.data.data);
    }).catch((e) => setError(errorMessage(e)));
  }, [showtimeId]);

  // So do ghe tu dong refresh de thay ghe nguoi khac vua giu
  useEffect(() => {
    loadLayout();
    const interval = setInterval(loadLayout, 10000);
    return () => clearInterval(interval);
  }, [loadLayout]);

  useEffect(() => {
    client.get('/api/v1/combos').then((res) => setCombos(res.data.data));
  }, []);

  useEffect(() => () => clearInterval(timerRef.current), []);

  if (error) return <p className="page muted">{error}</p>;
  if (!layout) return <p className="page muted">Đang tải sơ đồ ghế...</p>;

  const { showtime, seats, holdMinutes } = layout;
  const rows = [...new Set(seats.map((s) => s.rowLabel))].sort();

  const toggleSeat = (seat) => {
    if (seat.status === 'SOLD') return;
    if (seat.status === 'HELD' && !seat.heldByMe) return;
    setSelected((prev) => {
      const next = new Map(prev);
      if (next.has(seat.seatId)) next.delete(seat.seatId);
      else next.set(seat.seatId, seat);
      return next;
    });
  };

  const seatTotal = [...selected.values()].reduce((sum, s) => sum + Number(s.price), 0);
  const comboTotal = Object.entries(comboQty).reduce((sum, [id, qty]) => {
    const combo = combos.find((c) => c.id === Number(id));
    return sum + (combo ? Number(combo.price) * qty : 0);
  }, 0);

  const confirmBooking = async () => {
    if (selected.size === 0) {
      setError('Vui lòng chọn ít nhất một ghế');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      const items = Object.entries(comboQty)
        .filter(([, qty]) => qty > 0)
        .map(([comboId, quantity]) => ({ comboId: Number(comboId), quantity }));
      const res = await client.post('/api/v1/bookings', {
        showtimeId: Number(showtimeId),
        seatIds: [...selected.keys()],
        items,
      });
      navigate(`/checkout/${res.data.data.bookingCode}`);
    } catch (e) {
      setError(errorMessage(e));
      loadLayout();
      setSelected(new Map());
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="section booking-box">
        <h2>Chọn ghế — {showtime.movieTitle}</h2>
        <p className="muted">
          {showtime.cinemaName} · {showtime.roomName} · {showtime.startTime.replace('T', ' ').slice(0, 16).replace('-', '/')}
        </p>

        <div className="screen">MÀN HÌNH</div>

        <div className="seat-map">
          {rows.map((row) => (
            <div className="seat-row" key={row}>
              <span className="row-label">{row}</span>
              {seats
                .filter((s) => s.rowLabel === row)
                .sort((a, b) => a.seatNumber - b.seatNumber)
                .map((seat) => (
                  <button
                    key={seat.seatId}
                    className={seatClass(seat, selected.has(seat.seatId))}
                    title={`${seat.rowLabel}${seat.seatNumber} — ${Number(seat.price).toLocaleString('vi')}đ`}
                    onClick={() => toggleSeat(seat)}
                  >
                    {seat.seatNumber}
                  </button>
                ))}
            </div>
          ))}
        </div>

        <div className="legend">
          <span><i className="seat standard" /> Ghế thường</span>
          <span><i className="seat vip" /> Ghế VIP (+20k)</span>
          <span><i className="seat couple" /> Ghế đôi</span>
          <span><i className="seat selected" /> Đang chọn</span>
          <span><i className="seat held" /> Đang giữ</span>
          <span><i className="seat sold" /> Đã bán</span>
        </div>

        <div className="combo-box">
          <h3>Bắp nước (tùy chọn)</h3>
          <div className="combo-list">
            {combos.map((c) => (
              <div className="combo-item" key={c.id}>
                <div>
                  <strong>{c.name}</strong>
                  <p className="muted">{Number(c.price).toLocaleString('vi')}đ</p>
                </div>
                <div className="qty">
                  <button onClick={() => setComboQty((q) => ({ ...q, [c.id]: Math.max(0, (q[c.id] || 0) - 1) }))}>−</button>
                  <span>{comboQty[c.id] || 0}</span>
                  <button onClick={() => setComboQty((q) => ({ ...q, [c.id]: Math.min(10, (q[c.id] || 0) + 1) }))}>+</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {error && <p className="alert error">{error}</p>}

        <div className="summary">
          <div>
            Ghế: <strong>{[...selected.values()].map((s) => `${s.rowLabel}${s.seatNumber}`).join(', ') || '—'}</strong>
            <br />
            <span className="muted">
              Ghế {seatTotal.toLocaleString('vi')}đ{comboTotal > 0 && ` + Combo ${comboTotal.toLocaleString('vi')}đ`}
            </span>
          </div>
          <div className="summary-total">
            <span className="muted">Tạm tính</span>
            <strong>{(seatTotal + comboTotal).toLocaleString('vi')}đ</strong>
            <p className="muted small">Ghế sẽ được giữ {holdMinutes} phút sau khi tạo đơn</p>
          </div>
          <button className="btn btn-primary btn-lg" onClick={confirmBooking} disabled={submitting}>
            {submitting ? 'Đang xử lý...' : 'Xác nhận & Thanh toán'}
          </button>
        </div>
      </div>
    </div>
  );
}
