import React, { useEffect, useState } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { User, Save } from 'lucide-react';

export default function WorkerProfile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get('/api/worker/profile')
      .then(r => setProfile(r.data.data))
      .catch(() => setProfile({
        name: user?.name, mobile: user?.mobile, city: 'Mumbai', platform: 'ZOMATO',
        avgDailyEarnings: 800, avgDailyHours: 8, riskScore: 50, upiId: '',
      }))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.put('/api/worker/profile', { upiId: profile.upiId, zone: profile.zone });
      toast.success('Profile updated!');
    } catch { toast.error('Update failed'); }
    finally { setSaving(false); }
  };

  const riskColor = (score) => score < 40 ? 'text-success' : score < 70 ? 'text-warning' : 'text-danger';

  if (loading) return <div className="gs-card h-64 skeleton" />;

  return (
    <div className="space-y-6 animate-fade-in max-w-2xl">
      <h1 className="text-2xl font-extrabold text-dark">My Profile</h1>

      <div className="gs-card text-center">
        <div className="w-20 h-20 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center mx-auto mb-4">
          <span className="text-white text-3xl font-bold">{profile?.name?.[0] || 'W'}</span>
        </div>
        <h2 className="text-xl font-bold text-dark">{profile?.name}</h2>
        <p className="text-slate">{profile?.mobile}</p>
        <div className="flex gap-2 justify-center mt-3">
          <span className="gs-badge bg-primary/10 text-primary">{profile?.platform}</span>
          <span className="gs-badge bg-slate/10 text-slate">{profile?.city}</span>
        </div>
      </div>

      <div className="gs-card">
        <h3 className="font-bold text-dark mb-4">Work Details</h3>
        <div className="grid grid-cols-2 gap-4 text-sm">
          {[
            ['Platform', profile?.platform], ['City', profile?.city], ['Zone', profile?.zone || '-'],
            ['Avg Earnings', '₹' + profile?.avgDailyEarnings + '/day'],
            ['Daily Hours', profile?.avgDailyHours + 'h'],
          ].map(([k, v]) => (
            <div key={k} className="bg-bg-light rounded-xl p-3">
              <p className="text-xs text-slate">{k}</p>
              <p className="font-semibold text-dark">{v}</p>
            </div>
          ))}
          <div className="bg-bg-light rounded-xl p-3">
            <p className="text-xs text-slate">AI Risk Score</p>
            <p className={`font-bold text-lg ${riskColor(profile?.riskScore)}`}>{profile?.riskScore}/100</p>
          </div>
        </div>
      </div>

      <div className="gs-card">
        <h3 className="font-bold text-dark mb-4">Payment Settings</h3>
        <div>
          <label className="gs-label">UPI ID</label>
          <input value={profile?.upiId || ''} onChange={e => setProfile(p => ({ ...p, upiId: e.target.value }))}
            placeholder="yourname@upi" className="gs-input" />
        </div>
        <button onClick={handleSave} disabled={saving} className="gs-btn-primary mt-4 flex items-center gap-2">
          <Save size={16} /> {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </div>
  );
}
