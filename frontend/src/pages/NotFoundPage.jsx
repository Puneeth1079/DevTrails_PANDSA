import React from 'react';
import { Link } from 'react-router-dom';
import { Shield } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen hero-gradient flex items-center justify-center text-white text-center p-6">
      <div>
        <div className="w-20 h-20 bg-white/10 rounded-3xl flex items-center justify-center mx-auto mb-6">
          <Shield size={40} className="text-white" />
        </div>
        <h1 className="text-8xl font-black mb-4 text-white/30">404</h1>
        <h2 className="text-2xl font-bold mb-2">Page not found</h2>
        <p className="text-white/70 mb-8">The page you're looking for doesn't exist.</p>
        <Link to="/" className="gs-btn-accent">← Back to GigShield</Link>
      </div>
    </div>
  );
}
