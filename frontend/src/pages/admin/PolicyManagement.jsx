import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import { FileText } from 'lucide-react';

export default function PolicyManagement() {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/policies?size=50')
      .then(r => setPolicies(r.data.data?.content || []))
      .catch(() => setPolicies([
        { id: 1, policyNumber: 'GS-2026-10000001', coverageTier: 'BASIC', weeklyPremium: 38, maxWeeklyPayout: 500, status: 'ACTIVE', autoRenew: true, startDate: '2026-03-02', endDate: '2026-03-09', worker: { user: { name: 'Rahul Sharma' }, city: 'Mumbai' } },
        { id: 2, policyNumber: 'GS-2026-10000002', coverageTier: 'STANDARD', weeklyPremium: 67, maxWeeklyPayout: 900, status: 'ACTIVE', autoRenew: false, startDate: '2026-03-02', endDate: '2026-03-09', worker: { user: { name: 'Priya Nair' }, city: 'Delhi' } },
        { id: 3, policyNumber: 'GS-2026-10000003', coverageTier: 'PREMIUM', weeklyPremium: 115, maxWeeklyPayout: 1500, status: 'ACTIVE', autoRenew: true, startDate: '2026-03-02', endDate: '2026-03-09', worker: { user: { name: 'Suresh Babu' }, city: 'Bengaluru' } },
      ]))
      .finally(() => setLoading(false));
  }, []);

  const tierBadge = t => ({
    BASIC: 'bg-slate/10 text-slate',
    STANDARD: 'bg-primary/10 text-primary',
    PREMIUM: 'bg-accent/10 text-accent',
  }[t] || '');

  const statusBadge = s => ({
    ACTIVE: 'bg-success/10 text-success',
    EXPIRED: 'bg-slate/10 text-slate',
    CANCELLED: 'bg-danger/10 text-danger',
  }[s] || '');

  return (
    <div className="space-y-6 animate-fade-in">
      <h1 className="text-2xl font-extrabold text-dark">Policy Management</h1>

      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Total Policies', value: policies.length },
          { label: 'Active', value: policies.filter(p => p.status === 'ACTIVE').length },
          { label: 'Revenue/Week', value: '₹' + policies.filter(p => p.status === 'ACTIVE').reduce((s, p) => s + (p.weeklyPremium || 0), 0).toLocaleString() },
        ].map((kpi, i) => <div key={i} className="gs-card text-center"><div className="text-2xl font-extrabold text-primary">{kpi.value}</div><div className="text-xs text-slate mt-1">{kpi.label}</div></div>)}
      </div>

      <div className="gs-card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border">
              {['Policy#', 'Worker', 'City', 'Tier', 'Premium', 'Max Payout', 'Expires', 'Auto-Renew', 'Status'].map(h => (
                <th key={h} className="text-left text-xs font-bold text-slate pb-3 pr-4">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {loading ? <tr><td colSpan={9} className="py-8 text-center text-slate">Loading...</td></tr>
              : policies.map(p => (
                <tr key={p.id} className="hover:bg-bg-light transition-colors text-xs">
                  <td className="py-3 pr-4 font-mono text-slate">{p.policyNumber}</td>
                  <td className="py-3 pr-4 font-semibold text-dark">{p.worker?.user?.name}</td>
                  <td className="py-3 pr-4 text-slate">{p.worker?.city}</td>
                  <td className="py-3 pr-4"><span className={`gs-badge ${tierBadge(p.coverageTier)}`}>{p.coverageTier}</span></td>
                  <td className="py-3 pr-4 font-semibold">₹{p.weeklyPremium}</td>
                  <td className="py-3 pr-4">₹{p.maxWeeklyPayout}</td>
                  <td className="py-3 pr-4 text-slate">{p.endDate}</td>
                  <td className="py-3 pr-4">{p.autoRenew ? '✅' : '❌'}</td>
                  <td className="py-3"><span className={`gs-badge ${statusBadge(p.status)}`}>{p.status}</span></td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
