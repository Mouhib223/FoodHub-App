# FoodHub - Detailed Architecture Guide

## 🏛️ Microservices Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Client Layer                                 │
│  ┌─────────────────┬──────────────────────────────────────────────┐ │
│  │  Angular UI     │           Mobile/Web Browsers              │ │
│  │  (port 4200)    │              (Xamarin/React)              │ │
│  └────────┬────────┴──────────────────────────────────────────────┘ │
└───────────┼──────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Keycloak (Port 8180)                              │
│              OAuth2 / OpenID Connect Authentication                  │
└────────────────────────┬────────────────────────────────────────────┘
                         │ (JWT Tokens)
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│            API Gateway (Spring Cloud Gateway, Port 8080)            │
│                                                                       │
│  ├─ Auth Interceptor (validate JWT)                                 │
│  ├─ Request/Response Logging                                        │
│  ├─ Load Balancing & Routing                                        │
│  └─ Rate Limiting & Circuit Breaker                                │
│                                                                       │
│            Discovers services via Eureka Service Registry           │
└────────────────┬────────────────┬──────────────┬────────────────────┘
                 │                │              │
        ┌────────▼────┐  ┌────────▼────┐  ┌─────▼──────┐
        │ Load Balance │  │ Load Balance │  │ Load Balance
        │              │  │              │  │
        ▼              ▼  ▼              ▼  ▼
┌────────────────┐ ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
│  User Service  │ │Restaurant Srv  │ │  Order Service │ │ Delivery Srv   │
│  (Port 8081)   │ │  (Port 8082)   │ │  (Port 8083)   │ │  (Port 8084)   │
│                │ │                │ │                │ │                │
│  ┌──────────┐  │ │  ┌──────────┐  │ │  ┌──────────┐  │ │  ┌──────────┐  │
│  │ Endpoints│  │ │  │ Endpoints│  │ │  │ Endpoints│  │ │  │ Endpoints│  │
│  └──────────┘  │ │  └──────────┘  │ │  └──────────┘  │ │  └──────────┘  │
│                │ │                │ │                │ │                │
│  ┌──────────┐  │ │  ┌──────────┐  │ │  ┌──────────┐  │ │  ┌──────────┐  │
│  │Feign CLI │  │ │  │Feign CLI │  │ │  │Feign CLI │  │ │  │Feign CLI │  │
│  │(sync RPC)│  │ │  │(sync RPC)│  │ │  │(sync RPC)│  │ │  │(sync RPC)│  │
│  └──────────┘  │ │  └──────────┘  │ │  └──────────┘  │ │  └──────────┘  │
└────────┬───────┘ └────────┬───────┘ └────────┬───────┘ └────────┬───────┘
         │                  │                  │                  │
    ┌────▼──┐          ┌────▼──┐          ┌────▼──┐          ┌────▼──┐
    │ MySQL │          │ MySQL │          │ MySQL │          │ MySQL │
    │ :3306 │          │ :3307 │          │ :3308 │          │ :3309 │
    │ users │          │restau  │          │orders │          │delivery
    └───────┘          └───────┘          └───────┘          └───────┘

        RabbitMQ Messaging Layer (Port 5672 / Management 15672)
        ┌────────────────────────────────────────────────┐
        │ OrderCreated → DeliveryCreated Event           │
        │ OrderAccepted → DriverAssigned Event           │
        │ OrderCancelled → CancellationEvent             │
        │ DeliveryCompleted → OrderCompleted Event       │
        │ RestaurantRated → RatingUpdated Event          │
        └────────────────────────────────────────────────┘
                │               │              │
                ▼               ▼              ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │ NestJS Rec.  │ │ Analytics    │ │ Notifications│
        │ Service      │ │ Tracking     │ │ (optional)   │
        │ (Port 3000)  │ │              │ │              │
        └──────────────┘ └──────────────┘ └──────────────┘
             │
        ┌────▼────┐
        │ MongoDB  │
        │ :27017   │
        │ Recs/    │
        │ Analytics│
        └──────────┘
