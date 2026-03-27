# OneClick - Gateway Service
## Quick Start Guide
> Central API Gateway routing traffic to Auth, CV/Job, and Social services with Redis rate limiting & Eureka discovery.

## 🎯 What it does
- Routes: /api/auth/** → Auth Service, /cv/** → Recruitment Service, /social/** → Social Service

- Rate Limits: 1000 req/min (Auth/CV), 300 req/min (Social) - Redis-backed

- Discovery: Eureka load balancing for microservices

- CORS: All origins/methods enabled

- Port: 8080

## 🚀 Quick Start (Fresh Clone)
### 📁 1. Create start.sh Script
```bash
chmod +x start.sh
```
### ⚡ 2. One-Command Start
```bash
./start.sh
```
### 🔍 3. Verify
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/gateway/routes
```
> ### Eureka UI: http://localhost:8761

## 🔄 Daily Dev Workflow
```bash
# Quick rebuild
docker-compose down -v && docker-compose up --build gateway

# Follow logs
docker-compose logs -f gateway

# Or use script
./start.sh rebuild  # Add to script above
```

## 📋 Service Ports

| Service     | External Port | Internal Port | Purpose                  | Health Check |
|-------------|---------------|---------------|--------------------------|--------------|
| **Gateway** | **8080**      | 8080          | API entrypoint           | `/actuator/health` |
| **Eureka**  | **8761**      | 8761          | Service discovery        | `http://localhost:8761` |
| **Redis**   | **6378**      | **6379**      | Rate limiting            | `docker-compose ps redis` |

### ⚡ Essential Commands

| Action             | Command                                                                 | Usage |
|--------------------|-------------------------------------------------------------------------|-------|
| Fresh Setup        | `./start.sh`                                                            | Lần đầu |
| Quick Rebuild      | `docker-compose down -v && docker-compose up --build gateway`           | Daily dev |
| View Logs          | `docker-compose logs -f gateway`                                        | Debug |
| Stop All           | `docker-compose down`                                                   | Pause |
| Network Check      | `docker network ls \| grep oneclick-network`                            | Verify |

### 🔍 Test Routes

| Endpoint                | Target Service | Expected |
|-------------------------|----------------|----------|
| `/api/auth/login`       | Auth           | 200 OK   |
| `/cv/search`            | Recruitment    | 200 OK   |
| `/social/feed`          | Social         | 200 OK   |
| `/actuator/health`      | Gateway        | UP       |

## 📦 Required Services Order
    
    1. eureka-server:8761     (discovery)
    2. gateway-redis:6379     (rate limiting)
    3. gateway-service:8080
    4. authService-be:8081
    5. recruitmentService-be:8082
    6. socialService-app:8080

## 🛠️ Troubleshooting


| Issue                    | Solution                                      |
|--------------------------|-----------------------------------------------|
| `Redis connection failed` | Ensure `gateway-redis` is running on 6378    |
| `Route not found`        | Check if target service is registered in Eureka |
| `Docker network error`   | Run `docker network create oneclick-network` |

## 🤝 Contributing
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.