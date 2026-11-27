package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarEstadoPedidoRequest {

    @NotBlank(message = "El estado del pedido es obligatorio")
    @Size(max = 30, message = "El estado del pedido no puede superar los 30 caracteres")
    private String estado;
}
