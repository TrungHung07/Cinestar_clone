import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import client, { errorMessage } from '../api/client.js';
import { useAuth } from '../store/auth.js';

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const login = useAuth((s) => s.login);
  const navigate = useNavigate();
  const location = useLocation();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await client.post('/api/v1/auth/login', form);
      login(res.data.data);
      navigate(location.state?.from || '/');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <form className="auth-form" onSubmit={submit}>
        <h1>Đăng nhập</h1>
        <input
          placeholder="Tên đăng nhập"
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Mật khẩu"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />
        {error && <p className="alert error">{error}</p>}
        <button className="btn btn-primary btn-lg" disabled={loading}>
          {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </button>
        <p className="muted">
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </p>
        <p className="muted small">Demo: user1/user123 · admin/admin123</p>
      </form>
    </div>
  );
}
