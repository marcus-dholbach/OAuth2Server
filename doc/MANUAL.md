# Servidor OAuth2 - Manual de Instalación y Ejecución

Este manual te guiará paso a paso para configurar y ejecutar el servidor OAuth2 desde cero.

## Requisitos Previos

### Software necesario

1. **Java 21** o superior
   - Verificar instalación: `java -version`
   - Descargar: https://adoptium.net/

2. **Maven 3.9** o superior
   - Verificar instalación: `mvn -version`
   - Descargar: https://maven.apache.org/

3. **Git** (para clonar el repositorio)
   - Verificar instalación: `git --version`

4. **Docker y Docker Compose** (opcional, para ejecutar con PostgreSQL)
   - Verificar instalación: `docker --version`

---

## Paso 1: Clonar el Repositorio

```bash
# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>
cd OAuth2Server
```

---

## Paso 2: Configuración de la Base de Datos

La aplicación usa **PostgreSQL** en todos los entornos (desarrollo y producción).

### Opción A: Docker Compose (Recomendado para desarrollo)

```bash
# Ejecutar PostgreSQL y OAuth2Server
docker-compose up --build
```

Esto iniciara:
- PostgreSQL en el puerto 5432
- OAuth2Server en el puerto 8080

### Opción B: PostgreSQL local

1. Instala PostgreSQL
2. Crea una base de datos:

```bash
createdb oauth2_dev
```

3. Configura las credenciales en `application-dev.properties`:

```properties
spring.datasource.url=jdbclocalhost:5432:postgresql:///oauth2_dev
spring.datasource.username=oauth2_user
spring.datasource.password=oauth2_dev_password
```

---

## Paso 3: Compilar el Proyecto

```bash
# Compilar el proyecto (sin tests)
mvn clean compile

# Compilar con tests
mvn clean verify
```

---

## Paso 4: Ejecutar el Servidor

### Opción A: Desde Maven

```bash
# Ejecutar en modo desarrollo
mvn spring-boot:run
```

### Opción B: Ejecutar JAR directamente

```bash
# Primero compilar el JAR
mvn clean package -DskipTests

# Ejecutar el JAR
java -jar target/oauth2server-0.0.1-SNAPSHOT.jar
```

El servidor se ejecutará en: **http://localhost:8080**

---

## Paso 5: Verificar que el Servidor está Activo

### Verificar en navegador

1. Abrir: http://localhost:8080/login
2. Debes ver la página de login

### Verificar con curl

```bash
# Verificar que el servidor responde
curl -s http://localhost:8080/login | head -20
```

---

## Paso 6: Credenciales por Defecto

Al iniciar por primera vez, se crea un usuario administrador:

| Campo | Valor |
|-------|-------|
| Usuario | admin |
| Contraseña | admin123 |

---

## Paso 7: Obtener Token de Acceso

### Flujo Authorization Code + PKCE

#### 1. Iniciar flujo de autorización

```bash
# Redirigir al usuario a esta URL
http://localhost:8080/oauth2/authorize?
    response_type=code&
    client_id=mi-ejemplo-app&
    redirect_uri=http://localhost:3000/callback&
    scope=openid%20profile%20read%20write&
    code_challenge=...&
    code_challenge_method=S256
```

#### 2. Después del login, obtener token

```bash
curl -v -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "mi-ejemplo-app:mi-ejemplo-secreto" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:3000/callback" \
  -d "code_verifier=CODE_VERIFIER" \
  http://localhost:8080/oauth2/token
```

### Flujo Client Credentials (M2M)

```bash
# Obtener token para máquina-a-máquina
curl -v -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "mi-ejemplo-app:mi-ejemplo-secreto" \
  -d "grant_type=client_credentials" \
  -d "scope=read write" \
  http://localhost:8080/oauth2/token
```

---

## Integración PKCE en Cliente

PKCE (Proof Key for Code Exchange) es una extensión del flujo Authorization Code que añade seguridad adicional. Es obligatorio para aplicaciones públicas (SPAs, móviles).

### Flujo PKCE Paso a Paso

#### Paso 1: Generar Code Verifier y Code Challenge

```javascript
// Generador de code_verifier (string aleatorio de 43-128 caracteres)
function generateCodeVerifier() {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return base64URLEncode(array);
}

// Generar code_challenge a partir del code_verifier
async function generateCodeChallenge(verifier) {
    const encoder = new TextEncoder();
    const data = encoder.encode(verifier);
    const digest = await crypto.subtle.digest('SHA-256', data);
    return base64URLEncode(new Uint8Array(digest));
}

// Helper para codificar en base64url
function base64URLEncode(buffer) {
    return btoa(String.fromCharCode(...new Uint8Array(buffer)))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}
```

