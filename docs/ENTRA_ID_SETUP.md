# Configuración de Microsoft Entra ID para Pedidos360

Esta guía configura autenticación y autorización para:

- Una SPA Angular que inicia sesión con MSAL mediante Authorization Code + PKCE.
- El BFF Spring Boot, que valida access tokens como OAuth 2.0 Resource Server.
- Un permiso delegado `access_as_user`.
- Roles de aplicación `USER` y `ADMIN` emitidos en el access token.

La configuración recomendada usa dos registros de aplicación separados: uno representa la API/BFF y otro representa la SPA. No escriba literalmente los marcadores `<...>` ni invente valores; obtenga cada ID desde Microsoft Entra admin center.

## 1. Requisitos y datos que se deben registrar

Se necesita una cuenta con permisos para crear registros de aplicación, conceder consentimiento y asignar usuarios. Según las políticas del tenant, estas tareas pueden requerir un rol como Application Administrator, Cloud Application Administrator o Global Administrator.

Prepare una hoja de trabajo local, sin secretos, con estos datos:

| Dato | Origen en Entra ID | Marcador usado en esta guía |
|---|---|---|
| Tenant ID | Microsoft Entra ID > Overview > Tenant ID | `<TENANT_ID>` |
| SPA Client ID | Registro de la SPA > Overview > Application (client) ID | `<SPA_CLIENT_ID>` |
| API Client ID | Registro de la API > Overview > Application (client) ID | `<API_CLIENT_ID>` |
| Application ID URI de la API | Registro de la API > Expose an API | `<API_APP_ID_URI>` |

`Tenant ID`, `Application (client) ID` y `Object ID` son valores distintos. Para esta configuración se usan los Application (client) ID de ambos registros; no sustituya un Object ID.

## 2. Seleccionar o crear el tenant

