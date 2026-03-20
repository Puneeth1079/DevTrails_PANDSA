import React, { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Shield, Eye, EyeOff, ChevronRight, ChevronLeft, Check } from 'lucide-react';

const CITIES = ['Mumbai', 'Delhi', 'Bengaluru', 'Chennai', 'Hyderabad', 'Pune', 'Kolkata', 'Ahmedabad', 'Jaipur', 'Surat', 'Others'];

const STEPS = ['Mobile OTP', 'Personal Info', 'Work Details', 'Review'];

function OtpInput({ onComplete }) {
  const [values, setValues] = useState(['', '', '', '', '', '']);
  const refs = useRef([]);
  const handleChange = (i, e) => {
    const val = e.target.value.replace(/\D/g, '').slice(-1);
    const newVals = [...values];
    newVals[i] = val;
    setValues(newVals);
    if (val && i < 5) refs.current[i + 1]?.focus();
    if (newVals.every(v => v !== '')) onComplete(newVals.join(''));
  };
  const handleKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !values[i] && i > 0) refs.current[i - 1]?.focus();
  };
  const handlePaste = (e) => {
    const paste = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (paste.length === 6) { setValues(paste.split('')); refs.current[5]?.focus(); onComplete(paste); }
    e.preventDefault();
  };
  return (
    <div className="flex gap-2 justify-center" onPaste={handlePaste}>
      {values.map((v, i) => (
        <input key={i} ref={el => refs.current[i] = el} type="text" inputMode="numeric" maxLength={1}
          value={v} onChange={e => handleChange(i, e)} onKeyDown={e => handleKeyDown(i, e)}
          className={`otp-input ${v ? 'filled' : ''}`} />
      ))}
    </div>
  );
}

