package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data 
public class ProductoDTO {

    private Integer idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String descripcion;
    private BigDecimal precioProducto;
    private Integer stockProducto;
    private Integer stockCritico;
    private BigDecimal pesoProducto;
    private BigDecimal pesoVolumetrico;
    private Integer idCategoria;
    private String nombreCategoria;

    
}
