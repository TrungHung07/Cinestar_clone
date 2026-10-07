import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <div>
          <div className="footer-brand">
            <span className="brand-c">C</span>
            <span className="brand-text">INESTAR</span>
          </div>
          <p>Hệ thống rạp chiếu phim giá rẻ, hiện đại. Tra cứu lịch chiếu và đặt vé trực tuyến siêu nhanh.</p>
        </div>
        <div>
          <h4>Về Cinestar</h4>
          <ul>
            <li><Link to="/cinemas">Hệ thống rạp</Link></li>
            <li><Link to="/movies?status=UPCOMING">Phim sắp chiếu</Link></li>
            <li><Link to="/promotions">Khuyến mãi</Link></li>
            <li><Link to="/">Giới thiệu</Link></li>
          </ul>
        </div>
        <div>
          <h4>Hỗ trợ</h4>
          <ul>
            <li>Hotline: 1900 6017</li>
            <li>Đặt vé: 08:00 - 22:00</li>
            <li>Chính sách bảo mật</li>
            <li>Điều khoản sử dụng</li>
          </ul>
        </div>
        <div>
          <h4>Kết nối với chúng tôi</h4>
          <ul>
            <li>📍 Cụm rạp tại Hà Nội, TP.HCM, Đà Lạt</li>
            <li>✉️ hotro@cinestar.vn</li>
            <li>🎬 Facebook | YouTube | TikTok</li>
          </ul>
        </div>
      </div>
      <div className="footer-bottom">
        Copyright 2026 © CineStar Cinema Clone — Đồ án React + React Native + Spring Boot + PostgreSQL + Redis + Kafka + VNPAY
      </div>
    </footer>
  );
}
