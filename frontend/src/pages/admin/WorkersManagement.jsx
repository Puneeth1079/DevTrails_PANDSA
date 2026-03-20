import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Users, Search } from 'lucide-react';

export default function WorkersManagement() {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [cityFilter, setCityFilter] = useState('ALL');

  useEffect(() => {
    api.get('/api/admin/workers')
      .then(r => setWorkers(r.data.data?.content || []))
      .catch(() => setWorkers([
        { id: 1, user: { name: 'Rahul Sharma', mobile: '9111111111' }, city: 'Mumbai', platform: 'ZOMATO', avgDailyEarnings: 950, riskScore: 40, zone: 'Andheri West' },
        { id: 2, user: { name: 'Priya Nair', mobile: '9111111112' }, city: 'Delhi', platform: 'SWIGGY', avgDailyEarnings: 800, riskScore: 55, zone: 'Karol Bagh' },
        { id: 3, user: { name: 'Suresh Babu', mobile: '9111111113' }, city: 'Bengaluru', platform: 'ZOMATO', avgDailyEarnings: 1100, riskScore: 35, zone: 'Koramangala' },
        { id: 4, user: { name: 'Lakshmi Devi', mobile: '9111111114' }, city: 'Chennai', platform: 'SWIGGY', avgDailyEarnings: 700, riskScore: 72, zone: 'Anna Nagar' },
        { id: 5, user: { name: 'Mohammad Ali', mobile: '9111111115' }, city: 'Hyderabad', platform: 'ZOMATO', avgDailyEarnings: 1200, riskScore: 28, zone: 'Banjara Hills' },
      ]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = workers.filter(w =>
    (cityFilter === 'ALL' || w.city === cityFilter) &&
    ((w.user?.name || '').toLowerCase().includes(search.toLowerCase()) ||
     (w.user?.mobile || '').includes(search))
  );

  const cities = ['ALL', ...new Set(workers.map(w => w.city))];

  const riskBadge = score => score < 40
    ? 'bg-success/10 text-success'
    : score < 70 ? 'bg-warning/10 text-warning' : 'bg-danger/10 text-danger';

  return (
    <div className="space-y-6 animate-fade-in">
      <h1 className="text-2xl font-extrabold text-dark">Workers Management</h1>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate" />
          <input value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search by name or mobile..." className="gs-input pl-9" />
        </div>
        <select value={cityFilter} onChange={e => setCityFilter(e.target.value)} className="gs-input w-auto">
          {cities.map(c => <option key={c}>{c}</option>)}
        </select>
      </div>

      <div className="gs-card overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border">
              {['Name', 'Mobile', 'City / Zone', 'Platform', 'Avg Earnings', 'Risk Score', ''].map(h => (
                <th key={h} className="text-left text-xs font-bold text-slate pb-3 pr-4">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {loading ? (
              <tr><td colSpan={7} className="py-8 text-center text-slate">Loading...</td></tr>
            ) : filtered.map(w => (
              <tr key={w.id} className="hover:bg-bg-light transition-colors">
                <td className="py-3 pr-4 font-semibold text-dark">{w.user?.name}</td>
                <td className="py-3 pr-4 text-slate">{w.user?.mobile}</td>
                <td className="py-3 pr-4 text-slate">{w.city}<br /><span className="text-xs">{w.zone}</span></td>
                <td className="py-3 pr-4">
                  <span className={`gs-badge ${w.platform === 'ZOMATO' ? 'bg-red-100 text-red-700' : 'bg-orange-100 text-orange-700'}`}>{w.platform}</span>
                </td>
                <td className="py-3 pr-4 font-semibold text-dark">₹{w.avgDailyEarnings}</td>
                <td className="py-3 pr-4">
                  <span className={`gs-badge ${riskBadge(w.riskScore)}`}>{w.riskScore}</span>
                </td>
                <td className="py-3">
                  <button className="text-xs text-primary font-semibold hover:underline">View</button>
                </td>
              </tr>
            ))}
            {!loading && filtered.length === 0 && (
              <tr><td colSpan={7} className="py-8 text-center text-slate">No workers found</td></tr>
            )}
          </tbody>
        </table>
        <div className="mt-3 text-xs text-slate">Showing {filtered.length} of {workers.length} workers</div>
      </div>
    </div>
  );
}
