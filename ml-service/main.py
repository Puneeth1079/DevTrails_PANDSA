from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import premium, fraud

app = FastAPI(
    title="GigShield ML Service",
    description="AI/ML microservice for premium calculation and fraud detection",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:5173", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(premium.router, tags=["Premium"])
app.include_router(fraud.router, tags=["Fraud"])


@app.get("/health")
async def health():
    return {"status": "healthy", "service": "GigShield ML Service", "version": "1.0.0"}


@app.get("/")
async def root():
    return {
        "service": "GigShield ML Microservice",
        "endpoints": ["/calculate-premium", "/fraud-score", "/health", "/docs"]
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
