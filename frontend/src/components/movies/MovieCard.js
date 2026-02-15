import React from 'react';
import { Link } from 'react-router-dom';
import './MovieCard.css';

export default function MovieCard({ movie }) {
  const statusColors = {
    NOW_SHOWING: 'badge-gold',
    COMING_SOON: 'badge-gray',
    ENDED: 'badge-red',
  };

  const statusLabels = {
    NOW_SHOWING: 'Now Showing',
    COMING_SOON: 'Coming Soon',
    ENDED: 'Ended',
  };

  return (
    <Link to={`/movies/${movie.id}`} className="movie-card">
      <div className="movie-card__poster-wrap">
        {movie.posterUrl ? (
          <img src={movie.posterUrl} alt={movie.title} className="movie-card__poster" />
        ) : (
          <div className="movie-card__poster-placeholder">
            <span>🎬</span>
          </div>
        )}
        <div className="movie-card__overlay">
          <span className="movie-card__play">▶</span>
        </div>
        <div className="movie-card__badge-wrap">
          <span className={`badge ${statusColors[movie.status] || 'badge-gray'}`}>
            {statusLabels[movie.status] || movie.status}
          </span>
        </div>
      </div>
      <div className="movie-card__info">
        <h3 className="movie-card__title">{movie.title}</h3>
        <div className="movie-card__meta">
          {movie.genre && <span className="movie-card__genre">{movie.genre}</span>}
          {movie.durationMinutes && <span className="movie-card__duration">{movie.durationMinutes} min</span>}
        </div>
        {movie.rating && (
          <div className="movie-card__rating">
            <span className="movie-card__star">★</span>
            <span>{movie.rating}</span>
          </div>
        )}
      </div>
    </Link>
  );
}
