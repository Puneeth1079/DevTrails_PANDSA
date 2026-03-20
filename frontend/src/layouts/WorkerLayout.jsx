import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, FileText, ClipboardList, User, LogOut,
  Shield, Menu, X, Bell, Home
} from 'lucide-react';

const navItems = [
  { to: '/worker/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/worker/policies', label: 'My Policies', icon: FileText },
  { to: '/worker/claims', label: 'Claims', icon: ClipboardList },
  { to: '/worker/profile', label: 'Profile', icon: User },
];

export default function WorkerLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-bg-light flex">
      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-border shadow-lg
                         transform transition-transform duration-300
                         ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:relative lg:translate-x-0`}>
        {/* Logo */}
        <div className="p-6 border-b border-border">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center">
              <Shield size={20} className="text-white" />
            </div>
            <div>
              <h1 className="font-bold text-dark text-lg">GigShield</h1>
              <p className="text-xs text-slate">Income Protection</p>
            </div>
          </div>
        </div>

        {/* User Info */}
        <div className="p-4 mx-4 mt-4 bg-primary/5 rounded-xl">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-primary rounded-full flex items-center justify-center text-white font-bold text-sm">
              {user?.name?.[0] || 'W'}
            </div>
            <div className="overflow-hidden">
              <p className="font-semibold text-dark text-sm truncate">{user?.name || 'Worker'}</p>
              <p className="text-xs text-slate">{user?.mobile}</p>
            </div>
          </div>
        </div>

        {/* Nav Links */}
        <nav className="p-4 space-y-1 mt-2">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-all duration-200 ${
                  isActive ? 'bg-primary/10 text-primary font-semibold' : 'text-slate hover:bg-primary/5 hover:text-primary'
                }`}>
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="absolute bottom-4 left-0 right-0 px-4">
          <NavLink to="/" className="flex items-center gap-3 px-4 py-3 rounded-xl text-slate hover:text-primary hover:bg-primary/5 font-medium transition-all mb-1">
            <Home size={18} /> Home
          </NavLink>
          <button onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-danger hover:bg-danger/5 font-medium transition-all">
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      {/* Overlay */}
      {sidebarOpen && <div className="fixed inset-0 z-40 bg-black/40 lg:hidden" onClick={() => setSidebarOpen(false)} />}

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-h-screen lg:ml-0">
        {/* Top bar */}
        <header className="bg-white border-b border-border px-6 py-4 flex items-center justify-between sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="lg:hidden text-slate hover:text-primary">
            <Menu size={24} />
          </button>
          <div className="hidden lg:flex items-center gap-2 text-sm text-slate">
            <Shield size={14} className="text-primary" />
            <span className="font-semibold text-dark">GigShield</span>
            <span>/ Worker Portal</span>
          </div>
          <div className="flex items-center gap-3">
            <button className="relative p-2 text-slate hover:text-primary transition-colors">
              <Bell size={20} />
              <span className="absolute top-0 right-0 w-2 h-2 bg-accent rounded-full"></span>
            </button>
            <div className="w-9 h-9 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center text-white font-bold text-sm">
              {user?.name?.[0] || 'W'}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <nav className="lg:hidden mobile-bottom-nav">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                isActive ? 'text-primary' : 'text-slate'
              }`}>
            <Icon size={20} />
            {label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
