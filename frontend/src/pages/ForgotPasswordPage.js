import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await api.post('/auth/forgot-password', { email });
            setSuccess(true);
        } catch (err) {
            setError(err.response?.data || 'Failed to send reset email.');
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="container" style={{ paddingTop: '8rem', textAlign: 'center', maxWidth: 500 }}>
                <div style={{ fontSize: '4rem', marginBottom: '1.5rem' }}>📧</div>
                <h1 style={{ marginBottom: '1rem' }}>Check Your Email</h1>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                    We've sent a password reset link to <strong>{email}</strong>.
                    Check your inbox and click the link to reset your password.
                </p>
                <Link to="/login" className="btn btn-primary">Back to Login</Link>
            </div>
        );
    }

    return (
        <div className="container" style={{ paddingTop: '8rem', maxWidth: 440 }}>
            <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Forgot Password</h1>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                Enter your email and we'll send you a reset link.
            </p>

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '1.25rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                        Email Address
                    </label>
                    <input
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                        placeholder="your@email.com"
                        style={{
                            width: '100%',
                            padding: '0.75rem 1rem',
                            borderRadius: '8px',
                            border: '1px solid var(--border)',
                            background: 'var(--bg-card)',
                            color: 'var(--text-primary)',
                            fontSize: '0.95rem',
                        }}
                    />
                </div>

                {error && <div className="alert alert-error" style={{ marginBottom: '1rem' }}>{error}</div>}

                <button
                    type="submit"
                    className="btn btn-primary"
                    style={{ width: '100%', padding: '0.85rem', fontSize: '1rem' }}
                    disabled={loading}
                >
                    {loading ? 'Sending...' : 'Send Reset Link'}
                </button>
            </form>

            <p style={{ textAlign: 'center', marginTop: '1.5rem', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                Remember your password? <Link to="/login" style={{ color: 'var(--accent)' }}>Log in</Link>
            </p>
        </div>
    );
}