import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';

// Lazy-load pages for performance
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import WorkerLayout from './layouts/WorkerLayout';
import AdminLayout from './layouts/AdminLayout';
import WorkerDashboard from './pages/worker/WorkerDashboard';
import MyPolicies from './pages/worker/MyPolicies';
import ClaimsHistory from './pages/worker/ClaimsHistory';
import WorkerProfile from './pages/worker/WorkerProfile';
import AdminDashboard from './pages/admin/AdminDashboard';
import WorkersManagement from './pages/admin/WorkersManagement';
import ClaimsManagement from './pages/admin/ClaimsManagement';
import TriggerConfig from './pages/admin/TriggerConfig';
import PolicyManagement from './pages/admin/PolicyManagement';
import NotFoundPage from './pages/NotFoundPage';

function ProtectedRoute({ children, role }) {
  const { isAuthenticated, user, loading } = useAuth();

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center hero-gradient">
      <div className="text-center text-white">
        <div className="w-16 h-16 border-4 border-white/30 border-t-white rounded-full animate-spin mx-auto mb-4"></div>
        <p className="text-lg font-semibold">Loading GigShield...</p>
      </div>
    </div>
  );

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (role && user?.role !== role) return <Navigate to="/" replace />;
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Worker Protected */}
      <Route path="/worker" element={
        <ProtectedRoute role="WORKER"><WorkerLayout /></ProtectedRoute>
      }>
        <Route index element={<Navigate to="/worker/dashboard" />} />
        <Route path="dashboard" element={<WorkerDashboard />} />
        <Route path="policies" element={<MyPolicies />} />
        <Route path="claims" element={<ClaimsHistory />} />
        <Route path="profile" element={<WorkerProfile />} />
      </Route>

      {/* Admin Protected */}
      <Route path="/admin" element={
        <ProtectedRoute role="ADMIN"><AdminLayout /></ProtectedRoute>
      }>
        <Route index element={<Navigate to="/admin/dashboard" />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="workers" element={<WorkersManagement />} />
        <Route path="claims" element={<ClaimsManagement />} />
        <Route path="policies" element={<PolicyManagement />} />
        <Route path="triggers" element={<TriggerConfig />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
        <Toaster
          position="top-right"
          toastOptions={{
            style: { borderRadius: '12px', fontFamily: 'Plus Jakarta Sans', fontWeight: 600 },
            success: { style: { background: '#2E7D32', color: 'white' } },
            error: { style: { background: '#C62828', color: 'white' } },
          }}
        />
      </BrowserRouter>
    </AuthProvider>
  );
}
