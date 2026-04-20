import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { format } from 'date-fns';
import api from '../api/axios';

const stripePromise = loadStripe('pk_test_51T0zI0RxkMNeF2Bvp9kqwyLmmCXT0dv6fLgBkPEaICsLnX1KmKE1WeUp6JuiIFcXgtkdUF8jAYVIURoPblwPphYk005bKLboAr');

const stripeAppearance = {
  theme: 'night',
  variables: {
    colorPrimary: '#e8b04b',
    colorBackground: '#1e1e2a',
    colorText: '#f0ece4',
    colorDanger: '#e05260',
    fontFamily: 'DM Sans, sans-serif',
    borderRadius: '8px',
  },
};

function CountdownTimer({ heldUntil, onExpired }) {
  const calculateTimeLeft = useCallback(() => {
    const diff = new Date(heldUntil + 'Z') - new Date();
    return Math.max(0, Math.floor(diff / 1000));
  }, [heldUntil]);

  const [secondsLeft, setSecondsLeft] = useState(calculateTimeLeft);

  useEffect(() => {
    if (!heldUntil) return;
    const interval = setInterval(() => {
      const remaining = calculateTimeLeft();
      setSecondsLeft(remaining);
      if (remaining <= 0) {
        clearInterval(interval);
        onExpired();
      }
    }, 1000);
    return () => clearInterval(interval);
  }, [heldUntil, calculateTimeLeft, onExpired]);

  const minutes = Math.floor(secondsLeft / 60);
  const seconds = secondsLeft % 60;
  const isWarning = secondsLeft <= 120;
  const isCritical = secondsLeft <= 60;

  return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        padding: '0.75rem 1rem',
        borderRadius: '8px',
        marginBottom: '1.5rem',
        background: isCritical ? 'rgba(224, 82, 96, 0.15)' : isWarning ? 'rgba(232, 176, 75, 0.15)' : 'rgba(255,255,255,0.05)',
        border: `1px solid ${isCritical ? '#e05260' : isWarning ? '#e8b04b' : 'var(--border)'}`,
        transition: 'all 0.3s ease',
      }}>
        <span style={{ fontSize: '1.2rem' }}>{isCritical ? '🚨' : isWarning ? '⚠️' : '⏱️'}</span>
        <div>
          <p style={{
            margin: 0,
            fontWeight: 600,
            color: isCritical ? '#e05260' : isWarning ? '#e8b04b' : 'var(--text-primary)',
            fontSize: '0.95rem',
          }}>
            {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')} remaining
          </p>
          <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            {isCritical ? 'Your seats are about to be released!' : isWarning ? 'Complete your payment soon!' : 'Your seats are being held for you'}
          </p>
        </div>
      </div>
  );
}

