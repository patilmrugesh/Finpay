# Wallet Service - API Endpoints Summary

This document outlines all available endpoints for the `wallet-service`.

> **IMPORTANT: Gateway Security Header**
> All endpoints are protected by the `InternalGatewayFilter`. When calling these endpoints directly (e.g., via Postman), you **must** include the following header:
> `X-Internal-Gateway-Token: finpay-internal-secret-key-2026`

---

## 1. Create Wallet
Creates a new wallet for a user.

- **URL:** `POST /api/wallet/create`
- **Request Body (`application/json`):**
  ```json
  {
    "userId": "UUID" // Required
  }
  ```

---

## 2. Get Balance
Retrieves the current balance of a user's wallet.

- **URL:** `GET /api/wallet/{userId}/balance`
- **Path Variables:**
  - `userId` (UUID): The ID of the user.

---

## 3. Deposit Funds
Deposits funds into a user's wallet.

- **URL:** `POST /api/wallet/{userId}/deposit`
- **Path Variables:**
  - `userId` (UUID): The ID of the user receiving the deposit.
- **Request Body (`application/json`):**
  ```json
  {
    "amount": 100.50,         // Required (Must be positive)
    "description": "Salary",  // Optional
    "counterpartyId": "UUID", // Optional (e.g., Bank account ID)
    "referenceId": "string"   // Optional (For idempotency/duplicate prevention)
  }
  ```

---

## 4. Withdraw Funds
Withdraws funds from a user's wallet. Fails if the wallet has insufficient funds or is frozen.

- **URL:** `POST /api/wallet/{userId}/withdraw`
- **Path Variables:**
  - `userId` (UUID): The ID of the user withdrawing funds.
- **Request Body (`application/json`):**
  ```json
  {
    "amount": 50.00,          // Required (Must be positive)
    "description": "ATM",     // Optional
    "counterpartyId": "UUID", // Optional 
    "referenceId": "string"   // Optional (For idempotency)
  }
  ```

---

## 5. Transfer Funds (P2P)
Transfers funds from the sender's wallet to another user's wallet. Fails if either wallet is frozen or if the sender has insufficient funds.

- **URL:** `POST /api/wallet/{userId}/transfer`
- **Path Variables:**
  - `userId` (UUID): The ID of the sender.
- **Request Body (`application/json`):**
  ```json
  {
    "recipientUserId": "UUID", // Required
    "amount": 25.50,           // Required (Min 0.01)
    "description": "Dinner",   // Optional
    "referenceId": "string"    // Optional (For idempotency)
  }
  ```

---

## 6. Get Transaction History
Retrieves a paginated list of transactions for a specific user's wallet, ordered by newest first.

- **URL:** `GET /api/wallet/{userId}/transactions`
- **Path Variables:**
  - `userId` (UUID): The ID of the user.
- **Query Parameters:**
  - `page` (integer): Page number (0-indexed). Default: `0`.
  - `size` (integer): Number of records per page. Default: `10`.
- **Example:** `GET /api/wallet/34e3bba1-d289-4d7f-9505-a74b1b35683e/transactions?page=0&size=5`
