package cl.duoc.pedidos360.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "OT_EVENT")
public class OtEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_log_sequence")
    @SequenceGenerator(name = "event_log_sequence", sequenceName = "SEQ_EVENT_LOG", allocationSize = 1)
    @Column(name = "EVENT_ID", nullable = false)
    private Long id;

    @Column(name = "OT_ID", length = 24)
    private String otId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 40)
    private String eventType;

    @Lob
    @Column(name = "PAYLOAD_JSON", nullable = false)
    private String payloadJson;

    @Generated(event = EventType.INSERT)
    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OtEvent() {
    }

    public OtEvent(String otId, String eventType, String payloadJson) {
        this.otId = otId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
    }

    public Long getId() {
        return id;
    }

    public String getOtId() {
        return otId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
