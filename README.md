# SwiftPay — Transaction Gateway Service

## Overview

The **Transaction Gateway** is the entry point of the SwiftPay platform. It handles all incoming payment requests, enforces idempotency using Redis, persists transactions to PostgreSQL, and publishes events to Kafka for downstream processing by the Ledger Service.

---

## Service Responsibilities

- Accept incoming payment requests
- Validate payment details
- Prevent duplicate transactions using Redis (24-hour TTL)
- Store transactions with **PENDING** status in PostgreSQL
- Publish **PaymentInitiated** events to Kafka
- Expose REST APIs using Spring Boot
- Provide API documentation through Swagger
- Support containerized deployment with Docker

---

## Architecture

```text
                    Client
                       │
             POST /v1/payments
                       │
      ┌──────────────────────────┐
      │ Transaction Gateway      │
      │      Spring Boot         │
      └──────────────────────────┘
          │                │
          │                │
    PostgreSQL          Redis
     (PENDING)      (Idempotency)
          │
          │
      Kafka Producer
          │
   payment-initiated
          │
      Ledger Service
```


## Payment Flow

1. Client sends a payment request.
2. Transaction Gateway validates the request.
3. Redis checks whether the transaction ID already exists.
4. If duplicate, returns **409 Conflict**.
5. Otherwise, stores the transaction in PostgreSQL with **PENDING** status.
6. Publishes a **PaymentInitiated** event to Kafka.
7. Ledger Service consumes the event and processes the payment.

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.5.14 | Application framework |
| PostgreSQL | 15 | Transaction storage |
| Apache Kafka | 3.7 | Event streaming |
| Redis | 7.0 | Idempotency & caching |
| Swagger/OpenAPI | 3.0 | API documentation |
| Docker | Latest | Containerization |
| GitHub Actions | - | CI/CD pipeline |

---

## Features

- Redis-based idempotency with 24-hour TTL
- Transaction persistence in PostgreSQL
- Event-driven architecture using Kafka
- Swagger/OpenAPI documentation
- Spring Boot Actuator health endpoint
- Docker & Docker Compose support
- GitHub Actions CI/CD pipeline
- High-throughput load testing using k6

---

## API Endpoints

| Method | Endpoint | Description | Status Codes |
|---------|----------|-------------|--------------|
| POST | `/v1/payments` | Initiate payment | 202, 409 |
| GET | `/actuator/health` | Health check | 200 |
| GET | `/swagger-ui.html` | Swagger UI | 200 |

---

# Request & Response

## Initiate Payment Request

```json
{
  "senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "receiverId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
  "amount": 500.00,
  "currency": "INR",
  "transactionId": "txn-unique-001"
}
```

---

## Success Response (202 Accepted)

```json
{
  "id": "abc12345-1234-1234-1234-abcdef123456",
  "senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "receiverId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
  "amount": 500.00,
  "currency": "INR",
  "status": "PENDING",
  "createdAt": "2026-05-03T10:00:00"
}
```

---

## Duplicate Request (409 Conflict)

```json
{
  "error": "Duplicate transaction!"
}
```

---

# Getting Started

## Prerequisites

- Java 21
- Maven 3.8+
- Docker Desktop

---

## Clone Repository

```bash
git clone https://github.com/syedshareena/swiftpay-transaction-gateway.git

cd swiftpay-transaction-gateway
```

---

## Start Infrastructure

```bash
docker-compose up -d
```

This starts:

- PostgreSQL
- Kafka
- Redis

---

## Run the Application

```bash
mvn spring-boot:run
```

---

## Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Health Check

```
http://localhost:8080/actuator/health
```

---

# Docker

## Build Docker Image

```bash
mvn clean package -DskipTests

docker build -t swiftpay-gateway .
```

---

## Run Docker Container

```bash
docker run -p 8080:8080 swiftpay-gateway
```

---

# Project Structure

```
transaction-gateway/
│
├── src/
│   └── main/
│       ├── java/com/swiftpay/transaction_gateway/
│       │   ├── config/          # Kafka & Redis configuration
│       │   ├── controller/      # REST Controllers
│       │   ├── dto/             # Request & Response DTOs
│       │   ├── model/           # JPA Entities
│       │   ├── repository/      # Database access layer
│       │   └── service/         # Business logic
│       │
│       └── resources/
│           └── application.properties
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── .github/workflows/ci.yml
```

---

# CI/CD Pipeline

GitHub Actions automatically:

- Compile Java code
- Run Unit & Integration Tests
- Build Docker Image

---

# Configuration

| Property | Value |
|-----------|-------|
| Server Port | 8080 |
| Database | PostgreSQL (swiftpay) |
| Kafka Topic | payment-initiated |
| Redis TTL | 24 Hours |

---

# Testing & Verification

### Health Check

```
http://localhost:8080/actuator/health
```

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### Test Scenarios

| Scenario | Expected Result |
|----------|-----------------|
| Successful Payment | PENDING |
| Duplicate Payment | 409 Conflict |
| Insufficient Funds | FAILED |

---

# Load Testing

Load testing was performed using **k6** to simulate high-throughput payment requests.

### Test Details

- Target Throughput: **250 TPS**
- Total Transactions: **~1,000,000**
- Endpoint Tested: **POST /v1/payments**
- Tool Used: **k6**

### Run

```bash
k6 run load-test.js
```

### Objectives

- Validate system performance
- Verify Redis idempotency
- Verify Kafka event publishing

---

# PCAP Capture

Network traffic was captured using **Wireshark** during load testing.

### Capture Details

- Interface: Loopback (localhost)
- Duration: Entire load test
- File: `load_test_capture.pcapng`

### Observed Traffic

- HTTP Requests
- Kafka Communication (Port 9092)
- Redis Communication
- Consumer Group Activity

---

# Design Decisions

### Why Redis?

Redis provides fast in-memory storage to prevent duplicate payment processing by storing transaction IDs with a 24-hour expiration.

### Why Kafka?

Kafka enables asynchronous communication between the Transaction Gateway and Ledger Service, improving scalability and decoupling the services.

### Why PostgreSQL?

PostgreSQL provides ACID-compliant transactions, making it ideal for financial applications that require data consistency.

---


# Related Repository

The payment processing logic is implemented in the **SwiftPay Ledger Service**.

Repository:
https://github.com/syedshareena/swiftpay-ledger

---

# Author

**Shareena Syed**

Java Full Stack Developer

Java 21 | Spring Boot | Apache Kafka | Redis | PostgreSQL | Docker