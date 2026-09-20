package cl.duoc.pedidos360.ot.repository;

import cl.duoc.pedidos360.ot.model.Ot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OtRepository extends JpaRepository<Ot, String> {

    @Query(value = "SELECT 'OT-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYY') || '-' || "
            + "LPAD(SEQ_OT.NEXTVAL, 6, '0') FROM DUAL", nativeQuery = true)
    String nextOtId();
}
