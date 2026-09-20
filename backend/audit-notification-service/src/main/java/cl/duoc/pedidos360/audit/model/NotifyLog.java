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
@Table(name = "NOTIFY_LOG")
public class NotifyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notify_log_sequence")
    @SequenceGenerator(name = "notify_log_sequence", sequenceName = "SEQ_NOTIFY_LOG", allocationSize = 1)
    @Column(name = "LOG_ID", nullable = false)
    private Long id;

    @Column(name = "OT_ID", length = 24)
    private String otId;

    @Column(name = "CLIENTE_ID", length = 20)
    private String clienteId;

    @Column(name = "CANAL", length = 20)
    private String canal;

    @Lob
    @Column(name = "PAYLOAD_JSON", nullable = false)
    private String payloadJson;

    @Generated(event = EventType.INSERT)
    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NotifyLog() {
    }

    public NotifyLog(String otId, String clienteId, String canal, String payloadJson) {
        this.otId = otId;
        this.clienteId = clienteId;
        this.canal = canal;
        this.payloadJson = payloadJson;
    }

    public Long getId() {
        return id;
    }

    public String getOtId() {
        return otId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getCanal() {
        return canal;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
