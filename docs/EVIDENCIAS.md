# Checklist de evidencias de Pedidos360

Guardar cada captura en formato PNG con el nombre exacto indicado. Las evidencias de Entra ID y AWS requieren recursos reales; no deben simularse con capturas del código.

## Seguridad de las capturas

- Ocultar por completo access tokens, ID tokens, refresh tokens y el valor de `Authorization`.
- No mostrar contraseñas, `.env`, claves PEM, cookies, códigos de autorización ni credenciales AWS.
- Anonimizar correos, nombres, `oid`, `sub` y otros datos personales.
- Los claims funcionales `iss`, `aud`, `scp` y `roles` pueden mostrarse con el JWT original oculto.
- Verificar que cada imagen permita leer URL o comando, código HTTP y resultado esperado.

## Capturas obligatorias

- [ ] **`01_backend_compilando.png`**
  - Mostrar desde la raiz: `backend\mvnw.cmd -f backend\pom.xml clean verify` y el resumen de los tres servicios.
  - Aceptación: `BUILD SUCCESS`, módulos `SUCCESS` y 14 pruebas sin fallos.

- [ ] **`02_ot_service_health.png`**
  - Mostrar: `GET http://localhost:8081/actuator/health`.
  - Aceptación: HTTP `200` y `{"status":"UP"}`.

- [ ] **`03_audit_service_health.png`**
  - Mostrar: `GET http://localhost:8082/actuator/health`.
  - Aceptación: HTTP `200` y `{"status":"UP"}`.

- [ ] **`04_bff_health.png`**
  - Mostrar: `GET http://localhost:8080/api/bff/health` sin token.
  - Aceptación: HTTP `200`, `service: bff-service` y `status: UP`.

- [ ] **`05_ots_get.png`**
  - Mostrar: `GET http://localhost:8080/api/bff/ots` con token válido `USER` o `ADMIN`.
  - Aceptación: HTTP `200`, array JSON y URL del BFF, no el puerto `8081`.

- [ ] **`06_ots_post.png`**
  - Mostrar: `POST http://localhost:8080/api/bff/ots` con rol `USER` o `ADMIN` y body válido.
  - Aceptación: HTTP `201` y JSON con `id`, `clienteId`, `patente`, `descripcion` y `total`.

- [ ] **`07_ot_items_get.png`**
  - Mostrar: `GET http://localhost:8080/api/bff/ots/{id}/items` con rol `USER` o `ADMIN`.
  - Aceptación: HTTP `200`, array JSON y subtotal calculado por Oracle; la URL usa el BFF.

- [ ] **`08_ot_items_post.png`**
  - Mostrar: `POST http://localhost:8080/api/bff/ots/{id}/items` con token válido y body de ítem.
  - Aceptación: HTTP `201`, `id`, `otId`, `concepto`, `cantidad`, `precioUnit`, `subtotal` y `createdAt`.

- [ ] **`09_angular_inicio.png`**
  - Mostrar: inicio en `http://localhost:4200` con marca Pedidos360 y flujo de arquitectura.
  - Aceptación: vista cargada, navegación legible y sin errores visibles.

- [ ] **`10_login_entra.png`**
  - Mostrar: pantalla oficial de Microsoft iniciada desde el botón de Pedidos360.
  - Aceptación: tenant y aplicación esperados, sin exponer contraseña ni correo personal completo.

- [ ] **`11_usuario_autenticado.png`**
  - Mostrar: inicio después del login, nombre anonimizado y sesión activa.
  - Aceptación: navegación protegida disponible y roles/scopes visibles si fueron emitidos.

- [ ] **`12_jwt_obtenido.png`**
  - Mostrar: DevTools Network con una llamada al BFF y presencia del header `Authorization`.
  - Aceptación: se demuestra `Bearer`, pero todo el valor del token está cubierto.

- [ ] **`13_claims_jwt.png`**
  - Mostrar: payload decodificado localmente con `iss`, `aud`, `exp`, `scp` y `roles`.
  - Aceptación: issuer/audience corresponden a la API, token vigente, scope `access_as_user` y rol esperado.

