from datetime import datetime, timedelta
from typing import List, Tuple, Optional
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from models import GameSession, GameEvent, FraudLog, GameStatus, EventType
import os
from dotenv import load_dotenv

load_dotenv()

# Configuration from .env
COIN_VALUE = float(os.getenv("COIN_VALUE", "0.1"))
MAX_WIN_AMOUNT = float(os.getenv("MAX_WIN_AMOUNT", "100.0"))
MAX_COINS_PER_GAME = int(os.getenv("MAX_COINS_PER_GAME", "1000"))
MIN_GAME_DURATION_MS = int(os.getenv("MIN_GAME_DURATION_MS", "100"))

class FraudDetectionService:

    @staticmethod
    async def log_fraud(
        db: AsyncSession,
        user_id: int,
        session_token: str,
        fraud_type: str,
        description: str,
        severity: str,
        ip_address: Optional[str] = None,
        user_agent: Optional[str] = None,
        data: Optional[dict] = None
    ):
        """Log a fraud event to database"""
        fraud_log = FraudLog(
            user_id=user_id,
            session_token=session_token,
            fraud_type=fraud_type,
            description=description,
            severity=severity,
            ip_address=ip_address,
            user_agent=user_agent,
            data=data
        )
        db.add(fraud_log)
        await db.commit()

    @staticmethod
    async def mark_session_suspicious(
        db: AsyncSession,
        session_token: str,
        reason: str
    ):
        """Mark a game session as suspicious"""
        result = await db.execute(
            select(GameSession).where(GameSession.session_token == session_token)
        )
        session = result.scalar_one_or_none()

        if session:
            session.is_suspicious = True
            session.fraud_reason = reason
            await db.commit()

    @staticmethod
    async def validate_coin_collection(
        db: AsyncSession,
        session_token: str,
        coin_position: dict,
        snake_head: dict,
        client_timestamp: int
    ) -> Tuple[bool, Optional[str]]:
        """
        Validate coin collection attempt
        Returns: (is_valid, error_message)
        """
        # Get game session
        result = await db.execute(
            select(GameSession).where(GameSession.session_token == session_token)
        )
        session = result.scalar_one_or_none()

        if not session:
            return False, "Invalid session"

        if session.status != GameStatus.ACTIVE:
            return False, "Game session is not active"

        # Check if positions match (snake head must be on coin)
        if coin_position['x'] != snake_head['x'] or coin_position['y'] != snake_head['y']:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="POSITION_MISMATCH",
                description="Snake head position doesn't match coin position",
                severity="HIGH",
                data={
                    "coin_position": coin_position,
                    "snake_head": snake_head
                }
            )
            return False, "Invalid position"

        # Check if too many coins collected
        if session.coins_collected >= MAX_COINS_PER_GAME:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="MAX_COINS_EXCEEDED",
                description=f"Exceeded maximum coins per game ({MAX_COINS_PER_GAME})",
                severity="CRITICAL"
            )
            await FraudDetectionService.mark_session_suspicious(
                db, session_token, "MAX_COINS_EXCEEDED"
            )
            return False, "Maximum coins exceeded"

        # Check event frequency (prevent spam)
        result = await db.execute(
            select(func.count(GameEvent.id))
            .where(GameEvent.session_token == session_token)
            .where(GameEvent.event_type == EventType.COIN_COLLECTED)
            .where(GameEvent.timestamp >= datetime.utcnow() - timedelta(seconds=1))
        )
        recent_events = result.scalar()

        if recent_events > 20:  # Max 20 coins per second
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="TOO_FAST_COLLECTION",
                description="Collecting coins too fast (>20/sec)",
                severity="HIGH"
            )
            await FraudDetectionService.mark_session_suspicious(
                db, session_token, "TOO_FAST_COLLECTION"
            )
            return False, "Collecting too fast"

        return True, None

    @staticmethod
    async def validate_cashout(
        db: AsyncSession,
        session_token: str,
        coins_collected: int,
        final_speed: float,
        game_duration: int
    ) -> Tuple[bool, Optional[str], Optional[float]]:
        """
        Validate cashout attempt
        Returns: (is_valid, error_message, calculated_win_amount)
        """
        # Get game session
        result = await db.execute(
            select(GameSession).where(GameSession.session_token == session_token)
        )
        session = result.scalar_one_or_none()

        if not session:
            return False, "Invalid session", None

        if session.status != GameStatus.ACTIVE:
            return False, "Game session is not active", None

        # Validate coins collected matches session
        if coins_collected != session.coins_collected:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="COIN_MISMATCH",
                description=f"Client reported {coins_collected} coins, server has {session.coins_collected}",
                severity="CRITICAL",
                data={
                    "client_coins": coins_collected,
                    "server_coins": session.coins_collected
                }
            )
            await FraudDetectionService.mark_session_suspicious(
                db, session_token, "COIN_MISMATCH"
            )
            return False, "Coin count mismatch", None

        # Calculate win amount server-side
        calculated_win = coins_collected * COIN_VALUE

        # Apply max win limit
        if calculated_win > MAX_WIN_AMOUNT:
            calculated_win = MAX_WIN_AMOUNT

        # Validate game duration
        actual_duration = (datetime.utcnow() - session.start_time).total_seconds() * 1000

        if game_duration < MIN_GAME_DURATION_MS:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="TOO_SHORT_GAME",
                description=f"Game duration too short: {game_duration}ms",
                severity="HIGH"
            )
            await FraudDetectionService.mark_session_suspicious(
                db, session_token, "TOO_SHORT_GAME"
            )
            return False, "Game duration too short", None

        # Check if duration is realistic (not more than 2x actual time)
        if game_duration > actual_duration * 2:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="INVALID_DURATION",
                description=f"Reported duration {game_duration}ms, actual {actual_duration}ms",
                severity="MEDIUM"
            )

        return True, None, calculated_win

    @staticmethod
    async def validate_game_over(
        db: AsyncSession,
        session_token: str,
        coins_collected: int,
        game_duration: int,
        collision_type: str
    ) -> Tuple[bool, Optional[str]]:
        """
        Validate game over attempt
        Returns: (is_valid, error_message)
        """
        # Get game session
        result = await db.execute(
            select(GameSession).where(GameSession.session_token == session_token)
        )
        session = result.scalar_one_or_none()

        if not session:
            return False, "Invalid session"

        if session.status != GameStatus.ACTIVE:
            return False, "Game session is not active"

        # Validate coins collected
        if coins_collected != session.coins_collected:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="COIN_MISMATCH",
                description=f"Client reported {coins_collected} coins, server has {session.coins_collected}",
                severity="HIGH",
                data={
                    "client_coins": coins_collected,
                    "server_coins": session.coins_collected
                }
            )

        # Validate game duration
        if game_duration < MIN_GAME_DURATION_MS:
            await FraudDetectionService.log_fraud(
                db=db,
                user_id=session.user_id,
                session_token=session_token,
                fraud_type="TOO_SHORT_GAME",
                description=f"Game duration too short: {game_duration}ms",
                severity="MEDIUM"
            )

        return True, None

    @staticmethod
    async def check_user_fraud_history(
        db: AsyncSession,
        user_id: int,
        hours: int = 24
    ) -> Tuple[int, List[str]]:
        """
        Check user's fraud history in last N hours
        Returns: (fraud_count, fraud_types)
        """
        since_time = datetime.utcnow() - timedelta(hours=hours)

        result = await db.execute(
            select(FraudLog)
            .where(FraudLog.user_id == user_id)
            .where(FraudLog.created_at >= since_time)
        )
        fraud_logs = result.scalars().all()

        fraud_types = [log.fraud_type for log in fraud_logs]

        return len(fraud_logs), fraud_types

    @staticmethod
    async def is_user_suspicious(
        db: AsyncSession,
        user_id: int
    ) -> bool:
        """
        Check if user has suspicious activity
        Returns: True if user has 3+ fraud events in last 24 hours
        """
        fraud_count, _ = await FraudDetectionService.check_user_fraud_history(
            db, user_id, hours=24
        )

        return fraud_count >= 3
