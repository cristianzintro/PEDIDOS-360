package cl.duoc.pedidos360.audit.repository;

import cl.duoc.pedidos360.audit.model.OtEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtEventRepository extends JpaRepository<OtEvent, Long> {

    List<OtEvent> findByOtIdOrderByCreatedAtDesc(String otId);
}
