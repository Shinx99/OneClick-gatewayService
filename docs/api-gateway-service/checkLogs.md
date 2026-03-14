```bash
docker logs -f gateway-service-gateway 2>&1 | grep -E "(ERROR|FAIL|Redis|LoadBalancer)"

curl http://localhost:8761 | grep authService-be

docker logs -f gateway-service-gateway | grep -E "auth|rate|LoadBalancer"

docker logs gateway-service-gateway | grep -i eureka

```

```bash
## Reset RequestRateLimiter
docker exec -it gateway-redis redis-cli FLUSHDB
docker exec -it gateway-redis redis-cli KEYS "*gateway-requests*"
```

```bash
