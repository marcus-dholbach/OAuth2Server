#!/bin/sh

echo "Down OAuth2Server en modo PRODUCCIÓN (si está corriendo)..."
docker-compose -f docker-compose.prod.yml down

echo "=== Arrancando OAuth2Server en modo PRODUCCIÓN ==="
docker-compose -f docker-compose.prod.yml up -d --build