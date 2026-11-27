package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PedidoDTO {

    private Integer idPedido;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal total;
    private Integer idUsuario;
    private Integer idMetodoEnvio;
    private Integer idTipoDocumento;
}
