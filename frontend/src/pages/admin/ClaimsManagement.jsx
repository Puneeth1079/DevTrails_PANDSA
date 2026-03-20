import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Check, X, AlertTriangle } from 'lucide-react';

const STATUS_STYLES = {
  AUTO_APPROVED: 'bg-success/10 text-success',
  APPROVED: 'bg-success/10 text-success',
  PAID: 'bg-blue-100 text-blue-700',
  PENDING_REVIEW: 'bg-warning/10 text-warning',
  REJECTED: 'bg-danger/10 text-danger',
};

export default function ClaimsManagement() {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('PENDING_REVIEW');

  const fetchClaims = () => {
    setLoading(true);
    api.get(`/api/claims?status=${filter}&size=50`)
      .then(r => setClaims(r.data.data?.content || []))
      .catch(() => setClaims([
        { id: 1, claimNumber: 'CLM-SEED-100', triggerType: 'HEAVY_RAIN', payoutAmount: 450, status: 'PAID', autoTriggered: true, fraudScore: 2, workerName: 'Rahul Sharma', city: 'Mumbai', claimedAt: '2026-03-01T10:00:00' },
        { id: 3, claimNumber: 'CLM-SEED-102', triggerType: 'CURFEW', payoutAmount: 450, status: 'PENDING_REVIEW', autoTriggered: false, fraudScore: 45, workerName: 'Lakshmi Devi', city: 'Chennai', claimedAt: '2026-02-10T09:00:00', fraudFlags: '["INACTIVE_WORKER"]' },
      ].filter(c => filter === 'PENDING_REVIEW' ? c.status === 'PENDING_REVIEW' : true)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchClaims(); }, [filter]);

  const handleAction = async (id, action) => {
    try {
      await api.patch(`/api/claims/${id}/${action}`, { notes: `${action} by admin` });
      toast.success(`Claim ${action}d`);
      fetchClaims();
    } catch { toast.error('Action failed'); }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-extrabold text-dark">Claims Management</h1>
        <span className="gs-badge bg-warning/10 text-warning text-sm">{claims.filter(c => c.status === 'PENDING_REVIEW').length} pending</span>
      </div>

      <div className="flex flex-wrap gap-2">
        {['PENDING_REVIEW', 'AUTO_APPROVED', 'APPROVED', 'REJECTED', 'PAID'].map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${filter === s ? 'bg-primary text-white' : 'bg-white border border-border text-slate hover:border-primary'}`}>
            {s.replace(/_/g, ' ')}
          </button>
        ))}
        <button onClick={() => setFilter('')} className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${!filter ? 'bg-dark text-white' : 'bg-white border border-border text-slate'}`}>
          ALL
        </button>
      </div>

      <div className="space-y-3">
        {loading ? (
          <div className="gs-card h-32 skeleton" />
        ) : claims.length > 0 ? claims.map(claim => (
          <div key={claim.id} className={`gs-card ${claim.fraudScore > 40 ? 'border-l-4 border-warning' : ''}`}>
            <div className="flex flex-col sm:flex-row justify-between gap-3">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-bold text-dark">{claim.claimNumber}</span>
                  <span className={`gs-badge ${STATUS_STYLES[claim.status] || 'bg-slate/10 text-slate'}`}>{claim.status?.replace(/_/g, ' ')}</span>
                  {claim.fraudScore > 40 && <span className="gs-badge bg-warning/10 text-warning"><AlertTriangle size={12} /> High Risk</span>}
                  {claim.autoTriggered && <span className="gs-badge bg-primary/10 text-primary">🤖 Auto</span>}
                </div>
                <p className="text-sm text-slate">{claim.workerName} • {claim.city} • {claim.triggerType?.replace(/_/g, ' ')}</p>
                {claim.fraudFlags && claim.fraudFlags !== '[]' && (
                  <p className="text-xs text-warning mt-1">Flags: {JSON.parse(claim.fraudFlags).join(', ')}</p>
                )}
                <p className="text-xs text-slate mt-1">{new Date(claim.claimedAt).toLocaleString('en-IN')}</p>
              </div>
              <div className="flex items-center gap-3 shrink-0">
                <span className="font-extrabold text-dark text-lg">₹{claim.payoutAmount}</span>
                {claim.status === 'PENDING_REVIEW' && (
                  <div className="flex gap-2">
                    <button onClick={() => handleAction(claim.id, 'approve')}
                      className="p-2 bg-success/10 text-success rounded-lg hover:bg-success hover:text-white transition-all">
                      <Check size={16} />
                    </button>
                    <button onClick={() => handleAction(claim.id, 'reject')}
                      className="p-2 bg-danger/10 text-danger rounded-lg hover:bg-danger hover:text-white transition-all">
                      <X size={16} />
                    </button>
                  </div>
                )}
              </div>
            </div>
            {claim.fraudScore > 0 && (
              <div className="mt-3 pt-3 border-t border-border">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs text-slate font-semibold">Fraud Risk Score</span>
                  <span className="text-xs font-bold">{claim.fraudScore}/100</span>
                </div>
                <div className="bg-border rounded-full h-1.5">
                  <div className={`h-1.5 rounded-full ${claim.fraudScore < 30 ? 'bg-success' : claim.fraudScore < 70 ? 'bg-warning' : 'bg-danger'}`}
                    style={{ width: claim.fraudScore + '%' }}></div>
                </div>
              </div>
            )}
          </div>
        )) : (
          <div className="gs-card text-center py-12">
            <p className="text-slate">No {filter?.replace(/_/g, ' ').toLowerCase()} claims</p>
          </div>
        )}
      </div>
    </div>
  );
}
