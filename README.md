# 💳 FinPay — Distributed Digital Wallet & Payment Platform

FinPay is a distributed digital wallet and payment platform built using **Java 21, Spring Boot, PostgreSQL, Docker, Kafka, and Angular**.

The project follows a **microservices architecture** with strict **database-per-service isolation** and unified routing through an **API Gateway**.

---

## 📢 Team Handover & Latest Progress

> **For Collaborators / Teammates**
>
> The following components are currently implemented and working:
>
> ### 🚀 Completed
>
> **1. End-to-End Integration**
>
> * `auth-service`, `wallet-service`, and `api-gateway` are fully functional.
> * All services are connected to their respective PostgreSQL Docker instances.
>
> **2. Port & Secret Alignment**
>
> * **API Gateway:** `8083`
> * **Auth Service:** `8082` → PostgreSQL `5434`
> * **Wallet Service:** `8084` → PostgreSQL `5435`
> * Local PostgreSQL credentials are standardized:
>
>   * Username: `finpay_user`
>   * Password: `0000`
>
> **3. Perimeter Security**
>
> * Direct requests to downstream services are blocked with `403 Forbidden`.
> * All external requests must go through the API Gateway on port `8083`.
> * The Gateway appends the internal gateway token before forwarding requests.
>
> **4. Auto-Wallet Provisioning**
>
> * When a new user's balance is checked, the Wallet Service automatically creates an active INR wallet with a `0.00` balance if one does not already exist.
>
> **5. Concurrency & Locking**
>
> * Optimistic locking and transactional isolation are implemented for wallet deposit, withdrawal, and ledger operations.

---

# 🏗️ Architecture

```text
                         ┌──────────────┐
                         │    Angular   │
                         │    :4200     │
                         └──────┬───────┘
                                │
                                ▼
                         ┌──────────────┐
                         │ API Gateway  │
                         │    :8083     │
                         └──────┬───────┘
                                │
                  Adds X-Internal-Gateway-Token
                                │
                 ┌──────────────┴──────────────┐
                 ▼                             ▼
          ┌────────────┐                ┌────────────┐
          │    Auth    │                │   Wallet   │
          │   :8082    │                │   :8084    │
          └─────┬──────┘                └─────┬──────┘
                │                             │
                ▼                             ▼
            Auth DB                      Wallet DB
             :5434                         :5435
```

---

# 🛠️ Tech Stack

| Category                    | Technology                            |
| --------------------------- | ------------------------------------- |
| **Backend**                 | Java 21, Spring Boot, Spring Data JPA |
| **API Gateway**             | Spring Cloud Gateway (MVC/WebMVC)     |
| **Database**                | PostgreSQL                            |
| **Database Infrastructure** | Docker                                |
| **Migrations**              | Flyway                                |
| **Authentication**          | Spring Security, JWT                  |
| **JWT Algorithm**           | HMAC-SHA256                           |
| **Internal Security**       | Gateway Secret Token Header           |
| **Concurrency**             | JPA Optimistic Locking (`@Version`)   |
| **Monetary Values**         | `BigDecimal`                          |
| **Messaging & Cache**       | Apache Kafka, Redis *(Planned)*       |
| **Frontend**                | Angular                               |

---

# 📦 Services

| Service             |   Port | Responsibility                          | Perimeter         | Status    |
| ------------------- | -----: | --------------------------------------- | ----------------- | --------- |
| **API Gateway**     | `8083` | Unified routing, CORS, token header     | Public Entry      | 🟢 Active |
| **Auth Service**    | `8082` | Registration, login, JWT authentication | Protected (`403`) | 🟢 Active |
| **Wallet Service**  | `8084` | Balance, deposits, withdrawals, ledger  | Protected (`403`) | 🟢 Active |
| **Payment Service** | `8085` | P2P transfers & transaction engine      | Protected (`403`) | ⏳ Planned |
| **Frontend**        | `4200` | Angular client application              | Client UI         | ⏳ Pending |

