# კაზინო თამაშების ინტეგრაციის სრული ინსტრუქცია

## შინაარსი
1. [არქიტექტურა](#არქიტექტურა)
2. [ახალი თამაშის დამატება](#ახალი-თამაშის-დამატება)
3. [RTP ინტეგრაციის პრინციპი](#rtp-ინტეგრაციის-პრინციპი)
4. [მაგალითები](#მაგალითები)

---

## არქიტექტურა

### სისტემის კომპონენტები

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  Frontend User  │◄────►│  Backend API    │◄────►│   PostgreSQL    │
│  (React/MUI)    │      │  (Spring Boot)  │      │   Database      │
└─────────────────┘      └─────────────────┘      └─────────────────┘
         │
         ▼
┌─────────────────┐
│   Game Files    │
│  (HTML/JS/CSS)  │
│  localhost:8888 │
└─────────────────┘
```

### თამაშის გაშვების ფლოუ

1. **მომხმარებელი** - აჭერს "Play" ღილაკს თამაშზე
2. **Frontend** - აგზავნის `POST /api/user/games/launch` რექვესტს
3. **Backend** - ქმნის game session-ს და აბრუნებს URL-ს session token-ით
4. **Frontend** - იხსნება Modal iframe-ში თამაში
5. **Game** - იღებს session token-ს URL-დან და აკავშირდება backend-თან
6. **Backend** - აკონტროლებს ყველა თამაშის შედეგს RTP-ის მიხედვით

---

## ახალი თამაშის დამატება

### ნაბიჯი 1: თამაშის ფაილების შექმნა

შექმენით თამაშის დირექტორია შესაბამის კატეგორიაში:

```bash
# Slot თამაშებისთვის
games/slots/your-game-name/index.html

# Table თამაშებისთვის
games/table-games/your-game-name/index.html

# Poker თამაშებისთვის
games/poker/your-game-name/index.html
```

### ნაბიჯი 2: HTML სტრუქტურა

თამაშის `index.html` ფაილი უნდა შეიცავდეს:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Game Name</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            overflow: hidden;
        }
        /* თქვენი სტილები */
    </style>
</head>
<body>
    <div class="game-container">
        <div class="game-header">
            <div class="balance-display">
                Balance: <span id="balance">0.00</span> ₾
            </div>
        </div>
        <!-- თქვენი თამაშის UI -->
    </div>

    <script>
        // თამაშის ლოგიკა
    </script>
</body>
</html>
```

### ნაბიჯი 3: RTP ინტეგრაციის კოდი

თამაშის JavaScript-ში დაამატეთ შემდეგი კოდი:

```javascript
const BACKEND_URL = 'http://localhost:8080';
let balance = 0;
let sessionToken = '';
let gameId = null;
let isDemo = false;
let roundId = 1;

// 1. ინიციალიზაცია
async function initGame() {
    // მიიღეთ session token URL-დან
    const urlParams = new URLSearchParams(window.location.search);
    sessionToken = urlParams.get('session') || '';
    isDemo = urlParams.get('demo') === 'true';

    if (!sessionToken && !isDemo) {
        alert('Invalid session. Please launch game from casino lobby.');
        return;
    }

    try {
        if (isDemo) {
            // Demo Mode
            balance = 1000.00;
            updateBalanceDisplay();
            return;
        }

        // Real Money Mode - მოითხოვეთ ბალანსი
        const response = await fetch(
            `${BACKEND_URL}/api/user/balance?sessionToken=${sessionToken}`
        );

        if (!response.ok) {
            throw new Error('Failed to fetch balance');
        }

        const data = await response.json();
        balance = data.balance;

        // მოითხოვეთ game info
        const gameInfoResponse = await fetch(
            `${BACKEND_URL}/api/user/game-info?sessionToken=${sessionToken}`
        );
        const gameData = await gameInfoResponse.json();
        gameId = gameData.gameId;

        updateBalanceDisplay();
    } catch (error) {
        console.error('Init error:', error);
        alert('Failed to initialize game');
    }
}

// 2. თამაშის რაუნდის გაშვება (Backend-controlled)
async function playRound(betAmount) {
    if (betAmount > balance) {
        alert('Insufficient balance!');
        return null;
    }

    try {
        if (isDemo) {
            // Demo Mode - local simulation
            const win = simulateLocalRound(betAmount);
            balance += win - betAmount;
            updateBalanceDisplay();
            return { win, balance };
        }

        // Real Money Mode - Backend RTP Control
        const response = await fetch(`${BACKEND_URL}/api/user/games/spin`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sessionToken: sessionToken,
                gameId: gameId,
                betAmount: betAmount,
                roundId: `round-${Date.now()}-${roundId++}`
            })
        });

        if (!response.ok) {
            throw new Error('Spin request failed');
        }

        const spinData = await response.json();

        // გამოიყენეთ Backend-ის შედეგი
        balance = spinData.newBalance;
        updateBalanceDisplay();

        return {
            win: spinData.winAmount,
            balance: spinData.newBalance,
            result: spinData.result // თამაშის სპეციფიკური შედეგი
        };

    } catch (error) {
        console.error('Play round error:', error);
        alert('Failed to play round');
        return null;
    }
}

// 3. ბალანსის განახლება
function updateBalanceDisplay() {
    document.getElementById('balance').textContent = balance.toFixed(2);
}

// 4. Demo Mode სიმულაცია (მხოლოდ demo-სთვის)
function simulateLocalRound(betAmount) {
    // მარტივი სიმულაცია demo mode-სთვის
    const random = Math.random();
    if (random < 0.40) {
        return betAmount * 2; // 40% chance to double
    } else if (random < 0.60) {
        return betAmount * 1.5; // 20% chance to 1.5x
    } else if (random < 0.75) {
        return betAmount; // 15% chance to return bet
    } else {
        return 0; // 25% chance to lose
    }
}

// ინიციალიზაცია გვერდის ჩატვირთვისას
window.addEventListener('load', initGame);
```

### ნაბიჯი 4: Backend-ში დარეგისტრირება

გახსენით `backend/src/main/java/com/casino/config/DataLoader.java` და დაამატეთ თამაში:

```java
// Create game - Your New Game
if (!gameRepository.findByGameCode("YOUR_GAME_CODE").isPresent()) {
    Game newGame = new Game();
    newGame.setGameCode("YOUR_GAME_CODE");
    newGame.setName("თქვენი თამაშის სახელი");
    newGame.setDescription("თამაშის აღწერა - დეტალური ინფორმაცია თამაშზე");
    newGame.setCategory(Game.GameCategory.SLOTS); // ან TABLE_GAMES, VIDEO_POKER
    newGame.setProvider(provider);
    newGame.setIframeUrl("http://localhost:8888/slots/your-game-name/index.html");
    newGame.setThumbnailUrl("https://via.placeholder.com/300x200?text=Your+Game");
    newGame.setRtp(new BigDecimal("96.50")); // RTP პროცენტი
    newGame.setFeatured(true);
    newGame.setStatus(Game.GameStatus.ACTIVE);
    newGame.setSortOrder(10); // რიგითობა
    gameRepository.save(newGame);
    log.info("Game data loaded: {}", newGame.getName());
}
```

### ნაბიჯი 5: Backend-ის გადატვირთვა

```bash
# Backend დირექტორიაში
cd backend
lsof -ti:8080 | xargs kill -9
mvn spring-boot:run
```

---

## RTP ინტეგრაციის პრინციპი

### რა არის RTP?

**RTP (Return to Player)** - მოთამაშეზე დაბრუნების პროცენტი. მაგალითად, 96.50% RTP ნიშნავს:
- თამაშეს თუ მოთამაშეები 100₾ აბარებენ
- თამაში უკან 96.50₾-ს დააბრუნებს (საშუალოდ)
- კაზინოს რჩება 3.50₾

### როგორ მუშაობს Backend RTP Control?

1. **მოთამაშე აბარებს ფსონს** - Frontend უგზავნის `betAmount`-ს Backend-ს
2. **Backend ითვლის RTP-ს** - `GameService.java`-ში:
   ```java
   // RTP-based calculation
   BigDecimal targetRtp = game.getRtp(); // 96.50%
   boolean isWin = random.nextDouble() * 100 < targetRtp.doubleValue();

   if (isWin) {
       // გამოთვლა მოგების თანხის
       winAmount = betAmount.multiply(multiplier);
   }
   ```
3. **Backend აბრუნებს შედეგს** - Frontend იღებს:
   - `winAmount` - მოგების თანხა
   - `newBalance` - ახალი ბალანსი
   - `result` - თამაშის სპეციფიკური შედეგი

### RTP გარანტია

Backend უზრუნველყოფს:
- ✅ **სამართლიანობას** - ყველა შედეგი Backend-ში გენერირდება
- ✅ **RTP დაცვას** - გრძელვადიან პერსპექტივაში RTP პროცენტი დაცულია
- ✅ **უსაფრთხოებას** - Frontend ვერ მოიტყუებს სისტემას
- ✅ **თვალთვალს** - ყველა რაუნდი ინახება database-ში

---

## მაგალითები

### 1. Slot თამაში (Classic Slots)

**ფაილი:** `games/slots/first/index.html`

**თამაშის მექანიკა:**
- 3 რულეტი (🍒 🍋 🍊 🍇 💎 7️⃣)
- 1₾, 5₾, 10₾ ფსონები
- გამარჯვება: სამივე სიმბოლო ერთნაირი

**ძირითადი კოდი:**
```javascript
async function spin() {
    if (isSpinning) return;

    const betAmount = parseInt(document.querySelector('.bet-btn.active').dataset.bet);

    if (betAmount > balance) {
        alert('Insufficient balance!');
        return;
    }

    isSpinning = true;

    // Backend-ზე მიმართვა
    const result = await playRound(betAmount);

    if (result) {
        // შედეგის ვიზუალიზაცია
        displaySpinResult(result);

        if (result.win > 0) {
            showWinAnimation(result.win);
        }
    }

    isSpinning = false;
}
```

### 2. Card თამაში (Blackjack)

**ფაილი:** `games/table-games/blackjack/index.html`

**თამაშის მექანიკა:**
- Hit/Stand მოქმედებები
- Dealer vs Player
- Blackjack pays 3:2

**სპეციფიკური ინტეგრაცია:**
```javascript
async function stand() {
    // პლეიერი დასრულებულია, გადადის Dealer-ზე

    const result = await playRound(currentBet);

    if (result && result.result) {
        // Backend აბრუნებს dealer-ის ბარათებს
        const dealerCards = result.result.dealerCards;
        const outcome = result.result.outcome; // 'WIN', 'LOSE', 'PUSH'

        // ვიზუალიზაცია
        displayDealerCards(dealerCards);
        showOutcome(outcome, result.win);
    }
}
```

### 3. Video Poker (Jacks or Better)

**ფაილი:** `games/poker/jack-or-better/index.html`

**თამაშის მექანიკა:**
- 5 ბარათი
- Hold/Draw მექანიზმი
- Paytable: Royal Flush 250x, Straight Flush 50x...

**ორ-ფაზიანი ინტეგრაცია:**
```javascript
// ფაზა 1: Initial Deal
async function deal() {
    const result = await playRound(currentBet);

    if (result && result.result) {
        initialCards = result.result.cards;
        displayCards(initialCards);
        gamePhase = 'hold';
    }
}

// ფაზა 2: Draw
async function draw() {
    // მომხმარებელი აირჩია რომელი ბარათები დატოვოს
    const heldCards = getHeldCards();

    const result = await playRound(0); // ფსონი უკვე გადახდილია

    if (result && result.result) {
        finalCards = result.result.finalCards;
        handRank = result.result.handRank;

        displayCards(finalCards);
        showHandRank(handRank, result.win);
    }
}
```

---

## კატეგორიები

### SLOTS
- Classic Slots (3-reel)
- Video Slots (5-reel)
- Book-style Slots
- Hold & Win

### TABLE_GAMES
- Blackjack
- Roulette
- Baccarat

### VIDEO_POKER
- Jacks or Better
- Three Card Poker
- Caribbean Stud Poker

---

## ტიპური პრობლემები და გადაწყვეტა

### 1. CORS Error

**პრობლემა:** `Access to fetch at 'http://localhost:8080/api/...' from origin 'http://localhost:8888' has been blocked by CORS policy`

**გადაწყვეტა:**
Backend-ში `WebConfig.java` უკვე კონფიგურირებულია CORS-ისთვის:
```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000", "http://localhost:8888")
    .allowedMethods("GET", "POST", "PUT", "DELETE");
```

### 2. Session Token არ მუშაობს

**პრობლემა:** თამაში ვერ იღებს session token-ს

**გადაწყვეტა:**
1. შეამოწმეთ URL parameter: `?session=xxx&demo=false`
2. Frontend-ში `Games.tsx`:
   ```typescript
   const response = await axios.post('/api/user/games/launch', { gameId, demoMode })
   setGameUrl(response.data.launchUrl)
   ```

### 3. ბალანსი არ განახლდება

**პრობლემა:** თამაში ვერ განაახლებს ბალანსს

**გადაწყვეტა:**
```javascript
// Backend-ის response-დან აუცილებლად გამოიყენეთ newBalance
const spinData = await response.json();
balance = spinData.newBalance; // არა += winAmount
updateBalanceDisplay();
```

---

## სრული Checklist ახალი თამაშისთვის

- [ ] შექმენით თამაშის დირექტორია და `index.html`
- [ ] დაამატეთ თამაშის UI და სტილები
- [ ] ინტეგრირეთ RTP კოდი (`initGame()`, `playRound()`)
- [ ] დაამატეთ თამაში `DataLoader.java`-ში
- [ ] გადატვირთეთ Backend (`mvn spring-boot:run`)
- [ ] შეამოწმეთ თამაში Demo Mode-ში
- [ ] შეამოწმეთ თამაში Real Money Mode-ში
- [ ] დარწმუნდით რომ ბალანსი სწორად განახლდება
- [ ] ტესტირება: Win/Lose/Push სცენარები
- [ ] დაამატეთ Thumbnail სურათი (300x200px)

---

## დამატებითი რესურსები

### Game Categories Enum
```java
public enum GameCategory {
    SLOTS,
    TABLE_GAMES,
    VIDEO_POKER,
    LIVE_CASINO,
    JACKPOT
}
```

### Game Status Enum
```java
public enum GameStatus {
    ACTIVE,      // თამაში ხელმისაწვდომია
    INACTIVE,    // დროებით გამორთული
    MAINTENANCE  // მომსახურების რეჟიმში
}
```

### Session Token Format
```
UUID format: 550e8400-e29b-41d4-a716-446655440000
Expiration: 24 საათი
```

---

## საბოლოო შენიშვნები

1. **RTP პროცენტი** - რეკომენდებული: 95% - 99%
2. **Demo Mode** - ყოველთვის უნდა მუშაობდეს offline
3. **Error Handling** - აუცილებლად დაამატეთ try/catch ბლოკები
4. **Mobile Responsive** - გაითვალისწინეთ mobile მოწყობილობები
5. **Performance** - ოპტიმიზაცია დიდი asset ფაილებისთვის

---

**შეკითხვების შემთხვევაში:**
- Backend API: http://localhost:8080/swagger-ui.html
- Game Server: http://localhost:8888/
- Frontend: http://localhost:3000/

გილოცავთ! 🎰🎲🃏