export default function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [otpSent, setOtpSent] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const [form, setForm] = useState({
    mobile: '', otp: '',
    name: '', email: '', password: '', confirmPassword: '',
    platform: 'ZOMATO', platformPartnerId: '',
    city: 'Mumbai', zone: '', pincode: '',
    avgDailyEarnings: 800, avgDailyHours: 8,
    upiId: '', bankAccount: '', ifsc: '',
  });

  const handleChange = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const startCountdown = () => {
    setCountdown(30);
    const t = setInterval(() => setCountdown(c => { if (c <= 1) { clearInterval(t); return 0; } return c - 1; }), 1000);
  };

  const sendOtp = async () => {
    if (!form.mobile || form.mobile.length !== 10) { toast.error('Enter valid 10-digit mobile'); return; }
    setLoading(true);
    try {
      await api.post('/api/auth/send-otp', { mobile: form.mobile });
      setOtpSent(true);
      startCountdown();
      toast.success('OTP sent to +91 ' + form.mobile);
    } catch (e) { toast.error(e.response?.data?.message || 'Failed to send OTP'); }
    finally { setLoading(false); }
  };

  const verifyOtp = async () => {
    if (!form.otp || form.otp.length !== 6) { toast.error('Enter 6-digit OTP'); return; }
    setStep(1);
    toast.success('Mobile verified ✓');
  };

  const handleStep2 = () => {
    if (!form.name) return toast.error('Name is required');
    if (!form.password) return toast.error('Password is required');
    if (form.password !== form.confirmPassword) return toast.error('Passwords do not match');
    setStep(2);
  };

  const handleStep3 = () => {
    if (!form.city) return toast.error('City is required');
    setStep(3);
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const res = await api.post('/api/auth/register', { ...form, otp: form.otp });
      const { token, role, name, userId, workerId } = res.data.data;
      login({ role, name, mobile: form.mobile, userId, workerId }, token);
      toast.success('Welcome to GigShield! 🎉');
      navigate('/worker/dashboard');
    } catch (e) { toast.error(e.response?.data?.message || 'Registration failed'); }
    finally { setLoading(false); }
  };

  // Estimated premium preview
  const estimatePremium = () => {
    let base = 65;
    if (['Mumbai', 'Delhi', 'Chennai'].includes(form.city)) base *= 1.2;
    else if (['Bengaluru', 'Hyderabad'].includes(form.city)) base *= 1.1;
    const month = new Date().getMonth() + 1;
    if (month >= 6 && month <= 9) base *= 1.15;
    return Math.round(base);
  };

  const riskScore = () => {
    let score = 50;
    if (['Mumbai', 'Delhi', 'Chennai'].includes(form.city)) score += 15;
    if (form.avgDailyEarnings < 600) score += 10;
    if (form.avgDailyEarnings > 1200) score -= 10;
    return Math.min(100, Math.max(0, score));
  };

  return (
    <div className="min-h-screen bg-bg-light py-8 px-4">
      <div className="max-w-xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-6">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center">
              <Shield size={20} className="text-white" />
            </div>
            <span className="font-black text-dark text-2xl">GigShield</span>
          </Link>
          <h1 className="text-2xl font-extrabold text-dark">Create Your Account</h1>
          <p className="text-slate text-sm mt-1">Start protecting your income in 2 minutes</p>
        </div>

        {/* Step Progress */}
        <div className="flex items-center justify-between mb-8">
          {STEPS.map((s, i) => (
            <React.Fragment key={i}>
              <div className="flex flex-col items-center">
                <div className={`w-9 h-9 rounded-full flex items-center justify-center font-bold text-sm transition-all
                  ${i < step ? 'bg-success text-white' : i === step ? 'bg-primary text-white shadow-lg' : 'bg-border text-slate'}`}>
                  {i < step ? <Check size={16} /> : i + 1}
                </div>
                <span className={`text-xs mt-1 font-medium hidden sm:block ${i === step ? 'text-primary' : 'text-slate'}`}>{s}</span>
              </div>
              {i < STEPS.length - 1 && (
                <div className={`flex-1 h-1 mx-2 rounded-full ${i < step ? 'bg-success' : 'bg-border'}`} />
              )}
            </React.Fragment>
          ))}
        </div>

        <div className="gs-card">
          {/* Step 0: OTP */}
          {step === 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-dark">Verify your mobile number</h2>
              <div>
                <label className="gs-label">Mobile Number</label>
                <div className="flex">
                  <span className="flex items-center px-3 bg-bg-light border-l border-t border-b border-border rounded-l-xl text-slate text-sm font-semibold">+91</span>
                  <input type="tel" maxLength={10} value={form.mobile}
                    onChange={e => handleChange('mobile', e.target.value.replace(/\D/, ''))}
                    placeholder="9876543210" className="gs-input rounded-l-none border-l-0" />
                </div>
              </div>
              {!otpSent ? (
                <button onClick={sendOtp} disabled={loading} className="w-full gs-btn-primary">
                  {loading ? '⏳ Sending...' : 'Send OTP'}</button>
              ) : (
                <>
                  <div>
                    <label className="gs-label mb-3">Enter 6-digit OTP</label>
                    <OtpInput onComplete={v => handleChange('otp', v)} />
                    <div className="text-center mt-2">
                      {countdown > 0 ? <span className="text-xs text-slate">Resend in {countdown}s</span>
                        : <button onClick={sendOtp} className="text-xs text-primary font-semibold">Resend OTP</button>}
                    </div>
                  </div>
                  <button onClick={verifyOtp} disabled={form.otp.length !== 6} className="w-full gs-btn-primary">
                    Verify OTP & Continue →</button>
                </>
              )}
            </div>
          )}

          {/* Step 1: Personal Details */}
          {step === 1 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-dark">Personal Details</h2>
              <div>
                <label className="gs-label">Full Name *</label>
                <input value={form.name} onChange={e => handleChange('name', e.target.value)} placeholder="Rahul Sharma" className="gs-input" />
              </div>
              <div>
                <label className="gs-label">Email (optional)</label>
                <input type="email" value={form.email} onChange={e => handleChange('email', e.target.value)} placeholder="rahul@example.com" className="gs-input" />
              </div>
              <div>
                <label className="gs-label">Password *</label>
                <div className="relative">
                  <input type={showPassword ? 'text' : 'password'} value={form.password}
                    onChange={e => handleChange('password', e.target.value)} placeholder="Min 8 characters" className="gs-input pr-10" />
                  <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate">
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>
              <div>
                <label className="gs-label">Confirm Password *</label>
                <input type="password" value={form.confirmPassword} onChange={e => handleChange('confirmPassword', e.target.value)} placeholder="Repeat password" className="gs-input" />
              </div>
              <div>
                <label className="gs-label">Delivery Platform *</label>
                <div className="flex gap-3">
                  {['ZOMATO', 'SWIGGY'].map(p => (
                    <button key={p} type="button" onClick={() => handleChange('platform', p)}
                      className={`flex-1 py-3 rounded-xl border-2 font-bold text-sm transition-all ${form.platform === p ? 'border-primary bg-primary/10 text-primary' : 'border-border text-slate'}`}>
                      {p === 'ZOMATO' ? '🔴 Zomato' : '🟠 Swiggy'}
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="gs-label">Platform Partner ID</label>
                <input value={form.platformPartnerId} onChange={e => handleChange('platformPartnerId', e.target.value)} placeholder="Your Zomato/Swiggy ID" className="gs-input" />
              </div>
              <div className="flex gap-3 pt-2">
                <button onClick={() => setStep(0)} className="gs-btn-outline flex-1"><ChevronLeft size={18} className="inline" /> Back</button>
                <button onClick={handleStep2} className="gs-btn-primary flex-1">Next <ChevronRight size={18} className="inline" /></button>
              </div>
            </div>
          )}

          {/* Step 2: Work Details */}
          {step === 2 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-dark">Work Details</h2>
              <div>
                <label className="gs-label">City *</label>
                <select value={form.city} onChange={e => handleChange('city', e.target.value)} className="gs-input">
                  {CITIES.map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="gs-label">Zone / Area</label>
                  <input value={form.zone} onChange={e => handleChange('zone', e.target.value)} placeholder="Andheri West" className="gs-input" />
                </div>
                <div>
                  <label className="gs-label">Pincode</label>
                  <input value={form.pincode} onChange={e => handleChange('pincode', e.target.value)} placeholder="400053" className="gs-input" maxLength={6} />
                </div>
              </div>
              <div>
                <label className="gs-label">Avg Daily Earnings: ₹{form.avgDailyEarnings}</label>
                <input type="range" min={400} max={2000} step={50} value={form.avgDailyEarnings}
                  onChange={e => handleChange('avgDailyEarnings', +e.target.value)}
                  className="w-full accent-primary" />
                <div className="flex justify-between text-xs text-slate mt-1"><span>₹400</span><span>₹2000</span></div>
              </div>
              <div>
                <label className="gs-label">Avg Daily Hours: {form.avgDailyHours}h</label>
                <input type="range" min={4} max={14} step={0.5} value={form.avgDailyHours}
                  onChange={e => handleChange('avgDailyHours', +e.target.value)}
                  className="w-full accent-primary" />
                <div className="flex justify-between text-xs text-slate mt-1"><span>4h</span><span>14h</span></div>
              </div>
              <div>
                <label className="gs-label">UPI ID</label>
                <input value={form.upiId} onChange={e => handleChange('upiId', e.target.value)} placeholder="rahul@paytm" className="gs-input" />
              </div>
              <div className="flex gap-3 pt-2">
                <button onClick={() => setStep(1)} className="gs-btn-outline flex-1"><ChevronLeft size={18} className="inline" /> Back</button>
                <button onClick={handleStep3} className="gs-btn-primary flex-1">Next <ChevronRight size={18} className="inline" /></button>
              </div>
            </div>
          )}

          {/* Step 3: Review */}
          {step === 3 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-dark">Review & Submit</h2>
              <div className="grid grid-cols-2 gap-3 text-sm">
                {[
                  ['Name', form.name], ['Mobile', '+91 ' + form.mobile],
                  ['Platform', form.platform], ['City', form.city],
                  ['Zone', form.zone || '-'], ['Avg Earnings', '₹' + form.avgDailyEarnings + '/day'],
                ].map(([k, v]) => (
                  <div key={k} className="bg-bg-light rounded-xl p-3">
                    <p className="text-xs text-slate">{k}</p>
                    <p className="font-semibold text-dark">{v}</p>
                  </div>
                ))}
              </div>
              <div className="bg-primary/5 border border-primary/20 rounded-xl p-4">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm font-semibold text-dark">AI Risk Score</span>
                  <span className={`gs-badge ${riskScore() < 50 ? 'bg-success/10 text-success' : riskScore() < 75 ? 'bg-warning/10 text-warning' : 'bg-danger/10 text-danger'}`}>
                    {riskScore()}/100
                  </span>
                </div>
                <div className="bg-border rounded-full h-2 mb-3">
                  <div className="bg-gradient-to-r from-success to-warning h-2 rounded-full" style={{ width: riskScore() + '%' }}></div>
                </div>
                <p className="text-xs text-slate">Estimated STANDARD plan: <strong className="text-primary">₹{estimatePremium()}/week</strong></p>
              </div>
              <div className="flex gap-3 pt-2">
                <button onClick={() => setStep(2)} className="gs-btn-outline flex-1"><ChevronLeft size={18} className="inline" /> Back</button>
                <button onClick={handleSubmit} disabled={loading} className="gs-btn-primary flex-1">
                  {loading ? '⏳ Creating...' : '🎉 Create Account'}
                </button>
              </div>
            </div>
          )}
        </div>

        <p className="text-center text-sm text-slate mt-4">
          Already have an account? <Link to="/login" className="text-primary font-semibold">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
