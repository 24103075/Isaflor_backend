package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearPagoRequest {

    @NotNull(message = "El pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer idPedido;

    @NotNull(message = "El metodo de pago es obligatorio")
    @Positive(message = "El ID del metodo de pago debe ser positivo")
    private Integer idMetodoPago;
}
