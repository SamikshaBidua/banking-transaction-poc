# Banking Transaction POC

A simplified banking system POC demonstrating core banking functionalities: transaction processing (withdrawals and top-ups), card validation, routing, and role-based transaction monitoring with secure PIN hashing and card encryption.

## Architecture

The system consists of two interconnected services with separate REST APIs:

- **System 1** (`/api/system1/transactions`) — Accepts transaction requests, performs validation (card number, PIN, amount, type), routes transactions to System 2 based on card number range (Visa = starts with `4`).
- **System 2** (`/api/system2/process`) — Validates card details, authenticates PIN using SHA-256 hashing, checks and updates card balance, and responds with success or failure.

System 1 calls System 2 internally for card validation and processing. System 2 is also exposed as a standalone REST API for direct access.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | H2 In-Memory |
| Frontend | React.js 18 |
| Security | JWT (authentication), SHA-256 (PIN hashing), AES (card storage encryption) |
| Build | Maven (backend), npm (frontend) |

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** and **npm 9+**

## Setup Instructions

### 1. Backend (Spring Boot)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

H2 Console available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:bankingdb`
- Username: `sa`
- Password: *(empty)*

### 2. Frontend (React.js)

```bash
cd frontend
npm install
npm start
```

The frontend starts on `http://localhost:3000` and proxies API calls to the backend.

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Super Admin | admin | admin123 |
| Customer | john | john123 |
| Customer | jane | jane123 |

## Sample Card Data

| Card Number | PIN | Balance | Holder | User |
|-------------|-----|---------|--------|------|
| 4111111111111111 | 1234 | $5,000.00 | John Doe | john |
| 4222222222222222 | 5678 | $3,000.00 | Jane Smith | jane |
| 4333333333333333 | 9999 | $10,000.00 | John Doe | john |
| 5111111111111111 | 1111 | $2,000.00 | Test User | john |

> Cards starting with `4` (Visa range) are supported. Cards starting with `5` will be declined with "Card range not supported".

## API Endpoints

### Authentication

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

### System 1 — Transaction Routing API

#### Create Transaction (Withdrawal)
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "cardNumber": "4111111111111111",
    "pin": "1234",
    "amount": 500.00,
    "type": "withdraw"
  }'
```

Success Response:
```json
{
  "success": true,
  "message": "Transaction approved",
  "transactionId": "1",
  "newBalance": 4500.0
}
```

#### Create Transaction (Top-Up)
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "cardNumber": "4111111111111111",
    "pin": "1234",
    "amount": 1000.00,
    "type": "topup"
  }'
```

#### Get Transactions
```bash
# Admin sees all transactions
curl http://localhost:8080/api/system1/transactions \
  -H "Authorization: Bearer <admin-token>"

# Customer sees own transactions only
curl http://localhost:8080/api/system1/transactions \
  -H "Authorization: Bearer <customer-token>"
```

### System 2 — Card Validation & Processing API

#### Process Transaction Directly
```bash
curl -X POST http://localhost:8080/api/system2/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "cardNumber": "4111111111111111",
    "pin": "1234",
    "amount": 500.00,
    "type": "withdraw"
  }'
```

> Note: System 2 is called internally by System 1, but is also exposed as a separate REST API for direct validation/processing access.

### Card Balance

```bash
curl http://localhost:8080/api/cards/balance \
  -H "Authorization: Bearer <token>"
```

## Error Scenarios

### Invalid Card
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"cardNumber": "4999999999999999", "pin": "1234", "amount": 100, "type": "withdraw"}'
```
Response: `{"success": false, "message": "Invalid card"}`

### Invalid PIN
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"cardNumber": "4111111111111111", "pin": "0000", "amount": 100, "type": "withdraw"}'
```
Response: `{"success": false, "message": "Invalid PIN"}`

### Insufficient Balance
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"cardNumber": "4111111111111111", "pin": "1234", "amount": 99999, "type": "withdraw"}'
```
Response: `{"success": false, "message": "Insufficient balance"}`

### Card Range Not Supported
```bash
curl -X POST http://localhost:8080/api/system1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"cardNumber": "5111111111111111", "pin": "1111", "amount": 100, "type": "withdraw"}'
```
Response: `{"success": false, "message": "Card range not supported"}`

## UI Access

1. Open `http://localhost:3000` in your browser
2. Sign in with demo credentials
3. **Super Admin** dashboard shows all transactions across the system with statistics
4. **Customer** dashboard shows own balance, cards, transaction history, and supports top-ups/withdrawals

## Security Features

- **PIN Hashing**: SHA-256 hashing for PIN storage and verification. Plain-text PINs are never stored or logged.
- **Card Encryption**: AES encryption for card numbers stored in the database. Card numbers are masked (`****-****-****-XXXX`) in API responses.
- **JWT Authentication**: Token-based authentication with role-based access control (ADMIN, CUSTOMER).
- **Role-Based Access**: Super Admin sees all transactions; Customers see only their own data.

## Running Tests

```bash
cd backend
mvn test
```

Test cases cover:
- Successful withdrawal with valid card/PIN
- Successful top-up with valid card/PIN
- Decline for invalid card number
- Decline for invalid PIN
- Decline for insufficient balance
- Decline for unsupported card range (non-Visa)
- Input validation (missing fields, negative/zero amount, invalid type)
- PIN hashing and verification
- Card encryption and decryption
- Super Admin sees all transactions
- Customer sees own transactions only
- Successful and failed login

## Project Structure

```
banking-transaction-poc/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/banking/poc/
│       ├── BankingTransactionPocApplication.java
│       ├── config/
│       │   ├── DataInitializer.java        # Seeds sample data
│       │   └── SecurityConfig.java         # Spring Security + CORS
│       ├── controller/
│       │   ├── AuthController.java         # /api/auth/**
│       │   ├── CardController.java         # /api/cards/**
│       │   ├── System1Controller.java      # /api/system1/transactions (Routing)
│       │   └── System2Controller.java      # /api/system2/process (Validation/Processing)
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── TransactionRequest.java
│       │   └── TransactionResponse.java
│       ├── model/
│       │   ├── Card.java
│       │   ├── Transaction.java
│       │   └── User.java
│       ├── repository/
│       │   ├── CardRepository.java
│       │   ├── TransactionRepository.java
│       │   └── UserRepository.java
│       ├── security/
│       │   ├── CardEncryptor.java          # AES encryption for card storage
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtUtil.java
│       │   └── PinHasher.java              # SHA-256 PIN hashing
│       └── service/
│           ├── AuthService.java
│           ├── CardService.java
│           ├── System1Service.java         # Validation + Routing
│           ├── System2Service.java         # Card auth + Balance processing
│           └── TransactionService.java
├── frontend/
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── App.js
│       ├── App.css
│       ├── index.js
│       ├── pages/
│       │   ├── LoginPage.js
│       │   ├── AdminDashboard.js
│       │   └── CustomerDashboard.js
│       └── services/
│           └── api.js
└── README.md
```
