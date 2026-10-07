import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './store/auth';
import Navbar from './components/Navbar.jsx';
import Footer from './components/Footer.jsx';
import HomePage from './pages/HomePage.jsx';
import MovieDetailPage from './pages/MovieDetailPage.jsx';
import SeatSelectionPage from './pages/SeatSelectionPage.jsx';
import CheckoutPage from './pages/CheckoutPage.jsx';
import PaymentResultPage from './pages/PaymentResultPage.jsx';
import MyTicketsPage from './pages/MyTicketsPage.jsx';
import TicketDetailPage from './pages/TicketDetailPage.jsx';
import CinemasPage from './pages/CinemasPage.jsx';
import PromotionsPage from './pages/PromotionsPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import AdminPage from './pages/AdminPage.jsx';

const Protected = ({ children }) => {
  const accessToken = useAuth((s) => s.accessToken);
  return accessToken ? children : <Navigate to="/login" replace />;
};

const AdminOnly = ({ children }) => {
  const user = useAuth((s) => s.user);
  return user?.roles?.includes('ROLE_ADMIN') ? children : <Navigate to="/" replace />;
};

export default function App() {
  return (
    <div className="app">
      <Navbar />
      <main className="main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/movies/:slug" element={<MovieDetailPage />} />
          <Route path="/cinemas" element={<CinemasPage />} />
          <Route path="/promotions" element={<PromotionsPage />} />
          <Route path="/seats/:showtimeId" element={<Protected><SeatSelectionPage /></Protected>} />
          <Route path="/checkout/:bookingCode" element={<Protected><CheckoutPage /></Protected>} />
          <Route path="/payment/result" element={<PaymentResultPage />} />
          <Route path="/tickets" element={<Protected><MyTicketsPage /></Protected>} />
          <Route path="/tickets/:bookingCode" element={<Protected><TicketDetailPage /></Protected>} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/admin" element={<AdminOnly><AdminPage /></AdminOnly>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