#### Paso 2: Redirigir al Servidor de Autorización

```javascript
const authUrl = new URL('http://localhost:8080/oauth2/authorize');
authUrl.searchParams.set('response_type', 'code');
authUrl.searchParams.set('client_id', 'mi-ejemplo-app');
authUrl.searchParams.set('redirect_uri', 'http://localhost:3000/callback');
authUrl.searchParams.set('scope', 'openid profile read write');
authUrl.searchParams.set('code_challenge', codeChallenge);
authUrl.searchParams.set('code_challenge_method', 'S256');
authUrl.searchParams.set('state', generateRandomState());
window.location.href = authUrl.toString();
```

#### Paso 3: Intercambiar Código por Token

```javascript
async function exchangeCodeForToken(code, codeVerifier) {
    const response = await fetch('http://localhost:8080/oauth2/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Basic ' + btoa('mi-ejemplo-app:mi-ejemplo-secreto')
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: 'http://localhost:3000/callback',
            code_verifier: codeVerifier
        })
    });
    return response.json();
}
```

#### Ejemplo Completo HTML

```html
<!DOCTYPE html>
<html>
<head><title>OAuth2 PKCE Login</title></head>
<body>
    <button id="loginBtn">Iniciar Sesión</button>
    <pre id="result"></pre>
    
    <script>
        const CLIENT_ID = 'mi-ejemplo-app';
        const CLIENT_SECRET = 'mi-ejemplo-secreto';
        const REDIRECT_URI = 'http://localhost:3000/callback';
        const AUTH_SERVER = 'http://localhost:8080';
        
        let codeVerifier = null;
        
        document.getElementById('loginBtn').addEventListener('click', startLogin);
        
        async function startLogin() {
            codeVerifier = generateCodeVerifier();
            const codeChallenge = await generateCodeChallenge(codeVerifier);
            sessionStorage.setItem('codeVerifier', codeVerifier);
            
            const authUrl = new URL(`${AUTH_SERVER}/oauth2/authorize`);
            authUrl.searchParams.set('response_type', 'code');
            authUrl.searchParams.set('client_id', CLIENT_ID);
            authUrl.searchParams.set('redirect_uri', REDIRECT_URI);
            authUrl.searchParams.set('scope', 'openid profile read write');
            authUrl.searchParams.set('code_challenge', codeChallenge);
            authUrl.searchParams.set('code_challenge_method', 'S256');
            authUrl.searchParams.set('state', Math.random().toString(36).substring(2));
            
            window.location.href = authUrl.toString();
        }
        
        if (window.location.search.includes('code=')) {
            const urlParams = new URLSearchParams(window.location.search);
            const code = urlParams.get('code');
            codeVerifier = sessionStorage.getItem('codeVerifier');
            if (code && codeVerifier) exchangeCodeForToken(code, codeVerifier);
        }
        
        async function exchangeCodeForToken(code, codeVerifier) {
            const response = await fetch(`${AUTH_SERVER}/oauth2/token`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Authorization': 'Basic ' + btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
                },
                body: new URLSearchParams({
                    grant_type: 'authorization_code',
                    code: code,
                    redirect_uri: REDIRECT_URI,
                    code_verifier: codeVerifier
                })
            });
            const tokens = await response.json();
            document.getElementById('result').textContent = JSON.stringify(tokens, null, 2);
            localStorage.setItem('accessToken', tokens.access_token);
        }
        
        function generateCodeVerifier() {
            const array = new Uint8Array(32);
            crypto.getRandomValues(array);
            return btoa(String.fromCharCode(...array)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
        }
        
        async function generateCodeChallenge(verifier) {
            const encoder = new TextEncoder();
            const data = encoder.encode(verifier);
            const digest = await crypto.subtle.digest('SHA-256', data);
            return btoa(String.fromCharCode(...new Uint8Array(digest))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
        }
    </script>
</body>
</html>
```

---

## Configuración de Aplicaciones

### Estructura de Base de Datos

El servidor usa la tabla `USUARIOS` con el campo `app` para distinguir aplicaciones:

| Campo | Descripción |
|-------|-------------|
| username | Nombre de usuario |
| password | Contraseña encriptada (BCrypt) |
| app | Identificador de aplicación |
| roles | Roles del usuario (USER, ADMIN) |
| fullName | Nombre completo |
| email | Correo electrónico |

### Clientes OAuth2 por Defecto

La aplicación viene con dos clientes de ejemplo configurados en `application-dev.properties`:

