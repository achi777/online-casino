from fastapi import FastAPI, Depends, HTTPException, Header, Request
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from contextlib import asynccontextmanager
from datetime import datetime
import secrets
import os
from dotenv import load_dotenv

from database import get_db, init_db
from models import GameSession, GameEvent, GameStatus, EventType
from schemas import (
    StartGameRequest, StartGameResponse,
    CoinCollectRequest, CoinCollectResponse,
    CashOutRequest, CashOutResponse,
    GameOverRequest, GameOverResponse,
    GameStatsResponse
)
from fraud_detection import FraudDetectionService
from main_backend_client import MainBackendClient

load_dotenv()

# Configuration
BET_AMOUNT = float(os.getenv("BET_AMOUNT", "5.0"))
COIN_VALUE = float(os.getenv("COIN_VALUE", "0.1"))
MAX_WIN_AMOUNT = float(os.getenv("MAX_WIN_AMOUNT", "100.0"))
INITIAL_SPEED = float(os.getenv("INITIAL_SPEED", "180"))
SPEED_MULTIPLIER = float(os.getenv("SPEED_MULTIPLIER", "0.95"))
MIN_SPEED = float(os.getenv("MIN_SPEED", "30"))
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))

# Initialize limiter
limiter = Limiter(key_func=get_remote_address)

# Lifespan context manager for startup/shutdown
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Initialize database
    await init_db()
    yield
    # Shutdown: cleanup if needed

# Create FastAPI app
app = FastAPI(
    title="Snake Game Backend",
    description="Production-ready backend for Snake game with fraud detection",
    version="1.0.0",
    lifespan=lifespan
)

# Add rate limiting
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8888", "http://localhost:3000", "http://localhost:3001"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize backend client
backend_client = MainBackendClient()

# Dependency to verify JWT token
async def get_current_user(authorization: str = Header(None)):
    if not authorization:
        raise HTTPException(status_code=401, detail="Authorization header missing")

    user_info = backend_client.verify_jwt_token(authorization)
    if not user_info:
        raise HTTPException(status_code=401, detail="Invalid or expired token")

    return user_info


@app.get("/")
async def root():
    return {
        "service": "Snake Game Backend",
        "version": "1.0.0",
        "status": "running"
    }


@app.get("/health")
async def health_check():
    return {"status": "healthy", "timestamp": datetime.utcnow().isoformat()}


@app.get("/api/game/config")
async def get_game_config():
    """Get game configuration (public endpoint - no auth required)"""
    return {
        "bet_amount": BET_AMOUNT,
        "coin_value": COIN_VALUE,
        "max_win_amount": MAX_WIN_AMOUNT,
        "initial_speed": INITIAL_SPEED,
        "speed_multiplier": SPEED_MULTIPLIER,
        "min_speed": MIN_SPEED
    }


@app.post("/api/game/start", response_model=StartGameResponse)
@limiter.limit("10/minute")
async def start_game(
    request: Request,
    game_request: StartGameRequest,
    db: AsyncSession = Depends(get_db),
    user_info: dict = Depends(get_current_user)
):
    """Start a new game session"""

    user_token = user_info["token"]

    # Get user info from main backend
    backend_user_info = await backend_client.get_user_info(user_token)
    if not backend_user_info or "id" not in backend_user_info:
        raise HTTPException(status_code=401, detail="Failed to get user information")

    user_id = backend_user_info["id"]

    # Check if user is flagged as suspicious
    is_suspicious = await FraudDetectionService.is_user_suspicious(db, user_id)
    if is_suspicious:
        raise HTTPException(
            status_code=403,
            detail="Account flagged for suspicious activity. Please contact support."
        )

    # Check if user has active game session
    result = await db.execute(
        select(GameSession)
        .where(GameSession.user_id == user_id)
        .where(GameSession.status == GameStatus.ACTIVE)
    )
    active_session = result.scalar_one_or_none()

    if active_session:
        raise HTTPException(
            status_code=400,
            detail="You already have an active game session. Please complete or cancel it first."
        )

    # Deduct bet amount from user's balance
    success, error = await backend_client.deduct_balance(
        user_token=user_token,
        amount=game_request.bet_amount,
        game_type="SNAKE"
    )

    if not success:
        raise HTTPException(status_code=400, detail=error or "Failed to deduct balance")

    # Create game session
    session_token = secrets.token_urlsafe(32)
    game_session = GameSession(
        session_token=session_token,
        user_id=user_id,
        user_token=user_token,
        bet_amount=game_request.bet_amount,
        status=GameStatus.ACTIVE,
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("user-agent")
    )

    db.add(game_session)

    # Log game start event
    start_event = GameEvent(
        session_token=session_token,
        user_id=user_id,
        event_type=EventType.GAME_START,
        data={"bet_amount": game_request.bet_amount}
    )
    db.add(start_event)

    await db.commit()

    return StartGameResponse(
        session_token=session_token,
        status="ACTIVE",
        start_time=game_session.start_time,
        message="Game started successfully"
    )


