import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';
import { Shield, Zap, IndianRupee, FileText, AlertTriangle, ChevronRight, Clock } from 'lucide-react';
import { AreaChart, Area, ResponsiveContainer, Tooltip, XAxis, CartesianGrid } from 'recharts';

const statusColors = {
  AUTO_APPROVED: 'bg-success/10 text-success',
  APPROVED: 'bg-success/10 text-success',
  PAID: 'bg-blue-100 text-blue-700',
  PENDING_REVIEW: 'bg-warning/10 text-warning',
  REJECTED: 'bg-danger/10 text-danger',
};

export default function WorkerDashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get('/api/dashboard/worker')
      .then(r => setData(r.data.data))
      .catch(() => setError('Could not load dashboard — showing demo data'))
      .finally(() => setLoading(false));
  }, []);

  const demo = {
    activePolicy: { coverageTier: 'STANDARD', weeklyPremium: 67, maxWeeklyPayout: 900, status: 'ACTIVE', endDate: '2026-03-09', triggersCovered: '["HEAVY_RAIN","EXTREME_HEAT","SEVERE_POLLUTION","CURFEW","FLOOD"]' },
    earningsProtected: 900,
    totalClaimsThisMonth: 1,
    totalPayoutReceived: 450,
    currentZoneAlerts: ['⚠️ HEAVY RAIN alert in Mumbai', '⚠️ AQI High alert in Mumbai'],
    recentClaims: [
      { id: 1, claimNumber: 'CLM-SEED-100', triggerType: 'HEAVY_RAIN', payoutAmount: 450, status: 'PAID', claimedAt: '2026-03-01T10:00:00', autoTriggered: true },
    ],
    weeklyProtectionHistory: [
      { week: 'W1', premium: 67, payout: 450 }, { week: 'W2', premium: 67, payout: 0 },
      { week: 'W3', premium: 67, payout: 0 }, { week: 'W4', premium: 67, payout: 0 },
      { week: 'W5', premium: 67, payout: 450 }, { week: 'W6', premium: 67, payout: 0 },
      { week: 'W7', premium: 67, payout: 900 }, { week: 'W8', premium: 67, payout: 0 },
    ],
    riskLevel: 'MEDIUM',
  };

  const d = data || demo;

  if (loading) return (
    <div className="space-y-4 animate-pulse">
      {[1,2,3,4].map(i => <div key={i} className="gs-card h-24 skeleton" />)}
    </div>
  );

  const triggers = d.activePolicy?.triggersCovered
    ? JSON.parse(d.activePolicy.triggersCovered).map(t => t.replace(/_/g, ' '))
    : [];

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold text-dark">
            Namaste, {user?.name?.split(' ')[0] || 'Partner'} 👋
          </h1>
          <p className="text-slate text-sm">Here's your income protection status today</p>
        </div>
        {error && <span className="gs-badge bg-warning/10 text-warning text-xs">{error}</span>}
      </div>

      {/* Active Alerts */}
      {d.currentZoneAlerts?.length > 0 && (
        <div className="alert-banner rounded-2xl p-4 text-white">
          <div className="flex items-center gap-2 font-bold mb-2"><AlertTriangle size={18} /> Zone Alert Active!</div>
          {d.currentZoneAlerts.map((a, i) => <p key={i} className="text-sm text-white/90">{a}</p>)}
          <p className="text-xs text-white/70 mt-2 font-medium">Auto-claim processing active — you'll receive UPI payout shortly</p>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="gs-card text-center">
          <div className="w-10 h-10 bg-success/10 rounded-xl flex items-center justify-center mx-auto mb-2">
            <Shield size={20} className="text-success" />
          </div>
          <p className="text-2xl font-extrabold text-dark">₹{d.earningsProtected || 0}</p>
          <p className="text-xs text-slate mt-1">Max Weekly Payout</p>
        </div>
        <div className="gs-card text-center">
          <div className="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center mx-auto mb-2">
            <IndianRupee size={20} className="text-primary" />
          </div>
          <p className="text-2xl font-extrabold text-dark">₹{d.totalPayoutReceived || 0}</p>
          <p className="text-xs text-slate mt-1">Total Payout Received</p>
        </div>
        <div className="gs-card text-center">
          <div className="w-10 h-10 bg-accent/10 rounded-xl flex items-center justify-center mx-auto mb-2">
            <FileText size={20} className="text-accent" />
          </div>
          <p className="text-2xl font-extrabold text-dark">{d.totalClaimsThisMonth || 0}</p>
          <p className="text-xs text-slate mt-1">Claims This Month</p>
        </div>
        <div className="gs-card text-center">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center mx-auto mb-2 ${d.riskLevel === 'LOW' ? 'bg-success/10' : d.riskLevel === 'HIGH' ? 'bg-danger/10' : 'bg-warning/10'}`}>
            <Zap size={20} className={d.riskLevel === 'LOW' ? 'text-success' : d.riskLevel === 'HIGH' ? 'text-danger' : 'text-warning'} />
          </div>
          <p className="text-2xl font-extrabold text-dark">{d.riskLevel || 'MEDIUM'}</p>
          <p className="text-xs text-slate mt-1">Risk Level</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Active Policy Card */}
        <div className="lg:col-span-1">
          <div className="gs-card h-full">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-dark">Active Policy</h3>
              {d.activePolicy ? (
                <span className="gs-badge bg-success/10 text-success">✓ Active</span>
              ) : (
                <span className="gs-badge bg-slate/10 text-slate">No Policy</span>
              )}
            </div>
            {d.activePolicy ? (
              <div className="space-y-3">
                <div className="text-center py-4 bg-primary/5 rounded-xl">
                  <div className="text-3xl font-extrabold text-primary">₹{d.activePolicy.weeklyPremium}</div>
                  <div className="text-xs text-slate">per week</div>
                  <div className="gs-badge bg-primary/10 text-primary mt-2">{d.activePolicy.coverageTier}</div>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-slate">Max payout</span>
                    <span className="font-semibold text-dark">₹{d.activePolicy.maxWeeklyPayout}/week</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate">Renews</span>
                    <span className="font-semibold text-dark">{d.activePolicy.endDate}</span>
                  </div>
                </div>
                <div>
                  <p className="text-xs text-slate mb-2">Covered triggers</p>
                  <div className="flex flex-wrap gap-1">
                    {triggers.map(t => (
                      <span key={t} className="gs-badge bg-primary/10 text-primary text-xs">{t}</span>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-6">
                <p className="text-slate text-sm mb-4">You don't have an active policy</p>
                <Link to="/worker/policies" className="gs-btn-primary text-sm py-2 px-4">Get Protected</Link>
              </div>
            )}
          </div>
        </div>

        {/* Chart */}
        <div className="lg:col-span-2">
          <div className="gs-card h-full">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-dark">Weekly Protection History</h3>
              <span className="text-xs text-slate">Last 8 weeks</span>
            </div>
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={d.weeklyProtectionHistory || []}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E3EAF4" />
                <XAxis dataKey="week" tick={{ fontSize: 11, fill: '#455A64' }} />
                <Tooltip formatter={(v, n) => ['₹' + v, n === 'premium' ? 'Premium Paid' : 'Payout Received']}
                  contentStyle={{ borderRadius: '12px', border: '1px solid #E3EAF4' }} />
                <Area type="monotone" dataKey="payout" stroke="#2E7D32" fill="#2E7D32" fillOpacity={0.1} strokeWidth={2} name="payout" />
                <Area type="monotone" dataKey="premium" stroke="#1A73E8" fill="#1A73E8" fillOpacity={0.1} strokeWidth={2} name="premium" />
              </AreaChart>
            </ResponsiveContainer>
            <div className="flex gap-4 mt-2 text-xs text-slate">
              <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-primary inline-block"></span> Premium Paid</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-success inline-block"></span> Payout Received</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Claims */}
      <div className="gs-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-dark">Recent Claims</h3>
          <Link to="/worker/claims" className="text-primary text-sm font-semibold hover:underline">View all →</Link>
        </div>
        {d.recentClaims?.length > 0 ? (
          <div className="space-y-3">
            {d.recentClaims.map(claim => (
              <div key={claim.id} className="flex items-center justify-between p-3 bg-bg-light rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-primary/10 rounded-xl flex items-center justify-center">
                    <Zap size={16} className="text-primary" />
                  </div>
                  <div>
                    <p className="font-semibold text-dark text-sm">{claim.claimNumber}</p>
                    <p className="text-xs text-slate">{claim.triggerType?.replace(/_/g, ' ')} {claim.autoTriggered ? '• Auto-triggered' : ''}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-bold text-dark">₹{claim.payoutAmount}</p>
                  <span className={`gs-badge text-xs ${statusColors[claim.status] || 'bg-slate/10 text-slate'}`}>{claim.status}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-slate text-sm text-center py-4">No claims yet — your zone is disruption-free 🌞</p>
        )}
      </div>
    </div>
  );
}
