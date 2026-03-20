import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import { Zap, Clock } from 'lucide-react';

const STATUS_STYLES = {
  AUTO_APPROVED: 'bg-success/10 text-success',
  APPROVED: 'bg-success/10 text-success',
  PAID: 'bg-blue-100 text-blue-700',
  PENDING_REVIEW: 'bg-warning/10 text-warning',
  REJECTED: 'bg-danger/10 text-danger',
};

const TRIGGER_ICONS = {
  HEAVY_RAIN: '🌧️',
  EXTREME_HEAT: '🌡️',
  SEVERE_POLLUTION: '💨',
  CURFEW: '🚧',
  FLOOD: '🌊',
};

export default function ClaimsHistory() {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    api.get('/api/claims/my-claims?size=50')
      .then(r => setClaims(r.data.data?.content || []))
      .catch(() => setClaims([
        { id: 1, claimNumber: 'CLM-SEED-100', triggerType: 'HEAVY_RAIN', payoutAmount: 450, status: 'PAID', claimedAt: '2026-03-01T10:00:00', autoTriggered: true, fraudScore: 2, notes: null },
        { id: 2, claimNumber: 'CLM-SEED-101', triggerType: 'SEVERE_POLLUTION', payoutAmount: 450, status: 'PAID', claimedAt: '2026-02-20T14:30:00', autoTriggered: true, fraudScore: 0, notes: null },
        { id: 3, claimNumber: 'CLM-SEED-102', triggerType: 'CURFEW', payoutAmount: 0, status: 'PENDING_REVIEW', claimedAt: '2026-02-10T09:00:00', autoTriggered: false, fraudScore: 45, notes: 'Under review' },
      ]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = filter === 'ALL' ? claims : claims.filter(c => c.status === filter);

  const totalPaid = claims.filter(c => c.status === 'PAID').reduce((s, c) => s + c.payoutAmount, 0);
  const totalClaims = claims.length;
  const pending = claims.filter(c => c.status === 'PENDING_REVIEW').length;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-extrabold text-dark">Claims History</h1>
        <p className="text-slate text-sm">Auto-triggered and manual claim records</p>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Total Claims', value: totalClaims, color: 'text-primary' },
          { label: 'Total Paid Out', value: '₹' + totalPaid, color: 'text-success' },
          { label: 'Pending Review', value: pending, color: 'text-warning' },
        ].map((s, i) => (
          <div key={i} className="gs-card text-center">
            <div className={`text-2xl font-extrabold ${s.color}`}>{s.value}</div>
            <div className="text-xs text-slate mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Status filter */}
      <div className="flex flex-wrap gap-2">
        {['ALL', 'PAID', 'AUTO_APPROVED', 'PENDING_REVIEW', 'REJECTED'].map(f => (
          <button key={f} onClick={() => setFilter(f)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${filter === f ? 'bg-primary text-white' : 'bg-white border border-border text-slate hover:border-primary'}`}>
            {f.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      {/* Claims List */}
      {loading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => <div key={i} className="gs-card h-20 skeleton" />)}
        </div>
      ) : filtered.length > 0 ? (
        <div className="space-y-3">
          {filtered.map(claim => (
            <div key={claim.id} className="gs-card hover:shadow-card-hover transition-all">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 bg-primary/10 rounded-xl flex items-center justify-center text-xl">
                    {TRIGGER_ICONS[claim.triggerType] || '⚡'}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-bold text-dark text-sm">{claim.claimNumber}</p>
                      {claim.autoTriggered && <span className="gs-badge bg-primary/10 text-primary text-xs">🤖 Auto</span>}
                    </div>
                    <p className="text-xs text-slate">{claim.triggerType?.replace(/_/g, ' ')} • {new Date(claim.claimedAt).toLocaleDateString('en-IN')}</p>
                    {claim.notes && <p className="text-xs text-warning mt-1">{claim.notes}</p>}
                  </div>
                </div>
                <div className="flex items-center gap-3 sm:flex-col sm:items-end">
                  <span className={`gs-badge ${STATUS_STYLES[claim.status] || 'bg-slate/10 text-slate'}`}>{claim.status?.replace(/_/g, ' ')}</span>
                  <div className="text-right">
                    <p className="font-extrabold text-dark">₹{claim.payoutAmount}</p>
                    {claim.fraudScore > 30 && <p className="text-xs text-warning">Risk: {claim.fraudScore}/100</p>}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="gs-card text-center py-12">
          <Zap size={48} className="text-border mx-auto mb-4" />
          <p className="font-bold text-dark">No {filter !== 'ALL' ? filter.replace(/_/g, ' ').toLowerCase() : ''} claims found</p>
          <p className="text-slate text-sm mt-1">Your zone is disruption-free 🌞</p>
        </div>
      )}
    </div>
  );
}
