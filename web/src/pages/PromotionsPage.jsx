import React, { useEffect, useState } from 'react';
import client, { imgUrl } from '../api/client.js';

export default function PromotionsPage() {
  const [promotions, setPromotions] = useState([]);

  useEffect(() => {
    client.get('/api/v1/promotions').then((res) => setPromotions(res.data.data));
  }, []);

  return (
    <div className="page">
      <h1>Khuyến mãi</h1>
      <div className="promo-grid">
        {promotions.map((p) => (
          <div className="promo-card big" key={p.id}>
            <img src={imgUrl(p.imageUrl)} alt={p.title} loading="lazy" />
            <div className="promo-body">
              <h3>{p.title}</h3>
              <p className="muted">{p.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
