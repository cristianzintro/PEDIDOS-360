# Pedidos360

Pedidos360 gestiona ordenes de trabajo de un taller sobre el esquema Oracle oficial `TALLERPRO360`. Conserva la SPA Angular, autenticacion Microsoft Entra ID, BFF seguro y microservicios Spring Boot.

## Arquitectura local

```text
Angular :4200
      |
      | Bearer access token
      v
BFF Spring Boot :8080
      |-----------------------------------|
      v                                   v
OT Service :8081                         Audit Notification Service :8082
OT / OT_ITEM                             OT_EVENT / NOTIFY_LOG
      |                                   |
      `------------- Oracle JDBC ---------'
                       |
              Oracle Database 21c XE
              localhost:1521/XEPDB1
              esquema TALLERPRO360
```

Los servicios `8081` y `8082` escuchan solo en `127.0.0.1`. El navegador consume exclusivamente rutas `/api/bff/**`.

## Tecnologias

| Componente | Tecnologia |
|---|---|
| Frontend | Angular 21, TypeScript, RxJS |
| Identidad | Microsoft Entra ID, MSAL Angular 6, OAuth 2.0/OIDC con PKCE |
| Seguridad backend | Spring Security OAuth2 Resource Server, JWT |
| Backend | Java 17, Spring Boot 3.5, Maven multimodulo |
| Persistencia | Spring Data JPA, Oracle JDBC, Oracle Database 21c XE |
| Pruebas | JUnit 5, MockMvc, H2 en modo Oracle, Vitest |

## Estructura

```text
PEDIDOS-360/
|-- backend/
|   |-- ot-service/
|   |-- audit-notification-service/
|   |-- bff-service/
|   |-- .mvn/
|   |-- mvnw
|   |-- mvnw.cmd
|   `-- pom.xml
|-- database/
|   |-- README.md
|   `-- patch_ot_item_trigger.sql
|-- frontend/pedidos360-web/
|-- docs/
|-- .env.example
`-- README.md
```

El `Script.sql` original del docente se conserva sin modificaciones en `../Script.sql`, fuera de la raiz de esta aplicacion.

## Requisitos

- Java 17.
- Oracle Database 21c XE iniciado.
- PDB `XEPDB1` disponible en `localhost:1521/XEPDB1`.
- Esquema `TALLERPRO360` creado mediante el `Script.sql` oficial.
- Node.js compatible con Angular 21 y npm 11.6.2.
- Tenant de Microsoft Entra ID solo cuando se pruebe login con tokens reales.

Maven global no es necesario: el repositorio incluye Maven Wrapper 3.9.11.

## Configuracion Oracle

No incluya la contrasena real en el repositorio. Configure estas variables en las terminales donde ejecute `ot-service` y `audit-notification-service`:

```dotenv
DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
DB_USERNAME=TALLERPRO360
DB_PASSWORD=<CONTRASENA_ORACLE_LOCAL>
```

PowerShell:

```powershell
$env:DB_URL="jdbc:oracle:thin:@localhost:1521/XEPDB1"
$env:DB_USERNAME="TALLERPRO360"
$env:DB_PASSWORD="<CONTRASENA_ORACLE_LOCAL>"
```

Bash:

```bash
export DB_URL='jdbc:oracle:thin:@localhost:1521/XEPDB1'
export DB_USERNAME='TALLERPRO360'
export DB_PASSWORD='<CONTRASENA_ORACLE_LOCAL>'
```

Los dos servicios usan `spring.jpa.hibernate.ddl-auto=validate`. Hibernate valida el mapping, pero no crea, actualiza ni elimina objetos del esquema docente.

Consulte [database/README.md](database/README.md) para ejecutar el script oficial, aplicar el trigger complementario y verificar datos.

## Ejecutar

Use terminales separadas desde `backend`.

OT Service:

```powershell
.\mvnw.cmd -pl ot-service spring-boot:run
```

Audit Notification Service:

```powershell
.\mvnw.cmd -pl audit-notification-service spring-boot:run
```

BFF, despues de configurar las variables de Entra ID descritas en [docs/ENTRA_ID_SETUP.md](docs/ENTRA_ID_SETUP.md):

```powershell
.\mvnw.cmd -pl bff-service spring-boot:run
```

El BFF puede iniciar con placeholders para comprobar health y `401`, pero solo acepta tokens reales cuando issuer, JWKS y audience coinciden con Entra ID.

Frontend, desde `frontend/pedidos360-web`:

```bash
npx --yes npm@11.6.2 ci
npx --yes npm@11.6.2 start
```

## Health

| Servicio | URL |
|---|---|
| OT Service | `http://localhost:8081/actuator/health` |
| Audit Notification Service | `http://localhost:8082/actuator/health` |
| BFF | `http://localhost:8080/api/bff/health` |

## API interna

### OT Service

| Metodo | Ruta | Resultado |
|---|---|---|
| GET | `/api/ots` | Lista OT |
| GET | `/api/ots/{id}` | Obtiene una OT |
| POST | `/api/ots` | Crea una OT |
| GET | `/api/ots/{id}/items` | Lista items de la OT |
| POST | `/api/ots/{id}/items` | Agrega un item |

### Audit Notification Service

| Metodo | Ruta | Resultado |
|---|---|---|
| GET | `/api/events` | Lista eventos |
| GET | `/api/events/{id}` | Obtiene un evento |
| GET | `/api/ots/{otId}/events` | Eventos de una OT |
| GET | `/api/notifications` | Lista notificaciones |
| GET | `/api/notifications/{id}` | Obtiene una notificacion |
| GET | `/api/ots/{otId}/notifications` | Notificaciones de una OT |

## API del BFF

| Metodo | Ruta | Autorizacion |
|---|---|---|
| GET | `/api/bff/health` | Publico |
| GET | `/api/bff/me` | Scope `access_as_user` |
| GET | `/api/bff/ots/**` | Scope + `USER` o `ADMIN` |
| POST | `/api/bff/ots` | Scope + `USER` o `ADMIN` |
| POST | `/api/bff/ots/{id}/items` | Scope + `USER` o `ADMIN` |
| GET | `/api/bff/events/**` | Scope + `ADMIN` |
| GET | `/api/bff/notifications/**` | Scope + `ADMIN` |

Otros metodos o rutas se rechazan mediante `denyAll()`.

## Modelo Oracle

- `OT`: cabecera de la orden de trabajo. `BIU_OT` genera el identificador con formato `OT-YYYY-NNNNNN`.
- `OT_ITEM`: detalle de la OT. `SUBTOTAL` es una columna virtual y nunca se escribe desde JPA.
- `OT_EVENT`: auditoria de eventos.
- `NOTIFY_LOG`: registro de notificaciones.
- `V_OT_RESUMEN`: vista oficial de resumen.

JPA obtiene previamente `SEQ_OT` para satisfacer el requisito de identificador asignado de Hibernate, usando exactamente el formato del trigger. Los IDs numericos usan las secuencias oficiales; los triggers siguen actuando como respaldo para inserts externos con ID nulo.

## Pruebas

Backend completo:

```powershell
cd backend
.\mvnw.cmd clean verify
```

La suite contiene 14 pruebas:

- OT Service: listado, alta y subtotal virtual de items.
- Audit Notification Service: consultas de eventos y notificaciones.
- BFF: health, `401`, `403`, scope, roles, metodos permitidos, CORS, claims y audience.

Los tests persistentes usan H2 en modo Oracle con DDL equivalente y no alteran Oracle local.

Frontend:

```bash
cd frontend/pedidos360-web
npx --yes npm@11.6.2 run build
npx --yes npm@11.6.2 run test:ci
```

## Alcance pendiente

El despliegue AWS, API Gateway y la configuracion real de Entra ID se retoman despues de cerrar esta migracion local. No se crean recursos cloud desde esta etapa.
