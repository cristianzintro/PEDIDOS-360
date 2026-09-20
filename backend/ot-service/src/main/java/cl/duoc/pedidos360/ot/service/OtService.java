package cl.duoc.pedidos360.ot.service;

import cl.duoc.pedidos360.ot.dto.OtItemRequest;
import cl.duoc.pedidos360.ot.dto.OtRequest;
import cl.duoc.pedidos360.ot.exception.ResourceNotFoundException;
import cl.duoc.pedidos360.ot.model.Ot;
import cl.duoc.pedidos360.ot.model.OtItem;
import cl.duoc.pedidos360.ot.repository.OtItemRepository;
import cl.duoc.pedidos360.ot.repository.OtRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OtService {

    private final OtRepository otRepository;
    private final OtItemRepository itemRepository;

    public OtService(OtRepository otRepository, OtItemRepository itemRepository) {
        this.otRepository = otRepository;
        this.itemRepository = itemRepository;
    }

    public List<Ot> findAll() {
        return otRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Ot findById(String id) {
        return otRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + id));
    }

    @Transactional
    public Ot create(OtRequest request) {
        BigDecimal total = request.total() == null ? BigDecimal.ZERO : request.total();
        Ot ot = new Ot(otRepository.nextOtId(), request.clienteId(), request.patente(),
                request.descripcion(), total);
        return otRepository.saveAndFlush(ot);
    }

    public List<OtItem> findItems(String otId) {
        requireOt(otId);
        return itemRepository.findByOtIdOrderByIdAsc(otId);
    }

    @Transactional
    public OtItem createItem(String otId, OtItemRequest request) {
        requireOt(otId);
        OtItem item = new OtItem(otId, request.concepto(), request.cantidad(), request.precioUnit());
        return itemRepository.saveAndFlush(item);
    }

    private void requireOt(String otId) {
        if (!otRepository.existsById(otId)) {
            throw new ResourceNotFoundException("OT no encontrada: " + otId);
        }
    }
}
