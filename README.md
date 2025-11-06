# Online Casino Platform

A full-featured online casino platform with user and admin interfaces, built with Spring Boot, React, and PostgreSQL.

## Project Structure

```
gambling/
├── backend/                 # Spring Boot backend (Port 8080)
│   ├── src/main/java/
│   │   └── com/casino/
│   │       ├── config/      # Security, CORS configuration
│   │       ├── controller/  # REST API endpoints
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Database repositories
│   │       ├── service/     # Business logic
│   │       ├── security/    # JWT, authentication
│   │       └── exception/   # Exception handling
│   └── pom.xml
├── frontend-user/           # User React frontend (Port 3000)
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── context/
│       └── services/
├── frontend-admin/          # Admin React frontend (Port 3001)
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── context/
│       └── services/
├── games/                   # HTML5 games (Port 8888)
│   ├── slots/              # Slot games
│   ├── table-games/        # Blackjack, etc.
│   ├── poker/              # Video poker games
│   └── fun/                # Crash games
├── logs/                    # Service logs (auto-created)
├── install.sh              # Install dependencies
├── start.sh                # Start all services
├── stop.sh                 # Stop all services
└── docker-compose.yml      # PostgreSQL database
```

## Features

### 🎮 Games (9 Available)
1. **Classic Fruit Slot** - 3-reel classic slot machine
2. **Simple 5-Reel Video Slots** - Modern 5-reel with 25 paylines
3. **Book of Fortune** - Book-style slot with expanding symbols
4. **Fortune Hold & Win** - Hold & Win mechanic with jackpots
5. **Blackjack - Classic 21** - Traditional blackjack (RTP 99.5%)
6. **Jacks or Better** - Video poker (RTP 99.54%)
7. **Three Card Poker** - 3-card poker vs dealer
8. **Caribbean Stud Poker** - 5-card poker vs dealer
9. **Chicken Road** - Crash-style risk ladder game (NEW!)

### User Platform
- ✅ User registration and authentication (JWT)
- ✅ Wallet management (deposits, withdrawals)
- ✅ Game catalog and launch system (iframe integration)
- ✅ Transaction history with filtering
- ✅ Game history with detailed session tracking
- ✅ Responsible gaming (deposit limits, self-exclusion)
- ✅ Real-time balance updates
- ✅ Demo mode for all games

### Admin Panel
- ✅ Admin authentication and role-based access (7 roles)
- ✅ User management (view, status changes, KYC, balance adjustments)
- ✅ Game management (create, edit, status, featured)
- ✅ Provider management
- ✅ Transaction monitoring (deposits, withdrawals, bets, wins)
- ✅ Financial reports (GGR, revenue, player stats)
- ✅ CSV export functionality
- ✅ Audit logging for all admin actions
- ✅ Administrator management (OWNER only)
- ✅ Profile management for all admins

### Security Features
- ✅ JWT authentication with access/refresh tokens
- ✅ Password encryption (BCrypt with strength 12)
- ✅ Role-based authorization (7 admin roles)
- ✅ Session validation and ownership checks
- ✅ Session expiration (2 hours)
- ✅ Win amount validation (max 1000x multiplier)
- ✅ Fraud attempt detection and logging
- ✅ CORS configuration
- ✅ Audit trail for all actions
- ✅ Server-side RTP (Return to Player) calculation

## Tech Stack

**Backend:**
- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- JWT (jjwt 0.12.3)
- PostgreSQL
- Lombok

**Frontend:**
- React 18
- TypeScript
- Material-UI
- React Router
- React Query
- Axios
- Vite

**Database:**
- PostgreSQL 15 (Docker)

## 🚀 Quick Start (Recommended)

### Prerequisites
- **Java 17+** - Backend runtime
- **Maven** - Backend build tool
- **Node.js 18+** - Frontend runtime
- **Python 3** - Game server
- **PostgreSQL** - Database (running on port 5432)

### Automated Setup

```bash
# 1. Install all dependencies
./install.sh

# 2. Configure database (if not already done)
# Make sure PostgreSQL is running with database 'casino_db'

# 3. Start all services (backend, frontends, game server)
./start.sh

# 4. Access the platform
# User Portal:  http://localhost:3000
# Admin Portal: http://localhost:3001

# 5. Stop all services when done
./stop.sh
```

### Default Credentials

**User Account:**
- Email: `test@casino.ge`
- Password: `Test1234`
- Balance: ₾1000

**Admin Accounts:**
- Owner: `owner@casino.ge` / `Test1234`
- Finance: `finance@casino.ge` / `Test1234`
- Support: `support@casino.ge` / `Test1234`
- Content: `content@casino.ge` / `Test1234`

---

## Manual Setup (Alternative)

### 1. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:
- Database: `casino_db`
- Username: `postgres`
- Password: `postgres`

### 2. Start Backend Server

```bash
cd backend
mvn clean package -DskipTests
java -jar target/casino-platform-1.0.0.jar
```

The backend will start on http://localhost:8080

### 3. Start Game Server

```bash
python3 -m http.server 8888 --directory games
```

The game server will start on http://localhost:8888

### 4. Start User Frontend

```bash
cd frontend-user
npm install
npm run dev
```

The user frontend will start on http://localhost:3000

### 5. Start Admin Frontend