1. Ingrese a [Microsoft Entra admin center](https://entra.microsoft.com/).
2. Cambie al directorio que alojará Pedidos360.
3. Abra Microsoft Entra ID > Overview.
4. Copie el valor Tenant ID y regístrelo como `<TENANT_ID>`.
5. Confirme que los usuarios de prueba existen en este mismo tenant. Si son invitados, deben haber aceptado previamente la invitación.

Para este proyecto se recomienda una aplicación de tenant único. En ambos registros seleccione **Accounts in this organizational directory only**. Esto evita aceptar emisores de tenants no configurados por el BFF.

## 3. Crear el registro de la API

1. Abra App registrations > New registration.
2. Use un nombre identificable, por ejemplo `Pedidos360 API`.
3. Seleccione **Accounts in this organizational directory only**.
4. No agregue una Redirect URI: la API no realiza inicio de sesión interactivo.
5. Complete el registro.
6. En Overview, copie Application (client) ID como `<API_CLIENT_ID>`.
7. Verifique que el access token solicitado para esta API sea versión 2. En Manifest, la propiedad equivalente debe quedar como `requestedAccessTokenVersion: 2`; según la versión del portal puede aparecer dentro del objeto `api`. No cambie otras propiedades del manifiesto.

La versión 2 es importante porque el BFF está preparado para validar el issuer de la plataforma v2.0.

### 3.1 Exponer la API y crear el scope

1. En el registro de la API, abra Expose an API.
2. Seleccione **Set** junto a Application ID URI.
3. Acepte el valor propuesto `api://<API_CLIENT_ID>` o defina otro URI válido y único.
4. Copie el valor final exactamente como `<API_APP_ID_URI>`; no lo reconstruya después de memoria.
5. Seleccione **Add a scope**.
6. Configure Scope name como `access_as_user`.
7. Mantenga State en **Enabled**.
8. Seleccione quién puede consentir. Para un entorno administrado se recomienda **Admins only** y conceder consentimiento administrativo en el paso 5. Si se elige **Admins and users**, una política del tenant todavía puede impedir el consentimiento de usuario.
9. Use textos descriptivos, por ejemplo “Acceder a Pedidos360 en nombre del usuario”, para Admin consent y User consent cuando corresponda.
10. Guarde el scope.

El identificador completo solicitado por Angular será:

```text
<API_APP_ID_URI>/access_as_user
```

Si se conservó el URI predeterminado, el valor tendrá esta forma:

```text
api://<API_CLIENT_ID>/access_as_user
```

### 3.2 Crear los app roles

En el mismo registro de la API, abra App roles y cree los siguientes roles. Los valores distinguen mayúsculas de minúsculas y deben coincidir exactamente con el BFF.

| Display name | Allowed member types | Value | Description | Enabled |
|---|---|---|---|---|
| Pedidos360 User | Users/Groups | `USER` | Consulta y crea OT e ítems | Sí |
| Pedidos360 Admin | Users/Groups | `ADMIN` | Accede además a eventos y notificaciones | Sí |

Los roles deben pertenecer al registro de la API, no al registro de la SPA. Así Entra ID los emite en el claim `roles` del access token destinado a la API.

## 4. Crear el registro de la SPA Angular

1. Abra App registrations > New registration.
2. Use un nombre identificable, por ejemplo `Pedidos360 SPA`.
3. Seleccione **Accounts in this organizational directory only**.
4. En Redirect URI, elija la plataforma **Single-page application (SPA)**.
5. Escriba exactamente `http://localhost:4200`.
6. Complete el registro.
7. En Overview, copie Application (client) ID como `<SPA_CLIENT_ID>`.
8. En Authentication, confirme que `http://localhost:4200` está bajo **Single-page application**, no bajo Web.
9. Si el portal ofrece Front-channel logout URL, no es necesario configurarlo para el `logoutRedirect` actual. El retorno posterior al cierre de sesión usa `http://localhost:4200`, que ya debe estar registrado como redirect URI.
10. En Implicit grant and hybrid flows, deje desmarcadas las opciones de Access tokens e ID tokens.

No cree un client secret para la SPA. Una aplicación ejecutada en el navegador no puede proteger secretos. MSAL Browser usa Authorization Code Flow con PKCE y el registro de plataforma SPA habilita el intercambio de código desde el navegador. No habilite implicit flow.

## 5. Conceder a la SPA el permiso delegado

1. En el registro `Pedidos360 SPA`, abra API permissions.
2. Seleccione Add a permission > My APIs.
3. Seleccione el registro `Pedidos360 API`.
4. Seleccione Delegated permissions.
5. Marque `access_as_user` y agregue el permiso.
6. Confirme que el tipo mostrado sea **Delegated**; no se requiere un Application permission para este flujo.
7. Si el scope requiere consentimiento administrativo, o si se desea evitar solicitudes individuales, seleccione **Grant admin consent for `<TENANT>`** y confirme.
8. Verifique que el estado indique consentimiento concedido para el tenant.

El consentimiento al scope y la asignación de app roles resuelven necesidades distintas:

- `scp` prueba que la SPA obtuvo el permiso delegado `access_as_user`.
- `roles` expresa el rol asignado al usuario para esta API.
- Conceder consentimiento no asigna `USER` ni `ADMIN` automáticamente.

## 6. Configurar Enterprise Applications y usuarios de prueba

Cada registro crea un service principal visible en Enterprise applications. Las asignaciones se realizan sobre esos service principals, no sobre App registrations.

### 6.1 Asignar roles en la aplicación empresarial de la API

1. Abra Enterprise applications > All applications.
2. Busque la aplicación empresarial correspondiente a `Pedidos360 API`. Verifique Application ID contra `<API_CLIENT_ID>` para no elegir la SPA.
3. Abra Properties.
4. Para un entorno de prueba controlado, establezca **Assignment required?** en Yes y guarde. Esto evita acceso de usuarios no asignados.
5. Abra Users and groups > Add user/group.
6. Seleccione un usuario de prueba estándar y asigne el rol `Pedidos360 User`/`USER`.
7. Seleccione otro usuario de prueba administrativo y asigne el rol `Pedidos360 Admin`/`ADMIN`.
8. Guarde y compruebe que cada asignación muestre el rol esperado.

Para demostrar separación de privilegios conviene usar dos cuentas diferentes. No asigne ambos roles al usuario estándar. Las asignaciones pueden tardar algunos minutos en reflejarse y requieren obtener un token nuevo.

### 6.2 Restringir opcionalmente el inicio de sesión en la SPA

1. Abra la aplicación empresarial correspondiente a `Pedidos360 SPA` y verifique Application ID contra `<SPA_CLIENT_ID>`.
2. Si se desea limitar quién puede iniciar sesión, establezca **Assignment required?** en Yes.
3. En Users and groups, agregue los dos usuarios de prueba con Default Access.

Si se activa Assignment required en la SPA y no se asignan usuarios, Entra ID bloqueará el login aunque esos usuarios tengan un rol en la API. Para un laboratorio abierto se puede dejar la SPA con Assignment required en No y mantener las asignaciones obligatorias solamente en la API.

## 7. Mapear los IDs a Pedidos360

Use este mapeo; no intercambie los Client ID:

| Valor de Entra | Configuración local |
|---|---|
| `<TENANT_ID>` | `environment.msal.tenantId`; segmento de `authority`, `ISSUER_URI` y `JWK_SET_URI` |
| `<SPA_CLIENT_ID>` | `environment.msal.clientId` |
| `<API_APP_ID_URI>/access_as_user` | `environment.msal.apiScope` |
| Claim `aud` real del access token | `API_AUDIENCE` |
| Origen Angular | `ALLOWED_ORIGINS=http://localhost:4200` |

El archivo `.env.example` también contiene `TENANT_ID` y `CLIENT_ID`, pero el `application.yml` actual del BFF no los consume directamente. Para la validación del BFF importan `ISSUER_URI`, `JWK_SET_URI` y `API_AUDIENCE`. No configure `CLIENT_ID` del BFF como si fuera un secreto: el BFF actual es un resource server y no realiza un flujo client credentials.

### 7.1 Configurar Angular

Actualice manualmente `frontend/pedidos360-web/src/environments/environment.ts` con valores reales:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  msal: {
    tenantId: '<TENANT_ID>',
    clientId: '<SPA_CLIENT_ID>',
    authority: 'https://login.microsoftonline.com/<TENANT_ID>',
    redirectUri: 'http://localhost:4200',
    apiScope: '<API_APP_ID_URI>/access_as_user',
  },
};
```

Si el URI predeterminado de la API es `api://<API_CLIENT_ID>`, `apiScope` será `api://<API_CLIENT_ID>/access_as_user`. El archivo `environment.production.ts` debe configurarse por separado cuando exista un dominio de despliegue; no reutilice `localhost` en producción.

