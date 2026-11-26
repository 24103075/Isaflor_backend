
package cl.isaflor.isaflor.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductoDTO {

    private Integer idProducto;

    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 20)
    private String codigoProducto;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombreProducto;

    @Size(max = 255)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precioProducto;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stockProducto;

    @NotNull(message = "El stock critico es obligatorio")
    @PositiveOrZero(message = "El stock critico no puede ser negativo")
    private Integer stockCritico;

    @Positive(message = "El peso debe ser mayor a cero")
    private BigDecimal pesoProducto;

    @Positive(message = "El peso volumetrico debe ser mayor a cero")
    private BigDecimal pesoVolumetrico;

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "La categoria debe tener un ID valido")
    private Integer idCategoria;

    private String nombreCategoria;
}
