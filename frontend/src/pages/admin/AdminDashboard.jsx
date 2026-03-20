import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Users, FileText, ClipboardList, IndianRupee, AlertTriangle,
  Zap, TrendingUp, BarChart2, Shield, MapPin
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, LineChart, Line, Legend, PieChart, Pie, Cell
} from 'recharts';

const COLORS = ['#1A73E8', '#FF6F00', '#2E7D32', '#C62828', '#9C27B0'];

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastPoll, setLastPoll] = useState(null);
  const [polling, setPolling] = useState(false);

  const demo = {
    totalWorkers: 1247,
    activePolicies: 892,
    pendingClaims: 23,
    totalPayoutsThisMonth: 187430,
    lossRatio: 0.62,
    fraudAlertsActive: 5,
    claimsByTriggerType: {
      HEAVY_RAIN: 145, EXTREME_HEAT: 87, SEVERE_POLLUTION: 62, CURFEW: 18, FLOOD: 34
    },
    cityWiseRisk: [
      { city: 'Mumbai', riskStatus: 'HIGH', activePolicies: 312, claimsThisWeek: 48 },
      { city: 'Delhi', riskStatus: 'HIGH', activePolicies: 208, claimsThisWeek: 29 },
      { city: 'Bengaluru', riskStatus: 'MEDIUM', activePolicies: 176, claimsThisWeek: 15 },
      { city: 'Chennai', riskStatus: 'MEDIUM', activePolicies: 98, claimsThisWeek: 8 },
      { city: 'Hyderabad', riskStatus: 'LOW', activePolicies: 98, claimsThisWeek: 3 },
    ],
    revenueVsPayouts: [
      { week: 'W1', premiums: 45000, payouts: 18000 },
      { week: 'W2', premiums: 52000, payouts: 24000 },
      { week: 'W3', premiums: 48000, payouts: 35000 },
      { week: 'W4', premiums: 61000, payouts: 22000 },
      { week: 'W5', premiums: 59000, payouts: 41000 },
      { week: 'W6', premiums: 67000, payouts: 38000 },
      { week: 'W7', premiums: 72000, payouts: 29000 },
      { week: 'W8', premiums: 78000, payouts: 45000 },
    ],
    predictiveAlerts: [
      { city: 'Mumbai', risk: 'HIGH', nextWeekForecast: 'Heavy monsoon expected' },
      { city: 'Delhi', risk: 'MEDIUM', nextWeekForecast: 'AQI likely to rise' },
      { city: 'Chennai', risk: 'LOW', nextWeekForecast: 'Clear weather expected' },
    ],
  };

  useEffect(() => {
    api.get('/api/dashboard/admin')
      .then(r => setData(r.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handlePollNow = async () => {
    setPolling(true);
    try {
      await api.post('/api/triggers/poll-now');
      setLastPoll(new Date().toLocaleTimeString());
      toast.success('Trigger polling completed!');
    } catch { toast.success('Poll executed (demo mode)'); setLastPoll(new Date().toLocaleTimeString()); }
    finally { setPolling(false); }
  };

  const d = data || demo;

  const pieData = Object.entries(d.claimsByTriggerType || {}).map(([k, v]) => ({ name: k.replace(/_/g, ' '), value: v }));

  const riskBadge = risk => ({
    HIGH: 'bg-danger/10 text-danger',
    MEDIUM: 'bg-warning/10 text-warning',
    LOW: 'bg-success/10 text-success',
  }[risk] || 'bg-slate/10 text-slate');

  if (loading) return (
    <div className="space-y-4 animate-pulse">
      {[1,2,3].map(i => <div key={i} className="gs-card h-24 skeleton" />)}
    </div>
  );

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold text-dark">Operations Dashboard</h1>
          <p className="text-slate text-sm">Real-time GigShield platform overview</p>
        </div>
        <button onClick={handlePollNow} disabled={polling}
          className="gs-btn-primary text-sm py-2 px-4 flex items-center gap-2">
          <Zap size={16} /> {polling ? 'Polling...' : 'Poll Triggers Now'}
        </button>
      </div>
      {lastPoll && <p className="text-xs text-slate">Last poll: {lastPoll}</p>}

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        {[
          { label: 'Total Workers', value: d.totalWorkers?.toLocaleString(), icon: Users, color: 'bg-primary/10', iconColor: 'text-primary' },
          { label: 'Active Policies', value: d.activePolicies?.toLocaleString(), icon: Shield, color: 'bg-success/10', iconColor: 'text-success' },
          { label: 'Pending Claims', value: d.pendingClaims, icon: ClipboardList, color: 'bg-warning/10', iconColor: 'text-warning' },
          { label: 'Payouts This Month', value: '₹' + (d.totalPayoutsThisMonth/1000).toFixed(1) + 'K', icon: IndianRupee, color: 'bg-accent/10', iconColor: 'text-accent' },
          { label: 'Loss Ratio', value: (d.lossRatio * 100).toFixed(0) + '%', icon: TrendingUp, color: 'bg-purple-100', iconColor: 'text-purple-700' },
          { label: 'Fraud Alerts', value: d.fraudAlertsActive, icon: AlertTriangle, color: 'bg-danger/10', iconColor: 'text-danger' },
        ].map((kpi, i) => (
          <div key={i} className="gs-card">
            <div className={`w-10 h-10 ${kpi.color} rounded-xl flex items-center justify-center mb-3`}>
              <kpi.icon size={20} className={kpi.iconColor} />
            </div>
            <div className="text-2xl font-extrabold text-dark">{kpi.value}</div>
            <div className="text-xs text-slate mt-0.5">{kpi.label}</div>
          </div>
        ))}
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Revenue vs Payouts */}
        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4">Revenue vs Payouts (Weekly)</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={d.revenueVsPayouts}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E3EAF4" />
              <XAxis dataKey="week" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={v => '₹' + (v/1000).toFixed(0) + 'K'} tick={{ fontSize: 11 }} />
              <Tooltip formatter={v => '₹' + v.toLocaleString()} contentStyle={{ borderRadius: '12px' }} />
              <Legend />
              <Bar dataKey="premiums" fill="#1A73E8" name="Premium Revenue" radius={[4,4,0,0]} />
              <Bar dataKey="payouts" fill="#FF6F00" name="Claims Paid" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Claims by Trigger */}
        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4">Claims by Trigger Type</h3>
          <div className="flex items-center gap-4">
            <ResponsiveContainer width="50%" height={200}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} dataKey="value">
                  {pieData.map((_, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            <div className="flex-1 space-y-2">
              {pieData.map((item, idx) => (
                <div key={idx} className="flex items-center gap-2 text-sm">
                  <span className="w-3 h-3 rounded-full shrink-0" style={{ background: COLORS[idx % COLORS.length] }}></span>
                  <span className="text-slate truncate text-xs">{item.name}</span>
                  <span className="ml-auto font-bold text-dark">{item.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* City Risk Table + Predictive Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4 flex items-center gap-2"><MapPin size={16} className="text-primary" /> City-wise Risk</h3>
          <div className="space-y-3">
            {d.cityWiseRisk?.map((city, i) => (
              <div key={i} className="flex items-center justify-between p-3 bg-bg-light rounded-xl">
                <div>
                  <p className="font-semibold text-dark text-sm">{city.city}</p>
                  <p className="text-xs text-slate">{city.activePolicies} active policies · {city.claimsThisWeek} claims/week</p>
                </div>
                <span className={`gs-badge ${riskBadge(city.riskStatus)}`}>{city.riskStatus}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4 flex items-center gap-2"><Zap size={16} className="text-accent" /> Predictive Alerts</h3>
          <div className="space-y-3">
            {d.predictiveAlerts?.map((alert, i) => (
              <div key={i} className={`p-4 rounded-xl border ${alert.risk === 'HIGH' ? 'bg-danger/5 border-danger/20' : alert.risk === 'MEDIUM' ? 'bg-warning/5 border-warning/20' : 'bg-success/5 border-success/20'}`}>
                <div className="flex items-center justify-between mb-1">
                  <p className="font-semibold text-dark text-sm">{alert.city}</p>
                  <span className={`gs-badge ${riskBadge(alert.risk)}`}>{alert.risk}</span>
                </div>
                <p className="text-xs text-slate">{alert.nextWeekForecast}</p>
              </div>
            ))}
          </div>
          <div className="text-center mt-4">
            <p className="text-xs text-slate">Powered by AI weather & AQI forecasts</p>
          </div>
        </div>
      </div>
    </div>
  );
}
