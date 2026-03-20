import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Shield, Zap, CloudRain, Thermometer, Wind, AlertCircle, CheckCircle, ArrowRight, Star } from 'lucide-react';

const STATS = [
  { value: 15, suffix: 'M+', label: 'Gig workers in India', prefix: '' },
  { value: 0, suffix: '', label: 'Downtime cost to you', prefix: '₹' },
  { value: 5, suffix: ' min', label: 'Instant payout', prefix: '' },
  { value: 100, suffix: '%', label: 'Auto-triggered claims', prefix: '' },
];

const TRIGGERS = [
  { icon: CloudRain, label: 'Heavy Rain', desc: '≥15mm/hr rainfall', color: 'from-blue-500 to-blue-700', bg: 'bg-blue-50' },
  { icon: Thermometer, label: 'Extreme Heat', desc: 'Feels like ≥46°C', color: 'from-orange-500 to-red-600', bg: 'bg-orange-50' },
  { icon: Wind, label: 'Severe Pollution', desc: 'AQI ≥300', color: 'from-purple-500 to-purple-700', bg: 'bg-purple-50' },
  { icon: AlertCircle, label: 'Curfew/Strike', desc: 'Civic disruption', color: 'from-gray-600 to-gray-800', bg: 'bg-gray-50' },
  { icon: CloudRain, label: 'Flood Alert', desc: 'Area flooding', color: 'from-cyan-500 to-blue-600', bg: 'bg-cyan-50' },
];

const PLANS = [
  { tier: 'BASIC', price: 29, maxPrice: 45, payout: 500, color: 'border-slate', btnClass: 'gs-btn-outline', triggers: 2, features: ['2 trigger types', '₹500 max payout/week', 'Auto-claim processing', 'UPI instant payout'] },
  { tier: 'STANDARD', price: 59, maxPrice: 79, payout: 900, color: 'border-primary', btnClass: 'gs-btn-primary', triggers: 5, features: ['All 5 trigger types', '₹900 max payout/week', 'Priority claim review', 'UPI instant payout', 'Zone alert notifications'], popular: true },
  { tier: 'PREMIUM', price: 99, maxPrice: 129, payout: 1500, color: 'border-accent', btnClass: 'gs-btn-accent', triggers: 5, features: ['All 5 trigger types', '₹1500 max payout/week', 'Enhanced fraud protection', 'Dedicated support', 'Weekly risk analysis', 'Auto-renewal discount'] },
];

const TESTIMONIALS = [
  { name: 'Rahul Sharma', city: 'Mumbai, Zomato Partner', text: 'Mumbai ki baarish mein delivery karna mushkil hota tha — ab GigShield mere saath hai. ₹800 payout 5 minutes mein aaya!', rating: 5, avatar: 'R' },
  { name: 'Priya Nair', city: 'Delhi, Swiggy Partner', text: 'Delhi ki garmi mein kaam karna impossible tha. GigShield ne heat wave ke time mera nuksaan cover kiya.', rating: 5, avatar: 'P' },
  { name: 'Suresh Babu', city: 'Bengaluru, Zomato Partner', text: 'Sabse acchi baat — kuch karna nahi padta. System automatic claim karke UPI mein paisa bhej deta hai.', rating: 5, avatar: 'S' },
];

const HOW_IT_WORKS = [
  { step: '01', title: 'Register in 2 minutes', desc: 'Mobile OTP verification + your Zomato/Swiggy details. No paperwork.', icon: '📱' },
  { step: '02', title: 'Choose your plan', desc: 'Pick BASIC, STANDARD, or PREMIUM. All weekly pricing from ₹29/week.', icon: '📋' },
  { step: '03', title: 'Work as usual', desc: 'Our system monitors weather, AQI, and civic alerts 24/7 in your zone.', icon: '🛵' },
  { step: '04', title: 'Get paid automatically', desc: 'When a disruption hits your zone, we detect it and pay you within 5 minutes.', icon: '💸' },
];

