# 🛡️ GigShield — AI-Powered Parametric Income Insurance for Food Delivery Partners

> **Guidewire DEVTrails 2026 | University Hackathon**
> Protecting India's Zomato & Swiggy delivery partners from income loss due to uncontrollable external disruptions.

---

## 📌 Problem Statement

India's food delivery partners (Zomato, Swiggy) are the backbone of our urban food economy. However, external disruptions — heavy rain, extreme heat, severe pollution, flash floods, and sudden bandhs — can halt their work entirely, causing them to lose **20–30% of their monthly earnings** with zero protection or safety net.

**When a Swiggy delivery partner named Ravi cannot step out due to a sudden downpour, he loses ₹400–₹600 that evening. Nobody compensates him. GigShield changes that.**

---

## 👤 Our Persona: Ravi — The Food Delivery Partner

| Attribute | Detail |
|---|---|
| Name | Ravi Kumar |
| Platform | Zomato / Swiggy |
| City | Bengaluru / Hyderabad |
| Daily Working Hours | 8–12 hours/day |
| Average Weekly Income | ₹3,000 – ₹5,000/week |
| Peak Hours | 12–2 PM (Lunch), 7–10 PM (Dinner) |
| Risk Exposure | Outdoor, 2-wheeler, rain/heat exposed |
| Pain Point | Zero income protection during disruptions |

### Persona Scenarios

**Scenario 1 — Heavy Monsoon Rain (Most Common)**
> It's a Tuesday evening in Bengaluru. Rainfall crosses 50mm in Zone 4. Zomato suspends deliveries. Ravi loses his entire ₹600 dinner shift. GigShield's weather trigger fires automatically → Ravi receives ₹350 in his UPI wallet within minutes. Zero paperwork.

**Scenario 2 — Extreme Heat Advisory**
> Hyderabad hits 44°C at 1 PM. A government health advisory discourages outdoor activity. Ravi stops working 3 hours early to protect himself. GigShield detects the heat index threshold breach → Triggers a partial payout of ₹180 for 3 lost hours.

**Scenario 3 — Sudden City Bandh**
> A political bandh is declared in the city. All restaurants shut by 11 AM. Zero orders are available for 8 hours. GigShield detects the civic disruption via news/traffic API → Triggers a full-day payout of ₹500.

**Scenario 4 — Severe Pollution (AQI > 300)**
> Delhi-level smog descends on a city. AQI crosses 300 (Very Poor). Outdoor workers are advised to stay indoors. GigShield detects sustained AQI breach → Triggers payout for disrupted hours.

---

## 💡 Our Solution: GigShield

GigShield is an **AI-enabled parametric insurance platform** that:
- Monitors real-time environmental and social disruption signals
- Automatically triggers claims when predefined thresholds are crossed
- Processes instant payouts to delivery workers — **zero human intervention**
- Operates on a **weekly subscription model** aligned with gig worker earnings cycles

---

## 💰 Weekly Premium Model

### Pricing Tiers

| Plan | Weekly Premium | Max Weekly Coverage | Best For |
|---|---|---|---|
| Basic Shield | ₹29/week | ₹500 | New / part-time workers |
| Standard Guard | ₹49/week | ₹1,000 | Regular full-time workers |
| Pro Protect | ₹79/week | ₹1,800 | High-earning / peak workers |

### Dynamic Pricing Formula (AI-Driven)

```
Weekly Premium = Base Rate
               × Zone Risk Multiplier        [flood-prone = 1.3x, safe zone = 0.9x]
               × Weather Season Factor       [monsoon = 1.2x, summer = 1.1x, normal = 1.0x]
               × Worker History Factor       [no past claims = 0.95x, frequent claims = 1.1x]
               × Coverage Tier Factor
```

**Example Calculation:**
> Ravi, Bengaluru, July (Monsoon), Zone 3 (flood-prone), Standard Guard:
> ₹49 × 1.3 × 1.2 × 0.95 = **₹72.7/week** (dynamically calculated, never static)

