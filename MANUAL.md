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

---

## Paso 1: Clonar el Repositorio

```bash
# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>
cd OAuth2Server
```

---

## Paso 2: Configurar Variables de Entorno (Opcional)

Si deseas usar variables de entorno en lugar de archivos de propiedades:

```bash
# Linux/Mac
export SPRING_PROFILES_ACTIVE=dev

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="dev"
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
java -jar target/OAuth2Server-0.0.1-SNAPSHOT.jar
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

## Paso 6: Obtener Token de Acceso

### Flujo Authorization Code + PKCE

#### 1. Iniciar flujo de autorización

```bash
# Redirigir al usuario a esta URL
http://localhost:8080/oauth2/authorize?
    response_type=code&
    client_id=proveedor-oauth&
    redirect_uri=http://localhost:8080/callback&
    scope=openid%20profile%20read%20write&
    code_challenge=...&
    code_challenge_method=S256
```

#### 2. Después del login, obtener token

```bash
curl -v -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "proveedor-oauth:123456" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:8080/callback" \
  -d "code_verifier=CODE_VERIFIER" \
  http://localhost:8080/oauth2/token
```

### Flujo Client Credentials (M2M)

```bash
# Obtener token para máquina-a-máquina
curl -v -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "proveedor-oauth:123456" \
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
authUrl.searchParams.set('client_id', 'cine-platform');
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
            'Authorization': 'Basic ' + btoa('cine-platform:cine-platform-secret')
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
        const CLIENT_ID = 'cine-platform';
        const CLIENT_SECRET = 'cine-platform-secret';
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

-- Ejemplo de datos en la tabla usuarios

| username | app           | roles          |
|----------|---------------|----------------|
| admin    | cine-platform | USER,ADMIN     |
| admin    | cine-admin    | ADMIN          |
| usuario  | cine-platform | USER           |

### Estructura de Base de Datos

El servidor usa la tabla `USUARIOS` con el campo `app` para distinguir aplicaciones:

| Campo | Descripción |
|-------|-------------|
| username | Nombre de usuario |
| password | Contraseña encriptada (BCrypt) |
| app | Identificador de aplicación (cine-platform, cine-admin, etc.) |
| roles | Roles del usuario (USER, ADMIN) |
| fullName | Nombre completo |
| email | Correo electrónico |

### Clientes OAuth2 Registrados

| client_id | client_secret | redirect_uri | scopes |
|-----------|---------------|--------------|--------|
| proveedor-oauth | 123456 | http://localhost:8080/callback | openid, profile, read, write |
| cine-platform | cine-platform-secret | http://localhost:3000/callback | openid, profile, read, write |
| cine-admin | cine-admin-secret | http://localhost:4000/callback | openid, profile, admin:users, admin:roles |
| otra-app | otra-app-secret | http://localhost:5000/callback | openid, profile |

### Agregar Nuevo Cliente

Para agregar un nuevo cliente, modifica [`OAuth2AuthorizationServer.java`](src/main/java/com/oauth/rest/security/oauth2/OAuth2AuthorizationServer.java):

```java
// Agregar nuevo cliente
RegisteredClient nuevoCliente = RegisteredClient.withId(UUID.randomUUID().toString())
    .clientId("nuevo-cliente")
    .clientSecret(passwordEncoder.encode("secreto"))
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
    .clientSettings(ClientSettings.builder()
        .requireAuthorizationConsent(true)
        .requireProofKey(true)
        .build())
    .tokenSettings(TokenSettings.builder()
        .accessTokenTimeToLive(Duration.ofSeconds(3600))
        .refreshTokenTimeToLive(Duration.ofSeconds(86400))
        .build())
    .scope("openid")
    .scope("profile")
    .redirectUri("http://tu-app.com/callback")
    .build();
```

---

## Configuración de Perfiles

### Perfil Dev (desarrollo)

Usa H2 en memoria (no persiste datos):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Perfil Prod (producción)

Requiere PostgreSQL configurado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Configurar en [`application-prod.properties`](src/main/resources/application-prod.properties):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/oauth2db
spring.datasource.username=postgres
spring.datasource.password=tu_password
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
| /h2-console | GET | Consola H2 | No (solo dev) |

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
# Verificar que la base de datos existe
# Para H2: se crea automáticamente
# Para PostgreSQL: crear base de datos manualmente
createdb oauth2db
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
│   │   │       ├── OAuth2SavedRequestAwareAuthSuccessHandler.java
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
2. **Para múltiples apps**: Usar el campo `app` en la tabla `USUARIOS`
3. **Para CI/CD**: Ver scripts en [`k8s/`](k8s/) para Kubernetes

---

## Contacto y Soporte

Para reportar problemas o sugerir mejoras, crear un issue en el repositorio de GitHub.
