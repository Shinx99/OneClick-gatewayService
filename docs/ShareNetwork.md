```bash
Shared Network (Khuyến Nghị)
bash
# Tạo 1 lần
docker network create oneclick-network

# AuthService compose.yml
networks:
  app-network:
    external:
      name: oneclick-network

# Gateway compose.yml  
networks:
  oneclick-network:
    external: true
Deploy Đúng Thứ Tự
bash
# 1. Tạo shared network
docker network create oneclick-network

# 2. Start services
cd authService && docker compose up -d
cd cvService && docker compose up -d  
cd gateway && docker compose up -d

# Verify
docker network inspect oneclick-network  # Thấy tất containers
```