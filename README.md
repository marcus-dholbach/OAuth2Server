# OAuth2Server

OAuth2Server es un servidor de autenticación basado en **Spring Boot** que emite tokens JWT firmados. Sirve para que otras aplicaciones puedan autenticar usuarios de forma segura.

---

## 🏗️ Arquitectura

Este proyecto implementa **Arquitectura Hexagonal** (Ports and Adapters) para mantener el código limpio, testeable y desacoplado del framework.

```
src/main/java/com/oauth/
├── domain/                    # 🔵 NÚCLEO - Sin dependencias externas
│   ├── exception/            # Excepciones del dominio
│   ├── model/                # Entidades y value objects
│   └── ports/                # Interfaces (contratos)
│       └── in/               # Puertos de entrada
│
├── application/              # 🟢 CASOS DE USO
│   └── usecase/             # Implementaciones de use cases
│
├── adapters/                 # 🟡 ADAPTADORES EXTERNOS
│   ├── input/               # Adaptadores de entrada (Driven)
│   │   └── rest/            # Controladores REST
│   │
│   └── output/              # Adaptadores de salida (Driving)
│       ├── persistence/      # Repositorios JPA
│       └── security/         # Adaptadores de seguridad
│
├── infrastructure/           # 🟠 INFRAESTRUCTURA
│   └── service/             # Servicios específicos del framework
│
└── config/                   # 🔴 Configuración Spring
```

### Principios aplicados
- **Dominio limpio**: La lógica de negocio no depende de frameworks
- **Puertos**: Interfaces que definen contratos entre capas
- **Adaptadores**: Implementaciones concretas de los puertos
- **Inversión de dependencias**: Las dependencias apuntan hacia el dominio

---

## 🚀 Inicio rápido

### Requisitos
- Java 21
- Maven 3.9+
- Docker y Docker Compose

### Ejecutar en 5 minutos

```bash
# 1. Clonar el proyecto
git clone https://github.com/FelixMarin/OAuth2Server.git
cd OAuth2Server

# 2. Ejecutar con Docker Compose (incluye PostgreSQL)
docker-compose up --build

# 3. Acceder a la aplicación
# http://localhost:8080 (desarrollo)
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

Consulta `docker-compose.prod.yml` para la configuración de producción.

---

## ⚙️ Configuración

### Variables de entorno principales

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto de la aplicación | 8080 |
| `SPRING_DATASOURCE_URL` | URL de PostgreSQL | jdbc:postgresql://postgres:5432/oauth2_dev |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | oauth2_user |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de BD | oauth2_dev_password |
| `JWT_SIGNING_KEY` | Clave para firmar tokens JWT | clave-secreta |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS | http://localhost:3000 |

### Perfiles Spring

- `dev`: Desarrollo (usa PostgreSQL en docker-compose)
- `prod`: Producción (configuración optimizada)

---

## 🏢 Añadir una nueva aplicación

Para que la aplicación pueda usar OAuth2, necesitas registrarla mediante variables de entorno:

```bash
# Configurar cliente OAuth2
OAUTH2_CLIENTS=APP1,APP2

# Cliente 1
APP1_SECRET=mi-secreto-seguro
APP1_REDIRECT_URI=http://localhost:3000/oauth/callback

# Cliente 2
APP2_SECRET=otra-secreto
APP2_REDIRECT_URI=http://localhost:9000/oauth/callback
```

### Flujo Authorization Code

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

### Flujo Client Credentials (M2M)

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
- **OAuth2Server** - Puerto 8080
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
| `/user/me` | GET | Info del usuario actual |
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
- Asegúrate de que la URL de callback está configurada mediante variables de entorno

**Error de conexión a PostgreSQL**
- Verifica que el contenedor de PostgreSQL está corriendo
- Comprueba las credenciales en las variables de entorno

---

## Documentación extendida
- [COMMANDS](doc/COMMANDS.md)
- [ENDPOINTS](doc/ENDPOINTS.md)
- [MANUAL](doc/MANUAL.md)
- [REGISTRAR APLICACIÓN](doc/REGSITRAR_NUEVA_APLICACION.md)

---

## ℹ️ Notas

- La base de datos es **PostgreSQL** (no H2)
- Los tokens JWT se firman con una clave configurada en las variables de entorno
- La configuración sensible se gestiona mediante variables de entorno (no hardcoded)

---

## 📄 Licencia

MIT
