# OneClick-gatewayService
## Gateway Service Quick Start Guide
>Central API Gateway routing traffic to Auth, CV/Job, and Social services with rate limiting and service discovery.

### What it does
- Routes: /api/auth/** → Auth Service, /cv/** → Recruitment Service, /social/** → Social Service

- Rate Limits: 1000 req/min for Auth/CV, 300 req/min for Social (Redis-backed)

- Discovery: Eureka load balancing for microservices

- CORS: All origins/methods enabled

- Port: 8080

## Quick Start (after git clone)

1. Prerequisites (must run first)
   ```text
   docker-compose up -d eureka-server gateway-redis
   ```
2. Start Gateway
   ```bash
   cd gateway-service
   docker-compose up -d gateway-service
    # or
    mvn spring-boot:run
    ```
3. Verify running
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/actuator/gateway/routes
   ```
4. Test routes
   ```text
   http://localhost:8080/api/auth/login    → Auth Service
   http://localhost:8080/cv/search        → Recruitment Service  
   http://localhost:8080/social/feed      → Social Service
   ```
   ### Required Services Order
   ```text
   1. eureka-server:8761  (discovery)
   2. gateway-redis:6379  (rate limiting)
   3. gateway-service:8080
   4. authService-be
   5. recruitmentService-be
   6. socialService-app:8080
    ```
   ### Common Dev Ports
   ```text
   Eureka:        :8761
   Gateway:       :8080
   Redis:         :6379
   Auth Service:  :8081  
   Recruitment:   :8082
   Social:        :8080
   ```
   
   ## Troubleshooting
   - 503 errors: Check Eureka dashboard http://localhost:8761

    - 429 rate limit: Redis down or limits hit

    - Gateway not starting: eureka-server + redis must be up first

    - CORS issues: All origins allowed by default
