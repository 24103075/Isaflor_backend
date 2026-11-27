package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetallePedidoDTO {

    private Integer idDetalle;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subTotal;
    private Integer idPedido;
    private Integer idProducto;
    private String nombreProducto;
}
