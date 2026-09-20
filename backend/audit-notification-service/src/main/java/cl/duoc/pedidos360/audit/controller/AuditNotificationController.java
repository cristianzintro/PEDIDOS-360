package cl.duoc.pedidos360.audit.controller;

import cl.duoc.pedidos360.audit.model.NotifyLog;
import cl.duoc.pedidos360.audit.model.OtEvent;
import cl.duoc.pedidos360.audit.service.AuditNotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuditNotificationController {

    private final AuditNotificationService service;

    public AuditNotificationController(AuditNotificationService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public List<OtEvent> findEvents() {
        return service.findEvents();
    }

    @GetMapping("/events/{id}")
    public OtEvent findEvent(@PathVariable Long id) {
        return service.findEvent(id);
    }

    @GetMapping("/ots/{otId}/events")
    public List<OtEvent> findEventsByOt(@PathVariable String otId) {
        return service.findEventsByOt(otId);
    }

    @GetMapping("/notifications")
    public List<NotifyLog> findNotifications() {
        return service.findNotifications();
    }

    @GetMapping("/notifications/{id}")
    public NotifyLog findNotification(@PathVariable Long id) {
        return service.findNotification(id);
    }

    @GetMapping("/ots/{otId}/notifications")
    public List<NotifyLog> findNotificationsByOt(@PathVariable String otId) {
        return service.findNotificationsByOt(otId);
    }
}
