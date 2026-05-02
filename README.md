# 🛒 Distributed E-Commerce Platform
## Production-Grade Microservices with Java 21 + Spring Boot 3 + Spring Cloud

---

## 🏗️ System Architecture

```
═══════════════════════════════════════════════════════════════════════
                    EXTERNAL CLIENTS
         ┌────────────┐     ┌────────────┐     ┌────────────┐
         │  Web App   │     │ Mobile App │     │ Third Party│
         │  (React)   │     │ (iOS/Andr) │     │    APIs    │
         └─────┬──────┘     └─────┬──────┘     └─────┬──────┘
               │                  │                   │
               └──────────────────┼───────────────────┘
                                  │  HTTPS
                                  ▼
═══════════════════════════════════════════════════════════════════════
                         API GATEWAY  :8080
          ┌─────────────────────────────────────────────────┐
          │  Spring Cloud Gateway                           │
          │  ├── JWT Authentication Filter                  │
          │  ├── Rate Limiting (Redis Token Bucket)         │
          │  ├── Circuit Breaker (Resilience4j)             │
          │  ├── Load Balancing (Ribbon/Eureka)             │
          │  └── Request/Response Logging                   │
          └──────────────────┬──────────────────────────────┘
                             │
═══════════════════════════════════════════════════════════════════════
                    SERVICE DISCOVERY (Eureka) :8761
                    CONFIG SERVER              :8888
═══════════════════════════════════════════════════════════════════════
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
   │  USER SVC   │   │ PRODUCT SVC │   │  CART SVC   │
   │    :8081    │   │    :8082    │   │    :8083    │
   │  MySQL      │   │  MySQL      │   │  Redis      │
   │  JWT/OAuth2 │   │  Elastic    │   │  Feign→Prod │
   └─────────────┘   └─────────────┘   └─────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
   │  ORDER SVC  │   │ PAYMENT SVC │   │INVENTORY SVC│
   │    :8084    │   │    :8085    │   │    :8086    │
   │  MySQL      │   │  MySQL      │   │  MySQL      │
   │  Saga Orch  │   │  Idempotent │   │  Opt. Lock  │
   └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
═══════════════════════════════════════════════════════════════════════
                     Apache Kafka (Event Bus)
         Topics: order-events, inventory-events, payment-events,
                 payment-commands, inventory-commands,
                 notification-events, user-events, inventory-alerts
═══════════════════════════════════════════════════════════════════════
                             │
                             ▼
                   ┌─────────────────┐
                   │NOTIFICATION SVC │
                   │     :8087       │
                   │  MongoDB (logs) │
                   │  Email + SMS    │
                   └─────────────────┘

═══════════════════════════════════════════════════════════════════════
                     INFRASTRUCTURE LAYER
  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐
  │  MySQL   │  │  Redis   │  │ Elastic  │  │ MongoDB  │  │ Kafka  │
  │  :3306   │  │  :6379   │  │  :9200   │  │  :27017  │  │  :9092 │
  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └────────┘
═══════════════════════════════════════════════════════════════════════

SAGA FLOW (Distributed Transaction):
  OrderService ──ORDER_CREATED──▶ InventoryService
  InventoryService ──INVENTORY_RESERVED──▶ PaymentService
  PaymentService ──PAYMENT_COMPLETED──▶ OrderService (CONFIRMED)
  
  COMPENSATION:
  PaymentService ──PAYMENT_FAILED──▶ InventoryService (RELEASE)
                                  ──▶ OrderService (CANCELLED)
```

---

## 📁 Project Structure

```
ecommerce-platform/
├── pom.xml                          ← Parent POM (Java 21, Spring Boot 3.2.3)
├── docker-compose.yml               ← Full local stack
├── ECommerce-Platform.postman_collection.json
├── infra/
│   └── mysql/init.sql               ← DB initialization + seed data
├── k8s/
│   └── base/
│       ├── namespace.yaml
│       ├── configmap.yaml
│       ├── infrastructure.yaml      ← MySQL, Redis, Kafka StatefulSets
│       ├── services.yaml            ← All service Deployments + HPAs
│       └── user-service.yaml        ← User service with HPA
├── ci-cd/
│   └── .github-actions-ci-cd.yml   ← GitHub Actions pipeline
├── service-discovery/               ← Eureka Server          :8761
├── config-server/                   ← Centralized Config     :8888
├── api-gateway/                     ← Spring Cloud Gateway   :8080
├── user-service/                    ← Auth + Users           :8081
├── product-service/                 ← Catalog + Search       :8082
├── cart-service/                    ← Redis Cart             :8083
├── order-service/                   ← Orders + Saga          :8084
├── payment-service/                 ← Payments               :8085
├── inventory-service/               ← Stock Management       :8086
└── notification-service/            ← Kafka Consumer         :8087
```

