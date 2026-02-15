import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { format } from 'date-fns';
import api from '../api/axios';

// Replace with your Stripe publishable key
const stripePromise = loadStripe('pk_test_51T0zICRpQCzaDXNuj6Y625142cAmQ64N9F7Bm7mvghQTx1JZdSP9hnUNfMs4ZRZsgUMWmjWxvqwrFzCtxKB1PFdg00pdphh6w2');

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

function CheckoutForm({ reservation, onSuccess }) {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState('');
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setProcessing(true);
    setError('');

    const result = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/my-reservations`,
      },
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
        disabled={!stripe || processing}
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
      <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginBottom: '1.5rem' }}>
        ← Back
      </button>

      <h1 style={{ fontSize: '2rem', marginBottom: '2rem' }}>Complete Your Booking</h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '2rem', alignItems: 'start' }}>
        {/* Payment form */}
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '2rem' }}>
          <h3 style={{ fontFamily: 'DM Sans', fontWeight: 600, marginBottom: '1.5rem' }}>Payment Details</h3>
          {clientSecret && (
            <Elements stripe={stripePromise} options={{ clientSecret, appearance: stripeAppearance }}>
              <CheckoutForm reservation={reservation} onSuccess={() => setSuccess(true)} />
            </Elements>
          )}
        </div>

        {/* Order summary */}
        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius)', padding: '1.75rem', position: 'sticky', top: '6rem' }}>
          <h3 style={{ fontFamily: 'DM Sans', fontWeight: 600, marginBottom: '1.25rem' }}>Booking Summary</h3>

          <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start', marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--border)' }}>
            {showtime?.movie?.posterUrl && (
              <img src={showtime.movie.posterUrl} alt={showtime.movie.title}
                style={{ width: 64, borderRadius: 8, flexShrink: 0 }} />
            )}
            <div>
              <p style={{ fontWeight: 600, marginBottom: '0.25rem' }}>{showtime?.movie?.title}</p>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: '0.15rem' }}>
                {showtime?.startTime && format(new Date(showtime.startTime), "MMM d, HH:mm")}
              </p>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{showtime?.theater?.name}</p>
            </div>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 600 }}>Seats</p>
            {reservation?.seats?.map(seat => (
              <div key={seat.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', padding: '0.4rem 0' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Row {seat.rowLabel}, Seat {seat.seatNumber}</span>
                <span style={{ color: 'var(--accent)', fontWeight: 600 }}>${showtime?.ticketPrice}</span>
              </div>
            ))}
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
            <span style={{ fontWeight: 700, fontSize: '1rem' }}>Total</span>
            <span style={{ fontWeight: 700, fontSize: '1.4rem', color: 'var(--accent)' }}>
              ${reservation?.totalPrice?.toFixed(2)}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
