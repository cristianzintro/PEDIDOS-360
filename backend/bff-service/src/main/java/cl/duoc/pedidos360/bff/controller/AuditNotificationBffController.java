package cl.duoc.pedidos360.bff.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/bff")
public class AuditNotificationBffController {

    private final RestClient auditClient;

    public AuditNotificationBffController(@Qualifier("auditRestClient") RestClient auditClient) {
        this.auditClient = auditClient;
    }

    @GetMapping("/events")
    public JsonNode findEvents() {
        return auditClient.get().uri("/api/events").retrieve().body(JsonNode.class);
    }

    @GetMapping("/events/{id}")
    public JsonNode findEvent(@PathVariable Long id) {
        return auditClient.get().uri("/api/events/{id}", id).retrieve().body(JsonNode.class);
    }

    @GetMapping("/events/ot/{otId}")
    public JsonNode findEventsByOt(@PathVariable String otId) {
        return auditClient.get().uri("/api/ots/{otId}/events", otId).retrieve().body(JsonNode.class);
    }

    @GetMapping("/notifications")
    public JsonNode findNotifications() {
        return auditClient.get().uri("/api/notifications").retrieve().body(JsonNode.class);
    }

    @GetMapping("/notifications/{id}")
    public JsonNode findNotification(@PathVariable Long id) {
        return auditClient.get().uri("/api/notifications/{id}", id).retrieve().body(JsonNode.class);
    }

    @GetMapping("/notifications/ot/{otId}")
    public JsonNode findNotificationsByOt(@PathVariable String otId) {
        return auditClient.get().uri("/api/ots/{otId}/notifications", otId)
                .retrieve().body(JsonNode.class);
    }
}
