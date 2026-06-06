# FoodHub - Setup & Deployment Guide

## Prerequisites

### For Docker Compose (Recommended)
- Docker 24.0+
- Docker Compose 2.0+
- 8GB+ RAM available

### For Local Development
- Java 21 JDK (download from [oracle.com](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK)
- Maven 3.8+
- Node.js 20+ & npm
- MySQL 8.0 (or use Docker for databases)
- Git

---

## Installation & Verification

### Java & Maven
```bash
# Verify Java (should be 21)
java -version

# Verify Maven
mvn --version

# Add to PATH if not found (Windows)
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
setx MAVEN_HOME "C:\path\to\maven"
```

### Node.js
```bash
# Verify Node & npm
node --version    # v20.x or later
npm --version     # 10.x or later

# If not installed, download from nodejs.org
```

### Git
```bash
# Verify Git
git --version

# Configure global settings
git config --global user.name "Your Name"
git config --global user.email "your@email.com"
```

---

## Setup Approach #1: Docker Compose (Recommended)

### Step 1: Clone Repository
```bash
git clone <your-repo-url>
cd Foodhub
```

### Step 2: Build All Services
This creates JAR files and Docker-ready builds.

```bash
# Build all Spring Boot services
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  cd $svc && mvn clean package -DskipTests && cd ..
done

# Build NestJS Recommendation Service
cd recommendation-service
npm ci
npm run build
cd ..

# Build Angular Frontend
cd frontend/foodhub-frontend
npm ci
npm run build -- --configuration=production
cd ../..
```

**Build Time:** ~10-15 minutes (first run, depends on internet speed)

### Step 3: Start Everything with Docker Compose
```bash
cd docker
docker-compose up -d

# Wait 30-60s for services to start
sleep 30

# Verify all services are running
docker-compose ps
```

**Expected Output (all RUNNING):**
```
NAME                      IMAGE                  STATUS
foodhub-eureka            food...               Up (healthy)
foodhub-config            food...               Up (healthy)
foodhub-api-gateway       food...               Up (healthy)
foodhub-user-service      food...               Up (healthy)
foodhub-restaurant-service food...              Up (healthy)
foodhub-order-service     food...               Up (healthy)
foodhub-delivery-service  food...               Up (healthy)
foodhub-recommendation    food...               Up (healthy)
foodhub-frontend          food...               Up (healthy)
rabbitmq                  rabbitmq:3.13-mgmt   Up
mysql-users               mysql:8.0             Up
mysql-restaurants         mysql:8.0             Up
mysql-orders              mysql:8.0             Up
mysql-delivery            mysql:8.0             Up
mongodb                   mongo:7.0             Up
keycloak                  keycloak:24.0.0       Up
prometheus                prom/prometheus       Up
grafana                   grafana               Up
```

### Step 4: Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:4200 | N/A |
| **API Gateway / Swagger** | http://localhost:8080/swagger-ui.html | N/A |
| **Eureka Dashboard** | http://localhost:8761 | N/A |
| **Keycloak Admin** | http://localhost:8180/admin | admin / admin123 |
| **RabbitMQ Management** | http://localhost:15672 | guest / guest |
| **Prometheus** | http://localhost:9090 | N/A |
| **Grafana** | http://localhost:3001 | admin / admin |

---

## Setup Approach #2: Local Development (Without Docker)

### Step 1: Start Infrastructure Services (via Docker)

Each service runs in its own Docker container for data persistence.

```bash
# RabbitMQ (Message Broker)
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.13-management

# MongoDB (NoSQL for recommendations)
docker run -d --name mongodb \
  -p 27017:27017 \
  mongo:7.0

# MySQL for User Service
docker run -d --name mysql-users \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=foodhub_users \
  mysql:8.0

# MySQL for Restaurant Service
docker run -d --name mysql-restaurants \
  -p 3307:3307 \
  -e MYSQL_PORT=3307 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=foodhub_restaurants \
  mysql:8.0 --port 3307

# MySQL for Order Service
docker run -d --name mysql-orders \
  -p 3308:3308 \
  -e MYSQL_PORT=3308 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=foodhub_orders \
  mysql:8.0 --port 3308

# MySQL for Delivery Service
docker run -d --name mysql-delivery \
  -p 3309:3309 \
  -e MYSQL_PORT=3309 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=foodhub_delivery \
  mysql:8.0 --port 3309

# Keycloak (Authentication & Authorization)
docker run -d --name keycloak \
  -p 8180:8180 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin123 \
  quay.io/keycloak/keycloak:24.0.0 start-dev

# Prometheus (Metrics)
docker run -d --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest

# Grafana (Dashboard)
docker run -d --name grafana \
  -p 3001:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  grafana/grafana:latest

# Wait for databases to initialize
sleep 20

# Verify all containers are running
docker ps
```

### Step 2: Start Java Microservices

Open 7 separate terminal windows/tabs, one for each service:

**Terminal 1: Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
# Expected: "Eureka server started" on port 8761
```

**Terminal 2: Config Server**
```bash
cd config-server
mvn spring-boot:run
# Expected: "Config Server started" on port 8888
```

**Terminal 3: API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
# Expected: "Gateway started" on port 8080
```

**Terminal 4: User Service**
```bash
cd user-service
mvn spring-boot:run
# Expected: "User Service started" on port 8081
```

**Terminal 5: Restaurant Service**
```bash
cd restaurant-service
mvn spring-boot:run
# Expected: "Restaurant Service started" on port 8082
```

**Terminal 6: Order Service**
```bash
cd order-service
mvn spring-boot:run
# Expected: "Order Service started" on port 8083
```

**Terminal 7: Delivery Service**
```bash
cd delivery-service
mvn spring-boot:run
# Expected: "Delivery Service started" on port 8084
```

### Step 3: Start Node.js Services

**Terminal 8: Recommendation Service (NestJS)**
```bash
cd recommendation-service
npm install
npm run start:dev
# Expected: "NestJS application listening on port 3000"
```

### Step 4: Start Frontend

**Terminal 9: Angular Frontend**
```bash
cd frontend/foodhub-frontend
npm install
ng serve
# Expected: "Application bundle generated successfully. 2134 ms" 
# Browse to http://localhost:4200
```

---

## Troubleshooting

### Issue: "Unable to connect to Eureka Server"

**Symptoms:** Services won't register, error logs show Eureka connection errors

**Solutions:**
1. Verify Eureka is running: http://localhost:8761/
2. Check service application.yml has correct Eureka URL: `http://localhost:8761/eureka/`
3. Wait 30s for registration (first time)
4. Check logs for java.net.ConnectException

```bash
# In the service terminal, look for:
# "Registering application X with Eureka with initial status UP"
# If not found, Eureka service is not accessible
```

### Issue: "Database connection refused"

**Symptoms:** Services crash on startup, error: "Connection refused: connect"

**Solutions:**
1. Verify MySQL containers are running:
   ```bash
   docker ps | grep mysql
   ```

2. Verify databases exist:
   ```bash
   docker exec mysql-users mysql -uroot -proot -e "SHOW DATABASES;"
   ```

3. Check connection string in application.yml:
   - User Service should connect to localhost:3306 (foodhub_users)
   - Restaurant Service should connect to localhost:3307 (foodhub_restaurants)
   - Order Service should connect to localhost:3308 (foodhub_orders)
   - Delivery Service should connect to localhost:3309 (foodhub_delivery)

### Issue: "Cannot find module 'rxjs'" (NestJS)

**Symptoms:** NestJS service fails to start

**Solution:**
```bash
cd recommendation-service
npm ci  # Clean install
npm run build
npm run start:dev
```

### Issue: "Port 3000 already in use"

**Symptoms:** NestJS fails to start

**Solution:**
```bash
# Find process using port 3000
netstat -ano | findstr :3000  # Windows
lsof -i :3000                  # macOS/Linux

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F         # Windows
kill -9 <PID>                  # macOS/Linux

# Or use a different port
NPM_PORT=3001 npm run start:dev
```

### Issue: "Angular compilation errors"

**Symptoms:** Compilation fails with TypeScript errors

**Solution:**
```bash
cd frontend/foodhub-frontend
npm ci
ng version  # Verify Angular CLI version
npm run build
# Fix any TypeScript errors shown in output
```

### Issue: "RabbitMQ queues not created"

**Symptoms:** Services run but messages aren't being sent/received

**Solution:**
1. Go to RabbitMQ Management UI: http://localhost:15672 (guest/guest)
2. Click "Queues and Streams" tab
3. You should see queues like:
   - order.created.queue
   - delivery.completed.queue
   - order.rated.queue

If queues don't exist, manually create them or restart services that create them.

---

## Stopping Services

### Docker Compose
```bash
# Stop all services (keep volumes)
docker-compose down

# Stop all services and remove volumes (database wipe)
docker-compose down -v

# Stop specific service
docker-compose stop <service-name>

# Restart specific service
docker-compose restart <service-name>

# View logs
docker-compose logs -f <service-name>
```

### Local Development
```bash
# Stop each service by pressing Ctrl+C in its terminal

# Stop all Docker infrastructure
docker stop $(docker ps -q)

# Remove stopped containers
docker container prune
```

---

## Running Tests

### Java Services
```bash
cd <service-name>
mvn test
```

### NestJS
```bash
cd recommendation-service
npm run test
npm run test:e2e  # End-to-end tests
```

### Angular
```bash
cd frontend/foodhub-frontend
npm run test
npm run test:watch  # Watch mode for development
```

---

## Building for Production

### Build All Services
```bash
# From repository root
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  cd $svc && mvn clean package -DskipTests -Pprod && cd ..
done

cd recommendation-service && npm ci && npm run build:prod && cd ..
cd frontend/foodhub-frontend && npm ci && npm run build:prod && cd ../..
```

### Create Production Docker Images
```bash
# Requires Docker buildx (multi-platform support)
docker login

# Build and push for production
for svc in eureka-server config-server api-gateway user-service restaurant-service order-service delivery-service; do
  docker buildx build --platform linux/amd64 -t <docker-username>/foodhub-$svc:1.0.0 --push ./$svc
done

docker buildx build --platform linux/amd64 -t <docker-username>/foodhub-recommendation:1.0.0 --push ./recommendation-service
docker buildx build --platform linux/amd64 -t <docker-username>/foodhub-frontend:1.0.0 --push ./frontend/foodhub-frontend
```

---

## Environment Configuration

### Local Development (.env)
```bash
# Communication ports
EUREKA_PORT=8761
CONFIG_PORT=8888
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
RESTAURANT_SERVICE_PORT=8082
ORDER_SERVICE_PORT=8083
DELIVERY_SERVICE_PORT=8084
RECOMMENDATION_PORT=3000

# Database
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=root
MONGODB_CONNECTION_STRING=mongodb://localhost:27017/foodhub_recommendations

# Message Queue
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Authentication
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=foodhub
KEYCLOAK_CLIENT_ID=foodhub-frontend
KEYCLOAK_CLIENT_SECRET=<your-secret>

# Monitoring
PROMETHEUS_PORT=9090
GRAFANA_PORT=3001
GRAFANA_PASSWORD=admin
```

### Production (.env.prod)
```bash
# Use Docker network names instead of localhost
MYSQL_HOST=mysql-users
RABBITMQ_HOST=rabbitmq
KEYCLOAK_URL=https://auth.yourdomain.com
MONGODB_CONNECTION_STRING=mongodb://mongodb:27017/foodhub_recommendations

# Production credentials (use secrets management)
MYSQL_ROOT_PASSWORD=<strong-password>
RABBITMQ_PASSWORD=<strong-password>
KEYCLOAK_CLIENT_SECRET=<secrets-from-vault>
```

---

## Health Checks

### Check Service Status
```bash
# Eureka dashboard
curl http://localhost:8761

# API Gateway health
curl http://localhost:8080/actuator/health

# Specific service health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Restaurant Service
```

### Check Database Connectivity
```bash
# MySQL (User Service)
docker exec mysql-users mysql -uroot -proot -e "SELECT 1"

# MongoDB
docker exec mongodb mongosh --eval "db.version()"

# RabbitMQ
curl -u guest:guest http://localhost:15672/api/aliveness-test/%2F
```

---

## Performance Tuning

### JVM Settings (for large deployments)
Edit `application.yml` in each service:

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10

spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### Database Indexing
Ensure all tables have proper indexes:
```sql
CREATE INDEX idx_user_id ON orders(user_id);
CREATE INDEX idx_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_delivery_order_id ON deliveries(order_id);
```

---

## Next Steps

1. **Test the Full Flow:** Use the Postman collection to test all scenarios
2. **Setup Monitoring:** Configure Grafana dashboards for your team
3. **Enable SSL/TLS:** Add certificates for HTTPS support
4. **Setup CI/CD:** Configure GitHub Actions secrets and deployments
5. **Scale Services:** Add service replicas in Docker Compose for load testing

---

**Last Updated:** June 6, 2026

