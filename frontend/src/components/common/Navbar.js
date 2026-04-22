import React, { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [highContrast, setHighContrast] = useState(false);
  const [fontSize, setFontSize] = useState('normal');

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    document.body.classList.toggle('high-contrast', highContrast);
  }, [highContrast]);

  useEffect(() => {
    document.body.classList.remove('font-large', 'font-xlarge');
    if (fontSize === 'large') document.body.classList.add('font-large');
    if (fontSize === 'xlarge') document.body.classList.add('font-xlarge');
  }, [fontSize]);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  const cycleFontSize = () => {
    setFontSize(prev => prev === 'normal' ? 'large' : prev === 'large' ? 'xlarge' : 'normal');
  };

  const fontSizeLabel = fontSize === 'normal' ? 'A' : fontSize === 'large' ? 'A+' : 'A++';

  return (
      <>

        <nav
            className={`navbar ${scrolled ? 'navbar--scrolled' : ''}`}
            role="navigation"
            aria-label="Main navigation"
        >
          <div className="container navbar__inner">
            <Link to="/" className="navbar__logo" aria-label="CineVault home">
              <span className="navbar__logo-icon" aria-hidden="true">🎬</span>
              <span className="navbar__logo-text">CineVault</span>
            </Link>

            <button
                className="navbar__burger"
                onClick={() => setMenuOpen(!menuOpen)}
                aria-label={menuOpen ? 'Close menu' : 'Open menu'}
                aria-expanded={menuOpen}
                aria-controls="navbar-links"
            >
              <span aria-hidden="true" /><span aria-hidden="true" /><span aria-hidden="true" />
            </button>

            <div
                id="navbar-links"
                className={`navbar__links ${menuOpen ? 'navbar__links--open' : ''}`}
                role="menubar"
            >
              <NavLink
                  to="/movies"
                  className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'}
                  onClick={() => setMenuOpen(false)}
                  role="menuitem"
              >
                Browse Films
              </NavLink>

              {user ? (
                  <>
                    <NavLink
                        to="/my-reservations"
                        className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'}
                        onClick={() => setMenuOpen(false)}
                        role="menuitem"
                    >
                      My Tickets
                    </NavLink>
                    {isAdmin && (
                        <NavLink
                            to="/admin"
                            className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'}
                            onClick={() => setMenuOpen(false)}
                            role="menuitem"
                        >
                          Admin
                        </NavLink>
                    )}
                    <div className="navbar__user">
                  <span className="navbar__user-name" aria-label={`Logged in as ${user.fullName?.split(' ')[0]}`}>
                    {user.fullName?.split(' ')[0]}
                  </span>
                      <button
                          className="btn btn-ghost"
                          onClick={handleLogout}
                          aria-label="Sign out of your account"
                      >
                        Sign Out
                      </button>
                    </div>
                  </>
              ) : (
                  <div className="navbar__auth">
                    <Link to="/login" className="btn btn-ghost" onClick={() => setMenuOpen(false)} aria-label="Sign in to your account">
                      Sign In
                    </Link>
                    <Link to="/register" className="btn btn-primary" onClick={() => setMenuOpen(false)} aria-label="Create a new account">
                      Join Now
                    </Link>
                  </div>
              )}

              {/* Accessibility controls */}
              <div className="navbar__a11y" role="group" aria-label="Accessibility controls">
                <button
                    className="btn btn-ghost navbar__a11y-btn"
                    onClick={() => setHighContrast(!highContrast)}
                    aria-label={highContrast ? 'Disable high contrast mode' : 'Enable high contrast mode'}
                    aria-pressed={highContrast}
                    title="Toggle high contrast"
                >
                  {highContrast ? '◑' : '○'}
                </button>
                <button
                    className="btn btn-ghost navbar__a11y-btn"
                    onClick={cycleFontSize}
                    aria-label={`Font size: ${fontSize}. Click to increase.`}
                    title="Change font size"
                >
                  {fontSizeLabel}
                </button>
              </div>
            </div>
          </div>
        </nav>
      </>
  );
}