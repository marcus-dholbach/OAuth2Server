# 📘 **GUÍA COMPLETA DE OPERACIONES – DOCKER + OAUTH2SERVER**

---

# 🟦 1. COMANDOS GENERALES DE DOCKER

### 🔍 Ver contenedores en ejecución
```bash
docker ps
```

### 📦 Ver todos los contenedores
```bash
docker ps -a
```

### 🧩 Ver imágenes
```bash
docker images
```

---

# 🟩 2. OAuth2Server con Docker Compose

### Iniciar servicios
```bash
docker-compose up --build
```

### Ver logs
```bash
docker-compose logs -f
```

### Detener servicios
```bash
docker-compose down
```

### Ver estado de servicios
```bash
docker-compose ps
```

---

# 🟧 3. OAuth2Server (local sin Docker)

## 🔧 Build y ejecución local

```bash
mvn clean package
java -jar target/oauth2server-0.0.1-SNAPSHOT.jar
```

### Comprobar puerto en uso
```bash
sudo lsof -i :8080
```

### Ejecutar con Spring Boot plugin
```bash
mvn spring-boot:run -X
```

---

## 🐳 Docker local

```bash
docker build -t mi-oauth2-server .
docker run -p 8080:8080 mi-oauth2-server
```

```bash
mvn clean package
docker build -t mi-oauth2-server:latest .
```

---

# 🟪 4. PostgreSQL (Base de datos)

## 🐚 Conectar a PostgreSQL

Si usas docker-compose, PostgreSQL está disponible en:
- Host: localhost
- Puerto: 5432
- Base de datos: oauth2_dev
- Usuario: oauth2_user
- Contraseña: oauth2_dev_password

### Conectar con psql
```bash
docker exec -it <NOMBRE_CONTENEDOR_POSTGRES> psql -U oauth2_user -d oauth2_dev
```

---

## 📤 Copiar base de datos

```bash
# Exportar
docker exec <CONTENEDOR> pg_dump -U oauth2_user oauth2_dev > backup.sql

# Importar
docker exec -i <CONTENEDOR> psql -U oauth2_user oauth2_dev < backup.sql
```

---

## 🔐 Cambiar contraseña de usuario

```sql
UPDATE usuarios
SET password = '$2b$12$...hash bcrypt...'
WHERE username = 'admin';
```

---

## 🔑 Generar bcrypt

```bash
python3 - <<'PY'
import bcrypt
print(bcrypt.hashpw(b"tu-contraseña", bcrypt.gensalt(rounds=10)).decode())
PY
```

---

# 🟫 5. Variables de entorno en producción

## 📌 ¿Dónde se definen?

Las variables de entorno se configuran en el archivo `application-prod.properties` o como variables de entorno del sistema.

### Variables principales

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/oauth2_prod
SPRING_DATASOURCE_USERNAME=oauth2_user
SPRING_DATASOURCE_PASSWORD=tu-contraseña-segura

# OAuth2
MI_EJEMPLO_APP_SECRET=tu-secreto
MI_EJEMPLO_APP_REDIRECT_URI=https://tu-dominio.com/callback

# JWT
JWT_SIGNING_KEY=tu-clave-secreta-jwt
ISSUER_URL=https://tu-dominio.com
```

---

# 🟬 6. Comandos adicionales

## 🔌 Port-forward (Kubernetes - si aplica)

```bash
kubectl port-forward -n auth svc/oauth2-server 8080:8080
```

## 📜 Logs en tiempo real

```bash
# Docker
docker-compose logs -f oauth2server

# Local
tail -f logs/oauth2server.log
```

## 📄 Documentación API (Swagger UI)

```
http://localhost:8080/swagger-ui/index.html
```

## 🔁 Reiniciar OAuth2Server

```bash
# Docker
docker-compose restart

# Local
# Detén y vuelve a ejecutar el JAR
```

---

## 🧼 Crear contenedor temporal (si usas Kubernetes)

```bash
kubectl run cleaner -n auth --image=mi-oauth2-server:latest --command -- sleep 3600
```

---

## 🧹 Kubernetes: Eliminar finalizers de un PVC

```bash
kubectl patch pvc oauth2-pvc -n auth -p '{"metadata":{"finalizers":null}}' --type=merge
```

---

## 🟭 7. Troubleshooting

### Verificar que PostgreSQL está disponible

```bash
docker exec -it <CONTENEDOR_POSTGRES> pg_isready -U oauth2_user
```

### Ver logs de OAuth2Server

```bash
docker-compose logs -f --tail=100 oauth2server
```

### Reiniciar servicios

```bash
docker-compose down
docker-compose up --build
```