La configuración existente de MSAL ya realiza lo siguiente:

- Usa `<SPA_CLIENT_ID>` como cliente público.
- Usa la autoridad tenant-specific.
- Ejecuta interacciones por redirect.
- Solicita el scope de la API durante login y adquisición silenciosa.
- Adjunta el bearer token a `http://localhost:8080/api/bff/*` mediante `MsalInterceptor`.
- Regresa a `http://localhost:4200` después del logout.

### 7.2 Calcular issuer y JWKS del BFF

Con `<TENANT_ID>` obtenido del portal y tokens v2, construya:

```text
ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
JWK_SET_URI=https://login.microsoftonline.com/<TENANT_ID>/discovery/v2.0/keys
```

Puede comprobar los valores publicados por Microsoft abriendo el documento de descubrimiento:

```text
https://login.microsoftonline.com/<TENANT_ID>/v2.0/.well-known/openid-configuration
```

Verifique que su campo `issuer` coincida exactamente con `ISSUER_URI` y que `jwks_uri` coincida con `JWK_SET_URI`. No descargue ni fije certificados individuales: el BFF debe usar JWKS para admitir rotación de claves.

### 7.3 Configurar variables del BFF

Configure el entorno desde el que se inicia `backend/bff-service`:

```dotenv
TENANT_ID=<TENANT_ID>
CLIENT_ID=<API_CLIENT_ID>
ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
JWK_SET_URI=https://login.microsoftonline.com/<TENANT_ID>/discovery/v2.0/keys
API_AUDIENCE=<VALOR_EXACTO_DEL_CLAIM_AUD>
ALLOWED_ORIGINS=http://localhost:4200
```

