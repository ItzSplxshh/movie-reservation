import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import HomePage from './pages/HomePage';
import MoviesPage from './pages/MoviesPage';
import MovieDetailPage from './pages/MovieDetailPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import CheckoutPage from './pages/CheckoutPage';
import MyReservationsPage from './pages/MyReservationsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminPage from './pages/AdminPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import AccountSettingsPage from "./pages/AccountSettingsPage";

function PrivateRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

function AdminRoute({ children }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/" replace />;
  return children;
}

function AppRoutes() {
  return (
      <>
        <Navbar />
        <main id="main-content" tabIndex="-1">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/movies" element={<MoviesPage />} />
            <Route path="/movies/:id" element={<MovieDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/account" element={<PrivateRoute><AccountSettingsPage /></PrivateRoute>} />
            <Route path="/select-seats/:showtimeId" element={
              <PrivateRoute><SeatSelectionPage /></PrivateRoute>
            } />
            <Route path="/checkout/:reservationId" element={
              <PrivateRoute><CheckoutPage /></PrivateRoute>
            } />
            <Route path="/my-reservations" element={
              <PrivateRoute><MyReservationsPage /></PrivateRoute>
            } />
            <Route path="/admin/*" element={
              <AdminRoute><AdminPage /></AdminRoute>
            } />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </>
  );
}

export default function App() {
  return (
      <BrowserRouter>
        <AuthProvider>
          <a href="#main-content" className="skip-link">Skip to main content</a>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
  );
}