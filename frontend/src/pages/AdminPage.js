import React, { useEffect, useState } from 'react';
import { Routes, Route, NavLink, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import api from '../api/axios';
import './AdminPage.css';

// ─── Movies Tab ──────────────────────────────────────────────────────────────
function AdminMovies() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editMovie, setEditMovie] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const emptyForm = { title: '', description: '', genre: '', durationMinutes: '', director: '', cast: '', posterUrl: '', rating: '', releaseYear: '', status: 'NOW_SHOWING' };
  const [form, setForm] = useState(emptyForm);

  const fetchMovies = () => api.get('/movies').then(r => setMovies(r.data)).finally(() => setLoading(false));
  useEffect(() => { fetchMovies(); }, []);

  const openCreate = () => { setForm(emptyForm); setEditMovie(null); setShowForm(true); setError(''); };
  const openEdit = (m) => { setForm({ ...m, durationMinutes: m.durationMinutes || '' }); setEditMovie(m); setShowForm(true); setError(''); };

  const handleSubmit = async e => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const payload = { ...form, durationMinutes: parseInt(form.durationMinutes) || null, rating: parseFloat(form.rating) || null };
      if (editMovie) await api.put(`/movies/${editMovie.id}`, payload);
      else await api.post('/movies', payload);
      setShowForm(false);
      fetchMovies();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save movie.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this movie?')) return;
    await api.delete(`/movies/${id}`);
    fetchMovies();
  };

  if (loading) return <div className="loading-spinner" />;

  return (
    <div>
      <div className="admin-tab-header">
        <h2>Movies</h2>
        <button className="btn btn-primary" onClick={openCreate}>+ Add Movie</button>
      </div>

      {showForm && (
        <div className="admin-modal-overlay" onClick={() => setShowForm(false)}>
          <div className="admin-modal" onClick={e => e.stopPropagation()}>
            <h3>{editMovie ? 'Edit Movie' : 'Add Movie'}</h3>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="admin-form-grid">
                <div className="form-group"><label>Title *</label><input required value={form.title} onChange={e => setForm({...form, title: e.target.value})} /></div>
                <div className="form-group"><label>Genre</label><input value={form.genre} onChange={e => setForm({...form, genre: e.target.value})} /></div>
                <div className="form-group"><label>Duration (min)</label><input type="number" value={form.durationMinutes} onChange={e => setForm({...form, durationMinutes: e.target.value})} /></div>
                <div className="form-group"><label>Release Year</label><input value={form.releaseYear} onChange={e => setForm({...form, releaseYear: e.target.value})} /></div>
                <div className="form-group"><label>Director</label><input value={form.director} onChange={e => setForm({...form, director: e.target.value})} /></div>
                <div className="form-group"><label>Rating (0-10)</label><input type="number" step="0.1" min="0" max="10" value={form.rating} onChange={e => setForm({...form, rating: e.target.value})} /></div>
                <div className="form-group" style={{ gridColumn: '1/-1' }}><label>Cast</label><input value={form.cast} onChange={e => setForm({...form, cast: e.target.value})} /></div>
                <div className="form-group" style={{ gridColumn: '1/-1' }}><label>Poster URL</label><input value={form.posterUrl} onChange={e => setForm({...form, posterUrl: e.target.value})} /></div>
                <div className="form-group" style={{ gridColumn: '1/-1' }}><label>Description</label><textarea rows={3} style={{ resize:'vertical' }} value={form.description} onChange={e => setForm({...form, description: e.target.value})} /></div>
                <div className="form-group">
                  <label>Status</label>
                  <select value={form.status} onChange={e => setForm({...form, status: e.target.value})}>
                    <option value="NOW_SHOWING">Now Showing</option>
                    <option value="COMING_SOON">Coming Soon</option>
                    <option value="ENDED">Ended</option>
                  </select>
                </div>
              </div>
              <div style={{ display:'flex', gap:'0.75rem', justifyContent:'flex-end', marginTop:'1rem' }}>
                <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Saving...' : 'Save'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>Title</th><th>Genre</th><th>Duration</th><th>Status</th><th>Rating</th><th>Actions</th></tr></thead>
          <tbody>
            {movies.map(m => (
              <tr key={m.id}>
                <td style={{ fontWeight: 500 }}>{m.title}</td>
                <td>{m.genre || '—'}</td>
                <td>{m.durationMinutes ? `${m.durationMinutes} min` : '—'}</td>
                <td><span className={`badge ${m.status === 'NOW_SHOWING' ? 'badge-gold' : m.status === 'COMING_SOON' ? 'badge-gray' : 'badge-red'}`}>{m.status?.replace('_',' ')}</span></td>
                <td>{m.rating ? `★ ${m.rating}` : '—'}</td>
                <td>
                  <button className="btn btn-ghost" style={{ padding:'0.35rem 0.75rem', fontSize:'0.8rem' }} onClick={() => openEdit(m)}>Edit</button>
                  <button className="btn btn-ghost" style={{ padding:'0.35rem 0.75rem', fontSize:'0.8rem', color:'var(--danger)' }} onClick={() => handleDelete(m.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ─── Theaters Tab ─────────────────────────────────────────────────────────────
function AdminTheaters() {
  const [theaters, setTheaters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', totalRows: 10, seatsPerRow: 12 });
  const [submitting, setSubmitting] = useState(false);

  const fetchTheaters = () => api.get('/admin/theaters').then(r => setTheaters(r.data)).catch(console.error).finally(() => setLoading(false));
  useEffect(() => { fetchTheaters(); }, []);

  const handleSubmit = async e => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/admin/theaters', { ...form, totalRows: parseInt(form.totalRows), seatsPerRow: parseInt(form.seatsPerRow) });
      setShowForm(false);
      setForm({ name: '', totalRows: 10, seatsPerRow: 12 });
      fetchTheaters();
    } finally { setSubmitting(false); }
  };

  if (loading) return <div className="loading-spinner" />;

  return (
    <div>
      <div className="admin-tab-header">
        <h2>Theaters</h2>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Add Theater</button>
      </div>

      {showForm && (
        <div className="admin-inline-form">
          <form onSubmit={handleSubmit} style={{ display:'flex', gap:'1rem', alignItems:'flex-end', flexWrap:'wrap' }}>
            <div className="form-group" style={{ marginBottom:0 }}><label>Name *</label><input required value={form.name} onChange={e => setForm({...form, name: e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0, width:120 }}><label>Rows</label><input type="number" min={1} max={30} value={form.totalRows} onChange={e => setForm({...form, totalRows: e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0, width:140 }}><label>Seats/Row</label><input type="number" min={1} max={30} value={form.seatsPerRow} onChange={e => setForm({...form, seatsPerRow: e.target.value})} /></div>
            <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Saving...' : 'Save'}</button>
            <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
          </form>
        </div>
      )}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>Name</th><th>Rows</th><th>Seats/Row</th><th>Total Seats</th></tr></thead>
          <tbody>
            {theaters.map(t => (
              <tr key={t.id}>
                <td style={{ fontWeight: 500 }}>{t.name}</td>
                <td>{t.totalRows}</td>
                <td>{t.seatsPerRow}</td>
                <td>{t.totalRows * t.seatsPerRow}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ─── Showtimes Tab ────────────────────────────────────────────────────────────
function AdminShowtimes() {
  const [showtimes, setShowtimes] = useState([]);
  const [movies, setMovies] = useState([]);
  const [theaters, setTheaters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ movieId: '', theaterId: '', startTime: '', ticketPrice: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const fetchAll = () => {
    Promise.all([
      api.get('/admin/showtimes'),
      api.get('/movies'),
      api.get('/admin/theaters'),
    ]).then(([sRes, mRes, tRes]) => {
      setShowtimes(sRes.data);
      setMovies(mRes.data);
      setTheaters(tRes.data);
    }).catch(console.error).finally(() => setLoading(false));
  };
  useEffect(() => { fetchAll(); }, []);

  const handleSubmit = async e => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await api.post('/showtimes', { movieId: parseInt(form.movieId), theaterId: parseInt(form.theaterId), startTime: form.startTime, ticketPrice: parseFloat(form.ticketPrice) });
      setShowForm(false);
      fetchAll();
    } catch(err) {
      setError(err.response?.data?.message || 'Failed.');
    } finally { setSubmitting(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this showtime?')) return;
    await api.delete(`/showtimes/${id}`);
    fetchAll();
  };

  if (loading) return <div className="loading-spinner" />;

  return (
    <div>
      <div className="admin-tab-header">
        <h2>Showtimes</h2>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Add Showtime</button>
      </div>

      {showForm && (
        <div className="admin-inline-form">
          {error && <div className="alert alert-error">{error}</div>}
          <form onSubmit={handleSubmit} style={{ display:'flex', gap:'1rem', alignItems:'flex-end', flexWrap:'wrap' }}>
            <div className="form-group" style={{ marginBottom:0, minWidth:160 }}>
              <label>Movie *</label>
              <select required value={form.movieId} onChange={e => setForm({...form, movieId: e.target.value})}>
                <option value="">Select movie</option>
                {movies.map(m => <option key={m.id} value={m.id}>{m.title}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom:0, minWidth:140 }}>
              <label>Theater *</label>
              <select required value={form.theaterId} onChange={e => setForm({...form, theaterId: e.target.value})}>
                <option value="">Select theater</option>
                {theaters.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom:0 }}><label>Start Time *</label><input type="datetime-local" required value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})} /></div>
            <div className="form-group" style={{ marginBottom:0, width:120 }}><label>Price ($) *</label><input type="number" step="0.01" min="0" required value={form.ticketPrice} onChange={e => setForm({...form, ticketPrice: e.target.value})} /></div>
            <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Saving...' : 'Save'}</button>
            <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
          </form>
        </div>
      )}

      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>Movie</th><th>Theater</th><th>Start Time</th><th>Price</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            {showtimes.map(st => (
              <tr key={st.id}>
                <td style={{ fontWeight:500 }}>{st.movie?.title}</td>
                <td>{st.theater?.name}</td>
                <td>{st.startTime && format(new Date(st.startTime), "MMM d, HH:mm")}</td>
                <td>${st.ticketPrice}</td>
                <td><span className="badge badge-gray">{st.status}</span></td>
                <td><button className="btn btn-ghost" style={{ padding:'0.35rem 0.75rem', fontSize:'0.8rem', color:'var(--danger)' }} onClick={() => handleDelete(st.id)}>Delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ─── Main Admin Layout ────────────────────────────────────────────────────────
export default function AdminPage() {
  const navigate = useNavigate();
  const tabs = [
    { label: 'Movies', path: '/admin' },
    { label: 'Theaters', path: '/admin/theaters' },
    { label: 'Showtimes', path: '/admin/showtimes' },
  ];

  return (
    <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem' }}>
      <div className="admin-header">
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Admin Panel</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Manage your cinema</p>
        </div>
      </div>

      <div className="admin-tabs">
        {tabs.map(tab => (
          <NavLink
            key={tab.path}
            to={tab.path}
            end={tab.path === '/admin'}
            className={({ isActive }) => `admin-tab ${isActive ? 'admin-tab--active' : ''}`}
          >
            {tab.label}
          </NavLink>
        ))}
      </div>

      <div className="admin-content">
        <Routes>
          <Route index element={<AdminMovies />} />
          <Route path="theaters" element={<AdminTheaters />} />
          <Route path="showtimes" element={<AdminShowtimes />} />
        </Routes>
      </div>
    </div>
  );
}