`TENANT_ID` y `CLIENT_ID` mantienen trazabilidad con `.env.example`; `CLIENT_ID` identifica aquí el registro de la API, pero el código actual del BFF no consume esas dos variables ni las usa en la validación. No sustituyen `ISSUER_URI`, `JWK_SET_URI` ni `API_AUDIENCE`.

**Advertencia crítica:** `API_AUDIENCE` debe ser exactamente el claim `aud` observado en un access token emitido para `access_as_user`. No asuma que siempre será `api://<API_CLIENT_ID>`: según la versión y configuración del token, Entra ID puede emitir el Application (client) ID de la API u otro identificador configurado. Copie el valor de `aud` sin el sufijo `/access_as_user`; `aud` identifica el recurso, no el scope.

## 8. Flujo de login, token y logout

El flujo esperado es:

1. El usuario abre `http://localhost:4200/login` y selecciona **Continuar con Microsoft**.
2. `loginRedirect` solicita `<API_APP_ID_URI>/access_as_user` y redirige al endpoint de autorización de Entra ID.
3. MSAL genera PKCE (`code_verifier` y `code_challenge`) y recibe un authorization code en la redirect URI registrada.
4. MSAL canjea el código desde la SPA sin client secret y procesa la respuesta al volver a `http://localhost:4200`.
5. `acquireTokenSilent` obtiene o renueva un access token para el mismo scope.
6. `MsalInterceptor` añade `Authorization: Bearer <access-token>` a solicitudes bajo `http://localhost:8080/api/bff/*`.
7. El BFF valida firma, expiración, issuer y audience, y convierte `roles` en autoridades `ROLE_USER`/`ROLE_ADMIN`.
8. `logoutRedirect` cierra la sesión de Entra y vuelve a `http://localhost:4200`.

No use el ID token para invocar el BFF. El ID token autentica al usuario ante la SPA; el access token autoriza acceso a la API y contiene `aud`, `scp` y, después de las asignaciones, `roles` relevantes para el BFF.

## 9. Obtener e inspeccionar el access token

La forma más segura para una comprobación local es usar las herramientas de desarrollo del navegador:

1. Abra DevTools > Network antes de entrar a Ordenes de trabajo o Auditoria.
2. Inicie sesión y seleccione una solicitud a `http://localhost:8080/api/bff/...`.
3. En Request Headers, identifique `Authorization: Bearer ...`.
4. Inspeccione el payload localmente o con una herramienta aprobada por la organización. Decodificar Base64URL permite leer claims, pero no valida firma ni confiabilidad.
5. No pegue tokens reales en tickets, chats, commits, capturas ni servicios públicos de decodificación.

En el access token, verifique:

| Claim | Resultado esperado |
|---|---|
| `iss` | Exactamente `https://login.microsoftonline.com/<TENANT_ID>/v2.0` |
| `aud` | Recurso real aceptado por la API; copie este valor exacto a `API_AUDIENCE` |
| `exp` | Timestamp Unix futuro; confirme que el equipo tenga fecha y hora correctas |
| `scp` | Cadena separada por espacios que incluya `access_as_user` |
| `roles` | Array que incluya `USER` o `ADMIN`, según la asignación del usuario |

También son útiles `tid` para confirmar el tenant, `azp` para confirmar la SPA que solicitó el token y `ver` para confirmar que el token sea v2.0. En un access token v2, `azp` normalmente debe corresponder a `<SPA_CLIENT_ID>`.