---

# 🗄️ Database Setup

PostgreSQL instances are provisioned using **Docker Compose**.

| Database   | Container          | Host Port | Database           | Username      | Password |
| ---------- | ------------------ | --------: | ------------------ | ------------- | -------- |
| **Auth**   | `finpay-auth-db`   |    `5434` | `finpay_auth_db`   | `finpay_user` | `0000`   |
| **Wallet** | `finpay-wallet-db` |    `5435` | `finpay_wallet_db` | `finpay_user` | `0000`   |

## Start the Databases

```bash
# Start PostgreSQL containers
docker compose up -d

# Check running containers
docker ps
```

## Reset Database Volumes

> ⚠️ This removes the existing Docker volumes and database data.

```bash
docker compose down -v
docker compose up -d
```

---

# 🧪 Running & Testing the Application

## 1. Start the Infrastructure

Make sure Docker Desktop is running:

```bash
docker compose up -d
```

Verify:

```bash
docker ps
```

---

## 2. Start the Services

Run the following applications from IntelliJ or your terminal:

| Application                |   Port |
| -------------------------- | -----: |
| `AuthServiceApplication`   | `8082` |
| `WalletServiceApplication` | `8084` |
| `ApiGatewayApplication`    | `8083` |

---

# 🔄 End-to-End API Testing

All external API requests should go through the **API Gateway (`8083`)**.

## Step A — Register a User

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Mrugesh Patil",
    "email": "mrugesh@finpay.com",
    "password": "password123"
  }'
```

> Copy the returned `userId` and `token` from the response.

---

## Step B — Login

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mrugesh@finpay.com",
    "password": "password123"
  }'
```

---

## Step C — Check Wallet Balance

Replace `<PASTE_USER_ID_HERE>` with the user's ID:

```bash
curl -X GET \
  http://localhost:8083/api/wallet/<PASTE_USER_ID_HERE>/balance
```

Expected response:

```json
{
  "balance": 0.0000,
  "currency": "INR"
}
```

> If the wallet doesn't exist, the Wallet Service automatically provisions an active INR wallet with a `0.00` balance.

---

## Step D — Deposit Funds

```bash
curl -X POST \
  http://localhost:8083/api/wallet/<PASTE_USER_ID_HERE>/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "description": "Initial Wallet Top-Up"
  }'
```

---

## Step E — Withdraw Funds

```bash
curl -X POST \
  http://localhost:8083/api/wallet/<PASTE_USER_ID_HERE>/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 250.00,
    "description": "ATM Withdrawal"
  }'
```

---

## Step F — Retrieve Transaction History

```bash
curl -X GET \
  http://localhost:8083/api/wallet/<PASTE_USER_ID_HERE>/transactions
```

---

# 🔒 Security Perimeter Verification

The downstream Wallet Service should **not** be directly accessible.

Try calling it directly:

```bash
curl -X GET \
  http://localhost:8084/api/wallet/<PASTE_USER_ID_HERE>/balance
```

### Expected Result

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Direct access to internal microservices is blocked. Please route all requests through the API Gateway (Port 8083)."
}
```

This confirms that requests are required to enter through the **API Gateway**.

---

# 🔐 Engineering Guidelines

### 💰 Monetary Precision

Always use:

```java
BigDecimal
```

for currency and wallet balances.

**Never use `float` or `double` for monetary values.**

### 🗄️ Database Isolation

Each microservice owns its own database.

Services must **never**:

* Share database connections
* Access another service's tables directly
* Create cross-service database dependencies

### 🛡️ Perimeter Defense

All downstream APIs must validate:

```text
X-Internal-Gateway-Token
```

Direct access to internal services should be rejected.

### 🌿 Git Workflow

Create a feature branch for every task:

```bash
git checkout -b feature/<name>
```

Keep your branch synchronized with `main` and rebase before submitting a Pull Request.
