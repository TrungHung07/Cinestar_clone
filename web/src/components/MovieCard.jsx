import React from 'react';
import { useNavigate } from 'react-router-dom';
import { imgUrl } from '../api/client.js';

const ageColor = (rating) => {
  if (!rating || rating === 'CHUA_PHAN_LOAI') return 'gray';
  if (rating === 'P' || rating === 'K') return 'orange';
  return ''; // T13/T16/T18 -> đỏ mặc định
};

/** Thẻ phim kiểu Cinestar: badge 2 phần góc trên trái + tên in hoa + Trailer/ĐẶT VÉ */
export default function MovieCard({ movie }) {
  const navigate = useNavigate();

  return (
    <div className="movie-card">
      <div className="poster-wrap" onClick={() => navigate(`/movies/${movie.slug}`)} style={{ cursor: 'pointer' }}>
        <img src={imgUrl(movie.posterUrl)} alt={movie.title} loading="lazy" />
        <span className="age-badge-wrap">
          <span className="fmt">{movie.format || '2D'}</span>
          <span className={`age ${ageColor(movie.ageRating)}`}>{movie.ageRating || 'C'}</span>
        </span>
        {movie.rating && <span className="rating-badge">★ {movie.rating}</span>}
      </div>
      <div className="movie-card-body">
        <h3 title={movie.title}>{movie.title} {movie.ageRating ? `(${movie.ageRating})` : ''}</h3>
        <div className="movie-actions">
          {movie.trailerUrl ? (
            <a className="trailer-link" href={movie.trailerUrl} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()}>
              <span className="play-icon">▶</span> Xem Trailer
            </a>
          ) : <span />}
          <button className="btn-datve" onClick={() => navigate(`/movies/${movie.slug}`)}>
            Đặt vé
          </button>
        </div>
      </div>
    </div>
  );
}
