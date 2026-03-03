# OAuth2Server

OAuth2Server es un servidor de autenticación basado en **Spring Boot** que emite tokens JWT firmados. Sirve para que otras aplicaciones puedan autenticar usuarios de forma segura.

---

## 🚀 Inicio rápido

### Requisitos
- Java 21
- Maven 3.9+
- Docker y Docker Compose

### Ejecutar en 5 minutos

```bash
# 1. Descargar el proyecto
git clone https://github.com/la-usuario/OAuth2Server.git
cd OAuth2Server

# 2. Ejecutar con Docker Compose (incluye PostgreSQL)
docker-compose up --build

# 3. Acceder a la aplicación
# http://localhost:8080 (desarrollo)
# https://localhost:8443 (producción)
```

---

## 🔐 Primeros pasos

### Credenciales por defecto

Al iniciar por primera vez, se crea un usuario administrador:

| Campo | Valor |
|-------|-------|
| Usuario | admin |
| Contraseña | admin123 |

### Probar que funciona

1. Abre http://localhost:8080/login
2. Inicia sesión con **admin / admin123**
3. Verás la página de consentimiento OAuth2

---

## 🗄️ Base de datos

La aplicación usa **PostgreSQL** en todos los entornos (desarrollo y producción).

### Desarrollo (docker-compose)

El archivo `docker-compose.yml` configura automáticamente:
- PostgreSQL en el puerto 5432
- Base de datos: `oauth2_dev`
- Usuario: `oauth2_user`
- Contraseña: `oauth2_dev_password`

### Producción

Configura las variables de entorno:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/oauth2_prod
SPRING_DATASOURCE_USERNAME=oauth2_user
SPRING_DATASOURCE_PASSWORD=la-contraseña-segura
```

---

## ⚙️ Configuración básica

### Cambiar puerto

En `src/main/resources/application.properties`:

```properties
server.port=8080
```

### Cambiar usuario y contraseña por defecto

En `src/main/resources/application-dev.properties`:

```properties
# Usuario administrador por defecto
default.admin.username=admin
default.admin.password=la-nueva-contraseña-segura
```

### Configuración de PostgreSQL (desarrollo)

En `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://postgres-dev:5432/oauth2_dev
spring.datasource.username=oauth2_user
spring.datasource.password=oauth2_dev_password
```

---

## 🏢 Añadir una nueva aplicación

Para que la aplicación pueda usar OAuth2, necesitas registrarla.

### Paso 1: Configurar el cliente

Edita `src/main/resources/application-dev.properties` y añade:

```properties
# Cliente para MI-APLICACION
oauth2.clients[0].client-id=mi-aplicacion
oauth2.clients[0].client-secret=mi-secreto-seguro
oauth2.clients[0].redirect-uris[0]=http://localhost:3000/callback
oauth2.clients[0].scopes[0]=openid
oauth2.clients[0].scopes[1]=profile
oauth2.clients[0].scopes[2]=read
oauth2.clients[0].scopes[3]=write
oauth2.clients[0].authorization-grant-types=authorization_code,client_credentials,refresh_token
```

### Explicación de cada campo

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| `client-id` | Identificador único de la app | mi-aplicacion |
| `client-secret` | Contraseña secreta de la app | mi-secreto-seguro |
| `redirect-uris` | URL donde OAuth2 devolverá al usuario | http://localhost:3000/callback |
| `scopes` | Permisos que pide la app | openid, profile, read, write |
| `authorization-grant-types` | Tipos de flujo OAuth2 soportados | authorization_code, client_credentials |

### Paso 2: Reiniciar el servidor

```bash
docker-compose down
docker-compose up --build
```

---

## 👤 Añadir nuevos usuarios

### Mediante API REST

```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "contraseña123",
    "email": "juan@ejemplo.com",
    "app": "mi-aplicacion",
    "role": "USER"
  }'
```

### Explicación de campos

| Campo | Descripción | Valores posibles |
|-------|-------------|------------------|
| `username` | Nombre de usuario | Cualquier texto |
| `password` | Contraseña | Cualquier texto |
| `email` | Correo electrónico | Formato email |
| `app` | Aplicación a la que pertenece | El client-id configurado |
| `role` | Rol del usuario | USER, ADMIN |

---

## 🔗 Cómo usar OAuth2 en la aplicación

### Flujo Authorization Code (para apps web)

**1. Redirige al usuario a:**

```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=mi-aplicacion&
  redirect_uri=http://localhost:3000/callback&
  scope=openid%20profile%20read%20write&
  state=texto-aleatorio
```

**2. El usuario se loguea en OAuth2Server**

**3. La app recibe un código en el callback:**

```
http://localhost:3000/callback?code=XYZ123&state=texto-aleatorio
```

**4. Canjea el código por tokens:**

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u "mi-aplicacion:mi-secreto-seguro" \
  -d "grant_type=authorization_code" \
  -d "code=CODIGO_RECIBIDO" \
  -d "redirect_uri=http://localhost:3000/callback"
```

**5. Respuesta (tokens):**

```json
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "refresh_token": "abc...",
  "scope": "openid profile read write"
}
```

### Flujo Client Credentials (M2M - máquina a máquina)

Sin usuario, solo para comunicación entre servicios:

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u "mi-aplicacion:mi-secreto-seguro" \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

---

## 🐳 Docker Compose

El archivo `docker-compose.yml` incluye:

- **OAuth2Server** - Puerto 8080 (dev) / 8443 (prod)
- **PostgreSQL** - Puerto 5432

### Comandos útiles

```bash
# Iniciar servicios
docker-compose up --build

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

---

## 📡 Endpoints principales

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/oauth2/authorize` | GET | Iniciar login OAuth2 |
| `/oauth2/token` | POST | Obtener tokens |
| `/login` | GET/POST | Página de login |
| `/user/me` | GET | info del usuario actual |
| `/user` | POST | Crear usuario |

---

## 🧪 Tests

```bash
# Ejecutar tests
mvn test

# Ver cobertura
mvn verify
```

---

## ❓ Problemas frecuentes

**No puedo iniciar sesión**
- Verifica que el usuario existe en la base de datos
- Prueba con las credenciales por defecto: admin / admin123

**Error de redirect_uri**
- Asegúrate de que la URL de callback está registrada en `application-dev.properties`

**Error de conexión a PostgreSQL**
- Verifica que el contenedor de PostgreSQL está corriendo
- Comprueba las credenciales en `application-dev.properties`

---

## ℹ️ Notas

- La base de datos es **PostgreSQL** (no H2)
- Los tokens JWT se firman con una clave configurada en las propiedades
- Para producción, configura SSL en `application-prod.properties`

---

## 📄 Licencia

MIT