- [ ] **`14_msal_interceptor.png`**
  - Mostrar: código de `MsalInterceptor`/`protectedResourceMap` junto a Network con llamada `/api/bff/...`.
  - Aceptación: queda claro que el token se adjunta automáticamente solo al recurso configurado.

- [ ] **`15_api_gateway.png`**
  - Mostrar: consola de la HTTP API con nombre, protocolo, stage y URL de invocación.
  - Aceptación: recurso real desplegado y asociado a la integración del BFF.

- [ ] **`16_api_gateway_routes.png`**
  - Mostrar: tabla de rutas `GET/POST/PUT/DELETE /api/bff/...` y sus integraciones.
  - Aceptación: están OT, ítems, eventos, notificaciones y health; las rutas protegidas tienen authorizer.

- [ ] **`17_api_gateway_cors.png`**
  - Mostrar: CORS del HTTP API.
  - Aceptación: origen exacto del frontend, headers `Authorization`/`Content-Type`, métodos requeridos y sin `*`.

- [ ] **`18_jwt_authorizer.png`**
  - Mostrar: JWT authorizer con identity source, issuer y audience parcialmente anonimizados.
  - Aceptación: issuer termina en el tenant `/v2.0` y audience coincide con el claim `aud` real.

- [ ] **`19_peticion_sin_token_401.png`**
  - Mostrar: petición a una ruta protegida de API Gateway sin `Authorization`.
  - Aceptación: HTTP `401`; health sigue siendo público.

- [ ] **`20_peticion_token_valido_200.png`**
  - Mostrar: misma API con token válido oculto, idealmente `GET /api/bff/ots`.
  - Aceptación: HTTP `200` y JSON esperado.

- [ ] **`21_peticion_sin_rol_403.png`**
  - Mostrar: `USER` ejecutando `GET /api/bff/events`, con token oculto.
  - Aceptación: HTTP `403` del BFF y mensaje de rol insuficiente.

- [ ] **`22_frontend_ots.png`**
  - Mostrar: vista protegida de OT con los datos cargados.
  - Aceptación: se observan OT e ítems reales y Network apunta a API Gateway/BFF.

- [ ] **`23_frontend_audit.png`**
  - Mostrar: vista protegida de eventos y notificaciones con rol `ADMIN`.
  - Aceptación: se ven payloads de auditoría reales; Network apunta a API Gateway/BFF.

- [ ] **`24_ec2_running.png`**
  - Mostrar: infraestructura cloud real cuando sea definida, con checks aprobados y servicios Java/Oracle activos.
  - Aceptación: instancia real con `2/2 checks`; no se muestran claves, user data ni variables sensibles.

- [ ] **`25_backend_ec2_health.png`**
  - Mostrar: `GET <URL_API_GATEWAY>/api/bff/health` desde fuera de EC2.
  - Aceptación: HTTPS, HTTP `200` y JSON `status: UP`.

## Evidencias adicionales recomendadas

- [ ] **`26_backend_tests.png`**: detalle de las 14 pruebas y cero fallos.
- [ ] **`27_frontend_build_tests.png`**: `npx --yes npm@11.6.2 run build` y `npx --yes npm@11.6.2 run test:ci` exitosos.
- [ ] **`28_oracle_local.png`**: SQL*Plus muestra `XEPDB1`, dos OT, cuatro ítems y triggers habilitados.
- [ ] **`29_cors_preflight_local.png`**: `OPTIONS` local devuelve el origen exacto `http://localhost:4200`.
- [ ] **`30_entra_scope_roles.png`**: scope `access_as_user`, app roles `USER`/`ADMIN` y asignaciones.
- [ ] **`31_arquitectura.png`**: diagrama Angular -> Entra/API Gateway -> BFF -> microservicios -> Oracle.

## Control final

- [ ] Existen las 25 capturas obligatorias con nombres exactos y sin saltos.
- [ ] Los resultados pertenecen a ejecuciones y recursos reales.
- [ ] Ninguna imagen expone tokens, secretos, credenciales o datos personales.
- [ ] Cada evidencia muestra el criterio técnico que pretende demostrar.
