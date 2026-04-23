import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

export default function AccountSettingsPage() {
    const { user } = useAuth();

    const [profile, setProfile] = useState({ firstName: '', lastName: '' });
    const [profileSuccess, setProfileSuccess] = useState('');
    const [profileError, setProfileError] = useState('');
    const [profileSubmitting, setProfileSubmitting] = useState(false);

    const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
    const [passwordSuccess, setPasswordSuccess] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [passwordSubmitting, setPasswordSubmitting] = useState(false);

    useEffect(() => {
        api.get('/users/profile').then(res => {
            setProfile({ firstName: res.data.firstName, lastName: res.data.lastName });
        }).catch(console.error);
    }, []);

    const handleProfileSubmit = async e => {
        e.preventDefault();
        setProfileSubmitting(true);
        setProfileError('');
        setProfileSuccess('');
        try {
            await api.put('/users/profile', profile);
            setProfileSuccess('Profile updated successfully!');
        } catch (err) {
            setProfileError(err.response?.data?.message || 'Failed to update profile.');
        } finally {
            setProfileSubmitting(false);
        }
    };

    const handlePasswordSubmit = async e => {
        e.preventDefault();
        setPasswordSubmitting(true);
        setPasswordError('');
        setPasswordSuccess('');
        try {
            await api.put('/users/password', passwords);
            setPasswordSuccess('Password changed successfully!');
            setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' });
        } catch (err) {
            setPasswordError(err.response?.data?.message || 'Failed to change password.');
        } finally {
            setPasswordSubmitting(false);
        }
    };

    return (
        <div className="container" style={{ paddingTop: '6rem', paddingBottom: '4rem', maxWidth: 600 }}>
            <div className="page-header">
                <h1 style={{ fontSize: '2.25rem' }}>Account Settings</h1>
                <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    Manage your profile and security settings
                </p>
            </div>

            {/* Profile Section */}
            <div style={{
                background: 'var(--bg-card)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius)',
                padding: '1.75rem',
                marginBottom: '1.5rem'
            }}>
                <h2 style={{ fontSize: '1.25rem', marginBottom: '1.5rem', fontFamily: 'DM Sans' }}>
                    👤 Personal Information
                </h2>

                {profileSuccess && <div className="alert alert-success">{profileSuccess}</div>}
                {profileError && <div className="alert alert-error">{profileError}</div>}

                <form onSubmit={handleProfileSubmit}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div className="form-group">
                            <label>First Name</label>
                            <input
                                value={profile.firstName}
                                onChange={e => setProfile({ ...profile, firstName: e.target.value })}
                                placeholder="First name"
                            />
                        </div>
                        <div className="form-group">
                            <label>Last Name</label>
                            <input
                                value={profile.lastName}
                                onChange={e => setProfile({ ...profile, lastName: e.target.value })}
                                placeholder="Last name"
                            />
                        </div>
                    </div>
                    <div className="form-group">
                        <label>Email Address</label>
                        <input value={user?.email || ''} disabled style={{ opacity: 0.6, cursor: 'not-allowed' }} />
                        <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Email cannot be changed</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
                        <button type="submit" className="btn btn-primary" disabled={profileSubmitting}>
                            {profileSubmitting ? 'Saving...' : 'Save Changes'}
                        </button>
                    </div>
                </form>
            </div>

            {/* Password Section */}
            <div style={{
                background: 'var(--bg-card)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius)',
                padding: '1.75rem'
            }}>
                <h2 style={{ fontSize: '1.25rem', marginBottom: '1.5rem', fontFamily: 'DM Sans' }}>
                    🔒 Change Password
                </h2>

                {passwordSuccess && <div className="alert alert-success">{passwordSuccess}</div>}
                {passwordError && <div className="alert alert-error">{passwordError}</div>}

                <form onSubmit={handlePasswordSubmit}>
                    <div className="form-group">
                        <label>Current Password</label>
                        <input
                            type="password"
                            value={passwords.currentPassword}
                            onChange={e => setPasswords({ ...passwords, currentPassword: e.target.value })}
                            placeholder="Enter current password"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>New Password</label>
                        <input
                            type="password"
                            value={passwords.newPassword}
                            onChange={e => setPasswords({ ...passwords, newPassword: e.target.value })}
                            placeholder="Enter new password"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Confirm New Password</label>
                        <input
                            type="password"
                            value={passwords.confirmPassword}
                            onChange={e => setPasswords({ ...passwords, confirmPassword: e.target.value })}
                            placeholder="Confirm new password"
                            required
                        />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
                        <button type="submit" className="btn btn-primary" disabled={passwordSubmitting}>
                            {passwordSubmitting ? 'Changing...' : 'Change Password'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}