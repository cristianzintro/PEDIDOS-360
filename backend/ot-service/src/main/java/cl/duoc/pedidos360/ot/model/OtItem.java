package cl.duoc.pedidos360.ot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "OT_ITEM")
public class OtItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ot_item_sequence")
    @SequenceGenerator(name = "ot_item_sequence", sequenceName = "SEQ_OT_ITEM", allocationSize = 1)
    @Column(name = "ITEM_ID", nullable = false)
    private Long id;

    @Column(name = "OT_ID", nullable = false, length = 24)
    private String otId;

    @Column(name = "CONCEPTO", nullable = false, length = 40)
    private String concepto;

    @Column(name = "CANTIDAD", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "PRECIO_UNIT", nullable = false, precision = 12, scale = 0)
    private BigDecimal precioUnit;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "SUBTOTAL", precision = 12, scale = 0, insertable = false, updatable = false)
    private BigDecimal subtotal;

    @Generated(event = EventType.INSERT)
    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected OtItem() {
    }

    public OtItem(String otId, String concepto, BigDecimal cantidad, BigDecimal precioUnit) {
        this.otId = otId;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioUnit = precioUnit;
    }

    public Long getId() {
        return id;
    }

    public String getOtId() {
        return otId;
    }

    public String getConcepto() {
        return concepto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnit() {
        return precioUnit;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
