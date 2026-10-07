import React, { useEffect, useState } from 'react';
import client, { imgUrl } from '../api/client.js';

export default function CinemasPage() {
  const [cinemas, setCinemas] = useState([]);

  useEffect(() => {
    client.get('/api/v1/cinemas').then((res) => setCinemas(res.data.data));
  }, []);

  return (
    <div className="page">
      <h1>Hệ thống rạp Cinestar</h1>
      <div className="cinema-grid">
        {cinemas.map((c) => (
          <div className="cinema-card" key={c.id}>
            <img src={imgUrl(c.imageUrl)} alt={c.name} loading="lazy" />
            <div>
              <h3>{c.name}</h3>
              <p className="muted">{c.address}, {c.city}</p>
              <p className="muted">Hotline: {c.hotline} · {c.roomCount} phòng chiếu</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
