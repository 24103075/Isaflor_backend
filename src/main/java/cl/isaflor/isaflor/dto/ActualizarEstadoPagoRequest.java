package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarEstadoPagoRequest {

    @NotBlank(message = "El estado del pago es obligatorio")
    @Size(max = 30, message = "El estado del pago no puede superar los 30 caracteres")
    private String estadoPago;

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;
}