La pantalla de inicio de Pedidos360 decodifica el access token y muestra roles y scopes como ayuda visual. Esa decodificación del frontend no reemplaza la validación criptográfica que realiza el BFF.

Para obtener un token nuevo después de cambiar permisos o roles:

1. Cierre sesión desde la aplicación.
2. Cierre las sesiones de prueba adicionales si el navegador conserva varias cuentas.
3. Inicie sesión otra vez con la cuenta asignada.
4. Si persisten claims anteriores, use una ventana privada o elimine únicamente el almacenamiento local del origen `http://localhost:4200` y repita el login.

## 10. Pruebas de aceptación

Ejecute las pruebas con Angular en `http://localhost:4200` y el BFF en `http://localhost:8080`.

| Caso | Acción | Resultado esperado |
|---|---|---|
| Endpoint público | Abrir `http://localhost:8080/api/bff/health` sin token | HTTP 200 |
| Recurso sin token | Solicitar `GET /api/bff/ots` sin Authorization | HTTP 401 |
| Login USER | Iniciar sesión con el usuario asignado a `USER` | Login correcto; `scp` contiene `access_as_user`; `roles` contiene `USER` |
| Lectura USER | Abrir Ordenes de trabajo como `USER` | Las solicitudes GET protegidas no fallan por autorización |
| Auditoría USER | Intentar consultar eventos o notificaciones | HTTP 403 |
| Login ADMIN | Cerrar sesión e iniciar con el usuario asignado a `ADMIN` | El token nuevo contiene `ADMIN` |
| Auditoría ADMIN | Consultar eventos o notificaciones | No falla por autorización; el resultado funcional depende de los servicios backend |
| Perfil | Solicitar `GET /api/bff/me` autenticado | HTTP 200 y respuesta con roles/scopes del token |
| Audience incorrecto | Probar en un entorno controlado un `API_AUDIENCE` diferente | El BFF rechaza el token con HTTP 401 |
| Logout | Cerrar sesión desde la SPA | Redirección a Entra y retorno a `http://localhost:4200` sin cuenta activa |

Un HTTP 401 significa que el token falta o no supera autenticación/validación. Un HTTP 403 significa que el token fue aceptado, pero no contiene la autoridad requerida para la operación.

## 11. Troubleshooting

### Error de redirect URI o `AADSTS50011`

- Confirme coincidencia exacta con `http://localhost:4200`, incluidos esquema, puerto y ausencia de ruta adicional.
- Confirme que la URI está registrada como plataforma SPA, no Web.
- Confirme que Angular usa el Client ID de la SPA, no el de la API.

### Consentimiento requerido o permiso ausente

- Confirme que la SPA tiene el permiso delegado `access_as_user` de la API correcta.
- Confirme que el scope está Enabled.
- Conceda admin consent si la política del tenant impide consentimiento de usuario.
- Solicite exactamente `<API_APP_ID_URI>/access_as_user`.

### El token no contiene `roles`

- Confirme que `USER` y `ADMIN` se crearon como app roles en el registro de la API.
- Confirme que Allowed member types incluye Users/Groups.
- Confirme la asignación en Enterprise applications > `Pedidos360 API`, no en la SPA.
- Obtenga un access token nuevo después de la asignación.
- Confirme que está inspeccionando el access token de la API, no el ID token.

### El BFF responde 401 por issuer

- Compare `iss` del token carácter por carácter con `ISSUER_URI`.
- Confirme `<TENANT_ID>` y que el usuario inició sesión en el tenant esperado.
- Confirme `ver: "2.0"` y `requestedAccessTokenVersion: 2` en la API.
- Verifique `issuer` en el documento OpenID Connect de descubrimiento.

### El BFF responde 401 por audience

- Lea `aud` del access token real.
- Configure `API_AUDIENCE` con ese valor exacto.
- No use `<SPA_CLIENT_ID>` como audience y no agregue `/access_as_user`.
- Reinicie el BFF después de cambiar variables de entorno.

