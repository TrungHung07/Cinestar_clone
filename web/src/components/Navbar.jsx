import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../store/auth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [showBanner, setShowBanner] = useState(true);
  const [keyword, setKeyword] = useState('');

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const search = (e) => {
    e.preventDefault();
    navigate(keyword.trim() ? `/movies?search=${encodeURIComponent(keyword.trim())}` : '/movies');
  };

  return (
    <>
      {showBanner && (
        <div className="top-banner">
          <span>📞 Tải ứng dụng Cinestar Cinemas. Tra cứu lịch chiếu và đặt vé siêu nhanh</span>
          <button className="btn-open-banner" onClick={() => navigate('/')}>Mở</button>
          <button className="btn-close-banner" onClick={() => setShowBanner(false)}>✕</button>
        </div>
      )}
      <nav className="navbar">
        <div className="navbar-inner">
          <Link to="/" className="brand">
            <span className="brand-c">C</span>
            <span className="brand-text">INESTAR</span>
          </Link>
          <Link to="/movies?status=NOW_SHOWING" className="btn-yellow">🎟 Đặt Vé Ngay</Link>
          <Link to="/promotions" className="btn-purple-cta">🍿 Đặt Bắp Nước</Link>
          <form className="nav-search" onSubmit={search}>
            <input
              placeholder="Tìm phim, rạp"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            <button type="submit" aria-label="Tìm">🔍</button>
          </form>
          <div className="nav-auth">
            {user ? (
              <div className="user-menu">
                <button className="btn btn-outline" onClick={() => setOpen(!open)}>
                  👤 {user.fullName} ▾
                </button>
                {open && (
                  <div className="dropdown">
                    <button onClick={() => { navigate('/tickets'); setOpen(false); }}>Vé của tôi</button>
                    {user.roles?.includes('ROLE_ADMIN') && (
                      <button onClick={() => { navigate('/admin'); setOpen(false); }}>Quản trị</button>
                    )}
                    <button onClick={handleLogout}>Đăng xuất</button>
                  </div>
                )}
              </div>
            ) : (
              <Link to="/login" className="login-link">
                <span className="flag-icon" style={{ background: 'transparent', border: '1.5px solid #6b6790' }}>👤</span>
                Đăng nhập
              </Link>
            )}
            <span className="flag">
              <span className="flag-icon">★</span> VN ▾
            </span>
          </div>
        </div>
        <div className="nav-links">
          <Link to="/cinemas"><i className="pin">📍</i> Chọn rạp</Link>
          <Link to="/movies?status=NOW_SHOWING"><i className="pin">📍</i> Lịch chiếu</Link>
          <span className="spacer" />
          <Link to="/promotions">Khuyến mãi</Link>
          <Link to="/cinemas">Tổ chức sự kiện</Link>
          <Link to="/cinemas">Dịch vụ giải trí khác</Link>
          <Link to="/cinemas">Giới thiệu</Link>
        </div>
      </nav>
    </>
  );
}