### Why Weekly?
- Food delivery workers are paid weekly by Zomato/Swiggy
- Weekly micro-premiums (₹29–₹79) are affordable and fit the income cycle
- Workers can pause/resume coverage weekly based on their work schedule
- Reduces financial commitment compared to monthly/annual plans

---

## ⚡ Parametric Triggers (5 Automated Triggers)

| # | Trigger | Condition | Payout Amount | Data Source |
|---|---|---|---|---|
| 1 | Heavy Rain | Rainfall > 35mm in 3 hrs in worker's active zone | ₹150–₹300 | OpenWeatherMap API |
| 2 | Extreme Heat | Temperature > 42°C between 11AM–4PM | ₹100–₹200 | OpenWeatherMap API |
| 3 | Severe AQI | AQI > 300 (Very Poor) sustained 4+ hours | ₹100–₹150 | OpenAQ API (free) |
| 4 | Flood Alert | IMD flood alert issued for worker's pincode | ₹300–₹500 | IMD Mock / Govt API |
| 5 | Bandh/Curfew | Govt curfew or declared city strike | ₹200–₹400 | News API / Mock |

**Payout Logic:**
- Payout is proportional to hours lost (confirmed by platform inactivity data)
- Maximum one payout per trigger type per day per worker
- Overlapping triggers (e.g., rain + flood) pay the higher amount only

---

## 🤖 AI/ML Integration Plan

### 1. Dynamic Premium Engine (Week 2–3)
- **Model**: Weighted scoring algorithm in Spring Boot
- **Inputs**: Zone (pincode), city, historical weather frequency, worker's 30-day activity data, past claims
- **Output**: Risk score (0–100) → maps to weekly premium
- **Logic**: Rule-based ML model trained on historical weather + claim data

### 2. Fraud Detection System (Week 3–4)
- **GPS Validation**: Was the worker's last known location in the disruption zone?
- **Timing Anomaly Check**: Did the worker stop activity exactly when the event started? (suspiciously precise = flag)
- **Duplicate Detection**: Same worker, same event, same day = auto-reject
- **Cross-reference**: Compare claimed hours vs actual platform inactivity log
- **Fraud Score**: 0–100 (>70 = manual review, >90 = auto-reject)

### 3. Predictive Risk Analytics (Week 5–6)
- Use OpenWeatherMap 5-day forecast to predict next week's disruption probability
- Adjust premium recommendations proactively before the week starts
- Admin dashboard shows "Expected High-Risk Days Next Week"

---

## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      FRONTEND LAYER                          │
│  React.js Web App              React Native Mobile App       │
│  ├── Worker Dashboard          ├── Worker Onboarding         │
│  ├── Policy Management         ├── Active Policy View        │
│  ├── Claims & Payouts View     ├── Disruption Alerts         │
│  └── Admin Analytics Portal   └── Payout Notifications      │
└────────────────────────┬─────────────────────────────────────┘
                         │ REST APIs (JSON/JWT Auth)
┌────────────────────────▼─────────────────────────────────────┐
│                   SPRING BOOT BACKEND                        │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐    │
│  │ Auth Service │  │Policy Service│  │  Claims Service  │    │
│  │ (JWT/Spring  │  │ (CRUD, Tier  │  │ (Auto-trigger,  │    │
│  │  Security)   │  │  Management) │  │  Fraud Check)   │    │
│  └──────────────┘  └──────────────┘  └─────────────────┘    │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐    │
│  │ Premium AI   │  │ Fraud Engine │  │  Payout Service  │    │
│  │ (Risk Score  │  │ (Anomaly     │  │  (Mock Razorpay  │    │
│  │  Calculator) │  │  Detection)  │  │   UPI Gateway)  │    │
│  └──────────────┘  └──────────────┘  └─────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐     │
│  │           Disruption Monitor (Scheduler)            │     │
│  │  Polls APIs every 15 mins → evaluates triggers      │     │
│  └─────────────────────────────────────────────────────┘     │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                     MySQL DATABASE                           │
│  workers │ policies │ disruption_events │ claims │ payouts   │
│  zones   │ premium_history │ fraud_logs │ audit_trail        │
└────────────────────────┬─────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────┐
│                  EXTERNAL INTEGRATIONS                       │
│  OpenWeatherMap API  │  OpenAQ API  │  News API (mock)       │
│  IMD Alerts (mock)   │  Razorpay Test Mode / UPI Simulator   │
│  Zomato/Swiggy API (simulated)  │  Google Maps (zones)       │
└──────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema (MySQL)

