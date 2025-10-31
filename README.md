# Online Casino Platform

A full-featured online casino platform with user and admin interfaces, built with Spring Boot, React, and PostgreSQL.

## Project Structure

```
gambling/
├── backend/                 # Spring Boot backend
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
└── docker-compose.yml       # PostgreSQL database
```

## Features

### User Platform
- ✅ User registration and authentication (JWT)
- ✅ Wallet management (deposits, withdrawals)
- ✅ Game catalog and launch system (API + iframe)
- ✅ Transaction history
- ✅ Responsible gaming (deposit limits, self-exclusion)
- ✅ Balance tracking

### Admin Panel
- ✅ Admin authentication and role-based access
- ✅ User management (view, status changes, KYC)
- ✅ Game management (create, edit, status)
- ✅ Provider management
- ✅ Financial reports (GGR, revenue)
- ✅ CSV export functionality
- ✅ Audit logging

### Security Features
- ✅ JWT authentication with refresh tokens
- ✅ Password encryption (BCrypt)
- ✅ Role-based authorization
- ✅ CORS configuration
- ✅ Audit trail for all actions

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

## Setup Instructions

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
./mvnw spring-boot:run
```

The backend will start on http://localhost:8080

### 3. Start User Frontend

```bash
cd frontend-user
npm install
npm run dev
```

The user frontend will start on http://localhost:3000

### 4. Start Admin Frontend

```bash
cd frontend-admin
npm install
npm run dev
```

The admin frontend will start on http://localhost:3001

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
- **OWNER** - Full access
- **FINANCE** - Financial management
- **SUPPORT** - User support
- **CONTENT** - Game management

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
