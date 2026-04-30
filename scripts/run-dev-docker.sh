#!/bin/sh

echo "Down OAuth2Server en modo DESARROLLO (si está corriendo)..."
docker-compose -f docker-compose.yml down

echo "=== Arrancando OAuth2Server en modo DESARROLLO ==="
docker-compose -f docker-compose.yml up -d