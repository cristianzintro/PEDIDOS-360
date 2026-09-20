package cl.duoc.pedidos360.ot.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OtItemRequest(
        @NotBlank @Size(max = 40) String concepto,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 2)
        BigDecimal cantidad,
        @NotNull @DecimalMin(value = "0", inclusive = true) @Digits(integer = 12, fraction = 0)
        BigDecimal precioUnit
) {
}
