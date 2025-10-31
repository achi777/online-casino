// Game Configuration
const symbols = ['🍒', '🍋', '🍊', '🍇', '🍉', '💎', '🎰', '7️⃣'];
const payouts = {
    '🍒': 10,
    '🍋': 15,
    '🍊': 20,
    '🍇': 25,
    '🍉': 30,
    '💎': 50,
    '🎰': 100,
    '7️⃣': 500
};

// Game State
let balance = 0;
let betAmount = 1;
let isSpinning = false;
let totalBets = 0;
let totalWins = 0;
let roundsPlayed = 0;

// API Configuration
const API_BASE_URL = window.location.hostname === 'localhost'
    ? 'http://localhost:8080/api'
    : '/api';

// Session Management
let sessionToken = null;
let gameId = null;
let userId = null;

// Get URL parameters
function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// Initialize game from URL parameters
function initializeGame() {
    sessionToken = getUrlParameter('sessionToken');
    gameId = getUrlParameter('gameId');
    userId = getUrlParameter('userId');

    console.log('Game initialized with:', { sessionToken, gameId, userId });

    if (sessionToken && gameId && userId) {
        // In real scenario, validate session with backend
        loadBalance();
    } else {
        // Demo mode
        balance = 100;
        updateDisplay();
        console.log('Running in demo mode');
    }
}

// Load user balance from backend
async function loadBalance() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

        if (!token) {
            // Demo mode
            balance = 100;
            updateDisplay();
            return;
        }

        const response = await fetch(`${API_BASE_URL}/user/wallet/balance`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            balance = await response.json();
            updateDisplay();
        } else {
            console.error('Failed to load balance');
            balance = 100; // Demo fallback
            updateDisplay();
        }
    } catch (error) {
        console.error('Error loading balance:', error);
        balance = 100; // Demo fallback
        updateDisplay();
    }
}

// Place bet via API
async function placeBet(amount) {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

        if (!token || !gameId || !sessionToken) {
            // Demo mode - just deduct from local balance
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }

        const response = await fetch(`${API_BASE_URL}/user/games/bet`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                gameId: parseInt(gameId),
                sessionToken: sessionToken,
                betAmount: amount,
                roundId: `round-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
            })
        });

        if (response.ok) {
            const result = await response.json();
            balance = result.balance;
            return true;
        } else {
            const error = await response.json();
            showMessage('Bet failed: ' + (error.error || 'Unknown error'), 'error');
            return false;
        }
    } catch (error) {
        console.error('Error placing bet:', error);
        // Demo mode fallback
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

// Process win via API
async function processWin(amount) {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

        if (!token || !gameId || !sessionToken) {
            // Demo mode - just add to local balance
            balance += amount;
            return true;
        }

        const response = await fetch(`${API_BASE_URL}/user/games/win`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                gameId: parseInt(gameId),
                sessionToken: sessionToken,
                winAmount: amount,
                roundId: `round-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
            })
        });

        if (response.ok) {
            const result = await response.json();
            balance = result.balance;
            return true;
        } else {
            console.error('Failed to process win');
            balance += amount; // Fallback
            return true;
        }
    } catch (error) {
        console.error('Error processing win:', error);
        balance += amount; // Fallback
        return true;
    }
}

// Change bet amount
function changeBet(delta) {
    const newBet = betAmount + delta;
    if (newBet >= 1 && newBet <= Math.min(100, balance)) {
        betAmount = newBet;
        document.getElementById('betAmount').value = betAmount;
        document.getElementById('spinCost').textContent = `₾${betAmount.toFixed(2)}`;
    }
}

// Set bet amount
function setBet(amount) {
    if (amount <= balance) {
        betAmount = amount;
        document.getElementById('betAmount').value = betAmount;
        document.getElementById('spinCost').textContent = `₾${betAmount.toFixed(2)}`;
    }
}

