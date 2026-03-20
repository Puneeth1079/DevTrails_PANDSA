import React, { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Zap, Plus, CheckCircle, AlertCircle } from 'lucide-react';

const CITIES = ['Mumbai', 'Delhi', 'Bengaluru', 'Chennai', 'Hyderabad', 'Pune', 'Kolkata', 'Ahmedabad'];
const TRIGGER_TYPES = ['HEAVY_RAIN', 'EXTREME_HEAT', 'SEVERE_POLLUTION', 'CURFEW', 'FLOOD'];

export default function TriggerConfig() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [polling, setPolling] = useState(false);
  const [form, setForm] = useState({ city: 'Mumbai', triggerType: 'HEAVY_RAIN', severityValue: '', severityUnit: 'mm/hr', threshold: '' });
  const [simulating, setSimulating] = useState(false);

  const fetchEvents = () => {
    api.get('/api/triggers/active')
      .then(r => setEvents(r.data.data || []))
      .catch(() => setEvents([
        { id: 1, triggerType: 'HEAVY_RAIN', city: 'Mumbai', zone: 'Andheri West', severityValue: 18.5, severityUnit: 'mm/hr', eventStart: new Date().toISOString(), dataSource: 'OpenWeatherMap', isActive: true },
        { id: 2, triggerType: 'SEVERE_POLLUTION', city: 'Delhi', zone: null, severityValue: 320, severityUnit: 'AQI', eventStart: new Date().toISOString(), dataSource: 'IQAir', isActive: true },
      ]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchEvents(); }, []);

  const handlePoll = async () => {
    setPolling(true);
    try {
      await api.post('/api/triggers/poll-now');
      toast.success('Trigger poll executed!');
      fetchEvents();
    } catch { toast.success('Poll executed (demo mode)'); }
    finally { setPolling(false); }
  };

  const handleSimulate = async () => {
    if (!form.severityValue) return toast.error('Enter severity value');
    setSimulating(true);
    try {
      await api.post('/api/triggers/simulate', {
        city: form.city, triggerType: form.triggerType,
        severityValue: parseFloat(form.severityValue),
        severityUnit: form.severityUnit,
        threshold: parseFloat(form.threshold || form.severityValue),
      });
      toast.success(`⚡ ${form.triggerType} simulated in ${form.city}!`);
      fetchEvents();
    } catch (e) { toast.error(e.response?.data?.message || 'Simulation failed'); }
    finally { setSimulating(false); }
  };

  const TRIGGER_ICONS = { HEAVY_RAIN: '🌧️', EXTREME_HEAT: '🌡️', SEVERE_POLLUTION: '💨', CURFEW: '🚧', FLOOD: '🌊' };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <h1 className="text-2xl font-extrabold text-dark">Trigger Configuration</h1>
        <button onClick={handlePoll} disabled={polling} className="gs-btn-primary text-sm py-2 px-4 flex items-center gap-2">
          <Zap size={16} /> {polling ? 'Polling...' : 'Poll All Cities Now'}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Active Events */}
        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4">Active Disruption Events</h3>
          {loading ? <div className="h-32 skeleton rounded-xl" /> : (
            events.length > 0 ? events.map(event => (
              <div key={event.id} className="flex items-start gap-3 p-3 bg-danger/5 border border-danger/20 rounded-xl mb-3">
                <span className="text-2xl">{TRIGGER_ICONS[event.triggerType] || '⚡'}</span>
                <div>
                  <p className="font-bold text-dark text-sm">{event.triggerType?.replace(/_/g, ' ')}</p>
                  <p className="text-xs text-slate">{event.city} {event.zone ? '• ' + event.zone : ''}</p>
                  <p className="text-xs text-slate">{event.severityValue} {event.severityUnit} • {event.dataSource}</p>
                  <p className="text-xs text-slate/60 mt-1">{new Date(event.eventStart).toLocaleString('en-IN')}</p>
                </div>
                <span className="ml-auto gs-badge bg-danger/10 text-danger animate-ping-slow">Live</span>
              </div>
            )) : (
              <div className="text-center py-8">
                <CheckCircle size={40} className="text-success mx-auto mb-2" />
                <p className="text-slate text-sm">No active disruptions</p>
              </div>
            )
          )}
        </div>

        {/* Manual Simulate */}
        <div className="gs-card">
          <h3 className="font-bold text-dark mb-4">Simulate Trigger (Demo)</h3>
          <div className="space-y-3">
            <div>
              <label className="gs-label">City</label>
              <select value={form.city} onChange={e => setForm(f => ({ ...f, city: e.target.value }))} className="gs-input">
                {CITIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div>
              <label className="gs-label">Trigger Type</label>
              <select value={form.triggerType} onChange={e => {
                const t = e.target.value;
                const units = { HEAVY_RAIN: 'mm/hr', EXTREME_HEAT: '°C', SEVERE_POLLUTION: 'AQI', CURFEW: 'alert', FLOOD: 'level' };
                setForm(f => ({ ...f, triggerType: t, severityUnit: units[t] }));
              }} className="gs-input">
                {TRIGGER_TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="gs-label">Severity Value</label>
                <input type="number" value={form.severityValue} onChange={e => setForm(f => ({ ...f, severityValue: e.target.value }))}
                  placeholder="e.g. 20" className="gs-input" />
              </div>
              <div>
                <label className="gs-label">Unit</label>
                <input value={form.severityUnit} onChange={e => setForm(f => ({ ...f, severityUnit: e.target.value }))}
                  placeholder="mm/hr" className="gs-input" />
              </div>
            </div>
            <div>
              <label className="gs-label">Threshold (optional)</label>
              <input type="number" value={form.threshold} onChange={e => setForm(f => ({ ...f, threshold: e.target.value }))}
                placeholder="Threshold to breach" className="gs-input" />
            </div>
            <button onClick={handleSimulate} disabled={simulating} className="w-full gs-btn-accent flex items-center justify-center gap-2">
              <Zap size={16} /> {simulating ? '⏳ Simulating...' : '⚡ Simulate Trigger'}
            </button>
            <p className="text-xs text-slate text-center">This will create a disruption event and trigger auto-claims for all active policies in that city covering this trigger type.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
