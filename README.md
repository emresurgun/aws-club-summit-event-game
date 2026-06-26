# AWS Cloud Club Quiz Platform

A real-time, multi-player quiz platform built for live events. Supports 300+ concurrent players with sub-second WebSocket messaging.

Built by the IT team of **AWS Cloud Club Istanbul Okan University** and deployed at the C.O.D.E Summit on April 21, 2026.

---

## Project Status

This platform was successfully used in production at a live event with simultaneous participants. The system handled real-time question delivery, answer collection, leaderboard updates, and prize distribution without interruption.

---

## Motivation

Most quiz platforms (Kahoot, Mentimeter, etc.) are third-party SaaS tools with limited customization, branding restrictions, and no control over data. The goal of this project was to build a fully owned, customizable alternative that the club could deploy, modify, and extend for any event format.

The result is a event-ready platform with a dedicated game engine, admin panel, host screen, and player interface.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    Frontend (React + Nginx)                │
│         Landing Page │ Admin Panel │ Player │ Host Screen  │
└───────────┬──────────────────────────────┬────────────────┘
            │ REST (HTTP)                  │ WebSocket (STOMP)
            ▼                              ▼
┌───────────────────────┐    ┌─────────────────────────────┐
│      GameAdmin        │    │         GameEngine           │
│   Spring Boot :8081   │◄───│     Spring Boot :8080        │
│   PostgreSQL 16       │    │     Redis 7                  │
└───────────────────────┘    └─────────────────────────────┘
```

| Service | Technology | Port | Responsibility |
|---|---|---|---|
| GameEngine | Spring Boot 3.5 / WebSocket / STOMP | 8080 | Real-time game flow, session management |
| GameAdmin | Spring Boot 3.5 / REST | 8081 | Quiz management, user auth, result storage |
| Frontend | React 19 / Nginx | 80 | Player, Admin, and Host interfaces |
| Redis | Redis 7 | 6379 | Session store, leaderboard (ZSET), game state |
| PostgreSQL | PostgreSQL 16 | 5432 | Quiz data, users, game history |

---

## Game Flow

```
Player joins (JOIN_ACK)
       ↓
Admin starts the game (GAME_STARTED) → 5s countdown
       ↓
Question starts (QUESTION_START) → auto timer
       ↓
Player submits answer (ANSWER_RECEIVED)
       ↓
Timer expires (QUESTION_END) → correct answer revealed
       ↓
Personal result (ANSWER_REVEAL) → score + accuracy
       ↓
Admin approves leaderboard (LEADERBOARD_PENDING → 30s window)
       ↓
Scores broadcast (SCORE_REVEAL) → top 10
       ↓
Next question... (loop)
       ↓
Game ends (GAME_FINISHED) → results saved to PostgreSQL
```

---

## Features

- **Real-time gameplay** over WebSocket (STOMP protocol)
- **300+ concurrent players** — Redis-backed session and leaderboard
- **Admin panel** — create quizzes, manage questions, control game flow
- **Host screen** — full-screen projection view with QR code for joining
- **Live leaderboard** — Redis ZSET with per-question score updates
- **Ban system** — instant player removal with O(1) Redis lookup
- **Reconnect support** — players rejoin after page refresh via sessionStorage
- **Game history** — all results persisted to PostgreSQL after each game
- **Docker Compose** — single command deployment for all services

---

## WebSocket Channel Map

| Channel | Direction | Messages |
|---|---|---|
| `/user/queue/personal` | Server → Player | `JOIN_ACK`, `ANSWER_RECEIVED`, `ANSWER_REVEAL`, `BANNED`, `ERROR` |
| `/topic/game/{id}/lobby` | Server → All | `WAITING_ROOM_UPDATE` |
| `/topic/game/{id}` | Server → All | `GAME_STARTED`, `QUESTION_START`, `QUESTION_END`, `SCORE_REVEAL`, `GAME_FINISHED` |
| `/topic/game/{id}/host` | Server → Admin/Host | `HOST_WAITING_UPDATE`, `HOST_ANSWER_COUNT`, `LEADERBOARD_PENDING` + all above |
| `/user/queue/admin` | Server → Admin | `LEADERBOARD_PENDING` (fallback) |

---

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Node.js 20 (for local development)

### Run with Docker Compose

**1. Clone the repository:**
```bash
git clone https://github.com/emresurgun/aws-club-summit-event-game.git
cd aws-club-summit-event-game
```

**2. Build the backend JARs:**
```bash
cd GameAdmin && mvn clean package -DskipTests && cd ..
cd GameEngine && mvn clean package -DskipTests && cd ..
```

**3. Set frontend environment:**
```bash
# quiz-frontend/.env.development
REACT_APP_WS_URL=http://localhost:8080/ws
REACT_APP_API_URL=http://localhost:8081
```

**4. Start all services:**
```bash
docker compose up --build
```

The application will be available at `http://localhost`.

