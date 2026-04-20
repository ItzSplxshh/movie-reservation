import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import api from '../api/axios';

const statusBadge = {
  PENDING: 'badge-gray',
  CONFIRMED: 'badge-green',
  CANCELLED: 'badge-red',
  REFUNDED: 'badge-gray',
};

const statusLabel = {
  PENDING: 'Pending Payment',
  CONFIRMED: 'Confirmed',
  CANCELLED: 'Cancelled',
  REFUNDED: 'Refunded',
};

export default function MyReservationsPage() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(null);

  const fetchReservations = () => {
    api.get('/reservations/my')
      .then(res => setReservations(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchReservations(); }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this reservation?')) return;
    setCancelling(id);
    try {
      await api.delete(`/reservations/${id}`);
      fetchReservations();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel reservation.');
    } finally {
      setCancelling(null);
    }
  };

  if (loading) return <div className="loading-spinner" style={{ marginTop: '8rem' }} />;

  return (
    <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem', maxWidth: 800 }}>
      <div className="page-header">
        <h1 style={{ fontSize: '2.25rem' }}>My Tickets</h1>
      </div>

      {reservations.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-secondary)' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🎟️</div>
          <p style={{ marginBottom: '1.5rem' }}>No reservations yet.</p>
          <Link to="/movies" className="btn btn-primary">Browse Films</Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {reservations.filter(r => r.status !== 'CANCELLED').map(r => (
            <div key={r.id} style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '1.5rem', display: 'grid', gridTemplateColumns: '1fr auto', gap: '1rem', alignItems: 'start' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.6rem' }}>
                  <h3 style={{ fontFamily: 'DM Sans', fontWeight: 600, fontSize: '1rem' }}>
                    {r.showtime?.movie?.title || 'Unknown Movie'}
                  </h3>
                  <span className={`badge ${statusBadge[r.status] || 'badge-gray'}`}>
                    {statusLabel[r.status] || r.status}
                  </span>
                </div>

                {r.showtime?.startTime && (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>
                    📅 {format(new Date(r.showtime.startTime), "EEEE, MMMM d 'at' HH:mm")}
                  </p>
                )}

                {r.showtime?.theater?.name && (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>
                    🎭 {r.showtime.theater.name}
                  </p>
                )}

                {r.seats?.length > 0 && (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>
                    💺 {r.seats.map(s => `${s.rowLabel}${s.seatNumber}`).join(', ')}
                  </p>
                )}

                <p style={{ fontSize: '0.85rem', color: 'var(--accent)', fontWeight: 600, marginTop: '0.5rem' }}>
                  ${r.totalPrice?.toFixed(2)}
                </p>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', alignItems: 'flex-end' }}>
                {r.status === 'PENDING' && (
                  <Link to={`/checkout/${r.id}`} className="btn btn-primary" style={{ fontSize: '0.85rem', padding: '0.5rem 1rem' }}>
                    Complete Payment
                  </Link>
                )}
                {(r.status !== 'CANCELLED' && r.status !== 'REFUNDED') && (
                    <button
                        className="btn btn-ghost"
                        style={{ fontSize: '0.8rem', color: 'var(--danger)' }}
                        disabled={cancelling === r.id}
                        onClick={() => handleCancel(r.id)}
                    >
                      {cancelling === r.id ? 'Cancelling...' : 'Cancel'}
                    </button>
                )}
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                  #{r.id}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
