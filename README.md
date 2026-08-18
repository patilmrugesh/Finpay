# 💳 FinPay — Distributed Digital Wallet & Payment Platform

FinPay is a distributed digital wallet and payment platform built using **Java, Spring Boot, PostgreSQL, Docker, Kafka, and Angular**.

The project follows a **microservices architecture** with separate databases for each service.

---

## 🏗️ Architecture

```text
                         ┌──────────────┐
                         │   Angular    │
                         │    :4200     │
                         └──────┬───────┘
                                │
                                ▼
                         ┌──────────────┐
                         │ API Gateway  │
                         │    :8080     │
                         └──────┬───────┘
                                │
                 ┌──────────────┼──────────────┐
                 ▼              ▼              ▼
          ┌────────────┐ ┌────────────┐ ┌────────────┐
          │    Auth    │ │   Wallet   │ │  Payment   │
          │   :8081    │ │   :8082    │ │   :8083    │
          └─────┬──────┘ └─────┬──────┘ └─────┬──────┘
                │              │              │
                ▼              ▼              ▼
             Auth DB       Wallet DB      Payment DB
                                            │
                                            ▼
                                          Kafka
                                            │
                              ┌─────────────┴─────────────┐
                              ▼                           ▼
                       Notification                    Audit
                         :8084                         :8085
```

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot
* **API Gateway:** Spring Cloud Gateway
* **Database:** PostgreSQL
* **Messaging:** Apache Kafka
* **Authentication:** Spring Security + JWT
* **Frontend:** Angular
* **Infrastructure:** Docker & Docker Compose

---

## 📦 Services

| Service              |   Port | Responsibility              | Status         |
| -------------------- | -----: | --------------------------- | -------------- |
| API Gateway          | `8080` | Routing & JWT filtering     | 🟡 Setup       |
| Auth Service         | `8081` | Authentication & users      | 🔨 In Progress |
| Wallet Service       | `8082` | Wallet & balance management | 🔨 In Progress |
| Payment Service      | `8083` | Transfers & payments        | ⏳ Pending      |
| Notification Service | `8084` | Kafka notifications         | ⏳ Pending      |
| Audit Service        | `8085` | Transaction/event auditing  | ⏳ Pending      |
| Frontend             | `4200` | Angular UI                  | ⏳ Pending      |

---

## 🗄️ Database Setup

PostgreSQL databases are running through Docker Compose.

| Database           | Host Port |
| ------------------ | --------: |
| `finpay_auth_db`   |    `5434` |
| `finpay_wallet_db` |    `5435` |

Credentials for local development:

```text
Username: finpay_user
Password: 0000
```

Start the databases:

```bash
docker compose up -d
```

Check containers:

```bash
docker ps
```

Stop containers:

```bash
docker compose down
```

---

## 👨‍💻 Current Project Progress

### ✅ Completed

* [x] Project repository and structure
* [x] Microservices project structure
* [x] Docker Compose setup
* [x] PostgreSQL Docker containers
* [x] Auth database setup
* [x] Wallet database setup
* [x] Local development environment
* [x] Git branching workflow

### 🔨 Currently Working On

**Dev A — Auth Service**

* [ ] User registration
* [ ] Login
* [ ] Password hashing
* [ ] JWT generation
* [ ] JWT validation
* [ ] Spring Security configuration
* [ ] Auth APIs

### 🔨 In Progress — Wallet Service

**Dev B — Wallet Service**

The next task is to implement the Wallet Service.

Expected responsibilities:

* [ ] Wallet creation
* [ ] Wallet balance
* [ ] Deposit
* [ ] Withdrawal
* [ ] Debit/Credit operations
* [ ] Concurrency handling
* [ ] Optimistic/Pessimistic locking
* [ ] api gateway to validate request using jwt  and route request 

> **Important:** Wallet balance operations must be concurrency-safe because multiple transactions may access the same wallet simultaneously.

### ⏳ Remaining Services

After Auth and Wallet:

* [ ] Payment Service
* [ ] Kafka integration
* [ ] Notification Service
* [ ] Audit Service
* [ ] API Gateway integration
* [ ] Angular frontend
* [ ] Integration testing
* [ ] End-to-end testing

---

## 🔒 Development Rules

### Money

Never use `float` or `double` for monetary values.

Use:

```java
BigDecimal
```

### Database Isolation

Each service owns its own database.

**Do not:**

* Query another service's database
* Share JPA entities between services
* Create cross-database dependencies

Services communicate using **REST APIs or Kafka**.

### Git Workflow

Never push directly to `main`.

Create a feature branch:

```bash
git checkout main
git pull origin main
git checkout -b feature/<feature-name>
```

Example:

```bash
git checkout -b feature/wallet-service
```

Push:

```bash
git push origin feature/wallet-service
```

Then create a Pull Request.

---

## 🚀 Project Status

**Current Phase:** 🟡 Initial Development

```text
Project Setup       ████████████████████  Done
Auth Service        ███████░░░░░░░░░░░░░  In Progress
Wallet Service      ███████░░░░░░░░░░░░░  In Progress
Payment Service     ░░░░░░░░░░░░░░░░░░░░  Pending
Kafka               ░░░░░░░░░░░░░░░░░░░░  Pending
Notification        ░░░░░░░░░░░░░░░░░░░░  Pending
Audit               ░░░░░░░░░░░░░░░░░░░░  Pending
Frontend            ░░░░░░░░░░░░░░░░░░░░  Pending
```

**Current focus:** Auth Service implementation + Wallet Service development.