---

## 🚀 Quick Start — Run Locally with Docker

### Prerequisites
- Docker Desktop 4.x+ with 8GB+ RAM allocated
- Docker Compose 2.x
- Java 21 (for local development without Docker)
- Maven 3.9+

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-org/ecommerce-platform.git
cd ecommerce-platform
```

### Step 2: Start Infrastructure Only (fast local dev)

```bash
# Start only databases and messaging
docker-compose up -d mysql redis kafka zookeeper elasticsearch mongodb

# Wait for all to be healthy
docker-compose ps
```

### Step 3: Start All Services

```bash
# Build all services and start everything
docker-compose up -d --build

# Follow logs
docker-compose logs -f

# Check health of all services
curl http://localhost:8761   # Eureka Dashboard
curl http://localhost:8080/actuator/health  # API Gateway
```

### Step 4: Import Postman Collection

1. Open Postman
2. Import `ECommerce-Platform.postman_collection.json`
3. Run **Register User** → **Login** → the access token auto-saves
4. All subsequent calls use `{{accessToken}}` automatically

---

## 🔌 Service Endpoints

| Service             | Port  | Swagger UI                              |
|---------------------|-------|-----------------------------------------|
| API Gateway         | 8080  | http://localhost:8080/swagger-ui.html   |
| Service Discovery   | 8761  | http://localhost:8761 (Eureka Dashboard)|
| User Service        | 8081  | http://localhost:8081/swagger-ui.html   |
| Product Service     | 8082  | http://localhost:8082/swagger-ui.html   |
| Cart Service        | 8083  | http://localhost:8083/swagger-ui.html   |
| Order Service       | 8084  | http://localhost:8084/swagger-ui.html   |
| Payment Service     | 8085  | http://localhost:8085/swagger-ui.html   |
| Inventory Service   | 8086  | http://localhost:8086/swagger-ui.html   |
| Notification Svc    | 8087  | http://localhost:8087/swagger-ui.html   |
| Kafka UI            | 9000  | http://localhost:9000                   |
| Kibana              | 5601  | http://localhost:5601                   |
| Elasticsearch       | 9200  | http://localhost:9200                   |

---

## 🔐 Security Architecture

```
1. User calls POST /api/v1/auth/login
2. API Gateway lets it through (open endpoint)
3. User Service authenticates and returns:
   - accessToken  (JWT, 24h expiry)
   - refreshToken (UUID, 7d expiry, stored in DB)
4. All subsequent requests: Authorization: Bearer <accessToken>
5. API Gateway validates JWT signature, extracts userId + role
6. Gateway injects X-User-Id and X-User-Role headers
7. Downstream services trust these headers (no re-validation)
8. accessToken expires → call /auth/refresh-token with refreshToken
9. Refresh tokens are rotated on each use (rotation strategy)
10. Logout revokes the refreshToken in DB
```

---

## 📨 Kafka Topics

| Topic                | Producer           | Consumer(s)              |
|----------------------|--------------------|--------------------------|
| `order-events`       | Order Service      | Inventory Service        |
| `inventory-events`   | Inventory Service  | Order Service (saga)     |
| `payment-commands`   | Order Service      | Payment Service          |
| `payment-events`     | Payment Service    | Order Service (saga)     |
| `inventory-commands` | Order Service      | Inventory Service        |
| `notification-events`| Order, Payment Svc | Notification Service     |
| `user-events`        | User Service       | Notification Service     |
| `inventory-alerts`   | Inventory Service  | Notification Service     |
| `product-events`     | Product Service    | (Analytics, future use)  |

---

## ⚡ Saga Flow — Place Order

```
1.  User POST /api/v1/orders
2.  OrderService creates Order (PENDING)
3.  OrderService publishes ORDER_CREATED to Kafka
4.  InventoryService receives ORDER_CREATED
5.  InventoryService reserves stock (optimistic locking)
6.  InventoryService publishes INVENTORY_RESERVED
7.  OrderService receives INVENTORY_RESERVED → updates to PROCESSING
8.  OrderService publishes PROCESS_PAYMENT command
9.  PaymentService receives command → calls Mock Gateway
    ├── SUCCESS: publishes PAYMENT_COMPLETED
    └── FAILURE: publishes PAYMENT_FAILED
10. OrderService receives PAYMENT_COMPLETED → CONFIRMED
    └── Or PAYMENT_FAILED → compensate → CANCELLED
