import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import client, { imgUrl } from '../api/client.js';
import MovieCard from '../components/MovieCard.jsx';

/** Trang chủ kiểu Cinestar: hero slider + ĐẶT VÉ NHANH + các section phim */
export default function HomePage() {
  const navigate = useNavigate();
  const [nowShowing, setNowShowing] = useState([]);
  const [upcoming, setUpcoming] = useState([]);
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Hero slider
  const [heroIdx, setHeroIdx] = useState(0);

  // Đặt vé nhanh
  const [cinemas, setCinemas] = useState([]);
  const [qb, setQb] = useState({ cinemaId: '', movieId: '', date: dayjs().format('YYYY-MM-DD'), showtimeId: '' });
  const [showtimes, setShowtimes] = useState([]);

  useEffect(() => {
    Promise.all([
      client.get('/api/v1/movies/now-showing'),
      client.get('/api/v1/movies/upcoming'),
      client.get('/api/v1/movies/featured'),
      client.get('/api/v1/promotions'),
      client.get('/api/v1/cinemas'),
    ])
      .then(([now, up, feat, promos, cs]) => {
        setNowShowing(now.data.data);
        setUpcoming(up.data.data);
        setPromotions(promos.data.data);
        setCinemas(cs.data.data);
        setFeatured(feat.data.data);
        if (cs.data.data.length) setQb((q) => ({ ...q, cinemaId: String(cs.data.data[0].id) }));
        if (now.data.data.length) setQb((q) => ({ ...q, movieId: String(now.data.data[0].id) }));
      })
      .finally(() => setLoading(false));
  }, []);

  const [featured, setFeatured] = useState([]);

  // Tự động chuyển slide 5s
  useEffect(() => {
    if (featured.length < 2) return;
    const t = setInterval(() => setHeroIdx((i) => (i + 1) % featured.length), 5000);
    return () => clearInterval(t);
  }, [featured.length]);

  // Suất chiếu theo rạp + phim + ngày
  useEffect(() => {
    if (!qb.cinemaId || !qb.movieId || !qb.date) { setShowtimes([]); return; }
    client
      .get('/api/v1/showtimes', { params: { movieId: qb.movieId, cinemaId: qb.cinemaId, date: qb.date } })
      .then((res) => setShowtimes(res.data.data))
      .catch(() => setShowtimes([]));
  }, [qb.cinemaId, qb.movieId, qb.date]);

  const days = useMemo(() => Array.from({ length: 7 }, (_, i) => dayjs().add(i, 'day')), []);

  const quickBook = () => {
    if (!qb.showtimeId) {
      alert('Bạn chưa chọn suất chiếu');
      return;
    }
    navigate(`/seats/${qb.showtimeId}`);
  };

  const hero = featured[heroIdx];

  return (
    <div className="home-page">
      {/* ===== HERO SLIDER ===== */}
      {featured.length > 0 && (
        <div className="hero-slider">
          <div className="hero-track">
            {featured.map((m, i) => (
              <div key={m.id} className={`hero-slide ${i === heroIdx ? 'active' : ''}`}>
                <img src={imgUrl(m.backdropUrl || m.posterUrl)} alt={m.title} />
                <div className="hero-caption">
                  <h2>{m.title}</h2>
                  <button className="btn-yellow" onClick={() => navigate(`/movies/${m.slug}`)}>
                    Đặt vé ngay
                  </button>
                </div>
              </div>
            ))}
          </div>
          <button className="hero-arrow left" onClick={() => setHeroIdx((heroIdx - 1 + featured.length) % featured.length)}>❮</button>
          <button className="hero-arrow right" onClick={() => setHeroIdx((heroIdx + 1) % featured.length)}>❯</button>
          <div className="hero-dots">
            {featured.map((m, i) => (
              <button key={m.id} className={i === heroIdx ? 'active' : ''} onClick={() => setHeroIdx(i)} />
            ))}
          </div>
        </div>
      )}

      {/* ===== ĐẶT VÉ NHANH ===== */}
      <div className="quickbook">
        <div className="quickbook-inner">
          <h1>Đặt vé nhanh</h1>
          <div className="quickbook-fields">
            <select value={qb.cinemaId} onChange={(e) => setQb({ ...qb, cinemaId: e.target.value })}>
              <option value="">1. Chọn Rạp</option>
              {cinemas.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <select value={qb.movieId} onChange={(e) => setQb({ ...qb, movieId: e.target.value })}>
              <option value="">2. Chọn Phim</option>
              {nowShowing.map((m) => <option key={m.id} value={m.id}>{m.title}</option>)}
            </select>
            <select value={qb.date} onChange={(e) => setQb({ ...qb, date: e.target.value })}>
              {days.map((d) => (
                <option key={d.format('YYYY-MM-DD')} value={d.format('YYYY-MM-DD')}>
                  3. {d.format('DD/MM')} - {['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][d.day()]}
                </option>
              ))}
            </select>
            <select value={qb.showtimeId} onChange={(e) => setQb({ ...qb, showtimeId: e.target.value })}>
              <option value="">4. Chọn Suất</option>
              {showtimes.map((s) => (
                <option key={s.id} value={s.id}>
                  {dayjs(s.startTime).format('HH:mm')} - {s.roomName}
                </option>
              ))}
            </select>
          </div>
          <button className="btn-purple-cta" onClick={quickBook}>Đặt Ngay</button>
        </div>
      </div>

      {/* ===== PHIM ĐANG CHIẾU ===== */}
      <section className="section">
        <div className="section-head"><h2>Phim Đang Chiếu</h2></div>
        {loading ? (
          <p className="muted" style={{ textAlign: 'center' }}>Đang tải...</p>
        ) : (
          <>
            <div className="movie-grid">
              {nowShowing.slice(0, 4).map((m) => <MovieCard key={m.id} movie={m} />)}
            </div>
            <div className="xemthem-wrap">
              <Link className="btn-xemthem" to="/movies?status=NOW_SHOWING">Xem thêm</Link>
            </div>
          </>
        )}
      </section>

      {/* ===== PHIM SẮP CHIẾU ===== */}
      <section className="section">
        <div className="section-head"><h2>Phim Sắp Chiếu</h2></div>
        <div className="movie-grid">
          {upcoming.slice(0, 4).map((m) => <MovieCard key={m.id} movie={m} />)}
        </div>
        <div className="xemthem-wrap">
          <Link className="btn-xemthem" to="/movies?status=UPCOMING">Xem thêm</Link>
        </div>
      </section>

      {/* ===== KHUYẾN MÃI ===== */}
      {promotions.length > 0 && (
        <section className="section">
          <div className="section-head"><h2>Khuyến Mãi</h2></div>
          <div className="promo-row">
            {promotions.slice(0, 2).map((p) => (
              <div key={p.id} className="promo-card">
                <img src={imgUrl(p.imageUrl)} alt={p.title} loading="lazy" />
                <div className="promo-body">
                  <h4>{p.title}</h4>
                  <p className="muted">{p.description}</p>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* ===== THÀNH VIÊN (trang trí kiểu Cinestar) ===== */}
      <section className="section">
        <div className="member-grid">
          <div className="member-card" style={{ backgroundImage: 'url(https://picsum.photos/seed/cfriend/900/500)' }}>
            <div className="member-overlay">
              <h3>Thành Viên C'Friend</h3>
              <p className="muted">Thẻ C'Friend mang đến nhiều ưu đãi cho thành viên mới</p>
              <Link to="/register" className="btn-yellow" style={{ alignSelf: 'flex-start' }}>Tìm hiểu ngay</Link>
            </div>
          </div>
          <div className="member-card" style={{ backgroundImage: 'url(https://picsum.photos/seed/cvip/900/500)' }}>
            <div className="member-overlay">
              <h3>Thành Viên C'VIP</h3>
              <p className="muted">Thẻ VIP Cinestar dành riêng cho bạn những đặc quyền chất riêng</p>
              <Link to="/register" className="btn-yellow" style={{ alignSelf: 'flex-start' }}>Tìm hiểu ngay</Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
