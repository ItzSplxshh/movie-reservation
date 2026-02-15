import React, { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  return (
    <nav className={`navbar ${scrolled ? 'navbar--scrolled' : ''}`}>
      <div className="container navbar__inner">
        <Link to="/" className="navbar__logo">
          <span className="navbar__logo-icon">🎬</span>
          <span className="navbar__logo-text">CineVault</span>
        </Link>

        <button className="navbar__burger" onClick={() => setMenuOpen(!menuOpen)}>
          <span /><span /><span />
        </button>

        <div className={`navbar__links ${menuOpen ? 'navbar__links--open' : ''}`}>
          <NavLink to="/movies" className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'} onClick={() => setMenuOpen(false)}>
            Browse Films
          </NavLink>

          {user ? (
            <>
              <NavLink to="/my-reservations" className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'} onClick={() => setMenuOpen(false)}>
                My Tickets
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className={({isActive}) => isActive ? 'navbar__link navbar__link--active' : 'navbar__link'} onClick={() => setMenuOpen(false)}>
                  Admin
                </NavLink>
              )}
              <div className="navbar__user">
                <span className="navbar__user-name">{user.fullName?.split(' ')[0]}</span>
                <button className="btn btn-ghost" onClick={handleLogout}>Sign Out</button>
              </div>
            </>
          ) : (
            <div className="navbar__auth">
              <Link to="/login" className="btn btn-ghost" onClick={() => setMenuOpen(false)}>Sign In</Link>
              <Link to="/register" className="btn btn-primary" onClick={() => setMenuOpen(false)}>Join Now</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
