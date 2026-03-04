#!/bin/bash

export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

set -e

# ===== CONFIGURACIÓN =====
IMAGE_NAME="felixmurcia/oauth2server"
NAMESPACE="auth"
DEPLOYMENT="oauth2-server"
ENVIRONMENT=${1:-prod}

if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
    echo "❌ Entorno no válido. Usa: dev o prod"
    exit 1
fi

echo "======================================"
echo "  🌍 Desplegando en entorno: $ENVIRONMENT"
echo "======================================"

# ===== COMPILACIÓN Y TESTS =====
echo "======================================"
echo "  🔨 Compilando aplicación"
echo "======================================"

mvn clean install

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación"
    exit 1
fi

# ===== IMAGEN DOCKER =====
TAG=$(date +"v%Y%m%d-%H%M")
FULL_IMAGE="$IMAGE_NAME:$TAG"

echo "======================================"
echo "  🚀 Construyendo imagen: $FULL_IMAGE"
echo "======================================"

docker build -t $FULL_IMAGE .
docker push $FULL_IMAGE

# Actualizar imagen en base
sed -i "s|image: .*|image: $FULL_IMAGE|" k8s/base/deployment.yaml

# ===== DESPLEGAR POSTGRES PRIMERO =====
echo "======================================"
echo "  🗄️  Desplegando PostgreSQL"
echo "======================================"

kubectl apply -k k8s/postgres/overlays/$ENVIRONMENT

# Esperar a que PostgreSQL esté listo
echo "⏳ Esperando a que PostgreSQL esté listo..."
kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=120s

# ===== DESPLEGAR APLICACIÓN =====
echo "======================================"
echo "  🚀 Desplegando aplicación"
echo "======================================"

kubectl apply -k k8s/overlays/$ENVIRONMENT

# ===== REINICIAR Y VERIFICAR =====
echo "======================================"
echo "  🔄 Reiniciando deployment"
echo "======================================"

kubectl rollout restart deployment/$DEPLOYMENT -n $NAMESPACE
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE --timeout=120s

echo "======================================"
echo "  🧹 Limpiando imágenes antiguas de oauth2server"
echo "======================================"

IMAGES_TO_DELETE=$(docker images $IMAGE_NAME --format "{{.Repository}}:{{.Tag}} {{.CreatedAt}}" | sort -k2 -r | tail -n +2 | awk '{print $1}')

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
echo "  📊 Estado de los pods"
echo "======================================"
kubectl get pods -n $NAMESPACE

echo "======================================"
echo "  📜 Logs del nuevo pod (Ctrl+C para salir)"
echo "======================================"

# Mostrar logs del pod más reciente
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=$DEPLOYMENT -o jsonpath="{.items[0].metadata.name}" 2>/dev/null)
if [ ! -z "$POD_NAME" ]; then
    echo "📝 Mostrando logs de: $POD_NAME"
    kubectl logs -n $NAMESPACE $POD_NAME -f
else
    echo "❌ No se encontró ningún pod para mostrar logs"
fi

echo "======================================"
echo "  ✅ Despliegue completado"
echo "======================================"