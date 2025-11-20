from sqlalchemy import Column, Integer, String, Float, DateTime, JSON, Enum, Boolean
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime
import enum

Base = declarative_base()

class GameStatus(enum.Enum):
    ACTIVE = "ACTIVE"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"

class EventType(enum.Enum):
    GAME_START = "GAME_START"
    COIN_COLLECTED = "COIN_COLLECTED"
    GAME_OVER = "GAME_OVER"
    CASH_OUT = "CASH_OUT"

class GameSession(Base):
    __tablename__ = "game_sessions"
    
    id = Column(Integer, primary_key=True, index=True)
    session_token = Column(String, unique=True, index=True, nullable=False)
    user_id = Column(Integer, nullable=False, index=True)
    user_token = Column(String, nullable=False)  # JWT from main backend
    
    bet_amount = Column(Float, nullable=False)
    win_amount = Column(Float, default=0.0)
    coins_collected = Column(Integer, default=0)
    
    status = Column(Enum(GameStatus), default=GameStatus.ACTIVE)
    
    start_time = Column(DateTime, default=datetime.utcnow, nullable=False)
    end_time = Column(DateTime, nullable=True)
    
    ip_address = Column(String, nullable=True)
    user_agent = Column(String, nullable=True)
    
    is_suspicious = Column(Boolean, default=False)
    fraud_reason = Column(String, nullable=True)
    
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class GameEvent(Base):
    __tablename__ = "game_events"
    
    id = Column(Integer, primary_key=True, index=True)
    session_token = Column(String, index=True, nullable=False)
    user_id = Column(Integer, nullable=False, index=True)
    
    event_type = Column(Enum(EventType), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False)
    
    data = Column(JSON, nullable=True)  # coin position, speed, etc
    
    created_at = Column(DateTime, default=datetime.utcnow)

class FraudLog(Base):
    __tablename__ = "fraud_logs"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False, index=True)
    session_token = Column(String, nullable=True)
    
    fraud_type = Column(String, nullable=False)  # IMPOSSIBLE_WIN, TOO_FAST, etc
    description = Column(String, nullable=False)
    severity = Column(String, nullable=False)  # LOW, MEDIUM, HIGH, CRITICAL
    
    ip_address = Column(String, nullable=True)
    user_agent = Column(String, nullable=True)
    
    data = Column(JSON, nullable=True)
    
    created_at = Column(DateTime, default=datetime.utcnow)
