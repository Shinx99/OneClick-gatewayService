#!/bin/bash
set -e

echo "🌐 Ensuring oneclick-network..."
docker network create oneclick-network 2>/dev/null || true

# Kiểm tra gateway có chạy không
if docker compose ps gateway | grep -q "Up"; then
  echo "🔄 Gateway running → Rebuilding..."
  docker compose down -v gateway 2>/dev/null || true
  docker compose up --build gateway
else
  echo "🚀 Gateway not running → Fresh start..."
  docker compose up --build gateway
fi

echo "✅ Gateway ready!"
echo "🌐 Health: http://localhost:8080/actuator/health"
echo "📊 Eureka: http://localhost:8761"
