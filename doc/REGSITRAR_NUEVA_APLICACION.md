# 📋 **PASO A PASO: Registrar una nueva aplicación en OAuth2Server**

## **Escenario: Quieres añadir "nueva-app" que corre en http://localhost:6000**

---

## ✅ **Paso 1: Identificar los datos de la nueva aplicación**

| Dato | Ejemplo |
|------|---------|
| **client_id** | `nueva-app` |
| **client_secret** | `nueva-app-secret` (o el que elijas) |
| **redirect_uri** | `http://localhost:6000/oauth/callback` |
| **scopes** | `openid profile read write` (o los que necesite) |
| **Puerto** | `6000` |

---

## ✅ **Paso 2: Modificar `application-dev.properties`**

Añade un nuevo bloque al final de la sección de clientes:

```properties
# ============================
# 📋 MÚLTIPLES CLIENTES OAuth2 (Dinámicos)
# ============================

# Cliente existente - proveedor-oauth
oauth2.clients[0].client-id=proveedor-oauth
oauth2.clients[0].client-secret=123456
oauth2.clients[0].redirect-uris[0]=http://localhost:8080/callback
oauth2.clients[0].redirect-uris[1]=http://localhost:5000/oauth/callback
oauth2.clients[0].scopes[0]=openid
oauth2.clients[0].scopes[1]=profile
oauth2.clients[0].scopes[2]=read
oauth2.clients[0].scopes[3]=write
oauth2.clients[0].require-consent=true
oauth2.clients[0].require-proof-key=false

# Cliente existente - cine-platform
oauth2.clients[1].client-id=cine-platform
oauth2.clients[1].client-secret=cine-platform-secret
oauth2.clients[1].redirect-uris[0]=http://localhost:3000/callback
oauth2.clients[1].scopes[0]=openid
oauth2.clients[1].scopes[1]=profile
oauth2.clients[1].scopes[2]=read
oauth2.clients[1].scopes[3]=write
oauth2.clients[1].require-consent=true
oauth2.clients[1].require-proof-key=false

# ============================
# 🆕 NUEVA APLICACIÓN
# ============================
oauth2.clients[2].client-id=nueva-app
oauth2.clients[2].client-secret=nueva-app-secret
oauth2.clients[2].redirect-uris[0]=http://localhost:6000/oauth/callback
oauth2.clients[2].scopes[0]=openid
oauth2.clients[2].scopes[1]=profile
oauth2.clients[2].scopes[2]=read
oauth2.clients[2].scopes[3]=write
oauth2.clients[2].require-consent=true
oauth2.clients[2].require-proof-key=false
```

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

## ✅ **Paso 4: Verificar que el cliente se cargó correctamente**

1. **Reinicia OAuth2Server**
2. **Busca en los logs** al arrancar:

```
=== CLIENTES REGISTRADOS ===
Cliente: proveedor-oauth -> redirectUri: http://localhost:5000/oauth/callback
Cliente: cine-platform -> redirectUri: http://localhost:3000/callback
Cliente: nueva-app -> redirectUri: http://localhost:6000/oauth/callback
```

---

## ✅ **Paso 5: Configurar la nueva aplicación cliente**

En la aplicación `nueva-app`, necesitas configurar:

### **Variables de entorno (`.env` de la app)**

```properties
# ============================
# 🔑 OAuth2 Server
# ============================
OAUTH2_URL=http://localhost:8080
OAUTH2_CLIENT_ID=nueva-app
OAUTH2_CLIENT_SECRET=nueva-app-secret
OAUTH2_AUTHORIZE_ENDPOINT=/oauth2/authorize
OAUTH2_TOKEN_ENDPOINT=/oauth/token
OAUTH2_USERINFO_ENDPOINT=/user/me
OAUTH2_REDIRECT_URI=http://localhost:6000/oauth/callback
```

### **En el código de la app (si usas el mismo cliente Python)**

```python
from oauth_client import OAuth2Client

# Inicializar cliente
oauth = OAuth2Client()
# Las variables de entorno ya configuran todo automáticamente
```

---

## ✅ **Paso 6: Probar el flujo completo**

### **1. Probar la URL de autorización manualmente**

```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=nueva-app&
  redirect_uri=http://localhost:6000/oauth/callback&
  scope=openid profile read write&
  state=test123
```

Deberías ver la página de login.

### **2. Login con admin/Admin1**

### **3. Redirección esperada**

```
http://localhost:6000/oauth/callback?code=XXX&state=test123
```

---

## ✅ **Paso 7: Manejar el callback en la nueva app**

Si la app no tiene implementado el callback, necesitas añadir un endpoint:

```python
# En tu app Flask/FastAPI/etc.
@app.route('/oauth/callback')
def oauth_callback():
    code = request.args.get('code')
    state = request.args.get('state')
    
    # Verificar state (CSRF)
    saved_state = session.get('oauth_state')
    if state != saved_state:
        return "Error: State mismatch", 400
    
    # Canjear código
    oauth_client = OAuth2Client()
    success, token_data = oauth_client.exchange_code_for_token(code)
    
    if success:
        session['access_token'] = token_data['access_token']
        session['logged_in'] = True
        return redirect('/')
    else:
        return f"Error: {token_data}", 400
```

---

## ✅ **Paso 8: Para producción (PKCE)**

Si quieres activar PKCE:

### **En el servidor**
```properties
oauth2.clients[2].require-proof-key=true
```

### **En el cliente**
Asegura que el cliente genera y envía `code_challenge` y `code_verifier` (el cliente Python ya lo soporta).

---

## 🎯 **Resumen rápido**

| Paso | Acción |
|------|--------|
| 1 | Elegir `client_id` y `client_secret` |
| 2 | Añadir bloque en `application-dev.properties` |
| 3 | Reiniciar OAuth2Server |
| 4 | Configurar variables de entorno en la nueva app |
| 5 | Probar con `/oauth2/authorize` |
| 6 | Implementar callback si no existe |

---

## ⚠️ **Notas importantes**

- El **índice** (`[2]`) debe ser único y consecutivo
- El `redirect_uri` debe coincidir **exactamente** (incluyendo `/` al final o no)
- Si la app corre en otro dominio en producción, actualiza `redirect_uris` en el perfil `prod`
