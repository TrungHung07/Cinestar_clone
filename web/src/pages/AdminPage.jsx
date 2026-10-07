import React, { useEffect, useState } from 'react';
import dayjs from 'dayjs';
import client, { errorMessage, imgUrl } from '../api/client.js';

const emptyMovie = {
  title: '', slug: '', duration: 120, country: 'Việt Nam', status: 'NOW_SHOWING',
  ageRating: 'T13', format: '2D', subtitleType: 'PHU_DE', genreNames: '', posterUrl: '', description: '',
};

/** Trang quan tri: quan ly phim, tao suat chieu, bao cao doanh thu (ADMIN) */
export default function AdminPage() {
  const [tab, setTab] = useState('movies');
  const [movies, setMovies] = useState([]);
  const [message, setMessage] = useState('');
  const [newMovie, setNewMovie] = useState(emptyMovie);
  const [showtimes, setShowtimes] = useState([]);
  const [st, setSt] = useState({ movieId: '', roomId: '', startTime: '', priceBase: 75000 });
  const [revenue, setRevenue] = useState(null);
  const [range, setRange] = useState({ from: dayjs().subtract(7, 'day').format('YYYY-MM-DD'), to: dayjs().format('YYYY-MM-DD') });
  const [rooms, setRooms] = useState([]);

  const loadMovies = () =>
    client.get('/api/v1/movies?size=50').then((res) => {
      const { content } = res.data.data;
      setMovies(content);
      setSt((s) => ({ ...s, movieId: s.movieId || content[0]?.id }));
    });

  useEffect(() => { loadMovies(); }, []);
  useEffect(() => {
    client.get('/api/v1/cinemas').then(async (res) => {
      const cinemaId = res.data.data[0]?.id;
      if (cinemaId) {
        // Lay phong cua rap dau tien (API cong khai khong co danh sach phong,
        // nen lay showtimes hom nay de suy ra phong - du dung cho demo)
        const today = dayjs().format('YYYY-MM-DD');
        const sts = await client.get('/api/v1/showtimes', { params: { cinemaId, date: today } });
        const map = new Map();
        sts.data.data.forEach((s) => map.set(s.roomId, s.roomName));
        setRooms([...map.entries()].map(([id, name]) => ({ id, name })));
        setSt((s) => ({ ...s, roomId: s.roomId || [...map.keys()][0] }));
      }
    });
  }, []);
  useEffect(() => {
    if (tab !== 'showtimes') return;
    client.get('/api/v1/showtimes', { params: { date: dayjs().format('YYYY-MM-DD') } })
      .then((res) => setShowtimes(res.data.data.slice(0, 30)));
  }, [tab]);
  useEffect(() => {
    if (tab !== 'revenue') return;
    client.get('/api/v1/admin/reports/revenue', { params: range })
      .then((res) => setRevenue(res.data.data))
      .catch((e) => setMessage(errorMessage(e)));
  }, [tab, range]);

  const createMovie = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const body = {
        ...newMovie,
        duration: Number(newMovie.duration),
        genreNames: newMovie.genreNames.split(',').map((g) => g.trim()).filter(Boolean),
        posterUrl: newMovie.posterUrl || `https://picsum.photos/seed/${newMovie.slug}/400/600`,
      };
      await client.post('/api/v1/admin/movies', body);
      setMessage('✓ Đã thêm phim');
      setNewMovie(emptyMovie);
      loadMovies();
    } catch (err) {
      setMessage(errorMessage(err));
    }
  };

  const deleteMovie = async (id) => {
    if (!window.confirm('Xóa phim này?')) return;
    try {
      await client.delete(`/api/v1/admin/movies/${id}`);
      setMessage('✓ Đã xóa phim');
      loadMovies();
    } catch (err) {
      setMessage(errorMessage(err));
    }
  };

  const createShowtime = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      await client.post('/api/v1/admin/showtimes', {
        movieId: Number(st.movieId), roomId: Number(st.roomId),
        startTime: st.startTime, priceBase: Number(st.priceBase),
      });
      setMessage('✓ Đã tạo suất chiếu');
    } catch (err) {
      setMessage(errorMessage(err));
    }
  };

  return (
    <div className="page">
      <h1>Quản trị hệ thống</h1>
      <div className="tabs">
        <button className={tab === 'movies' ? 'tab active' : 'tab'} onClick={() => setTab('movies')}>Phim</button>
        <button className={tab === 'showtimes' ? 'tab active' : 'tab'} onClick={() => setTab('showtimes')}>Suất chiếu</button>
        <button className={tab === 'revenue' ? 'tab active' : 'tab'} onClick={() => setTab('revenue')}>Doanh thu</button>
      </div>
      {message && <p className="alert info">{message}</p>}

      {tab === 'movies' && (
        <div className="admin-split">
          <form className="admin-form" onSubmit={createMovie}>
            <h3>Thêm phim mới</h3>
            <input placeholder="Tên phim" value={newMovie.title} required
              onChange={(e) => setNewMovie({ ...newMovie, title: e.target.value })} />
            <input placeholder="slug (vd: dao-nguyen-bao-thu)" value={newMovie.slug} required
              onChange={(e) => setNewMovie({ ...newMovie, slug: e.target.value })} />
            <div className="form-grid">
              <input type="number" placeholder="Thời lượng (phút)" value={newMovie.duration}
                onChange={(e) => setNewMovie({ ...newMovie, duration: e.target.value })} />
              <select value={newMovie.status} onChange={(e) => setNewMovie({ ...newMovie, status: e.target.value })}>
                <option value="NOW_SHOWING">Đang chiếu</option>
                <option value="UPCOMING">Sắp chiếu</option>
              </select>
              <select value={newMovie.ageRating} onChange={(e) => setNewMovie({ ...newMovie, ageRating: e.target.value })}>
                {['P', 'K', 'T13', 'T16', 'T18'].map((a) => <option key={a} value={a}>{a}</option>)}
              </select>
              <select value={newMovie.format} onChange={(e) => setNewMovie({ ...newMovie, format: e.target.value })}>
                <option value="2D">2D</option>
                <option value="3D">3D</option>
              </select>
            </div>
            <input placeholder="Thể loại (cách nhau dấu phẩy)" value={newMovie.genreNames}
              onChange={(e) => setNewMovie({ ...newMovie, genreNames: e.target.value })} />
            <button className="btn btn-primary">Thêm phim</button>
          </form>
          <div className="admin-list">
            <h3>Danh sách phim ({movies.length})</h3>
            <table>
              <thead><tr><th>Tên</th><th>Trạng thái</th><th>Tuổi</th><th></th></tr></thead>
              <tbody>
                {movies.map((m) => (
                  <tr key={m.id}>
                    <td>{m.title}</td>
                    <td>{m.status === 'UPCOMING' ? 'Sắp chiếu' : 'Đang chiếu'}</td>
                    <td>{m.ageRating}</td>
                    <td><button className="btn btn-danger btn-sm" onClick={() => deleteMovie(m.id)}>Xóa</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'showtimes' && (
        <div className="admin-split">
          <form className="admin-form" onSubmit={createShowtime}>
            <h3>Tạo suất chiếu</h3>
            <select value={st.movieId} onChange={(e) => setSt({ ...st, movieId: Number(e.target.value) })}>
              {movies.map((m) => <option key={m.id} value={m.id}>{m.title}</option>)}
            </select>
            <select value={st.roomId} onChange={(e) => setSt({ ...st, roomId: Number(e.target.value) })}>
              {rooms.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
            </select>
            <input type="datetime-local" value={st.startTime} required
              onChange={(e) => setSt({ ...st, startTime: e.target.value })} />
            <input type="number" placeholder="Giá vé cơ bản" value={st.priceBase}
              onChange={(e) => setSt({ ...st, priceBase: e.target.value })} />
            <button className="btn btn-primary">Tạo suất chiếu</button>
            <p className="muted small">Suất chiếu mới tự invalidate cache Redis (showtimes) qua @CacheEvict.</p>
          </form>
          <div className="admin-list">
            <h3>Suất chiếu hôm nay ({showtimes.length})</h3>
            <table>
              <thead><tr><th>Phim</th><th>Rạp</th><th>Phòng</th><th>Giờ</th></tr></thead>
              <tbody>
                {showtimes.map((s) => (
                  <tr key={s.id}>
                    <td>{s.movieTitle}</td>
                    <td>{s.cinemaName}</td>
                    <td>{s.roomName}</td>
                    <td>{dayjs(s.startTime).format('HH:mm')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'revenue' && (
        <div>
          <div className="range-picker">
            <input type="date" value={range.from} onChange={(e) => setRange({ ...range, from: e.target.value })} />
            →
            <input type="date" value={range.to} onChange={(e) => setRange({ ...range, to: e.target.value })} />
          </div>
          {revenue && (
            <>
              <div className="revenue-cards">
                <div className="revenue-card">
                  <span className="muted">Tổng doanh thu</span>
                  <strong>{Number(revenue.totalRevenue).toLocaleString('vi')}đ</strong>
                </div>
                <div className="revenue-card">
                  <span className="muted">Tổng đơn vé</span>
                  <strong>{revenue.totalBookings}</strong>
                </div>
              </div>
              <h3>Doanh thu theo ngày</h3>
              <table>
                <thead><tr><th>Ngày</th><th>Đơn vé</th><th>Doanh thu</th></tr></thead>
                <tbody>
                  {revenue.byDate.map((r) => (
                    <tr key={r.date}><td>{r.date}</td><td>{r.bookings}</td><td>{Number(r.revenue).toLocaleString('vi')}đ</td></tr>
                  ))}
                </tbody>
              </table>
              <h3>Top phim</h3>
              <table>
                <thead><tr><th>Phim</th><th>Đơn vé</th><th>Doanh thu</th></tr></thead>
                <tbody>
                  {revenue.topMovies.map((m) => (
                    <tr key={m.movieTitle}><td>{m.movieTitle}</td><td>{m.bookings}</td><td>{Number(m.revenue).toLocaleString('vi')}đ</td></tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>
      )}
    </div>
  );
}
