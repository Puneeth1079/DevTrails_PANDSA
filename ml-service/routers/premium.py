from fastapi import APIRouter
from schemas import PremiumRequest, PremiumResponse, RiskBreakdown
from models.premium_model import get_premium_model
import datetime

router = APIRouter()


@router.post("/calculate-premium", response_model=PremiumResponse)
async def calculate_premium(req: PremiumRequest):
    """
    Calculate weekly insurance premium for a gig worker.
    Uses XGBoost model trained on 1200 synthetic Indian gig worker samples.
    """
    model = get_premium_model()
    month = req.month if req.month else datetime.date.today().month
    result = model.predict(
        city=req.city,
        zone=req.zone or "",
        avg_earnings=req.avgDailyEarnings,
        platform=req.platform,
        renewal_count=req.renewalCount,
        month=month,
        tier=req.coverageTier
    )

    breakdown = result["riskBreakdown"]
    return PremiumResponse(
        weeklyPremium=result["weeklyPremium"],
        riskScore=result["riskScore"],
        riskBreakdown=RiskBreakdown(
            cityFactor=breakdown["cityFactor"],
            seasonFactor=breakdown["seasonFactor"],
            zoneFactor=breakdown["zoneFactor"],
            loyaltyDiscount=breakdown["loyaltyDiscount"],
            autoRenewDiscount=breakdown["autoRenewDiscount"]
        )
    )