### Default Credentials

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| host | host123 | HOST |

> ⚠️ Change all passwords and the JWT secret before any production deployment.

---

## Environment Variables

### GameEngine

| Variable | Description | Default |
|---|---|---|
| `REDIS_HOST` | Redis host address | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `GAME_ADMIN_URL` | GameAdmin base URL | `http://localhost:8081` |
| `JWT_SECRET` | JWT signing key | `dev-secret-key` |

### GameAdmin

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/gameadmin` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | — |
| `JWT_SECRET` | JWT signing key | `dev-secret-key` |

---

## Project Structure

```
Online_Test_Platform/
├── GameEngine/                    # WebSocket game engine (Spring Boot)
│   └── src/main/java/com/awsokanclub/GameEngine/
│       ├── controller/            # GameController, AdminController
│       ├── service/               # GameFlowService, LeaderboardService,
│       │                          # GameSessionService, ModerationService
│       ├── model/                 # GameSession, GameState
│       └── dto/                   # Inbound & outbound DTOs
├── GameAdmin/                     # REST API and quiz management (Spring Boot)
│   └── src/main/java/com/awsokanclub/GameAdmin/
│       ├── controller/            # GameController, QuestionController, AuthController
│       ├── service/               # GameService, QuestionService
│       └── model/                 # Game, Question, AdminUser
├── quiz-frontend/                 # React application
│   └── src/
│       ├── pages/                 # PlayerPage, AdminPage, HostPage, LandingPage
│       ├── services/              # api.js (REST calls to GameAdmin)
│       └── config.js              # URL configuration via env vars
└── docker-compose.yml
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 3.5 / Java 21 |
| Real-time protocol | WebSocket / STOMP |
| In-memory store | Redis 7 (session, state, leaderboard) |
| Relational database | PostgreSQL 16 |
| Frontend | React 19 |
| Web server | Nginx |
| Containerization | Docker / Docker Compose |
| Cloud | AWS EC2 |

---

## Performance Notes

- Session lookups use a `user_to_session:{gameId}:{userId}` Redis index for O(1) ban and routing operations — no full player list scans.
- Score reveals use Redis `MGET` pipeline to fetch all sessions in a single round-trip instead of N individual GET calls.
- A `ThreadPoolTaskScheduler` with 10 threads handles all game timers, preventing single-thread contention across concurrent games.
- `questionScores` map is cleared after each game to prevent memory accumulation across sessions.

---

## Key Design Decisions

**Why a separate GameEngine and GameAdmin?**
Separating the real-time WebSocket engine from the REST management API allows each service to scale independently and keeps concerns isolated. GameAdmin can be taken down for maintenance without affecting active games.

**Why Redis for leaderboard?**
Redis Sorted Sets (ZSET) provide O(log N) score updates and O(N) range queries, which is exactly the access pattern needed for real-time leaderboard updates after each question.

**Why sessionId is not exposed to the frontend?**
Ban operations accept only `userId`. The backend resolves `sessionId` internally via the `user_to_session` index. This keeps session internals server-side and simplifies the frontend integration contract.

---

## Contributors

AWS Cloud Club Istanbul Okan University — IT Team

---

## License

This project was developed by AWS Cloud Club Istanbul Okan University.
