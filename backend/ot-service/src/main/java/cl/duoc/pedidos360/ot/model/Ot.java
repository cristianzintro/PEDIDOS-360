package cl.duoc.pedidos360.ot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OT")
public class Ot {

    @Id
    @Column(name = "OT_ID", nullable = false, length = 24)
    private String id;

    @Column(name = "CLIENTE_ID", nullable = false, length = 20)
    private String clienteId;

    @Column(name = "PATENTE", nullable = false, length = 10)
    private String patente;

    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;

    @Column(name = "TOTAL", precision = 12, scale = 0)
    private BigDecimal total;

    @Generated(event = EventType.INSERT)
    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Ot() {
    }

    public Ot(String id, String clienteId, String patente, String descripcion, BigDecimal total) {
        this.id = id;
        this.clienteId = clienteId;
        this.patente = patente;
        this.descripcion = descripcion;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getPatente() {
        return patente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
