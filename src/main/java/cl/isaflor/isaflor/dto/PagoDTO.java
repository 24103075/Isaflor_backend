package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PagoDTO {

    private Integer idPago;
    private LocalDateTime fechaPago;
    private BigDecimal monto;
    private String estadoPago;
    private Integer idPedido;
    private Integer idMetodoPago;
}
