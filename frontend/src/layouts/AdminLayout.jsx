import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Users, ClipboardList, FileText, Zap,
  Shield, Menu, LogOut, Settings, Bell
} from 'lucide-react';

const navItems = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/workers', label: 'Workers', icon: Users },
  { to: '/admin/claims', label: 'Claims', icon: ClipboardList },
  { to: '/admin/policies', label: 'Policies', icon: FileText },
  { to: '/admin/triggers', label: 'Triggers', icon: Zap },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="min-h-screen bg-bg-light flex">
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-dark text-white
                         transform transition-transform duration-300
                         ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:relative lg:translate-x-0`}>
        <div className="p-6 border-b border-white/10">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center">
              <Shield size={20} className="text-white" />
            </div>
            <div>
              <h1 className="font-bold text-white text-lg">GigShield</h1>
              <span className="text-xs bg-accent/20 text-accent px-2 py-0.5 rounded-full">Admin Panel</span>
            </div>
          </div>
        </div>

        <div className="p-4 mx-4 mt-4 bg-white/5 rounded-xl">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-primary rounded-full flex items-center justify-center text-white font-bold text-sm">
              {user?.name?.[0] || 'A'}
            </div>
            <div>
              <p className="font-semibold text-white text-sm">{user?.name || 'Admin'}</p>
              <p className="text-xs text-white/50">Super Admin</p>
            </div>
          </div>
        </div>

        <nav className="p-4 space-y-1 mt-2">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-all ${
                  isActive ? 'bg-primary text-white' : 'text-white/60 hover:bg-white/10 hover:text-white'
                }`}>
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="absolute bottom-4 left-0 right-0 px-4">
          <button onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-white/60 hover:bg-red-500/20 hover:text-red-400 font-medium transition-all">
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      {sidebarOpen && <div className="fixed inset-0 z-40 bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />}

      <div className="flex-1 flex flex-col">
        <header className="bg-white border-b border-border px-6 py-4 flex items-center justify-between sticky top-0 z-30">
          <button onClick={() => setSidebarOpen(true)} className="lg:hidden text-slate">
            <Menu size={24} />
          </button>
          <div className="hidden lg:block">
            <h2 className="font-bold text-dark text-lg">Admin Control Center</h2>
            <p className="text-xs text-slate">GigShield Operations Dashboard</p>
          </div>
          <div className="flex items-center gap-3">
            <button className="relative p-2 text-slate hover:text-primary">
              <Bell size={20} />
              <span className="absolute top-0 right-0 w-2 h-2 bg-danger rounded-full animate-ping-slow"></span>
            </button>
          </div>
        </header>

        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
