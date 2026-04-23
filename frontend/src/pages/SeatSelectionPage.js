import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import api from '../api/axios';
import SeatMap from '../components/seats/SeatMap';

export default function SeatSelectionPage() {
  const { showtimeId } = useParams();
  const navigate = useNavigate();

  const [showtime, setShowtime] = useState(null);
  const [allSeats, setAllSeats] = useState([]);
  const [availableSeats, setAvailableSeats] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.get(`/showtimes/${showtimeId}`),
      api.get(`/seats/showtime/${showtimeId}/all`),     // All seats for the theater (for seat map display)
      api.get(`/reservations/seats/${showtimeId}`),      // Available seats only (to determine which are taken)
    ]).then(([stRes, allRes, availableRes]) => {
      setShowtime(stRes.data);
      setAllSeats(allRes.data);                          // All seats shown on map (available + taken)
      setAvailableSeats(availableRes.data);              // Available seats used to grey out taken seats
    }).catch(err => {
      // Fallback: show only available seats if all seats fetch fails
      console.error(err);
    }).finally(() => setLoading(false));
  }, [showtimeId]);

  // Re-derive all seats from showtime theater if needed
  useEffect(() => {
    if (showtime && availableSeats.length > 0 && allSeats.length === 0) {
      setAllSeats(availableSeats);
    }
  }, [showtime, availableSeats, allSeats]);

  const toggleSeat = (seatId) => {
    setSelectedIds(prev =>
      prev.includes(seatId) ? prev.filter(id => id !== seatId) : [...prev, seatId]
    );
  };

  const handleProceed = async () => {
    if (selectedIds.length === 0) return;
    setSubmitting(true);
    setError('');
    try {
      const { data: reservation } = await api.post('/reservations', {
        showtimeId: parseInt(showtimeId),
        seatIds: selectedIds,
      });
      navigate(`/checkout/${reservation.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create reservation. Seats may have been taken.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading-spinner" style={{ marginTop: '8rem' }} />;
  if (!showtime) return <div className="container" style={{ paddingTop: '8rem' }}><p>Showtime not found.</p></div>;

  const VIP_SURCHARGE = 3;
  const getSeatPrice = (seat) => seat?.type === 'VIP'
      ? showtime.ticketPrice + VIP_SURCHARGE
      : showtime.ticketPrice;
  const totalPrice = selectedIds.reduce((sum, id) => {
    const seat = [...allSeats, ...availableSeats].find(s => s.id === id);
    return sum + getSeatPrice(seat);
  }, 0);
  const availableIds = availableSeats.map(s => s.id);

  return (
    <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginBottom: '1rem' }}>
          ← Back
        </button>
        <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>
          {showtime.movie?.title}
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          {format(new Date(showtime.startTime), "EEEE, MMMM d 'at' HH:mm")} · {showtime.theater?.name}
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '2rem', alignItems: 'start' }}>
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '2rem' }}>
          <SeatMap
            allSeats={allSeats.length > 0 ? allSeats : availableSeats}
            availableSeatIds={availableIds}
            selectedIds={selectedIds}
            onToggle={toggleSeat}
          />
        </div>

        {/* Summary */}
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '1.5rem', position: 'sticky', top: '6rem' }}>
          <h3 style={{ fontFamily: 'DM Sans', fontWeight: 600, marginBottom: '1.25rem' }}>Order Summary</h3>

          {selectedIds.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
              Select your seats from the map.
            </p>
          ) : (
            <div style={{ marginBottom: '1.5rem' }}>
              {selectedIds.map(id => {
                const seat = [...allSeats, ...availableSeats].find(s => s.id === id);
                return seat ? (
                    <div key={id} style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      padding: '0.5rem 0',
                      borderBottom: '1px solid var(--border)',
                      fontSize: '0.875rem'
                    }}>
                    <span style={{color: 'var(--text-secondary)'}}>
                      Seat {seat.rowLabel}{seat.seatNumber}
                      {seat.type === 'VIP' && <span className="badge badge-gray"
                                                    style={{marginLeft: '0.5rem', fontSize: '0.65rem'}}>VIP</span>}
                    </span>
                      <span style={{color: 'var(--accent)', fontWeight: 600}}>
  ${getSeatPrice(seat).toFixed(2)}
                        {seat?.type === 'VIP' &&
                            <span style={{fontSize: '0.72rem', marginLeft: '0.3rem', opacity: 0.7}}>+$3 VIP</span>}
</span>
                    </div>
                ) : null;
              })}
            </div>
          )}

          <div
              style={{display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', paddingTop: '0.75rem'}}>
            <span style={{ fontWeight: 600 }}>Total</span>
            <span style={{ fontWeight: 700, fontSize: '1.25rem', color: 'var(--accent)' }}>
              ${totalPrice.toFixed(2)}
            </span>
          </div>

          <button
            className="btn btn-primary"
            style={{ width: '100%' }}
            disabled={selectedIds.length === 0 || submitting}
            onClick={handleProceed}
          >
            {submitting ? 'Processing...' : `Proceed to Payment (${selectedIds.length} seat${selectedIds.length !== 1 ? 's' : ''})`}
          </button>
        </div>
      </div>
    </div>
  );
}
