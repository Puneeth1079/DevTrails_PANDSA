import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { FileText, CheckCircle, XCircle, RefreshCw } from 'lucide-react';

const PLANS = [
  { tier: 'BASIC', price: 35, maxPrice: 45, payout: 500, triggers: 2, color: 'border-slate', popular: false },
  { tier: 'STANDARD', price: 65, maxPrice: 79, payout: 900, triggers: 5, color: 'border-primary', popular: true },
  { tier: 'PREMIUM', price: 109, maxPrice: 129, payout: 1500, triggers: 5, color: 'border-accent', popular: false },
];

const TRIGGER_LABELS = ['HEAVY_RAIN', 'EXTREME_HEAT', 'SEVERE_POLLUTION', 'CURFEW', 'FLOOD'];

export default function MyPolicies() {
  const { user } = useAuth();
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [subscribing, setSubscribing] = useState('');
  const [activeTab, setActiveTab] = useState('active');

  const fetchPolicies = () => {
    setLoading(true);
    api.get('/api/policies/my-policies')
      .then(r => setPolicies(r.data.data?.content || []))
      .catch(() => setPolicies([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchPolicies(); }, []);

  const handleSubscribe = async (tier) => {
    setSubscribing(tier);
    try {
      await api.post('/api/policies/subscribe', {
        coverageTier: tier,
        autoRenew: true,
        triggersCovered: TRIGGER_LABELS,
      });
      toast.success('Policy subscribed! Auto-protection enabled. 🛡️');
      fetchPolicies();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Subscription failed');
    } finally { setSubscribing(''); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this policy?')) return;
    try {
      await api.patch(`/api/policies/${id}/cancel`);
      toast.success('Policy cancelled');
      fetchPolicies();
    } catch { toast.error('Failed to cancel'); }
  };

  const statusBadge = s => ({
    ACTIVE: 'bg-success/10 text-success',
    EXPIRED: 'bg-slate/10 text-slate',
    CANCELLED: 'bg-danger/10 text-danger',
  }[s] || 'bg-slate/10 text-slate');

  const hasActivePolicy = policies.some(p => p.status === 'ACTIVE');

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-extrabold text-dark">My Policies</h1>
        <p className="text-slate text-sm">Manage your income protection coverage</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-border">
        {['active', 'all', 'plans'].map(tab => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 font-semibold text-sm capitalize transition-all
              ${activeTab === tab ? 'text-primary border-b-2 border-primary' : 'text-slate hover:text-dark'}`}>
            {tab === 'plans' ? '+ New Plan' : tab}
          </button>
        ))}
      </div>

      {/* Active / All policies */}
      {activeTab !== 'plans' && (
        <>
          {loading ? (
            <div className="gs-card h-32 skeleton" />
          ) : policies.filter(p => activeTab === 'all' || p.status === 'ACTIVE').length > 0 ? (
            <div className="space-y-4">
              {policies.filter(p => activeTab === 'all' || p.status === 'ACTIVE').map(policy => (
                <div key={policy.id} className="gs-card border-l-4 border-primary">
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-dark text-lg">{policy.coverageTier} Plan</span>
                        <span className={`gs-badge ${statusBadge(policy.status)}`}>{policy.status}</span>
                      </div>
                      <p className="text-sm text-slate font-mono">{policy.policyNumber}</p>
                      <div className="flex flex-wrap gap-3 text-sm text-slate">
                        <span>₹{policy.weeklyPremium}/week</span>
                        <span>•</span>
                        <span>Max payout: ₹{policy.maxWeeklyPayout}/week</span>
                        <span>•</span>
                        <span>Valid till: {policy.endDate}</span>
                        <span>•</span>
                        <span>{policy.autoRenew ? '🔄 Auto-renew on' : 'Manual renew'}</span>
                      </div>
                    </div>
                    {policy.status === 'ACTIVE' && (
                      <button onClick={() => handleCancel(policy.id)}
                        className="text-danger text-sm font-semibold flex items-center gap-1 hover:underline shrink-0">
                        <XCircle size={14} /> Cancel
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="gs-card text-center py-12">
              <FileText size={48} className="text-border mx-auto mb-4" />
              <h3 className="font-bold text-dark mb-2">No {activeTab === 'active' ? 'active' : ''} policies</h3>
              <p className="text-slate text-sm mb-4">Subscribe to a plan to start income protection</p>
              <button onClick={() => setActiveTab('plans')} className="gs-btn-primary">View Plans</button>
            </div>
          )}
        </>
      )}

      {/* Plan selection */}
      {activeTab === 'plans' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {PLANS.map(plan => (
            <div key={plan.tier} className={`gs-card border-2 ${plan.color} ${plan.popular ? 'md:-mt-3' : ''} relative`}>
              {plan.popular && <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary text-white text-xs font-bold px-4 py-1 rounded-full">Most Popular</div>}
              <div className="mb-4">
                <h3 className="font-black text-xl text-dark">{plan.tier}</h3>
                <div className="flex items-end gap-1 mt-1">
                  <span className="text-3xl font-extrabold text-dark">₹{plan.price}</span>
                  <span className="text-slate text-sm mb-1">–{plan.maxPrice}/week</span>
                </div>
                <p className="text-sm text-slate">Up to ₹{plan.payout} payout/week</p>
              </div>
              <ul className="space-y-2 mb-5 text-sm text-slate">
                <li className="flex items-center gap-2"><CheckCircle size={14} className="text-success" /> {plan.triggers} trigger types</li>
                <li className="flex items-center gap-2"><CheckCircle size={14} className="text-success" /> Auto-triggered claims</li>
                <li className="flex items-center gap-2"><CheckCircle size={14} className="text-success" /> Instant UPI payout</li>
                {plan.tier !== 'BASIC' && <li className="flex items-center gap-2"><CheckCircle size={14} className="text-success" /> Priority support</li>}
                {plan.tier === 'PREMIUM' && <li className="flex items-center gap-2"><CheckCircle size={14} className="text-success" /> Weekly risk analysis</li>}
              </ul>
              <button onClick={() => handleSubscribe(plan.tier)}
                disabled={!!subscribing || hasActivePolicy}
                className={`w-full ${plan.tier === 'STANDARD' ? 'gs-btn-primary' : plan.tier === 'PREMIUM' ? 'gs-btn-accent' : 'gs-btn-outline'} text-sm`}>
                {subscribing === plan.tier ? '⏳ Subscribing...' : hasActivePolicy ? 'Switch Plan (Cancel current first)' : `Subscribe to ${plan.tier}`}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
