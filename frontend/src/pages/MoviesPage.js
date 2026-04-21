import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import MovieCard from '../components/movies/MovieCard';

export default function MoviesPage() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchParams, setSearchParams] = useSearchParams();
  const activeStatus = searchParams.get('status') || 'ALL';

  useEffect(() => {
    setLoading(true);
    const url = activeStatus === 'ALL' ? '/movies' : `/movies?status=${activeStatus}`;
    api.get(url)
        .then(res => setMovies(res.data))
        .catch(console.error)
        .finally(() => setLoading(false));
  }, [activeStatus]);

  const filters = [
    { label: 'All Films', value: 'ALL' },
    { label: 'Now Showing', value: 'NOW_SHOWING' },
    { label: 'Coming Soon', value: 'COMING_SOON' },
  ];

  const filteredMovies = movies.filter(m =>
      m.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      m.genre?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
      <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem' }}>
        <div className="page-header">
          <h1 style={{ fontSize: '2.5rem', marginBottom: '1.5rem' }}>Browse Films</h1>

          {/* Search bar */}
          <input
              type="text"
              placeholder="Search by title or genre..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem 1rem',
                borderRadius: '8px',
                border: '1px solid var(--border)',
                background: 'var(--bg-card)',
                color: 'var(--text-primary)',
                fontSize: '0.95rem',
                marginBottom: '1rem',
                outline: 'none',
              }}
          />

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {filters.map(f => (
                <button
                    key={f.value}
                    className={`btn ${activeStatus === f.value ? 'btn-primary' : 'btn-ghost'}`}
                    onClick={() => setSearchParams(f.value === 'ALL' ? {} : { status: f.value })}
                >
                  {f.label}
                </button>
            ))}
          </div>
        </div>

        {loading ? (
            <div className="loading-spinner" />
        ) : filteredMovies.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-secondary)' }}>
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎬</div>
              <p>{searchQuery ? `No movies found for "${searchQuery}"` : 'No movies found.'}</p>
            </div>
        ) : (
            <div className="grid-movies">
              {filteredMovies.map(m => <MovieCard key={m.id} movie={m} />)}
            </div>
        )}
      </div>
  );
}
