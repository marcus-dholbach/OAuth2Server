# 📚 Documentación de Endpoints – OAuth2Server

OAuth2Server expone endpoints para autenticación OAuth2, emisión de tokens JWT y gestión básica de usuarios.  
Todos los endpoints siguen el estándar OAuth2 y devuelven respuestas en formato **JSON**.

---

# 🔐 1. Endpoints OAuth2

## 1.1. `/oauth2/authorize` – Autorización

Endpoint para iniciar el flujo de Authorization Code con PKCE.

### Método
```
GET /oauth2/authorize
```

### Parámetros
- `response_type`: Debe ser `"code"`
- `client_id`: ID del cliente (ej: `mi-ejemplo-app`)
- `redirect_uri`: URI de callback (ej: `http://localhost:3000/callback`)
- `scope`: scopes separados por espacio (ej: `openid profile read write`)
- `code_challenge`: Challenge de PKCE
- `code_challenge_method`: Método de verificación (`S256`)

### Ejemplo
```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=mi-ejemplo-app&
  redirect_uri=http://localhost:3000/callback&
  scope=openid%20profile%20read%20write&
  code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&
  code_challenge_method=S256
```

### Respuesta
- Si el usuario no está autenticado: Redirige a `/login`
- Si está autenticado: Muestra pantalla de consentimiento
- Después del consentimiento: Redirige al callback con el código

```
http://localhost:3000/callback?code=xxx
```

---

## 1.2. `/oauth2/token` – Obtener token

Endpoint para obtener tokens JWT.

### Método
```
POST /oauth2/token
```

### Headers
```
Authorization: Basic base64(client_id:client_secret)
Content-Type: application/x-www-form-urlencoded
```

---

### 🔹 A) Authorization Code + PKCE

Canjea el código de autorización por tokens.

### Request
```bash
curl -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "mi-ejemplo-app:mi-ejemplo-secreto" \
  -d "grant_type=authorization_code" \
  -d "code=CODIGO_RECIBIDO" \
  -d "redirect_uri=http://localhost:3000/callback" \
  -d "code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk" \
  http://localhost:8080/oauth2/token
```

### Response
```json
{
  "access_token": "eyJraWQiOi...",
  "id_token": "eyJraWQiOi...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "refresh_token": "xxx",
  "scope": "openid profile read write"
}
```

---

### 🔹 B) Client Credentials (M2M)

Para aplicaciones Machine-to-Machine.

### Request
```bash
curl -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "mi-ejemplo-app:mi-ejemplo-secreto" \
  -d "grant_type=client_credentials" \
  -d "scope=read write" \
  http://localhost:8080/oauth2/token
```

### Response
```json
{
  "access_token": "eyJraWQiOi...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "scope": "read write"
}
```

---

# 🧪 2. Endpoints de Usuario

Los endpoints de usuario están protegidos por **Bearer Token**.  
Requieren incluir:

```
Authorization: Bearer <ACCESS_TOKEN>
```

---

## 2.1. `GET /user/me` – Obtener usuario actual

### Request
```bash
curl -X GET \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/user/me
```

### Response
```json
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

---

## 2.2. `POST /user` – Crear usuario

### Request
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "username": "nuevo",
        "password": "1234",
        "email": "nuevo@ejemplo.com",
        "app": "mi-ejemplo-app",
        "role": "USER"
      }' \
  http://localhost:8080/user
```

### Response
```json
{
  "id": 3,
  "username": "nuevo",
  "role": "USER"
}
```

---

## 2.3. `GET /login` – Página de login

Página de login para usuarios.

### Request
```bash
curl -X GET http://localhost:8080/login
```

### Response
Página HTML con formulario de login.

---

# 🔒 3. Seguridad y Roles

El sistema define dos roles:

- `ADMIN`
- `USER`

### Permisos por defecto:

| Endpoint | USER | ADMIN |
|----------|------|-------|
| `/oauth2/authorize` | ✔️ | ✔️ |
| `/oauth2/token` | ✔️ | ✔️ |
| `/login` | ✔️ | ✔️ |
| `GET /user/me` | ✔️ | ✔️ |
| `POST /user` | ✔️ | ✔️ |

---

# 🧾 4. Errores comunes

### Token inválido
```json
{
  "error": "invalid_token",
  "error_description": "JWT expired"
}
```

### Código inválido
```json
{
  "error": "invalid_grant",
  "error_description": "Invalid authorization code"
}
```

### Credenciales incorrectas
```json
{
  "error": "invalid_grant",
  "error_description": "Bad credentials"
}
```

---

# 🧭 5. Swagger UI

El proyecto incluye documentación interactiva:

```
http://localhost:8080/swagger-ui/index.html
```

---

# 🎯 6. Resumen

OAuth2Server proporciona:

- **Authorization Code + PKCE** para aplicaciones web/móviles  
- **Client Credentials** para M2M  
- Emisión de JWT firmados con RSA  
- Gestión de usuarios  
- Seguridad basada en roles  
- Integración lista para microservicios  
- Despliegue completo en Docker/Kubernetes
