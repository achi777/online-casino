# კაზინო თამაშების ინტეგრაციის სრული ინსტრუქცია

## 📋 შინაარსი
1. [არქიტექტურა](#არქიტექტურა)
2. [ახალი თამაშის დამატება](#ახალი-თამაშის-დამატება)
3. [Authentication & Security](#authentication--security)
4. [Demo vs Real Money Mode](#demo-vs-real-money-mode)
5. [Backend API Integration](#backend-api-integration)
6. [მაგალითები](#მაგალითები)
7. [Testing & Troubleshooting](#testing--troubleshooting)

---

## 🏗️ არქიტექტურა

### სისტემის კომპონენტები

```
┌──────────────────────┐      ┌──────────────────────┐      ┌──────────────────────┐
│   Frontend User      │◄────►│    Backend API       │◄────►│     PostgreSQL       │
│   (React/Vite)       │ JWT  │   (Spring Boot)      │      │      Database        │
│   Port: 3000         │      │   Port: 8080         │      │                      │
└──────────────────────┘      └──────────────────────┘      └──────────────────────┘
         │                              │
         │                              │
         ▼                              ▼
┌──────────────────────┐      ┌──────────────────────┐
│  Frontend Admin      │      │    Game Server       │
│   (React/Vite)       │      │   (Python HTTP)      │
│   Port: 3001         │      │   Port: 8888         │
└──────────────────────┘      └──────────────────────┘
                                       │
                                       ▼
                              ┌──────────────────────┐
                              │   Game Files         │
                              │   (HTML/JS/CSS)      │
                              │   games/             │
                              └──────────────────────┘
```

### თამაშის გაშვების დეტალური ფლოუ

#### 1️⃣ **DEMO Mode** (უფასო თამაში)
```
User (Unauthorized) → Home Page (http://localhost:3000)
    ↓ Click "DEMO"
Frontend → POST /api/user/games/launch {gameId, demoMode: true}
    ↓ (No JWT Required)
Backend → Creates demo session (demo-UUID)
    ↓ Returns launchUrl
Frontend → Opens game in Modal/iframe
    ↓
Game → Reads ?session=demo-xxx&demo=true from URL
    ↓
Game → Local simulation (no backend calls for bets)
```

#### 2️⃣ **REAL MONEY Mode** (ავტორიზებული)
```
User (Authorized) → Login → Dashboard/Games
    ↓ Click "PLAY"
Frontend → POST /api/user/games/launch {gameId, demoMode: false}
    ↓ (JWT Required in Headers)
Backend → Validates JWT → Creates GameSession
    ↓ Saves session to DB (userId, gameId, sessionToken)
    ↓ Returns launchUrl with sessionToken
Frontend → Opens game in Modal/iframe
    ↓ Appends JWT token to URL: ?session=xxx&token=JWT
Game → Reads sessionToken & JWT from URL
    ↓
Game → GET /api/user/balance?sessionToken=xxx (with JWT)
    ↓ Backend validates session & JWT
Backend → Returns current balance
    ↓
Game → POST /api/user/games/bet {sessionToken, betAmount, roundId}
    ↓ Backend validates, creates GameRound, updates balance
Backend → Returns {newBalance, roundId}
    ↓
Game → POST /api/user/games/win {sessionToken, roundId, winAmount}
    ↓ Backend validates, updates balance
Backend → Returns {newBalance}
```

---

## 🎮 ახალი თამაშის დამატება

### ნაბიჯი 1: თამაშის ფაილების სტრუქტურა

შექმენით დირექტორია შესაბამის კატეგორიაში:

```bash
games/
├── slots/
│   └── your-game-name/
│       ├── index.html
│       └── photo.jpeg (300x200px thumbnail)
├── table-games/
│   └── your-game-name/
│       ├── index.html
│       └── photo.jpeg
└── poker/
    └── your-game-name/
        ├── index.html
        └── photo.jpeg
```

### ნაბიჯი 2: HTML Template

**თამაშის `index.html` ძირითადი სტრუქტურა:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Game Name 🎰</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Roboto', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            overflow: hidden;
        }
        .game-container {
            width: 100%;
            max-width: 1200px;
            padding: 20px;
        }
        .game-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .balance-display {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            padding: 15px 30px;
            border-radius: 15px;
            font-size: 24px;
            font-weight: bold;
            color: #FFD700;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        }
        /* თქვენი სტილები */
    </style>
</head>
<body>
    <div class="game-container">
        <div class="game-header">
            <h1>Your Game Name</h1>
            <div class="balance-display">
                💰 Balance: <span id="balance">0.00</span> ₾
            </div>
        </div>

        <!-- თქვენი თამაშის UI -->
        <div class="game-board">
            <!-- თამაშის ელემენტები -->
        </div>

        <div class="game-controls">
            <!-- კონტროლის ღილაკები -->
        </div>
    </div>

    <script>
        // თამაშის ლოგიკა (იხილეთ ქვემოთ)
    </script>
</body>
</html>
```

### ნაბიჯი 3: JavaScript Integration (სრული კოდი)

**Core თამაშის კოდი ყველა თამაშისთვის:**

```javascript
// ===== CONFIGURATION =====
const BACKEND_URL = 'http://localhost:8080';

// ===== STATE =====
let balance = 0;
let sessionToken = '';
let jwtToken = '';
let isDemo = false;
let roundIdCounter = 1;
let isPlaying = false;

// ===== INITIALIZATION =====
async function initGame() {
    // Get parameters from URL
    const urlParams = new URLSearchParams(window.location.search);
    sessionToken = urlParams.get('session') || '';
    jwtToken = urlParams.get('token') || '';
    isDemo = urlParams.get('demo') === 'true';

    console.log('Initializing game...', {
        sessionToken,
        isDemo,
        hasJWT: !!jwtToken
    });

    // Validate session
    if (!sessionToken) {
        alert('❌ Invalid session. Please launch game from casino lobby.');
        return;
    }

    try {
        if (isDemo) {
            // Demo Mode - Local simulation
            console.log('🎮 Demo Mode activated');
            balance = 1000.00;
            updateBalanceDisplay();
            enableGameControls();
            return;
        }

        // Real Money Mode - Fetch balance from backend
        console.log('💰 Real Money Mode - Fetching balance...');

        const response = await fetch(
            `${BACKEND_URL}/api/user/balance?sessionToken=${sessionToken}`,
            {
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        if (!response.ok) {
            throw new Error(`Balance fetch failed: ${response.status}`);
        }

        const data = await response.json();
        balance = parseFloat(data.balance || 0);

        console.log('✅ Balance fetched:', balance);
        updateBalanceDisplay();
        enableGameControls();

    } catch (error) {
        console.error('❌ Init error:', error);
        alert('Failed to initialize game. Please try again.');
    }
}

// ===== BET PLACEMENT (Real Money) =====
async function placeBet(betAmount) {
    if (betAmount > balance) {
        alert('❌ არასაკმარისი ბალანსი!');
        return null;
    }

    if (isDemo) {
        // Demo mode - local simulation
        balance -= betAmount;
        updateBalanceDisplay();
        return {
            success: true,
            roundId: `demo-round-${Date.now()}`,
            newBalance: balance
        };
    }

    // Real Money Mode
    const roundId = `round-${Date.now()}-${roundIdCounter++}`;

    try {
        const response = await fetch(`${BACKEND_URL}/api/user/games/bet`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sessionToken: sessionToken,
                betAmount: betAmount,
                roundId: roundId
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Bet placement failed');
        }

        const newBalance = await response.json();
        balance = parseFloat(newBalance);
        updateBalanceDisplay();

        return {
            success: true,
            roundId: roundId,
            newBalance: balance
        };

    } catch (error) {
        console.error('❌ Bet error:', error);
        alert('Failed to place bet: ' + error.message);
        return null;
    }
}

// ===== WIN PROCESSING (Real Money) =====
async function processWin(roundId, winAmount) {
    if (winAmount <= 0) {
        return { success: true, newBalance: balance };
    }

    if (isDemo) {
        // Demo mode - local simulation
        balance += winAmount;
        updateBalanceDisplay();
        return {
            success: true,
            newBalance: balance
        };
    }

    // Real Money Mode
    try {
        const response = await fetch(`${BACKEND_URL}/api/user/games/win`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sessionToken: sessionToken,
                roundId: roundId,
                winAmount: winAmount
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Win processing failed');
        }

        const newBalance = await response.json();
        balance = parseFloat(newBalance);
        updateBalanceDisplay();

        return {
            success: true,
            newBalance: balance
        };

    } catch (error) {
        console.error('❌ Win processing error:', error);
        alert('Failed to process win: ' + error.message);
        return null;
    }
}

// ===== DEMO MODE SIMULATION =====
function simulateGameOutcome(betAmount) {
    // თქვენი თამაშის შედეგის გენერაცია
    // მაგალითი: Slot თამაშისთვის
    const random = Math.random();

    if (random < 0.05) {
        // 5% - Jackpot (20x)
        return betAmount * 20;
    } else if (random < 0.15) {
        // 10% - Big win (10x)
        return betAmount * 10;
    } else if (random < 0.30) {
        // 15% - Medium win (5x)
        return betAmount * 5;
    } else if (random < 0.45) {
        // 15% - Small win (2x)
        return betAmount * 2;
    } else if (random < 0.55) {
        // 10% - Return bet (1x)
        return betAmount;
    } else {
        // 45% - Loss
        return 0;
    }
}

// ===== UI UPDATES =====
function updateBalanceDisplay() {
    const balanceElement = document.getElementById('balance');
    if (balanceElement) {
        balanceElement.textContent = balance.toFixed(2);
    }
}

function enableGameControls() {
    // Enable game buttons/controls
    document.querySelectorAll('.game-btn').forEach(btn => {
        btn.disabled = false;
    });
}

function disableGameControls() {
    // Disable game buttons/controls during play
    document.querySelectorAll('.game-btn').forEach(btn => {
        btn.disabled = true;
    });
}

// ===== EXAMPLE: PLAY ROUND =====
async function playRound(betAmount) {
    if (isPlaying) return;

    isPlaying = true;
    disableGameControls();

    // 1. Place bet
    const betResult = await placeBet(betAmount);

    if (!betResult || !betResult.success) {
        isPlaying = false;
        enableGameControls();
        return;
    }

    // 2. Simulate/calculate game outcome
    let winAmount = 0;

    if (isDemo) {
        // Demo: local simulation
        winAmount = simulateGameOutcome(betAmount);
    } else {
        // Real Money: თქვენი თამაშის ლოგიკა
        // მაგალითად Slot-ისთვის: შედეგი frontend-ში გენერირდება
        // ან Backend-იდან მოდის თუ Backend აკონტროლებს შედეგს
        winAmount = simulateGameOutcome(betAmount);
    }

    // 3. Show game animation/result
    await showGameAnimation(winAmount > 0);

    // 4. Process win
    if (winAmount > 0) {
        const winResult = await processWin(betResult.roundId, winAmount);
        if (winResult && winResult.success) {
            showWinNotification(winAmount);
        }
    }

    isPlaying = false;
    enableGameControls();
}

// ===== HELPER FUNCTIONS =====
function showGameAnimation(isWin) {
    return new Promise(resolve => {
        // თქვენი ანიმაცია
        setTimeout(resolve, 2000);
    });
}

function showWinNotification(amount) {
    // Win notification/animation
    console.log(`🎉 You won ${amount.toFixed(2)} ₾!`);
    // თქვენი win ანიმაცია
}

// ===== INITIALIZATION ON LOAD =====
window.addEventListener('load', initGame);
```

### ნაბიჯი 4: Backend-ში დარეგისტრირება

**ფაილი:** `backend/src/main/java/com/casino/config/DataLoader.java`

```java
// Example: Adding American Roulette
if (!gameRepository.findByGameCode("AMERICAN_ROULETTE").isPresent()) {
    Game americanRoulette = new Game();
    americanRoulette.setGameCode("AMERICAN_ROULETTE");
    americanRoulette.setName("American Roulette");
    americanRoulette.setDescription("Classic American Roulette with 38 pockets (0, 00, 1-36). Bet on numbers, colors, or sections and watch the wheel spin!");
    americanRoulette.setCategory(Game.GameCategory.TABLE_GAMES);
    americanRoulette.setProvider(provider);
    americanRoulette.setIframeUrl("http://localhost:8888/table-games/american-roulette/index.html");
    americanRoulette.setThumbnailUrl("http://localhost:8888/table-games/american-roulette/photo.jpeg");
    americanRoulette.setRtp(new BigDecimal("94.74")); // House edge: 5.26%
    americanRoulette.setFeatured(true);
    americanRoulette.setStatus(Game.GameStatus.ACTIVE);
    americanRoulette.setSortOrder(18);
    gameRepository.save(americanRoulette);
    log.info("Game data loaded: {}", americanRoulette.getName());
}
```

**სრული კატეგორიების სია:**
- `Game.GameCategory.SLOTS` - სლოტები
- `Game.GameCategory.TABLE_GAMES` - მაგიდის თამაშები
- `Game.GameCategory.VIDEO_POKER` - ვიდეო პოკერი
- `Game.GameCategory.LIVE_CASINO` - ლაივ კაზინო
- `Game.GameCategory.JACKPOT` - ჯეკპოტ თამაშები

### ნაბიჯი 5: Rebuild & Restart

```bash
# სრული გადატვირთვა
cd /Users/archilodishelidze/Desktop/dev/gambling
./stop.sh
cd backend && mvn clean install -DskipTests
cd ..
./start.sh
```

---

## 🔐 Authentication & Security

### JWT Token Flow

```
1. User Login → Backend generates JWT (valid 24h)
2. Frontend stores JWT in localStorage
3. Game Launch → Frontend appends JWT to game URL
4. Game → Includes JWT in all API calls
5. Backend → Validates JWT on every request
```

### Session Token

- **Format**: UUID (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- **Demo Format**: `demo-{UUID}` (e.g., `demo-550e8400-...`)
- **Expiration**: 2 hours (configurable in GameService.java:78)
- **Storage**: PostgreSQL `game_sessions` table

### Security Best Practices

✅ **DO:**
- Always validate sessionToken + JWT together
- Use HTTPS in production
- Implement rate limiting
- Log all financial transactions
- Validate bet amounts (min/max limits)
- Verify win amounts don't exceed max multiplier

❌ **DON'T:**
- Store JWT in cookies without httpOnly flag
- Trust client-side calculations for money
- Skip session validation
- Allow negative bet amounts
- Process wins without corresponding bets

---

## 🎯 Demo vs Real Money Mode

### Demo Mode
- **Access**: Public (no login required)
- **Session**: `demo-{UUID}` (not saved to DB)
- **Balance**: 1000.00₾ (initial, simulated)
- **API Calls**: None (local simulation only)
- **Purpose**: Marketing, testing, user experience

### Real Money Mode
- **Access**: Requires JWT authentication
- **Session**: Saved to `game_sessions` table
- **Balance**: User's actual balance from database
- **API Calls**: All bets/wins recorded
- **Purpose**: Real gambling with real money

### Comparison Table

| Feature | Demo Mode | Real Money Mode |
|---------|-----------|-----------------|
| Authentication | ❌ Not required | ✅ JWT Required |
| Session Storage | ❌ Not saved | ✅ Saved to DB |
| API Calls | ❌ None | ✅ /bet, /win endpoints |
| Balance Updates | 🔹 Local only | 🔹 Database updates |
| Transaction Logs | ❌ No | ✅ Full audit trail |
| Self-Exclusion Check | ❌ No | ✅ Yes |

---

## 🔌 Backend API Integration

### 1️⃣ Game Launch

**Endpoint:** `POST /api/user/games/launch`

**Request:**
```json
{
  "gameId": 18,
  "demoMode": false
}
```

**Response:**
```json
{
  "sessionToken": "550e8400-e29b-41d4-a716-446655440000",
  "launchUrl": "http://localhost:8888/table-games/american-roulette/index.html?session=550e8400-e29b-41d4-a716-446655440000&demo=false",
  "integrationType": "IFRAME"
}
```

### 2️⃣ Get Balance

**Endpoint:** `GET /api/user/balance?sessionToken={token}`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "balance": 1250.50
}
```

### 3️⃣ Place Bet

**Endpoint:** `POST /api/user/games/bet`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request:**
```json
{
  "sessionToken": "550e8400-e29b-41d4-a716-446655440000",
  "betAmount": 10.00,
  "roundId": "round-1730984567-1"
}
```

**Response:**
```json
1240.50
```
*(New balance after bet deduction)*

### 4️⃣ Process Win

**Endpoint:** `POST /api/user/games/win`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request:**
```json
{
  "sessionToken": "550e8400-e29b-41d4-a716-446655440000",
  "roundId": "round-1730984567-1",
  "winAmount": 360.00
}
```

**Response:**
```json
1600.50
```
*(New balance after win addition)*

### Error Responses

**401 Unauthorized:**
```json
{
  "error": "Authentication required",
  "message": "Please login to play"
}
```

**400 Bad Request:**
```json
{
  "error": "Insufficient balance",
  "message": "Your balance is too low"
}
```

**Session Expired:**
```json
{
  "error": "Session expired",
  "message": "Please relaunch the game"
}
```

---

## 💡 მაგალითები

### 1. Slot თამაში (5-Reel Video Slot)

**File:** `games/slots/simple5-reel-video-slots/index.html`

**Key Features:**
- 5 reels, 3 rows, 20 paylines
- Symbols: 🍒 🍋 🍊 🍇 💎 7️⃣ ⭐
- Bet levels: 1₾, 5₾, 10₾, 25₾, 100₾
- Auto-spin feature

**Play Function:**
```javascript
async function spin() {
    if (isSpinning) return;

    const betAmount = getCurrentBetAmount();

    if (betAmount > balance) {
        alert('არასაკმარისი ბალანსი!');
        return;
    }

    isSpinning = true;

    // Place bet
    const betResult = await placeBet(betAmount);
    if (!betResult) {
        isSpinning = false;
        return;
    }

    // Spin animation
    await animateReels();

    // Generate result
    const result = generateSlotResult();
    const winAmount = calculateWin(result, betAmount);

    // Display result
    displayReels(result);

    // Process win
    if (winAmount > 0) {
        await processWin(betResult.roundId, winAmount);
        showWinAnimation(winAmount);
    }

    isSpinning = false;
}
```

### 2. American Roulette

**File:** `games/table-games/american-roulette/index.html`

**Key Features:**
- 38 numbers (0, 00, 1-36)
- Bet types: Straight, Split, Street, Corner, Line, Dozen, Column, Red/Black, Even/Odd
- Chip selector: 1₾, 5₾, 10₾, 25₾, 100₾
- Animated wheel and ball

**Place Bet Function:**
```javascript
function placeBetOnTable(betType, numbers) {
    if (currentChip > balance) {
        alert('არასაკმარისი ბალანსი!');
        return;
    }

    // Add to bets object
    const betKey = `${betType}-${numbers.join('-')}`;
    if (!bets[betKey]) {
        bets[betKey] = {
            amount: 0,
            type: betType,
            numbers: numbers
        };
    }

    bets[betKey].amount += currentChip;
    totalBet += currentChip;

    updateBetDisplay();
    updateTotalBet();
}

async function spinWheel() {
    if (totalBet === 0) {
        alert('გთხოვთ გააკეთოთ ფსონი!');
        return;
    }

    // Place total bet
    const betResult = await placeBet(totalBet);
    if (!betResult) return;

    // Spin animation
    const winningNumber = await animateWheelSpin();

    // Calculate total winnings
    let totalWinnings = 0;
    for (const [key, bet] of Object.entries(bets)) {
        if (bet.numbers.includes(winningNumber)) {
            const payout = calculatePayout(bet.type, bet.amount);
            totalWinnings += payout;
        }
    }

    // Process win
    if (totalWinnings > 0) {
        await processWin(betResult.roundId, totalWinnings);
        showWinAnimation(winningNumber, totalWinnings);
    }

    // Clear bets
    bets = {};
    totalBet = 0;
    updateBetDisplay();
}
```

### 3. Video Poker (Jacks or Better)

**File:** `games/poker/jack-or-better/index.html`

**Key Features:**
- 52-card deck
- Hold/Draw mechanic
- Paytable: Royal Flush 250x, Straight Flush 50x, Four of a Kind 25x...
- Multi-hand option

**Two-Phase Play:**
```javascript
// Phase 1: Deal
async function deal() {
    const betAmount = getCurrentBetAmount();

    // Place bet
    const betResult = await placeBet(betAmount);
    if (!betResult) return;

    currentRoundId = betResult.roundId;

    // Deal 5 cards
    currentHand = shuffleAndDeal();
    displayCards(currentHand);

    gamePhase = 'draw';
    enableHoldButtons();
}

// Phase 2: Draw
async function draw() {
    disableHoldButtons();

    // Replace non-held cards
    const heldIndices = getHeldCardIndices();
    for (let i = 0; i < 5; i++) {
        if (!heldIndices.includes(i)) {
            currentHand[i] = drawCard();
        }
    }

    displayCards(currentHand);

    // Evaluate hand
    const handRank = evaluatePokerHand(currentHand);
    const winAmount = calculatePokerPayout(handRank, getCurrentBetAmount());

    // Process win
    if (winAmount > 0) {
        await processWin(currentRoundId, winAmount);
        showHandResult(handRank, winAmount);
    } else {
        showHandResult(handRank, 0);
    }

    gamePhase = 'deal';
}
```

---

## 🧪 Testing & Troubleshooting

### Testing Checklist

- [ ] **Demo Mode Works**
  - Launch game without login
  - Initial balance shows 1000.00₾
  - All game features functional
  - No API calls made

- [ ] **Real Money Mode Works**
  - Login required
  - Balance fetched correctly
  - Bet placement updates balance
  - Win processing updates balance

- [ ] **Session Management**
  - Session token passed correctly
  - JWT token validated
  - Session expiration handled

- [ ] **Error Handling**
  - Insufficient balance
  - Invalid session
  - Network errors
  - Session expired

- [ ] **UI/UX**
  - Mobile responsive
  - Balance updates in real-time
  - Smooth animations
  - Clear error messages

### Common Issues

#### 1. **CORS Error**

**Error:**
```
Access to fetch at 'http://localhost:8080/api/...' from origin
'http://localhost:8888' has been blocked by CORS policy
```

**Solution:**
Backend SecurityConfig already configured (line 100-107):
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    // "http://localhost:3000", "http://localhost:8888", "http://localhost:3001"
    ...
}
```

#### 2. **Session Token Missing**

**Error:**
```javascript
console.error('Invalid session');
```

**Solution:**
Check URL parameters:
```javascript
const urlParams = new URLSearchParams(window.location.search);
console.log('Session:', urlParams.get('session'));
console.log('Demo:', urlParams.get('demo'));
console.log('Token:', urlParams.get('token'));
```

#### 3. **Balance Not Updating**

**Problem:** Game shows old balance after bet/win

**Solution:**
Always use backend response:
```javascript
// ❌ Wrong
balance += winAmount;

// ✅ Correct
const newBalance = await response.json();
balance = parseFloat(newBalance);
```

#### 4. **JWT Validation Failed**

**Error:**
```
401 Unauthorized
```

**Solution:**
Include JWT in headers:
```javascript
headers: {
    'Authorization': `Bearer ${jwtToken}`,
    'Content-Type': 'application/json'
}
```

#### 5. **Game Server Not Running**

**Error:**
```
localhost sent an invalid response
ERR_INVALID_HTTP_RESPONSE
```

**Solution:**
```bash
# Check status
./status.sh

# Restart game server
./restart.sh

# Or manually
python3 -m http.server 8888 --directory games &
```

### Debug Mode

Add to your game JavaScript:
```javascript
const DEBUG = true;

function log(...args) {
    if (DEBUG) {
        console.log('[GAME]', ...args);
    }
}

// Usage
log('Balance:', balance);
log('Bet placed:', betAmount);
log('Win amount:', winAmount);
```

---

## 📦 Complete Integration Checklist

### Pre-Development
- [ ] Read this guide completely
- [ ] Understand Demo vs Real Money modes
- [ ] Review example games in `/games` directory

### Development
- [ ] Create game directory in correct category
- [ ] Create `index.html` with full HTML structure
- [ ] Add game styles (CSS)
- [ ] Implement `initGame()` function
- [ ] Implement `placeBet()` function
- [ ] Implement `processWin()` function
- [ ] Add game-specific logic
- [ ] Create thumbnail (300x200px JPEG)

### Backend Integration
- [ ] Add game to `DataLoader.java`
- [ ] Set correct RTP percentage
- [ ] Set category, provider, iframe URL
- [ ] Rebuild backend: `mvn clean install -DskipTests`
- [ ] Restart backend: `./restart.sh`

### Testing
- [ ] Test Demo mode (no login)
- [ ] Test Real Money mode (with login)
- [ ] Test bet placement
- [ ] Test win processing
- [ ] Test error handling
- [ ] Test mobile responsive design
- [ ] Test all game features

### Deployment
- [ ] Verify game appears in lobby
- [ ] Test with real users (staging)
- [ ] Monitor for errors in logs
- [ ] Verify RTP calculations

---

## 🎓 Best Practices

### 1. **RTP Configuration**
- **Slots**: 95% - 97%
- **Table Games**: 94% - 99%
- **Video Poker**: 96% - 99.5%

### 2. **Bet Limits**
```java
// In DataLoader.java
game.setMinBet(new BigDecimal("1.00"));    // Min 1₾
game.setMaxBet(new BigDecimal("1000.00")); // Max 1000₾
```

### 3. **Performance**
- Optimize images (use WebP when possible)
- Minimize JavaScript bundle size
- Use CSS animations over JavaScript
- Lazy load assets

### 4. **Mobile Optimization**
```css
@media (max-width: 768px) {
    .game-container {
        max-width: 100%;
        padding: 10px;
    }

    .balance-display {
        font-size: 18px;
        padding: 10px 20px;
    }
}
```

### 5. **Error Logging**
```javascript
try {
    const result = await placeBet(betAmount);
} catch (error) {
    console.error('Bet error:', {
        error: error.message,
        sessionToken: sessionToken,
        betAmount: betAmount,
        balance: balance,
        timestamp: new Date().toISOString()
    });
}
```

---

## 🚀 Quick Start Commands

```bash
# Check platform status
./status.sh

# Start all services
./start.sh

# Stop all services
./stop.sh

# Restart everything
./restart.sh

# Rebuild after changes
./install.sh && ./restart.sh

# View logs
tail -f logs/backend.log
tail -f logs/game-server.log
```

---

## 📚 Resources

### Documentation
- Backend API: http://localhost:8080/swagger-ui.html
- Game Server: http://localhost:8888/
- User Portal: http://localhost:3000/
- Admin Portal: http://localhost:3001/

### Example Games
1. **Simple 5-Reel Video Slots** - `/games/slots/simple5-reel-video-slots/`
2. **American Roulette** - `/games/table-games/american-roulette/`
3. **European Roulette** - `/games/table-games/european-roulette/`
4. **Blackjack** - `/games/table-games/blackjack/`
5. **Baccarat** - `/games/table-games/baccarat/`
6. **Three Card Poker** - `/games/poker/three-card-poker/`
7. **90-Ball Bingo** - `/games/fun/90-ball-bingo/`

### Support
- Check `SCRIPTS_README.md` for management scripts
- Check logs in `/logs` directory
- Verify database with PostgreSQL client
- Test API endpoints with Swagger UI

---

## ✅ Final Notes

1. **Always test in Demo mode first** before real money testing
2. **Balance must always match database** - never trust frontend calculations
3. **Log all financial transactions** for audit trail
4. **Validate all inputs** on backend (bet amounts, round IDs, session tokens)
5. **Handle session expiration gracefully** with user-friendly messages
6. **Mobile-first design** - most users play on phones
7. **Performance matters** - optimize images and animations
8. **Security is critical** - never skip JWT validation

---

**წარმატებები თამაშების შექმნაში! 🎰🎲🃏**

დამატებითი დახმარების შემთხვევაში:
- Backend logs: `tail -f logs/backend.log`
- Game server logs: `tail -f logs/game-server.log`
- Database: `psql -U postgres -d casino_db`
