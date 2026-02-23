# OAuth2Server

OAuth2Server es un servicio de autenticación y autorización basado en **Spring Boot**, diseñado para actuar como proveedor OAuth2 y emitir **tokens JWT** firmados. Su objetivo es centralizar la gestión de usuarios, roles y permisos dentro de un entorno de microservicios, ofreciendo un punto de entrada seguro y estandarizado para aplicaciones internas o externas.

El proyecto está preparado para ejecutarse tanto en **entornos locales** (H2, Docker) como en **producción** (Kubernetes), con migraciones gestionadas mediante **Flyway** y un despliegue completamente automatizado.

---

## ✨ Características principales

- **Servidor OAuth2 completo**  
  Implementación de los flujos:
  - **Authorization Code + PKCE** (para aplicaciones web/móviles)
  - **Client Credentials** (para M2M)
  - **Soporte para múltiples clientes** configurable desde properties

- **JWT firmado**  
  Tokens firmados con clave RSA, listos para validación en microservicios.

- **Gestión de usuarios**  
  - Entidad `UserEntity` con campo `app` para distinguir aplicaciones
  - Roles (`UserRole`: `USER`, `ADMIN`)  
  - Contraseñas con **BCrypt**  
  - Endpoints REST para consulta y creación de usuarios

- **Base de datos flexible**  
  - **H2** en desarrollo (en memoria o archivo persistente)  
  - **PostgreSQL** en producción (configurable)

- **Múltiples clientes OAuth2**  
  Configuración dinámica desde `application-dev.properties`:
  ```properties
  oauth2.clients[0].client-id=cine-platform
  oauth2.clients[0].client-secret=cine-platform-secret
  oauth2.clients[0].redirect-uris[0]=http://localhost:5000/oauth/callback
  ```

- **Seguridad personalizada**  
  - `OAuth2ParameterSavingFilter` - Guarda parámetros OAuth2 en sesión
  - `OAuth2SavedRequestAwareAuthSuccessHandler` - Redirección post-login
  - `AppAwareAuthenticationProvider` - Autenticación con filtro por aplicación

- **Despliegue en Kubernetes**  
  Incluye manifests completos:
  - Deployment
  - Service
  - PVC
  - Secrets
  - Ingress
  - Script de despliegue automatizado (`deploy.sh`)

- **Documentación automática**  
  Swagger UI habilitado para explorar y probar endpoints.

---

## 📁 Estructura del proyecto

```
OAuth2Server/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── doc/                          # Documentación detallada
│   ├── COMMANDS.md
│   ├── ENDPOINTS.md
│   ├── MANUAL.md
│   └── REGSITRAR_NUEVA_APLICACION.md
├── k8s/                           # Manifiestos Kubernetes
│   ├── deployment.yaml
│   ├── deploy.sh
│   ├── ingress.yaml
│   ├── namespace.yaml
│   ├── pvc.yaml
│   ├── secrets.yaml
│   └── service.yaml
├── scripts/                       # Scripts de utilidad
│   ├── generate-jwt-key.sh
│   ├── run-dev.sh
│   └── run-prod.sh
├── src/
│   ├── main/
│   │   ├── java/com/oauth/rest/
│   │   │   ├── Application.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── TokenController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   │   ├── UserEntity.java
│   │   │   │   └── UserRole.java
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   │   ├── AppAwareAuthenticationProvider.java
│   │   │   │   ├── oauth2/
│   │   │   │   │   ├── OAuth2AuthorizationServer.java
│   │   │   │   │   ├── OAuth2ClientProperties.java
│   │   │   │   │   ├── OAuth2ParameterSavingFilter.java
│   │   │   │   │   ├── OAuth2SavedRequestAwareAuthSuccessHandler.java
│   │   │   │   │   └── ... (otros)
│   │   │   │   ├── PasswordEncoderConfig.java
│   │   │   │   ├── RequestCacheConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── db/migration/         # Migraciones Flyway
│   │       └── templates/login.html  # Página de login personalizada
│   └── test/                          # Tests con Spock
│       ├── groovy/
│       └── resources/
└── logs/                             # Logs de la aplicación
```

