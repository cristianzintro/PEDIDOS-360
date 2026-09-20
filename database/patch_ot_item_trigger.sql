--------------------------------------------------------------------------------
-- Complemento al Script.sql original del docente.
-- El script crea SEQ_OT_ITEM, pero no incluye el trigger que la consume.
--------------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER BI_OT_ITEM
BEFORE INSERT ON OT_ITEM
FOR EACH ROW
BEGIN
  IF :NEW.ITEM_ID IS NULL THEN
    :NEW.ITEM_ID := SEQ_OT_ITEM.NEXTVAL;
  END IF;
END;
/
SHOW ERRORS
