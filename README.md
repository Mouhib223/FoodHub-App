# FoodHub - Smart Food Delivery Platform

A complete **microservices-based food delivery platform** with comprehensive service discovery, API gateway, authentication, asynchronous messaging, ML recommendations, and modern cloud-native architecture.

## 🏗️ Project Architecture

**Tech Stack:**
- **Backend:** Java 21 (Spring Boot 3.x), NestJS (Node.js)
- **Frontend:** Angular 18+ with NgRx state management
- **Databases:** MySQL, MongoDB
- **Message Queue:** RabbitMQ with event-driven architecture
- **Service Discovery:** Eureka Server
- **API Gateway:** Spring Cloud Gateway
- **Authentication:** Keycloak (OAuth2/OpenID Connect)
- **Monitoring:** Prometheus + Grafana
- **Containerization:** Docker & Docker Compose
- **CI/CD:** GitHub Actions
- **Load Balancing:** Reverse proxy via Nginx

---

## 📋 Services Overview

### Core Microservices

| Service | Port | Tech | Purpose |
|---------|------|------|---------|
| **Eureka Server** | 8761 | Spring Boot | Service registry & discovery |
| **Config Server** | 8888 | Spring Boot | Centralized configuration management |
| **API Gateway** | 8080 | Spring Cloud Gateway | Single entry point, auth, routing |
| **User Service** | 8081 | Spring Boot + MySQL | User profiles & authentication |
| **Restaurant Service** | 8082 | Spring Boot + MySQL | Restaurant & menu management |
| **Order Service** | 8083 | Spring Boot + MySQL | Order processing & tracking |
| **Delivery Service** | 8084 | Spring Boot + MySQL | Delivery management, driver assignments |
| **Recommendation Service** | 3000 | NestJS + MongoDB | ML-powered recommendations & analytics |
| **Frontend** | 4200 | Angular + Nginx | Web UI (port 80 in Docker) |

### Infrastructure Services

| Service | Port | Purpose |
|---------|------|---------|
| **Keycloak** | 8180 | OAuth2/OpenID Connect authentication |
| **RabbitMQ** | 5672 / 15672 | Message broker & management UI |
| **MySQL** | 3306–3309 | Relational databases (users, restaurants, orders, delivery) |
| **MongoDB** | 27017 | NoSQL for recommendations |
| **Prometheus** | 9090 | Metrics collection |
| **Grafana** | 3001 | Monitoring dashboard |

---

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (recommended)
- **Java 21** (for local Maven builds)
- **Node.js 20+** & npm (for NestJS & Angular)
- **Git**

### Option 1: Docker Compose (Recommended)

```bash
# Clone & navigate
git clone <your-repo-url>
cd Foodhub

# Build all services
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  cd $svc && mvn clean package -DskipTests && cd ..
done

cd recommendation-service && npm ci && npm run build && cd ..
cd frontend/foodhub-frontend && npm ci && npm run build -- --configuration=production && cd ../..

# Start everything
cd docker
docker-compose up -d

# Verify all services
docker-compose ps
```

### Option 2: Local Development (Without Docker)

#### Step 1: Start Infrastructure

```bash
# RabbitMQ (message broker)
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management

# MongoDB (for recommendations)
docker run -d -p 27017:27017 mongo:7.0

# MySQL (User Service)
docker run -d -p 3306:3306 --name mysql-users \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=foodhub_users mysql:8.0

# MySQL (Restaurant Service)
docker run -d -p 3307:3307 --name mysql-restaurants \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=foodhub_restaurants mysql:8.0

# MySQL (Order Service)
docker run -d -p 3308:3308 --name mysql-orders \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=foodhub_orders mysql:8.0

# MySQL (Delivery Service)
docker run -d -p 3309:3309 --name mysql-delivery \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=foodhub_delivery mysql:8.0

# Keycloak (Authentication)
docker run -d -p 8180:8180 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin123 \
  quay.io/keycloak/keycloak:24.0.0 start-dev

# Prometheus (Metrics)
docker run -d -p 9090:9090 -v $(pwd)/docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus

# Grafana (Dashboards)
docker run -d -p 3001:3000 grafana/grafana:latest
```

#### Step 2: Start Java/Spring Boot Services

```bash
# Terminal 1: Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2: Config Server
cd config-server && mvn spring-boot:run

# Terminal 3: API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4: User Service
cd user-service && mvn spring-boot:run

# Terminal 5: Restaurant Service
cd restaurant-service && mvn spring-boot:run

# Terminal 6: Order Service
cd order-service && mvn spring-boot:run

# Terminal 7: Delivery Service
cd delivery-service && mvn spring-boot:run
```

#### Step 3: Start Node.js & Frontend

```bash
# Terminal 8: Recommendation Service (NestJS)
cd recommendation-service
npm install
npm run start:dev

# Terminal 9: Angular Frontend
cd frontend/foodhub-frontend
npm install
ng serve
```

---

## 🌐 Access Points

| Service | URL |
|---------|-----|
| **Angular Frontend** | http://localhost:4200 |
| **API Gateway** | http://localhost:8080 |
| **Swagger API Docs** | http://localhost:8080/swagger-ui.html |
| **Eureka Dashboard** | http://localhost:8761 |
| **Keycloak Console** | http://localhost:8180/admin (admin/admin123) |
| **RabbitMQ Management** | http://localhost:15672 (guest/guest) |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3001 (admin/admin) |

---

## 🔐 Authentication

### Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| Keycloak Admin | admin | admin123 |
| RabbitMQ | guest | guest |
| Grafana | admin | admin |
| MySQL | root | root |

### API Authentication Flow

