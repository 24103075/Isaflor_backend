package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearDetallePedidoRequest {

    @NotNull(message = "El pedido es obligatorio")
    @Positive(message = "El ID del pedido debe ser positivo")
    private Integer idPedido;

    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Integer idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como minimo 1")
    private Integer cantidad;
}