```bash
cd frontend-admin
npm install
npm run dev
```

The admin frontend will start on http://localhost:3001

---

## 🛠️ Management Scripts

The project includes three management scripts for easy setup and operation:

### install.sh
Installs all project dependencies:
- Checks for required prerequisites (Java, Maven, Node.js, Python, PostgreSQL)
- Builds backend with Maven
- Installs npm packages for both frontends
- Validates successful installation

```bash
./install.sh
```

### start.sh
Starts all services automatically:
- Backend (Spring Boot on port 8080)
- Game Server (Python HTTP on port 8888)
- User Portal (React on port 3000)
- Admin Portal (React on port 3001)
- Waits for services to be ready
- Creates PID files in `logs/` directory for tracking

```bash
./start.sh
```

### stop.sh
Stops all running services:
- Gracefully stops all services using PID files
- Falls back to port-based killing if needed
- Force kills if graceful shutdown fails
- Optional: Clean log files with `--clean` flag

```bash
# Stop all services
./stop.sh

# Stop and clean logs
./stop.sh --clean
```

**Logs Location:** All service logs are stored in `logs/` directory:
- `backend.log` - Spring Boot application
- `game-server.log` - Python HTTP server
- `frontend-user.log` - User portal console
- `frontend-admin.log` - Admin portal console

---

## API Endpoints

### Authentication (User)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh access token

### Authentication (Admin)
- `POST /api/admin/auth/login` - Admin login

### Wallet
- `GET /api/user/wallet/balance` - Get user balance
- `POST /api/user/wallet/deposit` - Deposit funds
- `POST /api/user/wallet/withdraw` - Withdraw funds
- `GET /api/user/wallet/transactions` - Transaction history

### Games
- `GET /api/user/games` - List all games
- `GET /api/user/games/category/{category}` - Games by category
- `GET /api/user/games/featured` - Featured games
- `POST /api/user/games/launch` - Launch a game
- `POST /api/user/games/bet` - Place bet
- `POST /api/user/games/win` - Process win

### Responsible Gaming
- `GET /api/user/responsible-gaming/limits` - Get current limits
- `PUT /api/user/responsible-gaming/limits` - Set limits
- `POST /api/user/responsible-gaming/self-exclusion` - Set self-exclusion
- `DELETE /api/user/responsible-gaming/self-exclusion` - Remove self-exclusion

### Admin - User Management
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/status` - Update user status
- `PUT /api/admin/users/{id}/kyc` - Update KYC status

### Admin - Game Management
- `GET /api/admin/games` - List all games
- `POST /api/admin/games` - Create game
- `PUT /api/admin/games/{id}/status` - Update game status
- `DELETE /api/admin/games/{id}` - Delete game
- `GET /api/admin/games/providers` - List providers
- `POST /api/admin/games/providers` - Create provider

### Admin - Financial Reports
- `GET /api/admin/financial/daily/{date}` - Daily report
- `GET /api/admin/financial/monthly/{year}/{month}` - Monthly report
- `GET /api/admin/financial/range?startDate&endDate` - Date range report
- `GET /api/admin/financial/export/csv?startDate&endDate` - Export to CSV

## Database Schema

### Core Entities
- **users** - User accounts with balance and limits
- **admins** - Admin accounts with roles
- **transactions** - All financial transactions
- **games** - Game catalog
- **game_providers** - Game provider information
- **game_sessions** - Active game sessions
- **game_rounds** - Individual game rounds
- **audit_logs** - Audit trail for all actions

## Security Configuration

### JWT Settings
- Access Token: 24 hours
- Refresh Token: 7 days
- Algorithm: HMAC-SHA256

### Admin Roles
- **OWNER** - Full access including administrator management
- **FINANCE** - Financial management, transaction monitoring, reports
- **SUPPORT** - User support, status changes, KYC management
- **CONTENT** - Game management, provider management
- **ANALYST** - Read-only access to reports and analytics
- **COMPLIANCE** - Responsible gaming and compliance monitoring
- **ADMIN** - General administrative tasks

### User Statuses
- **ACTIVE** - Can play normally
- **SUSPENDED** - Temporarily blocked
- **BLOCKED** - Permanently blocked
- **CLOSED** - Account closed

## Environment Variables

Create `application-prod.yml` for production:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}
```

## Default Credentials

After initial setup, create an admin account manually in the database:

```sql
INSERT INTO admins (username, email, password, first_name, last_name, role, status, created_at, updated_at)
VALUES ('admin', 'admin@casino.com', '$2a$12$encrypted_password', 'Admin', 'User', 'OWNER', 'ACTIVE', NOW(), NOW());
```

## Development Notes

1. **User ID Extraction**: The placeholder `getUserIdFromAuth()` methods need to be implemented to extract user ID from JWT tokens properly.

2. **Payment Integration**: The deposit/withdraw functionality needs integration with actual payment service providers (PSP).

3. **Game Provider Integration**: Game launch URLs should be configured based on actual provider requirements.

4. **Email Notifications**: Add email service for password reset, transaction confirmations, etc.

5. **Rate Limiting**: Implement rate limiting for API endpoints.

6. **Monitoring**: Add monitoring and logging (e.g., Prometheus, Grafana).

## License

Proprietary - All rights reserved
