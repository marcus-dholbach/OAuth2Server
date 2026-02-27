#!/bin/bash

set -e

# ===== CONFIGURACIÓN =====
IMAGE_NAME="felixmurcia/oauth2server"
NAMESPACE="auth"
DEPLOYMENT="oauth2-server"
CONTAINER_NAME="oauth2-server"

# ===== COMPILACIÓN Y TESTS =====
echo "======================================"
echo "  🔨 Compilando aplicación y ejecutando tests"
echo "======================================"

# Ejecutar mvn clean install (esto compila y ejecuta los tests)
mvn clean install

# Verificar que la compilación fue exitosa
if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación o los tests. Abortando despliegue."
    exit 1
fi

echo "✅ Compilación y tests exitosos"

# ===== GENERAR TAG AUTOMÁTICO =====
TAG=$(date +"v%Y%m%d-%H%M")
FULL_IMAGE="$IMAGE_NAME:$TAG"

echo "======================================"
echo "  🚀 Construyendo imagen: $FULL_IMAGE"
echo "======================================"

docker build -t $FULL_IMAGE .

echo "======================================"
echo "  📤 Subiendo imagen a Docker Hub"
echo "======================================"

docker push $FULL_IMAGE

echo "======================================"
echo "  📝 Actualizando deployment.yaml con la nueva imagen"
echo "======================================"

# Sustituye la línea de imagen en TU YAML
sed -i "s|image: .*|image: $FULL_IMAGE|" k8s/deployment.yaml

echo "======================================"
echo "  📦 Aplicando manifests"
echo "======================================"

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/tls-secret.yaml


echo "======================================"
echo "  🔄 Reiniciando pod"
echo "======================================"

kubectl delete pod -n $NAMESPACE -l app=$DEPLOYMENT --ignore-not-found=true

echo "======================================"
echo "  🧹 Eliminando imágenes antiguas de oauth2server"
echo "======================================"

IMAGES_TO_DELETE=$(docker images $IMAGE_NAME --format "{{.Repository}}:{{.Tag}} {{.CreatedAt}}" \
  | sort -k2 -r \
  | tail -n +2 \
  | awk '{print $1}')

for IMG in $IMAGES_TO_DELETE; do
  echo "🗑️  Eliminando imagen antigua: $IMG"
  docker rmi -f "$IMG" || true
done

echo "======================================"
echo "  🧹 Limpiando imágenes antiguas de Docker"
echo "======================================"

docker image prune -f
docker container prune -f
docker image prune -a --filter "until=720h" -f

echo "======================================"
echo "  📜 Logs del nuevo pod"
echo "======================================"

kubectl logs -n $NAMESPACE -l app=$DEPLOYMENT -f