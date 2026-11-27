package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CrearPedidoRequest {

    @NotNull(message = "El metodo de envio es obligatorio")
    @Positive(message = "El ID del metodo de envio debe ser positivo")
    private Integer idMetodoEnvio;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Positive(message = "El ID del tipo de documento debe ser positivo")
    private Integer idTipoDocumento;
}
