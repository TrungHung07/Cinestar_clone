import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client, { errorMessage } from '../api/client.js';
import { useAuth } from '../store/auth.js';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '', phone: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const login = useAuth((s) => s.login);
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await client.post('/api/v1/auth/register', form);
      login(res.data.data);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <form className="auth-form" onSubmit={submit}>
        <h1>Đăng ký</h1>
        <input placeholder="Họ và tên" value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
        <input placeholder="Tên đăng nhập" value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        <input type="email" placeholder="Email" value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        <input placeholder="Số điện thoại (0xxxxxxxxx)" value={form.phone}
          onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        <input type="password" placeholder="Mật khẩu (tối thiểu 6 ký tự)" value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        {error && <p className="alert error">{error}</p>}
        <button className="btn btn-primary btn-lg" disabled={loading}>
          {loading ? 'Đang tạo tài khoản...' : 'Đăng ký'}
        </button>
        <p className="muted">Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
      </form>
    </div>
  );
}