function CheckoutForm({ reservation, onSuccess, expired }) {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState('');
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements || expired) return;
    setProcessing(true);
    setError('');
    const result = await stripe.confirmPayment({
      elements,
      confirmParams: { return_url: `${window.location.origin}/my-reservations` },
      redirect: 'if_required',
    });
    if (result.error) {
      setError(result.error.message);
      setProcessing(false);
    } else if (result.paymentIntent?.status === 'succeeded') {
      onSuccess();
    }
  };

  return (
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1.5rem' }}>
          <PaymentElement />
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', fontSize: '1rem', padding: '1rem' }}
            disabled={!stripe || processing || expired}
        >
          {processing ? 'Processing Payment...' : `Pay $${reservation?.totalPrice?.toFixed(2)}`}
        </button>
        <p style={{ textAlign: 'center', marginTop: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          🔒 Secured by Stripe. Your card details are never stored.
        </p>
      </form>
  );
}

export default function CheckoutPage() {
  const { reservationId } = useParams();
  const navigate = useNavigate();

  const [reservation, setReservation] = useState(null);
  const [clientSecret, setClientSecret] = useState('');
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [expired, setExpired] = useState(false);

  useEffect(() => {
    Promise.all([
      api.get(`/reservations/${reservationId}`),
      api.post(`/payments/create-intent/${reservationId}`),
    ]).then(([resRes, payRes]) => {
      setReservation(resRes.data);
      setClientSecret(payRes.data.clientSecret);
    }).catch(err => {
      setError(err.response?.data?.message || 'Failed to initialize checkout.');
    }).finally(() => setLoading(false));
  }, [reservationId]);

  const handleExpired = useCallback(() => {
    setExpired(true);
  }, []);

  useEffect(() => {
    if (!expired) return;
    const timeout = setTimeout(() => {
      navigate('/movies', { state: { message: 'Your seat hold expired. Please select seats again.' } });
    }, 3000);
    return () => clearTimeout(timeout);
  }, [expired, navigate]);

  if (loading) return <div className="loading-spinner" style={{ marginTop: '8rem' }} />;

  if (success) {
    return (
        <div className="container" style={{ paddingTop: '8rem', textAlign: 'center', maxWidth: 500 }}>
          <div style={{ fontSize: '4rem', marginBottom: '1.5rem' }}>🎉</div>
          <h1 style={{ marginBottom: '1rem' }}>Booking Confirmed!</h1>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
            Your tickets have been booked successfully. You'll receive a confirmation shortly.
          </p>
          <button className="btn btn-primary" onClick={() => navigate('/my-reservations')}>
            View My Tickets
          </button>
        </div>
    );
  }

  if (expired) {
    return (
        <div className="container" style={{ paddingTop: '8rem', textAlign: 'center', maxWidth: 500 }}>
          <div style={{ fontSize: '4rem', marginBottom: '1.5rem' }}>⏰</div>
          <h1 style={{ marginBottom: '1rem' }}>Your Hold Has Expired</h1>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
            Your seats have been released. Redirecting you to choose seats again...
          </p>
          <div className="loading-spinner" />
        </div>
    );
  }

  if (error) {
    return (
        <div className="container" style={{ paddingTop: '8rem', maxWidth: 500 }}>
          <div className="alert alert-error">{error}</div>
          <button className="btn btn-ghost" onClick={() => navigate(-1)}>← Go Back</button>
        </div>
    );
  }

  const showtime = reservation?.showtime;

  return (
      <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem', maxWidth: 900 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
          <button className="btn btn-ghost" onClick={() => navigate(-1)}>
            ← Back
          </button>
          <button
              className="btn btn-ghost"
              style={{ color: 'var(--danger)' }}
              onClick={async () => {
                try {
                  await api.delete(`/reservations/${reservationId}`);
                } catch (err) {
                  console.error('Failed to cancel:', err);
                }
                navigate('/movies');
              }}
          >
            Cancel Reservation
          </button>
        </div>

        <h1 style={{ fontSize: '2rem', marginBottom: '2rem' }}>Complete Your Booking</h1>

        {reservation?.heldUntil && (
            <CountdownTimer heldUntil={reservation.heldUntil} onExpired={handleExpired} />
        )}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '2rem', alignItems: 'start' }}>
          <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '2rem' }}>
            <h3 style={{ fontFamily: 'DM Sans', fontWeight: 600, marginBottom: '1.5rem' }}>Payment Details</h3>
            {clientSecret && (
                <Elements stripe={stripePromise} options={{ clientSecret, appearance: stripeAppearance }}>
                  <CheckoutForm reservation={reservation} onSuccess={() => setSuccess(true)} expired={expired} />
                </Elements>
            )}
          </div>

          <div style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            padding: '1.75rem',
            position: 'sticky',
            top: '6rem'
          }}>
            <h3 style={{fontFamily: 'DM Sans', fontWeight: 600, marginBottom: '1.25rem'}}>Booking Summary</h3>
            <div style={{
              display: 'flex',
              gap: '1rem',
              alignItems: 'flex-start',
              marginBottom: '1.5rem',
              paddingBottom: '1.5rem',
              borderBottom: '1px solid var(--border)'
            }}>
              {showtime?.movie?.posterUrl && (
                  <img src={showtime.movie.posterUrl} alt={showtime.movie.title}
                       style={{width: 64, borderRadius: 8, flexShrink: 0}}/>
              )}
              <div>
                <p style={{fontWeight: 600, marginBottom: '0.25rem'}}>{showtime?.movie?.title}</p>
                <p style={{fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: '0.15rem'}}>
                  {showtime?.startTime && format(new Date(showtime.startTime), "MMM d, HH:mm")}
                </p>
                <p style={{fontSize: '0.82rem', color: 'var(--text-muted)'}}>{showtime?.theater?.name}</p>
              </div>
            </div>
            <div style={{marginBottom: '1.5rem'}}>
              <p style={{
                fontSize: '0.8rem',
                color: 'var(--text-muted)',
                marginBottom: '0.75rem',
                textTransform: 'uppercase',
                letterSpacing: '0.08em',
                fontWeight: 600
              }}>Seats</p>
              {reservation?.seats?.map(seat => (
                  <div key={seat.id} style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    fontSize: '0.875rem',
                    padding: '0.4rem 0'
                  }}>
                    <span style={{color: 'var(--text-secondary)'}}>Row {seat.rowLabel}, Seat {seat.seatNumber}</span>
                    <span style={{color: 'var(--accent)', fontWeight: 600}}>${showtime?.ticketPrice}</span>
                  </div>
              ))}
            </div>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              paddingTop: '1rem',
              borderTop: '1px solid var(--border)'
            }}>
              <span style={{fontWeight: 700, fontSize: '1rem'}}>Total</span>
              <span style={{fontWeight: 700, fontSize: '1.4rem', color: 'var(--accent)'}}>
              ${reservation?.totalPrice?.toFixed(2)}
            </span>
            </div>
            <button
                className="btn btn-ghost"
                style={{width: '100%', marginTop: '1rem', color: 'var(--danger)'}}
                onClick={async () => {
                  try {
                    await api.delete(`/reservations/${reservationId}`);
                  } catch (err) {
                    console.error('Failed to cancel:', err);
                  }
                  navigate('/movies');
                }}
            >
              Cancel Reservation
            </button>
          </div>
        </div>
      </div>
  );
}