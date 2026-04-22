import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import './MovieDetailPage.css';

export default function MovieDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [movie, setMovie] = useState(null);
  const [showtimes, setShowtimes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState('ALL');

  useEffect(() => {
    Promise.all([
      api.get(`/movies/${id}`),
      api.get(`/showtimes/movie/${id}`),
    ]).then(([mRes, sRes]) => {
      setMovie(mRes.data);
      setShowtimes(sRes.data);
    }).catch(console.error)
        .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading-spinner" style={{ marginTop: '8rem' }} />;
  if (!movie) return <div className="container" style={{ paddingTop: '8rem' }}><p>Movie not found.</p></div>;

  const handleBookShowtime = (showtime) => {
    if (!user) {
      navigate(`/login?redirect=/select-seats/${showtime.id}`);
    } else {
      navigate(`/select-seats/${showtime.id}`);
    }
  };

  // Group showtimes by date
  const byDate = showtimes.reduce((acc, st) => {
    const date = format(new Date(st.startTime), 'yyyy-MM-dd');
    if (!acc[date]) acc[date] = [];
    acc[date].push(st);
    return acc;
  }, {});

  // Get unique dates for filter
  const uniqueDates = Object.keys(byDate).sort();

  // Filter by selected date
  const filteredByDate = selectedDate === 'ALL'
      ? byDate
      : { [selectedDate]: byDate[selectedDate] || [] };

  return (
      <div className="movie-detail">
        {/* Backdrop */}
        {movie.posterUrl && (
            <div className="movie-detail__backdrop">
              <img src={movie.posterUrl} alt="" aria-hidden />
            </div>
        )}

        <div className="container movie-detail__layout">
          {/* Poster */}
          <div className="movie-detail__poster-col">
            {movie.posterUrl ? (
                <img src={movie.posterUrl} alt={movie.title} className="movie-detail__poster" />
            ) : (
                <div className="movie-detail__poster-placeholder">🎬</div>
            )}
          </div>

          {/* Info */}
          <div className="movie-detail__info-col">
            <div className="movie-detail__meta-tags">
              {movie.genre && <span className="badge badge-gray">{movie.genre}</span>}
              {movie.releaseYear && <span className="badge badge-gray">{movie.releaseYear}</span>}
              {movie.durationMinutes && <span className="badge badge-gray">{movie.durationMinutes} min</span>}
            </div>

            <h1 className="movie-detail__title">{movie.title}</h1>

            {movie.rating && (
                <div className="movie-detail__rating">
                  <span className="movie-detail__star">★</span>
                  <span className="movie-detail__rating-val">{movie.rating}</span>
                  <span className="movie-detail__rating-max">/10</span>
                </div>
            )}

            {movie.description && (
                <p className="movie-detail__desc">{movie.description}</p>
            )}

            <div className="movie-detail__credits">
              {movie.director && <div><span className="credit-label">Director</span><span className="credit-val">{movie.director}</span></div>}
              {movie.cast && <div><span className="credit-label">Cast</span><span className="credit-val">{movie.cast}</span></div>}
            </div>

            {/* Showtimes */}
            <div className="movie-detail__showtimes">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.75rem' }}>
                <h3 className="movie-detail__showtimes-title" style={{ marginBottom: 0 }}>Book Tickets</h3>
                {uniqueDates.length > 1 && (
                    <select
                        value={selectedDate}
                        onChange={e => setSelectedDate(e.target.value)}
                        style={{
                          width: 'auto',
                          padding: '0.4rem 0.75rem',
                          fontSize: '0.85rem',
                          borderRadius: '8px',
                          background: 'var(--bg-elevated)',
                          border: '1px solid var(--border)',
                          color: 'var(--text-primary)',
                          cursor: 'pointer',
                        }}
                        aria-label="Filter showtimes by date"
                    >
                      <option value="ALL">All Dates</option>
                      {uniqueDates.map(date => (
                          <option key={date} value={date}>
                            {format(new Date(date), 'EEE, MMM d')}
                          </option>
                      ))}
                    </select>
                )}
              </div>

              {Object.keys(filteredByDate).length === 0 ? (
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No upcoming showtimes available.</p>
              ) : (
                  Object.entries(filteredByDate).map(([date, times]) => (
                      <div key={date} className="showtime-day">
                        <p className="showtime-date">{format(new Date(date), 'EEEE, MMMM d')}</p>
                        <div className="showtime-slots">
                          {times.map(st => (
                              <button
                                  key={st.id}
                                  className="showtime-slot"
                                  onClick={() => handleBookShowtime(st)}
                              >
                        <span className="showtime-slot__time">
                          {format(new Date(st.startTime), 'HH:mm')}
                        </span>
                                <span className="showtime-slot__price">
                          ${st.ticketPrice}
                        </span>
                                <span className="showtime-slot__theater">
                          {st.theater?.name}
                        </span>
                              </button>
                          ))}
                        </div>
                      </div>
                  ))
              )}
            </div>
          </div>
        </div>
      </div>
  );
}