package cl.duoc.pedidos360.audit.repository;

import cl.duoc.pedidos360.audit.model.NotifyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifyLogRepository extends JpaRepository<NotifyLog, Long> {

    List<NotifyLog> findByOtIdOrderByCreatedAtDesc(String otId);
}
