# TaskHub

> Full-stack task management application — Spring Boot backend + Angular frontend

![Java](https://img.shields.io/badge/Java-21-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Angular](https://img.shields.io/badge/Angular-17+-red?logo=angular)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## Live Demo

- **Frontend:** https://taskhub-davidnorbert.netlify.app
- **Backend API:** https://taskhub-backend-z95q.onrender.com
- **Swagger UI:** https://taskhub-backend-z95q.onrender.com/swagger-ui.html

---

## About

TaskHub is a Trello-inspired project and task management web application. Users can create projects, invite team members, assign tasks with priorities and deadlines, and track progress through a Kanban-style board.

---

## Features

- **Authentication** — JWT-based registration & login, Google OAuth2 login
- **Projects** — Create, edit, delete; invite members by email
- **Role-based access** — OWNER / MEMBER per project
- **Tasks** — Status (TODO / IN_PROGRESS / DONE), priority, due date, assignee
- **Kanban board** — Drag & drop task management
- **Comments** — Per-task comment threads
- **Real-time updates** — WebSocket-based live notifications

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16 |
| Auth | JWT (jjwt), Google OAuth2 |
| Frontend | Angular 17+, Angular CDK, RxJS |
| Real-time | WebSocket (STOMP) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Deployment | Render (backend), Netlify (frontend) |
| DevOps | Docker, Docker Compose |

---

## Project Structure

```
taskhub/
├── backend/          # Spring Boot REST API
├── frontend/         # Angular SPA
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 21+
- Node.js 18+ & npm
- Docker & Docker Compose

### Run with Docker (recommended)

```bash
cp .env.example .env
docker-compose up --build
```

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Run locally (development)

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
ng serve
```

---

## Environment Variables

The backend requires the following environment variables:

| Variable | Description |
|---|---|
| `DATABASE_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWT tokens |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins |
| `FRONTEND_URL` | Frontend base URL for OAuth2 redirect |

---

## API Documentation

Swagger UI is available at `/swagger-ui.html` when the backend is running.

---

## License

[MIT](LICENSE)
