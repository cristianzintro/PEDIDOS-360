package cl.duoc.pedidos360.ot.controller;

import cl.duoc.pedidos360.ot.dto.OtItemRequest;
import cl.duoc.pedidos360.ot.dto.OtRequest;
import cl.duoc.pedidos360.ot.model.Ot;
import cl.duoc.pedidos360.ot.model.OtItem;
import cl.duoc.pedidos360.ot.service.OtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ots")
public class OtController {

    private final OtService service;

    public OtController(OtService service) {
        this.service = service;
    }

    @GetMapping
    public List<Ot> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Ot findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Ot> create(@Valid @RequestBody OtRequest request) {
        Ot created = service.create(request);
        return ResponseEntity.created(URI.create("/api/ots/" + created.getId())).body(created);
    }

    @GetMapping("/{id}/items")
    public List<OtItem> findItems(@PathVariable String id) {
        return service.findItems(id);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OtItem> createItem(@PathVariable String id,
                                             @Valid @RequestBody OtItemRequest request) {
        OtItem created = service.createItem(id, request);
        return ResponseEntity.created(URI.create("/api/ots/" + id + "/items/" + created.getId()))
                .body(created);
    }
}
