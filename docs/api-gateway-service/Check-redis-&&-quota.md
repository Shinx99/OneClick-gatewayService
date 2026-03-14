```text
Nguyên nhân & Fix RateLimiter
RequestRateLimiter dùng Redis bucket (replenishRate=10 req/phút, burst=20). Swagger test spam nhiều request → quota cạn.

Fix nhanh:

bash
# Kiểm tra Redis (docker exec vào authService-be-redis)
docker exec -it authService-be-redis redis-cli
KEYS *rateLimiter*  # Xem keys
DEL <key_name>  # Xóa quota (thay <key_name> thật)
FLUSHDB  # Clear hết (cẩn thận)
Hoặc tăng config tạm (application-dev.yml):

text
redis-rate-limiter.replenishRate: 100  # Tăng req/phút
redis-rate-limiter.burstCapacity: 200
Restart gateway → test lại /api/auth/register → success với response từ authService. [user query]
```
Tăng quota KHỦNG (dev):

text
redis-rate-limiter.replenishRate: 1000  # 1000 req/phút
redis-rate-limiter.burstCapacity: 5000