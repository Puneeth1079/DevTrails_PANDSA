import React, { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Shield, Eye, EyeOff, ArrowRight } from 'lucide-react';

function OtpInput({ onComplete }) {
  const [values, setValues] = useState(['', '', '', '', '', '']);
  const refs = useRef([]);

  const handleChange = (i, e) => {
    const val = e.target.value.replace(/\D/g, '').slice(-1);
    const newVals = [...values];
    newVals[i] = val;
    setValues(newVals);
    if (val && i < 5) refs.current[i + 1]?.focus();
    if (newVals.every(v => v)) onComplete(newVals.join(''));
  };

  const handleKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !values[i] && i > 0) refs.current[i - 1]?.focus();
  };

  const handlePaste = (e) => {
    const paste = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (paste.length === 6) {
      const newVals = paste.split('');
      setValues(newVals);
      refs.current[5]?.focus();
      onComplete(paste);
    }
    e.preventDefault();
  };

  return (
    <div className="flex gap-3 justify-center" onPaste={handlePaste}>
      {values.map((v, i) => (
        <input key={i} ref={el => refs.current[i] = el}
          type="text" inputMode="numeric" maxLength={1}
          value={v} onChange={e => handleChange(i, e)}
          onKeyDown={e => handleKeyDown(i, e)}
          className={`otp-input ${v ? 'filled' : ''}`} />
      ))}
    </div>
  );
}

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('password'); // 'password' | 'otp'
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [otpSent, setOtpSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);

  const startCountdown = () => {
    setCountdown(30);
    const timer = setInterval(() => setCountdown(c => { if (c <= 1) { clearInterval(timer); return 0; } return c - 1; }), 1000);
  };

  const sendOtp = async () => {
    if (!mobile || mobile.length !== 10) return toast.error('Enter valid 10-digit mobile number');
    setLoading(true);
    try {
      await api.post('/api/auth/send-otp', { mobile });
      setOtpSent(true);
      startCountdown();
      toast.success('OTP sent to +91 ' + mobile);
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to send OTP');
    } finally { setLoading(false); }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!mobile) return toast.error('Mobile required');
    setLoading(true);
    try {
      const payload = tab === 'otp'
        ? { mobile, otp, otpLogin: true }
        : { mobile, password, otpLogin: false };
      const res = await api.post('/api/auth/login', payload);
      const { token, role, name, userId, workerId } = res.data.data;
      login({ role, name, mobile, userId, workerId }, token);
      toast.success(`Welcome back, ${name}! 👋`);
      navigate(role === 'ADMIN' ? '/admin/dashboard' : '/worker/dashboard');
    } catch (e) {
      toast.error(e.response?.data?.message || 'Login failed');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left — branding */}
      <div className="hidden lg:flex lg:w-1/2 hero-gradient items-center justify-center p-12">
        <div className="text-center text-white">
          <div className="w-20 h-20 bg-white/20 rounded-3xl flex items-center justify-center mx-auto mb-6 backdrop-blur">
            <Shield size={40} className="text-white" />
          </div>
          <h1 className="text-4xl font-extrabold mb-4">GigShield</h1>
          <p className="text-white/80 text-lg max-w-xs">India's first parametric income insurance for food delivery workers.</p>
          <div className="mt-8 space-y-3">
            {['Zero paperwork', 'Auto-triggered payouts', '5-minute UPI transfers'].map((f, i) => (
              <div key={i} className="flex items-center gap-2 text-white/80 text-sm">
                <span className="text-accent">✓</span> {f}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right — form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 bg-bg-light">
        <div className="w-full max-w-md">
          <div className="gs-card">
            <div className="text-center mb-8">
              <div className="flex lg:hidden items-center justify-center gap-2 mb-4">
                <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center">
                  <Shield size={20} className="text-white" />
                </div>
                <span className="font-black text-dark text-2xl">GigShield</span>
              </div>
              <h2 className="text-2xl font-extrabold text-dark">Welcome back</h2>
              <p className="text-slate text-sm mt-1">Sign in to your account</p>
            </div>

            {/* Tabs */}
            <div className="flex bg-bg-light rounded-xl p-1 mb-6">
              {['password', 'otp'].map(t => (
                <button key={t} onClick={() => setTab(t)}
                  className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${tab === t ? 'bg-white text-primary shadow-sm' : 'text-slate'}`}>
                  {t === 'password' ? '🔒 Password' : '📱 OTP Login'}
                </button>
              ))}
            </div>

            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="gs-label">Mobile Number</label>
                <div className="flex">
                  <span className="flex items-center px-3 bg-bg-light border-l border-t border-b border-border rounded-l-xl text-slate text-sm font-semibold">+91</span>
                  <input type="tel" maxLength={10} value={mobile} onChange={e => setMobile(e.target.value.replace(/\D/, ''))}
                    placeholder="9876543210" className="gs-input rounded-l-none border-l-0" />
                </div>
              </div>

              {tab === 'password' ? (
                <div>
                  <label className="gs-label">Password</label>
                  <div className="relative">
                    <input type={showPassword ? 'text' : 'password'} value={password}
                      onChange={e => setPassword(e.target.value)} placeholder="Enter your password"
                      className="gs-input pr-10" />
                    <button type="button" onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate">
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  {!otpSent ? (
                    <button type="button" onClick={sendOtp} disabled={loading}
                      className="w-full gs-btn-outline">
                      {loading ? '⏳ Sending...' : 'Send OTP'}
                    </button>
                  ) : (
                    <div>
                      <label className="gs-label mb-3">Enter OTP</label>
                      <OtpInput onComplete={setOtp} />
                      <div className="text-center mt-3">
                        {countdown > 0 ? (
                          <span className="text-xs text-slate">Resend in {countdown}s</span>
                        ) : (
                          <button type="button" onClick={sendOtp} className="text-xs text-primary font-semibold">Resend OTP</button>
                        )}
                      </div>
                    </div>
                  )}
                </>
              )}

              <button type="submit" disabled={loading} className="w-full gs-btn-primary mt-2">
                {loading ? '⏳ Signing in...' : 'Sign In →'}
              </button>
            </form>

            <p className="text-center text-sm text-slate mt-4">
              New to GigShield? <Link to="/register" className="text-primary font-semibold hover:underline">Register here</Link>
            </p>
            <p className="text-center text-xs text-slate/60 mt-3">
              Admin portal: use email credentials at login
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
