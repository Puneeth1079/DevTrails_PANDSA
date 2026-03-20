from pydantic import BaseModel
from typing import Optional, List


class PremiumRequest(BaseModel):
    city: str
    zone: Optional[str] = None
    avgDailyEarnings: float = 800.0
    platform: str = "ZOMATO"
    renewalCount: int = 0
    month: int = 1
    coverageTier: str = "STANDARD"


class RiskBreakdown(BaseModel):
    cityFactor: float
    seasonFactor: float
    zoneFactor: float
    loyaltyDiscount: float
    autoRenewDiscount: float


class PremiumResponse(BaseModel):
    weeklyPremium: float
    riskScore: float
    riskBreakdown: RiskBreakdown


class FraudRequest(BaseModel):
    workerId: int
    claimAmount: float
    claimTriggerType: str
    workerGps: Optional[str] = None
    eventGps: Optional[str] = None
    daysActive: int = 90
    recentClaimsCount: int = 0
    policyAgeDays: int = 30
    coverageTier: str = "STANDARD"


class FraudResponse(BaseModel):
    fraudScore: float
    flags: List[str]
    recommendation: str  # APPROVE | REVIEW | REJECT
