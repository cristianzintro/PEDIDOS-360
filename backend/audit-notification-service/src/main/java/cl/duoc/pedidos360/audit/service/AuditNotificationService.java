package cl.duoc.pedidos360.audit.service;

import cl.duoc.pedidos360.audit.exception.ResourceNotFoundException;
import cl.duoc.pedidos360.audit.model.NotifyLog;
import cl.duoc.pedidos360.audit.model.OtEvent;
import cl.duoc.pedidos360.audit.repository.NotifyLogRepository;
import cl.duoc.pedidos360.audit.repository.OtEventRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditNotificationService {

    private final OtEventRepository eventRepository;
    private final NotifyLogRepository notificationRepository;

    public AuditNotificationService(OtEventRepository eventRepository,
                                    NotifyLogRepository notificationRepository) {
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<OtEvent> findEvents() {
        return eventRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public OtEvent findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
    }

    public List<OtEvent> findEventsByOt(String otId) {
        return eventRepository.findByOtIdOrderByCreatedAtDesc(otId);
    }

    public List<NotifyLog> findNotifications() {
        return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public NotifyLog findNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada: " + id));
    }

    public List<NotifyLog> findNotificationsByOt(String otId) {
        return notificationRepository.findByOtIdOrderByCreatedAtDesc(otId);
    }
}