1. **Get Token from Keycloak:**
   ```bash
   curl -X POST http://localhost:8180/realms/foodhub/protocol/openid-connect/token \
     -d "grant_type=password&client_id=foodhub-frontend&username=customer1&password=password"
   ```

2. **Use Token in API Requests:**
   ```bash
   curl -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
     http://localhost:8080/api/v1/restaurants
   ```

---

## 🧪 Testing with Postman

A comprehensive Postman collection is provided with 12 pre-configured scenarios:

1. **Auth** → Get JWT token from Keycloak
2. **Create User** → Register new user
3. **Create Restaurant** → Add restaurant
4. **Add Menu Item** → Define restaurant menu
5. **Add to Cart** → Order items
6. **Place Order** → Create order (triggers RabbitMQ event)
7. **Confirm Order** → Accept order (triggers delivery creation)
8. **Get Delivery** → Track delivery
9. **Complete Delivery** → Mark as delivered
10. **Rate Restaurant** → Submit rating
11. **Get Recommendations** → ML-powered suggestions
12. **Swagger UI** → View all API documentation

**Import:** `postman/FoodHub.postman_collection.json`

---

## 🔄 Event-Driven Architecture (RabbitMQ)

Services communicate asynchronously via event queue:

- **OrderCreated** → Order Service → Delivery Service (create delivery)
- **OrderAccepted** → Order Service → Delivery Service (assign driver)
- **OrderCancelled** → Order Service → RabbitMQ
- **DeliveryCompleted** → Delivery Service → Order Service (mark order complete)
- **RestaurantRated** → Order Service → Restaurant Service (update ratings)

---

## 📊 Service Communication Patterns

### Synchronous (Feign)
- **Order Service** → Restaurant Service (verify menu items)
- **Order Service** → User Service (get user address)
- **Delivery Service** → Order Service (get order details)

### Asynchronous (RabbitMQ)
- Order workflow events
- Delivery updates
- Rating submissions
- Analytics tracking

---

## 🛠️ Development

### Build All Services

```bash
cd Foodhub

# Maven services
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  cd $svc && mvn clean package -DskipTests && cd ..
done

# NestJS
cd recommendation-service && npm ci && npm run build && cd ..

# Angular
cd frontend/foodhub-frontend && npm ci && npm run build -- --configuration=production && cd ../..
```

### Run Tests

```bash
# Java services
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  cd $svc && mvn test && cd ..
done

# NestJS
cd recommendation-service && npm test && cd ..

# Angular
cd frontend/foodhub-frontend && npm run test && cd ../..
```

### Docker Build & Push

```bash
# All services are automatically built and pushed via GitHub Actions on main branch
# Manual push (requires DOCKERHUB credentials):

docker login
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  docker build -t <username>/foodhub-$svc:latest ./$svc
  docker push <username>/foodhub-$svc:latest
done

docker build -t <username>/foodhub-recommendation-service:latest ./recommendation-service
docker push <username>/foodhub-recommendation-service:latest

docker build -t <username>/foodhub-frontend:latest ./frontend/foodhub-frontend
docker push <username>/foodhub-frontend:latest
```

---

## 📈 Monitoring & Observability

### Prometheus Metrics

All Spring Boot services expose metrics at:
```
http://<service>:<port>/actuator/prometheus
```

### Grafana Dashboards

Pre-configured dashboards for:
- Service request rates
- Error rates & response times
- JVM memory & CPU usage
- RabbitMQ queue sizes

### Logs

Centralized logging can be added via **ELK Stack** or **Loki**.

---

## 🐳 Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f <service-name>

# Stop all services
docker-compose down

# Remove volumes (reset databases)
docker-compose down -v

# Rebuild and restart
docker-compose up -d --build
```

---

## 🔄 CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci-cd.yml`):

1. **On push to main/develop or PR to main:**
   - Build all Java services with Maven
   - Build NestJS & Angular
   - Run tests

2. **On push to main (after build succeeds):**
   - Login to DockerHub
   - Build Docker images for all services
   - Push to DockerHub registry
   - (Optional: Deploy to Kubernetes or cloud platform)

**Setup Required:**
- Add `DOCKERHUB_USERNAME` & `DOCKERHUB_TOKEN` as GitHub secrets

---

## 📦 Project Structure

```
Foodhub/
├── eureka-server/               # Service registry
├── config-server/               # Centralized config
├── api-gateway/                 # Single entry point
├── user-service/                # User management
├── restaurant-service/          # Restaurant & menu
├── order-service/               # Order processing
├── delivery-service/            # Delivery management
├── recommendation-service/      # NestJS + ML
├── frontend/foodhub-frontend/   # Angular UI
├── docker/                       # Docker Compose & configs
│   ├── docker-compose.yml
│   ├── prometheus/
│   ├── grafana/
│   └── keycloak/
├── postman/                      # Postman collection
├── .github/workflows/            # CI/CD pipelines
├── README.md                     # This file
└── ARCHITECTURE.md               # Detailed architecture
```

---

## 🚨 Troubleshooting

### Issue: Services won't start
- **Check Eureka:** http://localhost:8761
- **Check port conflicts:** `netstat -an | grep LISTEN`
- **Check logs:** `docker-compose logs <service-name>`

### Issue: Database connection errors
- Ensure MySQL containers are running: `docker ps | grep mysql`
- Verify connection strings in application.yml files

### Issue: API Gateway can't find services
- Wait 30s for Eureka registration
- Check service names in gateway routes match Eureka registrations

### Issue: RabbitMQ events not firing
- Check RabbitMQ is running: http://localhost:15672
- Verify queues are created (visit Management UI)
- Check service application properties have RabbitMQ host correct

---

## 📄 License

This project is proprietary. All rights reserved.

---

## 👥 Contributors

Developed as a comprehensive microservices learning platform.

**Last Updated:** June 6, 2026

