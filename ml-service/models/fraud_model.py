"""
GigShield Fraud Detection Model
Isolation Forest anomaly detection trained on synthetic claim data.
Flags: location mismatch, duplicate claims, inactive workers, high frequency, payout anomaly.
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
import random

np.random.seed(42)
random.seed(42)


def generate_fraud_training_data(n=1200):
    """Generate synthetic claim data: 80% legitimate, 20% fraudulent"""
    records = []
    for i in range(n):
        is_fraud = i < int(n * 0.2)

        if is_fraud:
            location_distance = random.uniform(60, 200)   # Far from event
            duplicate_count = random.randint(2, 5)
            days_active = random.randint(0, 7)
            recent_claims = random.randint(4, 10)
            policy_age = random.randint(0, 1)
            claim_ratio = random.uniform(1.5, 3.0)
        else:
            location_distance = random.uniform(0, 30)     # Close to event
            duplicate_count = random.randint(0, 1)
            days_active = random.randint(30, 365)
            recent_claims = random.randint(0, 2)
            policy_age = random.randint(7, 180)
            claim_ratio = random.uniform(0.2, 1.2)

        records.append({
            "location_distance": location_distance,
            "duplicate_count": duplicate_count,
            "days_active": days_active,
            "recent_claims": recent_claims,
            "policy_age": policy_age,
            "claim_ratio": claim_ratio,
            "label": 1 if is_fraud else 0
        })
    return pd.DataFrame(records)


class FraudModel:
    def __init__(self):
        self.model = None
        self._train()

    def _train(self):
        df = generate_fraud_training_data(1200)
        # Train only on legitimate samples (anomaly detection)
        legit = df[df["label"] == 0]
        features = ["location_distance", "duplicate_count", "days_active",
                    "recent_claims", "policy_age", "claim_ratio"]
        X_train = legit[features]

        self.model = IsolationForest(
            n_estimators=150, contamination=0.1,
            random_state=42, n_jobs=-1
        )
        self.model.fit(X_train)

    def score(self, location_distance: float, duplicate_count: int,
              days_active: int, recent_claims: int,
              policy_age_days: int, claim_amount: float,
              avg_earnings: float = 800.0):
        """
        Returns fraud score (0-100) and flags.
        Higher score = more suspicious.
        """
        claim_ratio = claim_amount / max(avg_earnings, 1)

        features = np.array([[location_distance, duplicate_count, days_active,
                               recent_claims, policy_age_days, claim_ratio]])

        # Isolation Forest decision: -1 = anomaly, 1 = normal
        decision = self.model.decision_function(features)[0]
        # Convert to 0–100 fraud score (lower decision = more anomalous)
        fraud_score = max(0, min(100, (0.5 - decision) * 100))

        # Rule-based flags to supplement ML score
        flags = []
        score_addition = 0

        if location_distance > 50:
            flags.append("LOCATION_MISMATCH")
            score_addition += 25

        if duplicate_count > 1:
            flags.append("DUPLICATE_CLAIM_WITHIN_24H")
            score_addition += 30

        if days_active < 7:
            flags.append("INACTIVE_WORKER")
            score_addition += 15

        if recent_claims > 3:
            flags.append("HIGH_CLAIM_FREQUENCY")
            score_addition += 10

        if policy_age_days < 2:
            flags.append("NEW_POLICY_CLAIM")
            score_addition += 10

        if claim_ratio > 1.5:
            flags.append("PAYOUT_EXCEEDS_EARNINGS")
            score_addition += 15

        final_score = min(100, fraud_score * 0.4 + score_addition * 0.6)

        if final_score >= 70:
            recommendation = "REJECT"
        elif final_score >= 30:
            recommendation = "REVIEW"
        else:
            recommendation = "APPROVE"

        return {
            "fraudScore": round(final_score, 2),
            "flags": flags,
            "recommendation": recommendation
        }


_fraud_model_instance = None

def get_fraud_model():
    global _fraud_model_instance
    if _fraud_model_instance is None:
        _fraud_model_instance = FraudModel()
    return _fraud_model_instance