// Update display
function updateDisplay() {
    document.getElementById('balance').textContent = `₾${balance.toFixed(2)}`;
    document.getElementById('totalBets').textContent = `₾${totalBets.toFixed(2)}`;
    document.getElementById('totalWins').textContent = `₾${totalWins.toFixed(2)}`;
    document.getElementById('roundsPlayed').textContent = roundsPlayed;
}

// Show message
function showMessage(text, type = 'info') {
    const messageEl = document.getElementById('message');
    messageEl.textContent = text;
    messageEl.style.color = type === 'win' ? '#4ade80' : type === 'error' ? '#ff6b6b' : '#ffd700';
}

// Spin reels with animation
function spinReels() {
    const reels = [
        document.getElementById('reel1'),
        document.getElementById('reel2'),
        document.getElementById('reel3')
    ];

    // Add spinning class
    reels.forEach(reel => {
        reel.classList.add('spinning');
    });

    // Generate random results
    const results = [
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)]
    ];

    // Stop reels one by one
    return new Promise((resolve) => {
        setTimeout(() => {
            reels[0].querySelector('.symbol').textContent = results[0];
            reels[0].classList.remove('spinning');
        }, 1000);

        setTimeout(() => {
            reels[1].querySelector('.symbol').textContent = results[1];
            reels[1].classList.remove('spinning');
        }, 1500);

        setTimeout(() => {
            reels[2].querySelector('.symbol').textContent = results[2];
            reels[2].classList.remove('spinning');
            resolve(results);
        }, 2000);
    });
}

// Check for win
function checkWin(results) {
    // Check if all three symbols match
    if (results[0] === results[1] && results[1] === results[2]) {
        const symbol = results[0];
        const multiplier = payouts[symbol];
        const winAmount = betAmount * multiplier;
        return { win: true, amount: winAmount, multiplier, symbol };
    }
    return { win: false, amount: 0 };
}

// Main spin function
async function spin() {
    if (isSpinning) return;

    // Check if user has enough balance
    if (balance < betAmount) {
        showMessage('Insufficient balance!', 'error');
        return;
    }

    isSpinning = true;
    document.getElementById('spinButton').disabled = true;
    showMessage('Good luck! 🍀');

    // Place bet
    const betSuccess = await placeBet(betAmount);
    if (!betSuccess) {
        isSpinning = false;
        document.getElementById('spinButton').disabled = false;
        return;
    }

    totalBets += betAmount;
    roundsPlayed++;
    updateDisplay();

    // Spin the reels
    const results = await spinReels();

    // Check for win
    const winResult = checkWin(results);

    if (winResult.win) {
        // Process win
        await processWin(winResult.amount);
        totalWins += winResult.amount;

        // Show win line animation
        const winLine = document.querySelector('.win-line');
        winLine.classList.add('active');

        setTimeout(() => {
            winLine.classList.remove('active');
        }, 2000);

        if (winResult.multiplier >= 500) {
            showMessage(`🎉 JACKPOT! You won ₾${winResult.amount.toFixed(2)}! 🎉`, 'win');
        } else if (winResult.multiplier >= 50) {
            showMessage(`💎 BIG WIN! You won ₾${winResult.amount.toFixed(2)}! 💎`, 'win');
        } else {
            showMessage(`🎊 You won ₾${winResult.amount.toFixed(2)}! (${winResult.multiplier}x)`, 'win');
        }
    } else {
        showMessage('Try again!');
    }

    updateDisplay();

    isSpinning = false;
    document.getElementById('spinButton').disabled = false;
}

// Keyboard controls
document.addEventListener('keydown', (e) => {
    if (e.code === 'Space' && !isSpinning) {
        e.preventDefault();
        spin();
    }
});

// Initialize on load
window.addEventListener('load', () => {
    initializeGame();
    updateDisplay();

    // Send message to parent window that game is loaded
    if (window.parent !== window) {
        window.parent.postMessage({ type: 'gameLoaded', gameId }, '*');
    }
});

// Listen for messages from parent window
window.addEventListener('message', (event) => {
    if (event.data.type === 'updateBalance') {
        balance = event.data.balance;
        updateDisplay();
    }
});
