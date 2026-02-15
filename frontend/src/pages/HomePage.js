import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import MovieCard from '../components/movies/MovieCard';
import './HomePage.css';

export default function HomePage() {
  const [nowShowing, setNowShowing] = useState([]);
  const [comingSoon, setComingSoon] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/movies?status=NOW_SHOWING'),
      api.get('/movies?status=COMING_SOON'),
    ]).then(([nowRes, soonRes]) => {
      setNowShowing(nowRes.data);
      setComingSoon(soonRes.data);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="home">
      {/* Hero */}
      <section className="hero">
        <div className="hero__bg" />
        <div className="container hero__content">
          <p className="hero__eyebrow">Premium Cinema Experience</p>
          <h1 className="hero__title">Your seat.<br/>Your story.</h1>
          <p className="hero__sub">Browse the latest films, pick your perfect seat, and skip the queue.</p>
          <div className="hero__cta">
            <Link to="/movies" className="btn btn-primary hero__btn">Browse Films</Link>
            <Link to="/movies?status=COMING_SOON" className="btn btn-outline">Coming Soon</Link>
          </div>
        </div>
        <div className="hero__dots" />
      </section>

      <div className="container home__content">
        {loading ? (
          <div className="loading-spinner" />
        ) : (
          <>
            {nowShowing.length > 0 && (
              <section className="home__section">
                <div className="home__section-header">
                  <h2>Now Showing</h2>
                  <Link to="/movies?status=NOW_SHOWING" className="btn btn-ghost">See all →</Link>
                </div>
                <div className="grid-movies">
                  {nowShowing.slice(0, 8).map(m => <MovieCard key={m.id} movie={m} />)}
                </div>
              </section>
            )}

            {comingSoon.length > 0 && (
              <section className="home__section">
                <div className="home__section-header">
                  <h2>Coming Soon</h2>
                  <Link to="/movies?status=COMING_SOON" className="btn btn-ghost">See all →</Link>
                </div>
                <div className="grid-movies">
                  {comingSoon.slice(0, 4).map(m => <MovieCard key={m.id} movie={m} />)}
                </div>
              </section>
            )}

            {nowShowing.length === 0 && comingSoon.length === 0 && (
              <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-secondary)' }}>
                <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎬</div>
                <p>No movies available yet. Check back soon!</p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