@app.post("/api/game/collect-coin", response_model=CoinCollectResponse)
@limiter.limit("30/second")
async def collect_coin(
    request: Request,
    coin_request: CoinCollectRequest,
    db: AsyncSession = Depends(get_db)
):
    """Register a coin collection event"""

    # Validate coin collection
    is_valid, error_msg = await FraudDetectionService.validate_coin_collection(
        db=db,
        session_token=coin_request.session_token,
        coin_position=coin_request.coin_position,
        snake_head=coin_request.snake_head,
        client_timestamp=coin_request.timestamp
    )

    if not is_valid:
        raise HTTPException(status_code=400, detail=error_msg)

    # Get game session
    result = await db.execute(
        select(GameSession).where(GameSession.session_token == coin_request.session_token)
    )
    session = result.scalar_one_or_none()

    if not session:
        raise HTTPException(status_code=404, detail="Game session not found")

    # Update coins collected
    session.coins_collected += 1
    session.win_amount = session.coins_collected * COIN_VALUE

    # Apply max win limit
    if session.win_amount > MAX_WIN_AMOUNT:
        session.win_amount = MAX_WIN_AMOUNT

    # Log coin collection event
    coin_event = GameEvent(
        session_token=coin_request.session_token,
        user_id=session.user_id,
        event_type=EventType.COIN_COLLECTED,
        data={
            "coin_position": coin_request.coin_position,
            "snake_head": coin_request.snake_head,
            "timestamp": coin_request.timestamp,
            "coins_collected": session.coins_collected
        }
    )
    db.add(coin_event)

    await db.commit()

    return CoinCollectResponse(
        coins_collected=session.coins_collected,
        total_amount=session.win_amount,
        message="Coin collected successfully"
    )


@app.post("/api/game/cashout", response_model=CashOutResponse)
@limiter.limit("10/minute")
async def cashout(
    request: Request,
    cashout_request: CashOutRequest,
    db: AsyncSession = Depends(get_db)
):
    """Cash out from an active game"""

    # Validate cashout
    is_valid, error_msg, calculated_win = await FraudDetectionService.validate_cashout(
        db=db,
        session_token=cashout_request.session_token,
        coins_collected=cashout_request.coins_collected,
        final_speed=cashout_request.final_speed,
        game_duration=cashout_request.game_duration
    )

    if not is_valid:
        raise HTTPException(status_code=400, detail=error_msg)

    # Get game session
    result = await db.execute(
        select(GameSession).where(GameSession.session_token == cashout_request.session_token)
    )
    session = result.scalar_one_or_none()

    if not session:
        raise HTTPException(status_code=404, detail="Game session not found")

    # Use server-calculated win amount
    session.win_amount = calculated_win
    session.status = GameStatus.COMPLETED
    session.end_time = datetime.utcnow()

    # Add winnings to user's balance
    if calculated_win > 0:
        success, error = await backend_client.add_balance(
            user_token=session.user_token,
            amount=calculated_win,
            game_type="SNAKE"
        )

        if not success:
            # Log error but don't fail the cashout
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=cashout_request.session_token,
                fraud_type="BALANCE_UPDATE_FAILED",
                description=f"Failed to add winnings: {error}",
                severity="HIGH"
            )

    # Record game session in main backend
    await backend_client.record_game_session(
        user_token=session.user_token,
        game_type="SNAKE",
        bet_amount=session.bet_amount,
        win_amount=session.win_amount,
        session_token=cashout_request.session_token
    )

    # Log cashout event
    cashout_event = GameEvent(
        session_token=cashout_request.session_token,
        user_id=session.user_id,
        event_type=EventType.CASH_OUT,
        data={
            "coins_collected": cashout_request.coins_collected,
            "win_amount": calculated_win,
            "game_duration": cashout_request.game_duration
        }
    )
    db.add(cashout_event)

    await db.commit()

    return CashOutResponse(
        win_amount=calculated_win,
        coins_collected=session.coins_collected,
        status="COMPLETED",
        message=f"Successfully cashed out ${calculated_win:.2f}"
    )


