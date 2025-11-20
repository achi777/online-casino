# Snake Game Backend

Production-ready Python backend for the Snake arcade game with comprehensive fraud detection and server-side validation.

## Features

- **Server-side validation**: All game logic validated on backend
- **Fraud detection**: Detects impossible wins, speed hacks, and suspicious patterns
- **Event tracking**: Every coin collection logged for audit trail
- **Rate limiting**: Prevents API abuse
- **JWT authentication**: Integrates with main backend authentication
- **Balance management**: Secure bet deduction and win payout
- **Game statistics**: Track wins, losses, and performance

## Architecture

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Frontend  │────────▶│ Snake Backend│────────▶│Main Backend  │
│  (HTML/JS)  │         │   (Python)   │         │    (Java)    │
└─────────────┘         └──────────────┘         └──────────────┘
                              │
                              ▼
                        ┌──────────────┐
                        │   SQLite DB  │
                        │ (Sessions,   │
                        │  Events,     │
                        │  Fraud Logs) │
                        └──────────────┘
```

## Installation

1. **Install Python 3.9+**

2. **Create virtual environment**:
```bash
python3 -m venv venv
source venv/bin/activate
```

3. **Install dependencies**:
```bash
pip install -r requirements.txt
```

4. **Configure environment**:
Copy `.env.example` to `.env` and update settings:
```bash
cp .env.example .env
```

Important: Make sure `JWT_SECRET_KEY` matches your main backend's JWT secret.

## Running

### Development Mode

```bash
# Using the startup script
chmod +x start.sh
./start.sh

# Or manually
source venv/bin/activate
python main.py
```

### Production Mode

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

## API Endpoints

### Start Game
```http
POST /api/game/start
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "bet_amount": 5.0
}
```

Response:
```json
{
  "session_token": "...",
  "status": "ACTIVE",
  "start_time": "2025-01-18T12:00:00",
  "message": "Game started successfully"
}
```

### Collect Coin
```http
POST /api/game/collect-coin
Content-Type: application/json

{
  "session_token": "...",
  "coin_position": {"x": 10, "y": 5},
  "snake_head": {"x": 10, "y": 5},
  "timestamp": 1705582800000
}
```

Response:
```json
{
  "coins_collected": 1,
  "total_amount": 0.1,
  "message": "Coin collected successfully"
}
```

### Cash Out
```http
POST /api/game/cashout
Content-Type: application/json

{
  "session_token": "...",
  "coins_collected": 50,
  "final_speed": 150,
  "game_duration": 120000
}
```

Response:
```json
{
  "win_amount": 5.0,
  "coins_collected": 50,
  "status": "COMPLETED",
  "message": "Successfully cashed out $5.00"
}
```

### Game Over
```http
POST /api/game/game-over
Content-Type: application/json

{
  "session_token": "...",
  "coins_collected": 25,
  "game_duration": 60000,
  "collision_type": "wall"
}
```

Response:
```json
{
  "coins_lost": 25,
  "amount_lost": 5.0,
  "status": "COMPLETED",
  "message": "Game over! You lost $5.00"
}
```

### Get Statistics
```http
GET /api/game/stats
Authorization: Bearer <JWT_TOKEN>
```

Response:
```json
{
  "total_games": 10,
  "total_wins": 6,
  "total_losses": 4,
  "total_wagered": 50.0,
  "total_won": 35.0,
  "biggest_win": 10.0,
  "average_coins": 45.5
}
```

## Security Features

### 1. Position Validation
- Snake head position must match coin position exactly
- Positions validated within grid bounds (0-19)

### 2. Coin Count Validation
- Maximum 1000 coins per game
- Server tracks coin count independently
- Client count must match server count

### 3. Time-based Validation
- Minimum game duration enforced
- Maximum coin collection rate (20/second)
- Timestamp validation against server time

### 4. Win Amount Calculation
- **Server-side only** - client cannot manipulate
- Maximum win amount enforced ($100)
- Formula: `coins × $0.10`

### 5. Fraud Detection
- Automatic flagging after 3+ fraud events
- Prevents suspicious users from playing
- Detailed fraud logs for investigation

### 6. Rate Limiting
- Start game: 10/minute
- Collect coin: 30/second
- Cash out: 10/minute
- Game over: 10/minute

## Database Schema

### game_sessions
- Tracks active and completed games
- Stores bet amount, win amount, coins collected
- Fraud detection flags

### game_events
- Logs every game action (start, coin collect, cash out, game over)
- Complete audit trail
- Used for fraud analysis

### fraud_logs
- Records all suspicious activities
- Severity levels: LOW, MEDIUM, HIGH, CRITICAL
- Used for user flagging

## Configuration

Key settings in `.env`:

- `BET_AMOUNT`: Fixed bet per game (default: 5.0)
- `COIN_VALUE`: Value per coin (default: 0.1)
- `MAX_WIN_AMOUNT`: Maximum payout (default: 100.0)
- `MAX_COINS_PER_GAME`: Maximum coins allowed (default: 1000)
- `MIN_GAME_DURATION_MS`: Minimum game time (default: 100ms)

## Integration with Main Backend

The backend communicates with the main Java backend for:

1. **JWT Verification**: Validates user tokens
2. **Balance Deduction**: Deducts bet amount at game start
3. **Balance Addition**: Adds winnings on cash out
4. **Game Recording**: Records session in main database

Required main backend endpoints:
- `POST /api/wallet/deduct`
- `POST /api/wallet/add`
- `GET /api/wallet/balance`
- `POST /api/games/record`

## Monitoring & Logs

Check for fraud logs:
```bash
# Query fraud logs
sqlite3 snake_game.db "SELECT * FROM fraud_logs ORDER BY created_at DESC LIMIT 10;"

# Check suspicious sessions
sqlite3 snake_game.db "SELECT * FROM game_sessions WHERE is_suspicious = 1;"
```

## Troubleshooting

### Database locked
If you get "database is locked" errors:
```bash
# Stop all instances
pkill -f "python main.py"

# Remove lock file
rm snake_game.db-journal
```

### JWT verification fails
Make sure JWT_SECRET_KEY in `.env` matches your main backend's secret.

### Balance deduction fails
Check that MAIN_BACKEND_URL points to your running Java backend.

## Production Deployment

1. **Use PostgreSQL** instead of SQLite:
```env
DATABASE_URL=postgresql+asyncpg://user:pass@localhost/snake_game
```

2. **Enable Redis** for rate limiting:
```env
REDIS_URL=redis://localhost:6379
```

3. **Run with Gunicorn** and multiple workers:
```bash
gunicorn main:app --worker-class uvicorn.workers.UvicornWorker --workers 4 --bind 0.0.0.0:8000
```

4. **Set DEBUG=false** in production

5. **Use HTTPS** with reverse proxy (nginx)

## License

Proprietary - Casino Platform
