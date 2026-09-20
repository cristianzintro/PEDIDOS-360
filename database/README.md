# Base de datos Oracle de Pedidos360

## Archivos

- `Script.sql` es el script original entregado por el docente y es la fuente de verdad del esquema `TALLERPRO360`. En este workspace se conserva sin modificaciones en `../../Script.sql`.
- `patch_ot_item_trigger.sql` complementa el script original. Agrega `BI_OT_ITEM`, que usa `SEQ_OT_ITEM` cuando un insert no entrega `ITEM_ID`.

El script original contiene la instruccion de creacion del usuario. Revise su contrasena antes de ejecutarlo y no copie credenciales reales al repositorio ni a capturas.

## Ejecucion inicial

Abra SQL*Plus con privilegios administrativos, seleccione la PDB y ejecute el script docente:

```sql
sqlplus / as sysdba
ALTER SESSION SET CONTAINER = XEPDB1;
@../../Script.sql
```

Despues conecte el esquema de aplicacion usando la contrasena local, sin escribirla en archivos versionados, y aplique el parche:

```text
sqlplus TALLERPRO360@localhost:1521/XEPDB1
```

```sql
@patch_ot_item_trigger.sql
```

Si el esquema ya existe, no vuelva a ejecutar `Script.sql`: contiene creacion de objetos y datos de ejemplo. Aplicar `patch_ot_item_trigger.sql` es idempotente porque usa `CREATE OR REPLACE TRIGGER`.

## Verificacion

```sql
SELECT COUNT(*) AS OT_COUNT FROM OT;
SELECT COUNT(*) AS OT_ITEM_COUNT FROM OT_ITEM;
SELECT * FROM V_OT_RESUMEN ORDER BY CREATED_AT DESC;

SELECT TRIGGER_NAME, STATUS
FROM USER_TRIGGERS
WHERE TRIGGER_NAME IN ('BIU_OT', 'BI_OT_ITEM', 'BI_OT_EVENT', 'BI_NOTIFY_LOG')
ORDER BY TRIGGER_NAME;
```

Para el estado inicial proporcionado, los conteos esperados son dos registros en `OT` y cuatro en `OT_ITEM`.
