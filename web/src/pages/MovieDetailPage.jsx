import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import client, { imgUrl } from '../api/client.js';

const AGE_LABELS = { P: 'P', K: 'K', T13: 'T13', T16: 'T16', T18: 'T18' };

export default function MovieDetailPage() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [cinemas, setCinemas] = useState([]);
  const [cinemaId, setCinemaId] = useState(null);
  const [date, setDate] = useState(dayjs().format('YYYY-MM-DD'));
  const [showtimes, setShowtimes] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      client.get(`/api/v1/movies/${slug}`),
      client.get('/api/v1/cinemas'),
    ])
      .then(([mv, cs]) => {
        setMovie(mv.data.data);
        setCinemas(cs.data.data);
        if (cs.data.data.length > 0) setCinemaId(cs.data.data[0].id);
      })
      .catch((e) => setError(e?.response?.data?.message || 'Không tải được phim'));
  }, [slug]);

  useEffect(() => {
    if (!cinemaId) return;
    client
      .get('/api/v1/showtimes', { params: { movieId: movie?.id, cinemaId, date } })
      .then((res) => setShowtimes(res.data.data))
      .catch(() => setShowtimes([]));
  }, [cinemaId, date, movie?.id]);

  const days = useMemo(
    () => Array.from({ length: 7 }, (_, i) => dayjs().add(i, 'day')),
    []
  );

  if (error) return <p className="page muted">{error}</p>;
  if (!movie) return <p className="page muted">Đang tải...</p>;

  return (
    <div className="page">
      <div className="movie-detail" style={{ backgroundImage: `url(${imgUrl(movie.backdropUrl) || imgUrl(movie.posterUrl)})` }}>
        <div className="movie-detail-overlay">
          <img className="detail-poster" src={imgUrl(movie.posterUrl)} alt={movie.title} />
          <div className="detail-info">
            <h1>{movie.title} <span className="age-badge inline">{AGE_LABELS[movie.ageRating]}</span></h1>
            <p className="muted">
              {movie.duration} phút · {movie.format} · {movie.subtitleType === 'LONG_TIENG' ? 'Lồng tiếng' : 'Phụ đề'} ·{' '}
              {movie.country} · {movie.releaseDate && `Khởi chiếu ${dayjs(movie.releaseDate).format('DD/MM/YYYY')}`}
            </p>
            <p className="muted">Thể loại: {movie.genres?.map((g) => g.name).join(', ')} · Đạo diễn: {movie.director}</p>
            <p className="muted">Diễn viên: {movie.actors}</p>
            <p className="desc">{movie.description}</p>
            {movie.trailerUrl && (
              <a className="btn btn-outline" href={movie.trailerUrl} target="_blank" rel="noreferrer">
                ▶ Xem trailer
              </a>
            )}
          </div>
        </div>
      </div>

      <section className="section booking-box">
        <h2>Đặt vé — Chọn rạp & suất chiếu</h2>
        <div className="form-row">
          <label>
            Rạp:
            <select value={cinemaId || ''} onChange={(e) => setCinemaId(Number(e.target.value))}>
              {cinemas.map((c) => <option key={c.id} value={c.id}>{c.name} ({c.city})</option>)}
            </select>
          </label>
        </div>
        <div className="day-tabs">
          {days.map((d) => (
            <button
              key={d.format('YYYY-MM-DD')}
              className={d.format('YYYY-MM-DD') === date ? 'tab active' : 'tab'}
              onClick={() => setDate(d.format('YYYY-MM-DD'))}
            >
              {d.format('DD/MM')} <small>{['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][d.day()]}</small>
            </button>
          ))}
        </div>

        <div className="showtime-grid">
          {showtimes.length === 0 && <p className="muted">Không có suất chiếu phù hợp.</p>}
          {showtimes.map((s) => (
            <button key={s.id} className="showtime-btn" onClick={() => navigate(`/seats/${s.id}`)}>
              <strong>{dayjs(s.startTime).format('HH:mm')}</strong>
              <span className="muted">~ {dayjs(s.endTime).format('HH:mm')}</span>
              <span className="muted">{s.roomName} · {Number(s.priceBase).toLocaleString('vi')}đ</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