### El BFF responde 401 por firma, JWKS o expiración

- Compare `JWK_SET_URI` con `jwks_uri` del documento de descubrimiento.
- Compruebe acceso de red HTTPS desde el BFF hacia `login.microsoftonline.com`.
- Confirme que `exp` esté en el futuro y sincronice el reloj del host/contenedor.
- No reutilice un token vencido capturado previamente.

### El BFF responde 403

- Confirme que el token contiene `roles: ["USER"]` o `roles: ["ADMIN"]` según la operación.
- Recuerde que `scp: access_as_user` no reemplaza un app role.
- Para auditoría y notificaciones se requiere `ADMIN`; para consultar o crear OT e ítems se acepta `USER` o `ADMIN`.

### CORS bloquea la solicitud

- Confirme `ALLOWED_ORIGINS=http://localhost:4200` sin barra final.
- Confirme que Angular llama a `http://localhost:8080`.
- Distinga un error CORS del navegador de un 401/403 devuelto por el BFF usando la pestaña Network y los logs del BFF.

### Login bloqueado por asignación requerida

- Revise Assignment required en las dos Enterprise Applications.
- Si está activo en la SPA, asigne al usuario Default Access en la SPA.
- Si está activo en la API, asigne al usuario uno de los roles de la API.

## 12. Evidencias que se deben capturar

Capture evidencias suficientes para demostrar la configuración, ocultando datos sensibles:

1. Overview del tenant con Tenant ID parcialmente oculto.
2. Overview del registro de API con Application (client) ID parcialmente oculto.
3. Expose an API mostrando Application ID URI y `access_as_user`.
4. App roles mostrando `USER` y `ADMIN`, sus valores y tipos permitidos.
5. Overview del registro SPA con Application (client) ID parcialmente oculto.
6. Authentication de la SPA mostrando `http://localhost:4200` bajo plataforma SPA y sin implicit grants.
7. API permissions de la SPA mostrando `access_as_user`, tipo Delegated y estado de consentimiento.
8. Enterprise application de la API mostrando usuarios de prueba y roles asignados; oculte correos si la evidencia será pública.
9. Pantalla de Pedidos360 después del login mostrando el rol y el scope.
10. Network mostrando una llamada autenticada y su código HTTP, con el valor completo de Authorization oculto.
11. Payload decodificado mostrando solo `iss`, `aud`, `exp`, `scp`, `roles`, `tid`, `azp` y `ver`; oculte identificadores personales y valores que la organización considere sensibles.
12. Pruebas de autorización: un 403 para `USER` en una operación administrativa y éxito de autorización para `ADMIN`.

Nunca incluya el access token o ID token completo en una captura. Cubra completamente el valor después de `Bearer`, parámetros `code`, cookies, correlation IDs si la política lo exige, nombres/correos personales y cualquier secreto. Aunque un JWT sea de vida corta, es una credencial reutilizable hasta su expiración. No lo almacene en el repositorio ni en documentación compartida.

## 13. Acciones manuales pendientes

Este documento no crea recursos en el tenant ni incorpora IDs reales. Deben completarse manualmente estas acciones:

- Seleccionar el tenant y registrar `<TENANT_ID>`.
- Crear los dos registros recomendados, API y SPA.
- Registrar `http://localhost:4200` como redirect URI de tipo SPA.
- Configurar el Application ID URI y el scope `access_as_user`.
- Crear los app roles `USER` y `ADMIN` en la API.
- Agregar a la SPA el permiso delegado y conceder el consentimiento requerido.
- Configurar las Enterprise Applications y asignar usuarios de prueba.
- Sustituir los marcadores en `environment.ts` y, cuando corresponda, en `environment.production.ts`.
- Configurar las variables de entorno del BFF.
- Iniciar sesión, inspeccionar el access token y fijar `API_AUDIENCE` al `aud` realmente observado.
- Reiniciar las aplicaciones y ejecutar toda la matriz de pruebas.
- Capturar evidencias con tokens e información personal ocultos.