@app.post("/api/game/game-over", response_model=GameOverResponse)
@limiter.limit("10/minute")
async def game_over(
    request: Request,
    game_over_request: GameOverRequest,
    db: AsyncSession = Depends(get_db)
):
    """Handle game over (collision)"""

    # Validate game over
    is_valid, error_msg = await FraudDetectionService.validate_game_over(
        db=db,
        session_token=game_over_request.session_token,
        coins_collected=game_over_request.coins_collected,
        game_duration=game_over_request.game_duration,
        collision_type=game_over_request.collision_type
    )

    if not is_valid:
        raise HTTPException(status_code=400, detail=error_msg)

    # Get game session
    result = await db.execute(
        select(GameSession).where(GameSession.session_token == game_over_request.session_token)
    )
    session = result.scalar_one_or_none()

    if not session:
        raise HTTPException(status_code=404, detail="Game session not found")

    # Player loses - no winnings
    session.win_amount = 0
    session.status = GameStatus.COMPLETED
    session.end_time = datetime.utcnow()

    # Record game session in main backend (as a loss)
    await backend_client.record_game_session(
        user_token=session.user_token,
        game_type="SNAKE",
        bet_amount=session.bet_amount,
        win_amount=0,
        session_token=game_over_request.session_token
    )

    # Log game over event
    game_over_event = GameEvent(
        session_token=game_over_request.session_token,
        user_id=session.user_id,
        event_type=EventType.GAME_OVER,
        data={
            "coins_collected": game_over_request.coins_collected,
            "collision_type": game_over_request.collision_type,
            "game_duration": game_over_request.game_duration
        }
    )
    db.add(game_over_event)

    await db.commit()

    return GameOverResponse(
        coins_lost=session.coins_collected,
        amount_lost=session.bet_amount,
        status="COMPLETED",
        message=f"Game over! You lost ${session.bet_amount:.2f}"
    )


@app.get("/api/game/stats", response_model=GameStatsResponse)
@limiter.limit("10/minute")
async def get_stats(
    request: Request,
    db: AsyncSession = Depends(get_db),
    user_info: dict = Depends(get_current_user)
):
    """Get user's game statistics"""

    user_id = user_info["user_id"]

    # Get completed games
    result = await db.execute(
        select(GameSession)
        .where(GameSession.user_id == user_id)
        .where(GameSession.status == GameStatus.COMPLETED)
    )
    completed_games = result.scalars().all()

    total_games = len(completed_games)
    total_wins = sum(1 for game in completed_games if game.win_amount > 0)
    total_losses = total_games - total_wins
    total_wagered = sum(game.bet_amount for game in completed_games)
    total_won = sum(game.win_amount for game in completed_games)
    biggest_win = max((game.win_amount for game in completed_games), default=0.0)
    average_coins = sum(game.coins_collected for game in completed_games) / total_games if total_games > 0 else 0

    return GameStatsResponse(
        total_games=total_games,
        total_wins=total_wins,
        total_losses=total_losses,
        total_wagered=total_wagered,
        total_won=total_won,
        biggest_win=biggest_win,
        average_coins=round(average_coins, 2)
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=HOST,
        port=PORT,
        reload=os.getenv("DEBUG", "false").lower() == "true"
    )
