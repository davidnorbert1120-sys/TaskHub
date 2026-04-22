# TaskHub

> Full-stack task management application — Spring Boot backend + Angular frontend

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Angular](https://img.shields.io/badge/Angular-17+-red?logo=angular)
![MySQL](https://img.shields.io/badge/MySQL-8-blue?logo=mysql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## About

TaskHub is a Trello-inspired project and task management web application. Users can create projects, invite team members, assign tasks with priorities and deadlines, and track progress through a Kanban-style board.

> **Status:** 🚧 In progress

---

## Features

- **Authentication** — JWT-based registration & login
- **Projects** — Create, edit, delete; invite members by email
- **Role-based access** — OWNER / MEMBER per project
- **Tasks** — Status (TODO / IN_PROGRESS / DONE), priority, due date, assignee
- **Comments** — Per-task comment threads
- **Dashboard** — All your tasks, upcoming deadlines
- **Search & filter** — By status, priority, assignee
- **Responsive UI** — Works on mobile too

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL 8 (production), H2 (tests) |
| Auth | JWT (jjwt) |
| Frontend | Angular 17+, Angular Material, RxJS |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Karma, Jasmine |
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

- Java 17+
- Node.js 18+ & npm
- Docker & Docker Compose

### Run with Docker (recommended)

```bash
# Copy and fill in environment variables
cp .env.example .env

# Start everything
docker-compose up --build
```

- Frontend: http://localhost
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

## API Documentation

Swagger UI is available at `/swagger-ui.html` when the backend is running.

---

## Running Tests

**Backend:**
```bash
cd backend
./mvnw test
```

**Frontend:**
```bash
cd frontend
ng test
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```env
DB_USERNAME=taskhub
DB_PASSWORD=your_password
DB_NAME=taskhub_db
JWT_SECRET=your_jwt_secret_key
```

---

## License

[MIT](LICENSE)