---

## 🚀 Ejecución local

### Requisitos previos
- Java 21
- Maven 3.9+

### Con Maven

```bash
# Compilar
mvn clean package

# Ejecutar (perfil dev por defecto)
java -jar target/OAuth2Server-0.0.1-SNAPSHOT.jar

# Con perfil específico
java -jar target/OAuth2Server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Con Spring Boot plugin

```bash
# Perfil dev (por defecto)
mvn spring-boot:run

# Perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Con Docker Compose (recomendado para desarrollo)

```bash
# Construir y levantar
docker-compose up --build

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

---

## 🐳 Ejecución con Docker

### Construir imagen

```bash
docker build -t felixmurcia/oauth2server:dev .
```

### Ejecutar contenedor

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  felixmurcia/oauth2server:dev
```

---

## 🔐 Flujos OAuth2 soportados

### 1. Authorization Code + PKCE (Recomendado para usuarios)

**Paso 1:** Redirigir al usuario al endpoint de autorización:

```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=cine-platform&
  redirect_uri=http://localhost:5000/oauth/callback&
  scope=openid%20profile%20read%20write&
  code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&
  code_challenge_method=S256&
  state=random_state_string
```

**Paso 2:** El usuario se autentica en la página de login (`/login`).

**Paso 3:** El servidor redirige al callback con el código:

```
http://localhost:5000/oauth/callback?code=xxx&state=random_state_string
```

**Paso 4:** Canjear el código por tokens:

```bash
curl -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "cine-platform:cine-platform" \
  -d "grant_type=authorization_code" \
  -d "code=CODIGO_RECIBIDO" \
  -d "redirect_uri=http://localhost:5000/oauth/callback" \
  -d "code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk" \
  http://localhost:8080/oauth2/token
```

**Respuesta exitosa:**

```json
{
  "access_token": "eyJraWQiOi...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "refresh_token": "xxx",
  "scope": "openid profile read write"
}
```

---

### 2. Client Credentials (M2M)

Para comunicación máquina-a-máquina (sin usuario):

```bash
curl -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "cine-platform:cine-platform" \
  -d "grant_type=client_credentials" \
  -d "scope=read write" \
  http://localhost:8080/oauth2/token
```

**Respuesta:**

```json
{
  "access_token": "eyJraWQiOi...",
  "token_type": "Bearer",
  "expires_in": 86399,
  "scope": "read write"
}
```

---

## 📝 Configuración de clientes OAuth2

Los clientes se definen en `application-dev.properties`:

```properties
# ============================
# 📋 CLIENTES OAuth2
# ============================

# Cliente para cine-platform
oauth2.clients[0].client-id=cine-platform
oauth2.clients[0].client-secret=cine-platform
oauth2.clients[0].redirect-uris[0]=http://localhost:5000/oauth/callback
oauth2.clients[0].scopes[0]=openid
oauth2.clients[0].scopes[1]=profile
oauth2.clients[0].scopes[2]=read
oauth2.clients[0].scopes[3]=write
oauth2.clients[0].require-consent=true
oauth2.clients[0].require-proof-key=false
oauth2.clients[0].authorization-grant-types=client_credentials,authorization_code,refresh_token

# Cliente para transcribeapp
oauth2.clients[1].client-id=transcribeapp
oauth2.clients[1].client-secret=transcribeapp-secret
oauth2.clients[1].redirect-uris[0]=http://localhost:3000/oauth/callback
oauth2.clients[1].scopes[0]=openid
oauth2.clients[1].scopes[1]=profile
oauth2.clients[1].scopes[2]=read
oauth2.clients[1].scopes[3]=write
oauth2.clients[1].require-consent=true
oauth2.clients[1].require-proof-key=false
```

---

## 🔑 Credenciales por defecto


### Cliente OAuth2 por defecto

```properties
client-id: cine-platform
client-secret: cine-platform
redirect-uri: http://localhost:5000/oauth/callback
```

---

## ☸️ Despliegue en Kubernetes

### Prerrequisitos
- Kubernetes cluster
- `kubectl` configurado
- Docker Hub acceso (para subir imagen)

