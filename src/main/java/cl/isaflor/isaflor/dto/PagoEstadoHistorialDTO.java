package cl.isaflor.isaflor.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PagoEstadoHistorialDTO {

    private Integer idHistorialPago;
    private String estadoAnterior;
    private String estadoNuevo;
    private String correoActor;
    private LocalDateTime fechaCambio;
    private String motivo;
}