```

---

## 📊 Service Dependencies & Communication Patterns

### User Service
- **Endpoints:**
  - `POST /api/v1/users` → Create user
  - `GET /api/v1/users/{id}` → Get user profile
  - `PUT /api/v1/users/{id}` → Update profile

- **Dependencies:** None (no outbound Feign calls)
- **Database:** MySQL (foodhub_users)
- **Messaging:** Publishes user-related events

---

### Restaurant Service
- **Endpoints:**
  - `POST /api/v1/restaurants` → Create restaurant
  - `GET /api/v1/restaurants` → List restaurants
  - `POST /api/v1/restaurants/menu` → Add menu items
  - `GET /api/v1/restaurants/{id}/menu` → Get menu

- **Dependencies:** None (no outbound Feign calls)
- **Database:** MySQL (foodhub_restaurants)
- **Messaging:** 
  - Consumes: RestaurantRated events
  - Updates ratings & metrics

---

### Order Service
- **Endpoints:**
  - `POST /api/v1/orders/users/{userId}` → Create order
  - `GET /api/v1/orders/{id}` → Get order details
  - `PATCH /api/v1/orders/{id}/status` → Update order status
  - `POST /api/v1/orders/{id}/rate` → Rate restaurant

- **Feign Dependencies:**
  - ✅ **User Service** (get user & address)
  - ✅ **Restaurant Service** (verify menu items, prices)

- **Database:** MySQL (foodhub_orders)
- **Messaging:**
  - Publishes: OrderCreated, OrderAccepted, OrderCancelled, RestaurantRated
  - Consumes: DeliveryCompleted → update order status to COMPLETED

---

### Delivery Service
- **Endpoints:**
  - `GET /api/v1/delivery/order/{orderId}` → Get delivery for order
  - `PATCH /api/v1/delivery/{id}/status` → Update delivery status
  - `GET /api/v1/delivery/{id}` → Get delivery details

- **Feign Dependencies:**
  - ✅ **Order Service** (get order details for delivery)

- **Database:** MySQL (foodhub_delivery)
- **Messaging:**
  - Consumes: OrderCreated → auto-create delivery, OrderAccepted → auto-assign driver
  - Publishes: DeliveryCompleted → triggers order completion

---

### Recommendation Service (NestJS)
- **Endpoints:**
  - `GET /api/v1/recommendations/users/{userId}` → Get recommendations
  - `POST /api/v1/analytics/track` → Track user activity (optional)

- **Feign Dependencies:** None (calls go through API Gateway)
- **Database:** MongoDB (recommendations, user_preferences, analytics)
- **Messaging:**
  - Consumes: All events (OrderCreated, RestaurantRated, etc.)
  - Tracks user behavior for ML model training
  - Simple collaborative filtering algorithm

- **Algorithm:**
  - Tracks: user_id, restaurant_id, rating, order_count
  - Recommends: restaurants with similar user preferences
  - Example: If user A & B rated restaurants 1,2,3 similarly, recommend A's favorites to B

---

### API Gateway
- **Port:** 8080 (production), 4200 (Angular dev server)
- **Features:**
  - ✅ Route all `/api/v1/**` requests to services via Eureka
  - ✅ Validate JWT tokens from Keycloak
  - ✅ Add Authorization header to outbound requests
  - ✅ Log all requests
  - ✅ Rate limiting (optional)
  - ✅ Centralized error handling

- **Service Routes:**
  ```
  /api/v1/users/** → User Service (8081)
  /api/v1/restaurants/** → Restaurant Service (8082)
  /api/v1/orders/** → Order Service (8083)
  /api/v1/cart/** → Order Service (8083)
  /api/v1/delivery/** → Delivery Service (8084)
  /api/v1/recommendations/** → Recommendation Service (3000)
  ```

---

### Eureka Server
- **Port:** 8761
- **Function:**
  - Service registry that all services register with
  - Gateway queries Eureka to find service instances
  - Enables dynamic load balancing & failover

- **Service Registration:**
  ```
  eureka-server:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  client:
    register-with-eureka: true
    fetch-registry: true
  ```

---

### Config Server
- **Port:** 8888
- **Function:**
  - Centralized configuration management
  - Services fetch configs on startup and poll periodically
  - Enables dynamic property updates without restart

- **Configuration Sources:**
  - Local files (config-server/src/main/resources/)
  - GitHub repo (optional)
  - Environment variables

---

## 🔄 Event Flow Example: Placing an Order

```
1. User clicks "Place Order" in Angular UI

2. Frontend calls: POST /api/v1/orders/users/1 (with JWT token)

3. API Gateway receives request
   ├─ Validates JWT token via Keycloak
   ├─ Extracts user info from token claims
   └─ Routes to Order Service (Eureka lookup → port 8083)

4. Order Service processes order
   ├─ Feign call: User Service → Get user address (sync)
   ├─ Feign call: Restaurant Service → Validate menu items (sync)
   ├─ Create order record in MySQL
   └─ Publish "OrderCreated" event to RabbitMQ

5. RabbitMQ event triggers Delivery Service
   ├─ Receive "OrderCreated" event
   ├─ Feign call: Order Service → Get order details (sync)
   ├─ Create delivery record in MySQL
   └─ Auto-assign available driver

6. RabbitMQ event triggers Recommendation Service
   ├─ Receive "OrderCreated" event
   ├─ Track: userId, restaurantId, order_amount in MongoDB
   └─ Update analytics for ML model training

7. User sees order confirmation
   ├─ Angular calls: GET /api/v1/orders/1 (via Gateway)
   └─ Displays order status, estimated delivery time

8. Driver accepts delivery
   ├─ Backend calls: PATCH /api/v1/delivery/1/status?status=CONFIRMED
   ├─ Publish "OrderAccepted" event
   └─ Frontend polls for status updates

9. Driver marks delivery as complete
   ├─ Backend calls: PATCH /api/v1/delivery/1/status?status=DELIVERED
   ├─ Publish "DeliveryCompleted" event
   └─ Order Service receives event, updates order status

10. User rates restaurant
    ├─ Angular calls: POST /api/v1/orders/1/rate (with {rating: 5})
    ├─ Order Service stores rating
    ├─ Publish "RestaurantRated" event
    ├─ Restaurant Service updates average rating
    └─ Recommendation Service updates user preferences for ML
```

---

## 🔐 Security Architecture

### Token Flow

```
┌─────────────────────────────────────────────────┐
│ Frontend (Angular)                              │
│  1. User logs in with username/password         │
│  2. Sends credentials to Keycloak               │
└─────────────┬───────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│ Keycloak (OAuth2/OpenID Connect)                │
│  1. Validates credentials                       │
│  2. Generates JWT token:                        │
│     - exp: expiration time                      │
│     - sub: user_id                              │
│     - email: user email                         │
│     - roles: [CUSTOMER/ADMIN/RESTAURANT_OWNER] │
│     - signed with private key                   │
└─────────────┬───────────────────────────────────┘
              │
              ▼ (JWT token returned to frontend)
┌─────────────────────────────────────────────────┐
│ Frontend stores token                           │
│  - localStorage (less secure, simpler)          │
│  - httpOnly cookie (more secure, same-origin)   │
└─────────────┬───────────────────────────────────┘
              │
              ▼ (Auth Interceptor adds JWT to every request)
┌─────────────────────────────────────────────────┐
│ API Gateway                                      │
│  1. Receives request with:                      │
│     Authorization: Bearer <JWT>                 │
│  2. Validates token:                            │
│     - Verifies signature (ensures not tampered) │
│     - Checks expiration (not expired)           │
│     - Validates user roles                      │
│  3. On success: adds user info to request ctx   │
│  4. Routes to backend service                   │
└─────────────┬───────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────┐
│ Backend Services (User/Restaurant/Order/etc)    │
│  - Receive user info from gateway context       │
│  - Use user_id for authorization checks        │
│  - Example: Only users can see their own orders │
└─────────────────────────────────────────────────┘
```

### Token Claims Example

```json
{
  "exp": 1686000000,
  "iat": 1685996400,
  "sub": "user-123",
  "email": "john@example.com",
  "name": "John Doe",
  "roles": ["CUSTOMER"],
  "iss": "http://localhost:8180/realms/foodhub",
  "aud": "foodhub-frontend",
  "typ": "Bearer"
}
```

---

## 📡 Database Schema Relationships

### User Service (MySQL - foodhub_users)
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  keycloak_id VARCHAR(255) UNIQUE,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(20),
  role ENUM('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_DRIVER', 'ADMIN'),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: keycloak_id, email
);

CREATE TABLE user_addresses (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY → users(id),
  address_line VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(100),
  zipcode VARCHAR(20),
  is_default BOOLEAN,
  INDEXES: user_id
);
```

### Restaurant Service (MySQL - foodhub_restaurants)
```sql
CREATE TABLE restaurants (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255),
  owner_id BIGINT FOREIGN KEY → users(id),
  description TEXT,
  address VARCHAR(255),
  city VARCHAR(100),
  delivery_fee DECIMAL(10,2),
  estimated_delivery_time INT,
  minimum_order_amount DECIMAL(10,2),
  average_rating DECIMAL(3,2),
  total_ratings INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: owner_id, city
);

CREATE TABLE menu_items (
  id BIGINT PRIMARY KEY,
  restaurant_id BIGINT FOREIGN KEY → restaurants(id),
  category_id BIGINT,
  name VARCHAR(255),
  description TEXT,
  price DECIMAL(10,2),
  available BOOLEAN,
  preparation_time INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: restaurant_id, category_id
);
```

### Order Service (MySQL - foodhub_orders)
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY → users(id),
  restaurant_id BIGINT FOREIGN KEY → restaurants(id),
  delivery_address_id BIGINT FOREIGN KEY → user_addresses(id),
  status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'COMPLETED'),
  total_amount DECIMAL(10,2),
  delivery_fee DECIMAL(10,2),
  special_instructions TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: user_id, restaurant_id, status, created_at
);

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY,
  order_id BIGINT FOREIGN KEY → orders(id),
  menu_item_id BIGINT,
  quantity INT,
  unit_price DECIMAL(10,2),
  subtotal DECIMAL(10,2),
  INDEXES: order_id
);

CREATE TABLE order_ratings (
  id BIGINT PRIMARY KEY,
  order_id BIGINT FOREIGN KEY → orders(id),
  restaurant_id BIGINT FOREIGN KEY → restaurants(id),
  user_id BIGINT FOREIGN KEY → users(id),
  rating INT (1-5),
  comment TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: order_id, restaurant_id, user_id
);
```

### Delivery Service (MySQL - foodhub_delivery)
```sql
CREATE TABLE deliveries (
  id BIGINT PRIMARY KEY,
  order_id BIGINT FOREIGN KEY → orders(id),
  driver_id BIGINT FOREIGN KEY → users(id),
  status ENUM('CREATED', 'ASSIGNED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'),
  pickup_time TIMESTAMP,
  delivery_time TIMESTAMP,
  estimated_delivery_time INT,
  current_latitude DECIMAL(10,8),
  current_longitude DECIMAL(10,8),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: order_id, driver_id, status
);

CREATE TABLE delivery_location_history (
  id BIGINT PRIMARY KEY,
  delivery_id BIGINT FOREIGN KEY → deliveries(id),
  latitude DECIMAL(10,8),
  longitude DECIMAL(10,8),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEXES: delivery_id, timestamp
);
```

### Recommendation Service (MongoDB)
```javascript
db.user_preferences.insertOne({
  userId: "user-123",
  cuisinePreferences: ["Pizza", "Italian"],
  favoriteRestaurants: [1, 2, 3],
  likedMenuItems: [101, 102, 103],
  averageOrderAmount: 25.50,
  orderFrequency: [
    { day: "Friday", count: 3 },
    { day: "Saturday", count: 2 }
  ],
  lastOrderDate: ISODate("2026-06-01"),
  updatedAt: ISODate("2026-06-06")
});

db.recommendations.insertOne({
  userId: "user-123",
  recommendedRestaurants: [5, 6, 7],
  scores: [0.95, 0.89, 0.82],
  reason: "Similar to your favorite restaurants",
  generatedAt: ISODate("2026-06-06"),
  expiresAt: ISODate("2026-06-13")
});

db.analytics.insertOne({
  event: "OrderCreated",
  userId: "user-123",
  restaurantId: 1,
  orderAmount: 45.99,
  timestamp: ISODate("2026-06-06"),
  metadata: { cuisine: "Pizza", deliveryTime: 30 }
});
```

---

## 🔌 RabbitMQ Queue Structure

### Exchanges & Bindings

```
Direct Exchange: foodhub.events

Queues:
├── order.created.queue
│   ├─ Routing Key: order.created
│   ├─ Consumers: DeliveryService, RecommendationService, AnalyticsService
│   └─ Message: { orderId, userId, restaurantId, totalAmount, timestamp }
│
├── order.accepted.queue
│   ├─ Routing Key: order.accepted
│   ├─ Consumers: DeliveryService, FrontendNotification
│   └─ Message: { orderId, restaurantId, preparationTime }
│
├── order.cancelled.queue
│   ├─ Routing Key: order.cancelled
│   ├─ Consumers: DeliveryService, UserService, RecommendationService
│   └─ Message: { orderId, userId, reason, timestamp }
│
├── delivery.completed.queue
│   ├─ Routing Key: delivery.completed
│   ├─ Consumers: OrderService, RecommendationService
│   └─ Message: { deliveryId, orderId, completedAt, deliveryTime }
│
└── restaurant.rated.queue
    ├─ Routing Key: restaurant.rated
    ├─ Consumers: RestaurantService, RecommendationService
    └─ Message: { restaurantId, orderId, rating, userId, comment }
```

---

## 📈 Monitoring Architecture

### Prometheus Metrics

Each Spring Boot service exposes metrics at `/actuator/prometheus`:

```
jvm_memory_used_bytes               (JVM memory usage)
jvm_gc_pause_seconds                (garbage collection time)
http_server_requests_seconds        (request latency)
http_server_requests_seconds_count  (request count)
spring_boot_application_started_total (startup time)
rabbitmq_acknowledged               (message acknowledgments)
```

### Grafana Dashboards

Pre-built dashboards for:
1. **System Health Dashboard**
   - Microservices status (up/down)
   - Memory & CPU usage per service
   - Error rates & 95th percentile latency

2. **Request Flow Dashboard**
   - Requests per second (RPS)
   - Average response time
   - Top slow endpoints

3. **Database Dashboard**
   - Query execution time
   - Connection pool usage
   - Deadlocks & locks

4. **Queue Dashboard**
   - RabbitMQ queue depth
   - Message throughput
   - Consumer lag

---

## 🚀 Scalability & Deployment

### Horizontal Scaling

Each service can be replicated:
```
Order Service Instance 1 (Eureka registered)
Order Service Instance 2 (Eureka registered)
Order Service Instance 3 (Eureka registered)

API Gateway uses Eureka to load balance across all 3 instances
```

### Database Replication

For production, consider:
- MySQL Master-Slave replication for high availability
- MongoDB sharding for large recommendation dataset
- Separate read replicas for reporting

### Kubernetes Deployment (Future Phase)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3  # Horizontal scaling
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: order-service
        image: foodhub-order-service:latest
        ports:
        - containerPort: 8083
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

---

## 📚 References

- **Spring Boot 3.x:** https://spring.io/projects/spring-boot
- **Spring Cloud:** https://spring.io/projects/spring-cloud
- **Eureka:** https://github.com/Netflix/eureka/wiki
- **Keycloak:** https://www.keycloak.org/getting-started/getting-started-docker
- **RabbitMQ:** https://www.rabbitmq.com/documentation.html
- **Angular 18:** https://angular.io/
- **NestJS:** https://docs.nestjs.com/
- **Docker Compose:** https://docs.docker.com/compose/

---

**Last Updated:** June 6, 2026

