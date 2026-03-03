# 📋 **PASO A PASO: Registrar una nueva aplicación en OAuth2Server**

## **Escenario: Quieres añadir una nueva aplicación que corre en http://localhost:6000**

---

## ✅ **Paso 1: Identificar los datos de la nueva aplicación**

| Dato | Ejemplo |
|------|---------|
| **client_id** | `mi-nueva-app` |
| **client_secret** | `mi-nueva-secreto` |
| **redirect_uri** | `http://localhost:6000/oauth/callback` |
| **scopes** | `openid profile read write` |
| **Puerto** | `6000` |

---

## ✅ **Paso 2: Modificar `application-dev.properties`**

Edita el archivo `src/main/resources/application-dev.properties` y añade un nuevo bloque:

```properties
# ============================
# 🆕 NUEVA APLICACIÓN
# ============================
oauth2.clients[2].client-id=mi-nueva-app
oauth2.clients[2].client-secret=mi-nueva-secreto
oauth2.clients[2].redirect-uris[0]=http://localhost:6000/oauth/callback
oauth2.clients[2].scopes[0]=openid
oauth2.clients[2].scopes[1]=profile
oauth2.clients[2].scopes[2]=read
oauth2.clients[2].scopes[3]=write
oauth2.clients[2].require-consent=true
oauth2.clients[2].require-proof-key=false
oauth2.clients[2].authorization-grant-types=authorization_code,client_credentials,refresh_token
```

### Explicación de campos

| Campo | Descripción |
|-------|-------------|
| `client-id` | Identificador único de tu aplicación |
| `client-secret` | Contraseña secreta (no compartir) |
| `redirect-uris` | URL donde OAuth2 devolverá al usuario |
| `scopes` | Permisos que solicita la app |
| `authorization-grant-types` | Tipos de flujo OAuth2 soportados |

---

## ✅ **Paso 3: (Opcional) Si la app necesita scopes específicos**

```properties
# Ejemplo con scopes personalizados
oauth2.clients[2].scopes[0]=openid
oauth2.clients[2].scopes[1]=profile
oauth2.clients[2].scopes[2]=custom:read
oauth2.clients[2].scopes[3]=custom:write
oauth2.clients[2].scopes[4]=admin:users
```

---

## ✅ **Paso 4: Reiniciar OAuth2Server**

Reinicia el servidor para que cargue la nueva configuración:

```bash
# Si usas Docker
docker-compose down
docker-compose up --build

# Si ejecutas directamente
java -jar target/oauth2server-0.0.1-SNAPSHOT.jar
```

---

## ✅ **Paso 5: Probar el flujo completo**

### 1. Probar la URL de autorización manualmente

```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=mi-nueva-app&
  redirect_uri=http://localhost:6000/oauth/callback&
  scope=openid%20profile%20read%20write&
  state=test123
```

Deberías ver la página de login.

### 2. Login con credenciales por defecto

- Usuario: `admin`
- Contraseña: `admin123`

### 3. Redirección esperada

```
http://localhost:6000/oauth/callback?code=XXX&state=test123
```

---

## ✅ **Paso 6: Obtener el token**

Canjea el código por un token:

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u "mi-nueva-app:mi-nueva-secreto" \
  -d "grant_type=authorization_code" \
  -d "code=CODIGO_RECIBIDO" \
  -d "redirect_uri=http://localhost:6000/oauth/callback"
```

---

## ✅ **Paso 7: Para producción**

En `application-prod.properties`, añade las variables de entorno:

```bash
# Variables de entorno para producción
MI_NUEVA_APP_SECRET=tu-secreto-produccion
MI_NUEVA_APP_REDIRECT_URI=https://tu-dominio.com/callback
```

O en el archivo de propiedades:

```properties
oauth2.clients[2].client-secret=${MI_NUEVA_APP_SECRET}
oauth2.clients[2].redirect-uris[0]=${MI_NUEVA_APP_REDIRECT_URI}
```

---

## 🎯 **Resumen rápido**

| Paso | Acción |
|------|--------|
| 1 | Elegir `client_id` y `client_secret` |
| 2 | Añadir bloque en `application-dev.properties` |
| 3 | Reiniciar OAuth2Server |
| 4 | Probar con `/oauth2/authorize` |
| 5 | Canjear código por token |

---

## ⚠️ **Notas importantes**

- El **índice** (`[2]`) debe ser único y consecutivo
- El `redirect_uri` debe coincidir **exactamente** (incluyendo `/` al final o no)
- Si la app corre en otro dominio en producción, actualiza `redirect_uris` en el perfil `prod`
- Usa valores diferentes para desarrollo y producción