### Estructura de manifests

```
k8s/
├── namespace.yaml     # Namespace 'auth'
├── secrets.yaml       # Secrets (JWT, client secrets)
├── pvc.yaml          # PersistentVolumeClaim
├── deployment.yaml    # Deployment con 1 réplica
├── service.yaml       # ClusterIP service
├── ingress.yaml      # Ingress (dominio personalizado)
└── deploy.sh         # Script de despliegue automatizado
```

### Despliegue completo

```bash
# Hacer el script ejecutable
chmod +x k8s/deploy.sh

# Ejecutar despliegue
./k8s/deploy.sh
```

El script `deploy.sh` automatiza:
1. Compilación con Maven (`mvn clean install`)
2. Construcción de imagen Docker
3. Subida a Docker Hub
4. Actualización del deployment.yaml
5. Aplicación de manifests en Kubernetes
6. Reinicio del pod
7. Limpieza de imágenes antiguas

### Comandos útiles

```bash
# Ver estado
kubectl get all -n auth

# Ver logs
kubectl logs -n auth -l app=oauth2-server -f

# Port-forward para pruebas locales
kubectl port-forward -n auth svc/oauth2-server 8080:8080

# Reiniciar deployment
kubectl rollout restart deployment/oauth2-server -n auth
```

---

## 🗄️ Base de datos

### Desarrollo (H2 en memoria)
```properties
spring.datasource.url=jdbc:h2:mem:oauth2db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Acceso a consola H2: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:oauth2db`
- Usuario: `sa`
- Contraseña: (vacío)

### Producción (PostgreSQL recomendado)
```properties
spring.datasource.url=jdbc:postgresql://postgres-service:5432/oauth2db
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

## 🔧 Variables de entorno

### Desarrollo (`.env` o `docker-compose.yml`)

```bash
# Puerto
PORT=8080

# H2 Database
H2_USERNAME=sa
H2_PASSWORD=

# JWT
ISSUER_URL=http://localhost:8080
JWT_AUDIENCE=oauth2-client
JWT_SIGNING_KEY=clave-secreta-jwt-para-desarrollo-cambiar-en-produccion

# Token validity
ACCESS_TOKEN_VALIDITY=86400
REFRESH_TOKEN_VALIDITY=1296000
```

### Producción (Kubernetes secrets)

```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: oauth2-secrets
  namespace: auth
type: Opaque
data:
  jwt-signing-key: <base64>
  oauth-client-id: <base64>
  oauth-client-secret: <base64>
  oauth-redirect-uri: <base64>
  oauth-audience: <base64>
  issuer-url: <base64>
  h2-username: <base64>
  h2-password: <base64>
  default-admin-username: <base64>
  default-admin-password: <base64>
```

---

## 📄 Endpoints disponibles

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/oauth2/authorize` | GET | Iniciar flujo Authorization Code | No |
| `/oauth2/token` | POST | Obtener token (code o client credentials) | Basic Auth |
| `/login` | GET/POST | Página de login | No |
| `/user/me` | GET | Información del usuario actual | Bearer Token |
| `/user` | POST | Crear nuevo usuario | No (configurable) |
| `/h2-console` | GET | Consola H2 (solo dev) | No |
| `/swagger-ui/**` | GET | Documentación API | ADMIN |

---

## 🧪 Tests

El proyecto incluye tests unitarios y de integración con **Spock Framework**:

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar con cobertura
mvn verify

# Ver reporte de cobertura
# Abrir target/site/jacoco/index.html
```

---

## 📚 Documentación adicional

- [`doc/COMMANDS.md`](doc/COMMANDS.md) - Comandos útiles
- [`doc/ENDPOINTS.md`](doc/ENDPOINTS.md) - Detalle de endpoints
- [`doc/MANUAL.md`](doc/MANUAL.md) - Manual de instalación
- [`doc/REGSITRAR_NUEVA_APLICACION.md`](doc/REGSITRAR_NUEVA_APLICACION.md) - Cómo añadir nuevas apps

---

## 🤝 Contribuir

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

---

## 📄 Licencia

MIT

---