function AnimatedCounter({ target, prefix = '', suffix = '' }) {
  const [count, setCount] = useState(0);
  const ref = useRef(null);
  const started = useRef(false);

  useEffect(() => {
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && !started.current) {
        started.current = true;
        const step = target / 60;
        let current = 0;
        const timer = setInterval(() => {
          current = Math.min(current + step, target);
          setCount(Math.floor(current));
          if (current >= target) clearInterval(timer);
        }, 25);
      }
    });
    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, [target]);

  return <span ref={ref}>{prefix}{count}{suffix}</span>;
}

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white font-sans">
      {/* Navbar */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-sm border-b border-border">
        <div className="gs-container flex items-center justify-between h-16">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center">
              <Shield size={18} className="text-white" />
            </div>
            <span className="font-bold text-dark text-xl">GigShield</span>
          </div>
          <div className="hidden md:flex items-center gap-8">
            <a href="#how-it-works" className="text-sm font-medium text-slate hover:text-primary transition-colors">How It Works</a>
            <a href="#plans" className="text-sm font-medium text-slate hover:text-primary transition-colors">Plans</a>
            <a href="#triggers" className="text-sm font-medium text-slate hover:text-primary transition-colors">What We Cover</a>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/login" className="text-sm font-semibold text-primary hover:text-primary-dark transition-colors hidden sm:block">Login</Link>
            <Link to="/register" className="gs-btn-accent text-sm py-2 px-5">Protect My Income →</Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero-gradient min-h-screen flex items-center pt-16">
        <div className="gs-container py-20">
          <div className="max-w-4xl mx-auto text-center">
            <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
              <span className="inline-flex items-center gap-2 bg-white/10 text-white/80 text-sm px-4 py-1.5 rounded-full mb-6 border border-white/20">
                <Zap size={14} className="text-accent" /> Guidewire DEVTrails 2026 Hackathon
              </span>
              <h1 className="text-4xl md:text-6xl font-extrabold text-white leading-tight mb-6">
                Your Income.<br />
                <span className="text-accent">Protected.</span><br />
                Automatically.
              </h1>
              <p className="text-xl text-white/80 mb-8 max-w-2xl mx-auto">
                India's first parametric income insurance for food delivery partners.
                When disruptions stop your earnings, we pay — automatically, within 5 minutes.
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Link to="/register" className="gs-btn-accent text-base px-8 py-4 w-full sm:w-auto text-center">
                  Protect My Income <ArrowRight size={18} className="inline ml-1" />
                </Link>
                <a href="#how-it-works" className="border-2 border-white/30 text-white font-bold py-4 px-8 rounded-xl hover:bg-white/10 transition-all w-full sm:w-auto text-center">
                  See How It Works
                </a>
              </div>
            </motion.div>

            {/* Stats */}
            <motion.div initial={{ opacity: 0, y: 40 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4, duration: 0.6 }}
              className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-16">
              {STATS.map((stat, i) => (
                <div key={i} className="bg-white/10 backdrop-blur rounded-2xl p-4 border border-white/20">
                  <div className="text-3xl font-extrabold text-white">
                    <AnimatedCounter target={stat.value} prefix={stat.prefix} suffix={stat.suffix} />
                  </div>
                  <div className="text-white/60 text-sm mt-1">{stat.label}</div>
                </div>
              ))}
            </motion.div>
          </div>
        </div>
      </section>

      {/* Alert Banner */}
      <div className="bg-accent text-white py-3 text-center text-sm font-semibold">
        🔔 Live: Heavy rain alert detected in Mumbai — 234 auto-claims triggered, ₹1.87L payouts initiated
      </div>

      {/* How It Works */}
      <section id="how-it-works" className="gs-section bg-bg-light">
        <div className="gs-container">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-extrabold text-dark mb-4">How GigShield Works</h2>
            <p className="text-slate text-lg max-w-2xl mx-auto">Zero paperwork. No claim forms. Just automatic protection.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {HOW_IT_WORKS.map((step, i) => (
              <motion.div key={i} initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.15, duration: 0.5 }} viewport={{ once: true }}
                className="gs-card-hover text-center relative">
                <div className="text-4xl mb-4">{step.icon}</div>
                <div className="text-primary font-black text-xs mb-2">{step.step}</div>
                <h3 className="font-bold text-dark text-lg mb-2">{step.title}</h3>
                <p className="text-slate text-sm">{step.desc}</p>
                {i < 3 && <div className="hidden md:block absolute top-1/2 -right-3 text-primary opacity-30 text-2xl">→</div>}
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Trigger Showcase */}
      <section id="triggers" className="gs-section bg-white">
        <div className="gs-container">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-extrabold text-dark mb-4">5 Disruptions We Cover</h2>
            <p className="text-slate text-lg">We monitor in real-time and trigger payouts automatically.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
            {TRIGGERS.map((t, i) => (
              <motion.div key={i} initial={{ opacity: 0, scale: 0.9 }} whileInView={{ opacity: 1, scale: 1 }}
                transition={{ delay: i * 0.1 }} viewport={{ once: true }}
                className={`${t.bg} rounded-2xl p-5 border border-white hover:shadow-card-hover transition-all duration-300 group cursor-default`}>
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${t.color} flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                  <t.icon size={22} className="text-white" />
                </div>
                <h3 className="font-bold text-dark mb-1">{t.label}</h3>
                <p className="text-slate text-xs">{t.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Pricing Plans */}
      <section id="plans" className="gs-section bg-bg-light">
        <div className="gs-container">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-extrabold text-dark mb-4">Simple Weekly Pricing</h2>
            <p className="text-slate text-lg">No monthly traps. No annual lock-ins. Just weekly protection.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl mx-auto">
            {PLANS.map((plan, i) => (
              <motion.div key={i} initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.15 }} viewport={{ once: true }}
                className={`gs-card relative border-2 ${plan.color} ${plan.popular ? 'md:-mt-4 md:mb-4' : ''}`}>
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-primary text-white text-xs font-bold px-4 py-1 rounded-full">
                    Most Popular
                  </div>
                )}
                <div className="mb-6">
                  <h3 className="font-black text-dark text-xl mb-1">{plan.tier}</h3>
                  <div className="flex items-end gap-1">
                    <span className="text-4xl font-extrabold text-dark">₹{plan.price}</span>
                    <span className="text-slate text-sm mb-1">–{plan.maxPrice}/week</span>
                  </div>
                  <p className="text-sm text-slate mt-1">Up to ₹{plan.payout} payout/week</p>
                </div>
                <ul className="space-y-2 mb-6">
                  {plan.features.map((f, j) => (
                    <li key={j} className="flex items-center gap-2 text-sm text-slate">
                      <CheckCircle size={14} className="text-success shrink-0" /> {f}
                    </li>
                  ))}
                </ul>
                <Link to="/register" className={`block text-center ${plan.btnClass}`}>
                  Get {plan.tier} Plan
                </Link>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="gs-section bg-white">
        <div className="gs-container">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-extrabold text-dark mb-4">Workers Love GigShield</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {TESTIMONIALS.map((t, i) => (
              <motion.div key={i} initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.15 }} viewport={{ once: true }} className="gs-card-hover">
                <div className="flex gap-1 mb-4">
                  {Array(t.rating).fill(0).map((_, j) => <Star key={j} size={16} className="text-warning fill-warning" />)}
                </div>
                <p className="text-slate text-sm mb-4 italic">"{t.text}"</p>
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gradient-to-br from-primary to-accent rounded-full flex items-center justify-center text-white font-bold">
                    {t.avatar}
                  </div>
                  <div>
                    <p className="font-semibold text-dark text-sm">{t.name}</p>
                    <p className="text-xs text-slate">{t.city}</p>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="hero-gradient py-20">
        <div className="gs-container text-center">
          <h2 className="text-3xl md:text-4xl font-extrabold text-white mb-4">Ready to Protect Your Income?</h2>
          <p className="text-white/80 text-lg mb-8">Join thousands of delivery partners who never lose income to disruptions.</p>
          <Link to="/register" className="gs-btn-accent text-base px-10 py-4 inline-block">
            Register Now — It's Free to Start →
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-dark text-white/60 py-8">
        <div className="gs-container flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <Shield size={20} className="text-primary" />
            <span className="font-bold text-white">GigShield</span>
          </div>
          <p className="text-sm">© 2026 GigShield — Guidewire DEVTrails Hackathon | Coverage: Income Loss Only</p>
          <div className="flex gap-4 text-sm">
            <a href="#" className="hover:text-white transition-colors">Privacy</a>
            <a href="#" className="hover:text-white transition-colors">Terms</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