| client_id | redirect_uri |
|-----------|--------------|
| mi-ejemplo-app | http://localhost:3000/callback |
| mi-segunda-app | http://localhost:9000/callback |

### Agregar Nuevo Cliente

Para agregar un nuevo cliente, modifica `application-dev.properties`:

```properties
# Nuevo cliente
oauth2.clients[2].client-id=mi-nueva-app
oauth2.clients[2].client-secret=mi-nueva-secreto
oauth2.clients[2].redirect-uris[0]=http://localhost:4000/callback
oauth2.clients[2].scopes[0]=openid
oauth2.clients[2].scopes[1]=profile
oauth2.clients[2].scopes[2]=read
oauth2.clients[2].authorization-grant-types=authorization_code,client_credentials,refresh_token
```

---

## Configuración de Perfiles

### Perfil Dev (desarrollo)

Usa PostgreSQL (configurado en docker-compose):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Perfil Prod (producción)

Requiere PostgreSQL configurado y variables de entorno:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Configurar variables de entorno:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/oauth2_prod
SPRING_DATASOURCE_USERNAME=oauth2_user
SPRING_DATASOURCE_PASSWORD=tu_password
```

---

## Añadir Nuevos Usuarios

### Mediante API REST

```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "contraseña123",
    "email": "juan@ejemplo.com",
    "app": "mi-ejemplo-app",
    "role": "USER"
  }'
```

---

## Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn verify

# Ver reporte de cobertura
# Abrir: target/site/jacoco/index.html
```

---

## Endpoints Disponibles

| Endpoint | Método | Descripción | Auth |
|----------|--------|-------------|------|
| /oauth2/authorize | GET | Iniciar autorización | No |
| /oauth2/token | POST | Obtener token | Sí (client_id:secret) |
| /login | GET | Página de login | No |
| /user | POST | Crear usuario | No |
| /user/me | GET | Info usuario actual | Sí |
| /swagger-ui | GET | Documentación API | ADMIN |

---

## Swagger UI

Disponible en: **http://localhost:8080/swagger-ui/index.html**

Requiere autenticación con rol ADMIN.

---

## Solución de Problemas

### Error: "The dependencies of some of the beans form a cycle"

Este error indica una dependencia circular entre beans de Spring. Para resolverlo:

1. Verifica que el bean `RequestCache` esté definido en una clase de configuración separada (`RequestCacheConfig.java`)
2. Asegúrate de que `SecurityConfig` no defina el bean `RequestCache` directamente
3. Ejecuta `mvn clean install` para recompilar

### Error: "Port 8080 already in use"

```bash
# Encontrar proceso usando el puerto
lsof -i :8080

# Matar proceso
kill -9 <PID>
```

### Error: "Database not found"

```bash
# Verificar que la base de datos PostgreSQL existe
# Crear base de datos manualmente si es necesario
createdb oauth2_dev
```

### Error: "Could not resolve placeholder"

Verificar que el perfil está configurado correctamente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ver logs en tiempo real

```bash
# Linux/Mac
tail -f server.log

# Windows (PowerShell)
Get-Content server.log -Wait
```

---

## Estructura del Proyecto

```
OAuth2Server/
├── src/main/
│   ├── java/com/oauth/rest/
│   │   ├── config/          # Configuración
│   │   ├── controller/     # Controladores REST
│   │   ├── dto/            # Objetos de transferencia
│   │   ├── exception/      # Excepciones
│   │   ├── mapper/         # Mapeadores
│   │   ├── model/         # Entidades
│   │   ├── repository/    # Repositorios JPA
│   │   ├── security/      # Seguridad
│   │   │   ├── RequestCacheConfig.java      # Bean RequestCache
│   │   │   ├── SecurityConfig.java          # Configuración principal
│   │   │   ├── AppAwareAuthenticationProvider.java
│   │   │   ├── PasswordEncoderConfig.java
│   │   │   └── oauth2/    # Componentes OAuth2
│   │   │       ├── OAuth2AuthorizationServer.java
│   │   │       └── ...
│   │   └── service/       # Servicios
│   └── resources/
│       ├── application-*.properties
│       ├── db/migration/   # Flyway migrations
│       └── templates/      # Plantillas Thymeleaf
├── src/test/              # Tests
├── pom.xml
├── docker-compose.yml     # Contenedores Docker
└── MANUAL.md             # Este archivo
```

---

## Próximos Pasos

1. **Para producción**: Configurar PostgreSQL y HTTPS
2. **Registrar nuevas aplicaciones**: Añadir clientes en `application-dev.properties`
3. **Crear usuarios**: Usar el endpoint `/user` o directamente en la base de datos
