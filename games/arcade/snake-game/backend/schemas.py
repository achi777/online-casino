from pydantic import BaseModel, Field, validator
from typing import Optional, Dict, Any
from datetime import datetime

class StartGameRequest(BaseModel):
    bet_amount: float = Field(..., gt=0, description="Bet amount must be positive")
    
    @validator('bet_amount')
    def validate_bet(cls, v):
        if v != 5.0:  # Fixed bet amount
            raise ValueError('Bet amount must be exactly 5.0')
        return v

class StartGameResponse(BaseModel):
    session_token: str
    status: str
    start_time: datetime
    message: str

class CoinCollectRequest(BaseModel):
    session_token: str
    coin_position: Dict[str, int] = Field(..., description="Coin position {x, y}")
    snake_head: Dict[str, int] = Field(..., description="Snake head position {x, y}")
    timestamp: int = Field(..., description="Client timestamp in milliseconds")
    
    @validator('coin_position', 'snake_head')
    def validate_position(cls, v):
        if 'x' not in v or 'y' not in v:
            raise ValueError('Position must have x and y coordinates')
        if not (0 <= v['x'] < 20) or not (0 <= v['y'] < 20):
            raise ValueError('Position must be within grid bounds (0-19)')
        return v

class CoinCollectResponse(BaseModel):
    coins_collected: int
    total_amount: float
    message: str

class CashOutRequest(BaseModel):
    session_token: str
    coins_collected: int = Field(..., ge=0)
    final_speed: float = Field(..., gt=0)
    game_duration: int = Field(..., gt=0, description="Game duration in milliseconds")
    
    @validator('coins_collected')
    def validate_coins(cls, v):
        if v > 1000:  # Maximum possible coins
            raise ValueError('Coins collected exceeds maximum')
        return v

class CashOutResponse(BaseModel):
    win_amount: float
    coins_collected: int
    status: str
    message: str

class GameOverRequest(BaseModel):
    session_token: str
    coins_collected: int = Field(..., ge=0)
    game_duration: int = Field(..., gt=0)
    collision_type: str = Field(..., description="wall or self")
    
    @validator('collision_type')
    def validate_collision(cls, v):
        if v not in ['wall', 'self']:
            raise ValueError('Collision type must be wall or self')
        return v

class GameOverResponse(BaseModel):
    coins_lost: int
    amount_lost: float
    status: str
    message: str

class GameStatsResponse(BaseModel):
    total_games: int
    total_wins: int
    total_losses: int
    total_wagered: float
    total_won: float
    biggest_win: float
    average_coins: float
