package cl.duoc.pedidos360.ot.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OtRequest(
        @NotBlank @Size(max = 20) String clienteId,
        @NotBlank @Size(max = 10) String patente,
        @Size(max = 200) String descripcion,
        @DecimalMin(value = "0", inclusive = true) @Digits(integer = 12, fraction = 0) BigDecimal total
) {
}
