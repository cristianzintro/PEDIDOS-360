package cl.duoc.pedidos360.ot.repository;

import cl.duoc.pedidos360.ot.model.OtItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtItemRepository extends JpaRepository<OtItem, Long> {

    List<OtItem> findByOtIdOrderByIdAsc(String otId);
}
