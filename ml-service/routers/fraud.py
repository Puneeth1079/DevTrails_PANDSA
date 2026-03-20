from fastapi import APIRouter
from schemas import FraudRequest, FraudResponse
from models.fraud_model import get_fraud_model

router = APIRouter()


@router.post("/fraud-score", response_model=FraudResponse)
async def fraud_score(req: FraudRequest):
    """
    Score an insurance claim for fraud risk using Isolation Forest.
    Returns score (0-100), flags, and recommendation.
    """
    model = get_fraud_model()

    # Parse GPS strings (format: "lat,lng")
    location_distance = 5.0  # Default: within zone
    if req.workerGps and req.eventGps:
        try:
            wlat, wlng = map(float, req.workerGps.split(","))
            elat, elng = map(float, req.eventGps.split(","))
            # Rough distance (Euclidean in degrees × 111km/degree)
            location_distance = ((wlat - elat)**2 + (wlng - elng)**2)**0.5 * 111
        except Exception:
            location_distance = 5.0

    result = model.score(
        location_distance=location_distance,
        duplicate_count=max(0, req.recentClaimsCount - 1),
        days_active=req.daysActive,
        recent_claims=req.recentClaimsCount,
        policy_age_days=req.policyAgeDays,
        claim_amount=req.claimAmount,
        avg_earnings=800.0
    )

    return FraudResponse(
        fraudScore=result["fraudScore"],
        flags=result["flags"],
        recommendation=result["recommendation"]
    )
