
package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OfertaDTO {

    @NotNull(message = "Debe indicar el estado de la oferta")
    private Boolean enOferta;

    @DecimalMin(value = "0.01",
            message = "El precio de oferta debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2,
            message = "El precio de oferta tiene un formato invalido")
    private BigDecimal precioOferta;

}