11. NotificationService receives ORDER_CONFIRMED/ORDER_CANCELLED
12. Email + SMS sent to user
```

---

## 🛡️ Circuit Breaker Configuration

```yaml
# Resilience4j defaults per service
slidingWindowSize: 10 calls
failureRateThreshold: 50%
waitDurationInOpenState: 10s
permittedCallsInHalfOpenState: 3
# States: CLOSED → OPEN → HALF_OPEN → CLOSED
```

---

## 🗄️ Database Design

| Service      | Database     | Technology  | Notes                        |
|--------------|--------------|-------------|------------------------------|
| User         | user_db      | MySQL 8     | Roles, refresh tokens         |
| Product      | product_db   | MySQL 8     | Categories, tags, images      |
| Product      | —            | Elasticsearch| Full-text search index       |
| Cart         | —            | Redis       | TTL=7days, user-keyed hash    |
| Order        | order_db     | MySQL 8     | Order items, status history   |
| Payment      | payment_db   | MySQL 8     | Idempotency key, txn ledger   |
| Inventory    | inventory_db | MySQL 8     | Optimistic locking (version)  |
| Notification | notification_db | MongoDB  | Notification logs             |

---

## 🐳 Deploy to Kubernetes

```bash
# 1. Build and push images
docker-compose build
docker tag ecommerce/user-service:latest ghcr.io/your-org/ecommerce/user-service:1.0.0
docker push ghcr.io/your-org/ecommerce/user-service:1.0.0

# 2. Create namespace and apply configs
kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml

# 3. Deploy infrastructure (MySQL, Redis, Kafka)
kubectl apply -f k8s/base/infrastructure.yaml

# 4. Wait for infrastructure
kubectl rollout status statefulset/mysql -n ecommerce

# 5. Deploy microservices
kubectl apply -f k8s/base/services.yaml
kubectl apply -f k8s/base/user-service.yaml

# 6. Check pods
kubectl get pods -n ecommerce

# 7. Port-forward gateway for testing
kubectl port-forward svc/api-gateway 8080:80 -n ecommerce
```

---

## 🧪 Running Tests

```bash
# All service tests
mvn test -pl user-service -Dspring.profiles.active=test
mvn test -pl product-service -Dspring.profiles.active=test
mvn test -pl order-service -Dspring.profiles.active=test

# Integration tests (requires Docker for Testcontainers)
mvn verify -pl user-service

# Coverage report
mvn jacoco:report -pl user-service
open user-service/target/site/jacoco/index.html
```

---

## 📊 Monitoring

```bash
# Health of all services
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Metrics (Prometheus format)
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/prometheus

# Circuit breakers status
curl http://localhost:8080/actuator/circuitbreakers
```

---

## 🔧 Environment Variables Reference

| Variable                    | Default                          | Description                  |
|-----------------------------|----------------------------------|------------------------------|
| `EUREKA_HOST`               | localhost                        | Eureka server hostname        |
| `MYSQL_HOST`                | localhost                        | MySQL hostname                |
| `MYSQL_USER`                | root                             | MySQL username                |
| `MYSQL_PASSWORD`            | rootpassword                     | MySQL password                |
| `REDIS_HOST`                | localhost                        | Redis hostname                |
| `KAFKA_BOOTSTRAP_SERVERS`   | localhost:9092                   | Kafka brokers                 |
| `ELASTICSEARCH_HOST`        | localhost                        | Elasticsearch hostname        |
| `MONGODB_HOST`              | localhost                        | MongoDB hostname              |
| `JWT_SECRET`                | (base64 key)                     | JWT signing key (256-bit min) |
| `JWT_EXPIRATION`            | 86400000                         | Access token TTL (ms)         |
| `JWT_REFRESH_EXPIRATION`    | 604800000                        | Refresh token TTL (ms)        |

---

## 🏆 Design Patterns Used

| Pattern              | Where Used                                               |
|----------------------|----------------------------------------------------------|
| Saga (Choreography)  | Order → Inventory → Payment distributed transaction      |
| Circuit Breaker      | All inter-service calls via Resilience4j                 |
| API Gateway          | Single entry point, auth, rate limiting, routing         |
| Service Registry     | Eureka for dynamic service discovery                     |
| Event-Driven         | Kafka for async communication between services           |
| CQRS (partial)       | Elasticsearch for reads, MySQL for writes (Product)      |
| Idempotency          | Payment service idempotency key header                   |
| Optimistic Locking   | Inventory `@Version` field for concurrent stock updates  |
| Repository Pattern   | JPA repositories for data access abstraction             |
| DTO/Mapper           | Clean separation of API and domain models                |
| Outbox (simulated)   | Kafka publishing in same transaction as DB write         |