### Core Tables

**workers**
```sql
id, name, phone, email, city, pincode, zone_id,
platform (ZOMATO/SWIGGY), avg_weekly_income,
risk_score, upi_id, kyc_verified, created_at
```

**policies**
```sql
id, worker_id, plan_type (BASIC/STANDARD/PRO),
weekly_premium, coverage_amount, start_date,
end_date, status (ACTIVE/PAUSED/EXPIRED), auto_renew
```

**disruption_events**
```sql
id, event_type (RAIN/HEAT/AQI/FLOOD/BANDH),
zone_id, city, severity_level, started_at,
ended_at, data_source, api_verified, raw_data_json
```

**claims**
```sql
id, worker_id, policy_id, event_id, hours_lost,
amount_claimed, amount_approved, status,
fraud_score, auto_approved, reviewed_by, created_at
```

**payouts**
```sql
id, claim_id, worker_id, amount, upi_id,
gateway_ref, gateway_response, status, paid_at
```

---

## 🛠️ Tech Stack

| Layer | Technology | Justification |
|---|---|---|
| Web Frontend | React.js + Tailwind CSS | Fast, component-based, great ecosystem |
| Mobile App | React Native | Code reuse with React web, single JS codebase |
| Backend API | Spring Boot (Java 17) | Robust, production-grade REST APIs |
| Database | MySQL 8.0 | Relational integrity for financial/insurance data |
| Authentication | Spring Security + JWT | Industry standard secure auth |
| AI/ML | Spring Boot Rule Engine + Python Scripts | Premium scoring + fraud detection |
| Weather API | OpenWeatherMap (Free Tier) | Real rainfall, temp, AQI data |
| AQI API | OpenAQ (Free) | Pollution monitoring |
| Payments | Razorpay Test Mode | Mock UPI instant payouts |
| Build Tool | Maven | Standard Java build |
| Version Control | GitHub | Hackathon requirement |
| Deployment | Railway.app / AWS Free Tier | Easy CI/CD |

---

## 📅 6-Week Development Plan

| Week | Phase | Focus | Key Deliverable |
|---|---|---|---|
| 1 (Mar 4–10) | Phase 1 | Ideation + Research | Persona, triggers, schema design |
| 2 (Mar 11–20) | Phase 1 | Foundation | README, Spring Boot skeleton, React setup |
| 3 (Mar 21–27) | Phase 2 | Core Features | Registration, policy CRUD, premium calc |
| 4 (Mar 28–Apr 4) | Phase 2 | Automation | Trigger monitoring, claims, fraud detection |
| 5 (Apr 5–11) | Phase 3 | Advanced Features | Advanced fraud, payout simulation |
| 6 (Apr 12–17) | Phase 3 | Polish + Submit | Dashboards, demo video, pitch deck |

---

## 📦 Deliverables Summary

- [x] Phase 1: README.md + GitHub Repo + 2-min Video (Due: March 20)
- [ ] Phase 2: Working Demo — Registration, Policy, Premium, Claims (Due: April 4)
- [ ] Phase 3: Fraud Detection + Dashboard + Final Pitch Deck (Due: April 17)

---

## 👥 Team Members

| Name | Role |
|---|---|
| Member 1 | Backend (Spring Boot + MySQL) |
| Member 2 | Frontend (React.js + React Native) |
| Member 3 | AI/ML + API Integrations |
| Member 4 | UI/UX + Documentation |

---

## 🚫 Exclusions (As Per Problem Statement)

This platform strictly does NOT cover:
- Health insurance or medical bills
- Life insurance
- Accident coverage
- Vehicle repair costs
- Any event not directly causing income loss

---

*Built for Guidewire DEVTrails 2026 University Hackathon*
