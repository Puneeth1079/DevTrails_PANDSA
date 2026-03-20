"""
GigShield Premium Calculation Model
Uses XGBoost trained on 1000 synthetic samples of Indian gig worker data.
Features: city risk, season factor, zone risk, earnings, platform, renewal history
"""

import numpy as np
import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error

try:
    from xgboost import XGBRegressor
    HAS_XGB = True
except ImportError:
    HAS_XGB = False

import random

# Seed for reproducibility
np.random.seed(42)
random.seed(42)

# ── Synthetic Training Data ────────────────────────────────────────────────────
def generate_synthetic_data(n=1000):
    cities = ["Mumbai", "Delhi", "Bengaluru", "Chennai", "Hyderabad", "Pune", "Kolkata", "Ahmedabad"]
    platforms = ["ZOMATO", "SWIGGY"]
    tiers = ["BASIC", "STANDARD", "PREMIUM"]
    base_premiums = {"BASIC": 35, "STANDARD": 65, "PREMIUM": 109}
    city_factors = {
        "Mumbai": 1.2, "Delhi": 1.2, "Chennai": 1.2,
        "Bengaluru": 1.1, "Hyderabad": 1.1, "Kolkata": 1.1,
        "Pune": 1.0, "Ahmedabad": 1.0
    }

    records = []
    for _ in range(n):
        city = random.choice(cities)
        platform = random.choice(platforms)
        tier = random.choice(tiers)
        earnings = random.uniform(400, 2000)
        renewal_count = random.randint(0, 10)
        month = random.randint(1, 12)
        zone_risk = random.uniform(0, 12)

        # Season factor
        season_factor = 1.15 if 6 <= month <= 9 else (0.95 if month >= 11 or month <= 2 else 1.0)
        city_factor = city_factors.get(city, 0.85)
        loyalty = 5.0 if renewal_count > 2 else 0.0
        base = base_premiums[tier]

        # Ground truth premium
        premium = base * city_factor * season_factor + zone_risk - loyalty
        premium = max(base * 0.8, premium)
        premium = round(premium)

        # Risk score (0–100)
        risk = 50 + (city_factor - 1.0) * 100 + zone_risk - (earnings / 200)
        risk = max(0, min(100, risk))

        records.append({
            "city": city, "platform": platform, "tier": tier,
            "earnings": earnings, "renewal_count": renewal_count,
            "month": month, "zone_risk": zone_risk,
            "city_factor": city_factor, "season_factor": season_factor,
            "premium": premium, "risk_score": risk
        })

    return pd.DataFrame(records)


class PremiumModel:
    def __init__(self):
        self.model = None
        self.risk_model = None
        self._train()

    def _train(self):
        df = generate_synthetic_data(1200)

        # Encode city and tier
        df["city_encoded"] = df["city"].map({
            "Mumbai": 3, "Delhi": 3, "Chennai": 3,
            "Bengaluru": 2, "Hyderabad": 2, "Kolkata": 2,
            "Pune": 1, "Ahmedabad": 1
        }).fillna(0)
        df["tier_encoded"] = df["tier"].map({"BASIC": 0, "STANDARD": 1, "PREMIUM": 2})
        df["platform_encoded"] = df["platform"].map({"ZOMATO": 0, "SWIGGY": 1})

        features = ["city_encoded", "tier_encoded", "platform_encoded",
                    "earnings", "renewal_count", "month", "zone_risk",
                    "city_factor", "season_factor"]

        X = df[features]
        y_premium = df["premium"]
        y_risk = df["risk_score"]

        if HAS_XGB:
            self.model = XGBRegressor(n_estimators=100, learning_rate=0.1, max_depth=5, random_state=42)
            self.risk_model = XGBRegressor(n_estimators=100, learning_rate=0.1, max_depth=4, random_state=42)
        else:
            from sklearn.ensemble import GradientBoostingRegressor
            self.model = GradientBoostingRegressor(n_estimators=100, learning_rate=0.1, max_depth=5, random_state=42)
            self.risk_model = GradientBoostingRegressor(n_estimators=100, learning_rate=0.1, max_depth=4, random_state=42)

        self.model.fit(X, y_premium)
        self.risk_model.fit(X, y_risk)

    def predict(self, city, zone, avg_earnings, platform, renewal_count, month, tier):
        city_map = {
            "mumbai": 3, "delhi": 3, "chennai": 3,
            "bengaluru": 2, "hyderabad": 2, "kolkata": 2,
            "pune": 1, "ahmedabad": 1
        }
        city_factor_map = {
            "mumbai": 1.2, "delhi": 1.2, "chennai": 1.2,
            "bengaluru": 1.1, "hyderabad": 1.1, "kolkata": 1.1,
            "pune": 1.0, "ahmedabad": 1.0
        }
        tier_map = {"BASIC": 0, "STANDARD": 1, "PREMIUM": 2}
        platform_map = {"ZOMATO": 0, "SWIGGY": 1}

        city_lower = city.lower()
        city_encoded = city_map.get(city_lower, 0)
        city_factor = city_factor_map.get(city_lower, 0.85)
        season_factor = 1.15 if 6 <= month <= 9 else (0.95 if month >= 11 or month <= 2 else 1.0)
        zone_risk = 8.0 if zone and any(z in zone.lower() for z in ["andheri", "dharavi", "kurla"]) else 3.0
        tier_encoded = tier_map.get(tier.upper(), 1)
        platform_encoded = platform_map.get(platform.upper(), 0)

        X = np.array([[city_encoded, tier_encoded, platform_encoded,
                        avg_earnings, renewal_count, month, zone_risk,
                        city_factor, season_factor]])

        premium = float(self.model.predict(X)[0])
        risk = float(self.risk_model.predict(X)[0])

        return {
            "weeklyPremium": max(25, round(premium)),
            "riskScore": max(0, min(100, round(risk, 2))),
            "riskBreakdown": {
                "cityFactor": city_factor,
                "seasonFactor": season_factor,
                "zoneFactor": zone_risk,
                "loyaltyDiscount": 5.0 if renewal_count > 2 else 0.0,
                "autoRenewDiscount": 0.0
            }
        }


# Singleton instance
_model_instance = None

def get_premium_model():
    global _model_instance
    if _model_instance is None:
        _model_instance = PremiumModel()
    return _model_instance
