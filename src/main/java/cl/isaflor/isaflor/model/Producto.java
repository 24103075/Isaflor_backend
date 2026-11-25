package cl.isaflor.isaflor.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @NotBlank(message = "El codigo del producto es obligatorio")
    @Size(min = 2, max = 20)
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigoProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombreProducto;

    @NotBlank(message = "La descripcion del producto es obligatoria")
    @Size(min = 2, max = 255)
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser positivo")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioProducto;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stockProducto;

    @NotNull(message = "El stock critico es obligatorio")
    @Min(value = 0, message = "El stock critico no puede ser negativo")
    @Column(name = "stock_critico", nullable = false)
    private Integer stockCritico;

    @NotNull(message = "El peso del producto es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser positivo")
    @Column(name = "peso_normal", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoProducto;

    @NotNull(message = "El peso volumetrico es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso volumetrico debe ser positivo")
    @Column(name = "peso_volumetrico", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoVolumetrico;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    
}
