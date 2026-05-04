SwiftPay — Transaction Gateway Service
Overview
The Transaction Gateway is the entry point of the SwiftPay platform. It handles all incoming payment requests, enforces idempotency using Redis, persists transactions to PostgreSQL, and publishes events to Kafka for downstream processing by the Ledger Service.

Architecture

Client
│
▼
POST /v1/payments
│
├──▶ Redis (Idempotency Check) ──▶ 409 Conflict if duplicate
│
├──▶ PostgreSQL (Save as PENDING)
│
└──▶ Kafka Topic: payment-initiated ──▶ Ledger Service


Tech Stack
Technology	           Version	                       Purpose
Java	                   21	                         Core language
Spring Boot	            3.5.14	                     Application framework
PostgreSQL	            15	                         Transaction storage
Apache Kafka	          3.7	                         Event streaming
Redis	                  7.0	                         Idempotency & caching
Swagger/OpenAPI	        3.0	                         API documentation
Docker	               Latest	                       Containerization
GitHub Actions	         -	                         CI/CD pipeline

Features
Idempotency — Redis-based duplicate prevention with 24-hour TTL
Persistence — Transactions saved as PENDING in PostgreSQL
Event-Driven — Publishes PaymentInitiated events to Kafka
API Docs — Fully documented with Swagger/OpenAPI
Health Check — Spring Actuator health endpoint
Containerized — Docker-ready with docker-compose
CI/CD — GitHub Actions pipeline for build, test & Docker image

API Endpoints
Method	Endpoint	Description	Status Codes
POST	/v1/payments	Initiate a payment	202, 409
GET	/actuator/health	Service health check	200

Request & Response
Initiate Payment
Request:
json
POST /v1/payments
Content-Type: application/json
{
"senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
"receiverId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
"amount": 500.00,
"currency": "INR",
"transactionId": "txn-unique-001"
}

Success Response (202 Accepted):
json
{
"id": "abc12345-1234-1234-1234-abcdef123456",
"senderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
"receiverId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
"amount": 500.00,
"currency": "INR",
"status": "PENDING",
"createdAt": "2026-05-03T10:00:00"
}

Duplicate Request (409 Conflict):
json
{
"error": "Duplicate transaction!"
}


Getting Started
Prerequisites
Java 21
Maven 3.8+
Docker Desktop
1. Clone the repository
```bash
git clone https://github.com/syedshareena/swiftpay.git
cd swiftpay
```
2. Start infrastructure (PostgreSQL + Kafka + Redis)
```bash
docker-compose up -d
```
3. Run the service
```bash
mvn spring-boot:run
```
4. Access Swagger UI

http://localhost:8080/swagger-ui.html

5. Check Health

http://localhost:8080/actuator/health


Docker
Build Docker image
```bash
mvn clean package -DskipTests
docker build -t swiftpay-gateway .
```
Run with Docker
```bash
docker run -p 8080:8080 swiftpay-gateway
```

Project Structure

transaction-gateway/
├── src/
│   └── main/
│       ├── java/com/swiftpay/transaction_gateway/
│       │   ├── config/          # Kafka & Redis configuration
│       │   ├── controller/      # REST API controllers
│       │   │   └── PaymentController.java
│       │   ├── dto/             # Data Transfer Objects
│       │   │   └── PaymentRequest.java
│       │   ├── model/           # JPA Entities
│       │   │   └── Transaction.java
│       │   ├── repository/      # Spring Data JPA repositories
│       │   │   └── TransactionRepository.java
│       │   └── service/         # Business logic
│       │       └── PaymentService.java
│       └── resources/
│           └── application.properties
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI/CD
├── Dockerfile
├── docker-compose.yml
└── pom.xml


CI/CD Pipeline
GitHub Actions workflow automatically:
Compiles Java code
Runs unit & integration tests
Builds Docker image

Configuration
Property	Value
Server Port	8080
Database	PostgreSQL (swiftpay)
Kafka Topic	payment-initiated
Redis TTL	24 hours
Testing & Verification

Health Check
http://localhost:8080/actuator/health

Swagger UI
http://localhost:8080/swagger-ui.html

Payment Success
```json

{

"transactionId": "txn-screen-001",

"senderId": "550e8400-e29b-41d4-a716-446655440000",

"receiverId": "550e8400-e29b-41d4-a716-446655440001",

"amount": 100.00,

"currency": "USD"

}

```

Duplicate Payment
Send same request again → Expected: 409 Conflict

Insufficient Funds
Amount: 999999.00 → Expected: FAILED

Load Testing
k6 run load-test.js

Database Verification
docker exec -it swiftpay-postgres psql -U postgres -d swiftpay -c "SELECT id, status, amount FROM transactions;"

Author
Shareena Syed  
Java Full Stack Developer  
Java 21 | Spring Boot | Kafka | Redis | Docker
