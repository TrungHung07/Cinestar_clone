import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import client from '../api/client.js';

/**
 * VNPAY redirect ve day voi day tham so (vnp_ResponseCode, vnp_TxnRef...).
 * Truyen nguyen bo tham so sang backend /payments/vnpay/return de xac minh
 * chu ky HMAC-SHA512 va cap nhat trang thai don.
 */
export default function PaymentResultPage() {
  const location = useLocation();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const query = location.search.startsWith('?') ? location.search.substring(1) : location.search;
    if (!query.includes('vnp_')) {
      setLoading(false);
      setResult({ success: false, message: 'Thiếu thông tin thanh toán' });
      return;
    }
    client
      .get(`/api/v1/payments/vnpay/return?${query}`)
      .then((res) => setResult(res.data))
      .catch((e) => setResult({ success: false, message: e?.response?.data?.message || 'Lỗi xác minh thanh toán' }))
      .finally(() => setLoading(false));
  }, [location.search]);

  if (loading) return <p className="page muted">Đang xác minh kết quả thanh toán...</p>;

  return (
    <div className="page">
      <div className="section booking-box narrow payment-result">
        {result?.success ? (
          <>
            <div className="result-icon ok">✔</div>
            <h2>Thanh toán thành công!</h2>
            <p>{result.message}</p>
            <div className="btn-row">
              <Link className="btn btn-primary" to={`/tickets/${result.data.bookingCode}`}>Xem vé của bạn</Link>
              <Link className="btn btn-outline" to="/tickets">Vé của tôi</Link>
            </div>
          </>
        ) : (
          <>
            <div className="result-icon fail">✘</div>
            <h2>Thanh toán chưa thành công</h2>
            <p>{result?.message}</p>
            <Link className="btn btn-outline" to="/">Về trang chủ</Link>
          </>
        )}
      </div>
    </div>
  );
}
